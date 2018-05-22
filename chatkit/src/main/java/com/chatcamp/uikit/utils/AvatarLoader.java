package com.chatcamp.uikit.utils;

import android.support.annotation.DrawableRes;
import android.widget.ImageView;

/**
 * Created by shubhamdhabhai on 17/05/18.
 */

public interface AvatarLoader {
    void loadImage(ImageView imageView, @DrawableRes int drawable);

    void loadImage(ImageView imageView, String url);
}
