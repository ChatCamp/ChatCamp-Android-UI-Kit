package com.chatcamp.uikit.setting;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.database.ChatCampDatabaseHelper;
import com.chatcamp.uikit.user.BlockedUserListActivity;
import com.chatcamp.uikit.utils.CircleTransform;
import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.chatcamp.sdk.ChatCamp;
import io.chatcamp.sdk.ChatCampException;
import io.chatcamp.sdk.ClientPushNotificationTemplate;
import io.chatcamp.sdk.User;

import static android.app.Activity.RESULT_OK;

/**
 * Created by shubhamdhabhai on 27/08/18.
 */

public class SettingView extends LinearLayout {

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA = 131;
    private static final int PICK_MEDIA_RESULT_CODE = 132;
    private ImageView avatarIv;
    private TextView usernameTv;
    private TextView editAvatarTv;
    private EditText statusEt;
    private TextView saveStatusTv;
    private CheckBox enableNotificationCb;
    private AppCompatSpinner snoozeSpinner;
    private LinearLayout containerSnoozeLl;
    private TextView snoozeMessageTv;
    private TextView resumeNotificationTv;
    private CheckBox disableNotificationIntervalCb;
    private AppCompatSpinner startTimeSpinner;
    private AppCompatSpinner endTimeSpinner;
    private AppCompatSpinner timezoneSpinner;
    private CheckBox muteSoundCb;
    private AppCompatSpinner notificationTemplateSpinner;
    private TextView manageBlockedUserTv;
    private TextView logoutTv;
    private WeakReference<Object> objectWeakReference;

    private UploadListener uploadListener;
    private OnLogoutClickListener logoutClickListener;

    public interface UploadListener {
        void onUploadProgress(int progress);

        void onUploadSuccess(User user);

        void onUploadFailed(ChatCampException error);
    }

    public interface OnLogoutClickListener {
        void onLogoutClicked();
    }

    public SettingView(Context context) {
        super(context);
        init(context);
    }

