package com.example.whatisthat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    // LOGS
    private static final String TAG = "what-is-that";
    // Permissions
    private static final int CAM_PERMISSION = 200;

    private TextureView cameraView;
    private TextView inceptionTextResponse;

    Classifier classifier;
    Photograph ph;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind frontend camera texture view holder
        cameraView = findViewById(R.id.camera_view);
        // Bind frontend take picture Button
        // frontend elements
        Button takePictureBtn = findViewById(R.id.btn_takepicture);

        // Bind frontend inception text holder
        inceptionTextResponse = findViewById(R.id.inception_response);


        ph = new Photograph(this, takePictureBtn, cameraView);

        // Thread Logic
        final Handler responseHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                inceptionTextResponse.setText(((String) msg.obj).toUpperCase());
                inceptionTextResponse.setBackgroundResource(R.drawable.round_rectangle);
            }
        };

        // This values allow to start and stop the "take pictures process"
        AtomicBoolean takingPictures = new AtomicBoolean(true);
        AtomicBoolean autoPictures = new AtomicBoolean(true);

        cameraView.setSurfaceTextureListener(ph.cameraListener);
        takePictureBtn.setOnClickListener(v -> {

            if (takingPictures.get()) {
                // Clear TextView text
                inceptionTextResponse.setText("Analyzing...");
                inceptionTextResponse.setBackgroundResource(0);

                // takingPictures to false so if we press the button again we will
                // enter the else statement, then autoPictures to true so while starts
                // the "taking pictures thread"
                takingPictures.set(false);
                autoPictures.set(true);
                takePictureBtn.setText(R.string.stop_taking_picture);
                Picture picture = null;
                String bestLabel;
                while (autoPictures.get()) {
                   try {
                       picture = ph.takePicture(false);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

                   Message msg = new Message();

                   // Avoid thread showing last update if
                   // button is pressed
                   if(autoPictures.get())
                       responseHandler.sendMessage(msg);

                }
            }
            else {
                // takingPictures to true so next time we press the button the app
                // starts the "taking pictures thread" and autoPictures to false so
                // we finish the running "taking pictures thread"
                takingPictures.set(true);
                autoPictures.set(false);
                takePictureBtn.setText(R.string.take_picture);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAM_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MainActivity.this, "We need access to your camera!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        ph.startBackGroundThread();
        if (cameraView.isAvailable())
            ph.managerOpenCamera();
        else
            cameraView.setSurfaceTextureListener(ph.cameraListener);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        ph.stopBackGroundThread();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        classifier.close();
        ph.closeCamera();
        super.onDestroy();
    }
}