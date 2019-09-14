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
        videoFrame.setFourCCType(DevolayFrameFourCCType.NDIlib_FourCC_type_BGRX);
        videoFrame.setData(data);
        videoFrame.setFrameRate(60, 1);

        int frameCounter = 0;
        long fpsPeriod = System.currentTimeMillis();

        // Run for one minute
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1000 * 60) {

            //Fill in the buffer for one frame.
            data.position(0);
            for(int i = 0; i < width * height; i++) {
                double xCoord = i % width;
                double yCoord = i / width;

                double frameOffset = (frameCounter % 120) / 120d;
                double convertedX = xCoord/width;
                double convertedY = yCoord/height;

                byte r = (byte) (255 * Math.sqrt(Math.pow(convertedX + frameOffset, 2) + Math.pow(convertedY, 2)));
                byte g = (byte) (255 * Math.sqrt(Math.pow(convertedX + frameOffset - 1, 2) + Math.pow(convertedY, 2)));
                byte b = (byte) (255 * Math.sqrt(Math.pow(convertedX + frameOffset, 2) + Math.pow(convertedY - 1, 2)));

                data.put(b).put(g).put(r).put((byte)255);
            }
            data.flip();

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
}
