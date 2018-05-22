package com.chatcamp.uikit.messages.typing;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chatcamp.uikit.R;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import io.chatcamp.sdk.Participant;

/**
 * Created by shubhamdhabhai on 30/04/18.
 */

public class DefaultTypingFactory extends TypingFactory<DefaultTypingFactory.DefaultTypingHolder> {

    @Override
    public DefaultTypingHolder createView(ViewGroup parent, LayoutInflater layoutInflater) {
        return new DefaultTypingHolder(layoutInflater.inflate(R.layout.layout_typing, parent, true));
    }

    @Override
    public void bindView(DefaultTypingHolder typingHolder, List<Participant> typingUsers) {
        if(typingUsers.size() > 0) {
            typingHolder.indicatorView.show();
        } else {
            typingHolder.indicatorView.hide();
        }
    }

    public static class DefaultTypingHolder extends TypingFactory.TypingHolder {
        AVLoadingIndicatorView indicatorView;

        public DefaultTypingHolder(View view) {
            //TODO add image of the user (in future we can combine the images like ( ( ( ) ...))
            indicatorView = view.findViewById(R.id.indication);
        }
    }
}
