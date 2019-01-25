package com.chatcamp.uikit.database;

import android.arch.persistence.room.ColumnInfo;

import io.chatcamp.sdk.Message;

public class DbAttachmentWrapper {

    public DbAttachmentWrapper() {
    }

    public DbAttachmentWrapper(Message.Attachment attachment) {
        if(attachment != null) {
            name = attachment.getName();
            type = attachment.getType();
            url = attachment.getUrl();
        }
    }

    @ColumnInfo
    private String name;
    @ColumnInfo(name = "attachment_type")
    private String type;
    @ColumnInfo
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
