package com.chatcamp.uikit.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.text.TextUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.GroupChannelListQuery;
import io.chatcamp.sdk.Message;

import static com.chatcamp.uikit.database.ChatCampDatabaseContract.DATABASE_NAME;
import static com.chatcamp.uikit.database.ChatCampDatabaseContract.DATABASE_VERSION;


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

        String CREATE_GROUP_CHANNEL_ALL_TABLE = "CREATE TABLE IF NOT EXISTS "
                + ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ALL + "("
                + ChatCampDatabaseContract.GroupEntry._ID + " TEXT PRIMARY KEY,"
                + ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP + " TEXT,"
                + ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS + " TEXT" + ")";

        sqLiteDatabase.execSQL(CREATE_GROUP_CHANNEL_ALL_TABLE);

        String CREATE_GROUP_CHANNEL_INVITE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + ChatCampDatabaseContract.GroupEntry.TABLE_NAME_INVITED + "("
                + ChatCampDatabaseContract.GroupEntry._ID + " TEXT PRIMARY KEY,"
                + ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP + " TEXT,"
                + ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS + " TEXT" + ")";

        sqLiteDatabase.execSQL(CREATE_GROUP_CHANNEL_INVITE_TABLE);

        String CREATE_GROUP_CHANNEL_ACCEPTED_TABLE = "CREATE TABLE IF NOT EXISTS "
                + ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ACCEPTED + "("
                + ChatCampDatabaseContract.GroupEntry._ID + " TEXT PRIMARY KEY,"
                + ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP + " TEXT,"
                + ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS + " TEXT" + ")";

        sqLiteDatabase.execSQL(CREATE_GROUP_CHANNEL_ACCEPTED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChatCampDatabaseContract.MessageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChatCampDatabaseContract.GroupEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ALL);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ACCEPTED);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ChatCampDatabaseContract.GroupEntry.TABLE_NAME_INVITED);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    public void addMessages(List<Message> messages, String channelId, BaseChannel.ChannelType channelType) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        new AddMessagesAsyncTask(messages, channelId, channelType, sqLiteDatabase).execute();
    }

    private static void addMessagesLocal(List<Message> messages, String channelId, BaseChannel.ChannelType channelType, SQLiteDatabase sqliteDatabase) {

        // delete all the messages of that particular group id and channel type and add the new messages.
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
        new AddMessageAsyncTask(message, channelId, channelType, sqliteDatabase).execute();
    }

    private static void addMessageLocal(Message message, String channelId, BaseChannel.ChannelType channelType, SQLiteDatabase sqliteDatabase) {
        // delete all the messages except top 19
        sqliteDatabase.execSQL("DELETE FROM " + ChatCampDatabaseContract.MessageEntry.TABLE_NAME
                + " WHERE " + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID
                + " = '" + channelId + "' AND "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE + " = '"
                + channelType.name() + "' AND "
                + ChatCampDatabaseContract.MessageEntry._ID + " NOT IN (SELECT "
                + ChatCampDatabaseContract.MessageEntry._ID + " FROM "
                + ChatCampDatabaseContract.MessageEntry.TABLE_NAME + " WHERE "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID
                + " = '" + channelId + "' AND "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE
                + " = '" + channelType.name() + "' AND "
                + ChatCampDatabaseContract.MessageEntry._ID + " ORDER BY "
                + ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_TIME_STAMP +
                "  DESC LIMIT 19)");

        // add the message
        ContentValues values = new ContentValues();
        values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_MESSAGE, message.serialize()); // serialized message
        values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_ID, channelId); // Group ID
        values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_CHANNEL_TYPE, channelType.name()); // Group ID
        values.put(ChatCampDatabaseContract.MessageEntry.COLUMN_NAME_TIME_STAMP, message.getInsertedAt()); // Time of message inserted
        sqliteDatabase.insert(ChatCampDatabaseContract.MessageEntry.TABLE_NAME, null, values);
    }

    public void getMessages(String channelId, BaseChannel.ChannelType channelType, GetMessagesListener getMessagesListener) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        new GetMessagesAsyncTask(channelId, channelType, sqLiteDatabase, getMessagesListener).execute();
    }

    private static List<Message> getMessagesLocal(String channelId, BaseChannel.ChannelType channelType, SQLiteDatabase sqLiteDatabase) {

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

        Cursor cursor = sqLiteDatabase.query(
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

    public void addGroupChannels(List<GroupChannel> groupChannels,
                                 GroupChannelListQuery.ParticipantState participantState,
                                 List<String> queryCustomFilter) {
        SQLiteDatabase sqliteDatabase = this.getWritableDatabase();
        new AddGroupChannelsAsyncTask(groupChannels, participantState, queryCustomFilter, sqliteDatabase).execute();
    }


    // group channels
    private static void addGroupChannelsLocal(List<GroupChannel> groupChannels,
                                              GroupChannelListQuery.ParticipantState participantState,
                                              List<String> queryCustomFilter, SQLiteDatabase sqliteDatabase) {
        synchronized (groupChannels) {
            StringBuilder inQuery = new StringBuilder();
            StringBuilder subQuery = new StringBuilder();
            String tableName = getTableName(participantState);

            // if there is no filter used then we delete all the groups which are not part of the groups
            // that we got from api of that state.
            if (queryCustomFilter == null) {
                inQuery.append("(");
                boolean first = true;
                for (GroupChannel groupChannel : groupChannels) {
                    if (first) {
                        first = false;
                        inQuery.append("'").append(groupChannel.getId()).append("'");
                    } else {
                        inQuery.append(", '").append(groupChannel.getId()).append("'");
                    }
                }
                inQuery.append(")");
                if (!TextUtils.isEmpty(inQuery.toString())) {
                    sqliteDatabase.execSQL("DELETE FROM " + tableName
                            + " WHERE "
                            + ChatCampDatabaseContract.GroupEntry._ID + " NOT IN "
                            + inQuery);
                }
            } else {
                // if there is any custom filter then we get all the groups of that state and that
                // filter from database and delete the one that we did not get from api
                List<GroupChannel> savedGroupChannel = getGroupChannelsLocal(participantState, queryCustomFilter, sqliteDatabase);
                if (savedGroupChannel != null && savedGroupChannel.size() > 0) {
                    boolean first = true;
                    for (GroupChannel groupChannel : savedGroupChannel) {
                        boolean continueLoop = false;
                        for (GroupChannel queryChannel : groupChannels) {
                            if (queryChannel.getId().equals(groupChannel.getId())) {
                                continueLoop = true;
                                break;
                            }
                        }
                        if (continueLoop) {
                            continue;
                        }
                        if (first) {
                            first = false;
                            subQuery.append("'").append(groupChannel.getId()).append("'");
                        } else {
                            subQuery.append(", '").append(groupChannel.getId()).append("'");
                        }
                    }
                    if (!TextUtils.isEmpty(subQuery.toString())) {
                        inQuery.append("(");
                        inQuery.append(subQuery);
                        inQuery.append(")");
                    }
                }
                if (!TextUtils.isEmpty(inQuery.toString())) {
                    sqliteDatabase.execSQL("DELETE FROM " + tableName
                            + " WHERE "
                            + ChatCampDatabaseContract.GroupEntry._ID + " IN"
                            + inQuery);
                }
            }
            for (int i = 0; i < groupChannels.size(); ++i) {
                Cursor cursor = sqliteDatabase.rawQuery("SELECT *    FROM " + tableName
                                + " WHERE " + ChatCampDatabaseContract.GroupEntry._ID + " = " + "'" + groupChannels.get(i).getId() + "'",
//                            + "' AND "
//                            + ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_PARTICIPANT_STATE + " = " + "'" + participantState.name() + "'",
                        null);

                if (cursor.getCount() == 0) {
                    // no group match with the id and participant state.
                    ContentValues values = new ContentValues();
                    values.put(ChatCampDatabaseContract.GroupEntry._ID, groupChannels.get(i).getId());
                    values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP,
                            groupChannels.get(i).serialize()); // serialized group channel
                    //  values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_PARTICIPANT_STATE, participantState.name());
                    List<String> customFilters = groupChannels.get(i).getCustomFilter();
                    if (customFilters != null) {
                        values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS, customFilters.toString());
                    }
                    sqliteDatabase.insert(tableName,
                            null, values);
                } else {
                    // a group already exists with this id and participant state
                    if (cursor.moveToFirst()) {
                        GroupChannel groupChannel = GroupChannel.createfromSerializedData(cursor.
                                getString(cursor.getColumnIndexOrThrow(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP)));
                        if (groupChannel != null && !groupChannel.equals(groupChannels.get(i))) {
                            ContentValues values = new ContentValues();
                            values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP,
                                    groupChannels.get(i).serialize());
//                        values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_PARTICIPANT_STATE, participantState.name());
                            List<String> customFilters = groupChannels.get(i).getCustomFilter();
                            if (customFilters != null) {
                                values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS, customFilters.toString());
                            }
                            sqliteDatabase.update(tableName, values,
                                    ChatCampDatabaseContract.GroupEntry._ID + "=?", new String[]{groupChannel.getId()});
                        }
                    }
                }
                cursor.close();
            }
        }
    }

    public void addGroupChannel(GroupChannel groupChannel) {
        SQLiteDatabase sqliteDatabase = this.getWritableDatabase();
        new AddGroupChannelAsyncTask(groupChannel, sqliteDatabase).execute();
    }

    private static void addGroupChannelLocal(GroupChannel groupChannel, SQLiteDatabase sqliteDatabase) {
        // Check if the group already exist
        //TODO check all three tables and update group in them
        Cursor cursor = sqliteDatabase.rawQuery("SELECT *    FROM " + ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ALL
                + " WHERE " + ChatCampDatabaseContract.GroupEntry._ID + " = " + "'" + groupChannel.getId() + "'", null);

        List<String> customFilters = groupChannel.getCustomFilter();
        if (cursor.getCount() >= 1) {
            // the group already exists so just update it
            ContentValues values = new ContentValues();
            values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP,
                    groupChannel.serialize());
            if (customFilters != null) {
                values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS, customFilters.toString());
            }
            sqliteDatabase.update(ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ALL, values,
                    ChatCampDatabaseContract.GroupEntry._ID + "=?", new String[]{groupChannel.getId()});
        } else {
            // the group doesnot exists so create a new group
            ContentValues values = new ContentValues();
            values.put(ChatCampDatabaseContract.GroupEntry._ID, groupChannel.getId());
            values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP,
                    groupChannel.serialize()); // serialized group channel
            if (customFilters != null) {
                values.put(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS, customFilters.toString());
            }
            sqliteDatabase.insert(ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ALL,
                    null, values);
        }
        cursor.close();
    }

    public void getGroupChannels(GroupChannelListQuery.ParticipantState participantState,
                                 List<String> customFilters, GetGroupChannelsListener getGroupChannelsListener) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        new GetGroupChannelsAsyncTask(participantState, customFilters, sqLiteDatabase, getGroupChannelsListener).execute();

    }

    private static List<GroupChannel> getGroupChannelsLocal(GroupChannelListQuery.ParticipantState participantState,
                                                            List<String> customFilters, SQLiteDatabase sqLiteDatabase) {
        ArrayList<GroupChannel> list = new ArrayList<>();
        String tableName = getTableName(participantState);


        String[] projection = {
                BaseColumns._ID,
                ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP,
//                ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_PARTICIPANT_STATE,
                ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS
        };
//        String selection = null;//ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_PARTICIPANT_STATE
//                + " = ?";
//        String[] selectionArgs = null;//{participantState.name()};

        try {
            Cursor cursor = sqLiteDatabase.query(
                    tableName,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null               // The sort order
            );

            try {
                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        String customFilterString = cursor.getString(cursor.getColumnIndexOrThrow(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_CUSTOM_FILTERS));
                        if (!TextUtils.isEmpty(customFilterString) && customFilters != null && customFilters.size() > 0) {
                            // the custom filter passed in the function has some value and custom filter saved in the database has some value
                            List<String> groupFilters = new ArrayList<>();
                            JSONArray sellerArray = new JSONArray(customFilterString);
                            for (int i = 0; i < sellerArray.length(); ++i) {
                                groupFilters.add(String.valueOf(sellerArray.get(i)));
                            }
                            Set<String> incomingFiltersSet = new HashSet<>(customFilters);
                            Set<String> groupFiltersSet = new HashSet<>(groupFilters);
                            if (groupFiltersSet.containsAll(incomingFiltersSet)) {
                                GroupChannel groupChannel = GroupChannel.createfromSerializedData(cursor.
                                        getString(cursor.getColumnIndexOrThrow(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP)));
                                list.add(groupChannel);
                            }
                        } else {
                            GroupChannel groupChannel = GroupChannel.createfromSerializedData(cursor.
                                    getString(cursor.getColumnIndexOrThrow(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP)));
                            list.add(groupChannel);
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void getGroupChannel(String groupChannelId, GetGroupChannelListener getGroupChannelListener) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        new GetGroupChannelAsyncTask(groupChannelId, sqLiteDatabase, getGroupChannelListener).execute();
    }

    private static GroupChannel getGroupChannelLocal(String groupChannelId, SQLiteDatabase sqLiteDatabase) {

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT *    FROM " + ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ALL
                + " WHERE " + ChatCampDatabaseContract.GroupEntry._ID + " = " + "'" + groupChannelId + "'", null);
        GroupChannel groupChannel = null;
        try {
            if (cursor.moveToFirst()) {
                groupChannel = GroupChannel.createfromSerializedData(cursor.
                        getString(cursor.getColumnIndexOrThrow(ChatCampDatabaseContract.GroupEntry.COLUMN_NAME_GROUP)));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupChannel;
    }

    private static String getTableName(GroupChannelListQuery.ParticipantState participantState) {
        if (participantState == GroupChannelListQuery.ParticipantState.INVITED) {
            return ChatCampDatabaseContract.GroupEntry.TABLE_NAME_INVITED;
        } else if (participantState == GroupChannelListQuery.ParticipantState.ACCEPTED) {
            return ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ACCEPTED;
        } else {
            return ChatCampDatabaseContract.GroupEntry.TABLE_NAME_ALL;
        }
    }

    public interface GetMessagesListener {
        void onGetMessages(List<Message> messages);
    }

    public interface GetGroupChannelsListener {
        void onGetGroupChannels(List<GroupChannel> groupChannels);
    }

    public interface GetGroupChannelListener {
        void onGetGroupChannel(GroupChannel groupChannel);
    }

    public static class AddMessagesAsyncTask extends AsyncTask<Void, Void, Void> {

        private final List<Message> messages;
        private final String channelId;
        private final BaseChannel.ChannelType channelType;
        private final SQLiteDatabase sqLiteDatabase;

        public AddMessagesAsyncTask(List<Message> messages, String channelId, BaseChannel.ChannelType channelType, SQLiteDatabase sqLiteDatabase) {
            this.messages = messages;
            this.channelId = channelId;
            this.channelType = channelType;
            this.sqLiteDatabase = sqLiteDatabase;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            addMessagesLocal(messages, channelId, channelType, sqLiteDatabase);
            return null;
        }
    }

    public static class AddMessageAsyncTask extends AsyncTask<Void, Void, Void> {
        private final Message message;
        private final String channelId;
        private final BaseChannel.ChannelType channelType;
        private final SQLiteDatabase sqLiteDatabase;

        public AddMessageAsyncTask(Message message, String channelId, BaseChannel.ChannelType channelType, SQLiteDatabase sqLiteDatabase) {
            this.message = message;
            this.channelId = channelId;
            this.channelType = channelType;
            this.sqLiteDatabase = sqLiteDatabase;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            addMessageLocal(message, channelId, channelType, sqLiteDatabase);
            return null;
        }
    }

    public static class GetMessagesAsyncTask extends AsyncTask<Void, Void, List<Message>> {
        private String channelId;
        private BaseChannel.ChannelType channelType;
        private SQLiteDatabase sqLiteDatabase;
        private GetMessagesListener getMessagesListener;

        public GetMessagesAsyncTask(String channelId, BaseChannel.ChannelType channelType, SQLiteDatabase sqLiteDatabase, GetMessagesListener getMessagesListener) {
            this.channelId = channelId;
            this.channelType = channelType;
            this.sqLiteDatabase = sqLiteDatabase;
            this.getMessagesListener = getMessagesListener;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            return getMessagesLocal(channelId, channelType, sqLiteDatabase);
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            if (getMessagesListener != null) {
                getMessagesListener.onGetMessages(messages);
            }
        }
    }

    public static class AddGroupChannelsAsyncTask extends AsyncTask<Void, Void, Void> {

        private List<GroupChannel> groupChannels;
        private GroupChannelListQuery.ParticipantState participantState;
        private List<String> queryCustomFilter;
        private SQLiteDatabase sqliteDatabase;

        public AddGroupChannelsAsyncTask(List<GroupChannel> groupChannels,
                                         GroupChannelListQuery.ParticipantState participantState,
                                         List<String> queryCustomFilter, SQLiteDatabase sqliteDatabase) {
            this.groupChannels = groupChannels;
            this.participantState = participantState;
            this.queryCustomFilter = queryCustomFilter;
            this.sqliteDatabase = sqliteDatabase;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            addGroupChannelsLocal(groupChannels, participantState, queryCustomFilter, sqliteDatabase);
            return null;
        }
    }

    public static class AddGroupChannelAsyncTask extends AsyncTask<Void, Void, Void> {

        private GroupChannel groupChannel;
        private SQLiteDatabase sqliteDatabase;

        public AddGroupChannelAsyncTask(GroupChannel groupChannel, SQLiteDatabase sqliteDatabase) {
            this.groupChannel = groupChannel;
            this.sqliteDatabase = sqliteDatabase;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            addGroupChannelLocal(groupChannel, sqliteDatabase);
            return null;
        }
    }

    public static class GetGroupChannelsAsyncTask extends AsyncTask<Void, Void, List<GroupChannel>> {
        private GroupChannelListQuery.ParticipantState participantState;
        private List<String> customFilters;
        private SQLiteDatabase sqLiteDatabase;
        private GetGroupChannelsListener getGroupChannelsListener;

        public GetGroupChannelsAsyncTask(GroupChannelListQuery.ParticipantState participantState,
                                         List<String> customFilters, SQLiteDatabase sqLiteDatabase,
                                         GetGroupChannelsListener getGroupChannelsListener) {
            this.participantState = participantState;
            this.customFilters = customFilters;
            this.sqLiteDatabase = sqLiteDatabase;
            this.getGroupChannelsListener = getGroupChannelsListener;
        }

        @Override
        protected List<GroupChannel> doInBackground(Void... voids) {
            return getGroupChannelsLocal(participantState, customFilters, sqLiteDatabase);
        }

        @Override
        protected void onPostExecute(List<GroupChannel> groupChannels) {
            if (getGroupChannelsListener != null) {
                getGroupChannelsListener.onGetGroupChannels(groupChannels);
            }
        }
    }

    public static class GetGroupChannelAsyncTask extends AsyncTask<Void, Void, GroupChannel> {

        private String groupChannelId;
        private SQLiteDatabase sqLiteDatabase;
        private GetGroupChannelListener getGroupChannelListener;

        public GetGroupChannelAsyncTask(String groupChannelId, SQLiteDatabase sqLiteDatabase,
                                        GetGroupChannelListener getGroupChannelListener) {
            this.groupChannelId = groupChannelId;
            this.sqLiteDatabase = sqLiteDatabase;
            this.getGroupChannelListener = getGroupChannelListener;
        }

        @Override
        protected GroupChannel doInBackground(Void... voids) {

            return getGroupChannelLocal(groupChannelId, sqLiteDatabase);
        }

        @Override
        protected void onPostExecute(GroupChannel groupChannel) {
            if (getGroupChannelListener != null) {
                getGroupChannelListener.onGetGroupChannel(groupChannel);
            }
        }
    }
}
