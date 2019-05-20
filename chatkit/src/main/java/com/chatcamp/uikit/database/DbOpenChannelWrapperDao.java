package com.chatcamp.uikit.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DbOpenChannelWrapperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DbOpenChannelWrapper wrapper);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DbOpenChannelWrapper> wrapper);

    //    ORDER BY inserted_at DESC
    @Query("SELECT * from open_channel")
    List<DbOpenChannelWrapper> getAll();

    @Query("DELETE from open_channel")
    int deleteAll();
}
