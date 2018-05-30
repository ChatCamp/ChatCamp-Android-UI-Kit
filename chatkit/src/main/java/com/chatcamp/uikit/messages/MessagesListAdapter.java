
package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.ImageLoader;
import com.chatcamp.uikit.messages.database.ChatCampDatabaseHelper;
import com.chatcamp.uikit.messages.messagetypes.MessageFactory;
import com.chatcamp.uikit.messages.typing.TypingFactory;
import com.chatcamp.uikit.utils.CircleTransform;
import com.chatcamp.uikit.utils.DateFormatter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Message;
import io.chatcamp.sdk.OpenChannel;
import io.chatcamp.sdk.Participant;
import io.chatcamp.sdk.PreviousMessageListQuery;

/**
 * Adapter for {@link MessagesList}.
 */
@SuppressWarnings("WeakerAccess")
public class MessagesListAdapter
        extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder>
        implements RecyclerScrollMoreListener.OnLoadMoreListener {

    private static final int VIEW_TYPE_FOOTER = 0;
    private static final String CHANNEL_LISTENER = "channel_listener";

    private List<Message> items;

    private MessagesListStyle messagesListStyle;

    private List<MessageFactory> messageFactories = new ArrayList<>();
    private int totalViewTypes = VIEW_TYPE_FOOTER;

    private Map<Integer, MessageType> viewTypeMessageTypeMap = new HashMap<>();
    private Map<MessageFactory, Integer> factoryMyViewTypeMap = new HashMap<>();
    private Map<MessageFactory, Integer> factoryTheirViewTypeMap = new HashMap<>();

    private Map<String, Cluster> messageIdClusterMap = new HashMap<>();

    private boolean isTyping;
    private List<Participant> typingParticipantList;
    private int footerPosition = 0;

    private BaseChannel channel;

    private long lastReadTime;
    private Handler mUiThreadHandler;

    private TypingFactory typingFactory;

    private RecyclerView recyclerView;

    PreviousMessageListQuery previousMessageListQuery;
    private ChatCampDatabaseHelper databaseHelper;
    private boolean loadingFirstTime = true;

    private Context context;
    private ImageLoader avatarImageLoader;

    public MessagesListAdapter(Context context) {
        items = new ArrayList<>();
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        typingParticipantList = new ArrayList<>();
        this.context = context;
        databaseHelper = new ChatCampDatabaseHelper(context);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void addMessageFactories(MessageFactory... messageFactories) {
        for (MessageFactory messageFactory : messageFactories) {
            messageFactory.setMessageStyle(messagesListStyle);

            this.messageFactories.add(messageFactory);
            totalViewTypes++;

            MessageType myMessageType = new MessageType(true, messageFactory);
            viewTypeMessageTypeMap.put(totalViewTypes, myMessageType);
            factoryMyViewTypeMap.put(messageFactory, totalViewTypes);

            totalViewTypes++;
            MessageType theirMessageType = new MessageType(false, messageFactory);
            viewTypeMessageTypeMap.put(totalViewTypes, theirMessageType);
            factoryTheirViewTypeMap.put(messageFactory, totalViewTypes);
        }
    }

    public void addTypingFactory(TypingFactory typingFactory) {
        this.typingFactory = typingFactory;
    }

    public void setMessagesListStyle(MessagesListStyle messagesListStyle) {
        this.messagesListStyle = messagesListStyle;
    }

    public void setChannel(final BaseChannel channel) {
        this.channel = channel;
        //TODO get the number of message from client
        loadMessages();
        addChannelListener();
    }

    public void setAvatarImageLoader(ImageLoader imageLoader) {
        this.avatarImageLoader = imageLoader;
    }

    //TODO We can create a layout for showing loading indicator for pagination.
    private void loadMessages() {
        if (previousMessageListQuery == null) {
            items = databaseHelper.getMessages(channel.getId(), channel.isGroupChannel()
                    ? BaseChannel.ChannelType.GROUP : BaseChannel.ChannelType.OPEN);
            previousMessageListQuery = channel.createPreviousMessageListQuery();
            notifyDataSetChanged();
        }
        previousMessageListQuery.load(20, true, new PreviousMessageListQuery.ResultListener() {
            @Override
            public void onResult(List<Message> list, ChatCampException e) {
                if (loadingFirstTime) {
                    items.clear();
                    items.addAll(list);
                    databaseHelper.addMessages(list, channel.getId(), channel.isGroupChannel()
                            ? BaseChannel.ChannelType.GROUP : BaseChannel.ChannelType.OPEN);
                    loadingFirstTime = false;
                } else {
                    items.addAll(list);
                }
                if (channel instanceof GroupChannel) {
                    //TODO should open channel also have something for mark as read?
                    ((GroupChannel) channel).markAsRead();

                }
                notifyDataSetChanged();
            }
        });
    }

    private void addChannelListener() {
        ChatCamp.addChannelListener(CHANNEL_LISTENER, new ChatCamp.ChannelListener() {
            @Override
            public void onOpenChannelMessageReceived(OpenChannel openChannel, Message message) {

            }

            @Override
            public void onGroupChannelMessageReceived(GroupChannel groupChannel, Message message) {
                if (groupChannel.getId().equals(channel.getId())) {
                    items.add(0, message);
                    databaseHelper.addMessage(message, channel.getId(), channel.isGroupChannel()
                            ? BaseChannel.ChannelType.GROUP : BaseChannel.ChannelType.OPEN);
                    if (channel instanceof GroupChannel) {
                        //TODO should open channel also have something for mark as read?
                        if (lastReadTime < message.getInsertedAt() * 1000) {
                            ((GroupChannel) channel).markAsRead();
                        }
                    }
                    if (isTyping) {
                        notifyItemInserted(1);
                    } else {
                        notifyItemInserted(0);
                    }
                    restoreScrollPositionAfterAdAdded();
                }
            }

            @Override
            public void onGroupChannelTypingStatusChanged(GroupChannel groupChannel) {
                //TODO use same method for open and group channel
                List<Participant> typingParticipants = groupChannel.getTypingParticipants();
                boolean isTyping;
                if (typingParticipants.size() > 0 && !isCurrentUserTyping(typingParticipants)) {
                    isTyping = true;
                    typingParticipantList = typingParticipants;
                } else {
                    isTyping = false;
                    typingParticipantList.clear();
                }
                if (isTyping ^ MessagesListAdapter.this.isTyping) {
                    if (isTyping) {
                        notifyItemInserted(footerPosition);
                    } else {
                        notifyItemRemoved(footerPosition);
                    }
                } else {
                    notifyItemChanged(footerPosition);
                }
                MessagesListAdapter.this.isTyping = isTyping;
                restoreScrollPositionAfterAdAdded();
            }

            @Override
            public void onOpenChannelTypingStatusChanged(OpenChannel openChannel) {

            }

            @Override
            public void onGroupChannelReadStatusUpdated(GroupChannel groupChannel) {
                Map<String, Long> readReceipt = groupChannel.getReadReceipt();
                if (readReceipt.size() == groupChannel.getParticipants().size()) {
                    Long lastRead = 0L;
                    for (Map.Entry<String, Long> entry : readReceipt.entrySet()) {
                        if (lastRead == 0L || entry.getValue() < lastRead) {
                            lastRead = entry.getValue();
                        }
                    }
                    lastReadTime = lastRead * 1000;
                    //TODO need to optimise this
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onOpenChannelReadStatusUpdated(OpenChannel openChannel) {

            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            TypingViewHolder typingViewHolder = new TypingViewHolder(layoutInflater.inflate(TypingViewHolder.RESOURCE_ID_FOOTER, parent, false));
            typingViewHolder.typingHolder = typingFactory.createView(typingViewHolder.vgContainer, layoutInflater);
            return typingViewHolder;
//            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(ViewHolder.RESOURCE_ID_FOOTER, parent, false));
        } else {
            MessageType messageType = viewTypeMessageTypeMap.get(viewType);
            int resId = messageType.isMe ? MessageViewHolder.RESOURCE_ID_MY : MessageViewHolder.RESOURCE_ID_THEIR;
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            MessageViewHolder viewHolder = new MessageViewHolder(layoutInflater.inflate(resId, parent, false));
            MessageFactory.MessageSpecs messageSpecs = new MessageFactory.MessageSpecs();
            messageType.messageFactory.setMessageSpecs(messageSpecs);
            viewHolder.messageHolder = messageType.messageFactory.createMessageHolder(viewHolder.messageContentContainer, messageType.isMe, layoutInflater);
            viewHolder.messageSpecs = messageSpecs;
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isTyping && position == footerPosition) {
            if (holder instanceof TypingViewHolder) {
                bindTypingViewHolder((TypingViewHolder) holder);
            }
        } else {
            if (holder instanceof MessageViewHolder) {
                bindMessageViewHolder((MessageViewHolder) holder, position);
            }
        }
    }

    private void bindTypingViewHolder(TypingViewHolder typingViewHolder) {
        if (typingFactory != null) {
            typingFactory.bindView(typingViewHolder.typingHolder, typingParticipantList);
        }
    }

    private void bindMessageViewHolder(MessageViewHolder holder, int position) {
        Message message = getItem(position);
        holder.message = message;
        MessageType messageType = viewTypeMessageTypeMap.get(holder.getItemViewType());
        MessageFactory.MessageHolder messageHolder = holder.messageHolder;
        holder.messageSpecs.isMe = messageType.isMe;
        holder.messageSpecs.position = position;
        // TODO for receipt we can pass a layout and use it, also add other custom attrs here
        // read receipt
        {
            boolean readReceiptVisibility = messageType.isMe ? messagesListStyle.isShowOutcomingReadReceipt() : messagesListStyle.isShowIncomingReadReceipt();
            if (readReceiptVisibility) {
                holder.readReceiptContainer.setVisibility(View.VISIBLE);
                holder.readReceiptContainer.removeAllViews();
                if (message.getInsertedAt() * 1000 > lastReadTime) {
                    // message is not read by everyone
                    int layoutRes = messagesListStyle.getReadReceiptUnReadLayout();
                    holder.readReceiptContainer.addView(LayoutInflater.from(context).inflate(layoutRes, holder.readReceiptContainer, false));
                } else {
                    int layoutRes = messagesListStyle.getReadReceiptReadLayout();
                    holder.readReceiptContainer.addView(LayoutInflater.from(context).inflate(layoutRes, holder.readReceiptContainer, false));
                }
            } else {
                holder.readReceiptContainer.removeAllViews();
                holder.readReceiptContainer.setVisibility(View.GONE);
            }
        }
        Cluster cluster = getClustering(message, position);

        // date header
        {
            if (cluster.dateBoundaryWithNext) {
                if (messagesListStyle.isShowDateHeader()) {
                    int dateHeaderPadding = messagesListStyle.getDateHeaderPadding();
                    int dateHeaderTextColor = messagesListStyle.getDateHeaderTextColor();
                    int dateHeaderTextSize = messagesListStyle.getDateHeaderTextSize();
                    int dateHeaderTextStyle = messagesListStyle.getDateHeaderTextStyle();
                    holder.messageDateHeader.setPadding(dateHeaderPadding, dateHeaderPadding, dateHeaderPadding, dateHeaderPadding);
                    holder.messageDateHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateHeaderTextSize);
                    holder.messageDateHeader.setTypeface(holder.messageDateHeader.getTypeface(), dateHeaderTextStyle);
                    holder.messageDateHeader.setTextColor(dateHeaderTextColor);
                    holder.messageDateHeader.setVisibility(View.VISIBLE);
                    bindDateTimeForMessage(holder, message);
                } else {
                    holder.messageDateHeader.setVisibility(View.GONE);
                }
            } else {
                holder.messageDateHeader.setVisibility(View.GONE);
            }
        }

        // time
        {
            boolean timeVisibility = messageType.isMe ? messagesListStyle.isShowOutcomingTimeText() : messagesListStyle.isShowIncomingTimeText();
            String timeFormat = messageType.isMe ? messagesListStyle.getOutcomingTimeTextFormat() : messagesListStyle.getIncomingTimeTextFormat();
            if (timeVisibility) {
                holder.messageTime.setVisibility(View.VISIBLE);
                int timeTextColor = messageType.isMe ? messagesListStyle.getOutcomingTimeTextColor() : messagesListStyle.getIncomingTimeTextColor();
                int timeTextSize = messageType.isMe ? messagesListStyle.getOutcomingTimeTextSize() : messagesListStyle.getIncomingTimeTextSize();
                int timeTextStyle = messageType.isMe ? messagesListStyle.getOutcomingTimeTextStyle() : messagesListStyle.getIncomingTimeTextStyle();
                int timeTextPaddingLeft = messageType.isMe ? messagesListStyle.getOutcomingTimeTextPaddingLeft() : messagesListStyle.getIncomingTimeTextPaddingLeft();
                int timeTextPaddingRight = messageType.isMe ? messagesListStyle.getOutcomingTimeTextPaddingRight() : messagesListStyle.getIncomingTimeTextPaddingRight();
                int timeTextPaddingTop = messageType.isMe ? messagesListStyle.getOutcomingTimeTextPaddingTop() : messagesListStyle.getIncomingTimeTextPaddingTop();
                int timeTextPaddingBottom = messageType.isMe ? messagesListStyle.getOutcomingTimeTextPaddingBottom() : messagesListStyle.getIncomingTimeTextPaddingBottom();
                holder.messageTime.setPadding(timeTextPaddingLeft, timeTextPaddingTop, timeTextPaddingRight, timeTextPaddingBottom);
                holder.messageTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeTextSize);
                holder.messageTime.setTypeface(holder.messageTime.getTypeface(), timeTextStyle);
                holder.messageTime.setTextColor(timeTextColor);
                Date date = new Date();
                date.setTime(message.getInsertedAt() * 1000);
                if (TextUtils.isEmpty(timeFormat)) {
                    holder.messageTime.setText(DateFormatter.format(date, DateFormatter.Template.TIME));
                } else {
                    holder.messageTime.setText(DateFormatter.format(date, timeFormat));
                }
            } else {
                holder.messageTime.setVisibility(View.GONE);
            }
        }


        //username

        boolean usernameVisibility = messageType.isMe ? messagesListStyle.isShowOutcomingUsername() : messagesListStyle.isShowIncomingUsername();
        if (usernameVisibility) {
            holder.messageUsername.setVisibility(View.VISIBLE);
            int usernameTextColor = messageType.isMe ? messagesListStyle.getOutcomingUsernameTextColor() : messagesListStyle.getIncomingUsernameTextColor();
            int usernameTextSize = messageType.isMe ? messagesListStyle.getOutcomingUsernameTextSize() : messagesListStyle.getIncomingUsernameTextSize();
            int usernameTextStyle = messageType.isMe ? messagesListStyle.getOutcomingUsernameTextStyle() : messagesListStyle.getIncomingUsernameTextStyle();
            int usernameTextPaddingLeft = messageType.isMe ? messagesListStyle.getOutcomingUsernameTextPaddingLeft() : messagesListStyle.getIncomingUsernameTextPaddingLeft();
            int usernameTextPaddingRight = messageType.isMe ? messagesListStyle.getOutcomingUsernameTextPaddingRight() : messagesListStyle.getIncomingUsernameTextPaddingRight();
            int usernameTextPaddingTop = messageType.isMe ? messagesListStyle.getOutcomingUsernameTextPaddingTop() : messagesListStyle.getIncomingUsernameTextPaddingTop();
            int usernameTextPaddingBottom = messageType.isMe ? messagesListStyle.getOutcomingUsernameTextPaddingBottom() : messagesListStyle.getIncomingUsernameTextPaddingBottom();
            String username = "Unknown";
            if (message.getUser() != null && !TextUtils.isEmpty(message.getUser().getDisplayName())) {
                username = message.getUser().getDisplayName();
            }
            holder.messageUsername.setPadding(usernameTextPaddingLeft, usernameTextPaddingTop, usernameTextPaddingRight, usernameTextPaddingBottom);
            holder.messageUsername.setTextSize(TypedValue.COMPLEX_UNIT_PX, usernameTextSize);
            holder.messageUsername.setTypeface(holder.messageUsername.getTypeface(), usernameTextStyle);
            holder.messageUsername.setTextColor(usernameTextColor);
            holder.messageUsername.setText(username);
        } else {
            holder.messageUsername.setVisibility(View.GONE);
        }

        //TODO use imageloader
        // avatar

        boolean avatarVisibility = messageType.isMe ? messagesListStyle.isShowOutcomingAvatar() : messagesListStyle.isShowIncomingAvatar();
        if (avatarVisibility) {
            holder.messageUserAvatar.setVisibility(View.VISIBLE);
            int avatarHeight = messageType.isMe ? messagesListStyle.getOutcomingAvatarHeight() : messagesListStyle.getIncomingAvatarHeight();
            int avatarWidth = messageType.isMe ? messagesListStyle.getOutcomingAvatarWidth() : messagesListStyle.getIncomingAvatarWidth();
            int avatarMarginLeft = messageType.isMe ? messagesListStyle.getOutcomingAvatarMarginLeft() : messagesListStyle.getIncomingAvatarMarginLeft();
            int avatarMarginRight = messageType.isMe ? messagesListStyle.getOutcomingAvatarMarginRight() : messagesListStyle.getIncomingAvatarMarginRight();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageUserAvatar.getLayoutParams();
            params.width = avatarWidth;
            params.height = avatarHeight;
            params.setMargins(avatarMarginLeft, 0, avatarMarginRight, 0);
            if (avatarImageLoader != null) {
                avatarImageLoader.loadImage(holder.messageUserAvatar, message.getUser().getAvatarUrl());
            } else {
                Picasso.with(context).load(message.getUser().getAvatarUrl())
                        .placeholder(R.drawable.icon_default_contact)
                        .transform(new CircleTransform()).into(holder.messageUserAvatar);
            }
        } else {
            holder.messageUserAvatar.setVisibility(View.GONE);
        }

        //container layout
        {
            RelativeLayout.LayoutParams messageContainerParams = (RelativeLayout.LayoutParams) holder.messageContainer.getLayoutParams();
            int messageContainerMarginRight = messagesListStyle.isShowOutcomingAvatar() ? messagesListStyle.getOutcomingAvatarWidth()
                    + messagesListStyle.getOutcomingAvatarMarginLeft()
                    + messagesListStyle.getOutcomingAvatarMarginRight()
                    : messagesListStyle.getRightMargin();
            int messageContainerMarginLeft = messagesListStyle.isShowIncomingAvatar() ? messagesListStyle.getIncomingAvatarWidth()
                    + messagesListStyle.getIncomingAvatarMarginLeft()
                    + messagesListStyle.getIncomingAvatarMarginRight() : messagesListStyle.getLeftMargin();
            messageContainerParams.setMargins(messageContainerMarginLeft, 0, messageContainerMarginRight, 0);
            holder.messageContainer.setLayoutParams(messageContainerParams);
        }

        // Cluster messages
        {
            if (cluster.clusterWithNext && !cluster.dateBoundaryWithNext) {
                // dont show avatar and name
                holder.messageUserAvatar.setVisibility(View.GONE);
                holder.messageUsername.setVisibility(View.GONE);
                holder.messageSpecs.isFirstMessage = false;
            } else {
                //show both avatar and name
                //TODO use imageloader to load the image
                if (avatarVisibility) {
                    holder.messageUserAvatar.setVisibility(View.VISIBLE);
                } else {
                    holder.messageUserAvatar.setVisibility(View.GONE);
                }
                if (usernameVisibility) {
                    holder.messageUsername.setVisibility(View.VISIBLE);
                } else {
                    holder.messageUsername.setVisibility(View.GONE);
                }
                holder.messageSpecs.isFirstMessage = true;
            }
        }
        messageType.messageFactory.setMessageSpecs(holder.messageSpecs);
        messageType.messageFactory.bindMessageHolder(messageHolder, message);
    }

    private void bindDateTimeForMessage(MessageViewHolder holder, Message message) {
        String dateString = "";
        Long time = message.getInsertedAt() * 1000;
        Date date = new Date();
        date.setTime(time);
        if (DateFormatter.isToday(date)) {
            // TODO Extract to string resources
            dateString = "Today";
        } else if (DateFormatter.isYesterday(date)) {
            dateString = "Yesterday";
        } else {
            dateString = DateFormatter.format(date, "MM-dd-yyyy");
        }
        holder.messageDateHeader.setText(dateString);
        holder.messageDateHeader.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return isTyping ? items.size() + 1 : items.size();
    }

    @Override
    public void onLoadMore(int page, int total) {
        loadMessages();
    }

    private Message getItem(int position) {
        if (isTyping && position == footerPosition) {
            return null;
        } else if (isTyping) {
            return items.get(position - 1);
        }
        return items.get(position);
    }


    @Override
    public int getItemViewType(int position) {
        if (isTyping && position == footerPosition) {
            return VIEW_TYPE_FOOTER;
        }
        Message message = getItem(position);
        boolean isMe = message.getUser().getId().equals(ChatCamp.getCurrentUser().getId());
        for (MessageFactory messageFactory : messageFactories) {
            if (messageFactory.isBindable(message)) {
                return isMe ? factoryMyViewTypeMap.get(messageFactory) : factoryTheirViewTypeMap.get(messageFactory);
            }
        }
        return -1;
    }

    private Cluster getClustering(Message message, int position) {
        Cluster result = messageIdClusterMap.get(message.getId());
        if (result == null) {
            result = new Cluster();
            messageIdClusterMap.put(message.getId(), result);
        }

        int previousPosition = position - 1;
        Message previousMessage = (previousPosition >= 0) ? getItem(previousPosition) : null;
        if (previousMessage != null) {
            result.dateBoundaryWithPrevious = isDateBoundary(previousMessage.getInsertedAt(), message.getInsertedAt());
            result.clusterWithPrevious = !isNewUser(previousMessage, message);

            Cluster previousCluster = messageIdClusterMap.get(previousMessage.getId());
            if (previousCluster == null) {
                previousCluster = new Cluster();
                messageIdClusterMap.put(previousMessage.getId(), previousCluster);
            } else {
                // does the previous need to change its clustering?
                if ((previousCluster.clusterWithNext != result.clusterWithPrevious) ||
                        (previousCluster.dateBoundaryWithNext != result.dateBoundaryWithPrevious)) {
                    requestUpdate(previousPosition);
                }
            }
            previousCluster.clusterWithNext = result.clusterWithPrevious;
            previousCluster.dateBoundaryWithNext = result.dateBoundaryWithPrevious;
        }

        int nextPosition = position + 1;
        Message nextMessage = (nextPosition < getItemCount()) ? getItem(nextPosition) : null;
        if (nextMessage != null) {
            result.dateBoundaryWithNext = isDateBoundary(message.getInsertedAt(), nextMessage.getInsertedAt());
            result.clusterWithNext = !isNewUser(message, nextMessage);

            Cluster nextCluster = messageIdClusterMap.get(nextMessage.getId());
            if (nextCluster == null) {
                nextCluster = new Cluster();
                messageIdClusterMap.put(nextMessage.getId(), nextCluster);
            } else {
                // does the next need to change its clustering?
                if ((nextCluster.clusterWithPrevious != result.clusterWithNext) ||
                        (nextCluster.dateBoundaryWithPrevious != result.dateBoundaryWithNext)) {
                    requestUpdate(nextPosition);
                }
            }
            nextCluster.clusterWithPrevious = result.clusterWithNext;
            nextCluster.dateBoundaryWithPrevious = result.dateBoundaryWithNext;
        } else {
            result.dateBoundaryWithNext = true;
        }

        return result;
    }

    private void requestUpdate(final int lastPosition) {
        mUiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(lastPosition);
            }
        });
    }


    private static boolean isDateBoundary(long t1, long t2) {
        Date d1 = new Date();
        d1.setTime(t1 * 1000);
        Date d2 = new Date();
        d2.setTime(t2 * 1000);
        return (d1.getYear() != d2.getYear()) || (d1.getMonth() != d2.getMonth()) || (d1.getDay() != d2.getDay());
    }


    private static class Cluster {
        public boolean dateBoundaryWithPrevious;
        public boolean clusterWithPrevious;

        public boolean dateBoundaryWithNext;
        public boolean clusterWithNext;
    }


    public boolean isNewUser(Message older, Message newer) {
        if (!older.getUser().getId().equals(newer.getUser().getId())) {
            return true;
        }
        return false;
    }

    private boolean isCurrentUserTyping(List<Participant> participants) {
        return (participants.size() == 1 && participants.get(0).getId().equals(ChatCamp.getCurrentUser().getId()));
    }

    //TODO This function is a workaround for scrolling the list when item is inserted at the bottom, there
    // is a bug that list doesnot scroll when as item is inserted at position 0. We can change the implementation
    // and reverse the layout, right now the implementation is
    // 5                           0
    // 4                           1
    // 3    it can be changed to   2
    // 2                           3
    // 1                           4
    // 0                           5
    private void restoreScrollPositionAfterAdAdded() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager != null) {
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (firstVisibleItemPosition == 0) {
                layoutManager.scrollToPosition(0);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        for (MessageFactory messageFactory : messageFactories) {
            messageFactory.onActivityResult(requestCode, resultCode, dataFile);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        for (MessageFactory messageFactory : messageFactories) {
            messageFactory.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private static class MessageType {
        private boolean isMe;
        private MessageFactory messageFactory;

        public MessageType(boolean isMe, MessageFactory messageFactory) {
            this.isMe = isMe;
            this.messageFactory = messageFactory;
        }

        @Override
        public boolean equals(Object obj) {
            if (this.equals(obj)) return true;
            if (obj == null) return false;
            if (!(obj instanceof MessageType)) return false;
            MessageType messageType = (MessageType) obj;
            if (isMe != messageType.isMe) return false;
            return this.messageFactory.equals(messageType.messageFactory);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public static final int RESOURCE_ID_FOOTER = R.layout.layout_message_footer;

        protected ViewGroup vgContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            vgContainer = itemView.findViewById(R.id.messageContentContainer);
        }
    }

    public static class TypingViewHolder extends ViewHolder {

        protected TypingFactory.TypingHolder typingHolder;

        public TypingViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class MessageViewHolder extends ViewHolder {

        public static final int RESOURCE_ID_MY = R.layout.layout_message_my;
        public static final int RESOURCE_ID_THEIR = R.layout.layout_message_their;

        protected Message message;

        protected TextView messageDateHeader;
        protected ImageView messageUserAvatar;
        protected ViewGroup messageContainer;
        protected TextView messageUsername;
        protected ViewGroup messageContentContainer;
        protected ViewGroup messageTimeContainer;
        protected TextView messageTime;
//        protected ImageView messageReadReceipt;
        protected ViewGroup readReceiptContainer;

        protected MessageFactory.MessageHolder messageHolder;
        protected MessageFactory.MessageSpecs messageSpecs;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageDateHeader = itemView.findViewById(R.id.messageDateHeader);
            messageUserAvatar = itemView.findViewById(R.id.messageUserAvatar);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            messageUsername = itemView.findViewById(R.id.messageUsername);
            messageContentContainer = itemView.findViewById(R.id.messageContentContainer);
            messageTimeContainer = itemView.findViewById(R.id.messageTimeContainer);
            messageTime = itemView.findViewById(R.id.messageTime);
//            messageReadReceipt = itemView.findViewById(R.id.messageReadReceipt);
            readReceiptContainer = itemView.findViewById(R.id.readReceiptContainer);
        }
    }
}