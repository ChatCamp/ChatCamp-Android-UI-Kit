package com.chatcamp.uikit.messages.sender;

import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Message;
import io.chatcamp.sdk.MessageParams;
import io.chatcamp.sdk.ThumbnailDimension;

/**
 * Created by shubhamdhabhai on 18/04/18.
 */

public abstract class AttachmentSender {

    protected BaseChannel channel;
    @DrawableRes
    //TODO set a default resource here
    protected int drawableRes;
    protected String title = "Attachment";

    private UploadListener uploadListener;

    public interface UploadListener {
        void onUploadProgress(int progress);

        void onUploadSuccess();

        void onUploadFailed(ChatCampException error);
    }

    public AttachmentSender(@NonNull BaseChannel channel, @NonNull String title, @NonNull @DrawableRes int drawableRes) {
        this.channel = channel;
        this.drawableRes = drawableRes;
        this.title = title;
    }

    public void setUploadListener(UploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    public int getDrawableRes() {
        return drawableRes;
    }

    public String getTitle() {
        return title;
    }

    protected void sendAttachment(final File file, String fileName, final String contentType) {
        MessageParams params = new MessageParams();
        params.setFile(file);
        params.setAttachmentType(contentType);
        List<ThumbnailDimension> thumbnailDimensions = new ArrayList<>();
        ThumbnailDimension dimension = new ThumbnailDimension(100, 100);
        ThumbnailDimension dimension1 = new ThumbnailDimension(100, 100);
        thumbnailDimensions.add(dimension);
        thumbnailDimensions.add(dimension1);
        params.setThumbnailDimension(thumbnailDimensions);
        channel.sendAttachment(params
                , new GroupChannel.UploadAttachmentListener() {
                    @Override
                    public void onUploadProgress(int progress) {
                        if (uploadListener != null) {
                            uploadListener.onUploadProgress(progress);
                        }
                    }

                    @Override
                    public void onUploadSuccess(Message message) {
                        if (uploadListener != null) {
                            uploadListener.onUploadSuccess();
                        }
                        // delete the temp file created to downsize the image
//                        if(contentType.contains("image")) {
//                            try {
//                                file.delete();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
                    }

                    @Override
                    public void onUploadFailed(Throwable error) {
                        if (uploadListener != null) {
                            ChatCampException exception = new ChatCampException(error.getMessage(), "FILE UPLOAD FAILED");
                            uploadListener.onUploadFailed(exception);
                        }
                    }
                });
    }

    protected void sendAttachmentError(ChatCampException e) {
        if(uploadListener != null) {
            uploadListener.onUploadFailed(e);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {}

    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {}

    public abstract void clickSend();
}
