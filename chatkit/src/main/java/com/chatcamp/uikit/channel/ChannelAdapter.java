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

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.ImageLoader;
import com.chatcamp.uikit.database.ChatCampDatabaseHelper;
import com.chatcamp.uikit.utils.DefaultTimeFormat;
import com.chatcamp.uikit.utils.TimeFormat;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.GroupChannelListQuery;
import io.chatcamp.sdk.Message;
import io.chatcamp.sdk.OpenChannel;
import io.chatcamp.sdk.OpenChannelListQuery;
import io.chatcamp.sdk.Participant;

import static android.view.View.VISIBLE;
import static android.view.View.resolveSize;

/**
 * Created by shubhamdhabhai on 18/05/18.
 */

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private static final String CHANNEL_LISTENER = "channel_list_channel_listener";

    public interface ChannelClickedListener {
        void onClick(BaseChannel baseChannel);
    }

    private List<BaseChannel> dataset;
    private ChannelClickedListener channelClickedListener;
    private Context context;
    private ChannelListStyle channelListStyle;
    private ImageLoader imageLoader;
    private ChatCampDatabaseHelper chatCampDatabaseHelper;
    private TimeFormat timeFormat;
    private GroupChannelListQuery.ParticipantState participantState;
    private BaseChannel.ChannelType channelType;

    public ChannelAdapter(Context context) {
        dataset = new ArrayList<>();
        this.context = context;
        chatCampDatabaseHelper = new ChatCampDatabaseHelper(context);
        timeFormat = new DefaultTimeFormat();

    }

    public void clear() {
        dataset.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<BaseChannel> data) {
        dataset = data;
        notifyDataSetChanged();
    }

    public void onWindowVisibilityChanged(int visibility) {
        if(visibility == VISIBLE) {
            addChannelListener();
        } else {
            removeChannelListener();
        }

    }

    private void addChannelListener() {
        ChatCamp.addChannelListener(CHANNEL_LISTENER, new ChatCamp.ChannelListener() {
            @Override
            public void onOpenChannelMessageReceived(OpenChannel openChannel, Message message) {

            }

            @Override
            public void onGroupChannelMessageReceived(GroupChannel groupChannel, Message message) {

            }

            @Override
            public void onGroupChannelUpdated(GroupChannel groupChannel) {
                if(channelType == null) {
                    return;
                }
                if (channelType == BaseChannel.ChannelType.GROUP) {
                    for (int i = 0; i < dataset.size(); ++i) {
                        if (dataset.get(i).getId().equals(groupChannel.getId())) {
                            dataset.set(i, groupChannel);
                            chatCampDatabaseHelper.addGroupChannel(groupChannel);
                            notifyItemChanged(i);
                        }
                    }
                }
            }

            @Override
            public void onGroupChannelTypingStatusChanged(GroupChannel groupChannel) {

            }

            @Override
            public void onOpenChannelTypingStatusChanged(OpenChannel openChannel) {

            }

            @Override
            public void onGroupChannelReadStatusUpdated(GroupChannel groupChannel) {

            }

            @Override
            public void onOpenChannelReadStatusUpdated(OpenChannel openChannel) {

            }
        });
    }

    private void removeChannelListener() {
        ChatCamp.removeChannelListener(CHANNEL_LISTENER);
    }

    public void setChannelClickedListener(ChannelClickedListener channelClickedListener) {
        this.channelClickedListener = channelClickedListener;
    }

    public void setChannelType(BaseChannel.ChannelType channelType, GroupChannelListQuery.ParticipantState participantState) {
        setChannelType(channelType, participantState, null);
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public void setChannelType(final BaseChannel.ChannelType channelType, final GroupChannelListQuery.ParticipantState participantState,
                               ChannelComparator comparator) {
        this.participantState = participantState;
        this.channelType = channelType;

        if (comparator == null) {
            comparator = new LastMessageChannelComparator();
        }
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
            final List<GroupChannel> groupChannels = chatCampDatabaseHelper.getGroupChannels(participantState);
            Collections.sort(groupChannels, comparator);
            dataset.clear();
            dataset.addAll(groupChannels);
            notifyDataSetChanged();
            GroupChannelListQuery groupChannelListQuery = GroupChannel.createGroupChannelListQuery();
            groupChannelListQuery.setParticipantState(participantState);
            final ChannelComparator finalComparator = comparator;
            groupChannelListQuery.get(new GroupChannelListQuery.ResultHandler() {
                @Override
                public void onResult(List<GroupChannel> groupChannelList, ChatCampException e) {
                    dataset.clear();
                    if (finalComparator != null) {
                        Collections.sort(groupChannelList, finalComparator);
                    }
                    dataset.addAll(groupChannelList);
                    chatCampDatabaseHelper.addGroupChannels(groupChannelList, participantState);
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
            if (baseChannel instanceof GroupChannel && ((GroupChannel) baseChannel).getLastMessage() != null) {
                GroupChannel groupChannel = (GroupChannel) baseChannel;
                timeFormat.setTime(timeTv, groupChannel.getLastMessage().getInsertedAt() * 1000);
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
                unreadMessageTv.setVisibility(VISIBLE);
                unreadMessageTv.setText(String.valueOf(((GroupChannel) baseChannel).getUnreadMessageCount()));
            } else {
                unreadMessageTv.setVisibility(View.GONE);
            }
            if (baseChannel instanceof GroupChannel
                    && ((GroupChannel) baseChannel).getParticipantsCount() <= 2
                    && ((GroupChannel) baseChannel).isDistinct()) {
                GroupChannel groupChannel = chatCampDatabaseHelper.getGroupChannel(baseChannel.getId());
                if (groupChannel != null && groupChannel.getParticipants() != null && groupChannel.getParticipants().size() > 0) {
                    List<Participant> participants = groupChannel.getParticipants();
                    for (Participant participant : participants) {
                        //TODO check if the chatcamp current user id is null or not, probably the user is not set when the app crashes and try to reconnect.
                        if (!participant.getId().equals(ChatCamp.getCurrentUser().getId())) {
                            populateTitle(participant.getAvatarUrl(), participant.getDisplayName());
                        }
                    }
                } else {
                    GroupChannel.get(baseChannel.getId(), new GroupChannel.GetListener() {
                        @Override
                        public void onResult(GroupChannel groupChannel, ChatCampException e) {
                            chatCampDatabaseHelper.addGroupChannel(groupChannel);
                            List<Participant> participants = groupChannel.getParticipants();
                            for (Participant participant : participants) {
                                if (!participant.getId().equals(ChatCamp.getCurrentUser().getId())) {
                                    populateTitle(participant.getAvatarUrl(), participant.getDisplayName());
                                }
                            }
                        }
                    });
                }

            } else {
                populateTitle(baseChannel.getAvatarUrl(), baseChannel.getName());
            }

        }

        private void populateTitle(String imageUrl, String title) {
            if (imageLoader != null) {
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

    public interface ChannelComparator extends Comparator<GroupChannel> {

    }
}
