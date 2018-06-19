package com.chatcamp.uikit.messages.messagetypes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.messages.PermissionsCode;
import com.chatcamp.uikit.preview.ShowImageActivity;
import com.chatcamp.uikit.utils.Directory;
import com.chatcamp.uikit.utils.DownloadFileListener;
import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 03/05/18.
 */

public class ImageMessageFactory extends MessageFactory<ImageMessageFactory.ImageMessageHolder> {

    private final WeakReference<Object> objectWeakReference;
    private Map<String, HelperClass> messageIdHelperClassMap;

    private Handler handler;

    public ImageMessageFactory(Activity activity) {
        objectWeakReference = new WeakReference<Object>(activity);
        handler = new Handler();
        messageIdHelperClassMap = new HashMap<>();
    }

    public ImageMessageFactory(Fragment fragment) {
        objectWeakReference = new WeakReference<Object>(fragment);
        handler = new Handler();
        messageIdHelperClassMap = new HashMap<>();
    }

    @Override
    public boolean isBindable(Message message) {
        if (message.getType().equals("attachment")) {
            if (message.getAttachment().isImage()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ImageMessageHolder createMessageHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        View view = layoutInflater.inflate(R.layout.layout_message_image, cellView, true);
        int downloadIcon = isMe ? R.drawable.ic_download_white : R.drawable.ic_download;
        ImageView downloadImage = view.findViewById(R.id.iv_download);
        downloadImage.setImageResource(downloadIcon);
        return new ImageMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(final ImageMessageHolder messageHolder, final Message message) {
        final Context context = Utils.getContext(objectWeakReference.get());
        HelperClass helperClass = new HelperClass();
        helperClass.downloadIcon = messageHolder.downloadIcon;
        helperClass.progressBar = messageHolder.progressBar;
        helperClass.imageUrl = message.getAttachment().getUrl();
        helperClass.isMe = messageSpecs.isMe;
        messageIdHelperClassMap.put(message.getId(), helperClass);
        if (context == null) {
            messageHolder.downloadIcon.setVisibility(View.GONE);
            return;
        }
        if (FileUtils.fileExists(context, message.getAttachment().getUrl(), Directory.PICTURES, messageSpecs.isMe)) {
            messageHolder.downloadIcon.setVisibility(View.GONE);
        } else {
            messageHolder.downloadIcon.setVisibility(View.VISIBLE);
        }

        Picasso.with(context).load(message.getAttachment().getUrl()).into(messageHolder.imageView);

        messageHolder.imageView.setTag(message);
        messageHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null && v.getTag() instanceof Message) {
                    onImageClick((Message) v.getTag());
                }
            }
        });


    }

    private void onImageClick(Message message) {
        Context context = Utils.getContext(objectWeakReference.get());

        if (context == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionsCode.IMAGE_MESSAGE_FACTORY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA, objectWeakReference.get());
        } else {
            downloadImage(message);
        }
    }

    protected void downloadImage(final Message message) {
        if (objectWeakReference.get() == null) {
            return;
        }
        if(FileUtils.fileExists(Utils.getContext(objectWeakReference.get()), message.getAttachment().getUrl(),
                Directory.PICTURES, messageIdHelperClassMap.get(message.getId()).isMe)) {
            messageIdHelperClassMap.get(message.getId()).progressBar.setVisibility(View.GONE);
        } else {
            messageIdHelperClassMap.get(message.getId()).progressBar.setVisibility(View.VISIBLE);
        }
        messageIdHelperClassMap.get(message.getId()).downloadIcon.setVisibility(View.GONE);
        final String imageUrl = message.getAttachment().getUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            new Thread(new Runnable() {
                public void run() {
                    Uri path = null;
                    try {
                        Context context = Utils.getContext(objectWeakReference.get());
                        path = FileProvider.getUriForFile(context,
                                context.getPackageName() + ".chatcamp.fileprovider",
                                FileUtils.downloadFile(context, imageUrl,
                                        Directory.PICTURES, messageIdHelperClassMap.get(message.getId()).isMe, new DownloadFileListener() {
                                            @Override
                                            public void downloadProgress(final int progress) {
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ProgressBar progressBar = messageIdHelperClassMap
                                                                .get(message.getId()) == null ? null : messageIdHelperClassMap
                                                                .get(message.getId()).progressBar;
                                                        if ( progressBar!= null && progressBar.getVisibility() == View.VISIBLE) {
                                                            progressBar.setProgress(progress);
                                                        }
                                                    }
                                                });
                                            }

                                            @Override
                                            public void downloadComplete() {
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ProgressBar progressBar = messageIdHelperClassMap
                                                                .get(message.getId()) == null ? null : messageIdHelperClassMap
                                                                .get(message.getId()).progressBar;
                                                        progressBar.setVisibility(View.GONE);

                                                    }
                                                });
                                            }
                                        }));

                        final Uri finalPath = path;
                        Intent intent = new Intent(context, ShowImageActivity.class);
                        intent.putExtra(ShowImageActivity.IMAGE_URL, finalPath.toString());
                        Utils.startActivity(intent, objectWeakReference.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        //do nothing, I will not do anything when the permission is granted, let the click again.
//        if (requestCode == PermissionsCode.IMAGE_MESSAGE_FACTORY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                downloadImage(view, progressBar, downloadIcon);
//            }
//        }
    }

    public static class ImageMessageHolder extends MessageFactory.MessageHolder {
        ImageView imageView;
        ProgressBar progressBar;
        ImageView downloadIcon;

        public ImageMessageHolder(View view) {
            imageView = view.findViewById(R.id.iv_image);
            progressBar = view.findViewById(R.id.progress_bar);
            downloadIcon = view.findViewById(R.id.iv_download);
        }

    }

    public class HelperClass {
        public ProgressBar progressBar;
        public ImageView downloadIcon;
        public String imageUrl;
        public boolean isMe;
    }
}
