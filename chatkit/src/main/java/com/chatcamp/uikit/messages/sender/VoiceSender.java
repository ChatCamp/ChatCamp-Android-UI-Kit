package com.chatcamp.uikit.messages.sender;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.chatcamp.uikit.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCampException;

/**
 * Created by shubhamdhabhai on 11/06/18.
 */

public class VoiceSender extends AttachmentSender {

    private static final int PERMISSIONS_REQUEST_RECORD_VOICE = 113;

    private WeakReference<Object> objectWeakReference;
    private MediaRecorder recorder;
    private String currentAudioFile;

    public VoiceSender(@NonNull Activity activity, @NonNull BaseChannel baseChannel) {
        super(baseChannel, null, -1);
        objectWeakReference = new WeakReference<Object>(activity);
    }

    public VoiceSender(@NonNull Fragment fragment, @NonNull BaseChannel baseChannel) {
        super(baseChannel, null, -1);
        objectWeakReference = new WeakReference<Object>(fragment);
    }

    public void startRecording() {
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            ChatCampException exception = new ChatCampException("Context is null", "VOICE RECORDING UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_VOICE, objectWeakReference.get());
        } else {
            startAudioRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_VOICE) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (objectWeakReference.get() != null) {
                    startAudioRecording();
                }
            }
        }
    }

    public void cancelRecording() {

    }

    private void stopRecording() {
        if(recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        if(!TextUtils.isEmpty(currentAudioFile)) {
            File file = new File(currentAudioFile);
//        MediaPlayer mediaPlayer = new MediaPlayer();
//        try {
//            mediaPlayer.setDataSource(currentAudioFile);
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//        } catch (IOException e) {
//            Log.e("sdsf", "prepare() failed");
//        }
            sendAttachment(file, file.getName(), "audio/mp3");
        }
    }

    private void startAudioRecording() {
        try {
            createAudioFile();
        } catch (IOException e) {
            Log.e("VOICE SENDER", "error creating audio file");
            ChatCampException exception = new ChatCampException("error creating audio file " + e.getMessage(), "VOICE RECORDING UPLOAD ERROR");
            sendAttachmentError(exception);
            e.printStackTrace();
            return;
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(currentAudioFile);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("VOICE SENDER", "prepare() failed");
            ChatCampException exception = new ChatCampException("prepare() failed " + e.getMessage(), "VOICE RECORDING UPLOAD ERROR");
            sendAttachmentError(exception);
        }

        recorder.start();
    }

    @Override
    public void clickSend() {
        stopRecording();
    }

    private File createAudioFile() throws IOException {
        // Create an image file name
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            ChatCampException exception = new ChatCampException("Context is null", "VOICE RECORDING UPLOAD ERROR");
            sendAttachmentError(exception);
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFile = "MP3_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File audio = File.createTempFile(
                audioFile,  /* prefix */
                ".mp3",         /* suffix */
                storageDir      /* directory */
        );

        currentAudioFile = audio.getAbsolutePath();

        return audio;
    }

    public void freeResources() {
        if(recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }
}
