package com.android.systemui.statusbar.notification;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.PanelExpansionListener;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: NotificationWakeUpCoordinator.kt */
public final class NotificationWakeUpCoordinator implements OnHeadsUpChangedListener, StatusBarStateController.StateListener, PanelExpansionListener {
    private final KeyguardBypassController bypassController;
    private boolean collapsedEnoughToHide;
    private boolean fullyAwake;
    public NotificationIconAreaController iconAreaController;
    private final Context mContext;
    private float mDozeAmount;
    private final DozeParameters mDozeParameters;
    private final Set<NotificationEntry> mEntrySetToClearWhenFinished = new LinkedHashSet();
    private final HeadsUpManagerPhone mHeadsUpManagerPhone;
    private float mLinearDozeAmount;
    /* access modifiers changed from: private */
    public float mLinearVisibilityAmount;
    private final NotificationWakeUpCoordinator$mNotificationVisibility$1 mNotificationVisibility = new NotificationWakeUpCoordinator$mNotificationVisibility$1("notificationVisibility");
    private float mNotificationVisibleAmount;
    private boolean mNotificationsVisible;
    /* access modifiers changed from: private */
    public boolean mNotificationsVisibleForExpansion;
    private NotificationStackScrollLayout mStackScroller;
    private float mVisibilityAmount;
    private ObjectAnimator mVisibilityAnimator;
    private Interpolator mVisibilityInterpolator = Interpolators.FAST_OUT_SLOW_IN_REVERSE;
    private boolean notificationsFullyHidden;
    /* access modifiers changed from: private */
    public boolean pulseExpanding;
    private boolean pulsing;
    private int state = 1;
    private final StatusBarStateController statusBarStateController;
    /* access modifiers changed from: private */
    public final ArrayList<WakeUpListener> wakeUpListeners = new ArrayList<>();
    private boolean wakingUp;
    private boolean willWakeUp;

    /* compiled from: NotificationWakeUpCoordinator.kt */
    public interface WakeUpListener {
        void onFullyHiddenChanged(boolean z) {
        }

        void onPulseExpansionChanged(boolean z) {
        }
    }

