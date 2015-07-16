package de.fu_berlin.cdv.chasingpictures.camera;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.fu_berlin.cdv.chasingpictures.R;

/**
 * Saves an image to a file in the background.
 *
 * @author Simon Kalt
 */
class SaveImageTask extends AsyncTask<Void, Void, File> {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final String TAG = "SaveImageTask";
    private static final File pictureStorageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    private final Context context;
    private final byte[] imageBytes;
    protected Exception exception;

    public SaveImageTask(Context context, byte[] imageBytes) {
        this.context = context;
        this.imageBytes = imageBytes;
    }

    /**
     * Retrieve any exception that occured while saving the file.
     *
     * @return An exception, or {@code null} if no exception was thrown.
     */
    @Nullable
    public Exception getException() {
        return exception;
    }

    @Override
    protected File doInBackground(@NonNull Void... params) {

        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

        if (pictureFile == null) {
            this.exception = new IOException("Error creating media file, check storage permissions.");
            return null;
        }

        // Write the image data to a file
        try {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(pictureFile));
            fos.write(imageBytes);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            this.exception = e;
            Log.e(TAG, "File not found.", e);
            return null;
        } catch (IOException e) {
            this.exception = e;
            Log.e(TAG, "Error accessing file.", e);
            return null;
        }

        // Refresh storage so our picture shows up in the gallery
        Log.i(TAG, "Scanning for file: " + pictureFile);
        MediaScannerConnection.scanFile(
                context,
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

        return pictureFile;
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(
                pictureStorageDirectory,
                context.getString(R.string.app_name).replace(" ", "")
        );

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            return new File(mediaStorageDir.getPath(), "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            return new File(mediaStorageDir.getPath(), "VID_" + timeStamp + ".mp4");
        }

        return null;
    }
}
