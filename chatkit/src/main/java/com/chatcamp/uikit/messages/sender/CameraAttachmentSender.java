package com.chatcamp.uikit.messages.sender;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.chatcamp.uikit.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.chatcamp.sdk.BaseChannel;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public class CameraAttachmentSender extends AttachmentSender {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA = 103;
    private static final int CAPTURE_MEDIA_RESULT_CODE = 123;
    private WeakReference<Activity> activityWeakReference;
    private String currentPhotoPath;

    public CameraAttachmentSender(@NonNull Activity activity, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void clickSend() {
        Activity activity = activityWeakReference.get();
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||  ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA);

        } else {
            openCamera(activity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (activityWeakReference.get() != null) {
                    openCamera(activityWeakReference.get());
                }
            }
        }
    }

    private void openCamera(Activity activity) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File file = createImageFile();
            if (file == null) {
                return;
            }
            //TODO take care of this file provider
            Uri photoURI = FileProvider.getUriForFile(activity,
                    activity.getPackageName() + ".chatcamp.fileprovider",
                    file);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Intent chooserIntent = Intent.createChooser(takePictureIntent, "Capture Image or Video");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});
            activity.startActivityForResult(chooserIntent, CAPTURE_MEDIA_RESULT_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        Activity activity = activityWeakReference.get();
        if (activity == null) {
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        if (resultCode == Activity.RESULT_OK && requestCode == CAPTURE_MEDIA_RESULT_CODE) {
            Uri uri;
            if (dataFile == null || dataFile.getData() == null) {
                uri = Uri.parse(currentPhotoPath);
            } else {
                uri = dataFile.getData();
            }
            uploadFile(uri);
        }
    }

    private void uploadFile(Uri uri) {
        Activity activity = activityWeakReference.get();
        if (activity == null) {
            return;
        }
        String path = FileUtils.getPath(activity, uri);
        String fileName = "";
        String contentType = "";
        File file;
        if (path == null) {
            path = uri.toString();
            fileName = new File(path).getName();
            contentType = "image/*";
        } else {
            fileName = FileUtils.getFileName(activity, uri);
            contentType = activity.getContentResolver().getType(uri);
        }
        if (TextUtils.isEmpty(contentType)) {
            return;
        }
        if (contentType.contains("image")) {
            file = new File(path);
            try {
                File compressedFile = createImageFile();
                if (compressedFile == null) {
                    return;
                }
                Bitmap bitmap = decodeSampledBitmapFromFile(path, 1280, 800);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(compressedFile));
                file = compressedFile;
            } catch (Throwable t) {
                Log.e("ERROR", "Error compressing file." + t.toString());
                t.printStackTrace();
            }
        } else {
            file = new File(path);
        }

        sendAttachment(file, fileName, contentType);
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqHeight,
                                               int reqWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth) {
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}
