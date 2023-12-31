package com.android.systemui.assist;

import android.content.Context;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.recents.OverviewProxyService;
import dagger.Lazy;
import java.io.PrintWriter;

final class AssistHandleLikeHomeBehavior implements AssistHandleBehaviorController.BehaviorController {
    private AssistHandleCallbacks mAssistHandleCallbacks;
    private boolean mIsAwake;
    private boolean mIsDozing;
    private boolean mIsHomeHandleHiding;
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() {
        public void onSystemUiStateChanged(int i) {
            AssistHandleLikeHomeBehavior.this.handleSystemUiStateChange(i);
        }
    };
    private final Lazy<OverviewProxyService> mOverviewProxyService;
    private final Lazy<StatusBarStateController> mStatusBarStateController;
    private final StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() {
        public void onDozingChanged(boolean z) {
            AssistHandleLikeHomeBehavior.this.handleDozingChanged(z);
        }
    };
    private final Lazy<WakefulnessLifecycle> mWakefulnessLifecycle;
    private final WakefulnessLifecycle.Observer mWakefulnessLifecycleObserver = new WakefulnessLifecycle.Observer() {
        public void onStartedWakingUp() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }

        public void onFinishedWakingUp() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(true);
        }

        public void onStartedGoingToSleep() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }

        public void onFinishedGoingToSleep() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }
    };

    private static boolean isHomeHandleHiding(int i) {
        return (i & 2) != 0;
    }

    AssistHandleLikeHomeBehavior(Lazy<StatusBarStateController> lazy, Lazy<WakefulnessLifecycle> lazy2, Lazy<OverviewProxyService> lazy3) {
        this.mStatusBarStateController = lazy;
        this.mWakefulnessLifecycle = lazy2;
        this.mOverviewProxyService = lazy3;
    }

    public void onModeActivated(Context context, AssistHandleCallbacks assistHandleCallbacks) {
        this.mAssistHandleCallbacks = assistHandleCallbacks;
        this.mIsDozing = this.mStatusBarStateController.get().isDozing();
        this.mStatusBarStateController.get().addCallback(this.mStatusBarStateListener);
        this.mIsAwake = this.mWakefulnessLifecycle.get().getWakefulness() == 2;
        this.mWakefulnessLifecycle.get().addObserver(this.mWakefulnessLifecycleObserver);
        this.mOverviewProxyService.get().addCallback(this.mOverviewProxyListener);
        callbackForCurrentState();
    }

    public void onModeDeactivated() {
        this.mAssistHandleCallbacks = null;
        this.mStatusBarStateController.get().removeCallback(this.mStatusBarStateListener);
        this.mWakefulnessLifecycle.get().removeObserver(this.mWakefulnessLifecycleObserver);
        this.mOverviewProxyService.get().removeCallback(this.mOverviewProxyListener);
    }

    /* access modifiers changed from: private */
    public void handleDozingChanged(boolean z) {
        if (this.mIsDozing != z) {
            this.mIsDozing = z;
            callbackForCurrentState();
        }
    }

    /* access modifiers changed from: private */
    public void handleWakefullnessChanged(boolean z) {
        if (this.mIsAwake != z) {
            this.mIsAwake = z;
            callbackForCurrentState();
        }
    }

    /* access modifiers changed from: private */
    public void handleSystemUiStateChange(int i) {
        boolean isHomeHandleHiding = isHomeHandleHiding(i);
        if (this.mIsHomeHandleHiding != isHomeHandleHiding) {
            this.mIsHomeHandleHiding = isHomeHandleHiding;
            callbackForCurrentState();
        }
    }

    private void callbackForCurrentState() {
        if (this.mAssistHandleCallbacks != null) {
            if (this.mIsHomeHandleHiding || !isFullyAwake()) {
                this.mAssistHandleCallbacks.hide();
            } else {
                this.mAssistHandleCallbacks.showAndStay();
            }
        }
    }

    private boolean isFullyAwake() {
        return this.mIsAwake && !this.mIsDozing;
    }

    public void dump(PrintWriter printWriter, String str) {
        printWriter.println(str + "Current AssistHandleLikeHomeBehavior State:");
        printWriter.println(str + "   mIsDozing=" + this.mIsDozing);
        printWriter.println(str + "   mIsAwake=" + this.mIsAwake);
        printWriter.println(str + "   mIsHomeHandleHiding=" + this.mIsHomeHandleHiding);
    }
}
