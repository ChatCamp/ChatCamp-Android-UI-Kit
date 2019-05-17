package com.chatcamp.uikit.conversationdetails;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatcamp.uikit.utils.DefaultTimeFormat;
import com.chatcamp.uikit.utils.TimeFormat;
import com.squareup.picasso.Picasso;
import com.chatcamp.uikit.R;

import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Participant;
import io.chatcamp.sdk.User;

public class UserProfileActivity extends AppCompatActivity {

    public static final String KEY_PARTICIPANT_ID = "key_participant_id";
    public static final String KEY_GROUP_ID = "key_group_id";
    public static final String KEY_SHOW_BLOCK_OPTION = "key_show_block_option";

    private TextView onlineStatusTv;
    private ImageView onlineIv;
    private TextView lastSeenTv;
    private ImageView toolbarIv;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private TextView blockTv;
    private boolean isBlocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onlineStatusTv = findViewById(R.id.tv_online_status);
        onlineIv = findViewById(R.id.iv_online);
        lastSeenTv = findViewById(R.id.tv_last_seen);
        toolbarIv = findViewById(R.id.toolbarImage);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        blockTv = findViewById(R.id.tv_block);
        final String participantId = getIntent().getStringExtra(KEY_PARTICIPANT_ID);
        String groupId = getIntent().getStringExtra(KEY_GROUP_ID);
        boolean showBlockOption = getIntent().getBooleanExtra(KEY_SHOW_BLOCK_OPTION, true);
        if(!TextUtils.isEmpty(groupId)) {
            GroupChannel.get(groupId, new GroupChannel.GetListener() {
                @Override
                public void onResult(GroupChannel oldGroupChannel, ChatCampException e) {
                    populateUi(oldGroupChannel, participantId);
                    oldGroupChannel.sync(new GroupChannel.SyncListener() {
                        @Override
                        public void onResult(GroupChannel groupChannel, ChatCampException e) {
                            populateUi(groupChannel, participantId);
                        }
                    });
                }
            });
        }
        if(showBlockOption) {
            blockTv.setVisibility(View.VISIBLE);
            blockTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isBlocked) {
                        ChatCamp.blockUser(participantId, new ChatCamp.OnUserBlockListener() {
                            @Override
                            public void onUserBlocked(User user, ChatCampException exception) {
                                Toast.makeText(UserProfileActivity.this, user.getDisplayName() + " Blocked", Toast.LENGTH_LONG).show();
                                blockTv.setText("UnBlock");
                                isBlocked = true;
                            }
                        });
                    } else {
                        ChatCamp.unblockuser(participantId, new ChatCamp.OnUserUnblockListener() {
                            @Override
                            public void onUserUnblocked(User user, ChatCampException exception) {
                                Toast.makeText(UserProfileActivity.this, user.getDisplayName() + " Unblocked", Toast.LENGTH_LONG).show();
                                blockTv.setText("Block");
                                isBlocked = false;
                            }
                        });
                    }
                }
            });
        } else {
            blockTv.setVisibility(View.GONE);
        }
    }

    private void populateUi(GroupChannel groupChannel,  String participantId) {
        Participant user = groupChannel.getParticipant(participantId);
        if (user != null) {
            isBlocked = user.ifBlockedByMe();
            if(isBlocked) {
                blockTv.setText("UnBlock");
            } else {
                blockTv.setText("Block");
            }
            collapsingToolbarLayout.setTitle(user.getDisplayName());
            Picasso.with(UserProfileActivity.this).load(user.getAvatarUrl())
                    .placeholder(R.drawable.icon_default_contact)
                    .error(R.drawable.icon_default_contact).into(toolbarIv);
            if (user.ifOnline()) {
                onlineIv.setVisibility(View.VISIBLE);
                lastSeenTv.setVisibility(View.GONE);
                onlineStatusTv.setText("Online");
            } else {
                onlineIv.setVisibility(View.GONE);
                lastSeenTv.setVisibility(View.VISIBLE);
                TimeFormat timeFormat = new DefaultTimeFormat();
                timeFormat.setTime(lastSeenTv, user.getLastSeen() * 1000);
                onlineStatusTv.setText("Last Seen");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
