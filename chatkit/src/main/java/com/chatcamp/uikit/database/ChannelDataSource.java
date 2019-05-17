package com.chatcamp.uikit.database;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.chatcamp.uikit.channel.ChannelAdapter;
import com.chatcamp.uikit.channel.LastMessageChannelComparator;

import java.util.ArrayList;
import java.util.Collections;
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
import io.chatcamp.sdk.TotalCountFilterParams;
import io.chatcamp.sdk.User;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class ChannelDataSource {
    private Context context;
    private ChatCampDb chatCampDb;
    private DbGroupWrapper.ParticipantState participantState;
    private DbBaseChannelWrapper.ChannelType channelType;
    private ChannelAdapter.ChannelComparator comparator;
    private List<String> customFilter;
    private boolean loadingFirstTime;

    private MutableLiveData<Boolean> recyclerLoadingStateLiveData = new MutableLiveData<>();
    private MutableLiveData<List<DbBaseChannelWrapper>> initialBaseChannelLiveData = new MutableLiveData<>();
    private MutableLiveData<List<DbBaseChannelWrapper>> baseChannelLiveData = new MutableLiveData<>();
    private MutableLiveData channelLoadedLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadViewLiveData = new MutableLiveData<>();

    private MutableLiveData<DbGroupWrapper> updateGroupLiveData = new MutableLiveData<>();
    private OpenChannelListQuery openChannelListQuery;

    private GroupChannelListQuery groupChannelListQuery;

    public ChannelDataSource(Context context) {
        this.context = context;
        chatCampDb = ChatCampDb.getInstance(context);
    }

    public MutableLiveData<Boolean> getRecyclerLoadingStateLiveData() {
        return recyclerLoadingStateLiveData;
    }

    public MutableLiveData<List<DbBaseChannelWrapper>> getInitialBaseChannelLiveData() {
        return initialBaseChannelLiveData;
    }

    public MutableLiveData<List<DbBaseChannelWrapper>> getBaseChannelLiveData() {
        return baseChannelLiveData;
    }

    public MutableLiveData getChannelLoadedLiveData() {
        return channelLoadedLiveData;
    }

    public MutableLiveData<Boolean> getLoadViewLiveData() {
        return loadViewLiveData;
    }

    public MutableLiveData<DbGroupWrapper> getUpdateGroupLiveData() {
        return updateGroupLiveData;
    }

    public void setChannelType(final DbBaseChannelWrapper.ChannelType channelType, final DbGroupWrapper.ParticipantState participantState,
                               List<String> customFilter, ChannelAdapter.ChannelComparator comparator) {
        this.participantState = participantState;
        this.channelType = channelType;
        this.comparator = comparator;
        this.customFilter = customFilter;
        loadingFirstTime = true;
        recyclerLoadingStateLiveData.postValue(false);
        loadChannels();
        addChannelListener();
    }

    private void addChannelListener() {
        ChatCamp.addChannelListener("channel_data_source" + channelType.toString() + participantState.toString(), new ChatCamp.ChannelListener() {

            @Override
            public void onMessageReceived(final BaseChannel baseChannel, final Message message) {
                if(baseChannel.getType() == BaseChannel.ChannelType.GROUP) {
                    final GroupChannel groupChannel = (GroupChannel)baseChannel ;
                    Completable.fromAction(new Action() {
                        @Override
                        public void run() throws Exception {
                            if (channelType == null) {
                                return;
                            }
                            String status = groupChannel.getParticipant(ChatCamp.getCurrentUser().getId()).getStatus();
                            DbGroupWrapper.ParticipantState groupParticipantState;
                            if (status.equalsIgnoreCase("accepted")) {
                                groupParticipantState = DbGroupWrapper.ParticipantState.ACCEPTED;
                            } else {
                                groupParticipantState = DbGroupWrapper.ParticipantState.INVITED;
                            }
                            if (channelType == DbBaseChannelWrapper.ChannelType.GROUP_CHANNEL
                                    && participantState == DbGroupWrapper.ParticipantState.ALL || groupParticipantState == participantState) {
                                DbGroupWrapper groupWrapper = new DbGroupWrapper(groupChannel);
                                groupWrapper.setChannelType(DbBaseChannelWrapper.ChannelType.GROUP_CHANNEL);
                                groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ALL);
                                Participant participant = groupChannel.getParticipant(ChatCamp.getCurrentUser().getId());
                                if (participant.getStatus().equals("accepted")) {
                                    groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ACCEPTED);
                                } else {
                                    groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.INVITED);
                                }

                                updateGroupLiveData.postValue(groupWrapper);
                                chatCampDb.dbGroupWrapperDao().insert(groupWrapper);
                                DbMessageWrapper messageWrapper = new DbMessageWrapper(message);
                                messageWrapper.setGroupId(groupChannel.getId());
                                messageWrapper.setMessageStatus("sent");
                                chatCampDb.dbMessageWrapperDao().insert(messageWrapper);
                            }
                        }
                    }).subscribeOn(Schedulers.io()).subscribe();
                }
            }

            @Override
            public void onParticipantUnmuted(BaseChannel baseChannel, User user) {
                super.onParticipantUnmuted(baseChannel, user);
            }

            @Override
            public void onParticipantBanned(BaseChannel baseChannel, User user) {
                super.onParticipantBanned(baseChannel, user);
            }

            @Override
            public void onParticipantUnbanned(BaseChannel baseChannel, User user) {
                super.onParticipantUnbanned(baseChannel, user);
            }

            @Override
            public void onTotalChannelCount(int count, TotalCountFilterParams params) {
                super.onTotalChannelCount(count, params);
            }

            @Override
            public void onChannelDeleted(String channelId) {
                super.onChannelDeleted(channelId);
            }

            @Override
            public void onArchived(GroupChannel groupChannel) {
                super.onArchived(groupChannel);
            }

            @Override
            public void onUnarchived(GroupChannel groupChannel) {
                super.onUnarchived(groupChannel);
            }

            @Override
            public void onMessageUpdated(BaseChannel baseChannel, Message message) {
                super.onMessageUpdated(baseChannel, message);
            }

            @Override
            public void onChannelUpdated(BaseChannel baseChannel) {
                super.onChannelUpdated(baseChannel);
            }

            @Override
            public void onHistoryCleared(BaseChannel baseChannel) {
                super.onHistoryCleared(baseChannel);
            }
        });
    }

    public void loadChannels() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (comparator == null) {
                    comparator = new LastMessageChannelComparator();
                }
                if (channelType == DbBaseChannelWrapper.ChannelType.OPEN_CHANNEL) {
                    List<DbOpenChannelWrapper> localOpenChannelWrappers = new ArrayList<>();
                    if (openChannelListQuery == null || loadingFirstTime) {
                        Log.e("Group Channel", "Loading for the first time from database");
                        localOpenChannelWrappers = chatCampDb.dbOpenChannelWrapperDao().getAll();
                        if (loadingFirstTime) {
                            initialBaseChannelLiveData.postValue(new ArrayList<DbBaseChannelWrapper>(localOpenChannelWrappers));
                            if (localOpenChannelWrappers.size() > 0) {
                                channelLoadedLiveData.postValue(true);
                            }
                        }
                        openChannelListQuery = OpenChannel.createOpenChannelListQuery();
                    } else {
                        loadViewLiveData.postValue(true);
                    }
                    final List<DbOpenChannelWrapper> finalLocalOpenChannelWrappers = localOpenChannelWrappers;
                    openChannelListQuery.load(new OpenChannelListQuery.ResultHandler() {
                        @Override
                        public void onResult(final List<OpenChannel> openChannelList, ChatCampException e) {
                            Completable.fromAction(new Action() {
                                @Override
                                public void run() throws Exception {
                                    List<DbOpenChannelWrapper> openChannelWrappers = new ArrayList<>();
                                    for (int i = 0; i < openChannelList.size(); ++i) {
                                        DbOpenChannelWrapper openChannelWrapper = new DbOpenChannelWrapper(openChannelList.get(i));
                                        openChannelWrappers.add(openChannelWrapper);
                                    }
                                    if (loadingFirstTime) {
                                        loadingFirstTime = false;
                                        recyclerLoadingStateLiveData.postValue(true);
                                        initialBaseChannelLiveData.postValue(new ArrayList<DbBaseChannelWrapper>(openChannelWrappers));
                                        if (finalLocalOpenChannelWrappers.size() == 0) {
                                            channelLoadedLiveData.postValue(true);
                                        }
                                        chatCampDb.dbOpenChannelWrapperDao().deleteAll();
                                        chatCampDb.dbOpenChannelWrapperDao().insertAll(openChannelWrappers);
                                    } else {
                                        baseChannelLiveData.postValue(new ArrayList<DbBaseChannelWrapper>(openChannelWrappers));
                                    }
                                    loadViewLiveData.postValue(false);
                                }
                            }).subscribeOn(Schedulers.io()).subscribe();

                        }
                    });
                } else if (channelType == DbBaseChannelWrapper.ChannelType.GROUP_CHANNEL) {
                    List<DbGroupWrapper> localGroupWrappers = new ArrayList<>();
                    if (groupChannelListQuery == null || loadingFirstTime) {
                        Log.e("Group Channel", "Loading for the first time from database");
                        if (participantState == DbGroupWrapper.ParticipantState.INVITED || participantState == DbGroupWrapper.ParticipantState.ACCEPTED) {
                            localGroupWrappers = chatCampDb.dbGroupWrapperDao().getAll(participantState);
                        } else {
                            localGroupWrappers = chatCampDb.dbGroupWrapperDao().getAll();
                        }
                        if (loadingFirstTime) {
                            Collections.sort(localGroupWrappers, comparator);
                            initialBaseChannelLiveData.postValue(new ArrayList<DbBaseChannelWrapper>(localGroupWrappers));
                            if (localGroupWrappers.size() > 0) {
                                channelLoadedLiveData.postValue(true);
                            }
                        }
                        // TODO check custom filters

                        groupChannelListQuery = GroupChannel.createGroupChannelListQuery();
                        GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter groupParticipantState
                                = GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter.PARTICIPANT_STATE_ALL;
                        if (participantState == DbGroupWrapper.ParticipantState.INVITED) {
                            groupParticipantState = GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter.PARTICIPANT_STATE_INVITED;
                        } else if (participantState == DbGroupWrapper.ParticipantState.ACCEPTED) {
                            groupParticipantState = GroupChannelListQuery.GroupChannelListQueryParticipantStateFilter.PARTICIPANT_STATE_ACCEPTED;
                        }
                        groupChannelListQuery.setParticipantStateFilter(groupParticipantState);
                        if (customFilter != null) {
                            groupChannelListQuery.setCustomFilter(customFilter);
                        }
                    } else {
                        loadViewLiveData.postValue(true);
                    }
                    Log.e("Group Channel", "querying group from api");
                    final List<DbGroupWrapper> finalLocalGroupWrappers = localGroupWrappers;
                    groupChannelListQuery.load(new GroupChannelListQuery.ResultHandler() {
                        @Override
                        public void onResult(final List<GroupChannel> groupChannelList, ChatCampException e) {
                            Completable.fromAction(new Action() {
                                @Override
                                public void run() throws Exception {
                                    List<DbGroupWrapper> groupWrappers = new ArrayList<>();
                                    List<DbGroupWrapper> dirtyGroups = chatCampDb.dbGroupWrapperDao().getDirtyGroup();
                                    for (int i = 0; i < groupChannelList.size(); ++i) {

                                        DbGroupWrapper groupWrapper = new DbGroupWrapper(groupChannelList.get(i));
                                        groupWrapper.setParticipantState(participantState);
                                        Participant participant = groupChannelList.get(i).getParticipant(ChatCamp.getCurrentUser().getId());
                                        if(participant != null) {
                                            if (participant.getStatus().equals("accepted")) {
                                                groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.ACCEPTED);
                                            } else {
                                                groupWrapper.setParticipantState(DbGroupWrapper.ParticipantState.INVITED);
                                            }
                                        }
                                        for (DbGroupWrapper dirtyGroup : dirtyGroups) {
                                            if (dirtyGroup.getId().equals(groupWrapper.getId())
                                                    && dirtyGroup.getLastMessage().getInsertedAt() > groupWrapper.getLastMessage().getInsertedAt()) {
                                                groupWrapper.setLastMessage(dirtyGroup.getLastMessage());
                                            }
                                        }
                                        groupWrappers.add(groupWrapper);
                                    }
                                    if (comparator != null) {
                                        Collections.sort(groupWrappers, comparator);
                                    }
                                    if (loadingFirstTime) {
                                        loadingFirstTime = false;
                                        recyclerLoadingStateLiveData.postValue(true);
                                        Log.e("Group Channel", "result from first api call");

                                        initialBaseChannelLiveData.postValue(new ArrayList<DbBaseChannelWrapper>(groupWrappers));
                                        if (finalLocalGroupWrappers.size() == 0) {
                                            channelLoadedLiveData.postValue(null);
                                        }
                                        chatCampDb.dbGroupWrapperDao().deleteAll();
                                        chatCampDb.dbGroupWrapperDao().insertAll(groupWrappers);
                                    } else {
                                        Log.e("Group Channel", "result from subsequent api call");
                                        baseChannelLiveData.postValue(new ArrayList<DbBaseChannelWrapper>(groupWrappers));
                                    }
                                    loadViewLiveData.postValue(false);
                                }
                            }).subscribeOn(Schedulers.io()).subscribe();

                        }
                    });
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe();

    }
}
