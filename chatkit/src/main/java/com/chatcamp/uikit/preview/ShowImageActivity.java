package com.chatcamp.uikit.preview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.chatcamp.uikit.R;
//import com.davemorrissey.labs.subscaleview.ImageSource;
//import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class ShowImageActivity extends AppCompatActivity {

    public static final String IMAGE_URL = "image_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
//        SubsamplingScaleImageView imageView = findViewById(R.id.iv_image);
//        String imageUrl = null;
//        if (getIntent().hasExtra(IMAGE_URL)) {
//            imageUrl = getIntent().getStringExtra(IMAGE_URL);
//        }
//        if (!TextUtils.isEmpty(imageUrl)) {
//            imageView.setImage(ImageSource.uri(imageUrl));
//        }
    }
}
