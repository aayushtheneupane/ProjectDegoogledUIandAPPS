package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;
import androidx.preference.Preference;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.slices.Sliceable;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.havoc.config.center.C1715R;

public class BuildNumberPreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart {
    static final int REQUEST_CONFIRM_PASSWORD_FOR_DEV_PREF = 100;
    static final int TAPS_TO_BE_A_DEVELOPER = 7;
    private Activity mActivity;
    private RestrictedLockUtils.EnforcedAdmin mDebuggingFeaturesDisallowedAdmin;
    private boolean mDebuggingFeaturesDisallowedBySystem;
    private int mDevHitCountdown;
    private Toast mDevHitToast;
    private InstrumentedPreferenceFragment mFragment;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private boolean mProcessingLastDevHit;
    private final UserManager mUm;

    public int getAvailabilityStatus() {
        return 0;
    }

    public boolean isCopyableSlice() {
        return true;
    }

    public boolean isSliceable() {
        return true;
    }

    public boolean useDynamicSliceSummary() {
        return true;
    }

    public BuildNumberPreferenceController(Context context, String str) {
        super(context, str);
        this.mUm = (UserManager) context.getSystemService("user");
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public void setHost(InstrumentedPreferenceFragment instrumentedPreferenceFragment) {
        this.mFragment = instrumentedPreferenceFragment;
        this.mActivity = instrumentedPreferenceFragment.getActivity();
    }

    public CharSequence getSummary() {
        return BidiFormatter.getInstance().unicodeWrap(Build.DISPLAY);
    }

    public void onStart() {
        this.mDebuggingFeaturesDisallowedAdmin = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(this.mContext, "no_debugging_features", UserHandle.myUserId());
        this.mDebuggingFeaturesDisallowedBySystem = RestrictedLockUtilsInternal.hasBaseUserRestriction(this.mContext, "no_debugging_features", UserHandle.myUserId());
        this.mDevHitCountdown = DevelopmentSettingsEnabler.isDevelopmentSettingsEnabled(this.mContext) ? -1 : 7;
        this.mDevHitToast = null;
    }

    public void copy() {
        Sliceable.setCopyContent(this.mContext, getSummary(), this.mContext.getText(C1715R.string.build_number));
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        ComponentName deviceOwnerComponent;
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey()) || Utils.isMonkeyRunning()) {
            return false;
        }
        if (!this.mUm.isAdminUser() && !this.mUm.isDemoUser()) {
            this.mMetricsFeatureProvider.action(this.mContext, 847, (Pair<Integer, Object>[]) new Pair[0]);
            return false;
        } else if (!Utils.isDeviceProvisioned(this.mContext)) {
            this.mMetricsFeatureProvider.action(this.mContext, 847, (Pair<Integer, Object>[]) new Pair[0]);
            return false;
        } else if (this.mUm.hasUserRestriction("no_debugging_features")) {
            if (this.mUm.isDemoUser() && (deviceOwnerComponent = Utils.getDeviceOwnerComponent(this.mContext)) != null) {
                Intent action = new Intent().setPackage(deviceOwnerComponent.getPackageName()).setAction("com.android.settings.action.REQUEST_DEBUG_FEATURES");
                if (this.mContext.getPackageManager().resolveActivity(action, 0) != null) {
                    this.mContext.startActivity(action);
                    return false;
                }
            }
            RestrictedLockUtils.EnforcedAdmin enforcedAdmin = this.mDebuggingFeaturesDisallowedAdmin;
            if (enforcedAdmin != null && !this.mDebuggingFeaturesDisallowedBySystem) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, enforcedAdmin);
            }
            this.mMetricsFeatureProvider.action(this.mContext, 847, (Pair<Integer, Object>[]) new Pair[0]);
            return false;
        } else {
            int i = this.mDevHitCountdown;
            if (i > 0) {
                this.mDevHitCountdown = i - 1;
                int i2 = this.mDevHitCountdown;
                if (i2 != 0 || this.mProcessingLastDevHit) {
                    int i3 = this.mDevHitCountdown;
                    if (i3 > 0 && i3 < 5) {
                        Toast toast = this.mDevHitToast;
                        if (toast != null) {
                            toast.cancel();
                        }
                        Context context = this.mContext;
                        Resources resources = context.getResources();
                        int i4 = this.mDevHitCountdown;
                        this.mDevHitToast = Toast.makeText(context, resources.getQuantityString(C1715R.plurals.show_dev_countdown, i4, new Object[]{Integer.valueOf(i4)}), 0);
                        this.mDevHitToast.show();
                    }
                } else {
                    this.mDevHitCountdown = i2 + 1;
                    this.mProcessingLastDevHit = new ChooseLockSettingsHelper(this.mActivity, this.mFragment).launchConfirmationActivity(REQUEST_CONFIRM_PASSWORD_FOR_DEV_PREF, this.mContext.getString(C1715R.string.unlock_set_unlock_launch_picker_title));
                    if (!this.mProcessingLastDevHit) {
                        enableDevelopmentSettings();
                    }
                    MetricsFeatureProvider metricsFeatureProvider = this.mMetricsFeatureProvider;
                    metricsFeatureProvider.action(metricsFeatureProvider.getAttribution(this.mActivity), 848, this.mFragment.getMetricsCategory(), (String) null, this.mProcessingLastDevHit ^ true ? 1 : 0);
                }
                MetricsFeatureProvider metricsFeatureProvider2 = this.mMetricsFeatureProvider;
                metricsFeatureProvider2.action(metricsFeatureProvider2.getAttribution(this.mActivity), 848, this.mFragment.getMetricsCategory(), (String) null, 0);
            } else if (i < 0) {
                Toast toast2 = this.mDevHitToast;
                if (toast2 != null) {
                    toast2.cancel();
                }
                this.mDevHitToast = Toast.makeText(this.mContext, C1715R.string.show_dev_already, 1);
                this.mDevHitToast.show();
                MetricsFeatureProvider metricsFeatureProvider3 = this.mMetricsFeatureProvider;
                metricsFeatureProvider3.action(metricsFeatureProvider3.getAttribution(this.mActivity), 848, this.mFragment.getMetricsCategory(), (String) null, 1);
            }
            return true;
        }
    }

    public boolean onActivityResult(int i, int i2, Intent intent) {
        if (i != REQUEST_CONFIRM_PASSWORD_FOR_DEV_PREF) {
            return false;
        }
        if (i2 == -1) {
            enableDevelopmentSettings();
        }
        this.mProcessingLastDevHit = false;
        return true;
    }

    private void enableDevelopmentSettings() {
        this.mDevHitCountdown = 0;
        this.mProcessingLastDevHit = false;
        DevelopmentSettingsEnabler.setDevelopmentSettingsEnabled(this.mContext, true);
        Toast toast = this.mDevHitToast;
        if (toast != null) {
            toast.cancel();
        }
        this.mDevHitToast = Toast.makeText(this.mContext, C1715R.string.show_dev_on, 1);
        this.mDevHitToast.show();
    }
}
