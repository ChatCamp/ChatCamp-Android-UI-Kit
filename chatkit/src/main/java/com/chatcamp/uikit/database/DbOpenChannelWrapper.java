package com.chatcamp.uikit.database;

import android.arch.persistence.room.Entity;

import io.chatcamp.sdk.OpenChannel;

@Entity(tableName = "open_channel")
public class DbOpenChannelWrapper extends DbBaseChannelWrapper {

    public DbOpenChannelWrapper() {
        channelType = ChannelType.OPEN_CHANNEL;
    }

    public DbOpenChannelWrapper(OpenChannel openChannel) {
        super(openChannel);
        channelType = ChannelType.OPEN_CHANNEL;
    }
}
