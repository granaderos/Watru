package com.example.root.watrulin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AppLauncher extends AppCompatActivity {

    private static final String TAG = "AppLauncher";

    private Button btnChooseImage;
    private ImageButton ibtnCapture;
    private TextureView tvImage;

    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString = "";
    private int screenWidth, screenHeight;

    private ImageView ivImageToProcess;

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
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler handler;
    private HandlerThread mBackgroundThread;

    @Override
    protected void onCreate(Bundle splashBundle) {
        super.onCreate(splashBundle);
        try {
            View decor = getWindow().getDecorView();
            int uiOption = View.SYSTEM_UI_FLAG_FULLSCREEN;
            //int uiOption = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            //decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            decor.setSystemUiVisibility(uiOption);

            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } catch (NullPointerException e) {}
        setContentView(R.layout.activity_app_launcher);

        tvImage = (TextureView) findViewById(R.id.tvImage);
        assert tvImage != null;
        tvImage.setSurfaceTextureListener(textureListener);

        ivImageToProcess = (ImageView) findViewById(R.id.ivImageToProcess);

        btnChooseImage = (Button) findViewById(R.id.btnChooseImage);
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Create intent to Open Image applications like Gallery, Google Photos
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);


                /*if(imgDecodableString != "") {
                    //ImageView imgView = (ImageView) findViewById(R.id.imgView);
                    // Set the Image in ImageView after decoding the String
                    Intent processImageIntent = new Intent(getApplicationContext(), ProcessImage.class);
                    processImageIntent.putExtra("imgDecodableString", imgDecodableString);
                    startActivity(processImageIntent);
                }*/
            }
        });

        ibtnCapture = (ImageButton) findViewById(R.id.btnCapture);
        assert ibtnCapture != null;
        ibtnCapture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePicure();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && data != null) {

                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                /*ivImageToProcess.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));*/

                //ImageView imgView = (ImageView) findViewById(R.id.imgView);
                // Set the Image in ImageView after decoding the String
                Intent processImageIntent = new Intent(getApplicationContext(), ProcessImage.class);
                processImageIntent.putExtra("imgDecodableString", imgDecodableString);
                startActivity(processImageIntent);

            } else {
                Toast.makeText(this, "Capture a new image instead ;)",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

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
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(AppLauncher.this, "Saved: " + file, Toast.LENGTH_SHORT).show();
            //createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        handler = new Handler(mBackgroundThread.getLooper());
    }

    protected  void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            handler = null;
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicure() {
        if(cameraDevice == null) {
            Log.e(TAG, "No camera device Mj :(");
            return;
        }

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            System.out.print("Cam size: " + jpegSizes);
            /*int width = 720;
            int height = 1280;*/
            int width = 0;
            int height = 0;
            if(jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            //Toast.makeText(AppLauncher.this, "width = " + width + " height = " + height, Toast.LENGTH_LONG).show();

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(tvImage.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            boolean folderExist = true;
            File watRuFolder = new File(Environment.getExternalStorageDirectory()+ File.separator + "WatRu");
            if(!watRuFolder.exists()) {
                folderExist = watRuFolder.mkdirs();
            }
            if(folderExist) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                final File file = new File(Environment.getExternalStorageDirectory()+ File.separator + "WatRu" + File.separator + "WatRu_"+timeStamp+".jpg");
                //imgDecodableString = file.toString();
                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = null;
                        try {
                            image = reader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            //Toast.makeText(AppLauncher.this, "Calling save now...", Toast.LENGTH_LONG).show();
                            save(bytes);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (image != null) {
                                image.close();
                            }
                        }
                    }
                    private void save(byte[] bytes) throws IOException {

                        OutputStream output = null;
                        //Toast.makeText(AppLauncher.this, "Inside save method now; ", Toast.LENGTH_LONG).show();
                        try {
                            output = new FileOutputStream(file);
                            //Toast.makeText(AppLauncher.this, "Writing bytes now", Toast.LENGTH_LONG).show();
                            output.write(bytes);

                            imgDecodableString = file.toString();
                            //Toast.makeText(AppLauncher.this, "Inside try in save method. value is " + imgDecodableString, Toast.LENGTH_LONG).show();
                            //ImageView imgView = (ImageView) findViewById(R.id.imgView);
                            // Set the Image in ImageView after decoding the String


                            //Toast.makeText(AppLauncher.this, "Will be processing image now Mj", Toast.LENGTH_LONG).show();
                            Intent processImageIntent = new Intent(getApplicationContext(), ProcessImage.class);
                            processImageIntent.putExtra("imgDecodableString", imgDecodableString);
                            startActivity(processImageIntent);


                        } catch (Exception e) {
                            Toast.makeText(AppLauncher.this, "Catch at save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            if (output != null) {
                                output.close();
/*
                                if(!imgDecodableString.equals("")) {
                                    Toast.makeText(AppLauncher.this, "Will be processing image now Mj", Toast.LENGTH_LONG).show();
                                    Intent processImageIntent = new Intent(getApplicationContext(), ProcessImage.class);
                                    processImageIntent.putExtra("imgDecodableString", imgDecodableString);
                                    startActivity(processImageIntent);
                                } else {
                                    Toast.makeText(AppLauncher.this, "Empty imgDecodableString? :D = " + imgDecodableString, Toast.LENGTH_LONG).show();
                                }*/
                            }
                        }
                    }
                };
                reader.setOnImageAvailableListener(readerListener, handler);
                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        //Toast.makeText(AppLauncher.this, "Saved:" + file, Toast.LENGTH_SHORT).show();

                        //imgDecodableString = file.toString();

                        //createCameraPreview();
                        // Interrupt here Mj to process the image :) (recognise the objects on it)

                        /*if(!imageDimension.equals("") && output != null) {


                            Toast.makeText(AppLauncher.this, "Will be processing image now Mj", Toast.LENGTH_LONG).show();
                            Intent processImageIntent = new Intent(getApplicationContext(), ProcessImage.class);
                            processImageIntent.putExtra("imgDecodableString", imgDecodableString);
                            startActivity(processImageIntent);
                        }*/
                    }
                };
                cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        try {
                            session.capture(captureBuilder.build(), captureListener, handler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {
                    }
                }, handler);
            } else { // folder doesn't exist! Mj

            }
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }
    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = tvImage.getSurfaceTexture();
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
                    Toast.makeText(AppLauncher.this, "Configuration change", Toast.LENGTH_SHORT).show();
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
            //Toast.makeText(AppLauncher.this, "Image Dimension = " + imageDimension, Toast.LENGTH_LONG).show();
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AppLauncher.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
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
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, handler);

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
                Toast.makeText(AppLauncher.this, "Sorry Mj! Permission problem :(", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (tvImage.isAvailable()) {
            openCamera();
        } else {
            tvImage.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
}
