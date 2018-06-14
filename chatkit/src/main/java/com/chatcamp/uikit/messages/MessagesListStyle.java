package com.chatcamp.uikit.messages;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.commons.Style;

/**
 * Style for MessagesListStyle customization by xml attributes
 */
@SuppressWarnings("WeakerAccess")
public class MessagesListStyle extends Style {

    // type of autolink like phone, web, email etc
    private int textAutoLinkMask;

    //incoming

    // text
    private int incomingTextLinkColor;

    private int incomingTextColor;
    private int incomingTextSize;
    private int incomingTextStyle;

    // bubble
    private int incomingBubbleDrawable;
    private int incomingDefaultBubbleColor;
    private int incomingDefaultBubblePressedColor;
    private int incomingDefaultBubbleSelectedColor;

    private int incomingDefaultBubblePaddingLeft;
    private int incomingDefaultBubblePaddingRight;
    private int incomingDefaultBubblePaddingTop;
    private int incomingDefaultBubblePaddingBottom;

    //avatar
    private int incomingAvatarWidth;
    private int incomingAvatarHeight;

    private int incomingAvatarMarginLeft;
    private int incomingAvatarMarginRight;
    private boolean showIncomingAvatar;

    // time
    private int incomingTimeTextColor;
    private int incomingTimeTextSize;
    private int incomingTimeTextStyle;
    private int incomingTimeTextPaddingLeft;
    private int incomingTimeTextPaddingRight;
    private int incomingTimeTextPaddingTop;
    private int incomingTimeTextPaddingBottom;
    private String incomingTimeTextFormat;
    private boolean showIncomingTimeText;


    //read receipt
    private boolean showIncomingReadReceipt;

    // username
    private int incomingUsernameTextColor;
    private int incomingUsernameTextSize;
    private int incomingUsernameTextStyle;
    private int incomingUsernameTextPaddingLeft;
    private int incomingUsernameTextPaddingRight;
    private int incomingUsernameTextPaddingTop;
    private int incomingUsernameTextPaddingBottom;
    private boolean showIncomingUsername;

    //outcoming

    //text
    private int outcomingTextLinkColor;
    private int outcomingTextColor;
    private int outcomingTextSize;
    private int outcomingTextStyle;

    //bubble
    private int outcomingBubbleDrawable;
    private int outcomingDefaultBubbleColor;
    private int outcomingDefaultBubblePressedColor;
    private int outcomingDefaultBubbleSelectedColor;

    private int outcomingDefaultBubblePaddingLeft;
    private int outcomingDefaultBubblePaddingRight;
    private int outcomingDefaultBubblePaddingTop;
    private int outcomingDefaultBubblePaddingBottom;

    //avatar
    private int outcomingAvatarWidth;
    private int outcomingAvatarHeight;
    private int outcomingAvatarMarginLeft;
    private int outcomingAvatarMarginRight;
    private boolean showOutcomingAvatar;

    //time
    private int outcomingTimeTextColor;
    private int outcomingTimeTextSize;
    private int outcomingTimeTextStyle;

    private int outcomingTimeTextPaddingLeft;
    private int outcomingTimeTextPaddingRight;
    private int outcomingTimeTextPaddingTop;
    private int outcomingTimeTextPaddingBottom;
    private String outcomingTimeTextFormat;
    private boolean showOutcomingTimeText;

    //read receipt
    private boolean showOutcomingReadReceipt;

    // username
    private int outcomingUsernameTextColor;
    private int outcomingUsernameTextSize;
    private int outcomingUsernameTextStyle;
    private int outcomingUsernameTextPaddingLeft;
    private int outcomingUsernameTextPaddingRight;
    private int outcomingUsernameTextPaddingTop;
    private int outcomingUsernameTextPaddingBottom;
    private boolean showOutcomingUsername;

    //dates
    private int dateHeaderPadding;
    private String dateHeaderFormat;
    private int dateHeaderTextColor;
    private int dateHeaderTextSize;
    private int dateHeaderTextStyle;

    private boolean showDateHeader;

    // space between messages
    // TODO add this also
    private int messageGap; //  padding bottom in layout_message_my / layout_message_their

