package com.chatcamp.uikit.database;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.Message;

public class MessageViewModel {

    private MessageDataSource dataSource;

    public MessageViewModel(Context context, BaseChannel channel) {
        dataSource = MessageDataSource.getInstance(context, channel);
    }

    public void setMessageList(List<DbMessageWrapper> messageList) {
        dataSource.setMessageList(messageList);
    }

    public MutableLiveData<List<DbMessageWrapper>> getMessageListLiveData() {
        return dataSource.getMessageListLiveData();
    }

    public MutableLiveData<Diff<DbMessageWrapper>> getDiffLiveData() {
        return dataSource.getDiffLiveData();
    }

    public MutableLiveData<List<DbMessageWrapper>> getInitialMessageListLiveData() {
        return dataSource.getInitialMessageListLiveData();
    }

    public MutableLiveData<Boolean> getRecyclerLoadingStateLiveData() {
        return dataSource.getRecyclerLoadingStateLiveData();
    }

    public MutableLiveData<Long> getMessageLastReadLiveData() {
        return dataSource.getMessageLastReadLiveData();
    }

    public MutableLiveData<Boolean> getLoadingViewLiveData() {
        return dataSource.getLoadingViewLiveData();
    }

    public MutableLiveData getOnMessageLoadedLiveData() {
        return dataSource.getOnMessageLoadedLiveData();
    }

    public MutableLiveData<Diff<MessageDataSource.TypingStatus>> getTypingStatusLiveData() {
        return dataSource.getTypingStatusLiveData();
    }

    public MutableLiveData<ChatCamp.NetworkState> getNetworkStateMutableLiveData() {
        return dataSource.getNetworkStateMutableLiveData();
    }

    public void loadMessages() {
        dataSource.loadMessages();
    }

    public void sendMessage(DbMessageWrapper messageWrapper) {
        dataSource.sendMessage(messageWrapper);
    }
    public MutableLiveData getMarkAsReadLiveData() {
        return dataSource.getMarkAsReadLiveData();
    }

    public void markAsRead() {
        dataSource.markAsRead();
    }
}
