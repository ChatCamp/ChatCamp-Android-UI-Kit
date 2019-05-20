package com.chatcamp.uikit.database.typeconvertor;

import android.arch.persistence.room.TypeConverter;

import com.chatcamp.uikit.database.DbGroupWrapper;

public class ParticipantStateTypeConvertor {
    @TypeConverter
    public static DbGroupWrapper.ParticipantState toStatus(int status) {
        if (status == DbGroupWrapper.ParticipantState.INVITED.getValue()) {
            return DbGroupWrapper.ParticipantState.INVITED;
        } else {
            return DbGroupWrapper.ParticipantState.ACCEPTED;
        }
    }

    @TypeConverter
    public static int toInteger(DbGroupWrapper.ParticipantState status) {
        return status.getValue();
    }
}
