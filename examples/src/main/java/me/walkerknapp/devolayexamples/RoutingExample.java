package me.walkerknapp.devolayexamples;

import me.walkerknapp.devolay.Devolay;
import me.walkerknapp.devolay.DevolayFinder;
import me.walkerknapp.devolay.DevolayRouter;
import me.walkerknapp.devolay.DevolaySource;

import java.util.Random;

public class RoutingExample {
    public static void main(String[] args) throws InterruptedException {
        Devolay.loadLibraries();

        // Create a new routed source named "Example Routed Source"
        DevolayRouter router = new DevolayRouter("Example Routed Source");
        // Create a new finder instance with default settings
        DevolayFinder finder = new DevolayFinder();

        // Switch between sources 1000 times
        for (int i = 0; i < 1000; i++) {
            // Get a list of current sources on the network
            DevolaySource[] availableSources = finder.getCurrentSources();

            if (availableSources.length == 0) {
                // If no sources were found, route to nowhere (black screen)
                router.clearSource();
            } else {
                // If a sources were found, choose one at random to route to
                DevolaySource targetSource = availableSources[new Random().nextInt(availableSources.length)];
                System.out.println("Routing to " + targetSource.getSourceName());
                router.setSource(targetSource);
            }

            // Stay on this source for 15 seconds
            Thread.sleep(15 * 1000);
        }

        // Destroy the references to each. Not necessary, but can free up the memory faster than Java's GC by itself
        finder.close();
        router.close();
    }
}
