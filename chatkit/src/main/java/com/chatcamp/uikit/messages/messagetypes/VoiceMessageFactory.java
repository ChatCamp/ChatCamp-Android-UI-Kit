package com.chatcamp.uikit.messages.messagetypes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.utils.DownloadFileListener;
import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 13/06/18.
 */

public class VoiceMessageFactory extends MessageFactory<VoiceMessageFactory.VoiceMessageHolder> {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA = 110;

    private final WeakReference<Object> objectWeakReference;
    private View view;
    private ProgressBar progressBar;
    private ImageView downloadIcon;
    private Handler handler;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private String messageId;

    public VoiceMessageFactory(Activity activity) {
        objectWeakReference = new WeakReference<Object>(activity);
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
    }

    public VoiceMessageFactory(Fragment fragment) {
        objectWeakReference = new WeakReference<Object>(fragment);
        handler = new Handler();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public boolean isBindable(Message message) {
        if (message.getType().equals("attachment")) {
            if (message.getAttachment().getType().contains("audio")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public VoiceMessageHolder createMessageHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        View view = layoutInflater.inflate(R.layout.layout_message_voice_recording, cellView, true);
        Drawable backgroundDrawable = isMe ? messageStyle.getOutcomingBubbleDrawable() :
                messageStyle.getIncomingBubbleDrawable();
        int downloadIcon = isMe ? R.drawable.ic_download_white : R.drawable.ic_download;
        int seekBarColor = isMe ? messageStyle.getOutcomingTextColor() : messageStyle.getIncomingTextColor();
        int playIcon = isMe ? R.drawable.ic_play_white : R.drawable.ic_play;
        //TODO get seekbar color from style
        SeekBar seekBar = view.findViewById(R.id.sb_audio);
        seekBar.getProgressDrawable().setColorFilter(seekBarColor, PorterDuff.Mode.SRC_ATOP);
        ImageView downloadImage = view.findViewById(R.id.iv_download);
        downloadImage.setImageResource(downloadIcon);
        ImageView playIv = view.findViewById(R.id.iv_play);
        playIv.setImageResource(playIcon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(backgroundDrawable);
        } else {
            view.setBackgroundDrawable(backgroundDrawable);
        }
        return new VoiceMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(final VoiceMessageHolder messageHolder, final Message message) {
        final Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            messageHolder.downloadIcon.setVisibility(View.INVISIBLE);
            return;
        }
        messageHolder.playImage.setTag(R.id.seekbar, messageHolder.seekBar);
        messageHolder.playImage.setTag(R.id.message, message);
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
        if (messageId != null && message.getId().equals(messageId)) {
            final int duration = mediaPlayer.getDuration();
            messageHolder.seekBar.setMax(duration);
            handler.removeCallbacksAndMessages(null);
            this.seekBar = messageHolder.seekBar;
            try {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                    int mCurrentPosition = mediaPlayer.getCurrentPosition();
                                    messageHolder.seekBar.setProgress(mCurrentPosition);
                                    handler.postDelayed(this, 1000);
                                }
                            } catch (Exception e) {

                            }
                        }
                    };
                    handler.post(runnable);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            messageHolder.seekBar.setProgress(0);
                            handler.removeCallbacks(runnable);
                        }
                    });

                }
            } catch (Exception e) {

            }
        } else {
            messageHolder.seekBar.setProgress(0);
        }

        messageHolder.playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (seekBar != null) {
                        seekBar.setProgress(0);
                    }
                    mediaPlayer.reset();
                    handler.removeCallbacksAndMessages(null);
                    final Message message = (Message) v.getTag(R.id.message);
                    messageId = message.getId();
                    final SeekBar seekbar = (SeekBar) v.getTag(R.id.seekbar);
                    VoiceMessageFactory.this.seekBar = seekbar;
                    final String imageUrl = message.getAttachment().getUrl();
                    mediaPlayer.setDataSource(imageUrl);
                    mediaPlayer.prepare();

                    int duration = mediaPlayer.getDuration();
                    seekbar.setMax(duration);
                    mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mp, int percent) {
                            final int duration = mediaPlayer.getDuration();
                        }
                    });
                    mediaPlayer.start();

                    duration = mediaPlayer.getDuration();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null) {
                                int mCurrentPosition = mediaPlayer.getCurrentPosition();
                                seekbar.setProgress(mCurrentPosition);
                                handler.postDelayed(this, 1000);
                            }
                        }
                    };
                    handler.post(runnable);

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            seekbar.setProgress(0);
                            handler.removeCallbacks(runnable);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }


//                if (v.getTag() != null && v.getTag() instanceof Message) {
//                    if (!FileUtils.fileExists(context, message.getAttachment().getUrl(), Environment.DIRECTORY_MOVIES)) {
//                        messageHolder.progressBar.setVisibility(View.VISIBLE);
//                        messageHolder.progressBar.setProgress(0);
//                    } else {
//                        messageHolder.progressBar.setVisibility(View.INVISIBLE);
//                    }
//                    onPlayClicked(v, messageHolder.progressBar, messageHolder.downloadIcon, messageHolder.seekBar);
//                }
            }
        });

    }

    private void onPlayClicked(View v, ProgressBar progressBar, ImageView downloadIcon, SeekBar seekBar) {
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
            this.seekBar = seekBar;
            Utils.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA, objectWeakReference.get());

        } else {
            downloadVoiceRecording(v, progressBar, downloadIcon, seekBar);
        }
    }

    private void downloadVoiceRecording(View v, final ProgressBar progressBar, ImageView downloadIcon, final SeekBar seekBar) {
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
                            final Context context = Utils.getContext(objectWeakReference.get());
                            path = FileProvider.getUriForFile(context,
                                    context.getPackageName() + ".chatcamp.fileprovider",
                                    FileUtils.downloadFile(context, imageUrl,
                                            Environment.DIRECTORY_MUSIC, new DownloadFileListener() {
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

                            MediaPlayer mediaPlayer = new MediaPlayer();
                            try {
                                mediaPlayer.setDataSource(path.toString());
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                                final int duration = mediaPlayer.getDuration();
                                final int amoungToupdate = duration / 100;
                                Timer mTimer = new Timer();
                                mTimer.schedule(new TimerTask() {

                                    @Override
                                    public void run() {
                                        handler.post(new Runnable() {

                                            @Override
                                            public void run() {
                                                if (!(amoungToupdate * seekBar.getProgress() >= duration)) {
                                                    int p = seekBar.getProgress();
                                                    p += 1;
                                                    seekBar.setProgress(p);
                                                }
                                            }
                                        });
                                    }
                                }, amoungToupdate);
                            } catch (IOException e) {
                                Log.e("sdsf", "prepare() failed");
                            }
//                            final Uri finalPath = path;
//                            Intent intent = new Intent(context, ShowVideoActivity.class);
//                            intent.putExtra(ShowVideoActivity.VIDEO_URL, finalPath.toString());
//                            Utils.startActivity(intent, objectWeakReference.get());
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
                downloadVoiceRecording(view, progressBar, downloadIcon, seekBar);
            }
        }
    }

    public static class VoiceMessageHolder extends MessageFactory.MessageHolder {
        ImageView playImage;
        SeekBar seekBar;
        ProgressBar progressBar;
        ImageView downloadIcon;
        View view;

        public VoiceMessageHolder(View view) {
            this.view = view;
            playImage = view.findViewById(R.id.iv_play);
            progressBar = view.findViewById(R.id.progress_bar);
            downloadIcon = view.findViewById(R.id.iv_download);
            seekBar = view.findViewById(R.id.sb_audio);
        }
    }

    public void freeResources() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


}