    // left space of message
    private int leftMargin;
    //right space of message
    private int rightMargin;

    //read receipt layout
    private int readReceiptReadLayout;
    private int readReeiptUnReadLayout;

    static MessagesListStyle parse(Context context, AttributeSet attrs) {
        MessagesListStyle style = new MessagesListStyle(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MessagesList);

        // type of autolink like phone, web, email etc
        style.textAutoLinkMask = typedArray.getInt(R.styleable.MessagesList_textAutoLink, 0);

        // incoming
        // text
        style.incomingTextLinkColor = typedArray.getColor(R.styleable.MessagesList_incomingTextLinkColor,
                style.getSystemAccentColor());
        style.incomingTextColor = typedArray.getColor(R.styleable.MessagesList_incomingTextColor,
                style.getColor(R.color.dark_grey_two));
        style.incomingTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingTextSize,
                style.getDimension(R.dimen.message_text_size));
        style.incomingTextStyle = typedArray.getInt(R.styleable.MessagesList_incomingTextStyle, Typeface.NORMAL);

        //bubble
        style.incomingBubbleDrawable = typedArray.getResourceId(R.styleable.MessagesList_incomingBubbleDrawable, -1);
        style.incomingDefaultBubbleColor = typedArray.getColor(R.styleable.MessagesList_incomingDefaultBubbleColor,
                style.getColor(R.color.white_two));
        style.incomingDefaultBubblePressedColor = typedArray.getColor(R.styleable.MessagesList_incomingDefaultBubblePressedColor,
                style.getColor(R.color.white_two));
        style.incomingDefaultBubbleSelectedColor = typedArray.getColor(R.styleable.MessagesList_incomingDefaultBubbleSelectedColor,
                style.getColor(R.color.cornflower_blue_two_24));

