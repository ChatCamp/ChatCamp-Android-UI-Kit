package com.chatcamp.uikit.channel;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.View;

import com.chatcamp.uikit.commons.ImageLoader;
import com.chatcamp.uikit.database.ChannelListViewModel;
import com.chatcamp.uikit.database.DbBaseChannelWrapper;
import com.chatcamp.uikit.database.DbGroupWrapper;
import com.chatcamp.uikit.messages.RecyclerScrollMoreListener;

import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.GroupChannelListQuery;

/**
 * Created by shubhamdhabhai on 18/05/18.
 */

public class ChannelList extends RecyclerView implements RecyclerScrollMoreListener.OnLoadMoreListener, LifecycleOwner {

    private ChannelAdapter adapter;
    private ChannelAdapter.ChannelClickedListener clickListener;
    private ChannelListStyle channelListStyle;
    private RecyclerScrollMoreListener recyclerScrollMoreListener;
    private OnChannelsLoadedListener onChannelsLoadedListener;
    private ChannelListViewModel channelListViewModel;
    private LifecycleRegistry registry;
    private View loadingView;

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }

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

    public void setChannelType(BaseChannel.ChannelType channelType, GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter participantState) {
        setChannelType(channelType, participantState, null);
    }

    public void setChannelType(BaseChannel.ChannelType channelType, GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter participantState, ChannelAdapter.ChannelComparator comparator) {
        setChannelType(channelType, participantState, null, comparator);
    }

    public void setChannelType(BaseChannel.ChannelType channelType, GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter participantState,
                               List<String> customFilter, ChannelAdapter.ChannelComparator comparator) {
//        if(recyclerScrollMoreListener != null) {
//            recyclerScrollMoreListener.resetLoading();
//        }
        DbBaseChannelWrapper.ChannelType type = DbBaseChannelWrapper.ChannelType.GROUP_CHANNEL;
        if (channelType == BaseChannel.ChannelType.OPEN) {
            type = DbBaseChannelWrapper.ChannelType.OPEN_CHANNEL;
        }

        DbGroupWrapper.ParticipantState state = DbGroupWrapper.ParticipantState.ALL;
        if (participantState == GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter.PARTICIPANT_STATE_ACCEPTED) {
            state = DbGroupWrapper.ParticipantState.ACCEPTED;
        } else if (participantState == GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter.PARTICIPANT_STATE_INVITED) {
            state = DbGroupWrapper.ParticipantState.INVITED;
        }
        channelListViewModel.setChannelType(type, state, customFilter, comparator);
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
        if (adapter != null) {
            adapter.setAvatarImageLoader(imageLoader);
        }
    }

    public void setLoadingView(View view) {
        this.loadingView = view;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("You can't set adapter to ChannelList.");
    }

    private void init() {
        adapter = new ChannelAdapter(getContext());
        registry = new LifecycleRegistry(this);
        channelListViewModel = new ChannelListViewModel(getContext());
        channelListViewModel.getBaseChannelLiveData().observe(this, new Observer<List<DbBaseChannelWrapper>>() {
            @Override
            public void onChanged(@Nullable List<DbBaseChannelWrapper> dbBaseChannelWrappers) {
                adapter.addAll(dbBaseChannelWrappers);
                adapter.notifyDataSetChanged();
            }
        });

        channelListViewModel.getInitialBaseChannelLiveData().observe(this, new Observer<List<DbBaseChannelWrapper>>() {
            @Override
            public void onChanged(@Nullable List<DbBaseChannelWrapper> dbBaseChannelWrappers) {
                adapter.clear();
                adapter.addAll(dbBaseChannelWrappers);
                adapter.notifyDataSetChanged();
            }
        });

        channelListViewModel.getRecyclerLoadingStateLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean) {
                    recyclerScrollMoreListener.resetLoading();
                } else {
                    recyclerScrollMoreListener.stopLoading();
                }
            }
        });

        channelListViewModel.getChannelLoadedLiveData().observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                if (onChannelsLoadedListener != null) {
                    onChannelsLoadedListener.onChannelsLoaded();
                }
            }
        });

        channelListViewModel.getLoadViewLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (loadingView != null) {
                    if (aBoolean) {
                        loadingView.setVisibility(VISIBLE);
                    } else {
                        loadingView.setVisibility(GONE);
                    }
                }
            }
        });

        channelListViewModel.getUpdateGroupLiveData().observe(this, new Observer<DbGroupWrapper>() {
            @Override
            public void onChanged(@Nullable DbGroupWrapper dbGroupWrapper) {
                adapter.updateGroup(dbGroupWrapper);
            }
        });


        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);

        setItemAnimator(itemAnimator);
        setLayoutManager(layoutManager);
        adapter.setStyle(channelListStyle);
        recyclerScrollMoreListener = new RecyclerScrollMoreListener(layoutManager, this);
        addOnScrollListener(recyclerScrollMoreListener);
//        adapter.setRecyclerScrollMoreListener(recyclerScrollMoreListener);
        super.setAdapter(adapter);

    }

    private void parseStyle(Context context, AttributeSet attrs) {
        channelListStyle = ChannelListStyle.parseStyle(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registry.markState(Lifecycle.State.STARTED);
    }

    @Override
    protected void onDetachedFromWindow() {
        registry.markState(Lifecycle.State.DESTROYED);
        super.onDetachedFromWindow();
    }

    //    @Override
//    protected void onWindowVisibilityChanged(int visibility) {
//        super.onWindowVisibilityChanged(visibility);
//        adapter.onWindowVisibilityChanged(visibility);
//    }

    @Override
    public void onLoadMore(int page, int total) {
        channelListViewModel.loadChannels();
    }
}
