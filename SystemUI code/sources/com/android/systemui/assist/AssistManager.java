package com.android.systemui.assist;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IVoiceInteractionSessionListener;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.applications.InterestingConfigChanges;
import com.android.systemui.C1775R$dimen;
import com.android.systemui.C1779R$layout;
import com.android.systemui.assist.p003ui.DefaultUiController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;

public class AssistManager {
    private final AssistDisclosure mAssistDisclosure;
    protected final AssistUtils mAssistUtils;
    private ConfigurationController.ConfigurationListener mConfigurationListener = new ConfigurationController.ConfigurationListener() {
        public void onConfigChanged(Configuration configuration) {
            boolean z;
            if (AssistManager.this.mInterestingConfigChanges.applyNewConfig(AssistManager.this.mContext.getResources())) {
                if (AssistManager.this.mView != null) {
                    z = AssistManager.this.mView.isShowing();
                    AssistManager.this.mWindowManager.removeView(AssistManager.this.mView);
                } else {
                    z = false;
                }
                AssistManager assistManager = AssistManager.this;
                AssistOrbContainer unused = assistManager.mView = (AssistOrbContainer) LayoutInflater.from(assistManager.mContext).inflate(C1779R$layout.assist_orb, (ViewGroup) null);
                AssistManager.this.mView.setVisibility(8);
                AssistManager.this.mView.setSystemUiVisibility(1792);
                AssistManager.this.mWindowManager.addView(AssistManager.this.mView, AssistManager.this.getLayoutParams());
                if (z) {
                    AssistManager.this.mView.show(true, false);
                }
            }
        }
    };
    protected final Context mContext;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private final AssistHandleBehaviorController mHandleController;
    /* access modifiers changed from: private */
    public Runnable mHideRunnable = new Runnable() {
        public void run() {
            AssistManager.this.mView.removeCallbacks(this);
            AssistManager.this.mView.show(false, true);
        }
    };
    /* access modifiers changed from: private */
    public final InterestingConfigChanges mInterestingConfigChanges;
    protected final OverviewProxyService mOverviewProxyService;
    private final PhoneStateMonitor mPhoneStateMonitor;
    private final boolean mShouldEnableOrb;
    private IVoiceInteractionSessionShowCallback mShowCallback = new IVoiceInteractionSessionShowCallback.Stub() {
        public void onFailed() throws RemoteException {
            AssistManager.this.mView.post(AssistManager.this.mHideRunnable);
        }

        public void onShown() throws RemoteException {
            AssistManager.this.mView.post(AssistManager.this.mHideRunnable);
        }
    };
    private final UiController mUiController;
    /* access modifiers changed from: private */
    public AssistOrbContainer mView;
    /* access modifiers changed from: private */
    public final WindowManager mWindowManager;

    public interface UiController {
        void onGestureCompletion(float f);

        void onInvocationProgress(int i, float f);
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowOrb() {
        return false;
    }

    public void showDisclosure() {
    }

