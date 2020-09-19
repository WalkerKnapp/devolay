import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import org.gradle.nativeplatform.toolchain.internal.tools.ToolSearchPath;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import java.nio.file.Files
import java.nio.file.Path

// For some reason, the compiler plugins (giving NativeToolChainRegistry the compiler factories) don't run
// until after the build script is evaluated, so we have to set up our toolchains as a part of a @Mutate rule.

open class ToolchainConfiguration : RuleSource() {
    @Mutate fun NativeToolChainRegistry.configureToolchains() {
        // On linux, compile using gcc, mingw, and osxcross
        if (OperatingSystem.current().isLinux) {
            register<Gcc>("gcc") {
                target("linux_x86-64")
                target("linux_x86")

                target("windows_x86-64") {
                    getcCompiler().executable = "x86_64-w64-mingw32-gcc"
                    cppCompiler.executable = "x86_64-w64-mingw32-g++"
                    linker.executable = "x86_64-w64-mingw32-g++"
                    staticLibArchiver.executable = "x86_64-w64-mingw32-ar"
                }

                target("windows_x86") {
                    getcCompiler().executable = "i686-w64-mingw32-gcc"
                    cppCompiler.executable = "i686-w64-mingw32-g++"
                    linker.executable = "i686-w64-mingw32-g++"
                    staticLibArchiver.executable = "i686-w64-mingw32-ar"
                }
            }

            // Gradle doesn't check that xcrun exists if there is a macos target specified, so do that check and only
            // register the target if it exists
            if (ToolSearchPath(OperatingSystem.current()).locate(ToolType.C_COMPILER, "xcrun").isAvailable) {
                register<Clang>("osxcross") {
                    target("macos_x86-64") {
                        this as org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain
                        getcCompiler().executable = "o64-clang"
                        cppCompiler.executable = "o64-clang++"
                        linker.executable = "o64-clang++"
                        assembler.executable = "o64-clang"
                        symbolExtractor.executable = "x86_64-apple-darwin19-dsymutil"
                        stripper.executable = "x86_64-apple-darwin19-strip"
                    }
                }
            } else {
                println("Osxcross is not present, these builds will not be compatible with Macos.")
            }
        }
    }
}
apply("plugin" to ToolchainConfiguration::class.java)


plugins {
    `cpp-library`
    id("de.undercouch.download") version "4.0.4"
}

// Download gulrak/filesystem to replace c++17's filesystem if not supported by the current system.
val downloadNativeDependencies by tasks.registering(Download::class) {
    src("https://github.com/gulrak/filesystem/releases/download/v1.3.2/filesystem.hpp")
    dest(temporaryDir)
    overwrite(false)

    outputs.dir(temporaryDir)
}

tasks.withType(CppCompile::class).configureEach {
    dependsOn(":devolay-java:generateJniHeaders")

    compilerArgs.addAll(toolChain.map { toolChain ->
        when (toolChain) {
            is VisualCpp -> listOf("/std:c++latest")
            is GccCompatibleToolChain -> listOf("-lstdc++", "-std=c++17", "-lstdc++fs", "-static")
            else -> listOf()
        }
    })
}

library {
    targetMachines.set(listOf(machines.windows.x86, machines.windows.x86_64,
            machines.macOS.x86_64,
            machines.linux.x86, machines.linux.x86_64))

    privateHeaders {
        // Include default headers
        from(files("src/main/headers"))

        // Include JNI Path
        file(Jvm.current().javaHome.toPath().resolve("include")).walk().forEach {
            if (it.isDirectory) {
                from(it)
            }
        }

        // Include our downloaded headers
        from(downloadNativeDependencies)

        // Include NDI headers
        from(files(locateNdiIncludes()))
    }
}

fun locateNdiIncludes(): Path {
    // Check system property
    var ndiSdk = if (System.getProperty("ndiSdk") != null) file(System.getProperty("ndiSdk")).toPath() else null

    // Check "NDI_SDK_DIR" environment variable
    if (ndiSdk == null && System.getenv("NDI_SDK_DIR") != null) {
        ndiSdk = file(System.getenv("NDI_SDK_DIR")).toPath()
    }

    // Check typical install locations
    if (ndiSdk == null && OperatingSystem.current().isWindows && file("C:/Program Files/NDI 4 SDK").exists()) {
        ndiSdk = file("C:/Program Files/NDI 4 SDK").toPath()
    }
    if (ndiSdk == null && OperatingSystem.current().isMacOsX && file("/Library/NDI SDK for Apple").exists()) {
        ndiSdk = file("/Library/NDI SDK for Apple").toPath()
    }

    // Check the working directory
    if (ndiSdk == null && file("../NDI SDK for Linux").exists()) {
        ndiSdk = file("../NDI SDK for Linux").toPath()
    }
    if (ndiSdk == null && file("../NDI SDK for Apple").exists()) {
        ndiSdk = file("../NDI SDK for Apple").toPath()
    }
    if (ndiSdk == null && file("../NDI 4 SDK").exists()) {
        ndiSdk = file("NDI 4 SDK").toPath()
    }

    if (ndiSdk == null) {
        throw IllegalStateException("No NDI SDK found. Please set the NDI_SDK_DIR variable to the install location, run gradle with -DndiSdk=<Install Path>, or symlink the install path to your \"devolay\" folder.")
    }

    return when {
        Files.exists(ndiSdk.resolve("include")) -> {
            ndiSdk.resolve("include")
        }
        Files.exists(ndiSdk.resolve("Include")) -> {
            ndiSdk.resolve("Include")
        }
        else -> {
            throw IllegalStateException("NDI SDK at $ndiSdk is invalid: Has no 'include' or 'Include' subdirectory.")
        }
    }
}

// Add artifacts for devolay-java to depend on
val assembleNativeArtifacts by tasks.registering(Jar::class) {
    archiveBaseName.set("devolay-native-artifacts")
    destinationDirectory.set(temporaryDir)

    components.withType(ComponentWithBinaries::class).forEach { component ->
        (component as ComponentWithBinaries).binaries.whenElementFinalized(ComponentWithOutputs::class.java) {
            if (this is ComponentWithNativeRuntime) {
                val machine = this.targetMachine

                // Only include release binaries
                if (this.isOptimized) {
                    dependsOn(this.outputs)
                    from(this.outputs) {
                        into("natives/" + machine.operatingSystemFamily.name + "/" + machine.architecture.name)
                    }
                }
            }
        }
    }
}

val nativeArtifacts: Configuration? by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add("nativeArtifacts", assembleNativeArtifacts)
}
