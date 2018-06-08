package com.chatcamp.uikit.utils;

import android.content.Context;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by shubhamdhabhai on 08/06/18.
 */

public class DefaultTimeFormat implements TimeFormat {

    private static final long SECOND_MILLIS = 1000;
    private static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    private static final long YEAR_MILLIS = 365 * DAY_MILLIS;

    @Override
    public void setTime(TextView textView, long time) {
        String timeString = "";
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if (time > now || time <= 0) {
            timeString = "";
            textView.setText(timeString);
            return;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            timeString = "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            timeString = "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            timeString = diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            timeString = "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            timeString = diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            timeString = "yesterday";
        } else if(diff < MONTH_MILLIS){
            timeString = diff / DAY_MILLIS + " days ago";
        } else if(diff < YEAR_MILLIS) {
            if(diff/MONTH_MILLIS < 2) {
                timeString = diff / MONTH_MILLIS + " month ago";
            } else {
                timeString = diff / MONTH_MILLIS + " months ago";
            }
        } else {
            if(diff/YEAR_MILLIS < 2) {
                timeString = diff / YEAR_MILLIS + " year ago";
            } else {
                timeString = diff / YEAR_MILLIS + " years ago";
            }
        }
        textView.setText(timeString);
    }
}
