package com.chatcamp.uikit.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by shubhamdhabhai on 14/06/18.
 */

public class UnClickableSeekBar  extends android.support.v7.widget.AppCompatSeekBar {

    public UnClickableSeekBar(Context context) {
        super(context);
    }

    public UnClickableSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnClickableSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}