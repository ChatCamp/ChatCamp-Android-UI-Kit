package com.chatcamp.uikit.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.chatcamp.uikit.R;

/**
 * Created by shubhamdhabhai on 12/07/18.
 */

public class DownloadProgressBar extends RelativeLayout implements View.OnClickListener {

    private ProgressBar progressBar;
    private ImageView downloadIcon;
    private ImageView cancelIcon;
    private DownloadProgressbarClickListener clickListener;
    private boolean isCanceled;

    public interface DownloadProgressbarClickListener {
        void onCancelClicked(Object object);

        void onDownloadClicked(Object object);
    }

    public DownloadProgressBar(Context context) {
        super(context);
        init(context);
    }

    public DownloadProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DownloadProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_custom_progressbar, this);
        progressBar = findViewById(R.id.progress_bar);
        downloadIcon = findViewById(R.id.icon_download);
        cancelIcon = findViewById(R.id.icon_cancel);
        progressBar.setOnClickListener(this);
        downloadIcon.setOnClickListener(this);
        cancelIcon.setOnClickListener(this);
    }

    public void setProgress(int progress) {
        if (!isCanceled) {
            if (progressBar.getVisibility() != VISIBLE) {
                progressBar.setVisibility(VISIBLE);
            }
            progressBar.setProgress(progress);
        }
    }

    public void startDownload() {
        isCanceled = false;
        downloadIcon.setVisibility(INVISIBLE);
        progressBar.setVisibility(VISIBLE);
        cancelIcon.setVisibility(VISIBLE);
    }

    public void completeDownload() {
        downloadIcon.setVisibility(INVISIBLE);
        progressBar.setVisibility(INVISIBLE);
        cancelIcon.setVisibility(INVISIBLE);
        progressBar.setProgress(0);
    }

    public void cancelDownload() {
        isCanceled = true;
        downloadIcon.setVisibility(VISIBLE);
        progressBar.setVisibility(INVISIBLE);
        cancelIcon.setVisibility(INVISIBLE);
        progressBar.setProgress(0);
    }

    public void resetView() {
        downloadIcon.setVisibility(VISIBLE);
        progressBar.setVisibility(INVISIBLE);
        cancelIcon.setVisibility(INVISIBLE);
        progressBar.setProgress(0);
    }

    public void setDownloadProgressbarClickListener(DownloadProgressbarClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.icon_cancel) {
            cancelDownload();
            if (clickListener != null) {
                clickListener.onCancelClicked(this.getTag());
            }
        } else if (v.getId() == R.id.icon_download) {
            startDownload();
            if (clickListener != null) {
                clickListener.onDownloadClicked(this.getTag());
            }
        }
    }
}

