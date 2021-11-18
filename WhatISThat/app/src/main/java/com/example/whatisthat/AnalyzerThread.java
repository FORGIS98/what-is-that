package com.example.whatisthat;

public class AnalyzerThread extends Thread {
    Classifier classifier;
    Photograph photograph;
    Picture picture;
    String label;
    private volatile boolean running;

    public AnalyzerThread(Classifier c, Photograph p) {
        classifier = c;
        photograph = p;
        picture = null;
        label = "Analyzing...";
        running = true;
    }

    @Override
    public void run() {
        while (running) {
            picture = photograph.takePicture(false);
            classifier.feed(picture);
            classifier.run();
            label = classifier.get();
        }
    }

    public void kill() {
        running = false;
    }
}