    public NotificationWakeUpCoordinator(Context context, HeadsUpManagerPhone headsUpManagerPhone, StatusBarStateController statusBarStateController2, KeyguardBypassController keyguardBypassController) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone, "mHeadsUpManagerPhone");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        this.mContext = context;
        this.mHeadsUpManagerPhone = headsUpManagerPhone;
        this.statusBarStateController = statusBarStateController2;
        this.bypassController = keyguardBypassController;
        this.mHeadsUpManagerPhone.addListener(this);
        this.statusBarStateController.addCallback(this);
        DozeParameters instance = DozeParameters.getInstance(this.mContext);
        Intrinsics.checkExpressionValueIsNotNull(instance, "DozeParameters.getInstance(mContext)");
        this.mDozeParameters = instance;
        addListener(new WakeUpListener(this) {
            final /* synthetic */ NotificationWakeUpCoordinator this$0;

            {
                this.this$0 = r1;
            }

            public void onFullyHiddenChanged(boolean z) {
                if (z && this.this$0.mNotificationsVisibleForExpansion) {
                    this.this$0.setNotificationsVisibleForExpansion(false, false, false);
                }
            }
        });
    }

    public final void setFullyAwake(boolean z) {
        this.fullyAwake = z;
    }

    public final void setWakingUp(boolean z) {
        this.wakingUp = z;
        setWillWakeUp(false);
        if (z) {
            if (this.mNotificationsVisible && !this.mNotificationsVisibleForExpansion && !this.bypassController.getBypassEnabled()) {
                NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
                if (notificationStackScrollLayout != null) {
                    notificationStackScrollLayout.wakeUpFromPulse();
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
                    throw null;
                }
            }
            if (this.bypassController.getBypassEnabled() && !this.mNotificationsVisible) {
                updateNotificationVisibility(shouldAnimateVisibility(), false);
            }
        }
    }

    public final void setWillWakeUp(boolean z) {
        if (!z || this.mDozeAmount != 0.0f) {
            this.willWakeUp = z;
        }
    }

    public final void setIconAreaController(NotificationIconAreaController notificationIconAreaController) {
        Intrinsics.checkParameterIsNotNull(notificationIconAreaController, "<set-?>");
        this.iconAreaController = notificationIconAreaController;
    }

    public final void setPulsing(boolean z) {
        this.pulsing = z;
        if (z) {
            updateNotificationVisibility(shouldAnimateVisibility(), false);
        }
    }

    public final boolean getNotificationsFullyHidden() {
        return this.notificationsFullyHidden;
    }

    private final void setNotificationsFullyHidden(boolean z) {
        if (this.notificationsFullyHidden != z) {
            this.notificationsFullyHidden = z;
            Iterator<WakeUpListener> it = this.wakeUpListeners.iterator();
            while (it.hasNext()) {
                it.next().onFullyHiddenChanged(z);
            }
        }
    }

    public final boolean getCanShowPulsingHuns() {
        boolean z = this.pulsing;
        if (!this.bypassController.getBypassEnabled()) {
            return z;
        }
        boolean z2 = z || ((this.wakingUp || this.willWakeUp || this.fullyAwake) && this.statusBarStateController.getState() == 1);
        if (this.collapsedEnoughToHide) {
            return false;
        }
        return z2;
    }

    public final void setStackScroller(NotificationStackScrollLayout notificationStackScrollLayout) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "stackScroller");
        this.mStackScroller = notificationStackScrollLayout;
        this.pulseExpanding = notificationStackScrollLayout.isPulseExpanding();
        notificationStackScrollLayout.setOnPulseHeightChangedListener(new NotificationWakeUpCoordinator$setStackScroller$1(this));
    }

    public final boolean isPulseExpanding() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            return notificationStackScrollLayout.isPulseExpanding();
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    public final void setNotificationsVisibleForExpansion(boolean z, boolean z2, boolean z3) {
        this.mNotificationsVisibleForExpansion = z;
        updateNotificationVisibility(z2, z3);
        if (!z && this.mNotificationsVisible) {
            this.mHeadsUpManagerPhone.releaseAllImmediately();
        }
    }

    public final void addListener(WakeUpListener wakeUpListener) {
        Intrinsics.checkParameterIsNotNull(wakeUpListener, "listener");
        this.wakeUpListeners.add(wakeUpListener);
    }

    public final void removeListener(WakeUpListener wakeUpListener) {
        Intrinsics.checkParameterIsNotNull(wakeUpListener, "listener");
        this.wakeUpListeners.remove(wakeUpListener);
    }

    private final void updateNotificationVisibility(boolean z, boolean z2) {
        boolean z3 = false;
        if ((this.mNotificationsVisibleForExpansion || this.mHeadsUpManagerPhone.hasNotifications()) && getCanShowPulsingHuns()) {
            z3 = true;
        }
        if (z3 || !this.mNotificationsVisible || ((!this.wakingUp && !this.willWakeUp) || this.mDozeAmount == 0.0f)) {
            setNotificationsVisible(z3, z, z2);
        }
    }

    private final void setNotificationsVisible(boolean z, boolean z2, boolean z3) {
        if (this.mNotificationsVisible != z) {
            this.mNotificationsVisible = z;
            ObjectAnimator objectAnimator = this.mVisibilityAnimator;
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            if (z2) {
                notifyAnimationStart(z);
                startVisibilityAnimation(z3);
                return;
            }
            setVisibilityAmount(z ? 1.0f : 0.0f);
        }
    }

    public void onDozeAmountChanged(float f, float f2) {
        if (!updateDozeAmountIfBypass()) {
            if (!(f == 1.0f || f == 0.0f)) {
                float f3 = this.mLinearDozeAmount;
                if (f3 == 0.0f || f3 == 1.0f) {
                    notifyAnimationStart(this.mLinearDozeAmount == 1.0f);
                }
            }
            setDozeAmount(f, f2);
        }
    }

    public final void setDozeAmount(float f, float f2) {
        boolean z = f != this.mLinearDozeAmount;
        this.mLinearDozeAmount = f;
        this.mDozeAmount = f2;
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.setDozeAmount(this.mDozeAmount);
            updateHideAmount();
            if (z && f == 0.0f) {
                setNotificationsVisible(false, false, false);
                setNotificationsVisibleForExpansion(false, false, false);
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    public void onStateChanged(int i) {
        updateDozeAmountIfBypass();
        if (this.bypassController.getBypassEnabled() && i == 1 && this.state == 2 && (!this.statusBarStateController.isDozing() || shouldAnimateVisibility())) {
            setNotificationsVisible(true, false, false);
            setNotificationsVisible(false, true, false);
        }
        this.state = i;
    }

    public void onPanelExpansionChanged(float f, boolean z) {
        boolean z2 = f <= 0.9f;
        if (z2 != this.collapsedEnoughToHide) {
            boolean canShowPulsingHuns = getCanShowPulsingHuns();
            this.collapsedEnoughToHide = z2;
            if (canShowPulsingHuns && !getCanShowPulsingHuns()) {
                updateNotificationVisibility(true, true);
                this.mHeadsUpManagerPhone.releaseAllImmediately();
            }
        }
    }

    private final boolean updateDozeAmountIfBypass() {
        if (!this.bypassController.getBypassEnabled()) {
            return false;
        }
        float f = 1.0f;
        if (this.statusBarStateController.getState() == 0 || this.statusBarStateController.getState() == 2) {
            f = 0.0f;
        }
        setDozeAmount(f, f);
        return true;
    }

    private final void startVisibilityAnimation(boolean z) {
        Interpolator interpolator;
        float f = this.mNotificationVisibleAmount;
        float f2 = 0.0f;
        if (f == 0.0f || f == 1.0f) {
            if (this.mNotificationsVisible) {
                interpolator = Interpolators.TOUCH_RESPONSE;
            } else {
                interpolator = Interpolators.FAST_OUT_SLOW_IN_REVERSE;
            }
            this.mVisibilityInterpolator = interpolator;
        }
        if (this.mNotificationsVisible) {
            f2 = 1.0f;
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, this.mNotificationVisibility, new float[]{f2});
        ofFloat.setInterpolator(Interpolators.LINEAR);
        long j = (long) 500;
        if (z) {
            j = (long) (((float) j) / 1.5f);
        }
        ofFloat.setDuration(j);
        ofFloat.start();
        this.mVisibilityAnimator = ofFloat;
    }

    /* access modifiers changed from: private */
    public final void setVisibilityAmount(float f) {
        this.mLinearVisibilityAmount = f;
        this.mVisibilityAmount = this.mVisibilityInterpolator.getInterpolation(f);
        handleAnimationFinished();
        updateHideAmount();
    }

    private final void handleAnimationFinished() {
        if (this.mLinearDozeAmount == 0.0f || this.mLinearVisibilityAmount == 0.0f) {
            for (NotificationEntry headsUpAnimatingAway : this.mEntrySetToClearWhenFinished) {
                headsUpAnimatingAway.setHeadsUpAnimatingAway(false);
            }
            this.mEntrySetToClearWhenFinished.clear();
        }
    }

    public final float getWakeUpHeight() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            return notificationStackScrollLayout.getWakeUpHeight();
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    private final void updateHideAmount() {
        float min = Math.min(1.0f - this.mLinearVisibilityAmount, this.mLinearDozeAmount);
        float min2 = Math.min(1.0f - this.mVisibilityAmount, this.mDozeAmount);
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.setHideAmount(min, min2);
            setNotificationsFullyHidden(min == 1.0f);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    private final void notifyAnimationStart(boolean z) {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.notifyHideAnimationStart(!z);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
            throw null;
        }
    }

    public void onDozingChanged(boolean z) {
        if (z) {
            setNotificationsVisible(false, false, false);
        }
    }

    public final float setPulseHeight(float f) {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            float pulseHeight = notificationStackScrollLayout.setPulseHeight(f);
            if (this.bypassController.getBypassEnabled()) {
                return 0.0f;
            }
            return pulseHeight;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        boolean shouldAnimateVisibility = shouldAnimateVisibility();
        if (!z) {
            if (!(this.mLinearDozeAmount == 0.0f || this.mLinearVisibilityAmount == 0.0f)) {
                if (notificationEntry.isRowDismissed()) {
                    shouldAnimateVisibility = false;
                } else if (!this.wakingUp && !this.willWakeUp) {
                    notificationEntry.setHeadsUpAnimatingAway(true);
                    this.mEntrySetToClearWhenFinished.add(notificationEntry);
                }
            }
        } else if (this.mEntrySetToClearWhenFinished.contains(notificationEntry)) {
            this.mEntrySetToClearWhenFinished.remove(notificationEntry);
            notificationEntry.setHeadsUpAnimatingAway(false);
        }
        updateNotificationVisibility(shouldAnimateVisibility, false);
    }

    private final boolean shouldAnimateVisibility() {
        return this.mDozeParameters.getAlwaysOn() && !this.mDozeParameters.getDisplayNeedsBlanking();
    }
}
