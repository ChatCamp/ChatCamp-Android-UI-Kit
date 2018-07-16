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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.messages.DownloadVideoService;
import com.chatcamp.uikit.messages.PermissionsCode;
import com.chatcamp.uikit.preview.ShowVideoActivity;
import com.chatcamp.uikit.utils.Directory;
import com.chatcamp.uikit.utils.DownloadProgressBar;
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

    private static final String TAG = "VideoMessageFactory";
    public static final String START = "start";
    public static final String PROGRESS = "progress";
    public static final String COMPLETE = "complete";
    public static final String STOP = "stop";

    private final WeakReference<Object> objectWeakReference;
    private Map<String, VideoMessageFactory.HelperClass> messageIdHelperClassMap = new HashMap<>();
    // ids of the messages that is being downloaded
    private static List<String> messageIds = new ArrayList<>();
    private DownloadResultReceiver downloadResultReceiver = new DownloadResultReceiver();

    public VideoMessageFactory(Activity activity) {
        objectWeakReference = new WeakReference<Object>(activity);
        // Since the first time onViewVisibilityChanged is called before se set factories we have to register the broadcast receiver here
        IntentFilter intentFilter = new IntentFilter(DownloadResultReceiver.ACTION);
        Utils.getContext(objectWeakReference.get()).registerReceiver(downloadResultReceiver, intentFilter);
    }

    public VideoMessageFactory(Fragment fragment) {
        objectWeakReference = new WeakReference<Object>(fragment);
        // Since the first time onViewVisibilityChanged is called before se set factories we have to register the broadcast receiver here
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
        ImageView videoImage = view.findViewById(R.id.iv_video);
        videoImage.setImageResource(videoIcon);

//        int downloadIcon = isMe ? R.drawable.ic_download_white : R.drawable.ic_download;
//        ImageView downloadImage = view.findViewById(R.id.iv_download);
//        downloadImage.setImageResource(downloadIcon);
        view.setBackground(backgroundDrawable);
        return new VideoMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(final VideoMessageHolder messageHolder, @NonNull final Message message) {
        final Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            return;
        }
        //TODO add comments
        VideoMessageFactory.HelperClass helperClass = new VideoMessageFactory.HelperClass();
        if (messageIds.contains(message.getId())) {
            messageHolder.progressBar.startDownload();
        } else {
            if (FileUtils.fileExists(context, message.getAttachment().getUrl(), Directory.VIDEOS, messageSpecs.isMe)) {
                messageHolder.progressBar.completeDownload();
            } else {
                messageHolder.progressBar.resetView();
            }
        }

        helperClass.progressBar = messageHolder.progressBar;
        helperClass.imageUrl = message.getAttachment().getUrl();
        helperClass.isMe = messageSpecs.isMe;
        messageIdHelperClassMap.put(message.getId(), helperClass);
        messageHolder.view.setTag(message);
        messageHolder.progressBar.setTag(message);
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
                    Log.e(TAG, "view clicked");
                }
            }
        });
        messageHolder.progressBar.setDownloadProgressbarClickListener(new DownloadProgressBar
                .DownloadProgressbarClickListener() {
            @Override
            public void onCancelClicked(Object object) {
                if (object != null && object instanceof Message) {
                    Log.e(TAG, "cancel clicked");
                    Message message = (Message) object;
                    String messageId = message.getId();
                    Intent progressIntent = new Intent(DownloadVideoService.CancelDownloadBroadcastReceiver.ACTION);
                    progressIntent.putExtra(DownloadVideoService.CancelDownloadBroadcastReceiver.MESSAGE_ID, messageId);
                    Context ctx = Utils.getContext(objectWeakReference.get());
                    if (ctx != null) {
                        ctx.sendBroadcast(progressIntent);
                    }
                }
            }

            @Override
            public void onDownloadClicked(Object object) {
                if (object != null && object instanceof Message) {
                    onVideoClick((Message) object);
                    Log.e(TAG, "view clicked");
                }
            }
        });
    }


    private void onVideoClick(Message message) {
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            Log.e(TAG, "could not find context");
            return;
        }
        if (messageIds.contains(message.getId())) {
            // if the video is already downloading we will not do anything on clicking that video again
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

    private void downloadVideo(final Message message) {
        if (objectWeakReference.get() == null) {
            Log.e(TAG, "could not find context");
            return;
        }
        // we are setting progressbar and download icon visibility here so that when we are trying to get the file
        // name it might take time and user will be shown nothing so we are showing the progressbar.
        ///TODO add null check for map items
        if (FileUtils.fileExists(Utils.getContext(objectWeakReference.get()), message.getAttachment().getUrl(),
                Directory.VIDEOS, messageIdHelperClassMap.get(message.getId()).isMe)) {
            messageIdHelperClassMap.get(message.getId()).progressBar.completeDownload();
//            messageIdHelperClassMap.get(message.getId()).cancelDownloadIcon.setVisibility(View.INVISIBLE);
            Uri path = null;
            if ((path = FileUtils.getLocalFilePath(Utils.getContext(objectWeakReference.get()), Directory.VIDEOS,
                    message.getAttachment().getUrl(), messageIdHelperClassMap.get(message.getId()).isMe)) != null) {
                Intent showVideoIntent = new Intent(Utils.getContext(objectWeakReference.get()), ShowVideoActivity.class);
                showVideoIntent.putExtra(ShowVideoActivity.VIDEO_URL, path.toString());
                Utils.startActivity(showVideoIntent, Utils.getContext(objectWeakReference.get()));
            }
            return;
        } else {
            messageIdHelperClassMap.get(message.getId()).progressBar.startDownload();
        }
        messageIds.add(message.getId());
        Log.e(TAG, "message" + message.getId() + "added to messageIds");
        final String videoUrl = message.getAttachment().getUrl();
        if (!TextUtils.isEmpty(videoUrl)) {
            Intent intent = new Intent(Utils.getContext(objectWeakReference.get()), DownloadVideoService.class);
            intent.putExtra(DownloadVideoService.VIDEO_URL, videoUrl);
            intent.putExtra(DownloadVideoService.IS_ME, messageIdHelperClassMap.get(message.getId()).isMe);
            intent.putExtra(DownloadVideoService.MESSAGE_ID, message.getId());
            Utils.getContext(objectWeakReference.get()).startService(intent);
        } else {
            Log.e(TAG, "The video url is empty, Most probably we are getting it null from server");
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
        DownloadProgressBar progressBar;
        TextView videoName;
        View view;

        public VideoMessageHolder(View view) {
            this.view = view;
            videoImage = view.findViewById(R.id.iv_video);
            progressBar = view.findViewById(R.id.video_progress_bar);
            videoName = view.findViewById(R.id.tv_video_name);
        }
    }

    @Override
    public void onViewVisibilityChange(int visibility) {
        if (visibility == View.VISIBLE) {
            IntentFilter intentFilter = new IntentFilter(DownloadResultReceiver.ACTION);
            Utils.getContext(objectWeakReference.get()).registerReceiver(downloadResultReceiver, intentFilter);
        } else {
            try {
                //We need to add try catch here as the app crashes when the receiver is not registered and
                // we try to unregister it. and there is no api to test it, bloody android
                Utils.getContext(objectWeakReference.get()).unregisterReceiver(downloadResultReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(String messageId) {
        messageIdHelperClassMap.remove(messageId);
    }

    public class HelperClass {
        public DownloadProgressBar progressBar;
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
            if (intent.getAction() != null && intent.getAction().equals(ACTION)) {
                String messageId = intent.getStringExtra(MESSAGE_ID);
                if (!TextUtils.isEmpty(messageId)) {
                    if (intent.hasExtra(STATUS) && intent.getStringExtra(STATUS).equals(START)) {
                        // download has started
                        if (!messageIds.contains(messageId)) {
                            messageIds.add(messageId);
                            Log.e(TAG, "message" + messageId + "added to messageIds in getting progress");
                        }
                        messageIdHelperClassMap.get(messageId).progressBar.startDownload();
                    } else if (intent.hasExtra(STATUS) && intent.getStringExtra(STATUS).equals(VideoMessageFactory.PROGRESS)) {
                        if (!messageIds.contains(messageId)) {
                            messageIds.add(messageId);
                            Log.e(TAG, "message" + messageId + "added to messageIds in getting progress");
                            return;
                        }
                        // Download is still in progress
                        if (messageIdHelperClassMap.get(messageId) != null) {
                            messageIdHelperClassMap.get(messageId).progressBar.setProgress(intent.getIntExtra(PROGRESS, 0));
                        }
                    } else if (intent.hasExtra(STATUS) && intent.getStringExtra(STATUS).equals(COMPLETE)) {
                        if (intent.hasExtra(VIDEO_URI)) {
                            //Download is complete and we have got the video url
                            messageIdHelperClassMap.get(messageId).progressBar.completeDownload();
                            messageIds.remove(messageId);
                            Log.e(TAG, "message" + messageId + "removed from messageIds in getting progress");
                            if (messageIds.size() == 0) {
                                Intent showVideoIntent = new Intent(context, ShowVideoActivity.class);
                                showVideoIntent.putExtra(ShowVideoActivity.VIDEO_URL, intent.getStringExtra(VIDEO_URI));
                                Utils.startActivity(showVideoIntent, Utils.getContext(objectWeakReference.get()));
                            }
                        } else {
                            // Download is complete but we have still not got the video url
                            if (messageIdHelperClassMap.get(messageId) != null) {
                                messageIdHelperClassMap.get(messageId).progressBar.setProgress(intent.getIntExtra(PROGRESS, 100));
                            }
                        }
                    } else if (intent.hasExtra(STATUS) && intent.getStringExtra(STATUS).equals(STOP)) {
                        // Download has been stopped
                        Log.e(TAG, "message" + messageId + "download stopped");
                        if (messageIdHelperClassMap.get(messageId) != null ) {
                            messageIdHelperClassMap.get(messageId).progressBar.cancelDownload();
                        }
                        messageIds.remove(messageId);
                    } else {
                        Log.e(TAG, "status sent from the downloadService is not correct, this should never be the case. " +
                                "Please contact developer for this");
                    }
                } else {
                    Log.e(TAG, "message set in the DownloadVideoService is empty, " +
                            "this should never be the case, Please contact developer for this");
                }

            }
        }
    }
}
