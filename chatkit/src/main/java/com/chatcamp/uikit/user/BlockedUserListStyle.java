package com.chatcamp.uikit.user;

import android.content.Context;
import android.util.AttributeSet;

import com.chatcamp.uikit.commons.Style;

/**
 * Created by shubhamdhabhai on 30/08/18.
 */

public class BlockedUserListStyle extends Style {


    protected BlockedUserListStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static BlockedUserListStyle parseStyle(Context context, AttributeSet attrs) {
        BlockedUserListStyle blockedUserListStyle = new BlockedUserListStyle(context, attrs);
        return blockedUserListStyle;
    }
}
