package com.chatcamp.uikit.user;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chatcamp.uikit.R;

import io.chatcamp.sdk.User;

public class BlockedUserListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_user_list);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Blocked Users");
        final ProgressBar progressBar = findViewById(R.id.progress_bar);
        final BlockedUserList blockedUserList = findViewById(R.id.blocked_user_list);
        final TextView placeholder = findViewById(R.id.tv_place_holder);
        blockedUserList.setOnBlockedUsersLoadedListener(new BlockedUserList.OnBlockedUsersLoadedListener() {
            @Override
            public void onBlockUsersLoaded() {
                progressBar.setVisibility(View.GONE);
                if(blockedUserList.getAdapter().getItemCount() == 0) {
                    placeholder.setVisibility(View.VISIBLE);
                } else {
                    placeholder.setVisibility(View.GONE);
                }
            }
        });
        blockedUserList.initialize();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
