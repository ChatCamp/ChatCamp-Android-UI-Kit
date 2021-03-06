package com.chatcamp.uikit.channel;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.ImageLoader;
import com.chatcamp.uikit.customview.AvatarView;
import com.chatcamp.uikit.database.DbBaseChannelWrapper;
import com.chatcamp.uikit.database.DbGroupWrapper;
import com.chatcamp.uikit.database.DbMessageWrapper;
import com.chatcamp.uikit.utils.DefaultTimeFormat;
import com.chatcamp.uikit.utils.TextViewFont;
import com.chatcamp.uikit.utils.TimeFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.GroupChannelListQuery;
import io.chatcamp.sdk.Message;
import io.chatcamp.sdk.OpenChannelListQuery;
import io.chatcamp.sdk.Participant;

import static android.view.View.VISIBLE;

/**
 * Created by shubhamdhabhai on 18/05/18.
 */

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private static final String CHANNEL_LISTENER = "channel_list_channel_listener";
    private static final String KEY_PRODUCT_NAME = "product_name";
    private ChannelComparator comparator;
    private GroupChannelListQuery groupChannelListQuery;
    private boolean loadingFirstTime = true;
    private OpenChannelListQuery openChannelListQuery;
    private ChannelList.OnChannelsLoadedListener onChannelsLoadedListener;
    private RecyclerView recyclerView;


    public interface ChannelClickedListener {
        void onClick(DbBaseChannelWrapper baseChannel);
    }

    private List<DbBaseChannelWrapper> dataset;
    private ChannelClickedListener channelClickedListener;
    private Context context;
    private ChannelListStyle channelListStyle;
    private ImageLoader imageLoader;
    private TimeFormat timeFormat;
    private BaseChannel.ChannelType channelType;
    private List<String> customFilter;

    public ChannelAdapter(Context context) {
        dataset = new ArrayList<>();
        this.context = context;
        timeFormat = new DefaultTimeFormat();

    }

    public void clear() {
        dataset.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<DbBaseChannelWrapper> data) {
        dataset.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void updateGroup(DbGroupWrapper dbGroupWrapper) {
        boolean isListUpdated = false;
        for (int i = 0; i < dataset.size(); ++i) {
            if (dataset.get(i).getId().equals(dbGroupWrapper.getId())) {
                dataset.remove(i);
                dataset.add(0, dbGroupWrapper);
                notifyItemRemoved(i);
                notifyItemInserted(0);
                isListUpdated = true;
            }
        }
        if (!isListUpdated) {
            dataset.add(0, dbGroupWrapper);
            notifyItemInserted(0);
        }
        if (recyclerView != null && recyclerView.getLayoutManager() != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            layoutManager.scrollToPosition(0);
        }
    }

//    public void onWindowVisibilityChanged(int visibility) {
//        if (visibility == VISIBLE) {
//            addChannelListener();
//        } else {
//            removeChannelListener();
//        }
//
//    }


    private void removeChannelListener() {
        ChatCamp.removeChannelListener(CHANNEL_LISTENER);
    }

    public void setChannelClickedListener(ChannelClickedListener channelClickedListener) {
        this.channelClickedListener = channelClickedListener;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }


    public void setStyle(ChannelListStyle channelListStyle) {
        this.channelListStyle = channelListStyle;
    }

    public void setOnChannelsLoadedListener(ChannelList.OnChannelsLoadedListener listener) {
        this.onChannelsLoadedListener = listener;
    }

    public void setAvatarImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    @Override
    public ChannelAdapter.ChannelViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_channel_list, parent, false);
        ImageView avatar = v.findViewById(R.id.iv_avatar);
        TextViewFont nameText = v.findViewById(R.id.tv_title);
        TextViewFont timeText = v.findViewById(R.id.tv_time);
        TextViewFont lastMessageText = v.findViewById(R.id.tv_last_message);
        TextViewFont unreadMessageText = v.findViewById(R.id.tv_unread_message);
        TextViewFont productName = v.findViewById(R.id.tv_product_name);

        avatar.getLayoutParams().width = channelListStyle.getAvatarWidth();
        avatar.getLayoutParams().height = channelListStyle.getAvatarHeight();

        nameText.setTextColor(channelListStyle.getNameTextColor());
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, channelListStyle.getNameTextSize());
        nameText.setTypeface(nameText.getTypeface(), channelListStyle.getNameTextStyle());
        if (!TextUtils.isEmpty(channelListStyle.getUsernameCustomFont())) {
            nameText.setCustomFont(channelListStyle.getUsernameCustomFont());
        }

        timeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, channelListStyle.getLastSeenTextSize());
        timeText.setTextColor(channelListStyle.getLastSeenTextColor());
        timeText.setTypeface(timeText.getTypeface(), channelListStyle.getLastSeenTextStyle());
        if (!TextUtils.isEmpty(channelListStyle.getCustomFont())) {
            timeText.setCustomFont(channelListStyle.getCustomFont());
        }

        lastMessageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, channelListStyle.getLastMessageTextSize());
        lastMessageText.setTextColor(channelListStyle.getLastMessageTextColor());
        lastMessageText.setTypeface(lastMessageText.getTypeface(), channelListStyle.getLastMessageTextStyle());

        if (!TextUtils.isEmpty(channelListStyle.getCustomFont())) {
            lastMessageText.setCustomFont(channelListStyle.getCustomFont());
        }
        Drawable unreadMessageTextBackground = unreadMessageText.getBackground();
        unreadMessageTextBackground.mutate().setColorFilter(channelListStyle.getUnreadMessageCountBackgroundColor(), PorterDuff.Mode.SRC_IN);

        unreadMessageText.setBackground(unreadMessageTextBackground);

        //unreadMessageText.setBackgroundColor(channelListStyle.getUnreadMessageCountBackgroundColor());
        unreadMessageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, channelListStyle.getUnreadMessageCountTextSize());
        unreadMessageText.setTextColor(channelListStyle.getUnreadMessageCountTextColor());
        unreadMessageText.setTypeface(unreadMessageText.getTypeface(), channelListStyle.getUnreadMessageCountTextStyle());
        if (!TextUtils.isEmpty(channelListStyle.getCustomFont())) {
            unreadMessageText.setCustomFont(channelListStyle.getCustomFont());
        }

        if (!TextUtils.isEmpty(channelListStyle.getCustomFont())) {
            productName.setCustomFont(channelListStyle.getCustomFont());
        }

        ChannelViewHolder vh = new ChannelViewHolder(v, channelClickedListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(ChannelViewHolder holder, int position) {
        holder.bind(dataset.get(position));

    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public class ChannelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ChannelClickedListener channelClickedListener;
        TextView titleTv;
        AvatarView avatarIv;
        TextView timeTv;
        TextView lastMessageTv;
        TextView unreadMessageTv;
        TextView productNameTv;

        public ChannelViewHolder(View v, ChannelClickedListener listener) {
            super(v);
            titleTv = v.findViewById(R.id.tv_title);
            avatarIv = v.findViewById(R.id.iv_avatar);
            timeTv = v.findViewById(R.id.tv_time);
            lastMessageTv = v.findViewById(R.id.tv_last_message);
            unreadMessageTv = v.findViewById(R.id.tv_unread_message);
            productNameTv = v.findViewById(R.id.tv_product_name);
            channelClickedListener = listener;
        }

        public void bind(DbBaseChannelWrapper baseChannel) {
            itemView.setTag(baseChannel);
            itemView.setOnClickListener(this);
            if (baseChannel instanceof DbGroupWrapper && ((DbGroupWrapper) baseChannel).getLastMessage() != null) {
                DbGroupWrapper groupChannel = (DbGroupWrapper) baseChannel;
                timeFormat.setTime(timeTv, groupChannel.getLastMessage().getInsertedAt() * 1000);
                DbMessageWrapper message = groupChannel.getLastMessage();

                String lastMessage = "";
                if (message.getType().equals("attachment")) {
                    if (message.getAttachment().getType().contains("image")) {
                        lastMessage = "Image";
                    } else if (message.getAttachment().getType().contains("video")) {
                        lastMessage = "Video";
                    } else if (message.getAttachment().getType().contains("application") || message.getAttachment().getType().contains("css") ||
                            message.getAttachment().getType().contains("csv") || message.getAttachment().getType().contains("text")) {
                        lastMessage = "Document";
                    }
                } else if (message.getType().equals("text")) {
                    lastMessage = message.getText();
                } else {
                    lastMessage = "New Message";
                }
                lastMessageTv.setText(message.getUser().getDisplayName() + " : " + lastMessage);
            } else {
                lastMessageTv.setText("");
                timeTv.setText("");
            }
            if (baseChannel instanceof DbGroupWrapper && ((DbGroupWrapper) baseChannel).getUnreadMessageCount() > 0) {
                unreadMessageTv.setVisibility(VISIBLE);
                unreadMessageTv.setText(String.valueOf(((DbGroupWrapper) baseChannel).getUnreadMessageCount()));
            } else {
                unreadMessageTv.setVisibility(View.GONE);
            }
            if (baseChannel instanceof DbGroupWrapper
                    && ((DbGroupWrapper) baseChannel).getParticipantsCount() == 2 && !channelListStyle.isAlwaysShowChannelName()) {
                DbGroupWrapper groupChannel = (DbGroupWrapper) baseChannel;
                if (groupChannel != null && groupChannel.getParticipants() != null && groupChannel.getParticipants().size() > 0) {
                    List<Participant> participants = groupChannel.getParticipants();
                    for (Participant participant : participants) {
                        if (!participant.getId().equals(ChatCamp.getCurrentUser().getId())) {
                            populateTitle(participant.getAvatarUrl(), participant.getDisplayName());
                        }
                    }
                }
            } else {
                populateTitle(baseChannel.getAvatarUrl(), baseChannel.getName());
            }
            if (baseChannel.getMetadata() != null && baseChannel.getMetadata().get(KEY_PRODUCT_NAME) != null &&
                    !TextUtils.isEmpty((CharSequence) baseChannel.getMetadata().get(KEY_PRODUCT_NAME))) {
                productNameTv.setText((CharSequence) baseChannel.getMetadata().get(KEY_PRODUCT_NAME));
                productNameTv.setVisibility(VISIBLE);
            } else {
                productNameTv.setVisibility(View.GONE);
            }

        }

        private void populateTitle(String imageUrl, String title) {
            if (imageLoader != null) {
                imageLoader.loadImage(avatarIv, imageUrl);
            } else {
                avatarIv.initView(imageUrl, title);
            }

            titleTv.setText(title);

        }

        @Override
        public void onClick(View view) {
            if (channelClickedListener != null) {
                channelClickedListener.onClick((DbBaseChannelWrapper) view.getTag()); // call the onClick in the OnItemClickListener
            }
        }
    }

//    @Override
//    public void onLoadMore(int page, int total) {
//        if (loadingView != null) {
//            loadingView.setVisibility(VISIBLE);
//        }
////        loadChannels();
//    }

    public interface ChannelComparator extends Comparator<DbGroupWrapper> {

    }
}
