package com.android.systemui.statusbar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.format.DateFormat;
import android.util.FloatProperty;
import android.util.Log;
import android.view.animation.Interpolator;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.Dumpable;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.policy.CallbackController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;

public class StatusBarStateControllerImpl implements SysuiStatusBarStateController, CallbackController<StatusBarStateController.StateListener>, Dumpable {
    private static final FloatProperty<StatusBarStateControllerImpl> SET_DARK_AMOUNT_PROPERTY = new FloatProperty<StatusBarStateControllerImpl>("mDozeAmount") {
        public void setValue(StatusBarStateControllerImpl statusBarStateControllerImpl, float f) {
            statusBarStateControllerImpl.setDozeAmountInternal(f);
        }

        public Float get(StatusBarStateControllerImpl statusBarStateControllerImpl) {
            return Float.valueOf(statusBarStateControllerImpl.mDozeAmount);
        }
    };
    private static final Comparator<SysuiStatusBarStateController.RankedListener> sComparator = Comparator.comparingInt(C1067xf20ff57f.INSTANCE);
    private ValueAnimator mDarkAnimator;
    /* access modifiers changed from: private */
    public float mDozeAmount;
    private float mDozeAmountTarget;
    private Interpolator mDozeInterpolator;
    private HistoricalState[] mHistoricalRecords;
    private int mHistoryIndex;
    private boolean mIsDozing;
    private boolean mKeyguardRequested;
    private int mLastState;
    private boolean mLeaveOpenOnKeyguardHide;
    private final ArrayList<SysuiStatusBarStateController.RankedListener> mListeners = new ArrayList<>();
    private boolean mPulsing;
    private int mState;
    private int mSystemUiVisibility;

    public StatusBarStateControllerImpl() {
        this.mHistoryIndex = 0;
        this.mHistoricalRecords = new HistoricalState[32];
        this.mSystemUiVisibility = 0;
        this.mDozeInterpolator = Interpolators.FAST_OUT_SLOW_IN;
        for (int i = 0; i < 32; i++) {
            this.mHistoricalRecords[i] = new HistoricalState();
        }
    }

    public int getState() {
        return this.mState;
    }

