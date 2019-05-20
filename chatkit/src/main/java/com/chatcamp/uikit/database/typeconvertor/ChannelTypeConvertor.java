package com.chatcamp.uikit.database.typeconvertor;

import android.arch.persistence.room.TypeConverter;

import com.chatcamp.uikit.database.DbBaseChannelWrapper;

public class ChannelTypeConvertor {
    @TypeConverter
    public static DbBaseChannelWrapper.ChannelType toStatus(int status) {
        if (status == DbBaseChannelWrapper.ChannelType.GROUP_CHANNEL.getValue()) {
            return DbBaseChannelWrapper.ChannelType.GROUP_CHANNEL;
        } else {
            return DbBaseChannelWrapper.ChannelType.OPEN_CHANNEL;
        }
    }

    @TypeConverter
    public static int toInteger(DbBaseChannelWrapper.ChannelType status) {
        return status.getValue();
    }
}
