package com.chatcamp.uikit.messages.messagetypes;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.messages.DownloadVideoIntentService;
import com.chatcamp.uikit.messages.PermissionsCode;
import com.chatcamp.uikit.preview.ShowVideoActivity;
import com.chatcamp.uikit.utils.Directory;
import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 04/05/18.
 */

public class VideoMessageFactory extends MessageFactory<VideoMessageFactory.VideoMessageHolder> {

    private final WeakReference<Object> objectWeakReference;
    private Map<String, VideoMessageFactory.HelperClass> messageIdHelperClassMap;
    // id of the message that is being downloaded, only one file can be downloaded at a time
    private List<String> messageIds = new ArrayList<>();
    private DownloadResultReceiver downloadResultReceiver = new DownloadResultReceiver();

    public VideoMessageFactory(Activity activity) {
        objectWeakReference = new WeakReference<Object>(activity);
        messageIdHelperClassMap = new HashMap<>();
        // Since the first time onViewVisibilityChanged is called before se set factories we have to register the broadcast receiver here
        IntentFilter intentFilter = new IntentFilter(DownloadResultReceiver.ACTION);
        Utils.getContext(objectWeakReference.get()).registerReceiver(downloadResultReceiver, intentFilter);
    }

    public VideoMessageFactory(Fragment fragment) {
        objectWeakReference = new WeakReference<Object>(fragment);
        messageIdHelperClassMap = new HashMap<>();
        IntentFilter intentFilter = new IntentFilter(DownloadResultReceiver.ACTION);
        Utils.getContext(objectWeakReference.get()).registerReceiver(downloadResultReceiver, intentFilter);
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
        VideoMessageFactory.HelperClass helperClass = new VideoMessageFactory.HelperClass();
        if (FileUtils.fileExists(context, message.getAttachment().getUrl(), Directory.VIDEOS, messageSpecs.isMe)) {
            messageHolder.downloadIcon.setVisibility(View.INVISIBLE);
            messageHolder.progressBar.setVisibility(View.INVISIBLE);
        } else {
            if (messageIds.contains(message.getId())) {
                messageHolder.progressBar.setVisibility(View.VISIBLE);
                messageHolder.downloadIcon.setVisibility(View.GONE);
            } else {
                messageHolder.progressBar.setVisibility(View.INVISIBLE);
                messageHolder.downloadIcon.setVisibility(View.VISIBLE);
            }
        }

        helperClass.downloadIcon = messageHolder.downloadIcon;
        helperClass.progressBar = messageHolder.progressBar;
        helperClass.imageUrl = message.getAttachment().getUrl();
        helperClass.isMe = messageSpecs.isMe;
        Log.d("adding to map", "adding to map" + helperClass.progressBar.toString());
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
        messageHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null && v.getTag() instanceof Message) {
                    onVideoClick((Message) v.getTag());
                }
            }
        });

    }


    private void onVideoClick(Message message) {
        Context context = Utils.getContext(objectWeakReference.get());
        if (messageIds.contains(message.getId())) {
            // if the video is already downloading we will not do anything on clicking that video again
            return;
        }

        if (context == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        messageIds.add(message.getId());
        if (FileUtils.fileExists(Utils.getContext(objectWeakReference.get()), message.getAttachment().getUrl(),
                Directory.VIDEOS, messageIdHelperClassMap.get(message.getId()).isMe)) {
            messageIdHelperClassMap.get(message.getId()).progressBar.setVisibility(View.INVISIBLE);
        } else {
            messageIdHelperClassMap.get(message.getId()).progressBar.setVisibility(View.VISIBLE);
        }
        messageIdHelperClassMap.get(message.getId()).downloadIcon.setVisibility(View.GONE);
        final String videoUrl = message.getAttachment().getUrl();
        if (!TextUtils.isEmpty(videoUrl)) {
            Intent intent = new Intent(Utils.getContext(objectWeakReference.get()), DownloadVideoIntentService.class);
            intent.putExtra(DownloadVideoIntentService.VIDEO_URL, videoUrl);
            intent.putExtra(DownloadVideoIntentService.IS_ME, messageIdHelperClassMap.get(message.getId()).isMe);
            intent.putExtra(DownloadVideoIntentService.MESSAGE_ID, message.getId());
            Utils.getContext(objectWeakReference.get()).startService(intent);
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

    @Override
    public void onViewVisibilityChange(int visibility) {
        if(visibility == View.VISIBLE) {
            IntentFilter intentFilter = new IntentFilter(DownloadResultReceiver.ACTION);
            Utils.getContext(objectWeakReference.get()).registerReceiver(downloadResultReceiver, intentFilter);
        } else {
            try {
                //We need to add try catch here as the app crashes when the receiver is not registered and
                // we try to unregister it. and there is no api to test it, bloody android
                Utils.getContext(objectWeakReference.get()).unregisterReceiver(downloadResultReceiver);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class HelperClass {
        public ProgressBar progressBar;
        public ImageView downloadIcon;
        public String imageUrl;
        public boolean isMe;
    }

    public class DownloadResultReceiver extends BroadcastReceiver {
        public static final String PROGRESS = "progress";
        public static final String STATUS = "status";
        public static final String MESSAGE_ID = "message_id";
        public static final String VIDEO_URI = "video_uri";
        public static final String ACTION = "io.chatcamp.chatcamp.download_video";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("receiving data", "receiving data");
            String messageId = intent.getStringExtra(MESSAGE_ID);
            if (!TextUtils.isEmpty(messageId)) {
                if (intent.hasExtra(STATUS) && intent.getStringExtra(STATUS).equals("progress")) {
                    messageIdHelperClassMap.get(messageId).progressBar.setVisibility(View.VISIBLE);
                    messageIdHelperClassMap.get(messageId).progressBar.setProgress(intent.getIntExtra(PROGRESS, 0));

                } else if (intent.hasExtra(STATUS) && intent.getStringExtra(STATUS).equals("complete")) {
                    if (intent.hasExtra(VIDEO_URI)) {
                        messageIdHelperClassMap.get(messageId).progressBar.setVisibility(View.INVISIBLE);
                        messageIds.remove(messageId);
//                        messageIdHelperClassMap.get(messageId).progressBar.setProgress(intent.getIntExtra(PROGRESS, 100));
                        Intent showVideoIntent = new Intent(context, ShowVideoActivity.class);
                        showVideoIntent.putExtra(ShowVideoActivity.VIDEO_URL, intent.getStringExtra(VIDEO_URI));
                        Utils.startActivity(showVideoIntent, Utils.getContext(objectWeakReference.get()));
                    } else {
                        messageIdHelperClassMap.get(messageId).progressBar.setVisibility(View.VISIBLE);
                        messageIdHelperClassMap.get(messageId).progressBar.setProgress(intent.getIntExtra(PROGRESS, 100));
                    }
                }
            }

        }
    }
}
