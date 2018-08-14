package com.chatcamp.uikit.database;

import android.provider.BaseColumns;

/**
 * Created by shubhamdhabhai on 15/03/18.
 */

public class ChatCampDatabaseContract {

    public static final String DATABASE_NAME = "chatcamp_database";
    public static final int DATABASE_VERSION = 4;

    public static class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "table_message";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_CHANNEL_ID = "channel_id";
        public static final String COLUMN_NAME_CHANNEL_TYPE = "channel_type";
        public static final String COLUMN_NAME_TIME_STAMP = "time";

    }

    public static class GroupEntry implements BaseColumns {
        public static final String TABLE_NAME_ALL = "table_group_channel_all";
        public static final String TABLE_NAME = "table_group_channel";
        public static final String TABLE_NAME_INVITED = "table_group_channel_invited";
        public static final String TABLE_NAME_ACCEPTED = "table_group_channel_accepted";
        public static final String COLUMN_NAME_GROUP = "groupChannel";
        public static final String COLUMN_NAME_CUSTOM_FILTERS = "custom_filters";
    }
}
