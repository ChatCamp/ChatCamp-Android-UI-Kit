package com.chatcamp.uikit.messages.sender;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.chatcamp.uikit.utils.FileUtils;

import java.io.File;
import java.lang.ref.WeakReference;

import io.chatcamp.sdk.BaseChannel;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public class FileAttachmentSender extends AttachmentSender {

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT = 100;
    private static final int PICK_FILE_RESULT_CODE = 120;
    private WeakReference<Activity> activityWeakReference;


    public FileAttachmentSender(@NonNull Activity activity, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
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
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT);

        } else {
            pickFile(activity);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_DOCUMENT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (activityWeakReference.get() != null) {
                    pickFile(activityWeakReference.get());
                }
            }
        }
    }

    private void pickFile(Activity activity) {
        Intent intent = new Intent();
        intent.setType("application/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select files"), PICK_FILE_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_FILE_RESULT_CODE && dataFile != null) {
            Uri uriFile = dataFile.getData();
            uploadFile(uriFile);
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
        File file = new File(path);
        sendAttachment(file, fileName, contentType);
    }
}
