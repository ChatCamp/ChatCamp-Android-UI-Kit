package com.chatcamp.uikit.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.customview.AvatarView;
import com.chatcamp.uikit.messages.RecyclerScrollMoreListener;
import com.chatcamp.uikit.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.User;
import io.chatcamp.sdk.UserListQuery;

public class SelectableUserAdapter extends RecyclerView.Adapter<SelectableUserAdapter.SelectableUserViewHolder> implements RecyclerScrollMoreListener.OnLoadMoreListener {

    private final Context context;
    private RecyclerScrollMoreListener recyclerScrollMoreListener;
    private UserListQuery userListQuery;
    private boolean isFirstTime = true;
    private List<User> userList = new ArrayList<>();
    private String params;
    private List<String> userIds;
    private List<User> selectedUsers = new ArrayList<>();
    private View loadingView;
    private Timer timer=new Timer();
    private final long DELAY = 500;

    public SelectableUserAdapter(Context context) {
        this.context = context;
    }

    public void setRecyclerScrollMoreListener(RecyclerScrollMoreListener recyclerScrollMoreListener) {
        this.recyclerScrollMoreListener = recyclerScrollMoreListener;
    }

    public void searchUsers(final String param, final List<String> userIds) {

        timer.cancel();
        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        // TODO: do what you need here (refresh list)
                        // you will probably need to use runOnUiThread(Runnable action) for some specific actions
                        SelectableUserAdapter.this.params = param;
                        SelectableUserAdapter.this.userIds = userIds;
                        isFirstTime = true;
                        userListQuery = ChatCamp.createUserListQuery();
                        if(!TextUtils.isEmpty(param)) {
                            userListQuery.setDisplayNameSearch(param);
                        }
                        loadUsers();
                        recyclerScrollMoreListener.stopLoading();
                        if (recyclerScrollMoreListener != null) {
                            recyclerScrollMoreListener.stopLoading();
                        }
                    }
                },
                DELAY
        );
    }

    private void loadUsers() {
        userListQuery.load(20, new UserListQuery.ResultHandler() {
            @Override
            public void onResult(List<User> userList, ChatCampException e) {
                {
                    boolean listLoaded = false;
                    if (isFirstTime) {
                        recyclerScrollMoreListener.resetLoading();
                        isFirstTime = false;
                        SelectableUserAdapter.this.userList.clear();
//                if(onBlockedUsersLoadedListener != null) {
//                    listLoaded = true;
//                }
                    }
                    if (userIds != null) {
                        Iterator<User> iterator = userList.iterator();
                        while (iterator.hasNext()) {
                            User user = iterator.next();
                            for (String userId : userIds) {
                                if (user.getId().equals(userId)) {
                                    iterator.remove();
                                    break;
                                }
                            }
                        }

                    }
                    SelectableUserAdapter.this.userList.addAll(userList);
//            if(listLoaded) {
//                onBlockedUsersLoadedListener.onBlockUsersLoaded();
//            }
                    if(loadingView != null) {
                        loadingView.setVisibility(View.GONE);
                    }
                    notifyDataSetChanged();
                }
            }
        });


    }

    @NonNull
    @Override
    public SelectableUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectableUserViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_user_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectableUserViewHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public void onLoadMore(int page, int total) {
        if(loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
        loadUsers();
    }

    public List<User> getSelectedUsers() {
        return selectedUsers;
    }

    public void setLoadingView(View view) {
        loadingView = view;
    }

    public class SelectableUserViewHolder extends RecyclerView.ViewHolder {
        private AvatarView avatar;
        private TextView nameTv;
        private AppCompatCheckBox checkBox;

        public SelectableUserViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.iv_avatar);
            nameTv = itemView.findViewById(R.id.tv_name);
            checkBox = itemView.findViewById(R.id.cb_select);
        }

        public void bind(final User user) {
            avatar.initView(user.getAvatarUrl(), user.getDisplayName());
            nameTv.setText(user.getDisplayName());
            checkBox.setTag(user);
            boolean isSelected = false;
            for (User user1 : selectedUsers) {
                if (user.getId().equals(user1.getId())) {
                    isSelected = true;
                    break;
                }
            }
            if (isSelected) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.getTag() != null && view.getTag() instanceof User) {
                        User clickedUser = (User) view.getTag();
                        Iterator<User> iterator = selectedUsers.iterator();
                        boolean isSelected1 = false;
                        while (iterator.hasNext()) {
                            if (clickedUser.getId().equals(iterator.next().getId())) {
                                isSelected1 = true;
                                iterator.remove();
                                break;
                            }
                        }
                        if (isSelected1) {
                            checkBox.setChecked(false);
                        } else {
                            checkBox.setChecked(true);
                            selectedUsers.add(clickedUser);
                        }
                    }
                }
            });
        }
    }
}
