import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem
import java.nio.file.Files
import java.nio.file.Path

plugins {
    `cpp-library`
    id("de.undercouch.download") version "4.0.4"
}

apply {
    from("gradle/toolchains.gradle.kts")
}

// Download gulrak/filesystem to replace c++17's filesystem if not supported by the current system.
val downloadNativeDependencies by tasks.registering(Download::class) {
    src("https://github.com/gulrak/filesystem/releases/download/v1.3.2/filesystem.hpp")
    dest(temporaryDir)

    outputs.file(temporaryDir.resolve("filesystem.hpp"))
    outputs.dir(temporaryDir)
}

val downloadJniHeader by tasks.registering(Download::class) {
    src("https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/share/native/include/jni.h")
    dest(temporaryDir)
    outputs.dir(temporaryDir)
}

val downloadJniMdHeaderUnix by tasks.registering(Download::class) {
    src("https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/unix/native/include/jni_md.h")
    dest(temporaryDir)
    outputs.dir(temporaryDir)
}

val downloadJniMdHeaderWindows by tasks.registering(Download::class) {
    src("https://raw.githubusercontent.com/openjdk/jdk/master/src/java.base/windows/native/include/jni_md.h")
    dest(temporaryDir)
    outputs.dir(temporaryDir)
}

tasks.withType(CppCompile::class).configureEach {
    compilerArgs.addAll(toolChain.map { toolChain ->
        if (this@configureEach.name.toLowerCase().contains("windows")) {
            when (toolChain) {
                is VisualCpp -> listOf("/std:c++11")
                is Gcc -> listOf("-lstdc++", "-std=c++11", "-static-libgcc", "-static-libstdc++")
                is Clang -> listOf("-lstdc++", "-std=c++11", "-static-libstdc++")
                else -> listOf()
            }
        } else {
            when (toolChain) {
                is VisualCpp -> listOf("/std:c++11")
                is Gcc -> listOf("-lstdc++", "-std=c++11", "-static-libgcc", "-static-libstdc++", "-ldl")
                is Clang -> listOf("-lstdc++", "-std=c++11", "-static-libstdc++", "-ldl")
                else -> listOf()
            }
        }
    })

    // A little bit of a weird workaround because we can't resolve targetMachine here
    if (name.toLowerCase().contains("android")) {
        compilerArgs.add("-llog")
    }
}

tasks.withType(LinkSharedLibrary::class).configureEach {
    linkerArgs.addAll(toolChain.map { toolChain ->
        if (this@configureEach.name.toLowerCase().contains("windows")) {
            when (toolChain) {
                is Gcc -> listOf("-shared", "-static-libgcc", "-static-libstdc++")
                is Clang -> listOf("-shared", "-static-libstdc++")
                else -> listOf()
            }
        } else {
            when (toolChain) {
                is Gcc -> listOf("-shared", "-static-libgcc", "-static-libstdc++", "-ldl")
                is Clang -> listOf("-shared", "-static-libstdc++", "-ldl")
                else -> listOf()
            }
        }
    })

    // A little bit of a weird workaround because we can't resolve targetMachine here
    if (name.toLowerCase().contains("android")) {
        linkerArgs.add("-llog")
    }
}

library {
    targetMachines.set(listOf(
            machines.windows.x86, machines.windows.x86_64,
            machines.macOS.x86_64,
            machines.linux.x86, machines.linux.x86_64,
            machines.os("android").architecture("armv7a"),
            machines.os("android").architecture("arm64-v8a"),
            machines.os("android").x86,
            machines.os("android").x86_64))

    // Include JNI headers generated by devolay-java
    val headerOnly: Configuration by configurations.creating {}
    dependencies {
        headerOnly(project(":devolay-java", "jniIncludes"))
    }

    privateHeaders {
        from(headerOnly)

        // Include platform-independent JNI Path
        from(downloadJniHeader)

        // Include our downloaded headers
        from(downloadNativeDependencies)

        // Include NDI headers
        from(files(locateNdiIncludes()))
    }

    binaries.whenElementFinalized {
        if (this.targetMachine.operatingSystemFamily.isWindows) {
            this.compileTask.get().dependsOn(downloadJniMdHeaderWindows)
            this.compileTask.get().includes.from(downloadJniMdHeaderWindows)
        } else {
            this.compileTask.get().dependsOn(downloadJniMdHeaderUnix)
            this.compileTask.get().includes.from(downloadJniMdHeaderUnix)
        }
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
                if (this.isOptimized && !this.getName().toLowerCase().contains("debug")) {
                    from(this.outputs) {
                        into("natives/" + machine.operatingSystemFamily.name + "/" + machine.architecture.name)
                        exclude("*.lib")
                        exclude("*.debug")

                        if (machine.operatingSystemFamily.name == "android") {
                            rename { it.subSequence(0, it.lastIndexOf('.')).toString() + ".androidnative" }
                        }
                    }
                }
            }
        }
    }
}

