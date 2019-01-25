package com.chatcamp.uikit.database;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;

import java.util.List;
import java.util.Map;

import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Participant;

@Entity(tableName = "group_table")
public class DbGroupWrapper extends DbBaseChannelWrapper {

    private List<Participant> participants;
    private int participantsCount;
    private int unreadMessageCount;
    @Embedded
    private DbMessageWrapper lastMessage;
    private Boolean isDistinct;
    private Map<String, Long> readReceipt;
    private boolean isArchived;

    private List<String> customFilter;

    private ParticipantState participantState;

    public DbGroupWrapper(GroupChannel groupChannel) {
        super(groupChannel);
        this.participants = groupChannel.getParticipants();
        this.participantsCount = groupChannel.getParticipantsCount();
        this.unreadMessageCount = groupChannel.getUnreadMessageCount();
        this.lastMessage = new DbMessageWrapper(groupChannel.getLastMessage());
        this.lastMessage.setGroupId(groupChannel.getId());
        this.isDistinct = groupChannel.isDistinct();
        this.readReceipt = groupChannel.getReadReceipt();
        this.channelType = ChannelType.GROUP_CHANNEL;
        this.customFilter = groupChannel.getCustomFilter();
    }

    public DbGroupWrapper() {
        super();
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public int getParticipantsCount() {
        return participantsCount;
    }

    public void setParticipantsCount(int participantsCount) {
        this.participantsCount = participantsCount;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public DbMessageWrapper getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(DbMessageWrapper lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Boolean getDistinct() {
        return isDistinct;
    }

    public void setDistinct(Boolean distinct) {
        isDistinct = distinct;
    }

    public Map<String, Long> getReadReceipt() {
        return readReceipt;
    }

    public void setReadReceipt(Map<String, Long> readReceipt) {
        this.readReceipt = readReceipt;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public ParticipantState getParticipantState() {
        return participantState;
    }

    public void setParticipantState(ParticipantState participantState) {
        this.participantState = participantState;
    }

    public List<String> getCustomFilter() {
        return customFilter;
    }

    public void setCustomFilter(List<String> customFilter) {
        this.customFilter = customFilter;
    }

    public enum ParticipantState {
        ACCEPTED(0),
        INVITED(1),
        ALL(2);
        int value;

        ParticipantState(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


}
