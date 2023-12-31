package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.SwipeHelper;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;

class NotificationSwipeHelper extends SwipeHelper implements NotificationSwipeActionHelper {
    @VisibleForTesting
    protected static final long COVER_MENU_DELAY = 4000;
    private final NotificationCallback mCallback;
    private NotificationMenuRowPlugin mCurrMenuRow;
    private final Runnable mFalsingCheck = new Runnable() {
        public void run() {
            NotificationSwipeHelper.this.resetExposedMenuView(true, true);
        }
    };
    private boolean mIsExpanded;
    private View mMenuExposedView;
    private final NotificationMenuRowPlugin.OnMenuEventListener mMenuListener;
    private boolean mPulsing;
    private View mTranslatingParentView;

    public interface NotificationCallback extends SwipeHelper.Callback {
        void handleChildViewDismissed(View view);

        void onDismiss();

        void onSnooze(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption);

        boolean shouldDismissQuickly();
    }

    NotificationSwipeHelper(int i, NotificationCallback notificationCallback, Context context, NotificationMenuRowPlugin.OnMenuEventListener onMenuEventListener, FalsingManager falsingManager) {
        super(i, notificationCallback, context, falsingManager);
        this.mMenuListener = onMenuEventListener;
        this.mCallback = notificationCallback;
    }

    public View getTranslatingParentView() {
        return this.mTranslatingParentView;
    }

