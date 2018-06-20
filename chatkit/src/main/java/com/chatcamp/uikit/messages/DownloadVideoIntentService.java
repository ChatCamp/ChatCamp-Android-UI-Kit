package com.chatcamp.uikit.messages;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.chatcamp.uikit.messages.messagetypes.VideoMessageFactory;
import com.chatcamp.uikit.utils.Directory;
import com.chatcamp.uikit.utils.DownloadFileListener;
import com.chatcamp.uikit.utils.FileUtils;

/**
 * Created by shubhamdhabhai on 20/06/18.
 */

public class DownloadVideoIntentService extends IntentService {

    private static final String TAG = "DownloadVideoIntent";

    public static final String VIDEO_URL = "video_url";
    public static final String IS_ME = "is_me";
    public static final String MESSAGE_ID = "message_id";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor
     */
    public DownloadVideoIntentService() {
        super(DownloadVideoIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        if (intent == null) {
            Log.e(TAG, "intent passed to this service is null, This should never happen");
            return;
        }

        if(!intent.hasExtra(VIDEO_URL)) {
            Log.e(TAG, "Video url is not passed to this service.");
            return;
        }
        if(!intent.hasExtra(IS_ME)) {
            Log.e(TAG, "is me is not passed to this service.");
            return;
        }
        if(!intent.hasExtra(MESSAGE_ID)) {
            Log.e(TAG, "message id is not passed to this service.");
            return;
        }
        String videoUrl = intent.getStringExtra(VIDEO_URL);
        boolean isMe = intent.getBooleanExtra(IS_ME, false);
        final String messageId = intent.getStringExtra(MESSAGE_ID);

        try {
            Uri path = FileProvider.getUriForFile(this,
                    getPackageName() + ".chatcamp.fileprovider",
                    FileUtils.downloadFile(this, videoUrl,
                            Directory.VIDEOS, isMe, new DownloadFileListener() {
                                @Override
                                public void downloadProgress(final int progress) {
                                    Log.d("download", "downloading" + progress);
                                    Intent progressIntent = new Intent(VideoMessageFactory.DownloadResultReceiver.ACTION);
                                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.PROGRESS, progress);
                                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.STATUS, VideoMessageFactory.PROGRESS);
                                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.MESSAGE_ID, messageId);
                                    sendBroadcast(progressIntent);
                                }

                                @Override
                                public void downloadComplete() {
                                    Intent progressIntent = new Intent(VideoMessageFactory.DownloadResultReceiver.ACTION);
                                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.PROGRESS, 100);
                                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.STATUS, VideoMessageFactory.COMPLETE);
                                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.MESSAGE_ID, messageId);
                                    sendBroadcast(progressIntent);
                                }
                            }));
            Intent progressIntent = new Intent(VideoMessageFactory.DownloadResultReceiver.ACTION);
            progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.PROGRESS, 100);
            progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.STATUS, VideoMessageFactory.COMPLETE);
            progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.MESSAGE_ID, messageId);
            progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.VIDEO_URI, path.toString());
            sendBroadcast(progressIntent);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

    }
}
