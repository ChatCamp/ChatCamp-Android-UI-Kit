package com.chatcamp.uikit.messages.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.Message;

import static com.chatcamp.uikit.messages.database.ChatCampDatabaseContract.DATABASE_NAME;
import static com.chatcamp.uikit.messages.database.ChatCampDatabaseContract.DATABASE_VERSION;


/**
 * Created by shubhamdhabhai on 15/03/18.
 */

public class ChatCampDatabaseHelper extends SQLiteOpenHelper {

    public ChatCampDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_MESSAGE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + ChatCampDatabaseContract.MessageEntry.TABLE_NAME + "("
                + ChatCampDatabaseContract.MessageEntry._ID + " INTEGER PRIMARY KEY,"
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID + " TEXT,"
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_MESSAGE + " TEXT,"
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE + " TEXT,"
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_TIME_STAMP + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_MESSAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChatCampDatabaseContract.MessageEntry.TABLE_NAME);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    public void addMessages(List<Message> messages, String channelId, BaseChannel.ChannelType channelType) {
        SQLiteDatabase sqliteDatabase = this.getWritableDatabase();
        sqliteDatabase.delete(ChatCampDatabaseContract.MessageEntry.TABLE_NAME,
                ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID + " =? AND "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE + "=?",
                new String[]{channelId, channelType.name()});

        for (int i = 0; i < messages.size(); ++i) {
            ContentValues values = new ContentValues();
            values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_MESSAGE,
                    messages.get(i).serialize()); // serialized message
            values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID,
                    channelId); // Group ID
            values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE,
                    channelType.name()); // Group Type
            values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_TIME_STAMP,
                    messages.get(i).getInsertedAt()); // Time of message inserted
            sqliteDatabase.insert(ChatCampDatabaseContract.MessageEntry.TABLE_NAME,
                    null, values);
        }
    }

    public void addMessage(Message message, String channelId, BaseChannel.ChannelType channelType) {
        SQLiteDatabase sqliteDatabase = this.getWritableDatabase();
        sqliteDatabase.execSQL("DELETE FROM " + ChatCampDatabaseContract.MessageEntry.TABLE_NAME
                + " WHERE " + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID
                + " = '" + channelId + "' AND "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE + " = '"
                + channelType.name() +  "' AND "
                + ChatCampDatabaseContract.MessageEntry._ID + " NOT IN (SELECT "
                + ChatCampDatabaseContract.MessageEntry._ID + " FROM "
                + ChatCampDatabaseContract.MessageEntry.TABLE_NAME + " WHERE "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID
                + " = '" + channelId + "' AND "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE
                + " = '" + channelType.name() +  "' AND "
                + ChatCampDatabaseContract.MessageEntry._ID + " ORDER BY "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_TIME_STAMP +
                "  DESC LIMIT 19)");

        ContentValues values = new ContentValues();
        values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_MESSAGE, message.serialize()); // serialized message
        values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID, channelId); // Group ID
        values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE, channelType.name()); // Group ID
        values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_TIME_STAMP, message.getInsertedAt()); // Time of message inserted
        sqliteDatabase.insert(ChatCampDatabaseContract.MessageEntry.TABLE_NAME, null, values);
    }

    public List<Message> getMessages(String channelId, BaseChannel.ChannelType channelType) {
        SQLiteDatabase db = this.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_MESSAGE,
                ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID
        };
        String selection = ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID
                + " = ? AND " + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE + " = ?";
        String[] selectionArgs = {channelId, channelType.name()};
        String sortOrder =
                ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_TIME_STAMP + " DESC";

        Cursor cursor = db.query(
                ChatCampDatabaseContract.MessageEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        List<Message> messages = new ArrayList<>();
        while (cursor.moveToNext()) {
            String message = cursor.getString(
                    cursor.getColumnIndexOrThrow(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_MESSAGE));
            messages.add(Message.createfromSerializedData(message));
        }
        cursor.close();
        return messages;
    }
}
