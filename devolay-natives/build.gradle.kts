import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import java.nio.file.Files
import java.nio.file.Path

// For some reason, the compiler plugins (giving NativeToolChainRegistry the compiler factories) don't run
// until after the build script is evaluated, so we have to set up our toolchains as a part of a @Mutate rule.

open class ToolchainConfiguration : RuleSource() {
    @Mutate fun NativeToolChainRegistry.configureToolchains() {
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

            register<Clang>("osxcross") {
                target("macos_x86-64") {
                    getcCompiler().executable = "o64-clang"
                    cppCompiler.executable = "o64-clang++"
                    linker.executable = "o64-clang++"
                    assembler.executable = "o64-clang"
                }
            }
        }
    }
}
apply("plugin" to ToolchainConfiguration::class.java)


plugins {
    `cpp-library`
    id("de.undercouch.download") version "4.0.4"
}

// Download gulrak/filesystem to replace c++17"s filesystem if not supported by the current system.
val downloadedHeadersPath = buildDir.toPath().resolve("headers").toFile()
downloadedHeadersPath.mkdirs()

tasks.register<Download>("downloadNativeDependencies") {
    src("https://github.com/gulrak/filesystem/releases/download/v1.3.2/filesystem.hpp")
    dest(downloadedHeadersPath)
    overwrite(false)
}

tasks.assemble {
    dependsOn("downloadNativeDependencies")
    dependsOn(":devolay-java:generateJniHeaders")
}

tasks.withType(CppCompile::class).configureEach {
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
        from(files(downloadedHeadersPath))

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
