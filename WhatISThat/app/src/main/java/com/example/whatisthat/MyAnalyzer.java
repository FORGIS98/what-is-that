package com.example.whatisthat;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;


public class MyAnalyzer implements ImageAnalysis.Analyzer {
    Classifier classifier;
    Handler labelHandler;

    public MyAnalyzer(Classifier c, Handler handler) {
        classifier = c;
        labelHandler = handler;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void analyze(ImageProxy image) {
        @SuppressLint("UnsafeOptInUsageError") Image img = image.getImage();
        Picture picture = new Picture(img);

        classifier.setHandler(labelHandler);
        classifier.feed(picture);
        classifier.run();
        classifier.get();

        image.close();
    }
}
