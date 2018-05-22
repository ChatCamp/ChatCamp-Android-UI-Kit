
package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.Space;
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

import org.apmem.tools.layouts.FlowLayout;

import java.lang.reflect.Field;
import java.util.List;

import io.chatcamp.sdk.BaseChannel;
import io.chatcamp.sdk.GroupChannel;

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

    private CharSequence input;
    private AttachmentsListener attachmentsListener;

    @NonNull
    private BaseChannel channel;
    @NonNull
    private TextSender textSender;
    private List<AttachmentSender> attachmentSenderList;
    private OnSendCLickedListener sendClickListener;

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

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.messageSendButton) {
            if (sendClickListener != null) {
                sendClickListener.onSendClicked(input.toString());
            }
            textSender.sendMessage(input.toString());
            messageInput.setText("");
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
    }

    private void init(Context context, AttributeSet attrs) {
        init(context);
        MessageInputStyle style = MessageInputStyle.parse(context, attrs);

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

        messageSendButton.setOnClickListener(this);
        attachmentButton.setOnClickListener(this);
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
}
