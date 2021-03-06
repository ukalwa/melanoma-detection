/**
 *
 *  @Class: Class OpenCVCamera
 *   	Handles taking picture and saving it to sdcard
 *
 * 	@Version: 2.2
 * 	@Author: Upender Kalwa
 * 	@Created: 04/16/15
 * 	@Modified_by Upender Kalwa
 * 	@Modified: 08/30/17
 *
 * 	Implementation inspired from https://inducesmile.com/android/android-camera2-api-example-tutorial/
 *
 */
package ukalwa.moledetection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.os.SystemClock.sleep;

public class OpenCVCamera extends Activity {
    private static final String TAG = "OpenCVCamera";
    private static final String mDirName = "MoleDetection";
    private File mDirectory = new File(Environment.getExternalStorageDirectory(), "/" + mDirName);
    private Button takePictureButton;
    private TextureView textureView;
    private Button retake_button,proceed_button;
    public final static String FILEPATH = "MESSAGE";
    protected String filePath = mDirectory +"/camera001.jpg";

    static{
        System.loadLibrary("opencv_java3");
    }
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private ImageView imageView;
    Mat croppedMat = new Mat();
    private boolean isImageAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cvcamera);
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
        imageView = (ImageView) findViewById(R.id.full_image_view2);
        retake_button = (Button) findViewById(R.id.button3);
        proceed_button = (Button) findViewById(R.id.button4);

        assert takePictureButton != null;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
                while(!isImageAvailable){
                    sleep(1000);
                }
                Bitmap bmp = Bitmap.createBitmap(croppedMat.width(), croppedMat.height(),
                        Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(croppedMat, bmp);
                textureView.setVisibility(View.GONE);
                takePictureButton.setVisibility(View.GONE);
                imageView.setImageBitmap(bmp);
                imageView.setVisibility(View.VISIBLE);
                retake_button.setVisibility(View.VISIBLE);
                proceed_button.setVisibility(View.VISIBLE);

                addListenerOnProceedButton();
                addListenerOnRetakeButton();
            }
        });
    }

    public void addListenerOnProceedButton() {

        Button button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(OpenCVCamera.this, ProcessImage.class);
                i.putExtra(FILEPATH, filePath);
                startActivity(i);

            }

        });

    }

    public void addListenerOnRetakeButton() {

        Button button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                imageView.setVisibility(View.GONE);
                retake_button.setVisibility(View.GONE);
                proceed_button.setVisibility(View.GONE);
                textureView.setVisibility(View.VISIBLE);
                takePictureButton.setVisibility(View.VISIBLE);
            }

        });

    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
//            Toast.makeText(OpenCVCamera.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    protected void takePicture() {
        if(null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
//            Size[] jpegSizes = null;
//            jpegSizes = Objects.requireNonNull(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)).getOutputSizes(ImageFormat.JPEG);
            int width = 1024;
            int height = 768;
//            if (jpegSizes != null && 0 < jpegSizes.length) {
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            Log.i(TAG,"Image height, width:"+ Integer.toString(height)+","+Integer.toString(width));
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            file = new File(filePath);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    Size crop_size = new Size(600, 500);
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                        Mat matImage = new Mat();
                        Utils.bitmapToMat(bitmapImage, matImage); //convert bitmap image to Mat image for image processing
                        int max_dim = Math.max(matImage.rows(), matImage.cols());
                        if (max_dim > 1024){
                            double ratio = max_dim/1024;
                            double nHeight = matImage.rows() / ratio ;
                            double nWidth = matImage.cols() / ratio ;
                            Imgproc.resize(matImage, matImage, new org.opencv.core.Size(nWidth, nHeight));
                        }
                        Log.i(TAG,"Image dimensions before"+matImage.width() +"x"+matImage.height());
                        // Crop height of the image
//                        Size crop_size = new Size(600, 500);
                        Point center = new Point(matImage.width()/2, matImage.height()/2);
                        Rect cropRect = new Rect((int)(center.x-crop_size.getWidth()/2),
                                (int)(center.y-crop_size.getHeight()/2), crop_size.getWidth(),
                                crop_size.getHeight());
                        croppedMat = matImage.submat(cropRect);
                        Log.i(TAG,"Image dimensions after"+croppedMat.width() +"x"+croppedMat.height());
                        Imgproc.medianBlur(croppedMat, croppedMat ,5);
                        Bitmap bmp = Bitmap.createBitmap(croppedMat.cols(), croppedMat.rows(),
                                Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(croppedMat, bmp); //convert processed Mat image back to  bitmap image
                        matImage.release();
//                        croppedMat.release();
                        save(bmp);
                        isImageAvailable = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(Bitmap bm) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, output);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.i(TAG,"FileNotFound");
                    }finally {
                        if (null != output) {
                            output.flush();
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(OpenCVCamera.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Toast.makeText(OpenCVCamera.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(OpenCVCamera.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
//                Toast.makeText(OpenCVCamera.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
}