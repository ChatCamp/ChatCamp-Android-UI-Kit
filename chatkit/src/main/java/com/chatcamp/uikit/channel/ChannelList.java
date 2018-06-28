package com.chatcamp.uikit.channel;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;

import com.chatcamp.uikit.commons.ImageLoader;
import com.chatcamp.uikit.messages.RecyclerScrollMoreListener;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.GroupChannelListQuery;

/**
 * Created by shubhamdhabhai on 18/05/18.
 */

public class ChannelList extends RecyclerView {

    private ChannelAdapter adapter;
    private ChannelAdapter.ChannelClickedListener clickListener;
    private ChannelListStyle channelListStyle;
    private RecyclerScrollMoreListener recyclerScrollMoreListener;
    private OnChannelsLoadedListener onChannelsLoadedListener;

    public interface OnChannelsLoadedListener {
        void onChannelsLoaded();
    }


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
        setChannelType(channelType, participantState, null);
    }

    public void setChannelType(BaseChannel.ChannelType channelType, GroupChannelListQuery.ParticipantState participantState, ChannelAdapter.ChannelComparator comparator) {
        if(recyclerScrollMoreListener != null) {
            recyclerScrollMoreListener.resetLoading();
        }
        adapter.setChannelType(channelType, participantState, comparator);
    }

    public void setChannelClickListener(ChannelAdapter.ChannelClickedListener channelClickListener) {
        clickListener = channelClickListener;
        adapter.setChannelClickedListener(channelClickListener);
    }

    public void setOnChannelsLoadedListener(OnChannelsLoadedListener listener) {
        onChannelsLoadedListener = listener;
        adapter.setOnChannelsLoadedListener(onChannelsLoadedListener);
    }

    public void setAvatarImageLoader(ImageLoader imageLoader) {
        if(adapter != null) {
            adapter.setAvatarImageLoader(imageLoader);
        }
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
        recyclerScrollMoreListener = new RecyclerScrollMoreListener(layoutManager, adapter);
        addOnScrollListener(recyclerScrollMoreListener);
        adapter.setRecyclerScrollMoreListener(recyclerScrollMoreListener);
        super.setAdapter(adapter);

    }

    private void parseStyle(Context context, AttributeSet attrs) {
        channelListStyle = ChannelListStyle.parseStyle(context, attrs);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        adapter.onWindowVisibilityChanged(visibility);
    }
}