    public boolean setState(int i) {
        if (i > 3 || i < 0) {
            throw new IllegalArgumentException("Invalid state " + i);
        }
        int i2 = this.mState;
        if (i == i2) {
            return false;
        }
        recordHistoricalState(i, i2);
        if (this.mState == 0 && i == 2) {
            Log.e("SbStateController", "Invalid state transition: SHADE -> SHADE_LOCKED", new Throwable());
        }
        synchronized (this.mListeners) {
            Iterator it = new ArrayList(this.mListeners).iterator();
            while (it.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onStatePreChange(this.mState, i);
            }
            this.mLastState = this.mState;
            this.mState = i;
            Iterator it2 = new ArrayList(this.mListeners).iterator();
            while (it2.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it2.next()).mListener.onStateChanged(this.mState);
            }
            Iterator it3 = new ArrayList(this.mListeners).iterator();
            while (it3.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it3.next()).mListener.onStatePostChange();
            }
        }
        return true;
    }

    public boolean isDozing() {
        return this.mIsDozing;
    }

    public float getDozeAmount() {
        return this.mDozeAmount;
    }

    public float getInterpolatedDozeAmount() {
        return this.mDozeInterpolator.getInterpolation(this.mDozeAmount);
    }

    public boolean setIsDozing(boolean z) {
        if (this.mIsDozing == z) {
            return false;
        }
        this.mIsDozing = z;
        synchronized (this.mListeners) {
            Iterator it = new ArrayList(this.mListeners).iterator();
            while (it.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onDozingChanged(z);
            }
        }
        return true;
    }

    public void setDozeAmount(float f, boolean z) {
        ValueAnimator valueAnimator = this.mDarkAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            if (!z || this.mDozeAmountTarget != f) {
                this.mDarkAnimator.cancel();
            } else {
                return;
            }
        }
        this.mDozeAmountTarget = f;
        if (z) {
            startDozeAnimation();
        } else {
            setDozeAmountInternal(f);
        }
    }

    private void startDozeAnimation() {
        Interpolator interpolator;
        float f = this.mDozeAmount;
        if (f == 0.0f || f == 1.0f) {
            if (this.mIsDozing) {
                interpolator = Interpolators.FAST_OUT_SLOW_IN;
            } else {
                interpolator = Interpolators.TOUCH_RESPONSE_REVERSE;
            }
            this.mDozeInterpolator = interpolator;
        }
        this.mDarkAnimator = ObjectAnimator.ofFloat(this, SET_DARK_AMOUNT_PROPERTY, new float[]{this.mDozeAmountTarget});
        this.mDarkAnimator.setInterpolator(Interpolators.LINEAR);
        this.mDarkAnimator.setDuration(500);
        this.mDarkAnimator.start();
    }

    /* access modifiers changed from: private */
    public void setDozeAmountInternal(float f) {
        this.mDozeAmount = f;
        float interpolation = this.mDozeInterpolator.getInterpolation(f);
        synchronized (this.mListeners) {
            Iterator it = new ArrayList(this.mListeners).iterator();
            while (it.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onDozeAmountChanged(this.mDozeAmount, interpolation);
            }
        }
    }

    public boolean goingToFullShade() {
        return this.mState == 0 && this.mLeaveOpenOnKeyguardHide;
    }

    public void setLeaveOpenOnKeyguardHide(boolean z) {
        this.mLeaveOpenOnKeyguardHide = z;
    }

    public boolean leaveOpenOnKeyguardHide() {
        return this.mLeaveOpenOnKeyguardHide;
    }

    public boolean fromShadeLocked() {
        return this.mLastState == 2;
    }

    public void addCallback(StatusBarStateController.StateListener stateListener) {
        synchronized (this.mListeners) {
            addListenerInternalLocked(stateListener, Integer.MAX_VALUE);
        }
    }

    @Deprecated
    public void addCallback(StatusBarStateController.StateListener stateListener, int i) {
        synchronized (this.mListeners) {
            addListenerInternalLocked(stateListener, i);
        }
    }

    @GuardedBy({"mListeners"})
    private void addListenerInternalLocked(StatusBarStateController.StateListener stateListener, int i) {
        Iterator<SysuiStatusBarStateController.RankedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            if (it.next().mListener.equals(stateListener)) {
                return;
            }
        }
        this.mListeners.add(new SysuiStatusBarStateController.RankedListener(stateListener, i));
        this.mListeners.sort(sComparator);
    }

    public void removeCallback(StatusBarStateController.StateListener stateListener) {
        synchronized (this.mListeners) {
            this.mListeners.removeIf(new Predicate() {
                public final boolean test(Object obj) {
                    return ((SysuiStatusBarStateController.RankedListener) obj).mListener.equals(StatusBarStateController.StateListener.this);
                }
            });
        }
    }

    public void setKeyguardRequested(boolean z) {
        this.mKeyguardRequested = z;
    }

    public boolean isKeyguardRequested() {
        return this.mKeyguardRequested;
    }

    public void setSystemUiVisibility(int i) {
        if (this.mSystemUiVisibility != i) {
            this.mSystemUiVisibility = i;
            synchronized (this.mListeners) {
                Iterator it = new ArrayList(this.mListeners).iterator();
                while (it.hasNext()) {
                    ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onSystemUiVisibilityChanged(this.mSystemUiVisibility);
                }
            }
        }
    }

    public void setPulsing(boolean z) {
        if (this.mPulsing != z) {
            this.mPulsing = z;
            synchronized (this.mListeners) {
                Iterator it = new ArrayList(this.mListeners).iterator();
                while (it.hasNext()) {
                    ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onPulsingChanged(z);
                }
            }
        }
    }

    public static String describe(int i) {
        return StatusBarState.toShortString(i);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("StatusBarStateController: ");
        printWriter.println(" mState=" + this.mState + " (" + describe(this.mState) + ")");
        printWriter.println(" mLastState=" + this.mLastState + " (" + describe(this.mLastState) + ")");
        StringBuilder sb = new StringBuilder();
        sb.append(" mLeaveOpenOnKeyguardHide=");
        sb.append(this.mLeaveOpenOnKeyguardHide);
        printWriter.println(sb.toString());
        printWriter.println(" mKeyguardRequested=" + this.mKeyguardRequested);
        printWriter.println(" mIsDozing=" + this.mIsDozing);
        printWriter.println(" Historical states:");
        int i = 0;
        for (int i2 = 0; i2 < 32; i2++) {
            if (this.mHistoricalRecords[i2].mTimestamp != 0) {
                i++;
            }
        }
        for (int i3 = this.mHistoryIndex + 32; i3 >= ((this.mHistoryIndex + 32) - i) + 1; i3 += -1) {
            printWriter.println("  (" + (((this.mHistoryIndex + 32) - i3) + 1) + ")" + this.mHistoricalRecords[i3 & 31]);
        }
    }

    private void recordHistoricalState(int i, int i2) {
        this.mHistoryIndex = (this.mHistoryIndex + 1) % 32;
        HistoricalState historicalState = this.mHistoricalRecords[this.mHistoryIndex];
        historicalState.mState = i;
        historicalState.mLastState = i2;
        historicalState.mTimestamp = System.currentTimeMillis();
    }

    private static class HistoricalState {
        int mLastState;
        int mState;
        long mTimestamp;

        private HistoricalState() {
        }

        public String toString() {
            if (this.mTimestamp != 0) {
                return "state=" + this.mState + " (" + StatusBarStateControllerImpl.describe(this.mState) + ")" + "lastState=" + this.mLastState + " (" + StatusBarStateControllerImpl.describe(this.mLastState) + ")" + "timestamp=" + DateFormat.format("MM-dd HH:mm:ss", this.mTimestamp);
            }
            return "Empty " + HistoricalState.class.getSimpleName();
        }
    }
}
