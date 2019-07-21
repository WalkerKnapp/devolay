package com.walker.devolay;

import java.lang.ref.Cleaner;

public class Devolay {

    public static final Cleaner cleaner = Cleaner.create();

    static {

    }

    /**
     * Loads the NDI run-time from the NDI Redist Environment Variable (NDI_RUNTIME_DIR_V3)
     *
     * @return An int representing the success of the library loading.
     *      0 - Success
     *      -1 - The libraries are not installed (the environment variable doesn't exist)
     *      -2 - The library load failed. (The end user should reinstall the libraries, and should be provided with the redist URL)
     */
    private native int loadLibraries();
}
