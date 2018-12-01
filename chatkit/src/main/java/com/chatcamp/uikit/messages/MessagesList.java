package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.chatcamp.uikit.channel.ChannelList;
import com.chatcamp.uikit.commons.ImageLoader;
import com.chatcamp.uikit.messages.messagetypes.MessageFactory;
import com.chatcamp.uikit.messages.typing.TypingFactory;

import io.chatcamp.sdk.BaseChannel;

/**
 * Component for displaying list of messages
 */
public class MessagesList extends RecyclerView {
    private MessagesListStyle messagesListStyle;
    private RecyclerScrollMoreListener recyclerScrollMoreListener;
    private BaseChannel channel;
    private MessagesListAdapter adapter;
    private OnMessagesLoadedListener onMessagesLoadedListener;

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
    }

    public void setOnMessagesLoadedListener(OnMessagesLoadedListener onMessagesLoadedListener) {
        this.onMessagesLoadedListener = onMessagesLoadedListener;
        adapter.setOnMesaageLoadedListener(onMessagesLoadedListener);
    }

    public void setAvatarImageLoader(ImageLoader imageLoader) {
        adapter.setAvatarImageLoader(imageLoader);
    }

    public void setLoadingView(View view) {
        adapter.setLoadingView(view);
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
        recyclerScrollMoreListener = new RecyclerScrollMoreListener(layoutManager, adapter);
        addOnScrollListener(recyclerScrollMoreListener);
        adapter.setRecyclerScrollMoreListener(recyclerScrollMoreListener);
        super.setAdapter(adapter);
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(adapter != null) {
            adapter.onDetachedFromWindow();
        }
    }
}
