package com.example.whatisthat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // LOGS
    private static final String TAG = "what-is-that";
    // Permissions
    private static final int CAM_PERMISSION = 200;

    // frontend elements
    private Button takePictureBtn;
    private TextureView cameraView;
    private TextView inceptionTextResponse;

    //classifier elements
    private Classifier classifier;

    Photography phy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind frontend camera textureview holder
        cameraView = findViewById(R.id.camera_view);
        // Bind frontend takepicture Button
        takePictureBtn = findViewById(R.id.btn_takepicture);

        // Bind frontend inception text holder
        // inceptionTextResponse = (TextView) findViewById(R.id.inception_response);

        phy = new Photography(this, takePictureBtn, cameraView);
        final byte[][] pictureBytes = {null};

        cameraView.setSurfaceTextureListener(phy.cameraListener);
        takePictureBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // ture: if I want to save the picture
                // false: if I DON'T want to save the picture
                pictureBytes[0] = phy.takePicture(true);
            }
        });

        //Classifier creation
        classifier = new Classifier(getApplicationContext());

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
        phy.startBackGroundThread();
        if (cameraView.isAvailable())
            phy.managerOpenCamera();
        else
            cameraView.setSurfaceTextureListener(phy.cameraListener);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        phy.stopBackGroundThread();
        super.onPause();
    }
}