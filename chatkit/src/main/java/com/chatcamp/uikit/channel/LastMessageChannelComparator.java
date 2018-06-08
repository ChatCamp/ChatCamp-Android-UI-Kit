package com.chatcamp.uikit.channel;

import io.chatcamp.sdk.GroupChannel;

/**
 * Created by shubhamdhabhai on 07/06/18.
 */

public class LastMessageChannelComparator implements ChannelAdapter.ChannelComparator {
    @Override
    public int compare(GroupChannel o1, GroupChannel o2) {
        if(o1.getLastMessage() != null && o2.getLastMessage() != null){
            if (o1.getLastMessage().getInsertedAt() > o2.getLastMessage().getInsertedAt()) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }
}
