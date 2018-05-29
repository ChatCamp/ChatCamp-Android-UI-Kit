package com.chatcamp.uikit.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import java.io.ObjectStreamException;

/**
 * Created by shubhamdhabhai on 29/05/18.
 */

public class Utils {

    public static Context getContext(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getContext();
        } else {
            return null;
        }
    }

    public static void requestPermission(@NonNull String[] permissions, int requestCode, Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof Activity) {
            Activity activity = (Activity) object;
            ActivityCompat.requestPermissions(activity, permissions,
                    requestCode);
        } else if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            fragment.requestPermissions(permissions,
                    requestCode);
        }
    }

    public static void startActivity(Intent intent, Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof Activity) {
            Activity activity = (Activity) object;
            activity.startActivity(intent);
        } else if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            fragment.startActivity(intent);
        }
    }

    public static void startActivityForResult(Intent intent, int requestCode, Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof Activity) {
            Activity activity = (Activity) object;
            activity.startActivityForResult(intent, requestCode);
        } else if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            fragment.startActivityForResult(intent, requestCode);
        }
    }
}
