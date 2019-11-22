package com.walker.devolay;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

public class Devolay {

    private static AtomicBoolean librariesLoaded = new AtomicBoolean(false);

    static {
        try(InputStream is = Devolay.class.getResourceAsStream("/devolay-natives.dll")) {
            Path tempPath = Files.createTempFile("devolay-natives", ".dll");
            System.out.println(tempPath.toAbsolutePath().toString());
            long b = Files.copy(is, tempPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println(b);
            System.load(tempPath.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }


        // The libraries are not correctly installed.
        int ret = loadLibraries();
        if(ret != 0) {
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

    private static native int nLoadLibraries();
}
