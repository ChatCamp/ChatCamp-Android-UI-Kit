package com.chatcamp.uikit.database.typeconvertor;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class MapStringLongTypeConvertor {
    @TypeConverter
    public static Map<String, Long> fromString(String value) {
        Type listType = new TypeToken<Map<String, Long>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromMap(Map<String, Long> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
