package com.chatcamp.uikit.database.typeconvertor;

import android.arch.persistence.room.TypeConverter;
import android.provider.Telephony;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.chatcamp.sdk.Participant;

public class ParticipantListTypeConvertor {

    @TypeConverter
    public static List<Participant> fromString(String value) {
        Type listType = new TypeToken<List<Participant>>() {}.getType();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(List<Participant> list) {
        Gson gson =  new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(list);
        return json;
    }
}
