package com.chatcamp.uikit.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DbGroupWrapperDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DbGroupWrapper wrapper);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DbGroupWrapper> wrapper);

    //    ORDER BY inserted_at DESC
    @Query("SELECT * from group_table ORDER BY inserted_at DESC")
    List<DbGroupWrapper> getAll();

    @Query("SELECT * from group_table WHERE participantState = :participantState ORDER BY inserted_at DESC")
    List<DbGroupWrapper> getAll(DbGroupWrapper.ParticipantState participantState);

    @Query("SELECT * from group_table WHERE messageStatus = 'unsent' ORDER BY inserted_at DESC")
    List<DbGroupWrapper> getDirtyGroup();

    @Query("DELETE from group_table")
    int deleteAll();
}
