package com.chatcamp.uikit.messages.messagetypes;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.utils.TextViewFont;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 21/04/18.
 */

public class TextMessageFactory extends MessageFactory<TextMessageFactory.TextMessageHolder> {

    @Override
    public boolean isBindable(Message message) {
        return message.getType().equals("text");
    }

    @Override
    public TextMessageHolder createMessageHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        View view = layoutInflater.inflate(R.layout.layout_message_text, cellView, true);
        TextViewFont textView = view.findViewById(R.id.messageTextView);
        Drawable backgroundDrawable = isMe ? messageStyle.getOutcomingBubbleDrawable() :
                messageStyle.getIncomingBubbleDrawable();
        int paddingTop = isMe ? messageStyle.getOutcomingDefaultBubblePaddingTop() : messageStyle.getIncomingDefaultBubblePaddingTop();
        int paddingBottom = isMe ? messageStyle.getOutcomingDefaultBubblePaddingBottom() : messageStyle.getIncomingDefaultBubblePaddingBottom();
        int paddingLeft = isMe ? messageStyle.getOutcomingDefaultBubblePaddingLeft() : messageStyle.getIncomingDefaultBubblePaddingLeft();
        int paddingRight = isMe ? messageStyle.getOutcomingDefaultBubblePaddingRight() : messageStyle.getIncomingDefaultBubblePaddingRight();
        int textColor = isMe ? messageStyle.getOutcomingTextColor() : messageStyle.getIncomingTextColor();
        int textSize = isMe ? messageStyle.getOutcomingTextSize() : messageStyle.getIncomingTextSize();
        int textStyle = isMe ? messageStyle.getOutcomingTextStyle() : messageStyle.getIncomingTextStyle();
        int textLinkColor = isMe ? messageStyle.getOutcomingTextLinkColor() : messageStyle.getIncomingTextLinkColor();
        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setTypeface(textView.getTypeface(), textStyle);
        textView.setAutoLinkMask(messageStyle.getTextAutoLinkMask());
        textView.setLinkTextColor(textLinkColor);
        textView.setTextColor(textColor);
        if(!TextUtils.isEmpty(messageStyle.getCustomFont())) {
            textView.setCustomFont(messageStyle.getCustomFont());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            textView.setBackground(backgroundDrawable);
        } else {
            textView.setBackgroundDrawable(backgroundDrawable);
        }
        return new TextMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(TextMessageHolder cellHolder, Message message) {
        Drawable backgroundDrawable = cellHolder.messageText.getBackground().mutate();
        cellHolder.messageText.setText(message.getText());
        boolean isFirstMessage = messageSpecs.isFirstMessage;
        float cornerRadius = cellHolder.messageText.getContext()
                .getResources().getDimensionPixelSize(R.dimen.message_bubble_corners_radius);

        if (isFirstMessage) {
            float[] cornerRadii = messageSpecs.isMe ? new float[]{cornerRadius, cornerRadius,
                    0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius}
                    : new float[]{0f, 0f, cornerRadius, cornerRadius,
                    cornerRadius, cornerRadius, cornerRadius, cornerRadius};
            ((GradientDrawable) backgroundDrawable).setCornerRadii(cornerRadii);
        } else {
            ((GradientDrawable) backgroundDrawable).setCornerRadius(cornerRadius);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            cellHolder.messageText.setBackground(backgroundDrawable);
        } else {
            cellHolder.messageText.setBackgroundDrawable(backgroundDrawable);
        }
    }

    public static class TextMessageHolder extends MessageFactory.MessageHolder {
        TextView messageText;

        public TextMessageHolder(View view) {
            messageText = view.findViewById(R.id.messageTextView);
        }

    }
}
