package com.chatcamp.uikit.messages.messagetypes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.database.DbMessageWrapper;
import com.chatcamp.uikit.messages.sender.AskPaymentSender;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.Message;

public class AskPaymentFactory extends MessageFactory<AskPaymentFactory.PaymentMessageHolder> {

    private final BaseChannel channel;

    public AskPaymentFactory(BaseChannel channel) {
        this.channel = channel;
    }


    @Override
    public boolean isBindable(DbMessageWrapper message) {
        if (message.getMetadata() != null && message.getMetadata()
                .get("type") != null && message.getMetadata()
                .get("type").equals(AskPaymentSender.PaymentMeta.TYPE)) {
            return true;
        }
        return false;
    }

    @Override
    public PaymentMessageHolder createMessageHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        ViewGroup.LayoutParams params = cellView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        cellView.setLayoutParams(params);
        View view = layoutInflater.inflate(R.layout.layout_ask_payment, cellView, true);
        return new PaymentMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(PaymentMessageHolder messageHolder, final DbMessageWrapper message) {
        final String amount = message.getMetadata().get("value");
        final String userName = message.getMetadata().get("name");
        messageHolder.descriptionTv.setText(String.format("Please make a payment of ₹ %s to %s", amount, userName));
        messageHolder.makePaymentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!message.getUser().getId().equals(ChatCamp.getCurrentUser().getId())) {
                    MakePaymentMeta makePaymentMeta = new MakePaymentMeta();
                    makePaymentMeta.setName(userName);
                    makePaymentMeta.setValue(amount);
                    channel.sendMessage("", makePaymentMeta, null, new BaseChannel.SendMessageListener() {
                        @Override
                        public void onSent(Message message, ChatCampException e) {
                            // do nothing
                        }
                    });
                }
            }
        });
    }

    public static class PaymentMessageHolder extends MessageFactory.MessageHolder {
        TextView descriptionTv;
        TextView makePaymentTv;

        public PaymentMessageHolder(View view) {
            descriptionTv = view.findViewById(R.id.tv_description);
            makePaymentTv = view.findViewById(R.id.tv_make_payment);
        }

    }

    public class MakePaymentMeta {
        public static final String TYPE = "make_payment";

        private String name;
        private String value;
        private String type = TYPE;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
