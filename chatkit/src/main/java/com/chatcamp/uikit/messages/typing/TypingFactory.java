package com.chatcamp.uikit.messages.typing;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.chatcamp.uikit.messages.MessagesListStyle;

import java.util.List;

import io.chatcamp.sdk.Participant;

/**
 * Created by shubhamdhabhai on 30/04/18.
 */

public abstract class TypingFactory<T extends TypingFactory.TypingHolder> {

    protected MessagesListStyle messageStyle;

    public void setMessageStyle(MessagesListStyle messageStyle) {
        this.messageStyle = messageStyle;
    }

    public abstract T createView(ViewGroup parent, LayoutInflater layoutInflater);

    public abstract void bindView(T typingHolder, List<Participant> typingUsers);

    public static abstract class TypingHolder {

    }

}
