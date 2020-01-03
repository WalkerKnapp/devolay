package com.walker.devolayexamples;

import com.walker.devolay.Devolay;
import com.walker.devolay.DevolayFrameFourCCType;
import com.walker.devolay.DevolaySender;
import com.walker.devolay.DevolayVideoFrame;

import java.nio.ByteBuffer;

/**
 * Adapted from NDIlib_Send_Video.cpp
 */
public class SendVideoExample {
    public static void main(String[] args) {
        Devolay.loadLibraries();

        // Create the sender using the default settings, other than setting a name for the source.
        DevolaySender sender = new DevolaySender("Devolay Example Video");

        final int width = 1920;
        final int height = 1080;
        // BGRX has a pixel depth of 4
        final ByteBuffer data = ByteBuffer.allocateDirect(width * height * 4);

        // Create a video frame
        DevolayVideoFrame videoFrame = new DevolayVideoFrame();
        videoFrame.setResolution(width, height);
        videoFrame.setFourCCType(DevolayFrameFourCCType.BGRX);
        videoFrame.setData(data);
        videoFrame.setFrameRate(60, 1);

        int frameCounter = 0;
        long fpsPeriod = System.currentTimeMillis();

        // Run for ten minutes
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1000 * 60 * 10) {

            //Fill in the buffer for one frame.
            fillFrame(width, height, frameCounter, data);

            // Submit the frame. This is clocked by default, so it will be submitted at <= 60 fps.
            sender.sendVideoFrame(videoFrame);

            // Give an FPS message every 30 frames submitted
            if(frameCounter % 30 == 29) {
                long timeSpent = System.currentTimeMillis() - fpsPeriod;
                System.out.println("Sent 30 frames. Average FPS: " + 30f / (timeSpent / 1000f));
                fpsPeriod = System.currentTimeMillis();
            }

            frameCounter++;
        }

        // Destroy the references to each. Not necessary, but can free up the memory faster than Java's GC by itself
        videoFrame.close();
        sender.close();
    }

    private static void fillFrame(int width, int height, int frameCounter, ByteBuffer data) {
        data.position(0);
        double frameOffset = Math.sin(frameCounter / 120d);
        for(int i = 0; i < width * height; i++) {
            double xCoord = i % width;
            double yCoord = i / (double)width;

            double convertedX = xCoord/width;
            double convertedY = yCoord/height;

            double xWithFrameOffset = convertedX + frameOffset;
            double xWithScreenOffset = xWithFrameOffset - 1;
            double yWithScreenOffset = convertedY + 1;

            double squaredX = xWithFrameOffset * xWithFrameOffset;
            double offsetSquaredX = xWithScreenOffset * xWithScreenOffset;
            double squaredY = convertedY * convertedY;
            double offsetSquaredY = yWithScreenOffset * yWithScreenOffset;

            byte r = (byte) (Math.min(255 * Math.sqrt(squaredX + squaredY), 255));
            byte g = (byte) (Math.min(255 * Math.sqrt(offsetSquaredX + squaredY), 255));
            byte b = (byte) (Math.min(255 * Math.sqrt(squaredX + offsetSquaredY), 255));

            data.put(b).put(g).put(r).put((byte)255);
        }
        data.flip();
    }
}
