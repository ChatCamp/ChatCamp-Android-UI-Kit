
package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.messages.sender.AttachmentSender;
import com.chatcamp.uikit.messages.sender.DefaultTextSender;
import com.chatcamp.uikit.messages.sender.TextSender;
import com.chatcamp.uikit.messages.sender.VoiceSender;

import org.apmem.tools.layouts.FlowLayout;

import java.lang.reflect.Field;
import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.GroupChannel;
import io.chatcamp.sdk.Participant;

/**
 * Component for input outcoming messages
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MessageInput extends RelativeLayout
        implements View.OnClickListener, TextWatcher {

    protected EditText messageInput;
    protected ImageButton messageSendButton;
    protected ImageButton attachmentButton;
    protected Space sendButtonSpace, attachmentButtonSpace;
    protected ImageButton voiceMessageSendButton;

    private CharSequence input;
    private AttachmentsListener attachmentsListener;

    @NonNull
    private BaseChannel channel;
    @NonNull
    private TextSender textSender;
    private List<AttachmentSender> attachmentSenderList;
    private OnSendCLickedListener sendClickListener;

    private VoiceSender voiceSender;

    private boolean isRecording;
    private MessageInputStyle style;

    private OnUserUnblockedListener onUserUnblockedListener;
    private Participant otherParticipant;

    public MessageInput(Context context) {
        super(context);
        init(context);
    }

    public MessageInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MessageInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    /**
     * Sets callback for 'add' button.
     *
     * @param attachmentsListener input callback
     */
    public void setAttachmentsListener(AttachmentsListener attachmentsListener) {
        this.attachmentsListener = attachmentsListener;
    }

    public void setOnUserOnUnblockedListener(OnUserUnblockedListener listener) {
        this.onUserUnblockedListener = listener;
    }

    /**
     * Returns EditText for messages input
     *
     * @return EditText
     */
    public EditText getInputEditText() {
        return messageInput;
    }

    /**
     * Returns `submit` button
     *
     * @return ImageButton
     */
    public ImageButton getButton() {
        return messageSendButton;
    }

    public ImageButton getVoiceMessageSendButton() {
        return voiceMessageSendButton;
    }

    @Override
    public void onClick(final View view) {
        if (channel != null && channel.isGroupChannel()) {
            GroupChannel.get(channel.getId(), new GroupChannel.GetListener() {
                @Override
                public void onResult(GroupChannel groupChannel, ChatCampException e) {
                    boolean isBlocked = false;
                    channel = groupChannel;
                    boolean isOneToOneConversation = false;
                    if (groupChannel.getParticipants().size() == 2) {
                        isOneToOneConversation = true;
                    }
                    if (isOneToOneConversation) {
                        List<Participant> participants = ((GroupChannel) channel).getParticipants();
                        for (Participant participant : participants) {
                            if (!participant.getId().equals(ChatCamp.getCurrentUser().getId())) {
                                otherParticipant = participant;
                                isBlocked = otherParticipant.isBlockedByMe();
                            }
                        }
                    }
                    if (isBlocked) {
                        // show a dialog that You cannot send a message, You have blocked {user},
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("User has been blocked by you. Unblock user to continue chatting with them.");
                        builder.setCancelable(true);

                        builder.setPositiveButton(
                                "Unblock",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (otherParticipant != null) {
                                            ChatCamp.unBlockUser(otherParticipant.getId(), new ChatCamp.OnUserUnBlockListener() {
                                                @Override
                                                public void onUserUnBlocked(Participant participant, ChatCampException exception) {
                                                    if (onUserUnblockedListener != null) {
                                                        onUserUnblockedListener.onUserUnblocked(participant);
                                                    }
                                                }
                                            });
                                        }
                                        dialog.cancel();
                                    }

                                });

                        builder.setNegativeButton(
                                "Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();
                        return;
                    }
                    performOnClick(view);
                }
            });
        } else {
            performOnClick(view);
        }
    }

    private void performOnClick(View view) {
        int id = view.getId();
        if (id == R.id.messageSendButton) {
            if (sendClickListener != null) {
                sendClickListener.onSendClicked(input.toString());
            }
            textSender.sendMessage(input.toString());
            messageInput.setText("");
        } else if (id == R.id.voiceMessageSendButton) {
            if (!isRecording) {
                voiceSender.startRecording();
                voiceMessageSendButton.setImageDrawable(style.getVoiceMessageButtonMuteIcon());
            } else {
                voiceSender.clickSend();
                voiceMessageSendButton.setImageDrawable(style.getVoiceMessageButtonIcon());
            }
            isRecording = !isRecording;
        } else if (id == R.id.attachmentButton) {
            boolean continueExecution = true;
            if (attachmentsListener != null) {
                continueExecution = attachmentsListener.onAddAttachments();
            }
            if (continueExecution && attachmentSenderList != null && attachmentSenderList.size() > 0) {
                final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                // inflate the custom popup layout
                final View bottomSheetView = layoutInflater.inflate(R.layout.layout_attachment, null, false);
                FlowLayout container = bottomSheetView.findViewById(R.id.ll_attachment_sender_container);
                if (attachmentSenderList != null) {
                    for (final AttachmentSender attachmentSender : attachmentSenderList) {
                        final View attachmentSenderView = layoutInflater.inflate(R.layout.layout_attachment_sender_item, null, false);
                        ImageView image = attachmentSenderView.findViewById(R.id.iv_sender_icon);
                        TextView title = attachmentSenderView.findViewById(R.id.tv_sender_title);
                        image.setImageResource(attachmentSender.getDrawableRes());
                        title.setText(attachmentSender.getTitle());
                        attachmentSenderView.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.hide();
                                attachmentSender.clickSend();
                            }
                        });
                        container.addView(attachmentSenderView);
                    }

                    dialog.setContentView(bottomSheetView);
                    dialog.show();
                }
            }
        }
    }

    /**
     * This method is called to notify you that, within s,
     * the count characters beginning at start have just replaced old text that had length before
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        input = s;
        messageSendButton.setEnabled(input.length() > 0);
    }

    /**
     * This method is called to notify you that, within s,
     * the count characters beginning at start are about to be replaced by new text with length after.
     */
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //do nothing
    }

    /**
     * This method is called to notify you that, somewhere within s, the text has been changed.
     */
    @Override
    public void afterTextChanged(Editable editable) {
        //do nothing
        //TODO Do we need typing indicator for Openchannel also
        if (channel instanceof GroupChannel) {
            String message = editable.toString().trim();
            if (message.length() > 0) {
                ((GroupChannel) channel).startTyping();
            } else {
                ((GroupChannel) channel).stopTyping();
            }
        }
    }

    public void setChannel(@NonNull BaseChannel channel) {
        this.channel = channel;
        textSender = new DefaultTextSender(channel);
    }

    public void setTextSender(@NonNull TextSender textSender) {
        this.textSender = textSender;
    }

    public void setVoiceSender(@NonNull VoiceSender voiceSender) {
        this.voiceSender = voiceSender;
    }

    public void setAttachmentSenderList(List<AttachmentSender> attachmentSenderList) {
        this.attachmentSenderList = attachmentSenderList;
    }

    public void setOnSendClickListener(OnSendCLickedListener sendClickListener) {
        this.sendClickListener = sendClickListener;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        for (AttachmentSender attachmentSender : attachmentSenderList) {
            attachmentSender.onActivityResult(requestCode, resultCode, dataFile);
        }
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        for (AttachmentSender attachmentSender : attachmentSenderList) {
            attachmentSender.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (voiceSender != null) {
            voiceSender.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        init(context);
        style = MessageInputStyle.parse(context, attrs);

        this.messageInput.setMaxLines(style.getInputMaxLines());
        this.messageInput.setHint(style.getInputHint());
        this.messageInput.setText(style.getInputText());
        this.messageInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getInputTextSize());
        this.messageInput.setTextColor(style.getInputTextColor());
        this.messageInput.setHintTextColor(style.getInputHintColor());
        ViewCompat.setBackground(this.messageInput, style.getInputBackground());
        setCursor(style.getInputCursorDrawable());

        this.attachmentButton.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
        this.attachmentButton.setImageDrawable(style.getAttachmentButtonIcon());
        this.attachmentButton.getLayoutParams().width = style.getAttachmentButtonWidth();
        this.attachmentButton.getLayoutParams().height = style.getAttachmentButtonHeight();
        ViewCompat.setBackground(this.attachmentButton, style.getAttachmentButtonBackground());

        this.attachmentButtonSpace.setVisibility(style.showAttachmentButton() ? VISIBLE : GONE);
        this.attachmentButtonSpace.getLayoutParams().width = style.getAttachmentButtonMargin();

        this.messageSendButton.setImageDrawable(style.getInputButtonIcon());
        this.messageSendButton.getLayoutParams().width = style.getInputButtonWidth();
        this.messageSendButton.getLayoutParams().height = style.getInputButtonHeight();
        ViewCompat.setBackground(messageSendButton, style.getInputButtonBackground());
        this.sendButtonSpace.getLayoutParams().width = style.getInputButtonMargin();
        if (style.isShowVoiceMessageButton()) {
            this.voiceMessageSendButton.setVisibility(VISIBLE);
            this.voiceMessageSendButton.setImageDrawable(style.getVoiceMessageButtonIcon());
            this.voiceMessageSendButton.getLayoutParams().width = style.getInputButtonWidth();
            this.voiceMessageSendButton.getLayoutParams().height = style.getInputButtonHeight();
            ViewCompat.setBackground(voiceMessageSendButton, style.getInputButtonBackground());
        } else {
            this.voiceMessageSendButton.setVisibility(GONE);
        }

        if (getPaddingLeft() == 0
                && getPaddingRight() == 0
                && getPaddingTop() == 0
                && getPaddingBottom() == 0) {
            setPadding(
                    style.getInputDefaultPaddingLeft(),
                    style.getInputDefaultPaddingTop(),
                    style.getInputDefaultPaddingRight(),
                    style.getInputDefaultPaddingBottom()
            );
        }
    }

    private void init(Context context) {
        inflate(context, R.layout.view_message_input, this);

        messageInput = (EditText) findViewById(R.id.messageInput);
        messageSendButton = (ImageButton) findViewById(R.id.messageSendButton);
        attachmentButton = (ImageButton) findViewById(R.id.attachmentButton);
        sendButtonSpace = (Space) findViewById(R.id.sendButtonSpace);
        attachmentButtonSpace = (Space) findViewById(R.id.attachmentButtonSpace);
        voiceMessageSendButton = findViewById(R.id.voiceMessageSendButton);

        messageSendButton.setOnClickListener(this);
        attachmentButton.setOnClickListener(this);
        voiceMessageSendButton.setOnClickListener(this);
        messageInput.addTextChangedListener(this);
        messageInput.setText("");
    }

    private void setCursor(Drawable drawable) {
        if (drawable == null) return;

        try {
            final Field drawableResField = TextView.class.getDeclaredField("mCursorDrawableRes");
            drawableResField.setAccessible(true);

            final Object drawableFieldOwner;
            final Class<?> drawableFieldClass;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                drawableFieldOwner = this.messageInput;
                drawableFieldClass = TextView.class;
            } else {
                final Field editorField = TextView.class.getDeclaredField("mEditor");
                editorField.setAccessible(true);
                drawableFieldOwner = editorField.get(this.messageInput);
                drawableFieldClass = drawableFieldOwner.getClass();
            }
            final Field drawableField = drawableFieldClass.getDeclaredField("mCursorDrawable");
            drawableField.setAccessible(true);
            drawableField.set(drawableFieldOwner, new Drawable[]{drawable, drawable});
        } catch (Exception ignored) {
        }
    }

    /**
     * Interface definition for a callback to be invoked when user presses 'add' button
     */
    public interface AttachmentsListener {

        /**
         * Fires when user presses 'add' button.
         */
        boolean onAddAttachments();
    }

    public interface OnSendCLickedListener {
        void onSendClicked(String text);
    }

    public interface OnUserUnblockedListener {
        void onUserUnblocked(Participant participant);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (channel != null && channel.isGroupChannel() && visibility == VISIBLE) {
            GroupChannel.get(channel.getId(), new GroupChannel.GetListener() {
                @Override
                public void onResult(GroupChannel groupChannel, ChatCampException e) {
                    channel = groupChannel;
                }
            });
        }
        if (visibility != VISIBLE && voiceSender != null) {
            voiceSender.freeResources();
        }
    }
}
