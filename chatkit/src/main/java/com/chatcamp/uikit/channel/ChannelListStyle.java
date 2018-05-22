package com.chatcamp.uikit.channel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.Style;

/**
 * Created by shubhamdhabhai on 18/05/18.
 */

public class ChannelListStyle extends Style {

    private int avatarWidth;
    private int avatarHeight;

    private int nameTextSize;
    private int nameTextStyle;
    private int nameTextColor;

    private int lastMessageTextSize;
    private int lastMessageTextStyle;
    private int lastMessageTextColor;

    private int lastSeenTextSize;
    private int lastSeenTextStyle;
    private int lastSeenTextColor;

    private int unreadMessageCountTextColor;
    private int unreadMessageCountTextSize;
    private int unreadMessageCountTextStyle;
    private int unreadMessageCountBackgroundColor;


    protected ChannelListStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static ChannelListStyle parseStyle(Context context, AttributeSet attrs){
        ChannelListStyle style = new ChannelListStyle(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChannelList);
        style.avatarHeight = typedArray.getDimensionPixelSize(R.styleable.ChannelList_avatarHeight,
                style.getDimension(R.dimen.channel_list_avatar_height));
        style.avatarWidth = typedArray.getDimensionPixelSize(R.styleable.ChannelList_avatarWidth,
                style.getDimension(R.dimen.channel_list_avatar_width));

        style.nameTextSize = typedArray.getDimensionPixelSize(R.styleable.ChannelList_nameTextSize,
                style.getDimension(R.dimen.channel_list_name_text_size));
        style.nameTextColor = typedArray.getColor(R.styleable.ChannelList_nameTextColor, style.getColor(R.color.black));
        style.nameTextStyle = typedArray.getInteger(R.styleable.ChannelList_nameTextStyle, Typeface.BOLD);

        style.lastMessageTextSize = typedArray.getDimensionPixelSize(R.styleable.ChannelList_lastMessageTextSize,
                style.getDimension(R.dimen.channel_list_last_message_text_size));
        style.lastMessageTextColor = typedArray.getColor(R.styleable.ChannelList_lastMessageTextColor,
                style.getColor(R.color.black));
        style.lastMessageTextStyle = typedArray.getInteger(R.styleable.ChannelList_lastMessageTextStyle,
                Typeface.NORMAL);

        style.lastSeenTextSize = typedArray.getDimensionPixelSize(R.styleable.ChannelList_lastSeenTextSize,
                style.getDimension(R.dimen.channel_list_last_seen_text_size));
        style.lastSeenTextColor = typedArray.getColor(R.styleable.ChannelList_lastSeenTextColor,
                style.getColor(R.color.black));
        style.lastSeenTextStyle = typedArray.getInteger(R.styleable.ChannelList_lastSeenTextStyle, Typeface.NORMAL);

        style.unreadMessageCountTextSize = typedArray.getDimensionPixelSize(R.styleable.ChannelList_unreadMessageCountTextSize,
                style.getDimension(R.dimen.channel_list_unread_message_count_text_size));
        style.unreadMessageCountTextColor = typedArray.getColor(R.styleable.ChannelList_unreadMessageCountTextColor,
                style.getColor(R.color.white));
        style.unreadMessageCountTextStyle = typedArray.getInteger(R.styleable.ChannelList_unreadMessageCountTextStyle, Typeface.NORMAL);
        style.unreadMessageCountBackgroundColor = typedArray.getColor(R.styleable.ChannelList_unreadMessageCountBackgroundColor,
                style.getColor(R.color.colorPrimary));
        typedArray.recycle();
        return style;
    }

    public int getAvatarWidth() {
        return avatarWidth;
    }

    public int getAvatarHeight() {
        return avatarHeight;
    }

    public int getNameTextSize() {
        return nameTextSize;
    }

    public int getNameTextStyle() {
        return nameTextStyle;
    }

    public int getNameTextColor() {
        return nameTextColor;
    }

    public int getLastMessageTextSize() {
        return lastMessageTextSize;
    }

    public int getLastMessageTextStyle() {
        return lastMessageTextStyle;
    }

    public int getLastMessageTextColor() {
        return lastMessageTextColor;
    }

    public int getLastSeenTextSize() {
        return lastSeenTextSize;
    }

    public int getLastSeenTextStyle() {
        return lastSeenTextStyle;
    }

    public int getLastSeenTextColor() {
        return lastSeenTextColor;
    }

    public int getUnreadMessageCountTextColor() {
        return unreadMessageCountTextColor;
    }

    public int getUnreadMessageCountTextSize() {
        return unreadMessageCountTextSize;
    }

    public int getUnreadMessageCountTextStyle() {
        return unreadMessageCountTextStyle;
    }

    public int getUnreadMessageCountBackgroundColor() {
        return unreadMessageCountBackgroundColor;
    }
}
