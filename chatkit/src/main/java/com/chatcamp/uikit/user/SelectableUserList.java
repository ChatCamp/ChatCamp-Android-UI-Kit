package com.chatcamp.uikit.user;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.View;

import com.chatcamp.uikit.messages.RecyclerScrollMoreListener;

import java.util.List;

import io.chatcamp.sdk.User;

public class SelectableUserList extends RecyclerView {

    private RecyclerScrollMoreListener recyclerScrollMoreListener;
    private SelectableUserAdapter adapter;

    public SelectableUserList(Context context) {
        super(context);
        init();
    }

    public SelectableUserList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectableUserList(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        adapter = new SelectableUserAdapter(getContext());
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);

        setItemAnimator(itemAnimator);
        setLayoutManager(layoutManager);
        recyclerScrollMoreListener = new RecyclerScrollMoreListener(layoutManager, adapter);
        addOnScrollListener(recyclerScrollMoreListener);
        adapter.setRecyclerScrollMoreListener(recyclerScrollMoreListener);
        super.setAdapter(adapter);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new IllegalArgumentException("You can't set adapter to ChannelList.");
    }

    public void search(String param, List<String> userIds) {
        adapter.searchUsers(param, userIds);
    }

    public List<User> getSelectedUsers() {
        return adapter.getSelectedUsers();
    }

    public void setLoadingView(View view) {
        adapter.setLoadingView(view);
    }
}
