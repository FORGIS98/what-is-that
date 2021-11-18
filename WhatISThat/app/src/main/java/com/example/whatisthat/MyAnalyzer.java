package com.example.whatisthat;

import android.annotation.SuppressLint;
import android.media.Image;
import android.util.Log;
import android.util.Size;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class MyAnalyzer implements ImageAnalysis.Analyzer{
    Classifier classifier;
    String label;

    final int HEIGHT = 299;
    final int WIDTH = 299;

    public MyAnalyzer(Classifier c) {
        classifier = c;
        label = "Analyzing...";
    }

    @Override
    public void analyze(ImageProxy imageProxy) {
        Log.i("Analyze", imageProxy.toString());
        @SuppressLint("UnsafeOptInUsageError") Image img = imageProxy.getImage();
        img.getPlanes();
        Log.i("Analyze", String.valueOf(img.getHeight()));
        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
        Log.i("Analyze", String.valueOf(buffer.capacity()));
        Picture picture = new Picture(buffer);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Analyze", picture.toString());
        classifier.feed(picture);
        classifier.run();
        label = classifier.get();
        Log.i("Analyze-label", label);
    }
}
