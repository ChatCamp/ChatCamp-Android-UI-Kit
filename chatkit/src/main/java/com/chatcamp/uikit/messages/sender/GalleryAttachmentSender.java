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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.chatcamp.uikit.preview.MediaPreviewActivity;
import com.chatcamp.uikit.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.chatcamp.sdk.BaseChannel;

import static android.app.Activity.RESULT_OK;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public class GalleryAttachmentSender extends AttachmentSender {

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA = 101;
    private static final int PICK_MEDIA_RESULT_CODE = 121;
    private static final int PREVIEW_FILE_RESULT_CODE = 102;
    private WeakReference<Activity> activityWeakReference;

    public GalleryAttachmentSender(@NonNull Activity activity, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void clickSend() {
        Activity activity = activityWeakReference.get();
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA);

        } else {
            chooseMedia(activity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (activityWeakReference.get() != null) {
                    chooseMedia(activityWeakReference.get());
                }
            }
        }
    }

    private void chooseMedia(Activity activity) {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/* video/*");
            activity.startActivityForResult(Intent.createChooser(intent, "Select Media"), PICK_MEDIA_RESULT_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
            activity.startActivityForResult(intent, PICK_MEDIA_RESULT_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        if (resultCode == RESULT_OK && dataFile != null) {
            if (requestCode == PICK_MEDIA_RESULT_CODE) {
                Uri uri = dataFile.getData();
                if (uri == null) {
                    return;
                }
                Activity activity = activityWeakReference.get();
                if (activity == null) {
                    return;
                }
                Intent intent = new Intent(activity, MediaPreviewActivity.class);
                intent.putExtra(MediaPreviewActivity.IMAGE_URI, uri.toString());
                activity.startActivityForResult(intent, PREVIEW_FILE_RESULT_CODE);
            } else if (requestCode == PREVIEW_FILE_RESULT_CODE) {
                String uriMedia = dataFile.getExtras().getString(MediaPreviewActivity.IMAGE_URI);
                uploadFile(Uri.parse(uriMedia));
            }
        }
    }

    private void uploadFile(Uri uri) {
        Activity activity = activityWeakReference.get();
        if (activity == null) {
            return;
        }
        String path = FileUtils.getPath(activity, uri);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        String fileName = FileUtils.getFileName(activity, uri);
        String contentType = activity.getContentResolver().getType(uri);
        if (TextUtils.isEmpty(contentType)) {
            return;
        }
        File file;
        if (contentType.contains("image")) {
            file = new File(path);
            try {
                File compressedFile = createImageFile();
                if(compressedFile == null) return;
                Bitmap bitmap = decodeSampledBitmapFromFile(path, 1280, 800);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(compressedFile));
                file = compressedFile;
            } catch (Throwable t) {
                Log.e("ERROR", t.toString());
                t.printStackTrace();
            }
        } else {
            file = new File(path);
        }
        sendAttachment(file, fileName, contentType);
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

        return image;
    }

    // TODO should do this in background
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
