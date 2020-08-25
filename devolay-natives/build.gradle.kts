import org.gradle.internal.jvm.Jvm

import de.undercouch.gradle.tasks.download.Download

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

library {
    binaries.configureEach {
        val compileTask = compileTask.get()

        if (toolChain is VisualCpp) {
            compileTask.compilerArgs.add("/std:c++latest")
        } else if (toolChain is GccCompatibleToolChain) {
            compileTask.compilerArgs.add("-std=c++17")
            compileTask.compilerArgs.add("-lstdc++fs")

            compileTask.compilerArgs.add("-static")
        }
    }

    publicHeaders {
        // Include JNI Path
        file(Jvm.current().javaHome.toPath().resolve("include")).walk().forEach {
            if (it.isDirectory) {
                from(it)
            }
        }

        // Include our downloaded headers
        from(files(downloadedHeadersPath))

        // Include NDI headers
        from(files(file(System.getenv("NDI_SDK_DIR")).toPath().resolve("Include")))
    }
}
