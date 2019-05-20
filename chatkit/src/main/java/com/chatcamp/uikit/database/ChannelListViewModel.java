package com.chatcamp.uikit.database;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.chatcamp.uikit.channel.ChannelAdapter;

import java.util.List;

public class ChannelListViewModel {

    private ChannelDataSource channelDataSource;

    public ChannelListViewModel(Context context) {
        channelDataSource = new ChannelDataSource(context);
    }

    public void setChannelType(final DbBaseChannelWrapper.ChannelType channelType, final DbGroupWrapper.ParticipantState participantState,
                               List<String> customFilter, ChannelAdapter.ChannelComparator comparator) {
        channelDataSource.setChannelType(channelType, participantState, customFilter, comparator);
    }

    public void loadChannels() {
        channelDataSource.loadChannels();
    }

    public MutableLiveData<Boolean> getRecyclerLoadingStateLiveData() {
        return channelDataSource.getRecyclerLoadingStateLiveData();
    }

    public MutableLiveData<List<DbBaseChannelWrapper>> getInitialBaseChannelLiveData() {
        return channelDataSource.getInitialBaseChannelLiveData();
    }

    public MutableLiveData<List<DbBaseChannelWrapper>> getBaseChannelLiveData() {
        return channelDataSource.getBaseChannelLiveData();
    }

    public MutableLiveData getChannelLoadedLiveData() {
        return channelDataSource.getChannelLoadedLiveData();
    }

    public MutableLiveData<Boolean> getLoadViewLiveData() {
        return channelDataSource.getLoadViewLiveData();
    }

    public MutableLiveData<DbGroupWrapper> getUpdateGroupLiveData() {
        return channelDataSource.getUpdateGroupLiveData();
    }

}
