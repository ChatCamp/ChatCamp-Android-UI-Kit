package com.chatcamp.uikit.channel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatcamp.uikit.commons.ImageLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.chatcamp.uikit.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.GroupChannelListQuery;
import io.chatcamp.sdk.OpenChannel;
import io.chatcamp.sdk.OpenChannelListQuery;
import io.chatcamp.sdk.Participant;

/**
 * Created by shubhamdhabhai on 18/05/18.
 */

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    public interface ChannelClickedListener {
        void onClick(BaseChannel baseChannel);
    }

    private List<BaseChannel> dataset;
    private ChannelClickedListener channelClickedListener;
    private Context context;
    private ChannelListStyle channelListStyle;
    private ImageLoader imageLoader;

    public ChannelAdapter(Context context) {
        dataset = new ArrayList<>();
        this.context = context;
    }

    public void clear() {
        dataset.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<BaseChannel> data) {
        dataset = data;
        notifyDataSetChanged();
    }

    public void setChannelClickedListener(ChannelClickedListener channelClickedListener) {
        this.channelClickedListener = channelClickedListener;
    }

    public void setChannelType(BaseChannel.ChannelType channelType, GroupChannelListQuery.ParticipantState participantState) {

        if (channelType == BaseChannel.ChannelType.OPEN) {
            OpenChannelListQuery openChannelListQuery = OpenChannel.createOpenChannelListQuery();
            openChannelListQuery.get(new OpenChannelListQuery.ResultHandler() {
                @Override
                public void onResult(List<OpenChannel> openChannelList, ChatCampException e) {
                    dataset.clear();
                    dataset.addAll(openChannelList);
                    notifyDataSetChanged();
                }
            });
        } else if (channelType == BaseChannel.ChannelType.GROUP) {
            GroupChannelListQuery groupChannelListQuery = GroupChannel.createGroupChannelListQuery();
            groupChannelListQuery.setParticipantState(participantState);
            groupChannelListQuery.get(new GroupChannelListQuery.ResultHandler() {
                @Override
                public void onResult(List<GroupChannel> groupChannelList, ChatCampException e) {
                    dataset.clear();
                    dataset.addAll(groupChannelList);
                    notifyDataSetChanged();
                }
            });
        }
    }

    public void setStyle(ChannelListStyle channelListStyle) {
        this.channelListStyle = channelListStyle;
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
        TextView nameText = v.findViewById(R.id.tv_title);
        TextView timeText = v.findViewById(R.id.tv_time);
        TextView lastMessageText = v.findViewById(R.id.tv_last_message);
        TextView unreadMessageText = v.findViewById(R.id.tv_unread_message);

        avatar.getLayoutParams().width = channelListStyle.getAvatarWidth();
        avatar.getLayoutParams().height = channelListStyle.getAvatarHeight();

        nameText.setTextColor(channelListStyle.getNameTextColor());
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, channelListStyle.getNameTextSize());
        nameText.setTypeface(nameText.getTypeface(), channelListStyle.getNameTextStyle());

        timeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, channelListStyle.getLastSeenTextSize());
        timeText.setTextColor(channelListStyle.getLastSeenTextColor());
        timeText.setTypeface(timeText.getTypeface(), channelListStyle.getLastSeenTextStyle());

        lastMessageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, channelListStyle.getLastMessageTextSize());
        lastMessageText.setTextColor(channelListStyle.getLastMessageTextColor());
        lastMessageText.setTypeface(lastMessageText.getTypeface(), channelListStyle.getLastMessageTextStyle());

        Drawable unreadMessageTextBackground = unreadMessageText.getBackground();
        unreadMessageTextBackground.mutate().setColorFilter(channelListStyle.getUnreadMessageCountBackgroundColor(), PorterDuff.Mode.SRC_IN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            unreadMessageText.setBackground(unreadMessageTextBackground);
        } else {
            unreadMessageText.setBackgroundDrawable(unreadMessageTextBackground);
        }
        //unreadMessageText.setBackgroundColor(channelListStyle.getUnreadMessageCountBackgroundColor());
        unreadMessageText.setTextSize(TypedValue.COMPLEX_UNIT_PX, channelListStyle.getUnreadMessageCountTextSize());
        unreadMessageText.setTextColor(channelListStyle.getUnreadMessageCountTextColor());
        unreadMessageText.setTypeface(unreadMessageText.getTypeface(), channelListStyle.getUnreadMessageCountTextStyle());

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
        ImageView avatarIv;
        TextView timeTv;
        TextView lastMessageTv;
        TextView unreadMessageTv;

        public ChannelViewHolder(View v, ChannelClickedListener listener) {
            super(v);
            titleTv = v.findViewById(R.id.tv_title);
            avatarIv = v.findViewById(R.id.iv_avatar);
            timeTv = v.findViewById(R.id.tv_time);
            lastMessageTv = v.findViewById(R.id.tv_last_message);
            unreadMessageTv = v.findViewById(R.id.tv_unread_message);
            channelClickedListener = listener;
        }

        public void bind(BaseChannel baseChannel) {
            itemView.setTag(baseChannel);
            itemView.setOnClickListener(this);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (baseChannel instanceof GroupChannel && ((GroupChannel) baseChannel).getLastMessage() != null) {
                GroupChannel groupChannel = (GroupChannel) baseChannel;
                Date date = new Date(groupChannel.getLastMessage().getInsertedAt() * 1000);
                timeTv.setText(format.format(date));
                if (groupChannel.getLastMessage().getType().equalsIgnoreCase("text")) {
                    lastMessageTv.setText(groupChannel.getLastMessage().getText());
                } else {
                    lastMessageTv.setText("");
                }
            } else {
                lastMessageTv.setText("");
                timeTv.setText("");
            }
            if (baseChannel instanceof GroupChannel && ((GroupChannel) baseChannel).getUnreadMessageCount() > 0) {
                unreadMessageTv.setVisibility(View.VISIBLE);
                unreadMessageTv.setText(String.valueOf(((GroupChannel) baseChannel).getUnreadMessageCount()));
            } else {
                unreadMessageTv.setVisibility(View.GONE);
            }
            if (baseChannel instanceof GroupChannel
                    && ((GroupChannel) baseChannel).getParticipantsCount() <= 2
                    && ((GroupChannel) baseChannel).isDistinct()) {
                GroupChannel.get(baseChannel.getId(), new GroupChannel.GetListener() {
                    @Override
                    public void onResult(GroupChannel groupChannel, ChatCampException e) {
                        List<Participant> participants = groupChannel.getParticipants();
                        for (Participant participant : participants) {
                            if (!participant.getId().equals(ChatCamp.getCurrentUser().getId())) {
                                populateTitle(participant.getAvatarUrl(), participant.getDisplayName());
                            }
                        }
                    }
                });

            } else {
                populateTitle(baseChannel.getAvatarUrl(), baseChannel.getName());
            }

        }

        private void populateTitle(String imageUrl, String title) {
            if(imageLoader != null) {
                imageLoader.loadImage(avatarIv, imageUrl);
            } else {
                Picasso.with(context).load(imageUrl)
                        .placeholder(R.drawable.icon_default_contact)
                        .error(R.drawable.icon_default_contact)
                        .into(avatarIv, new Callback() {
                            @Override
                            public void onSuccess() {
                                Bitmap imageBitmap = ((BitmapDrawable) avatarIv.getDrawable()).getBitmap();
                                RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), imageBitmap);
                                imageDrawable.setCircular(true);
                                imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                                avatarIv.setImageDrawable(imageDrawable);
                            }

                            @Override
                            public void onError() {
                                avatarIv.setImageResource(R.drawable.icon_default_contact);
                            }
                        });
            }

            titleTv.setText(title);

        }

        @Override
        public void onClick(View view) {
            if (channelClickedListener != null) {
                channelClickedListener.onClick((BaseChannel) view.getTag()); // call the onClick in the OnItemClickListener
            }
        }
    }
}
