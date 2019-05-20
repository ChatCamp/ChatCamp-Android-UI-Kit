package com.chatcamp.uikit.messages;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.chatcamp.uikit.commons.ImageLoader;
import com.chatcamp.uikit.database.DbMessageWrapper;
import com.chatcamp.uikit.database.Diff;
import com.chatcamp.uikit.database.MessageDataSource;
import com.chatcamp.uikit.database.MessageViewModel;
import com.chatcamp.uikit.messages.messagetypes.MessageFactory;
import com.chatcamp.uikit.messages.typing.TypingFactory;

import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;

/**
 * Component for displaying list of messages
 */
public class MessagesList extends RecyclerView implements RecyclerScrollMoreListener.OnLoadMoreListener, LifecycleOwner {
    private MessagesListStyle messagesListStyle;
    private BaseChannel channel;
    private MessagesListAdapter adapter;
    private OnMessagesLoadedListener onMessagesLoadedListener;
    private View loadingView;
    private MessageViewModel messageModelView;
    private LifecycleRegistry lifecycleRegistry;
    private RecyclerScrollMoreListener recyclerScrollMoreListener;

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    public interface OnMessagesLoadedListener {
        void onMessagesLoaded();
    }

    public MessagesList(Context context) {
        super(context);
    }

    public MessagesList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init();
    }

    public MessagesList(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs);
        init();
    }

    private void init() {
        adapter = new MessagesListAdapter(getContext());
        lifecycleRegistry = new LifecycleRegistry(this);
        setAdapter(adapter);
    }

    public void addMessageFactories(MessageFactory... messageFactories) {
        adapter.addMessageFactories(messageFactories);
    }

    public void setTypingFactory(TypingFactory typingFactory) {
        adapter.addTypingFactory(typingFactory);
    }

    public void setChannel(BaseChannel channel) {
        this.channel = channel;
        adapter.setChannel(channel);
        messageModelView = new MessageViewModel(getContext(), channel);
        adapter.setViewModel(messageModelView);

        messageModelView.getInitialMessageListLiveData().observe(this, new Observer<List<DbMessageWrapper>>() {
            @Override
            public void onChanged(@Nullable List<DbMessageWrapper> messageWrappers) {
                adapter.clear();
                adapter.addAll(messageWrappers);
                adapter.notifyDataSetChanged();
                messageModelView.setMessageList(adapter.getMessageList());
            }
        });

        messageModelView.getDiffLiveData().observe(this, new Observer<Diff<DbMessageWrapper>>() {
            @Override
            public void onChanged(@Nullable Diff<DbMessageWrapper> diff) {
                Log.e("message ", diff.getPosition() + ": position in message list : " + diff.getChange().toString());
                if (diff.getChange() == Diff.CHANGE.INSERT) {
                    adapter.add(diff.getModel());
                    Log.e("insert message ", diff.getPosition() + ": position in message list");
                } else if (diff.getChange() == Diff.CHANGE.REMOVE) {
                    Log.e("remove message ", diff.getPosition() + ": position in message list");
                    adapter.remove(diff.getPosition());
                    adapter.add(diff.getModel());
                } else if (diff.getChange() == Diff.CHANGE.UPDATE) {
                    adapter.update(diff.getModel(), diff.getPosition());
                }
                messageModelView.setMessageList(adapter.getMessageList());
            }
        });

        messageModelView.getMessageListLiveData().observe(this, new Observer<List<DbMessageWrapper>>() {
            @Override
            public void onChanged(@Nullable List<DbMessageWrapper> messageWrappers) {
                adapter.addAll(messageWrappers);
                adapter.notifyDataSetChanged();
                messageModelView.setMessageList(adapter.getMessageList());
            }
        });

        messageModelView.getRecyclerLoadingStateLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean recyclerLoadingState) {
                if (recyclerLoadingState) {
                    recyclerScrollMoreListener.resetLoading();
                } else {
                    recyclerScrollMoreListener.stopLoading();
                }
            }
        });

        messageModelView.getMessageLastReadLiveData().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long messageLastRead) {
                adapter.setLastReadTime(messageLastRead);
            }
        });

        messageModelView.getOnMessageLoadedLiveData().observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                if(onMessagesLoadedListener != null) {
                    onMessagesLoadedListener.onMessagesLoaded();
                }
            }
        });

        messageModelView.getLoadingViewLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if(aBoolean) {
                    loadingView.setVisibility(VISIBLE);
                } else {
                    loadingView.setVisibility(GONE);
                }
            }
        });
        messageModelView.loadMessages();


        messageModelView.getTypingStatusLiveData().observe(this, new Observer<Diff<MessageDataSource.TypingStatus>>() {
            @Override
            public void onChanged(@Nullable Diff<MessageDataSource.TypingStatus> booleanDiff) {
                adapter.setTypingStatus(booleanDiff);
            }
        });

        messageModelView.getNetworkStateMutableLiveData().observe(this, new Observer<ChatCamp.NetworkState>() {
            @Override
            public void onChanged(@Nullable ChatCamp.NetworkState networkState) {

            }
        });

        messageModelView.getMarkAsReadLiveData().observe(this, new Observer() {
            @Override
            public void onChanged(@Nullable Object o) {
                messageModelView.markAsRead();
            }
        });
    }

    public void setOnMessagesLoadedListener(OnMessagesLoadedListener onMessagesLoadedListener) {
        this.onMessagesLoadedListener = onMessagesLoadedListener;
       // adapter.setOnMesaageLoadedListener(onMessagesLoadedListener);
    }

    public void setAvatarImageLoader(ImageLoader imageLoader) {
        adapter.setAvatarImageLoader(imageLoader);
    }

    public void setLoadingView(View view) {
        this.loadingView = view;
    }

    /**
     * Don't use this method for setting your adapter, otherwise exception will by thrown.
     * Call {@link #setAdapter(MessagesListAdapter)} instead.
     */
    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("You can't set adapter to MessagesList. Use #setAdapter(MessagesListAdapter) instead.");
    }

    /**
     * Sets adapter for MessagesList
     *
     * @param adapter Adapter. Must extend MessagesListAdapter
     */
    private void setAdapter(MessagesListAdapter adapter) {
        setAdapter(adapter, true);
    }

    /**
     * Sets adapter for MessagesList
     *
     * @param adapter       Adapter. Must extend MessagesListAdapter
     * @param reverseLayout weather to use reverse layout for layout manager.
     */
    private void setAdapter(MessagesListAdapter adapter, boolean reverseLayout) {
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, reverseLayout);

        setItemAnimator(itemAnimator);
        setLayoutManager(layoutManager);
        adapter.setMessagesListStyle(messagesListStyle);
        recyclerScrollMoreListener = new RecyclerScrollMoreListener(layoutManager, this);
        addOnScrollListener(recyclerScrollMoreListener);
        super.setAdapter(adapter);
    }

    @Override
    public void onLoadMore(int page, int total) {
//        if (loadingView != null) {
//            loadingView.setVisibility(VISIBLE);
//        }
        messageModelView.loadMessages();
    }

    @SuppressWarnings("ResourceType")
    private void parseStyle(Context context, AttributeSet attrs) {
        messagesListStyle = MessagesListStyle.parse(context, attrs);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        adapter.onActivityResult(requestCode, resultCode, dataFile);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        adapter.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        adapter.onWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lifecycleRegistry.markState(Lifecycle.State.STARTED);
    }

    @Override
    protected void onDetachedFromWindow() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
        super.onDetachedFromWindow();
        if (adapter != null) {
            adapter.onDetachedFromWindow();
        }
    }

}
