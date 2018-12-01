package com.chatcamp.uikit.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.chatcamp.uikit.R;

public class LoadingView extends RelativeLayout {


    public LoadingView(Context context) {
        super(context);
        init(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_loading, this);
    }
}
