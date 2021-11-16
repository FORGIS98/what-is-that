package com.example.whatisthat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Photograph {

    // TAG for logs
    private static final String TAG = "what-is-that";
    // Camera permission
    private static final int CAM_PERMISSION = 200;

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSession;
    protected CaptureRequest.Builder captureRequestBuilder;

    // Camera Orientation
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // Image vars
    private Size imgSize;
    private ImageReader imgReader;

    // Photography Object
    Context context;
    Button takePictureBtn;
    TextureView cameraView;

    // Others
    private Handler bckGroundHandler;
    private HandlerThread bckGroundThread;

    public Photograph(Context context, Button takePictureBtn, TextureView cameraView) {
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

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        // The method called when a camera device has finished opening
        public void onOpened(@NonNull CameraDevice camera) {
            Log.i(TAG, "INFO: stateCallback.onOpened() -- Camera OPEN");
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

    // if savePicture is TRUE: the method will save the picture and return the bytes
    // if savePicture is FALSE: the method only returns the bytes
    protected Picture takePicture(boolean savePicture) throws InterruptedException {
        if (cameraDevice == null) {
            Log.e(TAG, "ERROR: takePicture() -- cameraDevice is NULL");
            return null;
        }

        // CAMERA_SERVICE: Use with getSystemService(java.lang.String) to retrieve a CameraManager for interacting with camera devices.
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        Picture returnPicture = null;
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size [] jpegSizes = null;
            if (characteristics != null) {
                // Should return: 1920x1080, 1280x720, 640x480...
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }

            int width = 640;
            int height = 480;

            if (jpegSizes != null && 0 < jpegSizes.length) {
                for (Size s : jpegSizes) {
                    // Best quality is: 4032x3024
                    if (s.toString().equals("1920x1080")) {
                        width = s.getWidth();
                        height = s.getHeight();
                    }
                }
            }

            // The ImageReader class allows direct application access to image data rendered into a Surface
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(cameraView.getSurfaceTexture()));

            // TEMPLATE_STILL_CAPTURE: Create a request suitable for still image capture. Specifically, this means prioritizing image quality over frame rate.
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            // CONTROL_MODE: Overall mode of 3A (auto-exposure, auto-white-balance, auto-focus) control routines.
            // CONTROL_MODE_AUTO: Manual control of capture parameters is disabled.
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Returns the rotation of the screen from its "natural" orientation.
            int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            try (Image img = reader.acquireLatestImage()) {
                if (img == null) {
                    Log.e(TAG, "ERROR: reader.acquireLatestImage() return null");
                    throw new NullPointerException();
                }
                ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                returnPicture = new Picture(buffer);

            } catch (NullPointerException nullError) {
                Log.e(TAG, "ERROR: takePicture() Image == NULL");
                nullError.printStackTrace();
            }

            // Callback interface for being notified that a new image is available.
            // The onImageAvailable is called per image basis, that is, callback fires for every new frame available from ImageReader.
            /*ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    try (Image img = reader.acquireLatestImage()) {
                        if (img == null) {
                            Log.e(TAG, "ERROR: reader.acquireLatestImage() return null");
                            throw new NullPointerException();
                        }
                        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                        Picture returnPicture = new Picture(buffer);

                        if (savePicture)
                            save(bytes);

                    } catch (FileNotFoundException fileError) {
                        Log.e(TAG, "ERROR: takePicture().readListener.fileError");
                        fileError.printStackTrace();
                    } catch (IOException ioError) {
                        Log.e(TAG, "ERROR: takePicture().readListener.ioError");
                        ioError.printStackTrace();
                    } catch (NullPointerException nullError) {
                        Log.e(TAG, "ERROR: takePicture() Imagen == NULL");
                        nullError.printStackTrace();
                    }
                }

                private void save (byte [] bytes) throws IOException {
                    File path = context.getFilesDir();
                    final File file = new File(path, "inceptionV3.JPG");
                    Log.i(TAG, "Saving picture in folder: " + file.toString());

                    try (FileOutputStream out = new FileOutputStream(file)) {
                        out.write(bytes);
                        Log.d(TAG, "Picture successfully saved.");
                    }
                }
            };*/

            //reader.setOnImageAvailableListener(readerListener, bckGroundHandler);
            // A callback object for tracking the progress of a CaptureRequest submitted to the camera device.
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    // Toast.makeText(context, "Saved: " + file, Toast.LENGTH_SHORT).show();
                    cameraPreview();
                }
            };

            // Create a new CameraCaptureSession using a SessionConfiguration helper object that aggregates all supported parameters.
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, bckGroundHandler);
                    } catch (CameraAccessException camError) {
                        Log.e(TAG, "ERROR: takePicture().crateCaptureSession -- CameraAccessException");
                        camError.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "ERROR: takePicture().createCaptureSession");
                }
            }, bckGroundHandler);
        } catch (CameraAccessException camError) {
            Log.e(TAG, "ERROR: takePicture()");
            camError.printStackTrace();
        } catch (NullPointerException nullError) {
            Log.e(TAG, "ERROR: takePicture");
            nullError.printStackTrace();
        }

        // Wait for returnBytes to hold the picture.
        // Will "TimeOut" after 2000ms
        /*int times = 0;
        while (returnBytes[0] == null && times < 20) {
            Thread.sleep(100);
            times += 1;
        }
        if(returnBytes[0] == null) {
            Log.e(TAG, "returnBytes is NULL");
            throw new NullPointerException();
        }*/
        return returnPicture;
    }

    protected void managerOpenCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        Log.i(TAG, "managerOpenCamera()");

        try {
            // Rear camera identifier
            // Camera vars
            String cameraId = manager.getCameraIdList()[0];
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
            Log.e(TAG, "ERROR: managerOpenCamera()");
            camError.printStackTrace();
        }
    }

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
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    // Camera closed
                    if (cameraDevice == null)
                        return;

                    // Session ready to display the preview
                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "ERROR: cameraPreview()");
                    Toast.makeText(context, "Config Change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException camErr) {
            Log.e(TAG, "ERROR: cameraPreview()");
            camErr.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            Log.e(TAG, "ERROR: cameraDevice is null!");
        } else {
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            try {
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, bckGroundHandler);
            } catch (CameraAccessException camError) {
                Log.e(TAG, "ERROR: updatePreview()");
                camError.printStackTrace();
            }
        }
    }

    protected void closeCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        if (imgReader != null) {
            imgReader.close();
            imgReader = null;
        }
    }

    protected void startBackGroundThread() {
        bckGroundThread = new HandlerThread("Camera Background");
        bckGroundThread.start();
        bckGroundHandler = new Handler(bckGroundThread.getLooper());
    }

    protected void stopBackGroundThread() {
        bckGroundThread.quitSafely();
        try {
            bckGroundThread.join();
            bckGroundThread = null;
            bckGroundHandler = null;
        } catch (InterruptedException interError) {
            Log.e(TAG, "ERROR: stopBackgroundThread()");
            interError.printStackTrace();
        }
    }
}