val assembleIntegratedNDIArtifacts by tasks.registering(Jar::class) {
    archiveBaseName.set("ndi-lib-artifacts")
    destinationDirectory.set(temporaryDir)

    components.withType(ComponentWithBinaries::class).forEach { component ->
        (component as ComponentWithBinaries).binaries.whenElementFinalized(ComponentWithOutputs::class.java) {
            if (this is ComponentWithNativeRuntime && this.isOptimized) {
                val machine = this.targetMachine

                var nativeLibPath: Path? = null;
                val nativeLicensePaths: MutableList<Path> = mutableListOf();
                var nativeLibName: String? = null;

                if (machine.operatingSystemFamily.name == "android") {
                    nativeLibName = "libndi.androidnative"
                    nativeLicensePaths.add(file("../NDI SDK for Android/licenses/Bonjour.txt").toPath())
                    nativeLicensePaths.add(file("../NDI SDK for Android/licenses/libndi_licenses.txt").toPath())
                    when (machine.architecture.name) {
                        "armv7a" -> {
                            nativeLibPath = file("../NDI SDK for Android/lib/armeabi-v7a/libndi.so").toPath()
                        }
                        "arm64-v8a" -> {
                            nativeLibPath = file("../NDI SDK for Android/lib/arm64-v8a/libndi.so").toPath()
                        }
                        "x86" -> {
                            nativeLibPath = file("../NDI SDK for Android/lib/x86/libndi.so").toPath()
                        }
                        "x86-64" -> {
                            nativeLibPath = file("../NDI SDK for Android/lib/x86_64/libndi.so").toPath()
                        }
                    }
                } else if (machine.operatingSystemFamily.name == "windows") {
                    nativeLibName = "ndi.dll"
                    when (machine.architecture.name) {
                        "x86" -> {
                            nativeLibPath = file("../NDI SDK for Windows/Bin/x86/Processing.NDI.Lib.x86.dll").toPath()
                            nativeLicensePaths.add(file("../NDI SDK for Windows/Bin/x86/Processing.NDI.Lib.Licenses.txt").toPath())
                        }
                        "x86-64" -> {
                            nativeLibPath = file("../NDI SDK for Windows/Bin/x64/Processing.NDI.Lib.x64.dll").toPath()
                            nativeLicensePaths.add(file("../NDI SDK for Windows/Bin/x64/Processing.NDI.Lib.Licenses.txt").toPath())
                        }
                    }
                } else if (machine.operatingSystemFamily.name == "macos") {
                    nativeLibName = "libndi.dylib"
                    nativeLicensePaths.add(file("../NDI SDK for Apple/licenses/libndi_licenses.txt").toPath())
                    when (machine.architecture.name) {
                        "x86-64" -> {
                            nativeLibPath = file("../NDI SDK for Apple/lib/x64/libndi.4.dylib").toPath()
                        }
                    }
                } else if (machine.operatingSystemFamily.name == "linux") {
                    nativeLibName = "libndi.so"
                    nativeLicensePaths.add(file("../NDI SDK for Linux/licenses/libndi_licenses.txt").toPath())
                    // The linux libs have 2 symlinks and 1 regular file in the lib folder, find the regular file
                    var nativeLibParentPath: Path? = null;
                    when (machine.architecture.name) {
                        "x86" -> {
                            nativeLibParentPath = file("../NDI SDK for Linux/lib/i686-linux-gnu").toPath()
                        }
                        "x86-64" -> {
                            nativeLibParentPath = file("../NDI SDK for Linux/lib/x86_64-linux-gnu").toPath()
                        }
                    }
                    if (nativeLibParentPath != null) {
                        nativeLibPath = Files.walk(nativeLibParentPath).filter {
                            Files.isRegularFile(it) && Files.size(it) > 10 * 1000
                        }.findFirst().orElse(null)
                    }
                }

                if (nativeLibPath != null) {
                    if (Files.exists(nativeLibPath)) {
                        println("Adding NDI lib from " + nativeLibPath.toString() + " to integrated build.")
                        from(nativeLibPath!!) {
                            rename {
                                nativeLibName!!
                            }
                            into("natives/" + machine.operatingSystemFamily.name + "/" + machine.architecture.name)
                        }
                        nativeLicensePaths.forEach {
                            from(it) {
                                into("natives/" + machine.operatingSystemFamily.name + "/" + machine.architecture.name)
                            }
                        }
                    } else {
                        System.err.println("Could not find NDI lib in expected location (" + nativeLibPath.toString() + ") for OS \"" + machine.operatingSystemFamily.name + "\" and arch \"" + machine.architecture.name + "\". No integrated builds available.");
                    }
                } else {
                    System.err.println("No NDI path specified for OS \"" + machine.operatingSystemFamily.name + "\" and arch \"" + machine.architecture.name + "\", no integrated builds available.")
                }
            }
        }
    }
}

val nativeArtifacts: Configuration? by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

val integratedNdiArtifacts: Configuration? by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add("nativeArtifacts", assembleNativeArtifacts)
    add("integratedNdiArtifacts", assembleIntegratedNDIArtifacts)
}
