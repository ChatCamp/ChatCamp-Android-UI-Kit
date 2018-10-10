package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.ImageLoader;
import com.chatcamp.uikit.conversationdetails.GroupDetailActivity;
import com.chatcamp.uikit.conversationdetails.UserProfileActivity;
import com.chatcamp.uikit.utils.HeaderViewClickListener;
import com.chatcamp.uikit.utils.TextViewFont;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Participant;

/**
 * Created by shubhamdhabhai on 10/05/18.
 */

public class HeaderView extends LinearLayout {

    private Toolbar toolbar;
    private ImageView groupImageIv;
    private TextViewFont groupTitleTv;
    private BaseChannel channel;
    private Participant otherParticipant;
    private HeaderStyle headerStyle;
    private ImageLoader avatarLoader;
    private TextViewFont participantCount;
    private HeaderViewClickListener headerViewClickListener;
    private boolean isBlocked;

    public HeaderView(Context context) {
        super(context);
        init(context);
    }

    public HeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_header, this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        groupImageIv = toolbar.findViewById(R.id.iv_group_image);
        groupTitleTv = toolbar.findViewById(R.id.tv_group_name);
        participantCount = toolbar.findViewById(R.id.tv_participant_count);
    }

    private void init(Context context, AttributeSet attrs) {
        init(context);
        headerStyle = HeaderStyle.parseStyle(context, attrs);
        toolbar.setBackgroundColor(headerStyle.getBackgroundColor());
        groupImageIv.getLayoutParams().height = headerStyle.getImageHeight();
        groupImageIv.getLayoutParams().width = headerStyle.getImageWidth();
        groupTitleTv.setTextColor(headerStyle.getTitleTextColor());
        groupTitleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, headerStyle.getTitleTextSize());
        groupTitleTv.setTypeface(groupTitleTv.getTypeface(), headerStyle.getTitleTextStyle());
        if (!TextUtils.isEmpty(headerStyle.getCustomFont())) {
            groupTitleTv.setCustomFont(headerStyle.getCustomFont());
            participantCount.setCustomFont(headerStyle.getCustomFont());
        }
        if (headerStyle.isShowParticipantCount()) {
            participantCount.setVisibility(VISIBLE);
            participantCount.setTextColor(headerStyle.getParticipantCountTextColor());
            Drawable participantCountBackground = participantCount.getBackground();
            participantCountBackground.mutate().setColorFilter(headerStyle.getParticipantCountBackgroundColor(), PorterDuff.Mode.SRC_IN);

            participantCount.setBackground(participantCountBackground);
        } else {
            participantCount.setVisibility(GONE);
        }
        LayoutParams titleParams = (LayoutParams) groupTitleTv.getLayoutParams();
        titleParams.leftMargin = headerStyle.getTitleMarginLeft();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            titleParams.setMarginStart(headerStyle.getTitleMarginLeft());
        }
        groupTitleTv.setLayoutParams(titleParams);
    }

    public void setAvatarLoader(ImageLoader avatarLoader) {
        this.avatarLoader = avatarLoader;
    }

    public void setHeaderViewClickListener(HeaderViewClickListener headerViewClickListener) {
        this.headerViewClickListener = headerViewClickListener;
    }

    public void setChannel(BaseChannel channel) {
        this.channel = channel;
        boolean isOneToOneConversation = false;
        if (channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            participantCount.setText(String.valueOf(groupChannel.getParticipantsCount()));
            if (groupChannel.getParticipants().size() == 2 && !headerStyle.isHeaderViewAlwaysShowChannelName()) {
                isOneToOneConversation = true;
            }
            if (isOneToOneConversation) {
                List<Participant> participants = ((GroupChannel) channel).getParticipants();
                for (Participant participant : participants) {
                    if (!participant.getId().equals(ChatCamp.getCurrentUser().getId())) {
                        otherParticipant = participant;
                    }
                }
                populateToobar(otherParticipant.getAvatarUrl(), otherParticipant.getDisplayName());
            } else {
                populateToobar(channel.getAvatarUrl(), channel.getName());
            }
        } else {
            populateToobar(channel.getAvatarUrl(), channel.getName());
        }

        OnTitleClickListener titleClickListener =
                new OnTitleClickListener(channel.getId(), isOneToOneConversation,
                        otherParticipant == null ? null : otherParticipant.getId());
        groupTitleTv.setOnClickListener(titleClickListener);
        groupImageIv.setOnClickListener(titleClickListener);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    private void populateToobar(String imageUrl, String title) {
        if (avatarLoader != null) {
            avatarLoader.loadImage(groupImageIv, imageUrl);
        } else {
            Picasso.with(getContext()).load(imageUrl)
                    .placeholder(R.drawable.icon_default_contact)
                    .error(R.drawable.icon_default_contact)
                    .into(groupImageIv, new Callback() {
                        @Override
                        public void onSuccess() {
                            Bitmap imageBitmap = ((BitmapDrawable) groupImageIv.getDrawable()).getBitmap();
                            RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                            imageDrawable.setCircular(true);
                            imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                            groupImageIv.setImageDrawable(imageDrawable);
                        }

                        @Override
                        public void onError() {
                            groupImageIv.setImageResource(R.drawable.icon_default_contact);
                        }
                    });
        }
        groupTitleTv.setText(title);
    }

    public void onOptionsItemSelected(final MenuItem item) {
        if (otherParticipant != null) {
            if (!isBlocked) {
                ChatCamp.blockUser(otherParticipant.getId(), new ChatCamp.OnUserBlockListener() {
                    @Override
                    public void onUserBlocked(Participant participant, ChatCampException exception) {
                        item.setTitle("UnBlock");
                        isBlocked = true;
                    }
                });
            } else {
                ChatCamp.unBlockUser(otherParticipant.getId(), new ChatCamp.OnUserUnBlockListener() {
                    @Override
                    public void onUserUnBlocked(Participant participant, ChatCampException exception) {
                        item.setTitle("Block");
                        isBlocked = false;
                    }
                });
            }
        }
    }

    public void onCreateOptionMenu(MenuItem menuItem) {
        if (channel != null && channel instanceof GroupChannel) {
            GroupChannel groupChannel = (GroupChannel) channel;
            if (groupChannel.getParticipants().size() == 2) {
                List<Participant> participants = ((GroupChannel) channel).getParticipants();
                for (Participant participant : participants) {
                    if (!participant.getId().equals(ChatCamp.getCurrentUser().getId())) {
                        otherParticipant = participant;
                        isBlocked = otherParticipant.isBlockedByMe();
                    }
                }
            }
            if (isBlocked) {
                menuItem.setTitle("UnBlock");
            } else {
                menuItem.setTitle("Block");
            }
        }
    }

    public void onPrepareOptionsMenu(final MenuItem menuItem) {
        if(otherParticipant != null) {
            GroupChannel.get(channel.getId(), new GroupChannel.GetListener() {
                @Override
                public void onResult(GroupChannel groupChannel, ChatCampException e) {
                    List<Participant> participants = groupChannel.getParticipants();
                    for (Participant participant : participants) {
                        if (!participant.getId().equals(ChatCamp.getCurrentUser().getId())) {
                            otherParticipant = participant;
                            isBlocked = otherParticipant.isBlockedByMe();
                        }
                    }
                    if (isBlocked) {
                        menuItem.setTitle("UnBlock");
                    } else {
                        menuItem.setTitle("Block");
                    }
                }
            });
        }
    }


    class OnTitleClickListener implements View.OnClickListener {

        private final String channelId;
        private final boolean isOneToOneConversation;
        private final String participantId;

        public OnTitleClickListener(String channelId, boolean isOnToOneConversation, String participantId) {
            this.channelId = channelId;
            this.isOneToOneConversation = isOnToOneConversation;
            this.participantId = participantId;
        }

        @Override
        public void onClick(View view) {
            boolean continueExecution = true;
            if (headerViewClickListener != null) {
                continueExecution = headerViewClickListener.onHeaderViewClicked(channel.getId(),
                        isOneToOneConversation, participantId);
            }
            if (continueExecution) {
                if (isOneToOneConversation) {
                    Intent intent = new Intent(getContext(), UserProfileActivity.class);
                    if (!TextUtils.isEmpty(participantId)) {
                        intent.putExtra(UserProfileActivity.KEY_PARTICIPANT_ID, participantId);
                        intent.putExtra(UserProfileActivity.KEY_GROUP_ID, channelId);
                        intent.putExtra(UserProfileActivity.KEY_SHOW_BLOCK_OPTION, headerStyle.isShowBlockOption());
                    }
                    getContext().startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), GroupDetailActivity.class);
                    intent.putExtra(GroupDetailActivity.KEY_GROUP_ID, channelId);
                    getContext().startActivity(intent);
                }
            }

        }
    }
}
