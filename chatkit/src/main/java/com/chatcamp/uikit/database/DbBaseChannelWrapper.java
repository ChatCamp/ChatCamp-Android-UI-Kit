package com.chatcamp.uikit.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Map;

import io.chatcamp.sdk.BaseChannel;

@Entity(tableName = "base_channel")
public class DbBaseChannelWrapper {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "channel_id")
    private String id;
    @ColumnInfo(name = "channel_name")
    private String name;
    private String avatarUrl;
    @ColumnInfo(name = "channel_metadata")
    private Map metadata;

    protected ChannelType channelType;

    public DbBaseChannelWrapper() {
    }

    public DbBaseChannelWrapper(BaseChannel baseChannel) {
        this.id = baseChannel.getId();
        this.name = baseChannel.getName();
        this.avatarUrl = baseChannel.getAvatarUrl();
        this.metadata = baseChannel.getMetadata();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    public enum ChannelType {
        GROUP_CHANNEL(0),
        OPEN_CHANNEL(1);

        int value;

        ChannelType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
