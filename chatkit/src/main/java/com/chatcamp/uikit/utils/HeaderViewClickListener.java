package com.chatcamp.uikit.utils;

/**
 * Created by shubhamdhabhai on 17/05/18.
 */

public interface HeaderViewClickListener {
    boolean onHeaderViewClicked(String channelId, boolean isOneToOneConversation, String otherParticipantId);
}
