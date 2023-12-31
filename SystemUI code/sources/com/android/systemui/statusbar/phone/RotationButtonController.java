package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.IRotationWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManagerGlobal;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.view.RotationPolicy;
import com.android.systemui.C1785R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.phone.RotationButtonController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.statusbar.policy.RotationLockController;
import java.util.Optional;
import java.util.function.Consumer;

public class RotationButtonController {
    private AccessibilityManagerWrapper mAccessibilityManagerWrapper;
    private final Runnable mCancelPendingRotationProposal = new Runnable() {
        public final void run() {
            RotationButtonController.this.lambda$new$1$RotationButtonController();
        }
    };
    private final Context mContext;
    private boolean mHoveringRotationSuggestion;
    private boolean mIsNavigationBarShowing;
    private int mLastRotationSuggestion;
    private boolean mListenersRegistered = false;
    /* access modifiers changed from: private */
    public final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private final MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));
    private boolean mPendingRotationSuggestion;
    private final Runnable mRemoveRotationProposal = new Runnable() {
        public final void run() {
            RotationButtonController.this.lambda$new$0$RotationButtonController();
        }
    };
    /* access modifiers changed from: private */
    public Consumer<Integer> mRotWatcherListener;
    private Animator mRotateHideAnimator;
    /* access modifiers changed from: private */
    public final RotationButton mRotationButton;
    /* access modifiers changed from: private */
    public RotationLockController mRotationLockController;
    private final IRotationWatcher.Stub mRotationWatcher = new IRotationWatcher.Stub() {
        public void onRotationChanged(int i) throws RemoteException {
            RotationButtonController.this.mMainThreadHandler.postAtFrontOfQueue(new Runnable(i) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    RotationButtonController.C14431.this.lambda$onRotationChanged$0$RotationButtonController$1(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onRotationChanged$0$RotationButtonController$1(int i) {
            if (RotationButtonController.this.mRotationLockController.isRotationLocked()) {
                if (RotationButtonController.this.shouldOverrideUserLockPrefs(i)) {
                    RotationButtonController.this.setRotationLockedAtAngle(i);
                }
                RotationButtonController.this.setRotateSuggestionButtonState(false, true);
            }
            if (RotationButtonController.this.mRotWatcherListener != null) {
                RotationButtonController.this.mRotWatcherListener.accept(Integer.valueOf(i));
            }
        }
    };
    private int mStyleRes;
    private TaskStackListenerImpl mTaskStackListener;
    private final ViewRippler mViewRippler = new ViewRippler();

    static boolean hasDisable2RotateSuggestionFlag(int i) {
        return (i & 16) != 0;
    }

    private boolean isRotationAnimationCCW(int i, int i2) {
        if (i == 0 && i2 == 1) {
            return false;
        }
        if (i == 0 && i2 == 2) {
            return true;
        }
        if (i == 0 && i2 == 3) {
            return true;
        }
        if (i == 1 && i2 == 0) {
            return true;
        }
        if (i == 1 && i2 == 2) {
            return false;
        }
        if (i == 1 && i2 == 3) {
            return true;
        }
        if (i == 2 && i2 == 0) {
            return true;
        }
        if (i == 2 && i2 == 1) {
            return true;
        }
        if (i == 2 && i2 == 3) {
            return false;
        }
        if (i == 3 && i2 == 0) {
            return false;
        }
        if (i == 3 && i2 == 1) {
            return true;
        }
        return i == 3 && i2 == 2;
    }

    public /* synthetic */ void lambda$new$0$RotationButtonController() {
        setRotateSuggestionButtonState(false);
    }

    public /* synthetic */ void lambda$new$1$RotationButtonController() {
        this.mPendingRotationSuggestion = false;
    }

    RotationButtonController(Context context, int i, RotationButton rotationButton) {
        this.mContext = context;
        this.mRotationButton = rotationButton;
        this.mRotationButton.setRotationButtonController(this);
        this.mStyleRes = i;
        this.mIsNavigationBarShowing = true;
        this.mRotationLockController = (RotationLockController) Dependency.get(RotationLockController.class);
        this.mAccessibilityManagerWrapper = (AccessibilityManagerWrapper) Dependency.get(AccessibilityManagerWrapper.class);
        this.mTaskStackListener = new TaskStackListenerImpl();
        this.mRotationButton.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                RotationButtonController.this.onRotateSuggestionClick(view);
            }
        });
        this.mRotationButton.setOnHoverListener(new View.OnHoverListener() {
            public final boolean onHover(View view, MotionEvent motionEvent) {
                return RotationButtonController.this.onRotateSuggestionHover(view, motionEvent);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void registerListeners() {
        if (!this.mListenersRegistered) {
            this.mListenersRegistered = true;
            try {
                WindowManagerGlobal.getWindowManagerService().watchRotation(this.mRotationWatcher, this.mContext.getDisplay().getDisplayId());
                ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterListeners() {
        if (this.mListenersRegistered) {
            this.mListenersRegistered = false;
            try {
                WindowManagerGlobal.getWindowManagerService().removeRotationWatcher(this.mRotationWatcher);
                ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addRotationCallback(Consumer<Integer> consumer) {
        this.mRotWatcherListener = consumer;
    }

    /* access modifiers changed from: package-private */
    public void setRotationLockedAtAngle(int i) {
        this.mRotationLockController.setRotationLockedAtAngle(true, i);
    }

    public boolean isRotationLocked() {
        return this.mRotationLockController.isRotationLocked();
    }

    /* access modifiers changed from: package-private */
    public void setRotateSuggestionButtonState(boolean z) {
        setRotateSuggestionButtonState(z, false);
    }

    /* access modifiers changed from: package-private */
    public void setRotateSuggestionButtonState(boolean z, boolean z2) {
        View currentView;
        KeyButtonDrawable imageDrawable;
        if ((z || this.mRotationButton.isVisible()) && (currentView = this.mRotationButton.getCurrentView()) != null && (imageDrawable = this.mRotationButton.getImageDrawable()) != null) {
            this.mPendingRotationSuggestion = false;
            this.mMainThreadHandler.removeCallbacks(this.mCancelPendingRotationProposal);
            if (z) {
                Animator animator = this.mRotateHideAnimator;
                if (animator != null && animator.isRunning()) {
                    this.mRotateHideAnimator.cancel();
                }
                this.mRotateHideAnimator = null;
                currentView.setAlpha(1.0f);
                if (imageDrawable.canAnimate()) {
                    imageDrawable.resetAnimation();
                    imageDrawable.startAnimation();
                }
                if (!isRotateSuggestionIntroduced()) {
                    this.mViewRippler.start(currentView);
                }
                this.mRotationButton.show();
                return;
            }
            this.mViewRippler.stop();
            if (z2) {
                Animator animator2 = this.mRotateHideAnimator;
                if (animator2 != null && animator2.isRunning()) {
                    this.mRotateHideAnimator.pause();
                }
                this.mRotationButton.hide();
                return;
            }
            Animator animator3 = this.mRotateHideAnimator;
            if (animator3 == null || !animator3.isRunning()) {
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(currentView, "alpha", new float[]{0.0f});
                ofFloat.setDuration(100);
                ofFloat.setInterpolator(Interpolators.LINEAR);
                ofFloat.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        RotationButtonController.this.mRotationButton.hide();
                    }
                });
                this.mRotateHideAnimator = ofFloat;
                ofFloat.start();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDarkIntensity(float f) {
        this.mRotationButton.setDarkIntensity(f);
    }

    /* access modifiers changed from: package-private */
    public void onRotationProposal(int i, int i2, boolean z) {
        int i3;
        if (this.mRotationButton.acceptRotationProposal()) {
            if (!z) {
                setRotateSuggestionButtonState(false);
            } else if (i == i2) {
                this.mMainThreadHandler.removeCallbacks(this.mRemoveRotationProposal);
                setRotateSuggestionButtonState(false);
            } else {
                this.mLastRotationSuggestion = i;
                boolean isRotationAnimationCCW = isRotationAnimationCCW(i2, i);
                if (i2 == 0 || i2 == 2) {
                    i3 = isRotationAnimationCCW ? C1785R$style.RotateButtonCCWStart90 : C1785R$style.RotateButtonCWStart90;
                } else {
                    i3 = isRotationAnimationCCW ? C1785R$style.RotateButtonCCWStart0 : C1785R$style.RotateButtonCWStart0;
                }
                this.mStyleRes = i3;
                this.mRotationButton.updateIcon();
                if (this.mIsNavigationBarShowing) {
                    showAndLogRotationSuggestion();
                    return;
                }
                this.mPendingRotationSuggestion = true;
                this.mMainThreadHandler.removeCallbacks(this.mCancelPendingRotationProposal);
                this.mMainThreadHandler.postDelayed(this.mCancelPendingRotationProposal, 20000);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onDisable2FlagChanged(int i) {
        if (hasDisable2RotateSuggestionFlag(i)) {
            onRotationSuggestionsDisabled();
        }
    }

    /* access modifiers changed from: package-private */
    public void onNavigationBarWindowVisibilityChange(boolean z) {
        if (this.mIsNavigationBarShowing != z) {
            this.mIsNavigationBarShowing = z;
            if (z && this.mPendingRotationSuggestion) {
                showAndLogRotationSuggestion();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getStyleRes() {
        return this.mStyleRes;
    }

    /* access modifiers changed from: package-private */
    public RotationButton getRotationButton() {
        return this.mRotationButton;
    }

    /* access modifiers changed from: private */
    public void onRotateSuggestionClick(View view) {
        this.mMetricsLogger.action(1287);
        incrementNumAcceptedRotationSuggestionsIfNeeded();
        setRotationLockedAtAngle(this.mLastRotationSuggestion);
    }

    /* access modifiers changed from: private */
    public boolean onRotateSuggestionHover(View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        this.mHoveringRotationSuggestion = actionMasked == 9 || actionMasked == 7;
        rescheduleRotationTimeout(true);
        return false;
    }

    private void onRotationSuggestionsDisabled() {
        setRotateSuggestionButtonState(false, true);
        this.mMainThreadHandler.removeCallbacks(this.mRemoveRotationProposal);
    }

    private void showAndLogRotationSuggestion() {
        setRotateSuggestionButtonState(true);
        rescheduleRotationTimeout(false);
        this.mMetricsLogger.visible(1288);
    }

    /* access modifiers changed from: private */
    public boolean shouldOverrideUserLockPrefs(int i) {
        return i == RotationPolicy.getNaturalRotation();
    }

    private void rescheduleRotationTimeout(boolean z) {
        Animator animator;
        if (!z || (((animator = this.mRotateHideAnimator) == null || !animator.isRunning()) && this.mRotationButton.isVisible())) {
            this.mMainThreadHandler.removeCallbacks(this.mRemoveRotationProposal);
            this.mMainThreadHandler.postDelayed(this.mRemoveRotationProposal, (long) computeRotationProposalTimeout());
        }
    }

    private int computeRotationProposalTimeout() {
        return this.mAccessibilityManagerWrapper.getRecommendedTimeoutMillis(this.mHoveringRotationSuggestion ? 16000 : 5000, 4);
    }

    private boolean isRotateSuggestionIntroduced() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "num_rotation_suggestions_accepted", 0) >= 3) {
            return true;
        }
        return false;
    }

    private void incrementNumAcceptedRotationSuggestionsIfNeeded() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int i = Settings.Secure.getInt(contentResolver, "num_rotation_suggestions_accepted", 0);
        if (i < 3) {
            Settings.Secure.putInt(contentResolver, "num_rotation_suggestions_accepted", i + 1);
        }
    }

    private class TaskStackListenerImpl extends TaskStackChangeListener {
        private TaskStackListenerImpl() {
        }

        public void onTaskStackChanged() {
            RotationButtonController.this.setRotateSuggestionButtonState(false);
        }

        public void onTaskRemoved(int i) {
            RotationButtonController.this.setRotateSuggestionButtonState(false);
        }

        public void onTaskMovedToFront(int i) {
            RotationButtonController.this.setRotateSuggestionButtonState(false);
        }

        public void onActivityRequestedOrientationChanged(int i, int i2) {
            Optional.ofNullable(ActivityManagerWrapper.getInstance()).map($$Lambda$Zm3Yj0EQnVWvu_ZksQOsrTwJ3k.INSTANCE).ifPresent(new Consumer(i) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    RotationButtonController.TaskStackListenerImpl.this.mo16118x156bd330(this.f$1, (ActivityManager.RunningTaskInfo) obj);
                }
            });
        }

        /* renamed from: lambda$onActivityRequestedOrientationChanged$0$RotationButtonController$TaskStackListenerImpl */
        public /* synthetic */ void mo16118x156bd330(int i, ActivityManager.RunningTaskInfo runningTaskInfo) {
            if (runningTaskInfo.id == i) {
                RotationButtonController.this.setRotateSuggestionButtonState(false);
            }
        }
    }

    private class ViewRippler {
        private final Runnable mRipple;
        /* access modifiers changed from: private */
        public View mRoot;

        private ViewRippler() {
            this.mRipple = new Runnable() {
                public void run() {
                    if (ViewRippler.this.mRoot.isAttachedToWindow()) {
                        ViewRippler.this.mRoot.setPressed(true);
                        ViewRippler.this.mRoot.setPressed(false);
                    }
                }
            };
        }

        public void start(View view) {
            stop();
            this.mRoot = view;
            this.mRoot.postOnAnimationDelayed(this.mRipple, 50);
            this.mRoot.postOnAnimationDelayed(this.mRipple, 2000);
            this.mRoot.postOnAnimationDelayed(this.mRipple, 4000);
            this.mRoot.postOnAnimationDelayed(this.mRipple, 6000);
            this.mRoot.postOnAnimationDelayed(this.mRipple, 8000);
        }

        public void stop() {
            View view = this.mRoot;
            if (view != null) {
                view.removeCallbacks(this.mRipple);
            }
        }
    }
}
