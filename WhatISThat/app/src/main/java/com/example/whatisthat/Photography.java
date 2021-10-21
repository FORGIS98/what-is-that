package com.example.whatisthat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.Arrays;

public class Photography {

    // TAG for logs
    private static final String TAG = "what-is-that";
    // Camera permission
    private static final int CAM_PERMISSION = 200;

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

    // Photography Object
    Context context;
    Button takePictureBtn;
    TextureView cameraView;

    // Others
    private boolean flash;
    private Handler bgHandler;
    private HandlerThread bgThread;

    public Photography(Context context, Button takePictureBtn, TextureView cameraView) {
        this.context = context;
        this.takePictureBtn = takePictureBtn;
        this.cameraView = cameraView;
    }

    TextureView.SurfaceTextureListener cameraListener = new TextureView.SurfaceTextureListener() {
        @Override
        // Invoked when a TextureView's SurfaceTexture is ready for use.
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            managerOpenCamera();
        }

        @Override
        // Invoked when the SurfaceTexture's buffers size changed.
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {

        }

        @Override
        // Invoked when the specified SurfaceTexture is about to be destroyed.
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        // Invoked when the specified SurfaceTexture is updated through SurfaceTexture#updateTexImage().
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };

    // TODO
    protected void takePicture() {

    }

    private void managerOpenCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        Log.i(TAG, "managerOpenCamera()");

        try {
            // Rear camera identifier
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics camSpecs = manager.getCameraCharacteristics(cameraId);
            // Info about format, size and frame duration
            StreamConfigurationMap map = camSpecs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // Get supported sizes for the input format, returns: [width, height]
            imgSize = map.getOutputSizes(SurfaceTexture.class)[0];

            // We verify that the user has given us permission to access the camera and to save pictures
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions((Activity) context, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, CAM_PERMISSION);

                return;
            }

            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException camError) {
            camError.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        // The method called when a camera device has finished opening
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "stateCallback.onOpened()");
            cameraDevice = camera;
            cameraPreview();
        }

        @Override
        // The method called when a camera device is no longer available for use
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        // The method called when a camera device has encountered a serious error
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "ERROR: stateCallback.onError()");
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        // The method called when a camera device has been closed with CameraDevice#close
        public void onClosed(@NonNull CameraDevice camera) { }
    };

    protected void cameraPreview() {
        try {
            SurfaceTexture texture = cameraView.getSurfaceTexture();
            texture.setDefaultBufferSize(imgSize.getWidth(), imgSize.getHeight());

            // Handle onto a raw buffer that is being managed by the screen compositor.
            Surface surface = new Surface(texture);

            // Create a CaptureRequest.Builder for new capture requests, initialized with template for a target use case.
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // Add a surface to the list of targets for this request.
            captureRequestBuilder.addTarget(surface);
            // Create a new CameraCaptureSession using a SessionConfiguration helper object that aggregates all supported parameters.
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    // Camera closed
                    if (cameraDevice == null)
                        return;

                    // Session ready to display the preview
                    cameraCaptureSession = session;

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(context, "Config Change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException camErr) {
            Log.e(TAG, "ERROR: cameraPreview()");
            camErr.printStackTrace();
        }
    }

}
