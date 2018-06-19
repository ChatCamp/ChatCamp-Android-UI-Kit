package com.chatcamp.uikit.messages.messagetypes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chatcamp.uikit.R;
import com.chatcamp.uikit.utils.DownloadFileListener;
import com.chatcamp.uikit.utils.FileUtils;
import com.chatcamp.uikit.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;

import io.chatcamp.sdk.Message;

/**
 * Created by shubhamdhabhai on 07/05/18.
 */

// This should be the last factory added to adapter as it returns true for all the attachment types
public class FileMessageFactory<T> extends MessageFactory<FileMessageFactory.DocumentMessageHolder> {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA = 104;
    private WeakReference<Object> objectWeakReference;
    private Handler handler;
    private View view;
    private ProgressBar progressBar;
    private ImageView downloadIcon;

    public FileMessageFactory(Activity activity) {
        this.objectWeakReference = new WeakReference<Object>(activity);
        handler = new Handler();
    }

    public FileMessageFactory(Fragment fragment) {
        this.objectWeakReference = new WeakReference<Object>(fragment);
        handler = new Handler();
    }

    @Override
    public boolean isBindable(Message message) {
        if (message.getType().equals("attachment")) {
            if (message.getAttachment().getType().contains("application") || message.getAttachment().getType().contains("css") ||
                    message.getAttachment().getType().contains("csv") || message.getAttachment().getType().contains("text")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DocumentMessageHolder createMessageHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        View view = layoutInflater.inflate(R.layout.layout_message_document, cellView, true);
        TextView textView = view.findViewById(R.id.tv_document_name);

        Drawable backgroundDrawable = isMe ? messageStyle.getOutcomingBubbleDrawable() :
                messageStyle.getIncomingBubbleDrawable();
        int textColor = isMe ? messageStyle.getOutcomingTextColor() : messageStyle.getIncomingTextColor();
        textView.setTextColor(textColor);
        int documentIcon = isMe ? R.drawable.ic_document_white_chat : R.drawable.ic_document_chat;
        ImageView documentImage = view.findViewById(R.id.iv_document);
        documentImage.setImageResource(documentIcon);

        int downloadIcon = isMe ? R.drawable.ic_download_white : R.drawable.ic_download;
        ImageView downloadImage = view.findViewById(R.id.iv_download);
        downloadImage.setImageResource(downloadIcon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(backgroundDrawable);
        } else {
            view.setBackgroundDrawable(backgroundDrawable);
        }
        return new DocumentMessageHolder(view);
    }

    @Override
    public void bindMessageHolder(final DocumentMessageHolder messageHolder, final Message message) {
        messageHolder.view.setTag(message);
        messageHolder.documentName.setText(message.getAttachment().getName());
        Context context = Utils.getContext(objectWeakReference.get());
        if (context == null) {
            messageHolder.downloadIcon.setVisibility(View.INVISIBLE);
            return;
        }
        Drawable backgroundDrawable = messageHolder.view.getBackground().mutate();
        boolean isFirstMessage = messageSpecs.isFirstMessage;
        float cornerRadius = messageHolder.view.getContext()
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
            messageHolder.view.setBackground(backgroundDrawable);
        } else {
            messageHolder.view.setBackgroundDrawable(backgroundDrawable);
        }
        messageHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = Utils.getContext(objectWeakReference.get());
                if (context == null) {
                    return;
                }
                if (!FileUtils.fileExists(context, message.getAttachment().getUrl(), Environment.DIRECTORY_DOCUMENTS)) {
                    messageHolder.progressBar.setVisibility(View.VISIBLE);
                    messageHolder.progressBar.setProgress(0);
                } else {
                    messageHolder.progressBar.setVisibility(View.INVISIBLE);
                }
                onDocumentClick(v, messageHolder.progressBar, messageHolder.downloadIcon);
            }
        });

        if (FileUtils.fileExists(context, message.getAttachment().getUrl(), Environment.DIRECTORY_DOWNLOADS)) {
            messageHolder.downloadIcon.setVisibility(View.INVISIBLE);
        } else {
            messageHolder.downloadIcon.setVisibility(View.VISIBLE);
        }
    }

    private void onDocumentClick(View v, ProgressBar progressBar, ImageView downloadIcon) {
        Context context = Utils.getContext(objectWeakReference.get());

        if (context == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            view = v;
            this.progressBar = progressBar;
            this.downloadIcon = downloadIcon;
            Utils.requestPermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA, objectWeakReference.get());


        } else {
            downloadDocument(v, progressBar, downloadIcon);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (objectWeakReference.get() != null) {
                    if (view != null && progressBar != null) {
                        downloadDocument(view, progressBar, downloadIcon);
                    }
                }
            }
        }
    }

    public static class DocumentMessageHolder extends MessageFactory.MessageHolder {
        ImageView documentImage;
        ProgressBar progressBar;
        ImageView downloadIcon;
        TextView documentName;
        View view;

        public DocumentMessageHolder(View view) {
            this.view = view;
            documentImage = view.findViewById(R.id.iv_document);
            progressBar = view.findViewById(R.id.progress_bar);
            downloadIcon = view.findViewById(R.id.iv_download);
            documentName = view.findViewById(R.id.tv_document_name);
        }
    }

    protected void downloadDocument(View v, final ProgressBar progressBar, final ImageView downloadIcon) {
        if (v.getTag() != null && v.getTag() instanceof Message) {
            if (downloadIcon != null) {
                downloadIcon.setVisibility(View.INVISIBLE);
            }
            final Message message = (Message) v.getTag();
            new Thread(new Runnable() {
                public void run() {
                    Uri path = null;
                    try {
                        Context context = Utils.getContext(objectWeakReference.get());
                        File file = FileUtils.downloadFile(context, message.getAttachment().getUrl(),
                                Environment.DIRECTORY_DOWNLOADS, new DownloadFileListener() {
                                    @Override
                                    public void downloadProgress(final int progress) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (progressBar != null) {
                                                    progressBar.setProgress(progress);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void downloadComplete() {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.setVisibility(View.INVISIBLE);

                                            }
                                        });
                                    }
                                });
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            path = FileProvider.getUriForFile(context,
                                    context.getPackageName() + ".chatcamp.fileprovider", file
                            );
                        } else {
                            path = Uri.fromFile(file);
                        }

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(path, message.getAttachment().getType());
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Utils.startActivity(intent, objectWeakReference.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (path != null) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(path, "application/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                Utils.startActivity(intent, objectWeakReference.get());
                            } catch (Exception error) {
                                error.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }
}
