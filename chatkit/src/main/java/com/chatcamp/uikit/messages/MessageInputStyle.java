package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.Style;

/**
 * Style for MessageInputStyle customization by xml attributes
 */
@SuppressWarnings("WeakerAccess")
class MessageInputStyle extends Style {

    private static final int DEFAULT_MAX_LINES = 5;

    private boolean showAttachmentButton;

    private int attachmentButtonBackground;
    private int attachmentButtonDefaultBgColor;
    private int attachmentButtonDefaultBgPressedColor;
    private int attachmentButtonDefaultBgDisabledColor;

    private int attachmentButtonIcon;
    private int attachmentButtonDefaultIconColor;
    private int attachmentButtonDefaultIconPressedColor;
    private int attachmentButtonDefaultIconDisabledColor;

    private int attachmentButtonWidth;
    private int attachmentButtonHeight;
    private int attachmentButtonMargin;

    private int inputButtonBackground;
    private int inputButtonDefaultBgColor;
    private int inputButtonDefaultBgPressedColor;
    private int inputButtonDefaultBgDisabledColor;

    private int inputButtonIcon;
    private int inputButtonDefaultIconColor;
    private int inputButtonDefaultIconPressedColor;
    private int inputButtonDefaultIconDisabledColor;

    private int inputButtonWidth;
    private int inputButtonHeight;
    private int inputButtonMargin;

    private int inputMaxLines;
    private String inputHint;
    private String inputText;

    private int inputTextSize;
    private int inputTextColor;
    private int inputHintColor;

    private Drawable inputBackground;
    private Drawable inputCursorDrawable;

    private int inputDefaultPaddingLeft;
    private int inputDefaultPaddingRight;
    private int inputDefaultPaddingTop;
    private int inputDefaultPaddingBottom;

    private int voiceMessageButtonBackground;
    private int voiceMessageButtonDefaultBgColor;
    private int voiceMessageButtonDefaultBgPressedColor;
    private int voiceMessageButtonDefaultBgDisabledColor;

    private int voiceMessageButtonIcon;
    private int voiceMessageButtonMuteIcon;
    private int voiceMessageButtonDefaultIconColor;
    private int voiceMessageButtonDefaultIconPressedColor;
    private int voiceMessageButtonDefaultIconDisabledColor;

    private int voiceMessageButtonWidth;
    private int voiceMessageButtonHeight;
    private int voiceMessageButtonMargin;
    private boolean showVoiceMessageButton;

