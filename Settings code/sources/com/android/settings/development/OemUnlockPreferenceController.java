package com.android.settings.development;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.oemlock.OemLockManager;
import android.telephony.TelephonyManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.havoc.config.center.C1715R;

public class OemUnlockPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin, OnActivityResultListener {
    private final ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private final DevelopmentSettingsDashboardFragment mFragment;
    private final OemLockManager mOemLockManager;
    private RestrictedSwitchPreference mPreference;
    private final TelephonyManager mTelephonyManager;
    private final UserManager mUserManager;

    public String getPreferenceKey() {
        return "oem_unlock_enable";
    }

    public OemUnlockPreferenceController(Context context, Activity activity, DevelopmentSettingsDashboardFragment developmentSettingsDashboardFragment) {
        super(context);
        if (!Build.IS_EMULATOR || !Build.IS_ENG) {
            this.mOemLockManager = (OemLockManager) context.getSystemService("oem_lock");
        } else {
            this.mOemLockManager = null;
        }
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mFragment = developmentSettingsDashboardFragment;
        if (activity == null && this.mFragment == null) {
            this.mChooseLockSettingsHelper = null;
        } else {
            this.mChooseLockSettingsHelper = new ChooseLockSettingsHelper(activity, this.mFragment);
        }
    }

    public boolean isAvailable() {
        if (this.mOemLockManager == null || !SystemProperties.getBoolean("ro.oem_unlock_supported", false)) {
            return false;
        }
        return true;
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mPreference = (RestrictedSwitchPreference) preferenceScreen.findPreference(getPreferenceKey());
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (!((Boolean) obj).booleanValue()) {
            this.mOemLockManager.setOemUnlockAllowedByUser(false);
            OemLockInfoDialog.show(this.mFragment);
            return true;
        } else if (showKeyguardConfirmation(this.mContext.getResources(), 0)) {
            return true;
        } else {
            confirmEnableOemUnlock();
            return true;
        }
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreference.setChecked(isOemUnlockedAllowed());
        updateOemUnlockSettingDescription();
        this.mPreference.setDisabledByAdmin((RestrictedLockUtils.EnforcedAdmin) null);
        this.mPreference.setEnabled(enableOemUnlockPreference());
        if (this.mPreference.isEnabled()) {
            this.mPreference.checkRestrictionAndSetDisabled("no_factory_reset");
        }
    }

    public boolean onActivityResult(int i, int i2, Intent intent) {
        if (i != 0) {
            return false;
        }
        if (i2 != -1) {
            return true;
        }
        if (this.mPreference.isChecked()) {
            confirmEnableOemUnlock();
            return true;
        }
        this.mOemLockManager.setOemUnlockAllowedByUser(false);
        return true;
    }

    /* access modifiers changed from: protected */
    public void onDeveloperOptionsSwitchEnabled() {
        handleDeveloperOptionsToggled();
    }

    public void onOemUnlockConfirmed() {
        this.mOemLockManager.setOemUnlockAllowedByUser(true);
    }

    public void onOemUnlockDismissed() {
        RestrictedSwitchPreference restrictedSwitchPreference = this.mPreference;
        if (restrictedSwitchPreference != null) {
            updateState(restrictedSwitchPreference);
        }
    }

    private void handleDeveloperOptionsToggled() {
        this.mPreference.setEnabled(enableOemUnlockPreference());
        if (this.mPreference.isEnabled()) {
            this.mPreference.checkRestrictionAndSetDisabled("no_factory_reset");
        }
    }

    private void updateOemUnlockSettingDescription() {
        int i;
        if (isBootloaderUnlocked()) {
            i = C1715R.string.oem_unlock_enable_disabled_summary_bootloader_unlocked;
        } else if (isSimLockedDevice()) {
            i = C1715R.string.oem_unlock_enable_disabled_summary_sim_locked_device;
        } else {
            i = !isOemUnlockAllowedByUserAndCarrier() ? C1715R.string.oem_unlock_enable_disabled_summary_connectivity_or_locked : C1715R.string.oem_unlock_enable_summary;
        }
        this.mPreference.setSummary((CharSequence) this.mContext.getResources().getString(i));
    }

    private boolean isSimLockedDevice() {
        int phoneCount = this.mTelephonyManager.getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            if (this.mTelephonyManager.getAllowedCarriers(i).size() > 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isBootloaderUnlocked() {
        return this.mOemLockManager.isDeviceOemUnlocked();
    }

    private boolean enableOemUnlockPreference() {
        return !isBootloaderUnlocked() && isOemUnlockAllowedByUserAndCarrier();
    }

    /* access modifiers changed from: package-private */
    public boolean showKeyguardConfirmation(Resources resources, int i) {
        return this.mChooseLockSettingsHelper.launchConfirmationActivity(i, resources.getString(C1715R.string.oem_unlock_enable));
    }

    /* access modifiers changed from: package-private */
    public void confirmEnableOemUnlock() {
        EnableOemUnlockSettingWarningDialog.show(this.mFragment);
    }

    /* access modifiers changed from: package-private */
    public boolean isOemUnlockAllowedByUserAndCarrier() {
        return this.mOemLockManager.isOemUnlockAllowedByCarrier() && !this.mUserManager.hasBaseUserRestriction("no_factory_reset", UserHandle.of(UserHandle.myUserId()));
    }

    /* access modifiers changed from: package-private */
    public boolean isOemUnlockedAllowed() {
        return this.mOemLockManager.isOemUnlockAllowed();
    }
}