    public SettingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Activity activity) {
        objectWeakReference = new WeakReference<Object>(activity);
    }

    public void init(Fragment fragment) {
        objectWeakReference = new WeakReference<Object>(fragment);
    }

    public void setUploadAvatarListener(UploadListener uploadAvatarListener) {
        this.uploadListener = uploadAvatarListener;
    }

    public void setLogoutClickListener(OnLogoutClickListener logoutClickListener) {
        this.logoutClickListener = logoutClickListener;
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_setting_view, this);
        avatarIv = findViewById(R.id.iv_avatar);
        usernameTv = findViewById(R.id.tv_username);
        editAvatarTv = findViewById(R.id.tv_edit_avatar);
        statusEt = findViewById(R.id.et_status);
        saveStatusTv = findViewById(R.id.tv_save_status);
        enableNotificationCb = findViewById(R.id.cb_enable_notification);
        snoozeSpinner = findViewById(R.id.spinner_snooze);
        containerSnoozeLl = findViewById(R.id.container_snooze);
        snoozeMessageTv = findViewById(R.id.tv_snooze_message);
        resumeNotificationTv = findViewById(R.id.tv_resume_notification);
        disableNotificationIntervalCb = findViewById(R.id.cb_disable_notification_interval);
        startTimeSpinner = findViewById(R.id.spinner_start_time);
        endTimeSpinner = findViewById(R.id.spinner_end_time);
        timezoneSpinner = findViewById(R.id.spinner_timezone);
        muteSoundCb = findViewById(R.id.cb_mute_sound);
        notificationTemplateSpinner = findViewById(R.id.spinner_notification_template);
        manageBlockedUserTv = findViewById(R.id.tv_manage_blocked_user);
        logoutTv = findViewById(R.id.tv_logout);
        populateData();
    }

    private void init(Context context, AttributeSet attributeSet) {
        init(context);
        // todo add style here
    }

    private void populateData() {
        if (!TextUtils.isEmpty(ChatCamp.getCurrentUser().getAvatarUploadUrl())) {
            Picasso.with(getContext()).load(ChatCamp.getCurrentUser().getAvatarUploadUrl())
                    .placeholder(R.drawable.icon_default_contact)
                    .transform(new CircleTransform()).into(avatarIv);
        } else {
            Picasso.with(getContext()).load(ChatCamp.getCurrentUser().getAvatarUrl())
                    .placeholder(R.drawable.icon_default_contact)
                    .transform(new CircleTransform()).into(avatarIv);
        }


        usernameTv.setText(ChatCamp.getCurrentUser().getDisplayName());
        // show status

        // notification enabled or disabled

        //snooze
        final List<String> snoozeList = Arrays.asList(getResources().getStringArray(R.array.notification_snooze_items));
        ArrayAdapter<String> snoozeArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner_item, snoozeList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        snoozeArrayAdapter.setDropDownViewResource(R.layout.layout_spinner_item);
        snoozeSpinner.setAdapter(snoozeArrayAdapter);

        ChatCamp.getPushNotificationSnooze(new ChatCamp.OnGetPushNotificationSnoozeListener() {
            @Override
            public void onGetPushNotificationSnooze(long duration, ChatCampException exception) {
                if (exception != null) {
                    Toast.makeText(getContext(), "Coud not fetch snooze details", Toast.LENGTH_LONG).show();
                    return;
                }
                if (duration == 0) {
                    //snooze is off
                    containerSnoozeLl.setVisibility(GONE);
                    snoozeSpinner.setVisibility(VISIBLE);
                } else {
                    //snooze is on
                    containerSnoozeLl.setVisibility(VISIBLE);
                    snoozeSpinner.setVisibility(GONE);
                    snoozeMessageTv.setText(String.format("You are muted for next %s minutes", String.valueOf(duration)));
                }

            }
        });

        // dnd schedule
        final List<String> dndTimes = Arrays.asList(getResources().getStringArray(R.array.dnd_schedule));
        ArrayAdapter<String> dndAdapter = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner_item, dndTimes);
        dndAdapter.setDropDownViewResource(R.layout.layout_spinner_item);
        startTimeSpinner.setAdapter(dndAdapter);
        endTimeSpinner.setAdapter(dndAdapter);

        final List<String> timezones = Arrays.asList(getResources().getStringArray(R.array.time_zones));
        ArrayAdapter<String> timezoneAdapter = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner_item, timezones);
        timezoneAdapter.setDropDownViewResource(R.layout.layout_spinner_item);
        timezoneSpinner.setAdapter(timezoneAdapter);


        ChatCamp.getPushNotificationPreference(new ChatCamp.OnGetPushNotificationPreferenceListener() {
            @Override
            public void onGetPushNotificationPreference(boolean doNotDisturb, int startHour, int startMin,
                                                        int endHour, int endMin, String timezone, ChatCampException exception) {
                if (exception != null) {
                    Toast.makeText(getContext(), "Coud not fetch dnd details", Toast.LENGTH_LONG).show();
                    return;
                }
                if (doNotDisturb) {
                    disableNotificationIntervalCb.setChecked(true);
                    boolean isMorning = true;
                    if (startHour > 12) {
                        startHour = startHour - 12;
                        isMorning = false;
                    }
                    int startTimePosition = getPositionInList(dndTimes, String.valueOf(startHour) + ":" + String.valueOf(startMin) + " " + (isMorning ? "AM" : "PM"));
                    if (startTimePosition != -1) {
                        startTimeSpinner.setSelection(startTimePosition);
                    }

                    isMorning = true;
                    if (endHour > 12) {
                        endHour = endHour - 12;
                        isMorning = false;
                    }
                    int endTimePosition = getPositionInList(dndTimes, String.valueOf(endHour) + ":" + String.valueOf(endMin) + " " + (isMorning ? "AM" : "PM"));
                    if (endTimePosition != -1) {
                        endTimeSpinner.setSelection(endTimePosition);
                    }

                    int timezonePosition = getPositionInList(timezones, timezone);
                    if (timezonePosition != -1) {
                        timezoneSpinner.setSelection(timezonePosition);
                    }
                } else {
                    startTimeSpinner.setEnabled(false);
                    endTimeSpinner.setEnabled(false);
                    timezoneSpinner.setEnabled(false);
                }
            }
        });

        // mute sound
        ChatCamp.getPushNotificationSound(new ChatCamp.OnGetPushNotificationSoundListener() {
            @Override
            public void onGetPushNotificationSound(String sound, ChatCampException exception) {
                if (exception != null) {
                    Toast.makeText(getContext(), "Coud not fetch sound details", Toast.LENGTH_LONG).show();
                    return;
                }
                if (sound.equalsIgnoreCase("OFF")) {
                    muteSoundCb.setChecked(true);
                } else {
                    muteSoundCb.setChecked(false);
                }
            }
        });

        // Notification template
        final List<String> notificationTemplates = Arrays.asList(getResources().getStringArray(R.array.notification_template));
        ArrayAdapter<String> notificationTemplateAdapter = new ArrayAdapter<String>(getContext(), R.layout.layout_spinner_item, notificationTemplates);
        notificationTemplateAdapter.setDropDownViewResource(R.layout.layout_spinner_item);
        notificationTemplateSpinner.setAdapter(notificationTemplateAdapter);

        ChatCamp.getPushNotificationTemplate(new ChatCamp.OnGetPushNotificationTemplateListener() {
            @Override
            public void onGetPushNotificationTemplate(ClientPushNotificationTemplate template, ChatCampException exception) {
                if (exception != null) {
                    Toast.makeText(getContext(), "Coud not fetch template details", Toast.LENGTH_LONG).show();
                    return;
                }
                int notificationTemplatePosition = getPositionInList(notificationTemplates, template.getValue());
                if (notificationTemplatePosition != -1) {
                    notificationTemplateSpinner.setSelection(notificationTemplatePosition);
                }
            }
        });


        // setup click listeners

        // upload avatar
        editAvatarTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        //Enable push notification

        // snooze
        snoozeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    int duration = 0;
                    switch (position) {
                        case 1:
                            duration = 60;
                            break;
                        case 2:
                            duration = 180;
                            break;
                        case 3:
                            duration = 360;
                            break;
                        case 4:
                            duration = 540;
                            break;
                        case 5:
                            duration = 720;
                            break;
                    }
                    ChatCamp.updatePushNotificationSnooze(duration, onPushNotificationUpdateListener);
                    snoozeSpinner.setVisibility(GONE);
                    containerSnoozeLl.setVisibility(VISIBLE);
                    snoozeMessageTv.setText(String.format("You are muted for next %s minutes", String.valueOf(duration)));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        resumeNotificationTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatCamp.updatePushNotificationSnooze(0, onPushNotificationUpdateListener);
                snoozeSpinner.setVisibility(VISIBLE);
                snoozeSpinner.setSelection(0);
                containerSnoozeLl.setVisibility(GONE);
            }
        });

        //dnd

        //mute sound
        muteSoundCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ChatCamp.updatePushNotificationSound(isChecked ? "OFF" : "DEFAULT", onPushNotificationUpdateListener);
            }
        });
        // template
        notificationTemplateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ChatCamp.updatePushNotificationTemplate(position == 0 ?
                        ClientPushNotificationTemplate.DEFAULT : ClientPushNotificationTemplate.PRIVATE,
                        onPushNotificationUpdateListener);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //manage blocked users

        manageBlockedUserTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BlockedUserListActivity.class);
                getContext().startActivity(intent);
            }
        });

        //logout
        logoutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatCamp.deleteAllUserPushTokens(new ChatCamp.OnUserPushTokenDeletedListener() {
                    @Override
                    public void onUserPushTokenDeleted(ChatCampException exception) {
                        ChatCamp.disconnect(new ChatCamp.DisconnectListener() {
                            @Override
                            public void onDisconnected(ChatCampException e) {
                                ChatCampDatabaseHelper helper = new ChatCampDatabaseHelper(getContext());
                                helper.clearDatabase();
                                if(logoutClickListener != null) {
                                    logoutClickListener.onLogoutClicked();
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    private void uploadImage() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.requestPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_MEDIA, objectWeakReference.get());

        } else {
            chooseMedia();
        }
    }

    private void chooseMedia() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        Utils.startActivityForResult(intent, PICK_MEDIA_RESULT_CODE, objectWeakReference.get());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent dataFile) {
        if (resultCode == RESULT_OK && dataFile != null) {
            if (requestCode == PICK_MEDIA_RESULT_CODE) {
                Uri uri = dataFile.getData();
                if (uri == null) {
                    ChatCampException exception = new ChatCampException("Picked file is not valid", "GALLERY UPLOAD ERROR");
                    sendAttachmentError(exception);
                    return;
                }
                Context context = Utils.getContext(objectWeakReference.get());
                if (context == null) {
                    ChatCampException exception = new ChatCampException("Context is null", "GALLERY UPLOAD ERROR");
                    sendAttachmentError(exception);
                    return;
                }
                uploadFile(uri);

            }
        }
    }

    private void sendAttachmentError(ChatCampException exception) {
        if (uploadListener != null) {
            uploadListener.onUploadFailed(exception);
        }
    }

    private void uploadFile(Uri uri) {
        String path = FileUtils.getPath(getContext(), uri);
        if (TextUtils.isEmpty(path)) {
            Log.e("Upload avatar", "File path is null");
            ChatCampException exception = new ChatCampException("File path is null", "AVATAR UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        String fileName = FileUtils.getFileName(getContext(), uri);
        String contentType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getContext().getContentResolver();
            contentType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        if (TextUtils.isEmpty(contentType)) {
            ChatCampException exception = new ChatCampException("File content type is null", "AVATAR UPLOAD ERROR");
            sendAttachmentError(exception);
            return;
        }
        File file;

        file = new File(path);
        try {
            File compressedFile = createImageFile();
            if (compressedFile == null) {
                ChatCampException exception = new ChatCampException("Error compressing image", "GALLERY UPLOAD ERROR");
                sendAttachmentError(exception);
                return;
            }
            Bitmap bitmap = decodeSampledBitmapFromFile(path, 1280, 800);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(compressedFile));
            file = compressedFile;
        } catch (Throwable t) {
            Log.e("ERROR", t.toString());
            ChatCampException exception = new ChatCampException(t.toString(), "GALLERY UPLOAD ERROR");
            sendAttachmentError(exception);
            t.printStackTrace();
        }
        sendAttachment(file);
    }

    private void sendAttachment(File file) {
        ChatCamp.uploadUserAvatar(file, new ChatCamp.OnUploadAvatarListener() {
            @Override
            public void onUploadProgress(int progress) {
                if (uploadListener != null) {
                    uploadListener.onUploadProgress(progress);
                }
            }

            @Override
            public void onUploadSuccess(User user) {
                if (uploadListener != null) {
                    uploadListener.onUploadSuccess(user);
                    Picasso.with(getContext()).load(user.getAvatarUploadUrl())
                            .placeholder(R.drawable.icon_default_contact)
                            .transform(new CircleTransform()).into(avatarIv);

                }
            }

            @Override
            public void onUploadFailed(Throwable error) {
                if (uploadListener != null) {
                    ChatCampException exception = new ChatCampException(error.getMessage(), "FILE UPLOAD FAILED");
                    uploadListener.onUploadFailed(exception);
                }

            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    // TODO should do this in background
    private Bitmap decodeSampledBitmapFromFile(String path, int reqHeight,
                                               int reqWidth) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float) height / (float) reqHeight);
        }
        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth) {
            inSampleSize = Math.round((float) width / (float) reqWidth);
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }


    private int getPositionInList(List<String> list, String item) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).equalsIgnoreCase(item)) {
                return i;
            }
        }
        return -1;
    }

    private ChatCamp.OnPushNotificationUpdatedListener onPushNotificationUpdateListener = new ChatCamp.OnPushNotificationUpdatedListener() {
        @Override
        public void onPushNotificationTemplateUpdated(ChatCampException exception) {

        }

        @Override
        public void onPushNotificationSoundUpdated(ChatCampException exception) {

        }

        @Override
        public void onPushNotificationPreferenceUpdated(ChatCampException exception) {

        }

        @Override
        public void onPushNotificationSnoozeUpdated(ChatCampException exception) {

        }
    };
}
