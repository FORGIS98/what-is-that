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
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;

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

    // Others
    Context context;
    private boolean flash;
    private Handler bgHandler;
    private HandlerThread bgThread;

    public Photography(Context context) {
        this.context = context;
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

    // TODO
    protected void cameraPreview() {

    }

}
