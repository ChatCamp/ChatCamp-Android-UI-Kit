package com.chatcamp.uikit.user;

import android.content.Context;
import android.support.v4.widget.Space;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.customview.AvatarView;
import com.chatcamp.uikit.messages.RecyclerScrollMoreListener;
import com.chatcamp.uikit.utils.TextViewFont;

import java.util.ArrayList;
import java.util.List;

import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
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

        private AvatarView avatar;
        private TextViewFont nameTv;
        private ImageView onlineIndicator;
        private TextViewFont unblockTv;
        private RelativeLayout avatarContainer;
        private Space space;

        public BlockedUserViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            nameTv = itemView.findViewById(R.id.tv_name);
            onlineIndicator = itemView.findViewById(R.id.iv_online);
            unblockTv = itemView.findViewById(R.id.tv_unblock);
            avatarContainer = itemView.findViewById(R.id.container_avatar);
            space = itemView.findViewById(R.id.space);
        }

        public void bind(final int position) {
            User user = userList.get(position);
            boolean showAvatar = style.isShowUserAvatar();

            if(showAvatar) {
                int avatarHeight = style.getUserAvatarHeight();
                int avatarWidth = style.getUserAvatarWidth();

                int onlineIndicatorWidth = style.getOnlineIndicatorWidth() ;
                int onlineIndicatorHeight = style.getOnlineIndicatirHeight();

                LinearLayout.LayoutParams avatarParams
                        = (LinearLayout.LayoutParams) avatarContainer.getLayoutParams();
                avatarParams.height = avatarHeight;
                avatarParams.width = avatarWidth;

                RelativeLayout.LayoutParams onlineIndicatorParams
                        = (RelativeLayout.LayoutParams) onlineIndicator.getLayoutParams();
                onlineIndicatorParams.height = onlineIndicatorHeight;
                onlineIndicatorParams.width = onlineIndicatorWidth;

                avatar.initView(user.getAvatarUrl(), user.getDisplayName());
                if(user.ifOnline()) {
                    onlineIndicator.setVisibility(View.VISIBLE);
                } else {
                    onlineIndicator.setVisibility(View.GONE);
                }
                avatarContainer.setVisibility(View.VISIBLE);
            } else {
                avatarContainer.setVisibility(View.GONE);
            }

            boolean showUsername = style.isShowUsername();
            if(showUsername) {
                nameTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getUsernameTextSize());
                nameTv.setTypeface(nameTv.getTypeface(), style.getUsernameTextStyle());
                nameTv.setTextColor(style.getUsernameTextColor());
                nameTv.setVisibility(View.VISIBLE);
                nameTv.setText(user.getDisplayName());
                if(!TextUtils.isEmpty(style.getCustomFont())) {
                    nameTv.setCustomFont(style.getCustomFont());
                }
                space.setVisibility(View.GONE);
            } else {
                nameTv.setVisibility(View.GONE);
                space.setVisibility(View.VISIBLE);
            }

            boolean showUnblock = style.isShowUnBlock();
            if(showUnblock) {
                unblockTv.setTextColor(style.getUnBlockTextColor());
                unblockTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getUnBlockTextSize());
                unblockTv.setTypeface(unblockTv.getTypeface(), style.getUnBlockTextStyle());
                unblockTv.setBackground(style.getunBlockDrawable());
                unblockTv.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(style.getCustomFont())) {
                    unblockTv.setCustomFont(style.getCustomFont());
                }
            } else {
                unblockTv.setVisibility(View.GONE);
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
                            ChatCamp.unblockuser(localUser.getId(), new ChatCamp.OnUserUnblockListener() {
                                @Override
                                public void onUserUnblocked(User user, ChatCampException exception) {
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
