package de.fu_berlin.cdv.chasingpictures.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fu_berlin.cdv.chasingpictures.R;
import de.fu_berlin.cdv.chasingpictures.util.Utilities;


public class CameraActivity extends Activity {

    public static final String EXTRA_IMAGE_FILE = "de.fu_berlin.cdv.chasingpictures.EXTRA_IMAGE_FILE";
    private static final String TAG = "CameraActivity";
    private Camera mCamera;
    private Camera.Parameters mCameraParams;
    private ImageView buttonTakePicture;
    private ImageView buttonFinish;
    private ImageView buttonFlash;
    private boolean viewingPhoto;
    private FlashMode flashMode = FlashMode.OFF;
    private byte[] lastImage;
    private Camera.PictureCallback mPictureCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make activity fullscreen (we need to do this before setting the content view)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Inflate the layout
        setContentView(R.layout.activity_camera_activity);

        mPictureCallback = new PictureCallback();

        buttonTakePicture = (ImageView) findViewById(R.id.takePictureButton);
        buttonFinish = (ImageView) findViewById(R.id.finishCameraButton);
        buttonFlash = (ImageView) findViewById(R.id.flashButton);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
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

        setInitialCameraParameters();
        setupCameraPreview();
    }

    private void setInitialCameraParameters() {
        mCameraParams = mCamera.getParameters();

        // Use autofocus if available
        List<String> focusModes = mCameraParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        // Set the flash mode
        flashMode.setParam(mCameraParams);

        mCamera.setParameters(mCameraParams);
    }

    /**
     * Set up the preview for the camera.
     */
    private void setupCameraPreview() {
        // Create our Preview view and set it as the content of our activity.
        CameraPreview cameraPreview = new CameraPreview(this, mCamera);
        // TODO: Use wireframe of the place we are searching for
        WireframeView wireframe = new WireframeView(this, BitmapFactory.decodeResource(getResources(), R.drawable.wireframe_rathaus));

        // Add preview and wireframe to the activity
        ViewGroup previewLayout = (ViewGroup) findViewById(R.id.camera_preview);
        previewLayout.addView(cameraPreview);
        previewLayout.addView(wireframe);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void takePicture(View view) {
        mCamera.takePicture(null, null, mPictureCallback);
    }

    /**
     * Return to the previous activity, and return
     * the taken picture (if available).
     */
    public void acceptPhoto(View view) {
        // Save the image
        SaveImageTask saveImageTask = new SaveImageTask(this, lastImage) {
            @Override
            protected void onPostExecute(@Nullable File pictureFile) {
                if (pictureFile != null) {
                    Intent resultData = new Intent()
                            .putExtra(EXTRA_IMAGE_FILE, pictureFile);
                    setResult(RESULT_OK, resultData);
                    finish();
                } else {
                    Log.e(TAG, "Error occurred while saving the image", exception);
                }
            }
        };

        saveImageTask.execute();
        // TODO: Display a progress indicator while saving the image?
    }

    /**
     * Depending on where we are at, quit the camera
     * or go back to taking the photo.
     */
    public void retryOrQuit(View view) {
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

    public void cycleFlash(View view) {
        flashMode = flashMode.next();
        flashMode.setParam(mCameraParams);
        mCamera.setParameters(mCameraParams);
        buttonFlash.setImageResource(flashMode.getButtonImageResource());
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    @Nullable
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Utilities.showError(getApplicationContext(), R.string.error_camera_open);
        }
        return c;
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

    private class PictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Stop preview and adjust buttons
            mCamera.stopPreview();
            viewingPhoto = true;
            buttonTakePicture.setVisibility(View.INVISIBLE);
            buttonFinish.setVisibility(View.VISIBLE);
            buttonFlash.setVisibility(View.INVISIBLE);

            // Keep image data in case we want so save it
            CameraActivity.this.lastImage = data;
        }
    }
}
