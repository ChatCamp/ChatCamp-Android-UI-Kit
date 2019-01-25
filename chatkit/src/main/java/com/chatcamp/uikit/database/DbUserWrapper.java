package com.chatcamp.uikit.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Map;

import io.chatcamp.sdk.User;

@Entity(tableName = "user")
public class DbUserWrapper {

    public DbUserWrapper() {
    }

    public DbUserWrapper(User user) {
        id = user.getId();
        displayName = user.getDisplayName();
        avatarUrl = user.getAvatarUrl();
        avatarUploadUrl = user.getAvatarUploadUrl();
        isOnline = user.isOnline();
        lastSeen = user.getLastSeen();
        metadata = user.getMetadata();
    }

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    private String id;
    @ColumnInfo(name = "display_name")
    private String displayName;
    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;
    @ColumnInfo(name = "avatar_upload_url")
    private String avatarUploadUrl;
    @ColumnInfo(name = "is_online")
    private Boolean isOnline = false;
    @ColumnInfo(name = "last_seen")
    private long lastSeen;
    //TODO add type convertor
    @ColumnInfo(name = "user_metadata")
    private Map metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUploadUrl() {
        return avatarUploadUrl;
    }

    public void setAvatarUploadUrl(String avatarUploadUrl) {
        this.avatarUploadUrl = avatarUploadUrl;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DbUserWrapper) {
            DbUserWrapper user = (DbUserWrapper) obj;
            if(user.getId().equals(getId())) {
                return true;
            }

        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
