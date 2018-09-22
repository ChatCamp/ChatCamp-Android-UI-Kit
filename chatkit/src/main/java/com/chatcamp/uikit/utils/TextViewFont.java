package com.chatcamp.uikit.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by shubhamdhabhai on 21/09/18.
 */

public class TextViewFont extends AppCompatTextView {

    public TextViewFont(Context context) {
        super(context);
    }

    public TextViewFont(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewFont(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param asset asset string
     * @return true if set else false
     */
    public boolean setCustomFont(String asset) {
        Typeface typeface;
        try {
            typeface = Typeface.createFromAsset(getContext().getAssets(), asset);
        } catch (Exception e) {
            return false;
        }

        setTypeface(typeface);
        return true;
    }
}
