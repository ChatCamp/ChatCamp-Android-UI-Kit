package com.chatcamp.uikit.messages.messagetypes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
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
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.preview.ShowVideoActivity;
import com.chatcamp.uikit.utils.DownloadFileListener;
import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.TextViewFont;
import com.chatcamp.uikit.utils.Utils;

import java.lang.ref.WeakReference;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 04/05/18.
 */

public class VideoMessageFactory extends MessageFactory<VideoMessageFactory.VideoMessageHolder> {
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA = 106;

    private final WeakReference<Object> objectWeakReference;
    private View view;
    private ProgressBar progressBar;
    private ImageView downloadIcon;
    private Handler handler;

    public VideoMessageFactory(Activity activity) {
        objectWeakReference = new WeakReference<Object>(activity);
        handler = new Handler();
    }

    public VideoMessageFactory(Fragment fragment) {
        objectWeakReference = new WeakReference<Object>(fragment);
        handler = new Handler();
    }

    @Override
    public boolean isBindable(Message message) {
        if (message.getType().equals("attachment")) {
            if (message.getAttachment().isVideo()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public VideoMessageHolder createMessageHolder(ViewGroup cellView,
                                                  boolean isMe, LayoutInflater layoutInflater) {

        View view = layoutInflater.inflate(R.layout.layout_message_video, cellView, true);
        TextViewFont textView = view.findViewById(R.id.tv_video_name);

        Drawable backgroundDrawable = isMe ? messageStyle.getOutcomingBubbleDrawable() :
                messageStyle.getIncomingBubbleDrawable();
        int textColor = isMe ? messageStyle.getOutcomingTextColor() : messageStyle.getIncomingTextColor();
        textView.setTextColor(textColor);
        int videoIcon = isMe ? R.drawable.ic_video_white_placeholder : R.drawable.ic_video_placeholder;
        ImageView documentImage = view.findViewById(R.id.iv_video);
        documentImage.setImageResource(videoIcon);
        if(!TextUtils.isEmpty(messageStyle.getCustomFont())) {
            textView.setCustomFont(messageStyle.getCustomFont());
        }

        int downloadIcon = isMe ? R.drawable.ic_download_white : R.drawable.ic_download;
        ImageView downloadImage = view.findViewById(R.id.iv_download);
        downloadImage.setImageResource(downloadIcon);

        view.setBackground(backgroundDrawable);
        return new VideoMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(final VideoMessageHolder messageHolder, final Message message) {
        final Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            messageHolder.downloadIcon.setVisibility(View.INVISIBLE);
            return;
        }
        messageHolder.view.setTag(message);
        messageHolder.videoName.setText(message.getAttachment().getName());
        Drawable backgroundDrawable = messageHolder.view.getBackground().mutate();
        boolean isFirstMessage = messageSpecs.isFirstMessage;
        float cornerRadius = messageHolder.view.getContext()
                .getResources().getDimensionPixelSize(R.dimen.message_bubble_corners_radius);
        if (isFirstMessage) {
            float[] cornerRadii = messageSpecs.isMe ? new float[]{cornerRadius, cornerRadius,
                    0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius}
                    : new float[]{0f, 0f, cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius, cornerRadius, cornerRadius};
            ((GradientDrawable) backgroundDrawable).setCornerRadii(cornerRadii);
        } else {
            ((GradientDrawable) backgroundDrawable).setCornerRadius(cornerRadius);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            messageHolder.view.setBackground(backgroundDrawable);
        } else {
            messageHolder.view.setBackgroundDrawable(backgroundDrawable);
        }
        messageHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null && v.getTag() instanceof Message) {
                    if (!FileUtils.fileExists(context, message.getAttachment().getUrl(), Environment.DIRECTORY_MOVIES)) {
                        messageHolder.progressBar.setVisibility(View.VISIBLE);
                        messageHolder.progressBar.setProgress(0);
                    } else {
                        messageHolder.progressBar.setVisibility(View.INVISIBLE);
                    }
                    onVideoClick(v, messageHolder.progressBar, messageHolder.downloadIcon);
                }
            }
        });
        if (FileUtils.fileExists(context, message.getAttachment().getUrl(), Environment.DIRECTORY_MOVIES)) {
            messageHolder.downloadIcon.setVisibility(View.INVISIBLE);
        } else {
            messageHolder.downloadIcon.setVisibility(View.VISIBLE);
        }
    }


    private void onVideoClick(View v, ProgressBar progressBar, ImageView downloadIcon) {
        Context context = Utils.getContext(objectWeakReference.get());

        if (context == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            view = v;
            this.progressBar = progressBar;
            this.downloadIcon = downloadIcon;
            Utils.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA, objectWeakReference.get());

        } else {
            downloadVideo(v, progressBar, downloadIcon);
        }
    }

    protected void downloadVideo(View v, final ProgressBar progressBar, final ImageView downloadIcon) {
        if (objectWeakReference.get() == null) {
            return;
        }
        if (v.getTag() != null && v.getTag() instanceof Message) {
            if (downloadIcon != null) {
                downloadIcon.setVisibility(View.INVISIBLE);
            }
            final Message message = (Message) v.getTag();
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
                                            Environment.DIRECTORY_MOVIES, new DownloadFileListener() {
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
                                                            progressBar.setVisibility(View.INVISIBLE);

                                                        }
                                                    });
                                                }
                                            }));

                            final Uri finalPath = path;
                            Intent intent = new Intent(context, ShowVideoActivity.class);
                            intent.putExtra(ShowVideoActivity.VIDEO_URL, finalPath.toString());
                            Utils.startActivity(intent, objectWeakReference.get());
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
                downloadVideo(view, progressBar, downloadIcon);
            }
        }
    }

    public static class VideoMessageHolder extends MessageFactory.MessageHolder {
        ImageView videoImage;
        ProgressBar progressBar;
        ImageView downloadIcon;
        TextView videoName;
        View view;

        public VideoMessageHolder(View view) {
            this.view = view;
            videoImage = view.findViewById(R.id.iv_video);
            progressBar = view.findViewById(R.id.progress_bar);
            downloadIcon = view.findViewById(R.id.iv_download);
            videoName = view.findViewById(R.id.tv_video_name);
        }

    }
}
