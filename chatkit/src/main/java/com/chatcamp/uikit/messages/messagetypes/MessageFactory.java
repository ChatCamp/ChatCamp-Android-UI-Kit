package com.chatcamp.uikit.messages.messagetypes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chatcamp.uikit.messages.MessagesListAdapter;
import com.chatcamp.uikit.messages.MessagesListStyle;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 21/04/18.
 */

public abstract class MessageFactory<T extends MessageFactory.MessageHolder> {

    protected Message message;

    protected MessageSpecs messageSpecs;

    protected MessagesListStyle messageStyle;

    public abstract boolean isBindable(Message message);

    public abstract T createMessageHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater);

    public abstract void bindMessageHolder(T messageHolder,@NonNull Message message);

    public void setMessageSpecs(MessageSpecs messageStyle) {
        this.messageSpecs = messageStyle;
        if(messageStyle != null) {
            manipulateMessageStyle();
//            messageSpecs.messageStyle = this.messageStyle;
        }
    }

    public void setMessageStyle(MessagesListStyle messageStyle) {
        this.messageStyle = messageStyle;
        if(messageSpecs != null) {
            manipulateMessageStyle();
//            messageSpecs.messageStyle = messageStyle;
        }

    }

    public void manipulateMessageStyle() { }

    public void onActivityResult(int requestCode, int resultCode, Intent dataFile){ }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) { }

    public static abstract class MessageHolder {
//        private Message message;
//
//        public MessageHolder setMessage(Message message) {
//            this.message  = message;
//            return this;
//        }
//
//        public Message getMessage() {
//            return message;
//        }
    }

    public static class MessageSpecs {
        public boolean isMe;
        public int position;
        public boolean isFirstMessage;
//        public MessagesListStyle messageStyle;
    }

    public void onViewVisibilityChange(int visibility) {

    }

    public void onViewDetachedFromWindow(String messageId) {
    }
}
