package com.walker.devolayexamples;

import com.walker.devolay.Devolay;
import com.walker.devolay.DevolayFinder;
import com.walker.devolay.DevolaySource;

public class FindExample {
    public static void main(String[] args) {
        Devolay.loadLibraries();

        // Create a new finder instance with default settings
        DevolayFinder finder = new DevolayFinder();

        // Run for one minute
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 1000 * 60) {
            // Query with a timeout of 5 seconds
            if (!finder.waitForSources(5000)) {
                // If no new sources were found
                System.out.println("No change to the sources list found.");
                continue;
            }

            // Query the updated list of sources
            DevolaySource[] sources = finder.getCurrentSources();
            System.out.println("Network sources (" + sources.length + " found).");
            for (int i = 0; i < sources.length; i++) {
                System.out.println((i + 1) + ". " + sources[i].getSourceName());
            }
        }

        // Destroy the references to each. Not necessary, but can free up the memory faster than Java's GC by itself
        finder.close();
    }
}
