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

import com.squareup.picasso.Picasso;
import com.chatcamp.uikit.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.User;

public class UserProfileActivity extends AppCompatActivity {

    public static final String KEY_PARTICIPANT_ID = "key_participant_id";
    public static final String KEY_GROUP_ID = "key_group_id";

    private TextView onlineStatusTv;
    private ImageView onlineIv;
    private TextView lastSeenTv;
    private ImageView toolbarIv;
    private CollapsingToolbarLayout collapsingToolbarLayout;

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
        final String participantId = getIntent().getStringExtra(KEY_PARTICIPANT_ID);
        String groupId = getIntent().getStringExtra(KEY_GROUP_ID);
        if(!TextUtils.isEmpty(groupId)) {
            GroupChannel.get(groupId, new GroupChannel.GetListener() {
                @Override
                public void onResult(GroupChannel groupChannel, ChatCampException e) {
                    User user = groupChannel.getParticipant(participantId);
                    if (user != null) {
                        collapsingToolbarLayout.setTitle(user.getDisplayName());
                        Picasso.with(UserProfileActivity.this).load(user.getAvatarUrl())
                                .placeholder(R.drawable.icon_default_contact)
                                .error(R.drawable.icon_default_contact).into(toolbarIv);
                        if (user.isOnline()) {
                            onlineIv.setVisibility(View.VISIBLE);
                            lastSeenTv.setVisibility(View.GONE);
                            onlineStatusTv.setText("Online");
                        } else {
                            onlineIv.setVisibility(View.GONE);
                            lastSeenTv.setVisibility(View.VISIBLE);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date date = new Date(user.getLastSeen() * 1000);
                            lastSeenTv.setText(format.format(date));
                            onlineStatusTv.setText("Last Seen");
                        }
                    }
                }
            });
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
