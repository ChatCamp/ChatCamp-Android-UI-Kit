package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.Style;

/**
 * Created by shubhamdhabhai on 17/05/18.
 */

public class HeaderStyle extends Style {

    private int backgroundColor;
    private int titleTextSize;
    private int titleTextColor;
    private int titleTextStyle;
    private int imageHeight;
    private int imageWidth;
    private int titleMarginLeft;
    private String customFont;
    private boolean headerViewAlwaysShowChannelName;
    private int participantCountBackgroundColor;
    private int participantCountTextColor;
    private boolean showParticipantCount;
    private boolean showBlockOption;

    public static HeaderStyle parseStyle(Context context, AttributeSet attrs) {
        HeaderStyle headerStyle = new HeaderStyle(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderView);
        headerStyle.backgroundColor = typedArray.getColor(R.styleable.HeaderView_backgroundColor, headerStyle.getColor(R.color.chatCampColorPrimary));
        headerStyle.titleTextSize = typedArray.getDimensionPixelSize(R.styleable.HeaderView_headerTitleTextSize,
                headerStyle.getDimension(R.dimen.header_view_text_size));
        headerStyle.titleTextColor = typedArray.getColor(R.styleable.HeaderView_headerTitleTextColor, headerStyle.getColor(R.color.chatCampTextWhite));
        headerStyle.titleTextStyle = typedArray.getInt(R.styleable.HeaderView_headerTitleTextStyle, Typeface.NORMAL);
        headerStyle.imageHeight = typedArray.getDimensionPixelSize(R.styleable.HeaderView_imageHeight,
                headerStyle.getDimension(R.dimen.header_view_image_height));
        headerStyle.imageWidth = typedArray.getDimensionPixelSize(R.styleable.HeaderView_imageWidth,
                headerStyle.getDimension(R.dimen.header_view_image_width));
        headerStyle.titleMarginLeft = typedArray.getDimensionPixelSize(R.styleable.HeaderView_headerTitleMarginLeft,
                headerStyle.getDimension(R.dimen.header_view_title_left_margin));
        headerStyle.customFont = typedArray.getString(R.styleable.HeaderView_headerViewCustomFont);
        headerStyle.headerViewAlwaysShowChannelName = typedArray.getBoolean(R.styleable.HeaderView_headerViewAlwaysShowChannelName, false);
        headerStyle.participantCountBackgroundColor = typedArray.getColor(R.styleable.HeaderView_participantCountBackgroundColor,
                headerStyle.getColor(R.color.chatCampParticipantCountColor));
        headerStyle.participantCountTextColor = typedArray.getColor(R.styleable.HeaderView_participantCountTextColor,
                headerStyle.getColor(R.color.chatCampTextWhite));
        headerStyle.showParticipantCount = typedArray.getBoolean(R.styleable.HeaderView_showParticipantCount, false);
        headerStyle.showBlockOption = typedArray.getBoolean(R.styleable.HeaderView_showBlockOption, true);
        typedArray.recycle();
        return headerStyle;
    }


    private HeaderStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getTitleTextSize() {
        return titleTextSize;
    }

    public int getTitleTextColor() {
        return titleTextColor;
    }

    public void setTitleTextColor(int titleTextColor) {
        this.titleTextColor = titleTextColor;
    }

    public int getTitleTextStyle() {
        return titleTextStyle;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getTitleMarginLeft() {
        return titleMarginLeft;
    }

    public String getCustomFont() {
        return customFont;
    }

    public boolean isHeaderViewAlwaysShowChannelName() {
        return headerViewAlwaysShowChannelName;
    }

    public int getParticipantCountBackgroundColor() {
        return participantCountBackgroundColor;
    }

    public int getParticipantCountTextColor() {
        return participantCountTextColor;
    }

    public boolean isShowParticipantCount() {
        return showParticipantCount;
    }

    public boolean isShowBlockOption() {
        return showBlockOption;
    }
}
