package com.chatcamp.uikit.messages.sender;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;

import io.chatcamp.sdk.BaseChannel;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public class FileAttachmentSender extends AttachmentSender {

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT = 100;
    private static final int PICK_FILE_RESULT_CODE = 120;
    private WeakReference<Object> objectWeakReference;


    public FileAttachmentSender(@NonNull Activity activity, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        objectWeakReference = new WeakReference<Object>(activity);
    }

    public FileAttachmentSender(@NonNull Fragment fragment, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        objectWeakReference = new WeakReference<Object>(fragment);
    }

    @Override
    public void clickSend() {
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.requestPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT, objectWeakReference.get());

        } else {
            pickFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (objectWeakReference.get() != null) {
                    pickFile();
                }
            }
        }
    }

    private void pickFile() {
        Intent intent = new Intent();
        intent.setType("application/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Utils.startActivityForResult(Intent.createChooser(intent, "Select files"), PICK_FILE_RESULT_CODE, objectWeakReference.get());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_FILE_RESULT_CODE && dataFile != null) {
            Uri uriFile = dataFile.getData();
            uploadFile(uriFile);
        }
    }

    private void uploadFile(Uri uri) {
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            return;
        }
        String path = FileUtils.getPath(context, uri);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        String fileName = FileUtils.getFileName(context, uri);
        String contentType = context.getContentResolver().getType(uri);
        File file = new File(path);
        sendAttachment(file, fileName, contentType);
    }
}
