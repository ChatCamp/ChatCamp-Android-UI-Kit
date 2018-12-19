package com.chatcamp.uikit.messages.sender;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Message;

public class AskPaymentSender extends AttachmentSender {

    private WeakReference<Context> objectWeakReference;


    public AskPaymentSender(@NonNull Context context, @NonNull BaseChannel channel, @NonNull String title, @NonNull int drawableRes) {
        super(channel, title, drawableRes);
        objectWeakReference = new WeakReference<>(context);
    }

    @Override
    public void clickSend() {
        //show dialog
        if (objectWeakReference.get() == null) {
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(objectWeakReference.get());
        alertDialog.setTitle("Payment");
        alertDialog.setMessage("Enter Amount");

        final EditText input = new EditText(objectWeakReference.get());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(input.getText())) {
                            input.setError("Payment can not be empty");
                        } else {
                            try {
                                Integer.valueOf(input.getText().toString());
                                String userName = ChatCamp.getCurrentUser().getDisplayName();
                                PaymentMeta meta = new PaymentMeta();
                                meta.setName(userName);
                                meta.setValue(input.getText().toString());
                                channel.sendMessage("", meta, null, new BaseChannel.SendMessageListener() {
                                    @Override
                                    public void onSent(Message message, ChatCampException e) {
                                        // do nothing
                                    }
                                });
                            } catch (Exception e) {
                                input.setError("Payment format is wrong");
                            }
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

    public class PaymentMeta {

        public static final String TYPE = "ask_payment";

        private String value;
        private String name;
        private String type = TYPE;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }
    }
}
