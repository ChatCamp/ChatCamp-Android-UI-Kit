package com.chatcamp.uikit.messages.typing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.utils.CircleTransform;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import io.chatcamp.sdk.Participant;

/**
 * Created by shubhamdhabhai on 30/04/18.
 */

public class DefaultTypingFactory extends TypingFactory<DefaultTypingFactory.DefaultTypingHolder> {

    private final Context context;

    public DefaultTypingFactory(Context context) {
        this.context = context;
    }

    @Override
    public DefaultTypingHolder createView(ViewGroup parent, LayoutInflater layoutInflater) {
        return new DefaultTypingHolder(layoutInflater.inflate(R.layout.layout_typing, parent, true));
    }

    @Override
    public void bindView(DefaultTypingHolder typingHolder, List<Participant> typingUsers) {
        if(typingUsers.size() > 0) {
            Picasso.with(context).load(typingUsers.get(0).getAvatarUrl()).transform(new CircleTransform()).error(R.drawable.icon_default_contact).into(typingHolder.avatarView);
            typingHolder.indicatorView.show();
        } else {
            typingHolder.indicatorView.hide();
        }
    }

    public static class DefaultTypingHolder extends TypingFactory.TypingHolder {
        AVLoadingIndicatorView indicatorView;
        ImageView avatarView;

        public DefaultTypingHolder(View view) {
            //TODO add image of the user (in future we can combine the images like ( ( ( ) ...))
            indicatorView = view.findViewById(R.id.indication);
            avatarView = view.findViewById(R.id.messageUserAvatar);
        }
    }
}
