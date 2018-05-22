package com.chatcamp.uikit.conversationdetails;


import io.chatcamp.sdk.Participant;

/**
 * Created by shubhamdhabhai on 21/02/18.
 */

public class ParticipantView {

    private Participant participant;

    public ParticipantView(Participant participant) {
        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }
}
