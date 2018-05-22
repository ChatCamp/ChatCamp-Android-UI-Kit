package com.chatcamp.uikit.channel;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.GroupChannelListQuery;

/**
 * Created by shubhamdhabhai on 18/05/18.
 */

public class ChannelList extends RecyclerView {

    private ChannelAdapter adapter;
    private ChannelAdapter.ChannelClickedListener clickListener;
    private ChannelListStyle channelListStyle;


    public ChannelList(Context context) {
        super(context);
    }

    public ChannelList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init();
    }

    public ChannelList(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs);
        init();
    }

    public void setChannelType(BaseChannel.ChannelType channelType, GroupChannelListQuery.ParticipantState participantState) {
        adapter.setChannelType(channelType, participantState);
    }

    public void setChannelClickListener(ChannelAdapter.ChannelClickedListener channelClickListener) {
        clickListener = channelClickListener;
        adapter.setChannelClickedListener(channelClickListener);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("You can't set adapter to MessagesList. Use #setAdapter(MessagesListAdapter) instead.");
    }

    private void init() {
        adapter = new ChannelAdapter(getContext());
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);

        setItemAnimator(itemAnimator);
        setLayoutManager(layoutManager);
        adapter.setStyle(channelListStyle);
        super.setAdapter(adapter);

    }

    private void parseStyle(Context context, AttributeSet attrs) {
        channelListStyle = ChannelListStyle.parseStyle(context, attrs);
    }
}
