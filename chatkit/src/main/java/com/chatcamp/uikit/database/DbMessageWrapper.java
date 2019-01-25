package com.chatcamp.uikit.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.chatcamp.sdk.Message;

@Entity(tableName = "message_table")
public class DbMessageWrapper {

    @PrimaryKey()
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    //    private OpenChannel openChannel;

    @Embedded
    private DbUserWrapper user;

    @Embedded
    private DbAttachmentWrapper attachment;

//    @ColumnInfo(name = "message_id")
//    private String messageId;

    @ColumnInfo(name = "type")
    private String type;

    //TODO add type convertor for list
    @ColumnInfo(name = "custom_filter")
    private List<String> customFilter;

    private String data;
    @ColumnInfo(name = "text")
    private String text;

    //TODO add type convertor for map
    private Map<String, String> metadata;

    @ColumnInfo(name = "inserted_at")
    private long insertedAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;


    @ColumnInfo(name = "group_id")
    private String groupId;

    private String messageStatus;


    public DbMessageWrapper() {
        id = UUID.randomUUID().toString();
    }

    public DbMessageWrapper(Message message) {
        id = message.getId();
        type = message.getType();
        text = message.getText();
        insertedAt = message.getInsertedAt();
        updatedAt = message.getUpdatededAt();
        user = new DbUserWrapper(message.getUser());
        attachment = new DbAttachmentWrapper(message.getAttachment());
    }

//    public String getMessageId() {
//        return messageId;
//    }
//
//    public void setMessageId(String messageId) {
//        this.messageId = messageId;
//    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getInsertedAt() {
        return insertedAt;
    }

    public void setInsertedAt(long insertedAt) {
        this.insertedAt = insertedAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public DbUserWrapper getUser() {
        return user;
    }

    public void setUser(DbUserWrapper user) {
        this.user = user;
    }

    public DbAttachmentWrapper getAttachment() {
        return attachment;
    }

    public void setAttachment(DbAttachmentWrapper attachment) {
        this.attachment = attachment;
    }

    public List<String> getCustomFilter() {
        return customFilter;
    }

    public void setCustomFilter(List<String> customFilter) {
        this.customFilter = customFilter;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    public String getMessageStatus() {
        return messageStatus;
    }

//    public MESSAGE_STATUS getMessageStatus() {
//        switch (messageStatus) {
//            case "sent":
//                return MESSAGE_STATUS.SENT;
//            case "unsent":
//                return MESSAGE_STATUS.UNSENT;
//            case "update":
//                return MESSAGE_STATUS.UPDATE;
//            case "delete":
//                return MESSAGE_STATUS.DELETE;
//            default:
//                return MESSAGE_STATUS.SENT;
//        }
//    }

//    public void setMessageStatus(MESSAGE_STATUS messageStatus) {
//        this.messageStatus = messageStatus.getValue();
//    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public enum MESSAGE_STATUS {
        SENT("sent"),
        UNSENT("unsent"),
        DELETE("delete"),
        UPDATE("update");

        private String value;

        MESSAGE_STATUS(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

