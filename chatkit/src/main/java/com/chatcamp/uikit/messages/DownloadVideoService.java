package com.chatcamp.uikit.messages;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import com.chatcamp.uikit.messages.messagetypes.VideoMessageFactory;
import com.chatcamp.uikit.utils.Directory;
import com.chatcamp.uikit.utils.DownloadFileListener;
import com.chatcamp.uikit.utils.FileUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by shubhamdhabhai on 20/06/18.
 */

public class DownloadVideoService extends Service {

    private static final String TAG = "DownloadVideoIntent";

    public static final String VIDEO_URL = "video_url";
    public static final String IS_ME = "is_me";
    public static final String MESSAGE_ID = "message_id";

    private ConcurrentHashMap<String, Future> messageIdFutureMap;
    private ExecutorService threadExecutor;
    private CancelDownloadBroadcastReceiver cancelDownloadBroadcastReceiver = new CancelDownloadBroadcastReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        messageIdFutureMap = new ConcurrentHashMap<>();
        threadExecutor = Executors.newCachedThreadPool();
        IntentFilter intentFilter = new IntentFilter(CancelDownloadBroadcastReceiver.ACTION);
        registerReceiver(cancelDownloadBroadcastReceiver, intentFilter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.e(TAG, "intent passed to this service is null, This should never happen");
            return super.onStartCommand(intent, flags, startId);
        }

        if (!intent.hasExtra(VIDEO_URL)) {
            Log.e(TAG, "Video url is not passed to this service.");
            return super.onStartCommand(intent, flags, startId);
        }
        if (!intent.hasExtra(IS_ME)) {
            Log.e(TAG, "is me is not passed to this service.");
            return super.onStartCommand(intent, flags, startId);
        }
        if (!intent.hasExtra(MESSAGE_ID)) {
            Log.e(TAG, "message id is not passed to this service.");
            return super.onStartCommand(intent, flags, startId);
        }

        String videoUrl = intent.getStringExtra(VIDEO_URL);
        boolean isMe = intent.getBooleanExtra(IS_ME, false);
        final String messageId = intent.getStringExtra(MESSAGE_ID);
        Future future = threadExecutor.submit(new DownloadVideoRunnable(this, videoUrl, isMe, messageId));
        messageIdFutureMap.put(messageId, future);
        return super.onStartCommand(intent, flags, startId);
    }

    public class DownloadVideoRunnable implements Runnable {

        private String videoUrl;
        private boolean isMe;
        private String messageId;
        private Context context;
        private int progress;

        public DownloadVideoRunnable(Context context, String videoUrl, boolean isMe, String messageId) {
            this.videoUrl = videoUrl;
            this.isMe = isMe;
            this.messageId = messageId;
            this.context = context;
        }

        @Override
        public void run() {
//            String videoUrl = intent.getStringExtra(VIDEO_URL);
//            boolean isMe = intent.getBooleanExtra(IS_ME, false);
//            final String messageId = intent.getStringExtra(MESSAGE_ID);

            try {
                Uri path = FileProvider.getUriForFile(context,
                        getPackageName() + ".chatcamp.fileprovider",
                        FileUtils.downloadFile(context, videoUrl,
                                Directory.VIDEOS, isMe, new DownloadFileListener() {
                                    @Override
                                    public void downloadStart() {
                                        Intent progressIntent = new Intent(VideoMessageFactory.DownloadResultReceiver.ACTION);
                                        progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.STATUS, VideoMessageFactory.START);
                                        progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.MESSAGE_ID, messageId);
                                        sendBroadcast(progressIntent);
                                    }

                                    @Override
                                    public void downloadProgress(final int progress) {
                                        Log.d("download", "downloading" + progress);
                                        if(DownloadVideoRunnable.this.progress != progress) {
                                            DownloadVideoRunnable.this.progress = progress;
                                            Intent progressIntent = new Intent(VideoMessageFactory.DownloadResultReceiver.ACTION);
                                            progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.PROGRESS, progress);
                                            progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.STATUS, VideoMessageFactory.PROGRESS);
                                            progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.MESSAGE_ID, messageId);
                                            sendBroadcast(progressIntent);
                                        }
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
                if(path != null) {
                    Intent progressIntent = new Intent(VideoMessageFactory.DownloadResultReceiver.ACTION);
                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.PROGRESS, 100);
                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.STATUS, VideoMessageFactory.COMPLETE);
                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.MESSAGE_ID, messageId);
                    progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.VIDEO_URI, path.toString());
                    sendBroadcast(progressIntent);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("service", "service destroyed");
        try {
            //We need to add try catch here as the app crashes when the receiver is not registered and
            // we try to unregister it. and there is no api to test it, bloody android
            unregisterReceiver(cancelDownloadBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CancelDownloadBroadcastReceiver extends BroadcastReceiver {

        public static final String MESSAGE_ID = "message_id";
        public static final String ACTION = "io.chatcamp.chatcamp.cancel_download";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null && intent.getAction().equals(ACTION)) {
                String messageId = intent.getStringExtra(MESSAGE_ID);
                if (!TextUtils.isEmpty(messageId)) {
                    Future future = messageIdFutureMap.get(messageId);
                    if (future != null) {
                        future.cancel(true);
                        Intent progressIntent = new Intent(VideoMessageFactory.DownloadResultReceiver.ACTION);
                        progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.STATUS, VideoMessageFactory.STOP);
                        progressIntent.putExtra(VideoMessageFactory.DownloadResultReceiver.MESSAGE_ID, messageId);
                        sendBroadcast(progressIntent);
                    }
                }
            }
        }
    }
}
