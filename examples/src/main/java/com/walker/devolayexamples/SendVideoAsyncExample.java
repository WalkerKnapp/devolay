package com.walker.devolayexamples;

import com.walker.devolay.Devolay;
import com.walker.devolay.DevolayFrameFourCCType;
import com.walker.devolay.DevolaySender;
import com.walker.devolay.DevolayVideoFrame;

import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 * Adapted from NDIlib_Send_Video_Async.cpp
 */
public class SendVideoAsyncExample {
    public static void main(String[] args) {
        new Scanner(System.in).nextLine();
        Devolay.loadLibraries();

        DevolaySender sender = new DevolaySender("Devolay Example Video");

        final int width = 1920;
        final int height = 1080;
        // BGRA has a pixel depth of 4
        final int pixelDepth = 4;

        DevolayVideoFrame videoFrame = new DevolayVideoFrame();
        videoFrame.setResolution(width, height);
        videoFrame.setFourCCType(DevolayFrameFourCCType.BGRA);
        videoFrame.setLineStride(width * pixelDepth);
        videoFrame.setFrameRate(60, 1);

        // Use two frame buffers because one will typically be in flight (being used by NDI send) while the other is being filled.
        ByteBuffer[] frameBuffers = { ByteBuffer.allocateDirect(width * height * pixelDepth),
                ByteBuffer.allocateDirect(width * height * pixelDepth) };

        int frameCounter = 0;
        long fpsPeriod = System.currentTimeMillis();

        // Run for one minute
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 1000 * 60) {
            // Use the buffer that currently isn't in flight
            ByteBuffer buffer = frameBuffers[frameCounter & 1];

            // Fill in the buffer for one frame.
            fillFrame(width, height, frameCounter, buffer);
            videoFrame.setData(buffer);

            // Submit the frame asynchronously.
            // This call will return immediately and the API will "own" the buffer until a synchronizing event.
            // A synchronizing event is one of: DevolaySender#sendVideoFrameAsync, DevolaySender#sendVideoFrame, DevolaySender#close
            sender.sendVideoFrameAsync(videoFrame);

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
