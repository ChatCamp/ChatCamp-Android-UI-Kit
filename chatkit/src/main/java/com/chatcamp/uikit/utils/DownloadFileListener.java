package com.chatcamp.uikit.utils;

/**
 * Created by shubhamdhabhai on 09/05/18.
 */

public interface DownloadFileListener {
    void downloadStart();
    void downloadProgress(int progress);
    void downloadComplete();
}
