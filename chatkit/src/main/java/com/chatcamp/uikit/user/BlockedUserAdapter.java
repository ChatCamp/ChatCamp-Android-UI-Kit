package com.chatcamp.uikit.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.messages.RecyclerScrollMoreListener;
import com.chatcamp.uikit.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.Participant;
import io.chatcamp.sdk.User;
import io.chatcamp.sdk.UserListQuery;

/**
 * Created by shubhamdhabhai on 30/08/18.
 */

public class BlockedUserAdapter extends RecyclerView.Adapter<BlockedUserAdapter.BlockedUserViewHolder>
        implements RecyclerScrollMoreListener.OnLoadMoreListener {

    private final Context context;
    private BlockedUserListStyle style;
    private BlockedUserList.OnBlockedUsersLoadedListener onBlockedUsersLoadedListener;
    private BlockedUserList.OnUnBlockUserClickedListener onUnBlockUserClickedListener;
    private RecyclerScrollMoreListener recyclerScrollMoreListener;
    private UserListQuery userListQuery;
    private boolean isFirstTime = true;
    private List<User> userList = new ArrayList<>();

    public BlockedUserAdapter(Context context) {
        this.context = context;
    }

    public void setStyle(BlockedUserListStyle style) {
        this.style = style;
    }

    public void setOnBlockedUsersLoadedListener(BlockedUserList.OnBlockedUsersLoadedListener onBlockedUsersLoadedListener) {
        this.onBlockedUsersLoadedListener = onBlockedUsersLoadedListener;
    }

    public void setOnUnBlockUserClickedListener(BlockedUserList.OnUnBlockUserClickedListener onUnBlockUserClickeListener) {
        this.onUnBlockUserClickedListener = onUnBlockUserClickeListener;
    }

    public void setRecyclerScrollMoreListener(RecyclerScrollMoreListener recyclerScrollMoreListener) {
        this.recyclerScrollMoreListener = recyclerScrollMoreListener;
    }

    public void init() {
        userListQuery = ChatCamp.createBlockedUserListQuery();
        loadBlockedUsers();
        recyclerScrollMoreListener.stopLoading();
    }

    private void loadBlockedUsers() {
        userListQuery.load(20, new UserListQuery.ResultHandler() {
            @Override
            public void onResult(List<User> userList, ChatCampException e) {
                boolean listLoaded = false;
                if(isFirstTime) {
                    recyclerScrollMoreListener.resetLoading();
                    isFirstTime = false;
                    if(onBlockedUsersLoadedListener != null) {
                       listLoaded = true;
                    }
                }
                BlockedUserAdapter.this.userList.addAll(userList);
                if(listLoaded) {
                    onBlockedUsersLoadedListener.onBlockUsersLoaded();
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public BlockedUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BlockedUserViewHolder(LayoutInflater
                .from(parent.getContext()).inflate(R.layout.layout_blocked_user, parent, false));
    }

    @Override
    public void onBindViewHolder(BlockedUserViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public void onLoadMore(int page, int total) {
        loadBlockedUsers();
    }

    public class BlockedUserViewHolder extends RecyclerView.ViewHolder {

        private ImageView avatar;
        private TextView nameTv;
        private ImageView onlineIndicator;
        private TextView unblockTv;

        public BlockedUserViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            nameTv = itemView.findViewById(R.id.tv_name);
            onlineIndicator = itemView.findViewById(R.id.iv_online);
            unblockTv = itemView.findViewById(R.id.tv_unblock);
        }

        public void bind(final int position) {
            User user = userList.get(position);
            Picasso.with(context).load(user.getAvatarUrl())
                    .placeholder(com.chatcamp.uikit.R.drawable.icon_default_contact)
                    .transform(new CircleTransform()).into(avatar);
            nameTv.setText(user.getDisplayName());
            if(user.isOnline()) {
                onlineIndicator.setVisibility(View.VISIBLE);
            } else {
                onlineIndicator.setVisibility(View.GONE);
            }
            unblockTv.setTag(user);
            unblockTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v.getTag() != null && v.getTag() instanceof User) {
                        boolean eventConsumed = false;
                        User localUser = (User) v.getTag();
                        if(onUnBlockUserClickedListener != null) {
                            eventConsumed = onUnBlockUserClickedListener.unBlockUserClicked(localUser);
                        }
                        if(!eventConsumed) {
                            ChatCamp.unBlockUser(localUser.getId(), new ChatCamp.OnUserUnBlockListener() {
                                @Override
                                public void onUserUnBlocked(Participant participant, ChatCampException exception) {
                                    userList.remove(position);
                                    notifyItemRemoved(position);
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
