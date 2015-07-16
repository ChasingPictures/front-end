package de.fu_berlin.cdv.chasingpictures.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.fu_berlin.cdv.chasingpictures.R;
import de.fu_berlin.cdv.chasingpictures.util.Utilities;


public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
    private static final File pictureStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPictureCallback;
    private Camera.Parameters params;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final String EXTRA_IMAGE_FILE = "de.fu_berlin.cdv.chasingpictures.EXTRA_IMAGE_FILE";
    private Intent mResultData;
    private ImageView buttonTakePicture;
    private ImageView buttonRetry;
    private ImageView buttonFinish;
    private ImageView buttonFlash;
    private boolean viewingPhoto;
    private FlashMode flashMode = FlashMode.OFF;
    private WireframeView wireframeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make activity fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera_activity);
        mPictureCallback = new PictureCallback();
        mResultData = new Intent();

        buttonTakePicture = (ImageView) findViewById(R.id.takePictureButton);
        buttonRetry = (ImageView) findViewById(R.id.retryPictureButton);
        buttonFinish = (ImageView) findViewById(R.id.finishCameraButton);
        buttonFlash = (ImageView) findViewById(R.id.flashButton);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Create an instance of Camera
        mCamera = getCameraInstance();
        if (mCamera == null) {
            finish();
            return;
        }

        //region Autofocus
        // get Camera parameters
        params = mCamera.getParameters();

        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // Autofocus mode is supported
            // set the focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        flashMode.setParam(params);

        // set Camera parameters
        mCamera.setParameters(params);
        //endregion

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        // TODO: Use wireframe of the place we are searching for
        wireframeView = new WireframeView(this, BitmapFactory.decodeResource(getResources(), R.drawable.wireframe_rathaus));

        // Add preview and wireframe to the activity
        ViewGroup preview = (ViewGroup) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        preview.addView(wireframeView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void takePicture(View view){
        // get an image from the camera
        mCamera.takePicture(null, null, mPictureCallback);

        viewingPhoto = true;
        buttonTakePicture.setVisibility(View.INVISIBLE);
        buttonFinish.setVisibility(View.VISIBLE);
        buttonFlash.setVisibility(View.INVISIBLE);
    }

    /**
     * Return to the previous activity, and return
     * the taken picture (if available).
     */
    public void acceptPhoto(View view){
        setResult(RESULT_OK, mResultData);
        finish();
    }

    /**
     * Depending on where we are at, quit the camera
     * or go back to taking the photo.
     */
    public void retryOrQuit(View view){
        if (viewingPhoto) { // If we are viewing a photo, go back to camera preview mode
            viewingPhoto = false;
            mCamera.startPreview();
            buttonTakePicture.setVisibility(View.VISIBLE);
            buttonFinish.setVisibility(View.INVISIBLE);
            buttonFlash.setVisibility(View.VISIBLE);
        } else { // If in photo taking mode, just quit
            finish();
        }
    }

    private enum FlashMode {
        OFF, ON, AUTO // currently not implemented
        ;

        private static final Map<FlashMode, String> parameters = new HashMap<>(3);
        private static final Map<FlashMode, Integer> imageResources = new HashMap<>(3);

        static {
            parameters.put(OFF, Camera.Parameters.FLASH_MODE_OFF);
            parameters.put(ON, Camera.Parameters.FLASH_MODE_ON);
            parameters.put(AUTO, Camera.Parameters.FLASH_MODE_AUTO);

            imageResources.put(OFF, R.drawable.flash_off);
            imageResources.put(ON, R.drawable.flash_on);
            // not yet implemented (because we are missing the icon)
            // imageResources.put(AUTO, R.drawable.flash_auto);
        }

        @DrawableRes
        public int getButtonImageResource() {
            return imageResources.get(this);
        }

        public void setParam(Camera.Parameters cameraParams) {
            cameraParams.setFlashMode(parameters.get(this));
        }

        public FlashMode next() {
            switch (this) {
                case OFF:
                    return FlashMode.ON;
                case ON:
                    return FlashMode.OFF;
                case AUTO: // not implemented
                    return FlashMode.OFF;
                default:
                    return FlashMode.OFF;
            }
        }
    }

    public void cycleFlash(View view) {
        flashMode = flashMode.next();
        flashMode.setParam(params);
        mCamera.setParameters(params);
        buttonFlash.setImageResource(flashMode.getButtonImageResource());
    }

    /** A safe way to get an instance of the Camera object. */
    @Nullable
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Utilities.showError(getApplicationContext(), R.string.error_camera_open);
        }
        return c;
    }


    private class PictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.e(TAG, "Error creating media file, check storage permissions.");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();


                Toast.makeText(
                        getApplicationContext(),
                        "Image Saved",
                        Toast.LENGTH_SHORT
                ).show();

                /*
                ImageView iv = (ImageView) findViewById(R.id.imageView);
                Bitmap bmp = BitmapFactory.decodeFile(pictureFile);
                iv.setImageBitmap(bmp);
                */
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found", e);
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file", e);
            }

            // Refresh storage show our picture shows up in the gallery
            Log.i(TAG, "Scanning for file: " + pictureFile);
            MediaScannerConnection.scanFile(
                    getApplicationContext(),
                    new String[]{pictureFile.toString()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    }
            );

            // Put picture into result data
            mResultData.putExtra(EXTRA_IMAGE_FILE, pictureFile);
        }
    }

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(
                pictureStorageDirectory,
                getString(R.string.app_name).replace(" ", "")
        );
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        if (type == MEDIA_TYPE_IMAGE){
            return new File(mediaStorageDir.getPath(), "IMG_"+ timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            return new File(mediaStorageDir.getPath(), "VID_" + timeStamp + ".mp4");
        }

        return null;
    }

}
