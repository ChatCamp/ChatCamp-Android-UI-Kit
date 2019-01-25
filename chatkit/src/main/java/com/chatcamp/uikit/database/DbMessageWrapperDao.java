package com.chatcamp.uikit.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface DbMessageWrapperDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DbMessageWrapper wrapper);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DbMessageWrapper> wrapper);

//    ORDER BY inserted_at DESC
    @Query("SELECT * from message_table WHERE group_id = :channelId ORDER BY inserted_at DESC")
    List<DbMessageWrapper> getAll(String channelId);

    @Query("SELECT * from message_table WHERE id = :messageId")
    DbMessageWrapper getMessage(String messageId);

    @Query("DELETE from message_table WHERE group_id = :channelId " +
            "AND messageStatus == 'sent'")
    int deleteAll(String channelId);

    @Query("DELETE from message_table WHERE id = :messageId ")
    int delete(String messageId);
}