    static MessageInputStyle parse(Context context, AttributeSet attrs) {
        MessageInputStyle style = new MessageInputStyle(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MessageInput);

        style.showAttachmentButton = typedArray.getBoolean(R.styleable.MessageInput_showAttachmentButton, false);

        style.attachmentButtonBackground = typedArray.getResourceId(R.styleable.MessageInput_attachmentButtonBackground, -1);
        style.attachmentButtonDefaultBgColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultBgColor,
                style.getColor(R.color.chatCampTextWhite));
        style.attachmentButtonDefaultBgPressedColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultBgPressedColor,
                style.getColor(R.color.chatCampTextWhite));
        style.attachmentButtonDefaultBgDisabledColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultBgDisabledColor,
                style.getColor(R.color.transparent));

        style.attachmentButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_attachmentButtonIcon, -1);
        style.attachmentButtonDefaultIconColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultIconColor,
                style.getColor(R.color.chatCampColorPrimary));
        style.attachmentButtonDefaultIconPressedColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultIconPressedColor,
                style.getColor(R.color.chatCampColorPrimary));
        style.attachmentButtonDefaultIconDisabledColor = typedArray.getColor(R.styleable.MessageInput_attachmentButtonDefaultIconDisabledColor,
                style.getColor(R.color.chatCampColorPrimary));

        style.attachmentButtonWidth = typedArray.getDimensionPixelSize(R.styleable.MessageInput_attachmentButtonWidth, style.getDimension(R.dimen.input_button_width));
        style.attachmentButtonHeight = typedArray.getDimensionPixelSize(R.styleable.MessageInput_attachmentButtonHeight, style.getDimension(R.dimen.input_button_height));
        style.attachmentButtonMargin = typedArray.getDimensionPixelSize(R.styleable.MessageInput_attachmentButtonMargin, style.getDimension(R.dimen.input_button_margin));

        style.inputButtonBackground = typedArray.getResourceId(R.styleable.MessageInput_inputButtonBackground, -1);
        style.inputButtonDefaultBgColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultBgColor,
                style.getColor(R.color.chatCampColorPrimary));
        style.inputButtonDefaultBgPressedColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultBgPressedColor,
                style.getColor(R.color.chatCampColorPrimary));
        style.inputButtonDefaultBgDisabledColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultBgDisabledColor,
                style.getColor(R.color.chatCampTextWhite));

        style.inputButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_inputButtonIcon, -1);
        style.inputButtonDefaultIconColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultIconColor,
                style.getColor(R.color.chatCampTextWhite));
        style.inputButtonDefaultIconPressedColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultIconPressedColor,
                style.getColor(R.color.white));
        style.inputButtonDefaultIconDisabledColor = typedArray.getColor(R.styleable.MessageInput_inputButtonDefaultIconDisabledColor,
                style.getColor(R.color.chatCampGrayColor));

        style.inputButtonWidth = typedArray.getDimensionPixelSize(R.styleable.MessageInput_inputButtonWidth, style.getDimension(R.dimen.input_button_width));
        style.inputButtonHeight = typedArray.getDimensionPixelSize(R.styleable.MessageInput_inputButtonHeight, style.getDimension(R.dimen.input_button_height));
        style.inputButtonMargin = typedArray.getDimensionPixelSize(R.styleable.MessageInput_inputButtonMargin, style.getDimension(R.dimen.input_button_margin));

        style.inputMaxLines = typedArray.getInt(R.styleable.MessageInput_inputMaxLines, DEFAULT_MAX_LINES);
        style.inputHint = typedArray.getString(R.styleable.MessageInput_inputHint);
        style.inputText = typedArray.getString(R.styleable.MessageInput_inputText);

        style.inputTextSize = typedArray.getDimensionPixelSize(R.styleable.MessageInput_inputTextSize, style.getDimension(R.dimen.input_text_size));
        style.inputTextColor = typedArray.getColor(R.styleable.MessageInput_inputTextColor, style.getColor(R.color.chatCampTextBlack));
        style.inputHintColor = typedArray.getColor(R.styleable.MessageInput_inputHintColor, style.getColor(R.color.chatCampTextBlack));

        style.inputBackground = typedArray.getDrawable(R.styleable.MessageInput_inputBackground);
        style.inputCursorDrawable = typedArray.getDrawable(R.styleable.MessageInput_inputCursorDrawable);

        style.voiceMessageButtonBackground = typedArray.getResourceId(R.styleable.MessageInput_voiceMessageButtonBackground, -1);
        style.voiceMessageButtonDefaultBgColor = typedArray.getColor(R.styleable.MessageInput_voiceMessageButtonDefaultBgColor,
                style.getColor(R.color.chatCampColorPrimary));
        style.voiceMessageButtonDefaultBgPressedColor = typedArray.getColor(R.styleable.MessageInput_voiceMessageButtonDefaultBgPressedColor,
                style.getColor(R.color.chatCampColorPrimary));
        style.voiceMessageButtonDefaultBgDisabledColor = typedArray.getColor(R.styleable.MessageInput_voiceMessageButtonDefaultBgDisabledColor,
                style.getColor(R.color.chatCampTextWhite));

        style.voiceMessageButtonIcon = typedArray.getResourceId(R.styleable.MessageInput_voiceMessageButtonIcon, -1);
        style.voiceMessageButtonMuteIcon = typedArray.getResourceId(R.styleable.MessageInput_voiceMessageButtonMuteIcon, -1);
        style.voiceMessageButtonDefaultIconColor = typedArray.getColor(R.styleable.MessageInput_voiceMessageButtonDefaultIconColor,
                style.getColor(R.color.white));
        style.voiceMessageButtonDefaultIconPressedColor = typedArray.getColor(R.styleable.MessageInput_voiceMessageButtonDefaultIconPressedColor,
                style.getColor(R.color.white));
        style.voiceMessageButtonDefaultIconDisabledColor = typedArray.getColor(R.styleable.MessageInput_voiceMessageButtonDefaultIconDisabledColor,
                style.getColor(R.color.chatCampGrayColor));

        style.voiceMessageButtonWidth = typedArray.getDimensionPixelSize(R.styleable.MessageInput_voiceMessageButtonWidth, style.getDimension(R.dimen.input_button_width));
        style.voiceMessageButtonHeight = typedArray.getDimensionPixelSize(R.styleable.MessageInput_voiceMessageButtonHeight, style.getDimension(R.dimen.input_button_height));
        style.voiceMessageButtonMargin = typedArray.getDimensionPixelSize(R.styleable.MessageInput_voiceMessageButtonMargin, style.getDimension(R.dimen.input_button_margin));
        style.showVoiceMessageButton = typedArray.getBoolean(R.styleable.MessageInput_showVoiceMessageButton, false);

        typedArray.recycle();

        style.inputDefaultPaddingLeft = style.getDimension(R.dimen.input_padding_left);
        style.inputDefaultPaddingRight = style.getDimension(R.dimen.input_padding_right);
        style.inputDefaultPaddingTop = style.getDimension(R.dimen.input_padding_top);
        style.inputDefaultPaddingBottom = style.getDimension(R.dimen.input_padding_bottom);

        return style;
    }

    private MessageInputStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Drawable getSelector(@ColorInt int normalColor, @ColorInt int pressedColor,
                                 @ColorInt int disabledColor, @DrawableRes int shape) {

        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_enabled, -android.R.attr.state_pressed},
                                new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_enabled}
                        },
                        new int[]{normalColor, pressedColor, disabledColor}
                ));
        return drawable;
    }

    protected boolean showAttachmentButton() {
        return showAttachmentButton;
    }

    protected Drawable getAttachmentButtonBackground() {
        if (attachmentButtonBackground == -1) {
            return getSelector(attachmentButtonDefaultBgColor, attachmentButtonDefaultBgPressedColor,
                    attachmentButtonDefaultBgDisabledColor, R.drawable.mask);
        } else {
            return getDrawable(attachmentButtonBackground);
        }
    }

    protected Drawable getAttachmentButtonIcon() {
        if (attachmentButtonIcon == -1) {
            return getSelector(attachmentButtonDefaultIconColor, attachmentButtonDefaultIconPressedColor,
                    attachmentButtonDefaultIconDisabledColor, R.drawable.ic_add_attachment);
        } else {
            return getDrawable(attachmentButtonIcon);
        }
    }

    protected int getAttachmentButtonWidth() {
        return attachmentButtonWidth;
    }

    protected int getAttachmentButtonHeight() {
        return attachmentButtonHeight;
    }

    protected int getAttachmentButtonMargin() {
        return attachmentButtonMargin;
    }

    protected Drawable getInputButtonBackground() {
        if (inputButtonBackground == -1) {
            return getSelector(inputButtonDefaultBgColor, inputButtonDefaultBgPressedColor,
                    inputButtonDefaultBgDisabledColor, R.drawable.mask);
        } else {
            return getDrawable(inputButtonBackground);
        }
    }

    protected Drawable getInputButtonIcon() {
        if (inputButtonIcon == -1) {
            return getSelector(inputButtonDefaultIconColor, inputButtonDefaultIconPressedColor,
                    inputButtonDefaultIconDisabledColor, R.drawable.ic_send);
        } else {
            return getDrawable(inputButtonIcon);
        }
    }

    protected int getInputButtonMargin() {
        return inputButtonMargin;
    }

    protected int getInputButtonWidth() {
        return inputButtonWidth;
    }

    protected int getInputButtonHeight() {
        return inputButtonHeight;
    }

    protected int getInputMaxLines() {
        return inputMaxLines;
    }

    protected String getInputHint() {
        return inputHint;
    }

    protected String getInputText() {
        return inputText;
    }

    protected int getInputTextSize() {
        return inputTextSize;
    }

    protected int getInputTextColor() {
        return inputTextColor;
    }

    protected int getInputHintColor() {
        return inputHintColor;
    }

    protected Drawable getInputBackground() {
        return inputBackground;
    }

    protected Drawable getInputCursorDrawable() {
        return inputCursorDrawable;
    }

    protected int getInputDefaultPaddingLeft() {
        return inputDefaultPaddingLeft;
    }

    protected int getInputDefaultPaddingRight() {
        return inputDefaultPaddingRight;
    }

    protected int getInputDefaultPaddingTop() {
        return inputDefaultPaddingTop;
    }

    protected int getInputDefaultPaddingBottom() {
        return inputDefaultPaddingBottom;
    }

    public Drawable getVoiceMessageButtonBackground() {
        if (inputButtonBackground == -1) {
            return getSelector(inputButtonDefaultBgColor, inputButtonDefaultBgPressedColor,
                    inputButtonDefaultBgDisabledColor, R.drawable.mask);
        } else {
            return getDrawable(inputButtonBackground);
        }
    }

    public int getVoiceMessageButtonDefaultBgColor() {
        return voiceMessageButtonDefaultBgColor;
    }

    public int getVoiceMessageButtonDefaultBgPressedColor() {
        return voiceMessageButtonDefaultBgPressedColor;
    }

    public int getVoiceMessageButtonDefaultBgDisabledColor() {
        return voiceMessageButtonDefaultBgDisabledColor;
    }

    public Drawable getVoiceMessageButtonIcon() {
        if (voiceMessageButtonIcon == -1) {
            return getSelector(inputButtonDefaultIconColor, inputButtonDefaultIconPressedColor,
                    inputButtonDefaultIconDisabledColor, R.drawable.ic_mic);
        } else {
            return getDrawable(voiceMessageButtonIcon);
        }
    }

    public Drawable getVoiceMessageButtonMuteIcon() {
        if (voiceMessageButtonMuteIcon == -1) {
            return getSelector(inputButtonDefaultIconColor, inputButtonDefaultIconPressedColor,
                    inputButtonDefaultIconDisabledColor, R.drawable.ic_mic_mute);
        } else {
            return getDrawable(voiceMessageButtonMuteIcon);
        }
    }

    public int getVoiceMessageButtonDefaultIconColor() {
        return voiceMessageButtonDefaultIconColor;
    }

    public int getVoiceMessageButtonDefaultIconPressedColor() {
        return voiceMessageButtonDefaultIconPressedColor;
    }

    public int getVoiceMessageButtonDefaultIconDisabledColor() {
        return voiceMessageButtonDefaultIconDisabledColor;
    }

    public int getVoiceMessageButtonWidth() {
        return voiceMessageButtonWidth;
    }

    public int getVoiceMessageButtonHeight() {
        return voiceMessageButtonHeight;
    }

    public int getVoiceMessageButtonMargin() {
        return voiceMessageButtonMargin;
    }

    public boolean isShowVoiceMessageButton() {
        return showVoiceMessageButton;
    }

}
