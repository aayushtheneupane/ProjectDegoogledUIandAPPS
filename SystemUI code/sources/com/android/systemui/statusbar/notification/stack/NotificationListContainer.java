package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.VisibilityLocationProvider;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;

public interface NotificationListContainer extends ExpandableView.OnHeightChangedListener, VisibilityLocationProvider {
    void addContainerView(View view);

    void applyExpandAnimationParams(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
    }

    void bindRow(ExpandableNotificationRow expandableNotificationRow) {
    }

    void changeViewPosition(ExpandableView expandableView, int i);

    void cleanUpViewStateForEntry(NotificationEntry notificationEntry);

    boolean containsView(View view) {
        return true;
    }

    void generateAddAnimation(ExpandableView expandableView, boolean z);

    void generateChildOrderChangedEvent();

    View getContainerChildAt(int i);

    int getContainerChildCount();

    NotificationSwipeActionHelper getSwipeActionHelper();

    ViewGroup getViewParentForNotification(NotificationEntry notificationEntry);

    boolean hasPulsingNotifications();

    void notifyGroupChildAdded(ExpandableView expandableView);

    void notifyGroupChildRemoved(ExpandableView expandableView, ViewGroup viewGroup);

    void onNotificationViewUpdateFinished() {
    }

    void removeContainerView(View view);

    void resetExposedMenuView(boolean z, boolean z2);

    void setChildLocationsChangedListener(NotificationLogger.OnChildLocationsChangedListener onChildLocationsChangedListener);

    void setChildTransferInProgress(boolean z);

    void setExpandingNotification(ExpandableNotificationRow expandableNotificationRow) {
    }

    void setMaxDisplayedNotifications(int i);

    void setWillExpand(boolean z) {
    }
}
