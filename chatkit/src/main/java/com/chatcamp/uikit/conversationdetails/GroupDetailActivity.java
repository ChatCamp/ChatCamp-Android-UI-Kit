package com.chatcamp.uikit.conversationdetails;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.chatcamp.uikit.R;

import java.util.ArrayList;
import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.OpenChannel;
import io.chatcamp.sdk.Participant;

public class GroupDetailActivity extends AppCompatActivity implements GroupDetailAdapter.OnParticipantClickedListener {
    public  static final String KEY_GROUP_ID = "key_group_id";
    public  static final String KEY_IS_GROUP_CHANNEL = "key_is_group_channel";
    public  static final int ADD_PARTICIPANT_CODE = 101;


    private RecyclerView participantRv;
    private ImageView toolbarIv;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private GroupDetailAdapter adapter;
    private GroupChannel groupChannelGlobal;
    private OpenChannel openChannelGlobal;
    private boolean isGroupChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        participantRv  = findViewById(R.id.rv_participant_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        participantRv.setLayoutManager(manager);
        adapter = new GroupDetailAdapter(this);
        adapter.setParticipantClickedListener(this);
        participantRv.setAdapter(adapter);
        toolbarIv = findViewById(R.id.toolbarImage);
        String id = getIntent().getStringExtra(KEY_GROUP_ID);
        isGroupChannel = getIntent().getBooleanExtra(KEY_IS_GROUP_CHANNEL, true);
        adapter.setIsGroupChannel(isGroupChannel);
        if(isGroupChannel) {
            GroupChannel.get(id, new GroupChannel.GetListener() {
                @Override
                public void onResult(GroupChannel groupChannel, ChatCampException e) {
                    populateUi(groupChannel);
                }
            });
        } else {
            OpenChannel.get(id, new OpenChannel.GetListener() {
                @Override
                public void onResult(OpenChannel groupChannel, ChatCampException e) {
                    populateUi(groupChannel);
                }
            });
        }

    }

    private void populateUi(GroupChannel groupChannel) {
        groupChannelGlobal = groupChannel;
        collapsingToolbarLayout.setTitle(groupChannel.getName());
        Picasso.with(this).load(groupChannel.getAvatarUrl())
                .placeholder(R.drawable.icon_default_contact).error(R.drawable.icon_default_contact).into(toolbarIv);
        List<ParticipantView> participantList = new ArrayList<>();
        for(Participant participant : groupChannel.getParticipants()) {
            participantList.add(new ParticipantView(participant));
        }
        adapter.clear();
        adapter.addAll(participantList);
    }

    private void populateUi(OpenChannel openChannel) {
        openChannelGlobal = openChannel;
        collapsingToolbarLayout.setTitle(openChannel.getName());
        Picasso.with(this).load(openChannel.getAvatarUrl())
                .placeholder(R.drawable.icon_default_contact).error(R.drawable.icon_default_contact).into(toolbarIv);
        List<ParticipantView> participantList = new ArrayList<>();
        //TODO how to get participant list in open channel
//        for(Participant participant : openChannel()) {
//            participantList.add(new ParticipantView(participant));
//        }
//        adapter.clear();
//        adapter.addAll(participantList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_group_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        } else if(item.getItemId() == R.id.action_edit_group) {
            //TODO How to update info like group name in open channel
            if(groupChannelGlobal == null) {
                return true;
            }
            //Toast.makeText(this, "Edit group", Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(GroupDetailActivity.this);
            alertDialog.setTitle("Update Group");
            alertDialog.setMessage("Enter Group Name");

            final EditText input = new EditText(GroupDetailActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);

            alertDialog.setPositiveButton("UPDATE",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(TextUtils.isEmpty(input.getText())) {
                                input.setError("Group name could not be empty");
                            } else {
                                groupChannelGlobal.update(input.getText().toString(), null, null, new BaseChannel.UpdateListener() {
                                    @Override
                                    public void onResult(BaseChannel baseChannel, ChatCampException e) {
                                        if(baseChannel instanceof GroupChannel) {
                                            populateUi((GroupChannel) baseChannel);
                                        }
                                    }
                                });
                            }
                        }
                    });

            alertDialog.setNegativeButton("CANCEL",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAddParticipantClicked() {
        Intent intent = new Intent(this, AddParticipantActivity.class);
        if(groupChannelGlobal != null) {
            List<Participant> participants = groupChannelGlobal.getParticipants();
            ArrayList<String> participantIds = new ArrayList<>();
            for(Participant participant : participants) {
                participantIds.add(participant.getId());
            }
            intent.putStringArrayListExtra(AddParticipantActivity.KEY_PARTICIPANT_IDS, participantIds);
            intent.putExtra(AddParticipantActivity.KEY_GROUP_ID, groupChannelGlobal.getId());
            startActivityForResult(intent, ADD_PARTICIPANT_CODE);
        }
    }

    @Override
    public void onParticipantClicked(Participant participant) {
        Toast.makeText(this, participant.getDisplayName() + " Participant Clicked", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra(UserProfileActivity.KEY_PARTICIPANT_ID, participant.getId());
        intent.putExtra(UserProfileActivity.KEY_GROUP_ID, groupChannelGlobal.getId());
        intent.putExtra(UserProfileActivity.KEY_SHOW_BLOCK_OPTION, false);
        startActivity(intent);
    }

    @Override
    public void onExitGroupClicked() {
        //TODO what will happen to the exit button
        groupChannelGlobal.leave(new GroupChannel.LeaveListener() {
            @Override
            public void onResult(ChatCampException e) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == ADD_PARTICIPANT_CODE) {
            if(groupChannelGlobal != null) {
                GroupChannel.get(groupChannelGlobal.getId(), new GroupChannel.GetListener() {
                    @Override
                    public void onResult(GroupChannel groupChannel, ChatCampException e) {
                        populateUi(groupChannel);
                    }
                });
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
