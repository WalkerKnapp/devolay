package me.walkerknapp.devolay;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

public class Devolay {

    private static final AtomicBoolean librariesLoaded = new AtomicBoolean(false);
    private static String extractedNdiLibraryPath = null;

    static {
        String devolayLibraryName = System.mapLibraryName("devolay-natives");
        String ndiLibraryName = System.mapLibraryName("ndi");
        String libraryExtension = devolayLibraryName.substring(devolayLibraryName.indexOf('.'));

        String osDirectory = getOsDirectory();
        String archDirectory = getArchDirectory();

        if (!osDirectory.equals("android")) {
            Path devolayNativesPath = extractNative("devolay-natives", libraryExtension,
                    "/natives/" + osDirectory + "/" + archDirectory + "/" + devolayLibraryName);
            Path ndiLibraryPath = extractNative("ndi", libraryExtension,
                    "/natives/" + osDirectory + "/" + archDirectory + "/" + ndiLibraryName);

            if (devolayNativesPath == null) {
                throw new IllegalStateException("This build of Devolay is not compiled for your OS. Please use a different build or follow the compilation instructions on https://github.com/WalkerKnapp/devolay.");
            }

            if (ndiLibraryPath != null) {
                extractedNdiLibraryPath = ndiLibraryPath.toAbsolutePath().toString();
            }

            System.load(devolayNativesPath.toAbsolutePath().toString());
        } else {
            // Devolay on Android should be loaded as an aar, so natives don't have to be extracted.
            System.loadLibrary("devolay-natives");
            extractedNdiLibraryPath = findLibrary("ndi");
        }

        try {
            int ret = loadLibraries();
            if (ret != 0) {
                // The libraries are not correctly installed.
                if (ret == -1) {
                    throw new IllegalStateException("The NDI(tm) SDK libraries were not found.");
                } else if (ret == -2) {
                    throw new IllegalStateException("The NDI(tm) SDK libraries failed to load. Please reinstall.");
                }
            }
        } catch (UnsatisfiedLinkError e) {
            if (osDirectory.equals("android")) {
                throw new IllegalStateException("Devolay natives failed to load correctly." +
                        " Please ensure that you are using the android-specific builds!" +
                        " See https://github.com/WalkerKnapp/devolay#android-builds.", e);
            } else {
                throw new IllegalStateException("Devolay natives failed to load correctly. This is likely because this build of Devolay is not compiled for your OS." +
                        " Please use a different build or follow the compilation instructions on https://github.com/WalkerKnapp/devolay.", e);
            }
        }
    }

    private static String getOsDirectory() {
        final String osNameProperty = System.getProperty("os.name").toLowerCase();
        final String javaRuntimeProperty = System.getProperty("java.runtime.name");
        if (javaRuntimeProperty != null && javaRuntimeProperty.toLowerCase().contains("android")) {
            return "android";
        } else if (osNameProperty.contains("nix") || osNameProperty.contains("nux")) {
            return "linux";
        } else if (osNameProperty.contains("win")) {
            return "windows";
        } else if (osNameProperty.contains("mac")) {
            return "macos";
        } else {
            throw new IllegalStateException("Unsupported OS: " + osNameProperty + ". Please open an issue at https://github.com/WalkerKnapp/devolay/issues");
        }
    }

    private static String getArchDirectory() {
        final String osArchProperty = System.getProperty("os.arch").toLowerCase();
        if (osArchProperty.contains("aarch64") || (osArchProperty.contains("arm") && (osArchProperty.contains("64") || osArchProperty.contains("v8")))) {
            return "arm64-v8a";
        } else if (osArchProperty.contains("aarch32") || (osArchProperty.contains("arm") && (osArchProperty.contains("32") || osArchProperty.contains("v7")))) {
            return "armv7a";
        } else if (osArchProperty.contains("64")) {
            return "x86-64";
        } else if (osArchProperty.contains("86")) {
            return "x86";
        } else {
            throw new IllegalStateException("Unsupported Arch: " + osArchProperty + ". Please open an issue at https://github.com/WalkerKnapp/devolay/issues");
        }
    }

    private static Path extractNative(String prefix, String suffix, String pathInJar) {
        try(InputStream is = Devolay.class.getResourceAsStream(pathInJar)) {
            if(is == null) {
               return null;
            }

            // Get a temporary directory to place natives
            Path tempPath = Files.createTempFile(prefix, suffix);

            // Create a lock file for this dll
            Path tempLock = tempPath.resolveSibling(tempPath.getFileName().toString() + ".lock");
            Files.createFile(tempLock);
            tempLock.toFile().deleteOnExit();

            // Copy the natives to be loaded
            Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);

            // Clean up any natives from previous runs that do not have a corresponding lock file
            Files.list(tempPath.getParent())
                    .filter(path -> path.getFileName().toString().startsWith(prefix) && path.getFileName().toString().endsWith(suffix))
                    .filter(path -> !Files.exists(path.resolveSibling(path.getFileName().toString() + ".lock")))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // ignored, the file is in use without a lock
                        }
                    });

            return tempPath;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String findLibrary(String libraryName) {
        try {
            Method findLibraryHandle = ClassLoader.class.getDeclaredMethod("findLibrary", String.class);
            findLibraryHandle.setAccessible(true);
            return (String) findLibraryHandle.invoke(Devolay.class.getClassLoader(), libraryName);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Loads the NDI run-time from the NDI Redist Environment Variable (NDI_RUNTIME_DIR_V3)
     *
     * @return An int representing the success of the library loading.
     *      0 - Success
     *      -1 - The libraries are not installed (the environment variable doesn't exist)
     *      -2 - The library load failed. (The end user should reinstall the libraries, and should be provided with the redist URL)
     */
    public static int loadLibraries() {
        if(!librariesLoaded.get()) {
            int ret;
            if (extractedNdiLibraryPath != null) {
                ret = nLoadLibraries(extractedNdiLibraryPath);
            } else {
                ret = nLoadLibraries(null);
            }
            if(ret == 0) {
                librariesLoaded.set(true);
            }
            return ret;
        } else {
            return 0;
        }
    }

    /**
     * Returns the current version of the underlying NDI(tm) library runtime.
     *
     * @return A string containing the version of the NDI(tm) runtimes.
     */
    public static String getNDIVersion() {
        return nGetVersion();
    }

    /**
     * Returns whether the current CPU in the system is capable of running NDI(tm), and by extension, Devolay.
     *
     * @return true if the system's CPU is capable of running NDI(tm), false if it is not capable.
     */
    public static boolean isSupportedCpu() {
        return nIsSupportedCpu();
    }

    // Native Methods

    private static native int nLoadLibraries(String extractedNdiPath);
    private static native String nGetVersion();
    private static native boolean nIsSupportedCpu();
}
