package com.android.systemui.statusbar;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.AlertingNotificationManager;
import com.android.systemui.statusbar.NotificationLifetimeExtender;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.Iterator;
import java.util.stream.Stream;

public abstract class AlertingNotificationManager implements NotificationLifetimeExtender {
    protected final ArrayMap<String, AlertEntry> mAlertEntries = new ArrayMap<>();
    /* access modifiers changed from: protected */
    public int mAutoDismissNotificationDecay;
    protected final Clock mClock = new Clock();
    protected final ArraySet<NotificationEntry> mExtendedLifetimeAlertEntries = new ArraySet<>();
    @VisibleForTesting
    public Handler mHandler = new Handler(Looper.getMainLooper());
    protected int mMinimumDisplayTime;
    protected NotificationLifetimeExtender.NotificationSafeToRemoveCallback mNotificationLifetimeFinishedCallback;

    public abstract int getContentFlag();

    /* access modifiers changed from: protected */
    public abstract void onAlertEntryAdded(AlertEntry alertEntry);

    /* access modifiers changed from: protected */
    public abstract void onAlertEntryRemoved(AlertEntry alertEntry);

    public void showNotification(NotificationEntry notificationEntry) {
        if (Log.isLoggable("AlertNotifManager", 2)) {
            Log.v("AlertNotifManager", "showNotification");
        }
        addAlertEntry(notificationEntry);
        updateNotification(notificationEntry.key, true);
        notificationEntry.setInterruption();
    }

    public boolean removeNotification(String str, boolean z) {
        if (Log.isLoggable("AlertNotifManager", 2)) {
            Log.v("AlertNotifManager", "removeNotification");
        }
        AlertEntry alertEntry = this.mAlertEntries.get(str);
        if (alertEntry == null) {
            return true;
        }
        if (z || canRemoveImmediately(str)) {
            removeAlertEntry(str);
            return true;
        }
        alertEntry.removeAsSoonAsPossible();
        return false;
    }

    public void updateNotification(String str, boolean z) {
        if (Log.isLoggable("AlertNotifManager", 2)) {
            Log.v("AlertNotifManager", "updateNotification");
        }
        AlertEntry alertEntry = this.mAlertEntries.get(str);
        if (alertEntry != null) {
            alertEntry.mEntry.sendAccessibilityEvent(2048);
            if (z) {
                alertEntry.updateEntry(true);
            }
        }
    }

    public void releaseAllImmediately() {
        if (Log.isLoggable("AlertNotifManager", 2)) {
            Log.v("AlertNotifManager", "releaseAllImmediately");
        }
        Iterator it = new ArraySet(this.mAlertEntries.keySet()).iterator();
        while (it.hasNext()) {
            removeAlertEntry((String) it.next());
        }
    }

    public Stream<NotificationEntry> getAllEntries() {
        return this.mAlertEntries.values().stream().map(C1059x1f55dbd7.INSTANCE);
    }

    public boolean hasNotifications() {
        return !this.mAlertEntries.isEmpty();
    }

    public boolean isAlerting(String str) {
        return this.mAlertEntries.containsKey(str);
    }

    /* access modifiers changed from: protected */
    public final void addAlertEntry(NotificationEntry notificationEntry) {
        AlertEntry createAlertEntry = createAlertEntry();
        createAlertEntry.setEntry(notificationEntry);
        this.mAlertEntries.put(notificationEntry.key, createAlertEntry);
        onAlertEntryAdded(createAlertEntry);
        notificationEntry.sendAccessibilityEvent(2048);
    }

    /* access modifiers changed from: protected */
    public final void removeAlertEntry(String str) {
        AlertEntry alertEntry = this.mAlertEntries.get(str);
        if (alertEntry != null) {
            NotificationEntry notificationEntry = alertEntry.mEntry;
            this.mAlertEntries.remove(str);
            onAlertEntryRemoved(alertEntry);
            notificationEntry.sendAccessibilityEvent(2048);
            alertEntry.reset();
            if (this.mExtendedLifetimeAlertEntries.contains(notificationEntry)) {
                NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback = this.mNotificationLifetimeFinishedCallback;
                if (notificationSafeToRemoveCallback != null) {
                    notificationSafeToRemoveCallback.onSafeToRemove(str);
                }
                this.mExtendedLifetimeAlertEntries.remove(notificationEntry);
            }
        }
    }

    /* access modifiers changed from: protected */
    public AlertEntry createAlertEntry() {
        return new AlertEntry();
    }

