package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.metrics.LogMaker;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.widget.MediaNotificationView;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationMediaTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private View mActions;
    /* access modifiers changed from: private */
    public View.OnAttachStateChangeListener mAttachStateListener = new View.OnAttachStateChangeListener() {
        public void onViewAttachedToWindow(View view) {
        }

        public void onViewDetachedFromWindow(View view) {
            boolean unused = NotificationMediaTemplateViewWrapper.this.mIsViewVisible = false;
        }
    };
    private Context mContext;
    private long mDuration = 0;
    /* access modifiers changed from: private */
    public final Handler mHandler = ((Handler) Dependency.get(Dependency.MAIN_HANDLER));
    /* access modifiers changed from: private */
    public boolean mIsViewVisible;
    private MediaController.Callback mMediaCallback = new MediaController.Callback() {
        public void onSessionDestroyed() {
            NotificationMediaTemplateViewWrapper.this.clearTimer();
            NotificationMediaTemplateViewWrapper.this.mMediaController.unregisterCallback(this);
            NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper = NotificationMediaTemplateViewWrapper.this;
            MediaNotificationView mediaNotificationView = notificationMediaTemplateViewWrapper.mView;
            if (mediaNotificationView instanceof MediaNotificationView) {
                mediaNotificationView.removeVisibilityListener(notificationMediaTemplateViewWrapper.mVisibilityListener);
                NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper2 = NotificationMediaTemplateViewWrapper.this;
                notificationMediaTemplateViewWrapper2.mView.removeOnAttachStateChangeListener(notificationMediaTemplateViewWrapper2.mAttachStateListener);
            }
        }

        public void onPlaybackStateChanged(PlaybackState playbackState) {
            if (playbackState != null) {
                if (playbackState.getState() != 3) {
                    NotificationMediaTemplateViewWrapper.this.updatePlaybackUi(playbackState);
                    NotificationMediaTemplateViewWrapper.this.clearTimer();
                } else if (NotificationMediaTemplateViewWrapper.this.mSeekBarTimer == null && NotificationMediaTemplateViewWrapper.this.mSeekBarView != null && NotificationMediaTemplateViewWrapper.this.mSeekBarView.getVisibility() != 8) {
                    NotificationMediaTemplateViewWrapper.this.startTimer();
                }
            }
        }

        public void onMetadataChanged(MediaMetadata mediaMetadata) {
            if (NotificationMediaTemplateViewWrapper.this.mMediaMetadata == null || !NotificationMediaTemplateViewWrapper.this.mMediaMetadata.equals(mediaMetadata)) {
                MediaMetadata unused = NotificationMediaTemplateViewWrapper.this.mMediaMetadata = mediaMetadata;
                NotificationMediaTemplateViewWrapper.this.updateDuration();
            }
        }
    };
    /* access modifiers changed from: private */
    public MediaController mMediaController;
    private NotificationMediaManager mMediaManager;
    /* access modifiers changed from: private */
    public MediaMetadata mMediaMetadata;
    /* access modifiers changed from: private */
    public MetricsLogger mMetricsLogger;
    protected final Runnable mOnUpdateTimerTick = new Runnable() {
        public void run() {
            if (NotificationMediaTemplateViewWrapper.this.mMediaController == null || NotificationMediaTemplateViewWrapper.this.mSeekBar == null) {
                NotificationMediaTemplateViewWrapper.this.clearTimer();
                return;
            }
            PlaybackState playbackState = NotificationMediaTemplateViewWrapper.this.mMediaController.getPlaybackState();
            if (playbackState != null) {
                NotificationMediaTemplateViewWrapper.this.updatePlaybackUi(playbackState);
            } else {
                NotificationMediaTemplateViewWrapper.this.clearTimer();
            }
        }
    };
    /* access modifiers changed from: private */
    public SeekBar mSeekBar;
    private TextView mSeekBarElapsedTime;
    /* access modifiers changed from: private */
    public Timer mSeekBarTimer;
    private TextView mSeekBarTotalTime;
    /* access modifiers changed from: private */
    public View mSeekBarView;
    @VisibleForTesting
    protected SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (NotificationMediaTemplateViewWrapper.this.mMediaController != null) {
                NotificationMediaTemplateViewWrapper.this.mMediaController.getTransportControls().seekTo((long) NotificationMediaTemplateViewWrapper.this.mSeekBar.getProgress());
                NotificationMediaTemplateViewWrapper.this.mMetricsLogger.write(NotificationMediaTemplateViewWrapper.this.newLog(6));
            }
        }
    };
    /* access modifiers changed from: private */
    public MediaNotificationView.VisibilityChangeListener mVisibilityListener = new MediaNotificationView.VisibilityChangeListener() {
        public void onAggregatedVisibilityChanged(boolean z) {
            boolean unused = NotificationMediaTemplateViewWrapper.this.mIsViewVisible = z;
            if (!z || NotificationMediaTemplateViewWrapper.this.mMediaController == null) {
                NotificationMediaTemplateViewWrapper.this.clearTimer();
                return;
            }
            PlaybackState playbackState = NotificationMediaTemplateViewWrapper.this.mMediaController.getPlaybackState();
            if (playbackState != null && playbackState.getState() == 3 && NotificationMediaTemplateViewWrapper.this.mSeekBarTimer == null && NotificationMediaTemplateViewWrapper.this.mSeekBarView != null && NotificationMediaTemplateViewWrapper.this.mSeekBarView.getVisibility() != 8) {
                NotificationMediaTemplateViewWrapper.this.startTimer();
            }
        }
    };

    public boolean shouldClipToRounding(boolean z, boolean z2) {
        return true;
    }

    protected NotificationMediaTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mContext = context;
        this.mMediaManager = (NotificationMediaManager) Dependency.get(NotificationMediaManager.class);
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        MediaNotificationView mediaNotificationView = this.mView;
        if (mediaNotificationView instanceof MediaNotificationView) {
            mediaNotificationView.addVisibilityListener(this.mVisibilityListener);
            this.mView.addOnAttachStateChangeListener(this.mAttachStateListener);
        }
    }

    private void resolveViews() {
        boolean z;
        this.mActions = this.mView.findViewById(16909097);
        this.mIsViewVisible = this.mView.isShown();
        MediaSession.Token token = (MediaSession.Token) this.mRow.getEntry().notification.getNotification().extras.getParcelable("android.mediaSession");
        boolean showCompactMediaSeekbar = this.mMediaManager.getShowCompactMediaSeekbar();
        if (token == null || ("media".equals(this.mView.getTag()) && !showCompactMediaSeekbar)) {
            View view = this.mSeekBarView;
            if (view != null) {
                view.setVisibility(8);
                return;
            }
            return;
        }
        MediaController mediaController = this.mMediaController;
        if (mediaController == null || !mediaController.getSessionToken().equals(token)) {
            MediaController mediaController2 = this.mMediaController;
            if (mediaController2 != null) {
                mediaController2.unregisterCallback(this.mMediaCallback);
            }
            this.mMediaController = new MediaController(this.mContext, token);
            z = true;
        } else {
            z = false;
        }
        this.mMediaMetadata = this.mMediaController.getMetadata();
        MediaMetadata mediaMetadata = this.mMediaMetadata;
        if (mediaMetadata != null) {
            if (mediaMetadata.getLong("android.media.metadata.DURATION") <= 0) {
                View view2 = this.mSeekBarView;
                if (view2 != null && view2.getVisibility() != 8) {
                    this.mSeekBarView.setVisibility(8);
                    this.mMetricsLogger.write(newLog(2));
                    clearTimer();
                    return;
                } else if (this.mSeekBarView == null && z) {
                    this.mMetricsLogger.write(newLog(2));
                    return;
                } else {
                    return;
                }
            } else {
                View view3 = this.mSeekBarView;
                if (view3 != null && view3.getVisibility() == 8) {
                    this.mSeekBarView.setVisibility(0);
                    this.mMetricsLogger.write(newLog(1));
                    updateDuration();
                    startTimer();
                }
            }
        }
        ViewStub viewStub = (ViewStub) this.mView.findViewById(16909178);
        if (viewStub instanceof ViewStub) {
            viewStub.setLayoutInflater(LayoutInflater.from(viewStub.getContext()));
            viewStub.setLayoutResource(17367197);
            this.mSeekBarView = viewStub.inflate();
            this.mMetricsLogger.write(newLog(1));
            this.mSeekBar = (SeekBar) this.mSeekBarView.findViewById(16909176);
            this.mSeekBar.setOnSeekBarChangeListener(this.mSeekListener);
            this.mSeekBarElapsedTime = (TextView) this.mSeekBarView.findViewById(16909174);
            this.mSeekBarTotalTime = (TextView) this.mSeekBarView.findViewById(16909179);
            if (this.mSeekBarTimer == null) {
                MediaController mediaController3 = this.mMediaController;
                if (mediaController3 == null || !canSeekMedia(mediaController3.getPlaybackState())) {
                    setScrubberVisible(false);
                } else {
                    this.mMetricsLogger.write(newLog(3, 1));
                }
                updateDuration();
                startTimer();
                this.mMediaController.registerCallback(this.mMediaCallback);
            }
        }
        updateSeekBarTint(this.mSeekBarView);
    }

    /* access modifiers changed from: private */
    public void startTimer() {
        clearTimer();
        if (this.mIsViewVisible) {
            this.mSeekBarTimer = new Timer(true);
            this.mSeekBarTimer.schedule(new TimerTask() {
                public void run() {
                    NotificationMediaTemplateViewWrapper.this.mHandler.post(NotificationMediaTemplateViewWrapper.this.mOnUpdateTimerTick);
                }
            }, 0, 1000);
        }
    }

    /* access modifiers changed from: private */
    public void clearTimer() {
        Timer timer = this.mSeekBarTimer;
        if (timer != null) {
            timer.cancel();
            this.mSeekBarTimer.purge();
            this.mSeekBarTimer = null;
        }
    }

    public void setRemoved() {
        clearTimer();
        MediaController mediaController = this.mMediaController;
        if (mediaController != null) {
            mediaController.unregisterCallback(this.mMediaCallback);
        }
        MediaNotificationView mediaNotificationView = this.mView;
        if (mediaNotificationView instanceof MediaNotificationView) {
            mediaNotificationView.removeVisibilityListener(this.mVisibilityListener);
            this.mView.removeOnAttachStateChangeListener(this.mAttachStateListener);
        }
    }

    private boolean canSeekMedia(PlaybackState playbackState) {
        return (playbackState == null || (playbackState.getActions() & 256) == 0) ? false : true;
    }

    private void setScrubberVisible(boolean z) {
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null && seekBar.isEnabled() != z) {
            this.mSeekBar.getThumb().setAlpha(z ? 255 : 0);
            this.mSeekBar.setEnabled(z);
            this.mMetricsLogger.write(newLog(3, z ? 1 : 0));
        }
    }

    /* access modifiers changed from: private */
    public void updateDuration() {
        MediaMetadata mediaMetadata = this.mMediaMetadata;
        if (mediaMetadata != null && this.mSeekBar != null) {
            long j = mediaMetadata.getLong("android.media.metadata.DURATION");
            if (this.mDuration != j) {
                this.mDuration = j;
                this.mSeekBar.setMax((int) this.mDuration);
                this.mSeekBarTotalTime.setText(millisecondsToTimeString(j));
            }
        }
    }

    /* access modifiers changed from: private */
    public void updatePlaybackUi(PlaybackState playbackState) {
        if (this.mSeekBar != null && this.mSeekBarElapsedTime != null) {
            long position = playbackState.getPosition();
            this.mSeekBar.setProgress((int) position);
            this.mSeekBarElapsedTime.setText(millisecondsToTimeString(position));
            setScrubberVisible(canSeekMedia(playbackState));
        }
    }

    private String millisecondsToTimeString(long j) {
        return DateUtils.formatElapsedTime(j / 1000);
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        resolveViews();
        super.onContentUpdated(expandableNotificationRow);
    }

    private void updateSeekBarTint(View view) {
        if (view != null && getNotificationHeader() != null) {
            int originalIconColor = getNotificationHeader().getOriginalIconColor();
            this.mSeekBarElapsedTime.setTextColor(originalIconColor);
            this.mSeekBarTotalTime.setTextColor(originalIconColor);
            this.mSeekBarTotalTime.setShadowLayer(1.5f, 1.5f, 1.5f, this.mBackgroundColor);
            ColorStateList valueOf = ColorStateList.valueOf(originalIconColor);
            this.mSeekBar.setThumbTintList(valueOf);
            ColorStateList withAlpha = valueOf.withAlpha(192);
            this.mSeekBar.setProgressTintList(withAlpha);
            this.mSeekBar.setProgressBackgroundTintList(withAlpha.withAlpha(128));
        }
    }

    /* access modifiers changed from: protected */
    public void updateTransformedTypes() {
        super.updateTransformedTypes();
        View view = this.mActions;
        if (view != null) {
            this.mTransformationHelper.addTransformedView(5, view);
        }
    }

    public boolean isDimmable() {
        return getCustomBackgroundColor() == 0;
    }

    /* access modifiers changed from: private */
    public LogMaker newLog(int i) {
        return new LogMaker(1743).setType(i).setPackageName(this.mRow.getEntry().notification.getPackageName());
    }

    private LogMaker newLog(int i, int i2) {
        return new LogMaker(1743).setType(i).setSubtype(i2).setPackageName(this.mRow.getEntry().notification.getPackageName());
    }
}
