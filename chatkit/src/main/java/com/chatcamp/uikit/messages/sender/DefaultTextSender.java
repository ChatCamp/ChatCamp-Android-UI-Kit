package com.chatcamp.uikit.messages.sender;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.chatcamp.uikit.database.MessageDataSource;

import io.chatcamp.sdk.BaseChannel;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public class DefaultTextSender extends TextSender {

    private final Context context;
    private MessageDataSource dataSource;

    public DefaultTextSender(@NonNull BaseChannel channel, Context context) {
        super(channel);
        this.context = context;
        dataSource = MessageDataSource.getInstance(context, channel);
    }

    @Override
    public void sendMessage(@NonNull String message) {
        if(!TextUtils.isEmpty(message.trim())) {
            dataSource.sendMessage(message.trim());
//            ChatCampDatabase.getInstance(context).sendMessage((GroupChannel) channel, message.trim());
        }
    }
}