    public AssistManager(DeviceProvisionedController deviceProvisionedController, Context context, AssistUtils assistUtils, AssistHandleBehaviorController assistHandleBehaviorController, ConfigurationController configurationController, OverviewProxyService overviewProxyService) {
        this.mContext = context;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mAssistUtils = assistUtils;
        this.mAssistDisclosure = new AssistDisclosure(context, new Handler());
        this.mPhoneStateMonitor = new PhoneStateMonitor(context);
        this.mHandleController = assistHandleBehaviorController;
        configurationController.addCallback(this.mConfigurationListener);
        registerVoiceInteractionSessionListener();
        this.mInterestingConfigChanges = new InterestingConfigChanges(-2147482748);
        this.mConfigurationListener.onConfigChanged(context.getResources().getConfiguration());
        this.mShouldEnableOrb = !ActivityManager.isLowRamDeviceStatic();
        this.mUiController = new DefaultUiController(this.mContext);
        this.mOverviewProxyService = overviewProxyService;
        this.mOverviewProxyService.addCallback((OverviewProxyService.OverviewProxyListener) new OverviewProxyService.OverviewProxyListener() {
            public void onAssistantProgress(float f) {
                AssistManager.this.onInvocationProgress(1, f);
            }

            public void onAssistantGestureCompletion(float f) {
                AssistManager.this.onGestureCompletion(f);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void registerVoiceInteractionSessionListener() {
        this.mAssistUtils.registerVoiceInteractionSessionListener(new IVoiceInteractionSessionListener.Stub() {
            public void onVoiceSessionHidden() throws RemoteException {
            }

            public void onVoiceSessionShown() throws RemoteException {
            }

            public void onSetUiHints(Bundle bundle) {
                String string = bundle.getString("action");
                if ("show_assist_handles".equals(string)) {
                    AssistManager.this.requestAssistHandles();
                } else if ("set_assist_gesture_constrained".equals(string)) {
                    AssistManager.this.mOverviewProxyService.setSystemUiStateFlag(4096, bundle.getBoolean("should_constrain", false), 0);
                }
            }
        });
    }

    public void startAssist(Bundle bundle) {
        ComponentName assistInfo = getAssistInfo();
        if (assistInfo != null) {
            boolean equals = assistInfo.equals(getVoiceInteractorComponentName());
            if (!equals || (!isVoiceSessionRunning() && shouldShowOrb())) {
                showOrb(assistInfo, equals);
                this.mView.postDelayed(this.mHideRunnable, equals ? 2500 : 1000);
            }
            if (bundle == null) {
                bundle = new Bundle();
            }
            int i = bundle.getInt("invocation_type", 0);
            if (i == 1) {
                this.mHandleController.onAssistantGesturePerformed();
            }
            int phoneState = this.mPhoneStateMonitor.getPhoneState();
            bundle.putInt("invocation_phone_state", phoneState);
            bundle.putLong("invocation_time_ms", SystemClock.elapsedRealtime());
            logStartAssist(i, phoneState);
            startAssistInternal(bundle, assistInfo, equals);
        }
    }

    public void onInvocationProgress(int i, float f) {
        this.mUiController.onInvocationProgress(i, f);
    }

    public void onGestureCompletion(float f) {
        this.mUiController.onGestureCompletion(f);
    }

    /* access modifiers changed from: protected */
    public void requestAssistHandles() {
        this.mHandleController.onAssistHandlesRequested();
    }

    public void hideAssist() {
        this.mAssistUtils.hideCurrentSession();
    }

    /* access modifiers changed from: private */
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, this.mContext.getResources().getDimensionPixelSize(C1775R$dimen.assist_orb_scrim_height), 2033, 280, -3);
        layoutParams.token = new Binder();
        layoutParams.gravity = 8388691;
        layoutParams.setTitle("AssistPreviewPanel");
        layoutParams.softInputMode = 49;
        return layoutParams;
    }

    private void showOrb(ComponentName componentName, boolean z) {
        maybeSwapSearchIcon(componentName, z);
        if (this.mShouldEnableOrb) {
            this.mView.show(true, true);
        }
    }

    private void startAssistInternal(Bundle bundle, ComponentName componentName, boolean z) {
        if (z) {
            startVoiceInteractor(bundle);
        } else {
            startAssistActivity(bundle, componentName);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0036, code lost:
        r0 = r0.getAssistIntent(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startAssistActivity(android.os.Bundle r6, android.content.ComponentName r7) {
        /*
            r5 = this;
            com.android.systemui.statusbar.policy.DeviceProvisionedController r0 = r5.mDeviceProvisionedController
            boolean r0 = r0.isDeviceProvisioned()
            if (r0 != 0) goto L_0x0009
            return
        L_0x0009:
            android.content.Context r0 = r5.mContext
            java.lang.Class<com.android.systemui.statusbar.CommandQueue> r1 = com.android.systemui.statusbar.CommandQueue.class
            java.lang.Object r0 = com.android.systemui.SysUiServiceProvider.getComponent(r0, r1)
            com.android.systemui.statusbar.CommandQueue r0 = (com.android.systemui.statusbar.CommandQueue) r0
            r1 = 3
            r2 = 0
            r0.animateCollapsePanels(r1, r2)
            android.content.Context r0 = r5.mContext
            android.content.ContentResolver r0 = r0.getContentResolver()
            r1 = -2
            r3 = 1
            java.lang.String r4 = "assist_structure_enabled"
            int r0 = android.provider.Settings.Secure.getIntForUser(r0, r4, r3, r1)
            if (r0 == 0) goto L_0x0029
            r2 = r3
        L_0x0029:
            android.content.Context r0 = r5.mContext
            java.lang.String r1 = "search"
            java.lang.Object r0 = r0.getSystemService(r1)
            android.app.SearchManager r0 = (android.app.SearchManager) r0
            if (r0 != 0) goto L_0x0036
            return
        L_0x0036:
            android.content.Intent r0 = r0.getAssistIntent(r2)
            if (r0 != 0) goto L_0x003d
            return
        L_0x003d:
            r0.setComponent(r7)
            r0.putExtras(r6)
            android.content.Context r6 = r5.mContext     // Catch:{ ActivityNotFoundException -> 0x005b }
            int r7 = com.android.systemui.C1770R$anim.search_launch_enter     // Catch:{ ActivityNotFoundException -> 0x005b }
            int r1 = com.android.systemui.C1770R$anim.search_launch_exit     // Catch:{ ActivityNotFoundException -> 0x005b }
            android.app.ActivityOptions r6 = android.app.ActivityOptions.makeCustomAnimation(r6, r7, r1)     // Catch:{ ActivityNotFoundException -> 0x005b }
            r7 = 268435456(0x10000000, float:2.5243549E-29)
            r0.addFlags(r7)     // Catch:{ ActivityNotFoundException -> 0x005b }
            com.android.systemui.assist.AssistManager$6 r7 = new com.android.systemui.assist.AssistManager$6     // Catch:{ ActivityNotFoundException -> 0x005b }
            r7.<init>(r0, r6)     // Catch:{ ActivityNotFoundException -> 0x005b }
            android.os.AsyncTask.execute(r7)     // Catch:{ ActivityNotFoundException -> 0x005b }
            goto L_0x0075
        L_0x005b:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "Activity not found for "
            r5.append(r6)
            java.lang.String r6 = r0.getAction()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            java.lang.String r6 = "AssistManager"
            android.util.Log.w(r6, r5)
        L_0x0075:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.assist.AssistManager.startAssistActivity(android.os.Bundle, android.content.ComponentName):void");
    }

    private void startVoiceInteractor(Bundle bundle) {
        this.mAssistUtils.showSessionForActiveService(bundle, 4, this.mShowCallback, (IBinder) null);
    }

    public void launchVoiceAssistFromKeyguard() {
        this.mAssistUtils.launchVoiceAssistFromKeyguard();
    }

    public boolean canVoiceAssistBeLaunchedFromKeyguard() {
        return this.mAssistUtils.activeServiceSupportsLaunchFromKeyguard();
    }

    public ComponentName getVoiceInteractorComponentName() {
        return this.mAssistUtils.getActiveServiceComponentName();
    }

    private boolean isVoiceSessionRunning() {
        return this.mAssistUtils.isSessionRunning();
    }

    private void maybeSwapSearchIcon(ComponentName componentName, boolean z) {
        replaceDrawable(this.mView.getOrb().getLogo(), componentName, "com.android.systemui.action_assist_icon", z);
    }

    public void replaceDrawable(ImageView imageView, ComponentName componentName, String str, boolean z) {
        Bundle bundle;
        int i;
        if (componentName != null) {
            try {
                PackageManager packageManager = this.mContext.getPackageManager();
                if (z) {
                    bundle = packageManager.getServiceInfo(componentName, 128).metaData;
                } else {
                    bundle = packageManager.getActivityInfo(componentName, 128).metaData;
                }
                if (!(bundle == null || (i = bundle.getInt(str)) == 0)) {
                    imageView.setImageDrawable(packageManager.getResourcesForApplication(componentName.getPackageName()).getDrawable(i));
                    return;
                }
            } catch (PackageManager.NameNotFoundException unused) {
            } catch (Resources.NotFoundException e) {
                Log.w("AssistManager", "Failed to swap drawable from " + componentName.flattenToShortString(), e);
            }
        }
        imageView.setImageDrawable((Drawable) null);
    }

    public ComponentName getAssistInfoForUser(int i) {
        return this.mAssistUtils.getAssistComponentForUser(i);
    }

    private ComponentName getAssistInfo() {
        return getAssistInfoForUser(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void onLockscreenShown() {
        this.mAssistUtils.onLockscreenShown();
    }

    public long getAssistHandleShowAndGoRemainingDurationMs() {
        return this.mHandleController.getShowAndGoRemainingTimeMs();
    }

    public int toLoggingSubType(int i) {
        return toLoggingSubType(i, this.mPhoneStateMonitor.getPhoneState());
    }

    /* access modifiers changed from: protected */
    public void logStartAssist(int i, int i2) {
        MetricsLogger.action(new LogMaker(1716).setType(1).setSubtype(toLoggingSubType(i, i2)));
    }

    /* access modifiers changed from: protected */
    public final int toLoggingSubType(int i, int i2) {
        return ((this.mHandleController.areHandlesShowing() ^ true) | (i << 1)) | (i2 << 4) ? 1 : 0;
    }
}
