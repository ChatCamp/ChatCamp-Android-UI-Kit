package com.chatcamp.uikit.user;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.Style;

/**
 * Created by shubhamdhabhai on 30/08/18.
 */

public class BlockedUserListStyle extends Style {

    private int userAvatarWidth;
    private int userAvatarHeight;

    private boolean showUserAvatar;

    private int onlineIndicatorWidth;
    private int onlineIndicatirHeight;

    private int usernameTextSize;
    private int usernameTextStyle;
    private int usernameTextColor;
    private boolean showUsername;

    private int unBlockDrawable;
    private int unBlockDrawableColor;
    private boolean showUnBlock;

    private int unBlockTextSize;
    private int unBlockTextStyle;
    private int unBlockTextColor;

    protected BlockedUserListStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static BlockedUserListStyle parseStyle(Context context, AttributeSet attrs) {
        BlockedUserListStyle blockedUserListStyle = new BlockedUserListStyle(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BlockedUserList);
        blockedUserListStyle.userAvatarWidth = typedArray.getDimensionPixelSize(R.styleable.BlockedUserList_userAvatarWidth, blockedUserListStyle.getDimension(R.dimen.blocked_user_list_avatar_width));
        blockedUserListStyle.userAvatarHeight = typedArray.getDimensionPixelSize(R.styleable.BlockedUserList_userAvatarHeight, blockedUserListStyle.getDimension(R.dimen.blocked_user_list_avatar_height));
        blockedUserListStyle.showUserAvatar = typedArray.getBoolean(R.styleable.BlockedUserList_showUserAvatar, true);
        blockedUserListStyle.onlineIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.BlockedUserList_onlineIndicatorWidth, blockedUserListStyle.getDimension(R.dimen.blocked_user_list_online_indicator_width));
        blockedUserListStyle.onlineIndicatirHeight = typedArray.getDimensionPixelSize(R.styleable.BlockedUserList_onlineIndicatorHeight, blockedUserListStyle.getDimension(R.dimen.blocked_user_list_online_indicator_width));
        blockedUserListStyle.usernameTextSize = typedArray.getDimensionPixelSize(R.styleable.BlockedUserList_usernameTextSize, blockedUserListStyle.getDimension(R.dimen.blocked_user_list_username_textsize));
        blockedUserListStyle.usernameTextStyle = typedArray.getInt(R.styleable.BlockedUserList_usernameTextStyle, Typeface.NORMAL);
        blockedUserListStyle.usernameTextColor = typedArray.getColor(R.styleable.BlockedUserList_usernameTextColor, blockedUserListStyle.getColor(R.color.black));
        blockedUserListStyle.showUsername = typedArray.getBoolean(R.styleable.BlockedUserList_showUsername, true);
        blockedUserListStyle.unBlockDrawable = typedArray.getResourceId(R.styleable.BlockedUserList_unBlockDrawable, -1);
        blockedUserListStyle.unBlockDrawableColor = typedArray.getColor(R.styleable.BlockedUserList_unBlockDrawableColor, blockedUserListStyle.getColor(R.color.colorPrimary));
        blockedUserListStyle.showUnBlock = typedArray.getBoolean(R.styleable.BlockedUserList_showUnBlock, true);
        blockedUserListStyle.unBlockTextSize = typedArray.getDimensionPixelSize(R.styleable.BlockedUserList_unBlockTextSize,  blockedUserListStyle.getDimension(R.dimen.blocked_user_list_username_textsize));
        blockedUserListStyle.unBlockTextStyle = typedArray.getInt(R.styleable.BlockedUserList_unBlockTextStyle, Typeface.NORMAL);
        blockedUserListStyle.unBlockTextColor = typedArray.getColor(R.styleable.BlockedUserList_unBlockTextColor, blockedUserListStyle.getColor(R.color.white));
        return blockedUserListStyle;
    }

    public Drawable getunBlockDrawable() {
        if (unBlockDrawable == -1) {
            Drawable drawable = getDrawable(R.drawable.bg_unblock).mutate();
            drawable.setColorFilter(unBlockDrawableColor, PorterDuff.Mode.SRC_ATOP);
            return drawable;
        } else {
            return getDrawable(unBlockDrawable);
        }
    }

    public int getUserAvatarWidth() {
        return userAvatarWidth;
    }

    public int getUserAvatarHeight() {
        return userAvatarHeight;
    }

    public boolean isShowUserAvatar() {
        return showUserAvatar;
    }

    public int getOnlineIndicatorWidth() {
        return onlineIndicatorWidth;
    }

    public int getOnlineIndicatirHeight() {
        return onlineIndicatirHeight;
    }

    public int getUsernameTextSize() {
        return usernameTextSize;
    }

    public int getUsernameTextStyle() {
        return usernameTextStyle;
    }

    public int getUsernameTextColor() {
        return usernameTextColor;
    }

    public boolean isShowUsername() {
        return showUsername;
    }

    public boolean isShowUnBlock() {
        return showUnBlock;
    }

    public int getUnBlockTextSize() {
        return unBlockTextSize;
    }

    public int getUnBlockTextStyle() {
        return unBlockTextStyle;
    }

    public int getUnBlockTextColor() {
        return unBlockTextColor;
    }
}
