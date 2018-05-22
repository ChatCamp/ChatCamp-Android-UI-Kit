package com.chatcamp.uikit.messages.sender;

import android.support.annotation.NonNull;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public class DefaultTextSender extends TextSender {

    public DefaultTextSender(@NonNull BaseChannel channel) {
        super(channel);
    }

    @Override
    public void sendMessage(@NonNull String message) {
        channel.sendMessage(message, new BaseChannel.SendMessageListener() {
            @Override
            public void onSent(Message message, ChatCampException e) {
                // can do something here
            }
        });
    }
}
