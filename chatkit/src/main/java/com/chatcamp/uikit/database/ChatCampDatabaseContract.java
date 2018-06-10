package com.chatcamp.uikit.database;

import android.provider.BaseColumns;

/**
 * Created by shubhamdhabhai on 15/03/18.
 */

public class ChatCampDatabaseContract {

    public static final String DATABASE_NAME = "chatcamp_database";
    public static final int DATABASE_VERSION = 2;

    public static class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "table_message";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_CHANNEL_ID = "channel_id";
        public static final String COLUMN_NAME_CHANNEL_TYPE = "channel_type";
        public static final String COLUMN_NAME_TIME_STAMP = "time";

    }

    public static class GroupEntry implements BaseColumns {
        public static final String TABLE_NAME = "table_group_channel";
        public static final String COLUMN_NAME_GROUP = "groupChannel";
        public static final String COLUMN_NAME_GROUP_ID = "groupChannel_id";
        public static final String COLUMN_NAME_PARTICIPANT_STATE = "participant_state";
    }
}
