package com.example.whatisthat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    // LOGS
    private static final String TAG = "what-is-that";

    private PreviewView cameraView;
    private TextView inceptionTextResponse;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ImageAnalysis imageAnalysis;

    private boolean isAnalyzing;

    private Classifier classifier;
    final int WIDTH = 299;
    final int HEIGHT = 299;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.camera_view);
        Button takePictureBtn = findViewById(R.id.btn_takepicture);
        inceptionTextResponse = findViewById(R.id.inception_response);

        //Request a camera provider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "ERROR: binding camera provider failed");
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        classifier = new Classifier(getApplicationContext());

        isAnalyzing = false;
        imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(WIDTH, HEIGHT))
                        .setTargetRotation(Surface.ROTATION_0)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        final Handler responseHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (isAnalyzing) {
                    inceptionTextResponse.setText(((String) msg.obj).toUpperCase());
                    inceptionTextResponse.setBackgroundResource(R.drawable.round_rectangle);
                }
            }
        };

        takePictureBtn.setOnClickListener(v -> {
            if (isAnalyzing) {
                imageAnalysis.clearAnalyzer();
                takePictureBtn.setText(R.string.take_picture);
                inceptionTextResponse.setText("");
                inceptionTextResponse.setBackgroundResource(0);
            }
            else {
                imageAnalysis.setAnalyzer(AsyncTask.THREAD_POOL_EXECUTOR, new MyAnalyzer(classifier, responseHandler));
                takePictureBtn.setText(R.string.stop_taking_picture);
            }
            isAnalyzing = !isAnalyzing;
        });
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(cameraView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    @Override
    protected void onDestroy() {
        classifier.close();
        super.onDestroy();
    }
}