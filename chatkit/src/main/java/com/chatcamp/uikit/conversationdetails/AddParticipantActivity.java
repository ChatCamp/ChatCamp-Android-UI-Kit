package com.chatcamp.uikit.conversationdetails;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.customview.LoadingView;
import com.chatcamp.uikit.user.SelectableUserList;

import java.util.List;

import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.User;

public class AddParticipantActivity extends AppCompatActivity {

    public static final String KEY_PARTICIPANT_IDS = "key_participant_ids";
    public static final String KEY_GROUP_ID = "key_group_id";

    private LoadingView loadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participant);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final List<String> participantIds = getIntent().getStringArrayListExtra(KEY_PARTICIPANT_IDS);
        participantIds.add(ChatCamp.getCurrentUser().getId());
        final String groupId = getIntent().getStringExtra(KEY_GROUP_ID);
        final SelectableUserList userList = findViewById(R.id.selectable_user_list);
        loadingView = findViewById(R.id.loading_view);
        EditText searchUserEt = findViewById(R.id.et_search_user);
        userList.setLoadingView(loadingView);
        searchUserEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                userList.search(editable.toString(), participantIds);
            }
        });



        userList.search(null, participantIds);
        Button addParticipant = findViewById(R.id.button_add_participant);
        addParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<User> users = userList.getSelectedUsers();
                final String [] invitedUserIds = new String[users.size()];
                for(int i = 0; i < users.size(); ++i) {
                    invitedUserIds[i] = users.get(i).getId();
                }
                GroupChannel.get(groupId, new GroupChannel.GetListener() {
                    @Override
                    public void onResult(GroupChannel groupChannel, ChatCampException e) {

                        groupChannel.inviteParticipants(invitedUserIds, new GroupChannel.InviteParticipantsListener() {
                            @Override
                            public void onResult(ChatCampException e) {
                                if(e == null) {
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
