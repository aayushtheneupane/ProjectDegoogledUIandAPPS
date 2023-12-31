package com.android.systemui.assist;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.provider.Settings;
import androidx.slice.Clock;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import dagger.Lazy;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

final class AssistHandleReminderExpBehavior implements AssistHandleBehaviorController.BehaviorController {
    private static final String[] DEFAULT_HOME_CHANGE_ACTIONS = {"android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED", "android.intent.action.BOOT_COMPLETED", "android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_CHANGED", "android.intent.action.PACKAGE_REMOVED"};
    private static final long DEFAULT_LEARNING_TIME_MS = TimeUnit.DAYS.toMillis(2);
    private static final long DEFAULT_SHOW_AND_GO_DELAYED_LONG_DELAY_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long DEFAULT_SHOW_AND_GO_DELAY_RESET_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(3);
    private final Lazy<ActivityManagerWrapper> mActivityManagerWrapper;
    private AssistHandleCallbacks mAssistHandleCallbacks;
    private final Clock mClock;
    private int mConsecutiveTaskSwitches;
    private Context mContext;
    /* access modifiers changed from: private */
    public ComponentName mDefaultHome;
    private final BroadcastReceiver mDefaultHomeBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            AssistHandleReminderExpBehavior assistHandleReminderExpBehavior = AssistHandleReminderExpBehavior.this;
            ComponentName unused = assistHandleReminderExpBehavior.mDefaultHome = assistHandleReminderExpBehavior.getCurrentDefaultHome();
        }
    };
    private final IntentFilter mDefaultHomeIntentFilter;
    private final DeviceConfigHelper mDeviceConfigHelper;
    private final Handler mHandler;
    private boolean mIsAwake;
    private boolean mIsDozing;
    private boolean mIsLauncherShowing;
    private boolean mIsLearned;
    private boolean mIsNavBarHidden;
    private long mLastLearningTimestamp;
    private long mLearnedHintLastShownEpochDay;
    private int mLearningCount;
    private long mLearningTimeElapsed;
    private boolean mOnLockscreen;
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() {
        public void onOverviewShown(boolean z) {
            AssistHandleReminderExpBehavior.this.handleOverviewShown();
        }

        public void onSystemUiStateChanged(int i) {
            AssistHandleReminderExpBehavior.this.handleSystemUiStateChanged(i);
        }
    };
    private final Lazy<OverviewProxyService> mOverviewProxyService;
    private final Lazy<PackageManagerWrapper> mPackageManagerWrapper;
    private final Runnable mResetConsecutiveTaskSwitches = new Runnable() {
        public final void run() {
            AssistHandleReminderExpBehavior.this.resetConsecutiveTaskSwitches();
        }
    };
    private int mRunningTaskId;
    private final Lazy<StatusBarStateController> mStatusBarStateController;
    private final StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() {
        public void onStateChanged(int i) {
            AssistHandleReminderExpBehavior.this.handleStatusBarStateChanged(i);
        }

        public void onDozingChanged(boolean z) {
            AssistHandleReminderExpBehavior.this.handleDozingChanged(z);
        }
    };
    private final TaskStackChangeListener mTaskStackChangeListener = new TaskStackChangeListener() {
        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
            AssistHandleReminderExpBehavior.this.handleTaskStackTopChanged(runningTaskInfo.taskId, runningTaskInfo.topActivity);
        }

        public void onTaskCreated(int i, ComponentName componentName) {
            AssistHandleReminderExpBehavior.this.handleTaskStackTopChanged(i, componentName);
        }
    };
    private final Lazy<WakefulnessLifecycle> mWakefulnessLifecycle;
    private final WakefulnessLifecycle.Observer mWakefulnessLifecycleObserver = new WakefulnessLifecycle.Observer() {
        public void onStartedWakingUp() {
            AssistHandleReminderExpBehavior.this.handleWakefullnessChanged(false);
        }

        public void onFinishedWakingUp() {
            AssistHandleReminderExpBehavior.this.handleWakefullnessChanged(true);
        }

        public void onStartedGoingToSleep() {
            AssistHandleReminderExpBehavior.this.handleWakefullnessChanged(false);
        }

        public void onFinishedGoingToSleep() {
            AssistHandleReminderExpBehavior.this.handleWakefullnessChanged(false);
        }
    };

    private int getLearningCount() {
        return 2;
    }

    private boolean getShowWhenTaught() {
        return false;
    }

    private boolean getSuppressOnApps() {
        return true;
    }

    private boolean getSuppressOnLockscreen() {
        return true;
    }

    private boolean onLockscreen(int i) {
        return i == 1 || i == 2;
    }

    AssistHandleReminderExpBehavior(Clock clock, Handler handler, DeviceConfigHelper deviceConfigHelper, Lazy<StatusBarStateController> lazy, Lazy<ActivityManagerWrapper> lazy2, Lazy<OverviewProxyService> lazy3, Lazy<WakefulnessLifecycle> lazy4, Lazy<PackageManagerWrapper> lazy5) {
        this.mClock = clock;
        this.mHandler = handler;
        this.mDeviceConfigHelper = deviceConfigHelper;
        this.mStatusBarStateController = lazy;
        this.mActivityManagerWrapper = lazy2;
        this.mOverviewProxyService = lazy3;
        this.mWakefulnessLifecycle = lazy4;
        this.mPackageManagerWrapper = lazy5;
        this.mDefaultHomeIntentFilter = new IntentFilter();
        for (String addAction : DEFAULT_HOME_CHANGE_ACTIONS) {
            this.mDefaultHomeIntentFilter.addAction(addAction);
        }
    }

    public void onModeActivated(Context context, AssistHandleCallbacks assistHandleCallbacks) {
        int i;
        this.mContext = context;
        this.mAssistHandleCallbacks = assistHandleCallbacks;
        this.mConsecutiveTaskSwitches = 0;
        this.mDefaultHome = getCurrentDefaultHome();
        context.registerReceiver(this.mDefaultHomeBroadcastReceiver, this.mDefaultHomeIntentFilter);
        this.mOnLockscreen = onLockscreen(this.mStatusBarStateController.get().getState());
        this.mIsDozing = this.mStatusBarStateController.get().isDozing();
        this.mStatusBarStateController.get().addCallback(this.mStatusBarStateListener);
        ActivityManager.RunningTaskInfo runningTask = this.mActivityManagerWrapper.get().getRunningTask();
        if (runningTask == null) {
            i = 0;
        } else {
            i = runningTask.taskId;
        }
        this.mRunningTaskId = i;
        this.mActivityManagerWrapper.get().registerTaskStackListener(this.mTaskStackChangeListener);
        this.mOverviewProxyService.get().addCallback(this.mOverviewProxyListener);
        this.mIsAwake = this.mWakefulnessLifecycle.get().getWakefulness() == 2;
        this.mWakefulnessLifecycle.get().addObserver(this.mWakefulnessLifecycleObserver);
        this.mLearningTimeElapsed = Settings.Secure.getLong(context.getContentResolver(), "reminder_exp_learning_time_elapsed", 0);
        this.mLearningCount = Settings.Secure.getInt(context.getContentResolver(), "reminder_exp_learning_event_count", 0);
        this.mLearnedHintLastShownEpochDay = Settings.Secure.getLong(context.getContentResolver(), "reminder_exp_learned_hint_last_shown", 0);
        this.mLastLearningTimestamp = this.mClock.currentTimeMillis();
        callbackForCurrentState(false);
    }

    public void onModeDeactivated() {
        this.mAssistHandleCallbacks = null;
        Context context = this.mContext;
        if (context != null) {
            context.unregisterReceiver(this.mDefaultHomeBroadcastReceiver);
            this.mContext = null;
        }
        this.mStatusBarStateController.get().removeCallback(this.mStatusBarStateListener);
        this.mActivityManagerWrapper.get().unregisterTaskStackListener(this.mTaskStackChangeListener);
        this.mOverviewProxyService.get().removeCallback(this.mOverviewProxyListener);
        this.mWakefulnessLifecycle.get().removeObserver(this.mWakefulnessLifecycleObserver);
    }

    public void onAssistantGesturePerformed() {
        Context context = this.mContext;
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            int i = this.mLearningCount + 1;
            this.mLearningCount = i;
            Settings.Secure.putLong(contentResolver, "reminder_exp_learning_event_count", (long) i);
        }
    }

    public void onAssistHandlesRequested() {
        if (this.mAssistHandleCallbacks != null && isFullyAwake() && !this.mIsNavBarHidden && !this.mOnLockscreen) {
            this.mAssistHandleCallbacks.showAndGo();
        }
    }

    /* access modifiers changed from: private */
    public ComponentName getCurrentDefaultHome() {
        ArrayList arrayList = new ArrayList();
        ComponentName homeActivities = this.mPackageManagerWrapper.get().getHomeActivities(arrayList);
        if (homeActivities != null) {
            return homeActivities;
        }
        Iterator it = arrayList.iterator();
        int i = Integer.MIN_VALUE;
        while (true) {
            ComponentName componentName = null;
            while (true) {
                if (!it.hasNext()) {
                    return componentName;
                }
                ResolveInfo resolveInfo = (ResolveInfo) it.next();
                int i2 = resolveInfo.priority;
                if (i2 > i) {
                    componentName = resolveInfo.activityInfo.getComponentName();
                    i = resolveInfo.priority;
                } else if (i2 == i) {
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleStatusBarStateChanged(int i) {
        boolean onLockscreen = onLockscreen(i);
        if (this.mOnLockscreen != onLockscreen) {
            resetConsecutiveTaskSwitches();
            this.mOnLockscreen = onLockscreen;
            callbackForCurrentState(!onLockscreen);
        }
    }

    /* access modifiers changed from: private */
    public void handleDozingChanged(boolean z) {
        if (this.mIsDozing != z) {
            resetConsecutiveTaskSwitches();
            this.mIsDozing = z;
            callbackForCurrentState(false);
        }
    }

    /* access modifiers changed from: private */
    public void handleWakefullnessChanged(boolean z) {
        if (this.mIsAwake != z) {
            resetConsecutiveTaskSwitches();
            this.mIsAwake = z;
            callbackForCurrentState(false);
        }
    }

    /* access modifiers changed from: private */
    public void handleTaskStackTopChanged(int i, ComponentName componentName) {
        if (this.mRunningTaskId != i && componentName != null) {
            this.mRunningTaskId = i;
            this.mIsLauncherShowing = componentName.equals(this.mDefaultHome);
            if (this.mIsLauncherShowing) {
                resetConsecutiveTaskSwitches();
            } else {
                rescheduleConsecutiveTaskSwitchesReset();
                this.mConsecutiveTaskSwitches++;
            }
            callbackForCurrentState(false);
        }
    }

    /* access modifiers changed from: private */
    public void handleSystemUiStateChanged(int i) {
        boolean z = (i & 2) != 0;
        if (this.mIsNavBarHidden != z) {
            resetConsecutiveTaskSwitches();
            this.mIsNavBarHidden = z;
            callbackForCurrentState(false);
        }
    }

    /* access modifiers changed from: private */
    public void handleOverviewShown() {
        resetConsecutiveTaskSwitches();
        callbackForCurrentState(false);
    }

    private void callbackForCurrentState(boolean z) {
        updateLearningStatus();
        if (this.mIsLearned) {
            callbackForLearnedState(z);
        } else {
            callbackForUnlearnedState();
        }
    }

    private void callbackForLearnedState(boolean z) {
        if (this.mAssistHandleCallbacks != null) {
            if (!isFullyAwake() || this.mIsNavBarHidden || this.mOnLockscreen || !getShowWhenTaught()) {
                this.mAssistHandleCallbacks.hide();
            } else if (z) {
                long epochDay = LocalDate.now().toEpochDay();
                if (this.mLearnedHintLastShownEpochDay < epochDay) {
                    Context context = this.mContext;
                    if (context != null) {
                        Settings.Secure.putLong(context.getContentResolver(), "reminder_exp_learned_hint_last_shown", epochDay);
                    }
                    this.mLearnedHintLastShownEpochDay = epochDay;
                    this.mAssistHandleCallbacks.showAndGo();
                }
            }
        }
    }

    private void callbackForUnlearnedState() {
        if (this.mAssistHandleCallbacks != null) {
            if (!isFullyAwake() || this.mIsNavBarHidden || isSuppressed()) {
                this.mAssistHandleCallbacks.hide();
            } else if (this.mOnLockscreen) {
                this.mAssistHandleCallbacks.showAndStay();
            } else if (this.mIsLauncherShowing) {
                this.mAssistHandleCallbacks.showAndGo();
            } else if (this.mConsecutiveTaskSwitches == 1) {
                this.mAssistHandleCallbacks.showAndGoDelayed(getShowAndGoDelayedShortDelayMs(), false);
            } else {
                this.mAssistHandleCallbacks.showAndGoDelayed(getShowAndGoDelayedLongDelayMs(), true);
            }
        }
    }

    private boolean isSuppressed() {
        if (this.mOnLockscreen) {
            return getSuppressOnLockscreen();
        }
        if (this.mIsLauncherShowing) {
            return getSuppressOnLauncher();
        }
        return getSuppressOnApps();
    }

    private void updateLearningStatus() {
        if (this.mContext != null) {
            long currentTimeMillis = this.mClock.currentTimeMillis();
            this.mLearningTimeElapsed += currentTimeMillis - this.mLastLearningTimestamp;
            this.mLastLearningTimestamp = currentTimeMillis;
            this.mIsLearned = this.mLearningCount >= getLearningCount() || this.mLearningTimeElapsed >= getLearningTimeMs();
            this.mHandler.post(new Runnable() {
                public final void run() {
                    AssistHandleReminderExpBehavior.this.lambda$updateLearningStatus$0$AssistHandleReminderExpBehavior();
                }
            });
        }
    }

    public /* synthetic */ void lambda$updateLearningStatus$0$AssistHandleReminderExpBehavior() {
        Settings.Secure.putLong(this.mContext.getContentResolver(), "reminder_exp_learning_time_elapsed", this.mLearningTimeElapsed);
    }

    /* access modifiers changed from: private */
    public void resetConsecutiveTaskSwitches() {
        this.mHandler.removeCallbacks(this.mResetConsecutiveTaskSwitches);
        this.mConsecutiveTaskSwitches = 0;
    }

    private void rescheduleConsecutiveTaskSwitchesReset() {
        this.mHandler.removeCallbacks(this.mResetConsecutiveTaskSwitches);
        this.mHandler.postDelayed(this.mResetConsecutiveTaskSwitches, getShowAndGoDelayResetTimeoutMs());
    }

    private boolean isFullyAwake() {
        return this.mIsAwake && !this.mIsDozing;
    }

    private long getLearningTimeMs() {
        return DEFAULT_LEARNING_TIME_MS;
    }

    private long getShowAndGoDelayedShortDelayMs() {
        return this.mDeviceConfigHelper.getLong("assist_handles_show_and_go_delayed_short_delay_ms", 150);
    }

    private long getShowAndGoDelayedLongDelayMs() {
        return this.mDeviceConfigHelper.getLong("assist_handles_show_and_go_delayed_long_delay_ms", DEFAULT_SHOW_AND_GO_DELAYED_LONG_DELAY_MS);
    }

    private long getShowAndGoDelayResetTimeoutMs() {
        return this.mDeviceConfigHelper.getLong("assist_handles_show_and_go_delay_reset_timeout_ms", DEFAULT_SHOW_AND_GO_DELAY_RESET_TIMEOUT_MS);
    }

    private boolean getSuppressOnLauncher() {
        return this.mDeviceConfigHelper.getBoolean("assist_handles_suppress_on_launcher", false);
    }

    public void dump(PrintWriter printWriter, String str) {
        printWriter.println(str + "Current AssistHandleReminderExpBehavior State:");
        printWriter.println(str + "   mOnLockscreen=" + this.mOnLockscreen);
        printWriter.println(str + "   mIsDozing=" + this.mIsDozing);
        printWriter.println(str + "   mIsAwake=" + this.mIsAwake);
        printWriter.println(str + "   mRunningTaskId=" + this.mRunningTaskId);
        printWriter.println(str + "   mDefaultHome=" + this.mDefaultHome);
        printWriter.println(str + "   mIsNavBarHidden=" + this.mIsNavBarHidden);
        printWriter.println(str + "   mIsLauncherShowing=" + this.mIsLauncherShowing);
        printWriter.println(str + "   mConsecutiveTaskSwitches=" + this.mConsecutiveTaskSwitches);
        printWriter.println(str + "   mIsLearned=" + this.mIsLearned);
        printWriter.println(str + "   mLastLearningTimestamp=" + this.mLastLearningTimestamp);
        printWriter.println(str + "   mLearningTimeElapsed=" + this.mLearningTimeElapsed);
        printWriter.println(str + "   mLearningCount=" + this.mLearningCount);
        printWriter.println(str + "   mLearnedHintLastShownEpochDay=" + this.mLearnedHintLastShownEpochDay);
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("   mAssistHandleCallbacks present: ");
        sb.append(this.mAssistHandleCallbacks != null);
        printWriter.println(sb.toString());
        printWriter.println(str + "   Phenotype Flags:");
        printWriter.println(str + "      " + "assist_handles_learn_time_ms" + "=" + getLearningTimeMs());
        printWriter.println(str + "      " + "assist_handles_learn_count" + "=" + getLearningCount());
        printWriter.println(str + "      " + "assist_handles_show_and_go_delayed_short_delay_ms" + "=" + getShowAndGoDelayedShortDelayMs());
        printWriter.println(str + "      " + "assist_handles_show_and_go_delayed_long_delay_ms" + "=" + getShowAndGoDelayedLongDelayMs());
        printWriter.println(str + "      " + "assist_handles_show_and_go_delay_reset_timeout_ms" + "=" + getShowAndGoDelayResetTimeoutMs());
        printWriter.println(str + "      " + "assist_handles_suppress_on_lockscreen" + "=" + getSuppressOnLockscreen());
        printWriter.println(str + "      " + "assist_handles_suppress_on_launcher" + "=" + getSuppressOnLauncher());
        printWriter.println(str + "      " + "assist_handles_suppress_on_apps" + "=" + getSuppressOnApps());
        printWriter.println(str + "      " + "assist_handles_show_when_taught" + "=" + getShowWhenTaught());
    }
}
