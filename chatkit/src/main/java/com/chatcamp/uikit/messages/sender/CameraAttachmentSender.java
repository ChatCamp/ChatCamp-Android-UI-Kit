package com.chatcamp.uikit.messages.sender;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCampException;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public class CameraAttachmentSender extends AttachmentSender {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA = 103;
    private static final int CAPTURE_MEDIA_RESULT_CODE = 123;
    private WeakReference<Object> objectWeakReference;
    private String currentPhotoPath;

    public CameraAttachmentSender(@NonNull Activity activity, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        objectWeakReference = new WeakReference<Object>(activity);
    }

    public CameraAttachmentSender(@NonNull Fragment fragment, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        objectWeakReference = new WeakReference<Object>(fragment);
    }

    @Override
    public void clickSend() {

        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            ChatCampException exception = new ChatCampException("Context is null", "CAMERA FILE UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA, objectWeakReference.get());
        } else {
            openCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (objectWeakReference.get() != null) {
                    openCamera();
                }
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Context context = Utils.getContext(objectWeakReference.get());
            if (context == null) {
                ChatCampException exception = new ChatCampException("Context is null", "CAMERA FILE UPLOAD ERROR");
                sendAttachmentError(exception);
                return;
            }
            File file = createImageFile();
            if (file == null) {
                ChatCampException exception = new ChatCampException("File is null", "CAMERA FILE UPLOAD ERROR");
                sendAttachmentError(exception);
                return;
            }
            //TODO take care of this file provider
            Uri photoURI = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".chatcamp.fileprovider",
                    file);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Intent chooserIntent = Intent.createChooser(takePictureIntent, "Capture Image or Video");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takeVideoIntent});
            Utils.startActivityForResult(chooserIntent, CAPTURE_MEDIA_RESULT_CODE, objectWeakReference.get());
        } catch (IOException e) {
            e.printStackTrace();
            ChatCampException exception = new ChatCampException(e.getMessage(), "CAMERA FILE UPLOAD ERROR");
            sendAttachmentError(exception);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            ChatCampException exception = new ChatCampException("Context is null", "CAMERA FILE UPLOAD ERROR");
            sendAttachmentError(exception);
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            ChatCampException exception = new ChatCampException("Context is null", "CAMERA FILE UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        String path = FileUtils.getPath(context, uri);
        String fileName = "";
        String contentType = "";
        File file;
        if (path == null) {
            path = uri.toString();
            fileName = new File(path).getName();
            contentType = "image/*";
        } else {
            fileName = FileUtils.getFileName(context, uri);
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                ContentResolver cr = context.getContentResolver();
                contentType = cr.getType(uri);
            } else {
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                        .toString());
                contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        fileExtension.toLowerCase());
            }
        }
        if (TextUtils.isEmpty(contentType)) {
            Log.e("CameraAttachmentSender", "content type is empty");
            ChatCampException exception = new ChatCampException("content type is empty", "CAMERA FILE UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        if (contentType.contains("image")) {
            file = new File(path);
            try {
                File compressedFile = createImageFile();
                if (compressedFile == null) {
                    Log.e("CameraAttachmentSender", "Error compressing file.");
                    ChatCampException exception = new ChatCampException("Error compressing file", "CAMERA FILE UPLOAD ERROR");
                    sendAttachmentError(exception);
                    return;
                }
                Bitmap bitmap = decodeSampledBitmapFromFile(path, 1280, 800);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(compressedFile));
                file = compressedFile;
            } catch (Throwable t) {
                Log.e("CameraAttachmentSender", "Error compressing file." + t.toString());
                ChatCampException exception = new ChatCampException("Error compressing file." + t.toString(), "CAMERA FILE UPLOAD ERROR");
                sendAttachmentError(exception);
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
