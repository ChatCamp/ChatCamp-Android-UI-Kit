package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.chatcamp.uikit.R;
import com.chatcamp.uikit.conversationdetails.GroupDetailActivity;
import com.chatcamp.uikit.conversationdetails.UserProfileActivity;
import com.chatcamp.uikit.utils.AvatarLoader;
import com.chatcamp.uikit.utils.HeaderViewClickListener;

import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Participant;

/**
 * Created by shubhamdhabhai on 10/05/18.
 */

public class HeaderView extends LinearLayout {

    private Toolbar toolbar;
    private ImageView groupImageIv;
    private TextView groupTitleTv;
    private BaseChannel channel;
    private Participant otherParticipant;
    private HeaderStyle headerStyle;
    private AvatarLoader avatarLoader;
    private HeaderViewClickListener headerViewClickListener;

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
        LayoutParams titleParams = (LayoutParams) groupTitleTv.getLayoutParams();
        titleParams.leftMargin = headerStyle.getTitleMarginLeft();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            titleParams.setMarginStart(headerStyle.getTitleMarginLeft());
        }
        groupTitleTv.setLayoutParams(titleParams);
    }

    public void setAvatarLoader(AvatarLoader avatarLoader) {
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
            if (groupChannel.getParticipants().size() <= 2 && groupChannel.isDistinct()) {
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
            if(continueExecution) {
                if (isOneToOneConversation) {
                    Intent intent = new Intent(getContext(), UserProfileActivity.class);
                    if (!TextUtils.isEmpty(participantId)) {
                        intent.putExtra(UserProfileActivity.KEY_PARTICIPANT_ID, participantId);
                        intent.putExtra(UserProfileActivity.KEY_GROUP_ID, channelId);
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
