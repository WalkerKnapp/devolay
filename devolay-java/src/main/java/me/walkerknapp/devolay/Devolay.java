package me.walkerknapp.devolay;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

public class Devolay {

    private static final AtomicBoolean librariesLoaded = new AtomicBoolean(false);

    static {
        final String libraryName = System.mapLibraryName("devolay-natives");
        final String libraryExtension = libraryName.substring(libraryName.indexOf('.'));

        final String osNameProperty = System.getProperty("os.name").toLowerCase();
        String osDirectory;
        if (osNameProperty.contains("nix") || osNameProperty.contains("nux")) {
            osDirectory = "linux";
        } else if (osNameProperty.contains("win")) {
            osDirectory = "windows";
        } else if (osNameProperty.contains("mac")) {
            osDirectory = "macos";
        } else {
            throw new IllegalStateException("Unsupported OS: " + osNameProperty + ". Please open an issue at https://github.com/WalkerKnapp/devolay/issues");
        }

        final String osArchProperty = System.getProperty("os.arch").toLowerCase();
        String archDirectory;
        if (osArchProperty.contains("64")) {
            archDirectory = "x86-64";
        } else if (osArchProperty.contains("86")) {
            archDirectory = "x86";
        } else {
            throw new IllegalStateException("Unsupported Arch: " + osArchProperty + ". Please open an issue at https://github.com/WalkerKnapp/devolay/issues");
        }

        try(InputStream is = Devolay.class.getResourceAsStream("/natives/" + osDirectory + "/" + archDirectory + "/" + libraryName)) {
            if(is == null) {
                throw new IllegalStateException("This build of Devolay is not compiled for your OS. Please use a different build or follow the compilation instructions on https://github.com/WalkerKnapp/devolay.");
            }

            // Copy the natives to a temp file to be loaded
            Path tempPath = Files.createTempFile("devolay-natives", libraryExtension);
            Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempPath.toAbsolutePath().toString());

            // Delete the natives when exiting
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Files.delete(tempPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }


        int ret = loadLibraries();
        if(ret != 0) {
            // The libraries are not correctly installed.
            if(ret == -1) {
                throw new IllegalStateException("The NDI(tm) SDK libraries were not found.");
            } else if(ret == -2) {
                throw new IllegalStateException("The NDI(tm) SDK libraries failed to load. Please reinstall.");
            }
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
            int ret = nLoadLibraries();
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

    private static native int nLoadLibraries();
    private static native String nGetVersion();
    private static native boolean nIsSupportedCpu();
}