        style.incomingDefaultBubblePaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingBubblePaddingLeft,
                style.getDimension(R.dimen.message_padding_left));
        style.incomingDefaultBubblePaddingRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingBubblePaddingRight,
                style.getDimension(R.dimen.message_padding_right));
        style.incomingDefaultBubblePaddingTop = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingBubblePaddingTop,
                style.getDimension(R.dimen.message_padding_top));
        style.incomingDefaultBubblePaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingBubblePaddingBottom,
                style.getDimension(R.dimen.message_padding_bottom));

        //avatar
        style.incomingAvatarWidth = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingAvatarWidth,
                style.getDimension(R.dimen.message_avatar_width));
        style.incomingAvatarHeight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingAvatarHeight,
                style.getDimension(R.dimen.message_avatar_height));
        style.incomingAvatarMarginLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingAvatarMarginLeft,
                style.getDimension(R.dimen.message_avatar_margin));
        style.incomingAvatarMarginRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingAvatarMarginRight,
                style.getDimension(R.dimen.message_avatar_margin));
        style.showIncomingAvatar = typedArray.getBoolean(R.styleable.MessagesList_showIncomingAvatar, true);


        //time
        style.incomingTimeTextColor = typedArray.getColor(R.styleable.MessagesList_incomingTimeTextColor,
                style.getColor(R.color.warm_grey_four));
        style.incomingTimeTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingTimeTextSize,
                style.getDimension(R.dimen.message_text_time));
        style.incomingTimeTextStyle = typedArray.getInt(R.styleable.MessagesList_incomingTimeTextStyle, Typeface.NORMAL);
        style.incomingTimeTextPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingTimeTextPaddingLeft, 0);
        style.incomingTimeTextPaddingRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingTimeTextPaddingLeft,
                style.getDimension(R.dimen.message_time_text_padding_right));
        style.incomingTimeTextPaddingTop = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingTimeTextPaddingLeft, 0);
        style.incomingTimeTextPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingTimeTextPaddingLeft, 0);
        style.incomingTimeTextFormat = typedArray.getString(R.styleable.MessagesList_incomingTimeTextFormat);
        style.showIncomingTimeText = typedArray.getBoolean(R.styleable.MessagesList_showIncomingTimeText, true);

        //read receipt
        style.showIncomingReadReceipt = typedArray.getBoolean(R.styleable.MessagesList_showIncomingReadReceipt, true);

        // Username
        style.incomingUsernameTextColor = typedArray.getColor(R.styleable.MessagesList_incomingUsernameTextColor,
                style.getColor(R.color.black));
        style.incomingUsernameTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingUsernameTextSize,
                style.getDimension(R.dimen.message_text_username));
        style.incomingUsernameTextStyle = typedArray.getInt(R.styleable.MessagesList_incomingUsernameTextStyle, Typeface.NORMAL);
        style.incomingUsernameTextPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingUsernameTextPaddingLeft, 0);
        style.incomingUsernameTextPaddingRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingUsernameTextPaddingRight, 0);
        style.incomingUsernameTextPaddingTop = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingUsernameTextPaddingTop, 0);
        style.incomingUsernameTextPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MessagesList_incomingUsernameTextPaddingBottom,
                style.getDimension(R.dimen.message_username_padding_bottom));
        style.showIncomingUsername = typedArray.getBoolean(R.styleable.MessagesList_showIncomingUsername, true);


        //outcoming

        //text
        style.outcomingTextLinkColor = typedArray.getColor(R.styleable.MessagesList_outcomingTextLinkColor,
                style.getSystemAccentColor());
        style.outcomingTextColor = typedArray.getColor(R.styleable.MessagesList_outcomingTextColor,
                style.getColor(R.color.white));
        style.outcomingTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingTextSize,
                style.getDimension(R.dimen.message_text_size));
        style.outcomingTextStyle = typedArray.getInt(R.styleable.MessagesList_outcomingTextStyle, Typeface.NORMAL);

        //bubble
        style.outcomingBubbleDrawable = typedArray.getResourceId(R.styleable.MessagesList_outcomingBubbleDrawable, -1);
        style.outcomingDefaultBubbleColor = typedArray.getColor(R.styleable.MessagesList_outcomingDefaultBubbleColor,
                style.getColor(R.color.cornflower_blue_two));
        style.outcomingDefaultBubblePressedColor = typedArray.getColor(R.styleable.MessagesList_outcomingDefaultBubblePressedColor,
                style.getColor(R.color.cornflower_blue_two));
        style.outcomingDefaultBubbleSelectedColor = typedArray.getColor(R.styleable.MessagesList_outcomingDefaultBubbleSelectedColor,
                style.getColor(R.color.cornflower_blue_two_24));

        style.outcomingDefaultBubblePaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingBubblePaddingLeft,
                style.getDimension(R.dimen.message_padding_left));
        style.outcomingDefaultBubblePaddingRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingBubblePaddingRight,
                style.getDimension(R.dimen.message_padding_right));
        style.outcomingDefaultBubblePaddingTop = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingBubblePaddingTop,
                style.getDimension(R.dimen.message_padding_top));
        style.outcomingDefaultBubblePaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingBubblePaddingBottom,
                style.getDimension(R.dimen.message_padding_bottom));

        //avatar
        style.outcomingAvatarWidth = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingAvatarWidth,
                style.getDimension(R.dimen.message_avatar_width));
        style.outcomingAvatarHeight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingAvatarHeight,
                style.getDimension(R.dimen.message_avatar_height));
        style.outcomingAvatarMarginLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingAvatarMarginLeft,
                style.getDimension(R.dimen.message_avatar_margin));
        style.outcomingAvatarMarginRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingAvatarMarginRight,
                style.getDimension(R.dimen.message_avatar_margin));
        style.showOutcomingAvatar = typedArray.getBoolean(R.styleable.MessagesList_showOutcomingAvatar, true);

        //time
        style.outcomingTimeTextColor = typedArray.getColor(R.styleable.MessagesList_outcomingTimeTextColor,
                style.getColor(R.color.warm_grey_four));
        style.outcomingTimeTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingTimeTextSize,
                style.getDimension(R.dimen.message_text_time));
        style.outcomingTimeTextStyle = typedArray.getInt(R.styleable.MessagesList_outcomingTimeTextStyle, Typeface.NORMAL);
        style.outcomingTimeTextPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingTimeTextPaddingLeft, 0);
        style.outcomingTimeTextPaddingRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingTimeTextPaddingLeft,
                style.getDimension(R.dimen.message_time_text_padding_right));
        style.outcomingTimeTextPaddingTop = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingTimeTextPaddingLeft, 0);
        style.outcomingTimeTextPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingTimeTextPaddingLeft, 0);
        style.outcomingTimeTextFormat = typedArray.getString(R.styleable.MessagesList_outcomingTimeTextFormat);
        style.showOutcomingTimeText = typedArray.getBoolean(R.styleable.MessagesList_showOutcomingTimeText, true);

        //read receipt
        //TODO add layout for read receipt
