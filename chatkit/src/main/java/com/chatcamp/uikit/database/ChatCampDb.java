package com.chatcamp.uikit.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.chatcamp.uikit.database.typeconvertor.ChannelTypeConvertor;
import com.chatcamp.uikit.database.typeconvertor.MapStringLongTypeConvertor;
import com.chatcamp.uikit.database.typeconvertor.MapTypeConvertor;
import com.chatcamp.uikit.database.typeconvertor.ParticipantListTypeConvertor;
import com.chatcamp.uikit.database.typeconvertor.ParticipantStateTypeConvertor;
import com.chatcamp.uikit.database.typeconvertor.StringTypeConvertor;

@Database(entities = {DbMessageWrapper.class, DbGroupWrapper.class, DbOpenChannelWrapper.class}, version = 1)
@TypeConverters({StringTypeConvertor.class, MapTypeConvertor.class,
        ParticipantListTypeConvertor.class, MapStringLongTypeConvertor.class,
        ParticipantStateTypeConvertor.class, ChannelTypeConvertor.class})
public abstract class ChatCampDb extends RoomDatabase {
    public abstract DbMessageWrapperDao dbMessageWrapperDao();
    public abstract DbGroupWrapperDao dbGroupWrapperDao();
    public abstract DbOpenChannelWrapperDao dbOpenChannelWrapperDao();

    public static volatile ChatCampDb INSTANCE;

    static ChatCampDb getInstance(final Context context) {
        if(INSTANCE == null) {
            synchronized (ChatCampDb.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ChatCampDb.class, "chatcamp_database.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
