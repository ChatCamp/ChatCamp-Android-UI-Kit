package com.chatcamp.uikit.messages.messagetypes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;
import com.chatcamp.uikit.R;
import com.chatcamp.uikit.preview.ShowImageActivity;
import com.chatcamp.uikit.utils.DownloadFileListener;
import com.chatcamp.uikit.utils.FileUtils;

import java.lang.ref.WeakReference;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 03/05/18.
 */

public class ImageMessageFactory extends MessageFactory<ImageMessageFactory.ImageMessageHolder> {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA = 105;

    private final WeakReference<Activity> activityWeakReference;
    private View view;
    private ProgressBar progressBar;
    private ImageView downloadIcon;

    private Handler handler;

    public ImageMessageFactory(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
        handler = new Handler();
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
        final Activity activity = activityWeakReference.get();
        if (activity == null) {
            messageHolder.downloadIcon.setVisibility(View.GONE);
            return;
        }
        if (FileUtils.fileExists(activity, message.getAttachment().getUrl(), Environment.DIRECTORY_PICTURES)) {
            messageHolder.downloadIcon.setVisibility(View.GONE);
        } else {
            messageHolder.downloadIcon.setVisibility(View.VISIBLE);
        }

        Picasso.with(activity).load(message.getAttachment().getUrl()).into(messageHolder.imageView);

        messageHolder.imageView.setTag(message);
        messageHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null && v.getTag() instanceof Message) {
                    if (!FileUtils.fileExists(activity, message.getAttachment().getUrl(), Environment.DIRECTORY_PICTURES)) {
                        messageHolder.progressBar.setVisibility(View.VISIBLE);
                        messageHolder.progressBar.setProgress(0);
                    } else {
                        messageHolder.progressBar.setVisibility(View.GONE);
                    }
                    onImageClick(v, messageHolder.progressBar, messageHolder.downloadIcon);
                }
            }
        });


    }

    private void onImageClick(View v, ProgressBar progressBar, ImageView downloadIcon) {
        Activity activity = activityWeakReference.get();

        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            view = v;
            this.progressBar = progressBar;
            this.downloadIcon = downloadIcon;
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA);

        } else {
            downloadImage(v, progressBar, downloadIcon, activity);
        }
    }

    protected void downloadImage(View v, final ProgressBar progressBar, final ImageView downloadIcon,
                                 final Activity activity) {
        if (activity == null) {
            return;
        }
        if (v.getTag() != null && v.getTag() instanceof Message) {
            if (downloadIcon != null) {
                downloadIcon.setVisibility(View.GONE);
            }
            final Message message = (Message) v.getTag();
            final String imageUrl = message.getAttachment().getUrl();
            if (!TextUtils.isEmpty(imageUrl)) {
                new Thread(new Runnable() {
                    public void run() {
                        Uri path = null;
                        try {
                            path = FileProvider.getUriForFile(activity,
                                    activity.getPackageName() + ".chatcamp.fileprovider",
                                    FileUtils.downloadFile(activity, imageUrl,
                                            Environment.DIRECTORY_PICTURES, new DownloadFileListener() {
                                                @Override
                                                public void downloadProgress(final int progress) {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (progressBar != null) {
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
                                                            progressBar.setVisibility(View.GONE);

                                                        }
                                                    });
                                                }
                                            }));

                            final Uri finalPath = path;
                            Intent intent = new Intent(activity, ShowImageActivity.class);
                            intent.putExtra(ShowImageActivity.IMAGE_URL, finalPath.toString());
                            activity.startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadImage(view, progressBar, downloadIcon, activityWeakReference.get());
            }
        }
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
}