    /* access modifiers changed from: protected */
    public boolean canRemoveImmediately(String str) {
        AlertEntry alertEntry = this.mAlertEntries.get(str);
        return alertEntry == null || alertEntry.wasShownLongEnough() || alertEntry.mEntry.isRowDismissed();
    }

    public void setCallback(NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback) {
        this.mNotificationLifetimeFinishedCallback = notificationSafeToRemoveCallback;
    }

    public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
        return !canRemoveImmediately(notificationEntry.key);
    }

    public void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z) {
        if (z) {
            this.mExtendedLifetimeAlertEntries.add(notificationEntry);
            this.mAlertEntries.get(notificationEntry.key).removeAsSoonAsPossible();
            return;
        }
        this.mExtendedLifetimeAlertEntries.remove(notificationEntry);
    }

    protected class AlertEntry implements Comparable<AlertEntry> {
        public long mEarliestRemovaltime;
        public NotificationEntry mEntry;
        public long mPostTime;
        protected Runnable mRemoveAlertRunnable;

        /* access modifiers changed from: protected */
        public boolean isSticky() {
            return false;
        }

        protected AlertEntry() {
        }

        public /* synthetic */ void lambda$setEntry$0$AlertingNotificationManager$AlertEntry(NotificationEntry notificationEntry) {
            AlertingNotificationManager.this.removeAlertEntry(notificationEntry.key);
        }

        public void setEntry(NotificationEntry notificationEntry) {
            setEntry(notificationEntry, new Runnable(notificationEntry) {
                private final /* synthetic */ NotificationEntry f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AlertingNotificationManager.AlertEntry.this.lambda$setEntry$0$AlertingNotificationManager$AlertEntry(this.f$1);
                }
            });
        }

        public void setEntry(NotificationEntry notificationEntry, Runnable runnable) {
            this.mEntry = notificationEntry;
            this.mRemoveAlertRunnable = runnable;
            this.mPostTime = calculatePostTime();
            updateEntry(true);
        }

        public void updateEntry(boolean z) {
            if (Log.isLoggable("AlertNotifManager", 2)) {
                Log.v("AlertNotifManager", "updateEntry");
            }
            long currentTimeMillis = AlertingNotificationManager.this.mClock.currentTimeMillis();
            this.mEarliestRemovaltime = ((long) AlertingNotificationManager.this.mMinimumDisplayTime) + currentTimeMillis;
            if (z) {
                this.mPostTime = Math.max(this.mPostTime, currentTimeMillis);
            }
            removeAutoRemovalCallbacks();
            if (!isSticky()) {
                AlertingNotificationManager.this.mHandler.postDelayed(this.mRemoveAlertRunnable, Math.max(calculateFinishTime() - currentTimeMillis, (long) AlertingNotificationManager.this.mMinimumDisplayTime));
            }
        }

        public boolean wasShownLongEnough() {
            return this.mEarliestRemovaltime < AlertingNotificationManager.this.mClock.currentTimeMillis();
        }

        public int compareTo(AlertEntry alertEntry) {
            long j = this.mPostTime;
            long j2 = alertEntry.mPostTime;
            if (j < j2) {
                return 1;
            }
            if (j == j2) {
                return this.mEntry.key.compareTo(alertEntry.mEntry.key);
            }
            return -1;
        }

        public void reset() {
            this.mEntry = null;
            removeAutoRemovalCallbacks();
            this.mRemoveAlertRunnable = null;
        }

        public void removeAutoRemovalCallbacks() {
            Runnable runnable = this.mRemoveAlertRunnable;
            if (runnable != null) {
                AlertingNotificationManager.this.mHandler.removeCallbacks(runnable);
            }
        }

        public void removeAsSoonAsPossible() {
            if (this.mRemoveAlertRunnable != null) {
                removeAutoRemovalCallbacks();
                AlertingNotificationManager alertingNotificationManager = AlertingNotificationManager.this;
                alertingNotificationManager.mHandler.postDelayed(this.mRemoveAlertRunnable, this.mEarliestRemovaltime - alertingNotificationManager.mClock.currentTimeMillis());
            }
        }

        /* access modifiers changed from: protected */
        public long calculatePostTime() {
            return AlertingNotificationManager.this.mClock.currentTimeMillis();
        }

        /* access modifiers changed from: protected */
        public long calculateFinishTime() {
            return this.mPostTime + ((long) AlertingNotificationManager.this.mAutoDismissNotificationDecay);
        }
    }

    protected static final class Clock {
        protected Clock() {
        }

        public long currentTimeMillis() {
            return SystemClock.elapsedRealtime();
        }
    }
}
