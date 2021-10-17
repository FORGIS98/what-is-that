package com.example.whatisthat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // TAG for logs
    private static final String TAG = "what-is-that";
    // Camera permission
    private static final int CAM_PERMISSION = 200;

    // frontend elements
    private Button takePictureBtn;
    private TextureView cameraView;
    private TextView inceptionTextResponse;

    // Camera vars
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSession;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;

    // Image vars
    private Size imgSize;
    private ImageReader imgReader;
    private File file;

    // Others
    private boolean flash;
    private Handler bgHandler;
    private HandlerThread bgThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind textureview
        cameraView = (TextureView) findViewById(R.id.camera_view);
        cameraView.setSurfaceTextureListener(cameraListener);

        // Bind text
        // inceptionTextResponse = (TextView) findViewById(R.id.inception_response);

        // Bind Button
        takePictureBtn = (Button) findViewById(R.id.btn_takepicture);
        takePictureBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                takePicture();
            }
        });
    }

    TextureView.SurfaceTextureListener cameraListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };

    protected void takePicture() {

    }
}