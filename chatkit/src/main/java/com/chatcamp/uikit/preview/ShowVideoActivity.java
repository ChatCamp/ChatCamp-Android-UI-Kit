package com.chatcamp.uikit.preview;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.MediaController;
import android.widget.VideoView;

import com.chatcamp.uikit.R;

public class ShowVideoActivity extends AppCompatActivity {

    public static final String VIDEO_URL = "video_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        VideoView videoView = findViewById(R.id.vv_video);
        String videoUrl = null;
        if (getIntent().hasExtra(VIDEO_URL)) {
            videoUrl = getIntent().getStringExtra(VIDEO_URL);
            if (!TextUtils.isEmpty(videoUrl)) {
                videoView.setVideoURI(Uri.parse(videoUrl));
                MediaController mediaController = new MediaController(ShowVideoActivity.this);
                videoView.setMediaController(mediaController);
                videoView.start();
            }
        }
    }
}
