package com.chatcamp.uikit.user;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;

import com.chatcamp.uikit.messages.RecyclerScrollMoreListener;

import io.chatcamp.sdk.User;

/**
 * Created by shubhamdhabhai on 30/08/18.
 */

public class BlockedUserList extends RecyclerView {

    private OnUnBlockUserClickedListener onUnBlockUserClickedListener;

    private OnBlockedUsersLoadedListener onBlockedUsersLoadedListener;

    private BlockedUserListStyle style;

    private BlockedUserAdapter adapter;
    private RecyclerScrollMoreListener recyclerScrollMoreListener;

    public interface OnUnBlockUserClickedListener {
        boolean unBlockUserClicked(User user);
    }

    public interface OnBlockedUsersLoadedListener {
        void onBlockUsersLoaded();
    }

    public BlockedUserList(Context context) {
        super(context);
    }

    public BlockedUserList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs);
        init();

    }

    public BlockedUserList(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs);
        init();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("You can't set adapter to BlockedUserList.");
    }

    public void setOnUnBlockUserClickedListener(OnUnBlockUserClickedListener listener) {
        onUnBlockUserClickedListener = listener;
        adapter.setOnUnBlockUserClickedListener(onUnBlockUserClickedListener);
    }

    public void setOnBlockedUsersLoadedListener(OnBlockedUsersLoadedListener listener) {
        onBlockedUsersLoadedListener = listener;
        adapter.setOnBlockedUsersLoadedListener(onBlockedUsersLoadedListener);
    }

    public void initialize() {
        adapter.init();
    }

    private void parseStyle(Context context, AttributeSet attrs) {
        style = BlockedUserListStyle.parseStyle(context, attrs);
    }

    private void init() {
        adapter = new BlockedUserAdapter(getContext());
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);

        setItemAnimator(itemAnimator);
        setLayoutManager(layoutManager);
        adapter.setStyle(style);
        recyclerScrollMoreListener = new RecyclerScrollMoreListener(layoutManager, adapter);
        addOnScrollListener(recyclerScrollMoreListener);
        adapter.setRecyclerScrollMoreListener(recyclerScrollMoreListener);
        super.setAdapter(adapter);
    }
}