    public void clearTranslatingParentView() {
        setTranslatingParentView((View) null);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setTranslatingParentView(View view) {
        this.mTranslatingParentView = view;
    }

    public void setExposedMenuView(View view) {
        this.mMenuExposedView = view;
    }

    public void clearExposedMenuView() {
        setExposedMenuView((View) null);
    }

    public void clearCurrentMenuRow() {
        setCurrentMenuRow((NotificationMenuRowPlugin) null);
    }

    public View getExposedMenuView() {
        return this.mMenuExposedView;
    }

    public void setCurrentMenuRow(NotificationMenuRowPlugin notificationMenuRowPlugin) {
        this.mCurrMenuRow = notificationMenuRowPlugin;
    }

    public NotificationMenuRowPlugin getCurrentMenuRow() {
        return this.mCurrMenuRow;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Handler getHandler() {
        return this.mHandler;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Runnable getFalsingCheck() {
        return this.mFalsingCheck;
    }

    public void setIsExpanded(boolean z) {
        this.mIsExpanded = z;
    }

    /* access modifiers changed from: protected */
    public void onChildSnappedBack(View view, float f) {
        NotificationMenuRowPlugin notificationMenuRowPlugin = this.mCurrMenuRow;
        if (notificationMenuRowPlugin != null && f == 0.0f) {
            notificationMenuRowPlugin.resetMenu();
            clearCurrentMenuRow();
        }
    }

    public void onDownUpdate(View view, MotionEvent motionEvent) {
        this.mTranslatingParentView = view;
        NotificationMenuRowPlugin currentMenuRow = getCurrentMenuRow();
        if (currentMenuRow != null) {
            currentMenuRow.onTouchStart();
        }
        clearCurrentMenuRow();
        getHandler().removeCallbacks(getFalsingCheck());
        resetExposedMenuView(true, false);
        if (view instanceof ExpandableNotificationRow) {
            initializeRow((ExpandableNotificationRow) view);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void initializeRow(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow.getEntry().hasFinishedInitialization()) {
            this.mCurrMenuRow = expandableNotificationRow.createMenu();
            NotificationMenuRowPlugin notificationMenuRowPlugin = this.mCurrMenuRow;
            if (notificationMenuRowPlugin != null) {
                notificationMenuRowPlugin.setMenuClickListener(this.mMenuListener);
                this.mCurrMenuRow.onTouchStart();
            }
        }
    }

    private boolean swipedEnoughToShowMenu(NotificationMenuRowPlugin notificationMenuRowPlugin) {
        return !swipedFarEnough() && notificationMenuRowPlugin.isSwipedEnoughToShowMenu();
    }

    public void onMoveUpdate(View view, MotionEvent motionEvent, float f, float f2) {
        getHandler().removeCallbacks(getFalsingCheck());
        NotificationMenuRowPlugin currentMenuRow = getCurrentMenuRow();
        if (currentMenuRow != null) {
            currentMenuRow.onTouchMove(f2);
        }
    }

    public boolean handleUpEvent(MotionEvent motionEvent, View view, float f, float f2) {
        NotificationMenuRowPlugin currentMenuRow = getCurrentMenuRow();
        if (currentMenuRow == null) {
            return false;
        }
        currentMenuRow.onTouchEnd();
        handleMenuRowSwipe(motionEvent, view, f, currentMenuRow);
        return true;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void handleMenuRowSwipe(MotionEvent motionEvent, View view, float f, NotificationMenuRowPlugin notificationMenuRowPlugin) {
        if (!notificationMenuRowPlugin.shouldShowMenu()) {
            if (isDismissGesture(motionEvent)) {
                dismiss(view, f);
                return;
            }
            snapClosed(view, f);
            notificationMenuRowPlugin.onSnapClosed();
        } else if (notificationMenuRowPlugin.isSnappedAndOnSameSide()) {
            handleSwipeFromOpenState(motionEvent, view, f, notificationMenuRowPlugin);
        } else {
            handleSwipeFromClosedState(motionEvent, view, f, notificationMenuRowPlugin);
        }
    }

    private void handleSwipeFromClosedState(MotionEvent motionEvent, View view, float f, NotificationMenuRowPlugin notificationMenuRowPlugin) {
        boolean isDismissGesture = isDismissGesture(motionEvent);
        boolean isTowardsMenu = notificationMenuRowPlugin.isTowardsMenu(f);
        boolean z = true;
        boolean z2 = getEscapeVelocity() <= Math.abs(f);
        boolean z3 = !notificationMenuRowPlugin.canBeDismissed() && ((double) (motionEvent.getEventTime() - motionEvent.getDownTime())) >= 200.0d;
        boolean z4 = isTowardsMenu && !isDismissGesture;
        boolean z5 = (swipedEnoughToShowMenu(notificationMenuRowPlugin) && (!z2 || z3)) || ((z2 && !isTowardsMenu && !isDismissGesture) && (notificationMenuRowPlugin.shouldShowGutsOnSnapOpen() || (this.mIsExpanded && !this.mPulsing)));
        int menuSnapTarget = notificationMenuRowPlugin.getMenuSnapTarget();
        if (isFalseGesture(motionEvent) || !z5) {
            z = false;
        }
        if ((z4 || z) && menuSnapTarget != 0) {
            snapOpen(view, menuSnapTarget, f);
            notificationMenuRowPlugin.onSnapOpen();
        } else if (!isDismissGesture(motionEvent) || isTowardsMenu) {
            snapClosed(view, f);
            notificationMenuRowPlugin.onSnapClosed();
        } else {
            dismiss(view, f);
            notificationMenuRowPlugin.onDismiss();
        }
    }

    private void handleSwipeFromOpenState(MotionEvent motionEvent, View view, float f, NotificationMenuRowPlugin notificationMenuRowPlugin) {
        boolean isDismissGesture = isDismissGesture(motionEvent);
        if (notificationMenuRowPlugin.isWithinSnapMenuThreshold() && !isDismissGesture) {
            notificationMenuRowPlugin.onSnapOpen();
            snapOpen(view, notificationMenuRowPlugin.getMenuSnapTarget(), f);
        } else if (!isDismissGesture || notificationMenuRowPlugin.shouldSnapBack()) {
            snapClosed(view, f);
            notificationMenuRowPlugin.onSnapClosed();
        } else {
            dismiss(view, f);
            notificationMenuRowPlugin.onDismiss();
        }
    }

    public void dismissChild(View view, float f, boolean z) {
        superDismissChild(view, f, z);
        if (this.mCallback.shouldDismissQuickly()) {
            this.mCallback.handleChildViewDismissed(view);
        }
        this.mCallback.onDismiss();
        handleMenuCoveredOrDismissed();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void superDismissChild(View view, float f, boolean z) {
        super.dismissChild(view, f, z);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void superSnapChild(View view, float f, float f2) {
        super.snapChild(view, f, f2);
    }

    public void snapChild(View view, float f, float f2) {
        superSnapChild(view, f, f2);
        this.mCallback.onDragCancelled(view);
        if (f == 0.0f) {
            handleMenuCoveredOrDismissed();
        }
    }

    public void snooze(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        this.mCallback.onSnooze(statusBarNotification, snoozeOption);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void handleMenuCoveredOrDismissed() {
        View exposedMenuView = getExposedMenuView();
        if (exposedMenuView != null && exposedMenuView == this.mTranslatingParentView) {
            clearExposedMenuView();
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Animator superGetViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        return super.getViewTranslationAnimator(view, f, animatorUpdateListener);
    }

    public Animator getViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        if (view instanceof ExpandableNotificationRow) {
            return ((ExpandableNotificationRow) view).getTranslateViewAnimator(f, animatorUpdateListener);
        }
        return superGetViewTranslationAnimator(view, f, animatorUpdateListener);
    }

    public void setTranslation(View view, float f) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setTranslation(f);
        }
    }

    public float getTranslation(View view) {
        if (view instanceof ExpandableNotificationRow) {
            return ((ExpandableNotificationRow) view).getTranslation();
        }
        return 0.0f;
    }

    public boolean swipedFastEnough(float f, float f2) {
        return swipedFastEnough();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean swipedFastEnough() {
        return super.swipedFastEnough();
    }

    public boolean swipedFarEnough(float f, float f2) {
        return swipedFarEnough();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean swipedFarEnough() {
        return super.swipedFarEnough();
    }

    public void dismiss(View view, float f) {
        dismissChild(view, f, !swipedFastEnough());
    }

    public void snapOpen(View view, int i, float f) {
        snapChild(view, (float) i, f);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void snapClosed(View view, float f) {
        snapChild(view, 0.0f, f);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public float getEscapeVelocity() {
        return super.getEscapeVelocity();
    }

    public float getMinDismissVelocity() {
        return getEscapeVelocity();
    }

    public void onMenuShown(View view) {
        setExposedMenuView(getTranslatingParentView());
        this.mCallback.onDragCancelled(view);
        Handler handler = getHandler();
        if (this.mCallback.isAntiFalsingNeeded()) {
            handler.removeCallbacks(getFalsingCheck());
            handler.postDelayed(getFalsingCheck(), COVER_MENU_DELAY);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean shouldResetMenu(boolean z) {
        View view = this.mMenuExposedView;
        if (view != null) {
            return z || view != this.mTranslatingParentView;
        }
        return false;
    }

    public void resetExposedMenuView(boolean z, boolean z2) {
        if (shouldResetMenu(z2)) {
            View exposedMenuView = getExposedMenuView();
            if (z) {
                Animator viewTranslationAnimator = getViewTranslationAnimator(exposedMenuView, 0.0f, (ValueAnimator.AnimatorUpdateListener) null);
                if (viewTranslationAnimator != null) {
                    viewTranslationAnimator.start();
                }
            } else if (exposedMenuView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) exposedMenuView;
                if (!expandableNotificationRow.isRemoved()) {
                    expandableNotificationRow.resetTranslation();
                }
            }
            clearExposedMenuView();
        }
    }

    public static boolean isTouchInView(MotionEvent motionEvent, View view) {
        int i;
        if (view == null) {
            return false;
        }
        if (view instanceof ExpandableView) {
            i = ((ExpandableView) view).getActualHeight();
        } else {
            i = view.getHeight();
        }
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        int i2 = iArr[0];
        int i3 = iArr[1];
        return new Rect(i2, i3, view.getWidth() + i2, i + i3).contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    public void setPulsing(boolean z) {
        this.mPulsing = z;
    }
}
