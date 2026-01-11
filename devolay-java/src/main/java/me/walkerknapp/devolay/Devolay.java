package me.walkerknapp.devolay;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to control loading of Devolay and NDI libraries.  
 * 
 * Loading of the Devolay native libraries requires either:
 * <ul>
 * <li>Using the devolay-integrated maven package</li>
 * <li>Using the {@link #loadLibraries(Path, Path)} method to tell Devolay where the devolay native is</li>
 * </ul>
 * 
 * Loading of the NDI native libraries requires either:
 * <ul>
 * <li>Using the devolay-integrated maven package</li>
 * <li>Android - libraries are loaded from an aar</li>
 * <li>Using the {@link #loadLibraries(Path, Path)} method to tell Devolay where the NDI native is</li>
 * </ul>
 * 
 * Recommended usage:
 * <ul>
 * <li>Use the devolay-integrated maven package until you run into a problem.</li>
 * <li>Windows / Windows Store deployment - use devolay-integrated</li>
 * <li>Ubuntu / Snap Store deployment - use devolay-integrated</li>
 * <li>MacOS - use devolay-integrated</li>
 * <li>MacOS Store - manually load both Devolay and NDI libraries using {@link #loadLibraries(Path, Path)} and packaging the libraries per MacOS Store requirements</li>
 * <li>Android - use devolay-integrated</li>
 * </ul>
 * 
 * Example usage - In your app initialization:
 * <pre>
 * // to load the native libraries from the devolay-integrated maven package
 * Devolay.loadLibraries();
 * // or to manually load the native libraries
 * Devolay.loadLibraries(pathToDevolayLibrary, pathToNDILibrary);
 * </pre>
 */
public class Devolay {

    private static final AtomicBoolean librariesLoaded = new AtomicBoolean(false);
    private static Path extractedDevolayNativesPath = null;
    private static Path extractedNDINativesPath = null;
    
    /**
     * Only extract natives from the integrated build during static initialization
     * so that the Devolay class can still be loaded and then used to load the native
     * libraries from anywhere.
     */
    static {
        String devolayLibraryName = System.mapLibraryName("devolay-natives");
        String ndiLibraryName = System.mapLibraryName("ndi");
        String libraryExtension = devolayLibraryName.substring(devolayLibraryName.indexOf('.'));

        String osDirectory = getOsDirectory();
        String archDirectory = getArchDirectory();

        if (!osDirectory.equals("android")) {
            extractedDevolayNativesPath = extractNative("devolay-natives", libraryExtension,
                    "/natives/" + osDirectory + "/" + archDirectory + "/" + devolayLibraryName);
            extractedNDINativesPath = extractNative("ndi", libraryExtension,
                    "/natives/" + osDirectory + "/" + archDirectory + "/" + ndiLibraryName);
        } else {
        	String path = findLibrary("ndi");
            extractedNDINativesPath = Paths.get(path);
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
    	return loadLibraries(null, null);
    }
    
    /**
     * Loads the native libraries for both Devolay and NDI.  The given paths can be null, in which case
     * the extracted library locations will be used.
     * @param overrideDevolayNativesPath can be null
     * @param overrideNDINativesPath can be null
     * @return int
     * @throws IllegalStateException If the extracted paths are null
     * @throws UnsatisfiedLinkError If the native library could not be found or loaded
     * @see #loadLibraries()
     */
    public static int loadLibraries(Path overrideDevolayNativesPath, Path overrideNDINativesPath) {
        if(!librariesLoaded.get()) {
            // load devolay natives first
            loadDevolayLibrary(overrideDevolayNativesPath != null ? overrideDevolayNativesPath : extractedDevolayNativesPath);

            // load ndi natives next
            int ret = loadNDILibrary(overrideNDINativesPath != null ? overrideNDINativesPath : extractedNDINativesPath);
            
            if (ret == 0) {
	            // mark that we've loaded successfully
	            librariesLoaded.set(true);
            }
            
            return ret;
        }
        
        return 0;
    }
    
    /**
     * Loads the Devolay library at the given path.
     * @param path the path; cannot be null
     * @throws UnsatisfiedLinkError if the path cannot be loaded
     * @throws NullPointerException if the path is null
     */
    private static void loadDevolayLibrary(Path path) {
    	String osDirectory = getOsDirectory();

        if (!osDirectory.equals("android")) {
            if (path == null) {
                throw new NullPointerException("This build of Devolay is not compiled for your OS. Please use a different build or follow the compilation instructions on https://github.com/WalkerKnapp/devolay.");
            }

            System.load(path.toAbsolutePath().toString());
        } else {
            // Devolay on Android should be loaded as an aar, so natives don't have to be extracted.
            System.loadLibrary("devolay-natives");
        }
    }
    
    /**
     * Loads the NDI library at the given path.
     * @param path; when null, the library is loaded from an install directory
     * @return int
     */
    private static int loadNDILibrary(Path path) {
        if (path != null) {
            return nLoadLibraries(path.toAbsolutePath().toString());
        } else {
            return nLoadLibraries(null);
        }
    }

    /**
     * Returns the current version of the underlying NDI(tm) library runtime.
     *
     * @return A string containing the version of the NDI(tm) runtimes.
     * @throws IllegalStateException if loadLibraries has not been called or failed
     */
    public static String getNDIVersion() {
    	if (!librariesLoaded.get())
    		throw new IllegalStateException("The getNDIVersion method cannot be called until native libraries have been loaded using one of the loadLibraries methods.");

        return nGetVersion();
    }

    /**
     * Returns whether the current CPU in the system is capable of running NDI(tm), and by extension, Devolay.
     *
     * @return true if the system's CPU is capable of running NDI(tm), false if it is not capable.
     * @throws IllegalStateException if loadLibraries has not been called or failed
     */
    public static boolean isSupportedCpu() {
    	if (!librariesLoaded.get())
    		throw new IllegalStateException("The isSupportedCpu method cannot be called until native libraries have been loaded using one of the loadLibraries methods.");
        
    	return nIsSupportedCpu();
    }

    // Native Methods

    private static native int nLoadLibraries(String extractedNdiPath);
    private static native String nGetVersion();
    private static native boolean nIsSupportedCpu();
}