//        style.outcomingReadReceiptLayout = typedArray.getResourceId(R.styleable.MessagesList_outcomingReadReceiptLayout, R.layout.activity_group_detail)
        style.showOutcomingReadReceipt = typedArray.getBoolean(R.styleable.MessagesList_showOutcomingReadReceipt, true);

        //username
        style.outcomingUsernameTextColor = typedArray.getColor(R.styleable.MessagesList_outcomingUsernameTextColor,
                style.getColor(R.color.black));
        style.outcomingUsernameTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingUsernameTextSize,
                style.getDimension(R.dimen.message_text_username));
        style.outcomingUsernameTextStyle = typedArray.getInt(R.styleable.MessagesList_outcomingUsernameTextStyle, Typeface.NORMAL);
        style.outcomingUsernameTextPaddingLeft = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingUsernameTextPaddingLeft, 0);
        style.outcomingUsernameTextPaddingRight = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingUsernameTextPaddingRight, 0);
        style.outcomingUsernameTextPaddingTop = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingUsernameTextPaddingTop, 0);
        style.outcomingUsernameTextPaddingBottom = typedArray.getDimensionPixelSize(R.styleable.MessagesList_outcomingUsernameTextPaddingBottom,
                style.getDimension(R.dimen.message_username_padding_bottom));
        style.showOutcomingUsername = typedArray.getBoolean(R.styleable.MessagesList_showOutcomingUsername, true);

        //dates
        style.dateHeaderPadding = typedArray.getDimensionPixelSize(R.styleable.MessagesList_dateHeaderPadding,
                style.getDimension(R.dimen.message_date_header_padding));
        style.dateHeaderFormat = typedArray.getString(R.styleable.MessagesList_dateHeaderFormat);
        style.dateHeaderTextColor = typedArray.getColor(R.styleable.MessagesList_dateHeaderTextColor,
                style.getColor(R.color.warm_grey_two));
        style.dateHeaderTextSize = typedArray.getDimensionPixelSize(R.styleable.MessagesList_dateHeaderTextSize,
                style.getDimension(R.dimen.message_date_header_text_size));
        style.dateHeaderTextStyle = typedArray.getInt(R.styleable.MessagesList_dateHeaderTextStyle, Typeface.NORMAL);
        style.showDateHeader = typedArray.getBoolean(R.styleable.MessagesList_showDateHeader, true);

        //Message gap
        style.messageGap = typedArray.getDimensionPixelSize(R.styleable.MessagesList_messageGap,
                style.getDimension(R.dimen.message_gap));

        // left space of message
        style.leftMargin = typedArray.getDimensionPixelSize(R.styleable.MessagesList_leftMargin,
                style.getDimension(R.dimen.message_margin));
        // right space of message
        style.rightMargin = typedArray.getDimensionPixelSize(R.styleable.MessagesList_rightMargin,
                style.getDimension(R.dimen.message_margin));
        // read receipt layout
        style.readReceiptReadLayout = typedArray.getResourceId(R.styleable.MessagesList_readReceiptReadLayout, R.layout.layout_read_receipt_read);
        style.readReeiptUnReadLayout = typedArray.getResourceId(R.styleable.MessagesList_readReceiptUnReadLayout, R.layout.layout_read_receipt_unread);
        typedArray.recycle();
        return style;
    }

    private MessagesListStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Drawable getMessageSelector(@ColorInt int normalColor, @ColorInt int selectedColor,
                                        @ColorInt int pressedColor, @DrawableRes int shape) {

        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_selected},
                                new int[]{android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected}
                        },
                        new int[]{selectedColor, pressedColor, normalColor}
                ));
        return drawable;
    }

    // type of autolink like phone, web, email etc
    public int getTextAutoLinkMask() {
        return textAutoLinkMask;
    }

    // incoming
    // text
    public int getIncomingTextLinkColor() {
        return incomingTextLinkColor;
    }

    public int getIncomingTextColor() {
        return incomingTextColor;
    }

    public int getIncomingTextSize() {
        return incomingTextSize;
    }

    public int getIncomingTextStyle() {
        return incomingTextStyle;
    }

    // bubble
    public Drawable getIncomingBubbleDrawable() {
        if (incomingBubbleDrawable == -1) {
            return getDrawable(R.drawable.shape_incoming_message);
        } else {
            return getDrawable(incomingBubbleDrawable);
        }
    }

    public int getIncomingDefaultBubblePaddingLeft() {
        return incomingDefaultBubblePaddingLeft;
    }

    public int getIncomingDefaultBubblePaddingRight() {
        return incomingDefaultBubblePaddingRight;
    }

    public int getIncomingDefaultBubblePaddingTop() {
        return incomingDefaultBubblePaddingTop;
    }

    public int getIncomingDefaultBubblePaddingBottom() {
        return incomingDefaultBubblePaddingBottom;
    }

    //avatar

    public int getIncomingAvatarWidth() {
        return incomingAvatarWidth;
    }

    public int getIncomingAvatarHeight() {
        return incomingAvatarHeight;
    }

    public int getIncomingAvatarMarginLeft() {
        return incomingAvatarMarginLeft;
    }

    public int getIncomingAvatarMarginRight() {
        return incomingAvatarMarginRight;
    }

    public boolean isShowIncomingAvatar() {
        return showIncomingAvatar;
    }

    //time
    public int getIncomingTimeTextSize() {
        return incomingTimeTextSize;
    }

    public int getIncomingTimeTextStyle() {
        return incomingTimeTextStyle;
    }

    public int getIncomingTimeTextColor() {
        return incomingTimeTextColor;
    }

    public int getIncomingTimeTextPaddingLeft() {
        return incomingTimeTextPaddingLeft;
    }

    public int getIncomingTimeTextPaddingRight() {
        return incomingTimeTextPaddingRight;
    }

    public int getIncomingTimeTextPaddingTop() {
        return incomingTimeTextPaddingTop;
    }

    public int getIncomingTimeTextPaddingBottom() {
        return incomingTimeTextPaddingBottom;
    }

    public String getIncomingTimeTextFormat() {
        return incomingTimeTextFormat;
    }

    public boolean isShowIncomingTimeText() {
        return showIncomingTimeText;
    }

    //read receipt

    public boolean isShowIncomingReadReceipt() {
        return showIncomingReadReceipt;
    }

    // username

    public int getIncomingUsernameTextColor() {
        return incomingUsernameTextColor;
    }

    public int getIncomingUsernameTextSize() {
        return incomingUsernameTextSize;
    }

    public int getIncomingUsernameTextStyle() {
        return incomingUsernameTextStyle;
    }

    public int getIncomingUsernameTextPaddingLeft() {
        return incomingUsernameTextPaddingLeft;
    }

    public int getIncomingUsernameTextPaddingRight() {
        return incomingUsernameTextPaddingRight;
    }

    public int getIncomingUsernameTextPaddingTop() {
        return incomingUsernameTextPaddingTop;
    }

    public int getIncomingUsernameTextPaddingBottom() {
        return incomingUsernameTextPaddingBottom;
    }

    public boolean isShowIncomingUsername() {
        return showIncomingUsername;
    }

    // outcoming

    //text
    public int getOutcomingTextLinkColor() {
        return outcomingTextLinkColor;
    }

    public int getOutcomingTextColor() {
        return outcomingTextColor;
    }

    public int getOutcomingTextSize() {
        return outcomingTextSize;
    }

    public int getOutcomingTextStyle() {
        return outcomingTextStyle;
    }

    //bubble
    public Drawable getOutcomingBubbleDrawable() {
        if (outcomingBubbleDrawable == -1) {
            return getMessageSelector(outcomingDefaultBubbleColor, outcomingDefaultBubbleSelectedColor,
                    outcomingDefaultBubblePressedColor, R.drawable.shape_outcoming_message);
        } else {
            return getDrawable(outcomingBubbleDrawable);
        }
    }

    public int getOutcomingDefaultBubblePaddingLeft() {
        return outcomingDefaultBubblePaddingLeft;
    }

    public int getOutcomingDefaultBubblePaddingRight() {
        return outcomingDefaultBubblePaddingRight;
    }

    public int getOutcomingDefaultBubblePaddingTop() {
        return outcomingDefaultBubblePaddingTop;
    }

    public int getOutcomingDefaultBubblePaddingBottom() {
        return outcomingDefaultBubblePaddingBottom;
    }

    //avatar
    public int getOutcomingAvatarWidth() {
        return outcomingAvatarWidth;
    }

    public int getOutcomingAvatarHeight() {
        return outcomingAvatarHeight;
    }

    public int getOutcomingAvatarMarginLeft() {
        return outcomingAvatarMarginLeft;
    }

    public int getOutcomingAvatarMarginRight() {
        return outcomingAvatarMarginRight;
    }

    public boolean isShowOutcomingAvatar() {
        return showOutcomingAvatar;
    }

    //time
    public int getOutcomingTimeTextColor() {
        return outcomingTimeTextColor;
    }

    public int getOutcomingTimeTextSize() {
        return outcomingTimeTextSize;
    }

    public int getOutcomingTimeTextStyle() {
        return outcomingTimeTextStyle;
    }

    public int getOutcomingTimeTextPaddingLeft() {
        return outcomingTimeTextPaddingLeft;
    }

    public int getOutcomingTimeTextPaddingRight() {
        return outcomingTimeTextPaddingRight;
    }

    public int getOutcomingTimeTextPaddingTop() {
        return outcomingTimeTextPaddingTop;
    }

    public int getOutcomingTimeTextPaddingBottom() {
        return outcomingTimeTextPaddingBottom;
    }

    public String getOutcomingTimeTextFormat() {
        return outcomingTimeTextFormat;
    }

    public boolean isShowOutcomingTimeText() {
        return showOutcomingTimeText;
    }

    //read receipt

    public boolean isShowOutcomingReadReceipt() {
        return showOutcomingReadReceipt;
    }

    //    username
    public int getOutcomingUsernameTextColor() {
        return outcomingUsernameTextColor;
    }

    public int getOutcomingUsernameTextSize() {
        return outcomingUsernameTextSize;
    }

    public int getOutcomingUsernameTextStyle() {
        return outcomingUsernameTextStyle;
    }

    public int getOutcomingUsernameTextPaddingLeft() {
        return outcomingUsernameTextPaddingLeft;
    }

    public int getOutcomingUsernameTextPaddingRight() {
        return outcomingUsernameTextPaddingRight;
    }

    public int getOutcomingUsernameTextPaddingTop() {
        return outcomingUsernameTextPaddingTop;
    }

    public int getOutcomingUsernameTextPaddingBottom() {
        return outcomingUsernameTextPaddingBottom;
    }

    public boolean isShowOutcomingUsername() {
        return showOutcomingUsername;
    }

    //dates header
    public int getDateHeaderTextColor() {
        return dateHeaderTextColor;
    }

    public int getDateHeaderTextSize() {
        return dateHeaderTextSize;
    }

    public int getDateHeaderTextStyle() {
        return dateHeaderTextStyle;
    }

    public int getDateHeaderPadding() {
        return dateHeaderPadding;
    }

    public String getDateHeaderFormat() {
        return dateHeaderFormat;
    }

    public boolean isShowDateHeader() {
        return showDateHeader;
    }

    // Gap between messages
    public int getMessageGap() {
        return messageGap;
    }

    // left space of message
    public int getLeftMargin() {
        return leftMargin;
    }

    // right space of message
    public int getRightMargin() {
        return rightMargin;
    }

    // read receipt layout

    public int getReadReceiptReadLayout() {
        return readReceiptReadLayout;
    }

    public int getReadReceiptUnReadLayout() {
        return readReeiptUnReadLayout;
    }
}
