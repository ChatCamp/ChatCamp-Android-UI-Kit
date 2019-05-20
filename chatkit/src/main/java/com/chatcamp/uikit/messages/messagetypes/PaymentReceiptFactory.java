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

public class PaymentReceiptFactory extends MessageFactory<PaymentReceiptFactory.PaymentReceiptMessageHolder>{

    @Override
    public boolean isBindable(DbMessageWrapper message) {
        if(message.getMetadata() != null && message.getMetadata()
                .get("type") != null && message.getMetadata()
                .get("type").equals(AskPaymentFactory.MakePaymentMeta.TYPE)) {
            return true;
        }
        return false;
    }

    @Override
    public PaymentReceiptMessageHolder createMessageHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        ViewGroup.LayoutParams params = cellView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        cellView.setLayoutParams(params);
        View view = layoutInflater.inflate(R.layout.layout_payment_receipt, cellView, true);
        return new PaymentReceiptMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(PaymentReceiptMessageHolder messageHolder, final DbMessageWrapper message) {
        final String amount = message.getMetadata().get("value");
        final String userName = message.getMetadata().get("name");
        messageHolder.descriptionTv.setText(String.format("Payment of ₹ %s successful to %s", amount, userName));

    }

    public static class PaymentReceiptMessageHolder extends MessageFactory.MessageHolder {
        TextView descriptionTv;

        public PaymentReceiptMessageHolder(View view) {
            descriptionTv = view.findViewById(R.id.tv_description);
        }

    }
}
