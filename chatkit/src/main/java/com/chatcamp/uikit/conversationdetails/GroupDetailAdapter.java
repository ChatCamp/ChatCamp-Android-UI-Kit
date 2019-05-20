package com.chatcamp.uikit.conversationdetails;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatcamp.uikit.customview.AvatarView;
import com.chatcamp.uikit.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.chatcamp.sdk.Participant;

/**
 * Created by shubhamdhabhai on 21/02/18.
 */

public class GroupDetailAdapter extends RecyclerView.Adapter {

    private static final int VIEW_ADD_PARTICIPANT = 0;
    private static final int VIEW_EXIT_GROUP = 1;
    private static final int VIEW_PARTICIPANT = 2;
    private static final int VIEW_PARTICIPANT_COUNT = 3;
    private final Context context;

    private boolean isGroupChannel;

    private List<ParticipantView> participants;

    public interface OnParticipantClickedListener {
        void onAddParticipantClicked();

        void onParticipantClicked(Participant participant);

        void onExitGroupClicked();
    }

    public void setParticipantClickedListener(OnParticipantClickedListener participantClickedListener) {
        this.participantClickedListener = participantClickedListener;
    }

    private OnParticipantClickedListener participantClickedListener;

    public GroupDetailAdapter(Context context) {
        participants = new ArrayList<>();
        this.context = context;
        if(isGroupChannel) {
            participants.add(0, new ParticipantView(null));
            participants.add(new ParticipantView(null));
            participants.add(new ParticipantView(null));
        }

    }

    public void setIsGroupChannel(boolean groupChannel) {
        isGroupChannel = groupChannel;
    }

    public void add(ParticipantView participant) {
        participants.add(participants.size() - 1, participant);
        notifyDataSetChanged();
    }

    public void addAll(List<ParticipantView> participantViews) {
        participants.addAll(participants.size() - 1, participantViews);
        notifyDataSetChanged();
    }

    public void clear() {
        if(isGroupChannel) {
            participants.clear();
            participants.add(0, new ParticipantView(null));
            participants.add(new ParticipantView(null));
            participants.add(new ParticipantView(null));
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_PARTICIPANT:
                return new ParticipantViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.layout_participant_view, parent, false));
            case VIEW_PARTICIPANT_COUNT:
                return new ParticipantCountViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_participant_count, parent, false));
            case VIEW_ADD_PARTICIPANT:
                return new AddParticipantViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_add_participant, parent, false));
            case VIEW_EXIT_GROUP:
                return new ExitGroupViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_exit_participant, parent, false));
            default:
                return new ParticipantViewHolder(LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.layout_participant_view, parent, false));

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_PARTICIPANT:
                ((ParticipantViewHolder) holder).bind(participants.get(position));
                break;
            case VIEW_PARTICIPANT_COUNT:
                ((ParticipantCountViewHolder) holder).bind();
                break;
            case VIEW_ADD_PARTICIPANT:
                ((AddParticipantViewHolder) holder).bind();
                break;
            case VIEW_EXIT_GROUP:
                ((ExitGroupViewHolder) holder).bind();

        }
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_PARTICIPANT_COUNT;
        } else if (position == 1) {
            return VIEW_ADD_PARTICIPANT;
        } else if (position == participants.size() - 1) {
            return VIEW_EXIT_GROUP;
        } else {
            return VIEW_PARTICIPANT;
        }
    }

    class ParticipantCountViewHolder extends RecyclerView.ViewHolder {

        TextView participantCountTv;

        public ParticipantCountViewHolder(View itemView) {
            super(itemView);
            participantCountTv = itemView.findViewById(R.id.tv_participant_count);
        }

        public void bind() {
            participantCountTv.setText(String.format("%d Participants", participants.size() - 3));
        }
    }

    class AddParticipantViewHolder extends RecyclerView.ViewHolder {

        public AddParticipantViewHolder(View itemView) {
            super(itemView);
        }

        public void bind() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (participantClickedListener != null) {
                        participantClickedListener.onAddParticipantClicked();
                    }
                }
            });
        }
    }

    class ExitGroupViewHolder extends RecyclerView.ViewHolder {

        public ExitGroupViewHolder(View itemView) {
            super(itemView);
        }

        public void bind() {
            if(isGroupChannel) {
                itemView.setVisibility(View.VISIBLE);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (participantClickedListener != null) {
                            participantClickedListener.onExitGroupClicked();
                        }
                    }
                });
            } else {
                itemView.setVisibility(View.GONE);
            }
        }
    }

    class ParticipantViewHolder extends RecyclerView.ViewHolder {

        AvatarView participantIv;

        TextView participantTv;

        ImageView onlineIv;

        TextView lastSeenTv;

        public ParticipantViewHolder(View itemView) {
            super(itemView);
            participantIv = itemView.findViewById(R.id.iv_participant_image);
            participantTv = itemView.findViewById(R.id.tv_participant_name);
            onlineIv = itemView.findViewById(R.id.iv_online);
            lastSeenTv = itemView.findViewById(R.id.tv_last_seen);
        }

        public void bind(final ParticipantView participantView) {

            participantIv.initView(participantView.getParticipant().getAvatarUrl(),
                    participantView.getParticipant().getDisplayName());
            participantTv.setText(participantView.getParticipant().getDisplayName());
            if (participantView.getParticipant().ifOnline()) {
                onlineIv.setVisibility(View.VISIBLE);
                lastSeenTv.setVisibility(View.GONE);
            } else {
                onlineIv.setVisibility(View.GONE);
                lastSeenTv.setVisibility(View.VISIBLE);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(participantView.getParticipant().getLastSeen() * 1000);
                lastSeenTv.setText(format.format(date));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (participantClickedListener != null) {
                        participantClickedListener.onParticipantClicked(participantView.getParticipant());
                    }
                }
            });
        }
    }
}
