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
import com.chatcamp.uikit.messages.PermissionsCode;
import com.chatcamp.uikit.preview.ShowVideoActivity;
import com.chatcamp.uikit.utils.Directory;
import com.chatcamp.uikit.utils.DownloadFileListener;
import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 04/05/18.
 */

public class VideoMessageFactory extends MessageFactory<VideoMessageFactory.VideoMessageHolder> {


    private final WeakReference<Object> objectWeakReference;
    private Handler handler;
    private Map<String, VideoMessageFactory.HelperClass> messageIdHelperClassMap;

    public VideoMessageFactory(Activity activity) {
        objectWeakReference = new WeakReference<Object>(activity);
        handler = new Handler();
        messageIdHelperClassMap = new HashMap<>();
    }

    public VideoMessageFactory(Fragment fragment) {
        objectWeakReference = new WeakReference<Object>(fragment);
        handler = new Handler();
        messageIdHelperClassMap = new HashMap<>();
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
        TextView textView = view.findViewById(R.id.tv_video_name);

        Drawable backgroundDrawable = isMe ? messageStyle.getOutcomingBubbleDrawable() :
                messageStyle.getIncomingBubbleDrawable();
        int textColor = isMe ? messageStyle.getOutcomingTextColor() : messageStyle.getIncomingTextColor();
        textView.setTextColor(textColor);
        int videoIcon = isMe ? R.drawable.ic_video_white_placeholder : R.drawable.ic_video_placeholder;
        ImageView documentImage = view.findViewById(R.id.iv_video);
        documentImage.setImageResource(videoIcon);

        int downloadIcon = isMe ? R.drawable.ic_download_white : R.drawable.ic_download;
        ImageView downloadImage = view.findViewById(R.id.iv_download);
        downloadImage.setImageResource(downloadIcon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(backgroundDrawable);
        } else {
            view.setBackgroundDrawable(backgroundDrawable);
        }
        return new VideoMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(final VideoMessageHolder messageHolder, final Message message) {
        final Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            messageHolder.downloadIcon.setVisibility(View.INVISIBLE);
            return;
        }
        VideoMessageFactory.HelperClass helperClass = new VideoMessageFactory.HelperClass();

        if(messageIdHelperClassMap.get(message.getId()) != null) {
            messageHolder.downloadIcon.setVisibility(messageIdHelperClassMap.get(message.getId()).downloadIcon.getVisibility());
            messageHolder.progressBar.setVisibility(messageIdHelperClassMap.get(message.getId()).progressBar.getVisibility());
        }

        helperClass.downloadIcon = messageHolder.downloadIcon;
        helperClass.progressBar = messageHolder.progressBar;
        helperClass.imageUrl = message.getAttachment().getUrl();
        helperClass.isMe = messageSpecs.isMe;
        messageIdHelperClassMap.put(message.getId(), helperClass);
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

        messageHolder.view.setBackground(backgroundDrawable);

        if (FileUtils.fileExists(context, message.getAttachment().getUrl(), Directory.VIDEOS, messageSpecs.isMe)) {
            messageHolder.downloadIcon.setVisibility(View.INVISIBLE);
        } else {
            messageHolder.downloadIcon.setVisibility(View.VISIBLE);
        }
        messageHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null && v.getTag() instanceof Message) {
//                    if (!FileUtils.fileExists(context, message.getAttachment().getUrl(), Environment.DIRECTORY_MOVIES)) {
//                        messageHolder.progressBar.setVisibility(View.VISIBLE);
//                        messageHolder.progressBar.setProgress(0);
//                    } else {
//                        messageHolder.progressBar.setVisibility(View.INVISIBLE);
//                    }
                    onVideoClick((Message) v.getTag());
                }
            }
        });

    }


    private void onVideoClick(Message message) {
        Context context = Utils.getContext(objectWeakReference.get());

        if (context == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            Utils.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PermissionsCode.VIDEO_MESSAGE_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA, objectWeakReference.get());

        } else {
            downloadVideo(message);
        }
    }

    protected void downloadVideo(final Message message) {
        if (objectWeakReference.get() == null) {
            return;
        }
        if (FileUtils.fileExists(Utils.getContext(objectWeakReference.get()), message.getAttachment().getUrl(),
                Directory.VIDEOS, messageIdHelperClassMap.get(message.getId()).isMe)) {
            messageIdHelperClassMap.get(message.getId()).progressBar.setVisibility(View.INVISIBLE);
        } else {
            messageIdHelperClassMap.get(message.getId()).progressBar.setVisibility(View.VISIBLE);
        }
        messageIdHelperClassMap.get(message.getId()).downloadIcon.setVisibility(View.GONE);
        final String videoUrl = message.getAttachment().getUrl();
        if (!TextUtils.isEmpty(videoUrl)) {
            new Thread(new Runnable() {
                public void run() {
                    Uri path = null;
                    try {
                        Context context = Utils.getContext(objectWeakReference.get());
                        path = FileProvider.getUriForFile(context,
                                context.getPackageName() + ".chatcamp.fileprovider",
                                FileUtils.downloadFile(context, videoUrl,
                                        Directory.VIDEOS, messageIdHelperClassMap.get(message.getId()).isMe, new DownloadFileListener() {
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
                                                        if(progressBar != null) {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        //do nothing, I will not do anything when the permission is granted, let the user click again.
//        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                downloadVideo(view, progressBar, downloadIcon);
//            }
//        }
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

    public class HelperClass {
        public ProgressBar progressBar;
        public ImageView downloadIcon;
        public String imageUrl;
        public boolean isMe;
    }
}
