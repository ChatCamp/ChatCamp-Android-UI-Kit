package com.chatcamp.uikit.messages.sender;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.chatcamp.uikit.messages.ActivityRequestCode;
import com.chatcamp.uikit.messages.PermissionsCode;
import com.chatcamp.uikit.preview.MediaPreviewActivity;
import com.chatcamp.uikit.utils.Directory;
import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCampException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public class GalleryAttachmentSender extends AttachmentSender {

    private WeakReference<Object> objectWeakReference;

    public GalleryAttachmentSender(@NonNull Activity activity, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        objectWeakReference = new WeakReference<Object>(activity);
    }

    public GalleryAttachmentSender(@NonNull Fragment fragment, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        objectWeakReference = new WeakReference<Object>(fragment);
    }

    @Override
    public void clickSend() {
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            ChatCampException exception = new ChatCampException("Context is null", "GALLERY UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.requestPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PermissionsCode.GALLERY_ATTACHMENT_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA, objectWeakReference.get());

        } else {
            chooseMedia();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PermissionsCode.GALLERY_ATTACHMENT_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (objectWeakReference.get() != null) {
                    chooseMedia();
                }
            }
        }
    }

    private void chooseMedia() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        Utils.startActivityForResult(intent, ActivityRequestCode.GALLERY_ATTACHMENT_PICK_MEDIA_RESULT_CODE, objectWeakReference.get());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        if (resultCode == RESULT_OK && dataFile != null) {
            if (requestCode == ActivityRequestCode.GALLERY_ATTACHMENT_PICK_MEDIA_RESULT_CODE) {
                Uri uri = dataFile.getData();
                if (uri == null) {
                    ChatCampException exception = new ChatCampException("Picked file is not valid", "GALLERY UPLOAD ERROR");
                    sendAttachmentError(exception);
                    return;
                }
                Context context = Utils.getContext(objectWeakReference.get());
                if (context == null) {
                    ChatCampException exception = new ChatCampException("Context is null", "GALLERY UPLOAD ERROR");
                    sendAttachmentError(exception);
                    return;
                }
                Intent intent = new Intent(context, MediaPreviewActivity.class);
                intent.putExtra(MediaPreviewActivity.MEDIA_URI, uri.toString());
                Utils.startActivityForResult(intent, ActivityRequestCode.GALLERY_ATTACHMENT_PREVIEW_FILE_RESULT_CODE, objectWeakReference.get());
            } else if (requestCode == ActivityRequestCode.GALLERY_ATTACHMENT_PREVIEW_FILE_RESULT_CODE) {
                String uriMedia = dataFile.getExtras().getString(MediaPreviewActivity.MEDIA_URI);
                uploadFile(Uri.parse(uriMedia));
            }
        }
    }

    private void uploadFile(Uri uri) {
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            ChatCampException exception = new ChatCampException("Context is null", "GALLERY UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        String path = null;//FileUtils.getPath(context, uri);
        if (TextUtils.isEmpty(path)) {
            try {

                //TODO Do this in background async task
                ParcelFileDescriptor parcelFileDescriptor =
                        context.getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                Bitmap bitmap = getResizedBitmap(image, 1280, 800);
                parcelFileDescriptor.close();
                File compressedFile = createImageFile();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(compressedFile));
                File file = compressedFile;
                String fileName = FileUtils.getFileName(context, uri);
                String contentType = context.getContentResolver().getType(uri);
                sendAttachment(file, fileName, contentType);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("GalleryAttachmentSender", "File path is null");
                ChatCampException exception = new ChatCampException("File path is null", "GALLERY UPLOAD ERROR");
                sendAttachmentError(exception);
            }
            return;
        }
        String fileName = FileUtils.getFileName(context, uri);
        String contentType = context.getContentResolver().getType(uri);
        if (TextUtils.isEmpty(contentType)) {
            ChatCampException exception = new ChatCampException("File content type is null", "GALLERY UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        File file = new File(path);
//        if (contentType.contains("image")) {
//            file = new File(path);
//            try {
//                File compressedFile = createImageFile();
//                if (compressedFile == null) {
//                    ChatCampException exception = new ChatCampException("Error compressing image", "GALLERY UPLOAD ERROR");
//                    sendAttachmentError(exception);
//                    return;
//                }
//                Bitmap bitmap = decodeSampledBitmapFromFile(path, 1280, 800);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(compressedFile));
//                file = compressedFile;
//            } catch (Throwable t) {
//                Log.e("ERROR", t.toString());
//                ChatCampException exception = new ChatCampException(t.toString(), "GALLERY UPLOAD ERROR");
//                sendAttachmentError(exception);
//                t.printStackTrace();
//            }
//        } else {
//            file = new File(path);
//        }
        sendAttachment(file, fileName, contentType);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            ChatCampException exception = new ChatCampException("Context is null", "GALLERY UPLOAD ERROR");
            sendAttachmentError(exception);
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File image = FileUtils.getFile(Directory.PICTURES, imageFileName, true);
        image.createNewFile();
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
        return image;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

//    // TODO should do this in background
//    private Bitmap decodeSampledBitmapFromFile(String path, int reqHeight,
//                                               int reqWidth) {
//
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(bitmap, options);
//
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        int inSampleSize = 1;
//
//        if (height > reqHeight) {
//            inSampleSize = Math.round((float) height / (float) reqHeight);
//        }
//        int expectedWidth = width / inSampleSize;
//
//        if (expectedWidth > reqWidth) {
//            inSampleSize = Math.round((float) width / (float) reqWidth);
//        }
//        options.inSampleSize = inSampleSize;
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(path, options);
//    }
}
