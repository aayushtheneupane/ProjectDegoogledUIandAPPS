package com.android.systemui.statusbar.notification;

import android.content.Intent;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public interface NotificationActivityStarter {
    boolean isCollapsingToShowActivityOverLockscreen() {
        return false;
    }

    void onNotificationClicked(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow);

    void startNotificationGutsIntent(Intent intent, int i, ExpandableNotificationRow expandableNotificationRow);
}
