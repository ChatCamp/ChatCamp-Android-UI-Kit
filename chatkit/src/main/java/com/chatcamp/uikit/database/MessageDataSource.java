package com.chatcamp.uikit.database;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Message;
import io.chatcamp.sdk.MessageParams;
import io.chatcamp.sdk.OpenChannel;
import io.chatcamp.sdk.Participant;
import io.chatcamp.sdk.PreviousMessageListQuery;
import io.chatcamp.sdk.TotalCountFilterParams;
import io.chatcamp.sdk.User;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class MessageDataSource {

    private BaseChannel channel;

    private static MessageDataSource instance;
    private Context context;
    private boolean loadingFirstTime;
    private PreviousMessageListQuery previousMessageListQuery;
    private long lastReadTime;
    private boolean isTyping;

    private MutableLiveData<List<DbMessageWrapper>> messageListLiveData = new MutableLiveData<>();
    private MutableLiveData<List<DbMessageWrapper>> initialMessageListLiveData = new MutableLiveData<>();
    private MutableLiveData onMessageLoadedLiveData = new MutableLiveData<>();
    private MutableLiveData<Diff<DbMessageWrapper>> diffLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> recyclerLoadingStateLiveData = new MutableLiveData<>();
    private MutableLiveData<Long> messageLastReadLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingViewLiveData = new MutableLiveData<>();

    private MutableLiveData markAsReadLiveData = new MutableLiveData();

    private MutableLiveData<Diff<TypingStatus>> typingStatusLiveData = new MutableLiveData<>();

    private MutableLiveData<ChatCamp.NetworkState> networkStateMutableLiveData = new MutableLiveData<>();


    private ChatCampDb chatCampDb;
    private List<DbMessageWrapper> messageWrapperList = new ArrayList<>();
    private List<Participant> typingParticipantList = new ArrayList<>();


    public MutableLiveData<Diff<DbMessageWrapper>> getDiffLiveData() {
        return diffLiveData;
    }

    public MutableLiveData<List<DbMessageWrapper>> getInitialMessageListLiveData() {
        return initialMessageListLiveData;
    }

    public MutableLiveData<List<DbMessageWrapper>> getMessageListLiveData() {
        return messageListLiveData;
    }

    public MutableLiveData<Boolean> getRecyclerLoadingStateLiveData() {
        return recyclerLoadingStateLiveData;
    }

    public MutableLiveData<Long> getMessageLastReadLiveData() {
        return messageLastReadLiveData;
    }

    public MutableLiveData<Boolean> getLoadingViewLiveData() {
        return loadingViewLiveData;
    }

    public MutableLiveData getOnMessageLoadedLiveData() {
        return onMessageLoadedLiveData;
    }

    public MutableLiveData<Diff<TypingStatus>> getTypingStatusLiveData() {
        return typingStatusLiveData;
    }

    public MutableLiveData getMarkAsReadLiveData() {
        return markAsReadLiveData;
    }

    public MutableLiveData<ChatCamp.NetworkState> getNetworkStateMutableLiveData() {
        return networkStateMutableLiveData;
    }

    public static MessageDataSource getInstance(Context context, BaseChannel channel) {
        if (instance == null) {
            synchronized (MessageDataSource.class) {
                if (instance == null) {
                    instance = new MessageDataSource();

                }
            }
        }
        instance.init(context, channel);
        return instance;
    }

    private MessageDataSource() {
    }

    private void init(Context context, final BaseChannel channel) {
        this.context = context;
        chatCampDb = ChatCampDb.getInstance(context);
        this.channel = channel;
        loadingFirstTime = true;
        previousMessageListQuery = channel.createPreviousMessageListQuery();
        messageWrapperList = new ArrayList<>();
        typingParticipantList = new ArrayList<>();

        messageListLiveData = new MutableLiveData<>();

        initialMessageListLiveData = new MutableLiveData<>();
        onMessageLoadedLiveData = new MutableLiveData<>();
        diffLiveData = new MutableLiveData<>();

        recyclerLoadingStateLiveData = new MutableLiveData<>();

        messageLastReadLiveData = new MutableLiveData<>();
        loadingViewLiveData = new MutableLiveData<>();
        typingStatusLiveData = new MutableLiveData<>();
        markAsReadLiveData = new MutableLiveData();
        networkStateMutableLiveData = new MutableLiveData<>();

        ChatCamp.addConnectionListener("message_data_source", new ChatCamp.ConnectionListener() {
            @Override
            public void onConnectionChanged(ChatCamp.NetworkState networkState) {
                networkStateMutableLiveData.postValue(networkState);
                if (networkState == ChatCamp.NetworkState.CONNECTED) {
                    recyclerLoadingStateLiveData.postValue(true);
                }
            }
        });

        if (channel instanceof GroupChannel) {
            Completable.fromAction(new Action() {
                @Override
                public void run() throws Exception {
                    GroupChannel groupChannel = (GroupChannel) channel;
                    DbGroupWrapper dbGroupWrapper = new DbGroupWrapper(groupChannel);
                    dbGroupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ALL);
                    Participant participant = groupChannel.getParticipant(ChatCamp.getCurrentUser().getId());
                    if (participant.getStatus().equals("accepted")) {
                        dbGroupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ACCEPTED);
                    } else {
                        dbGroupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.INVITED);
                    }
                    List<DbGroupWrapper> dirtyChannels = chatCampDb.dbGroupWrapperDao().getDirtyGroup();
                    for (DbGroupWrapper dirtyChannel : dirtyChannels) {
                        if (dirtyChannel.getId().equals(dbGroupWrapper.getId())
                                && dirtyChannel.getLastMessage().getInsertedAt()
                                > dbGroupWrapper.getLastMessage().getInsertedAt()) {
                            dbGroupWrapper.setLastMessage(dirtyChannel.getLastMessage());
                        }
                    }

                    chatCampDb.dbGroupWrapperDao().insert(dbGroupWrapper);
                }
            }).subscribeOn(Schedulers.io()).subscribe();
        }
        if (channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            Map<String, Long> readReceipt = groupChannel.getReadReceipt();
            if (readReceipt.size() == groupChannel.getParticipants().size()) {
                Long lastRead = 0L;
                for (Map.Entry<String, Long> entry : readReceipt.entrySet()) {
                    if (lastRead == 0L || entry.getValue() < lastRead) {
                        lastRead = entry.getValue();
                    }
                }

                this.lastReadTime = lastRead * 1000;
                messageLastReadLiveData.postValue(lastRead * 1000);
                //TODO need to optimise this
            }
        }
        //TODO get the number of message from client

        recyclerLoadingStateLiveData.postValue(false);


        ChatCamp.addChannelListener("message_data_source", new ChatCamp.ChannelListener() {

            @Override
            public void onMessageReceived(final BaseChannel baseChannel, final Message message) {
                if(baseChannel.getType() == BaseChannel.ChannelType.OPEN) {
                    if (!message.getUser().getId().equals(ChatCamp.getCurrentUser().getId())) {
                        Completable.fromAction(new Action() {
                            @Override
                            public void run() throws Exception {
                                DbMessageWrapper messageWrapper = new DbMessageWrapper(message);
                                messageWrapper.setMessageStatus("sent");
                                messageWrapper.setGroupId(baseChannel.getId());
                                chatCampDb.dbMessageWrapperDao().insert(messageWrapper);
                                if (baseChannel.getId().equals(channel.getId())) {
//                                    Diff<DbMessageWrapper> diff = new Diff<>();
//                                    diff.setModel(messageWrapper);
//                                    diff.setChange(Diff.CHANGE.INSERT);
//                                    diff.setPosition(0);
//                                    diffLiveData.postValue(diff);
                                    for (int i = 0; i < messageWrapperList.size(); ++i) {
                                        if (messageWrapperList.get(i).getId().equals(messageWrapper.getId())) {
                                            Diff<DbMessageWrapper> newMessageWrapperDiff = new Diff<>();
                                            newMessageWrapperDiff.setPosition(i);
                                            newMessageWrapperDiff.setChange(Diff.CHANGE.UPDATE);
                                            newMessageWrapperDiff.setModel(messageWrapper);
                                            diffLiveData.postValue(newMessageWrapperDiff);
                                            break;
                                        }
                                    }
                                }
                            }
                        }).subscribeOn(Schedulers.io()).subscribe();
                    }
                } else {

                    Completable.fromAction(new Action() {
                        @Override
                        public void run() throws Exception {
//                            if (!message.getUser().getId().equals(ChatCamp.getCurrentUser().getId())) {
                                DbMessageWrapper messageWrapper = new DbMessageWrapper(message);
                                messageWrapper.setMessageStatus("sent");
                                messageWrapper.setGroupId(baseChannel.getId());
                                if (baseChannel.getId().equals(channel.getId())) {
                                    if (lastReadTime < message.getInsertedAt() * 1000) {
                                        markAsReadLiveData.postValue(null);
                                    }
                                    for (int i = 0; i < messageWrapperList.size(); ++i) {
                                        if (messageWrapperList.get(i).getId().equals(messageWrapper.getId())) {
                                            Diff<DbMessageWrapper> newMessageWrapperDiff = new Diff<>();
                                            newMessageWrapperDiff.setPosition(i);
                                            newMessageWrapperDiff.setChange(Diff.CHANGE.UPDATE);
                                            newMessageWrapperDiff.setModel(messageWrapper);
                                            diffLiveData.postValue(newMessageWrapperDiff);
                                            break;
                                        }
                                    }
                                }
                                chatCampDb.dbMessageWrapperDao().insert(messageWrapper);
//                            }
                            final DbGroupWrapper groupWrapper = new DbGroupWrapper((GroupChannel) baseChannel);
                            groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ALL);
                            Participant participant = ((GroupChannel) baseChannel).getParticipant(ChatCamp.getCurrentUser().getId());

                            if (participant.getStatus().equals("accepted")) {
                                groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ACCEPTED);
                            } else {
                                groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.INVITED);
                            }
                            chatCampDb.dbGroupWrapperDao().insert(groupWrapper);
                        }
                    }).subscribeOn(Schedulers.io()).subscribe();
                }
            }


            @Override
            public void onTypingStatusChanged(GroupChannel groupChannel) {
                if (!groupChannel.getId().equals(channel.getId())) {
                    return;
                }
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
                Diff<TypingStatus> typingDiff = new Diff<>();

                if (isTyping ^ MessageDataSource.this.isTyping) {
                    if (isTyping) {
                        typingDiff.setChange(Diff.CHANGE.INSERT);
                    } else {
                        typingDiff.setChange(Diff.CHANGE.REMOVE);
                    }
                } else {
                    typingDiff.setChange(Diff.CHANGE.UPDATE);
                }
                TypingStatus typingStatus = new TypingStatus();
                typingStatus.setParticipants(typingParticipants);
                typingStatus.setTyping(isTyping);
                typingDiff.setModel(typingStatus);
                typingStatusLiveData.postValue(typingDiff);
                MessageDataSource.this.isTyping = isTyping;
            }

            @Override
            public void onReadStatusUpdated(final GroupChannel groupChannel) {
                Map<String, Long> readReceipt = groupChannel.getReadReceipt();
                if (readReceipt.size() == groupChannel.getParticipants().size()) {
                    Long lastRead = 0L;
                    for (Map.Entry<String, Long> entry : readReceipt.entrySet()) {
                        if (lastRead == 0L || entry.getValue() < lastRead) {
                            lastRead = entry.getValue();
                        }
                    }
                    lastReadTime = lastRead * 1000;
                    messageLastReadLiveData.postValue(lastReadTime);
                    //TODO need to optimise this

                }

                Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        final DbGroupWrapper groupWrapper = new DbGroupWrapper(groupChannel);
                        groupWrapper.setChannelType(DbBaseChannelWrapper.ChannelType.GROUP_CHANNEL);

                        groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ALL);
                        Participant participant = groupChannel.getParticipant(ChatCamp.getCurrentUser().getId());
                        if(participant != null) {
                            if (participant.getStatus().equals("accepted")) {
                                groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ACCEPTED);
                            } else {
                                groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.INVITED);
                            }
                        }
                        chatCampDb.dbGroupWrapperDao().insert(groupWrapper);
                    }
                }).subscribeOn(Schedulers.io()).subscribe();

            }

            @Override
            public void onMessageDeleted(BaseChannel baseChannel, Message message) {
                super.onMessageDeleted(baseChannel, message);
            }

            @Override
            public void onParticipantMuted(BaseChannel baseChannel, User user) {
                super.onParticipantMuted(baseChannel, user);
            }

            @Override
            public void onParticipantUnmuted(BaseChannel baseChannel, User user) {
                super.onParticipantUnmuted(baseChannel, user);
            }
        });

    }

    public void loadMessages() {
        if (loadingFirstTime) {

            Completable.fromAction(new Action() {
                @Override
                public void run() throws Exception {
                    List<DbMessageWrapper> messageWrappers = chatCampDb.dbMessageWrapperDao().getAll(channel.getId());
                    if (loadingFirstTime) {
                        initialMessageListLiveData.postValue(messageWrappers);
                        if (messageWrappers.size() > 0) {
                            onMessageLoadedLiveData.postValue(null);
                        }
                    }
                }
            }).subscribeOn(Schedulers.io()).subscribe();
        } else {
            loadingViewLiveData.postValue(true);
        }
        previousMessageListQuery.setLimit(20);
        previousMessageListQuery.load( new PreviousMessageListQuery.ResultListener() {
            @Override
            public void onResult(final List<Message> list, ChatCampException e) {
                Completable.fromAction(new Action() {
                    @Override
                    public void run() throws Exception {
                        //TODO handle announcements
                        Iterator<Message> iterator = list.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().getType().equals("announcement")) {
                                iterator.remove();
                            }
                        }
                        if (loadingFirstTime) {
                            loadingFirstTime = false;
                            recyclerLoadingStateLiveData.postValue(true);

                            if (messageWrapperList.size() == 0) {
                                onMessageLoadedLiveData.postValue(null);
                            }

                            chatCampDb.dbMessageWrapperDao().deleteAll(channel.getId());
                            List<DbMessageWrapper> wrappers = new ArrayList<>();
                            for (int i = 0; i < list.size(); ++i) {
                                DbMessageWrapper wrapper = new DbMessageWrapper(list.get(i));
                                wrapper.setGroupId(channel.getId());
                                wrapper.setMessageStatus("sent");
                                wrappers.add(wrapper);
                            }
                            chatCampDb.dbMessageWrapperDao().insertAll(wrappers);
                            List<DbMessageWrapper> messageWrappers = chatCampDb.dbMessageWrapperDao().getAll(channel.getId());
                            initialMessageListLiveData.postValue(messageWrappers);
                        } else {
                            List<DbMessageWrapper> wrappers = new ArrayList<>();
                            for (int i = 0; i < list.size(); ++i) {
                                wrappers.add(new DbMessageWrapper(list.get(i)));
                            }
                            messageListLiveData.postValue(wrappers);
                        }
                        markAsReadLiveData.postValue(null);
                        loadingViewLiveData.postValue(false);
                    }
                }).subscribeOn(Schedulers.io()).subscribe();

            }
        });

    }

    public void setMessageList(List<DbMessageWrapper> messageWrapperList) {
        this.messageWrapperList = messageWrapperList;
    }

    public void sendMessage(final String text) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                DbMessageWrapper messageWrapper = new DbMessageWrapper();
                messageWrapper.setGroupId(channel.getId());
                messageWrapper.setMessageStatus("unsent");
                messageWrapper.setType("text");
                messageWrapper.setText(text);
                messageWrapper.setInsertedAt(Calendar.getInstance().getTimeInMillis() / 1000);
                DbUserWrapper userWrapper = new DbUserWrapper();
                userWrapper.setId(ChatCamp.getCurrentUser().getId());
                userWrapper.setDisplayName(ChatCamp.getCurrentUser().getDisplayName());
                userWrapper.setAvatarUrl(ChatCamp.getCurrentUser().getAvatarUrl());
                messageWrapper.setUser(userWrapper);
                final String messageId = messageWrapper.getId();
                if (channel instanceof GroupChannel) {
                    GroupChannel groupChannel = (GroupChannel) channel;
                    DbGroupWrapper groupWrapper = new DbGroupWrapper(groupChannel);
                    groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ALL);

                    Participant participant = groupChannel.getParticipant(ChatCamp.getCurrentUser().getId());
                    if (participant.getStatus().equals("accepted")) {
                        groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ACCEPTED);
                    } else {
                        groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.INVITED);
                    }
                    groupWrapper.setLastMessage(messageWrapper);
                    chatCampDb.dbGroupWrapperDao().insert(groupWrapper);
                }
                chatCampDb.dbMessageWrapperDao().insert(messageWrapper);
                Diff<DbMessageWrapper> messageWrapperDiff = new Diff<>();
                messageWrapperDiff.setPosition(0);
                messageWrapperDiff.setChange(Diff.CHANGE.INSERT);
                messageWrapperDiff.setModel(messageWrapper);
                diffLiveData.postValue(messageWrapperDiff);
                MessageParams params = new MessageParams();
                List<String> customFilter = new ArrayList<>();
                customFilter.add("rty");
                customFilter.add("def");
                params.setCustomFilter(customFilter);
                params.setText(text);
                channel.sendMessage(params, new BaseChannel.SendMessageListener() {
                    @Override
                    public void onSent(final Message message, ChatCampException e) {
                        Completable.fromAction(new Action() {
                            @Override
                            public void run() throws Exception {
                                DbMessageWrapper newMessageWrapper = new DbMessageWrapper(message);
                                newMessageWrapper.setGroupId(channel.getId());
                                newMessageWrapper.setMessageStatus("sent");
//                                newMessageWrapper.setId(messageId);
                                chatCampDb.dbMessageWrapperDao().delete(messageId);
                                chatCampDb.dbMessageWrapperDao().insert(newMessageWrapper);
                                for (int i = 0; i < messageWrapperList.size(); ++i) {
                                    if (messageWrapperList.get(i).getId().equals(messageId)) {
                                        Diff<DbMessageWrapper> newMessageWrapperDiff = new Diff<>();
                                        newMessageWrapperDiff.setPosition(i);
                                        newMessageWrapperDiff.setChange(Diff.CHANGE.UPDATE);
                                        newMessageWrapperDiff.setModel(newMessageWrapper);
                                        diffLiveData.postValue(newMessageWrapperDiff);
                                        break;
                                    }
                                }
                                if (lastReadTime < message.getInsertedAt() * 1000) {
                                    markAsReadLiveData.postValue(null);
                                }
                            }
                        }).subscribeOn(Schedulers.io()).subscribe();
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).subscribe();

    }

    public void sendMessage(final DbMessageWrapper messageWrapper) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                final String messageId = messageWrapper.getId();
                if (channel instanceof GroupChannel) {
                    GroupChannel groupChannel = (GroupChannel) channel;
                    DbGroupWrapper groupWrapper = new DbGroupWrapper(groupChannel);
                    groupWrapper.setLastMessage(messageWrapper);
                    groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ALL);

                    groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ALL);
                    Participant participant = groupChannel.getParticipant(ChatCamp.getCurrentUser().getId());
                    if (participant.getStatus().equals("accepted")) {
                        groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ACCEPTED);
                    } else {
                        groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.INVITED);
                    }

                    chatCampDb.dbGroupWrapperDao().insert(groupWrapper);
                }
                channel.sendMessage(messageWrapper.getText(), new BaseChannel.SendMessageListener() {
                    @Override
                    public void onSent(final Message message, ChatCampException e) {
                        Completable.fromAction(new Action() {
                            @Override
                            public void run() throws Exception {
                                DbMessageWrapper newMessageWrapper = new DbMessageWrapper(message);
                                newMessageWrapper.setGroupId(channel.getId());
                                newMessageWrapper.setMessageStatus("sent");
                                chatCampDb.dbMessageWrapperDao().delete(messageId);
                                chatCampDb.dbMessageWrapperDao().insert(newMessageWrapper);
                                for (int i = 0; i < messageWrapperList.size(); ++i) {
                                    if (messageWrapperList.get(i).getId().equals(messageId)
                                            && messageWrapperList.get(i).getMessageStatus() != null
                                            && messageWrapperList.get(i).getMessageStatus().equals("unsent")) {
                                        Log.e("remove message", i + ": position ");
                                        Diff<DbMessageWrapper> newMessageWrapperDiff = new Diff<>();
                                        newMessageWrapperDiff.setPosition(i);
                                        newMessageWrapperDiff.setChange(Diff.CHANGE.REMOVE);
                                        newMessageWrapperDiff.setModel(newMessageWrapper);
                                        diffLiveData.postValue(newMessageWrapperDiff);
                                        break;
                                    }
                                }
                                if (lastReadTime < message.getInsertedAt() * 1000) {
                                    markAsReadLiveData.postValue(null);
                                }

                                //TODO see why it is not working
//                                Log.e("insert message", 0 + ": position ");
//                                Diff<DbMessageWrapper> newMessageWrapperDiff = new Diff<>();
//                                newMessageWrapperDiff.setPosition(0);
//                                newMessageWrapperDiff.setChange(Diff.CHANGE.INSERT);
//                                newMessageWrapperDiff.setModel(newMessageWrapper);
//                                diffLiveData.postValue(newMessageWrapperDiff);
                            }
                        }).subscribeOn(Schedulers.io()).subscribe();
                    }
                });
            }
        }).subscribeOn(Schedulers.io()).subscribe();

    }

    private boolean isCurrentUserTyping(List<Participant> participants) {
        return (participants.size() == 1 && participants.get(0).getId().equals(ChatCamp.getCurrentUser().getId()));
    }

    public void markAsRead() {
        if (channel instanceof GroupChannel) {
            ((GroupChannel) channel).markAsRead();
        }
    }

    public class TypingStatus {

        boolean isTyping;

        List<Participant> participants = new ArrayList<>();

        public List<Participant> getParticipants() {
            return participants;
        }

        public void setParticipants(List<Participant> participants) {
            this.participants = participants;
        }

        public boolean isTyping() {
            return isTyping;
        }

        public void setTyping(boolean typing) {
            isTyping = typing;
        }
    }
}
