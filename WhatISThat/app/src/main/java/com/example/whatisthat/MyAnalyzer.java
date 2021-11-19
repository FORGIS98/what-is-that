package com.example.whatisthat;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Handler;
import android.os.Message;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;


public class MyAnalyzer implements ImageAnalysis.Analyzer{
    Classifier classifier;
    Handler labelHandler;

    public MyAnalyzer(Classifier c, Handler handler) {
        classifier = c;
        labelHandler = handler;
    }

    @Override
    public void analyze(ImageProxy image) {
        @SuppressLint("UnsafeOptInUsageError") Image img = image.getImage();
        Picture picture = new Picture(img);

        classifier.feed(picture);
        classifier.run();
        String label = classifier.get();

        Message msg = new Message();
        msg.obj = label;
        labelHandler.sendMessage(msg);

        image.close();
    }
}
