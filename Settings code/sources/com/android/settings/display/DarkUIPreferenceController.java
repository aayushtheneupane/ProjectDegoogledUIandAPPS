package com.android.settings.display;

import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.havoc.config.center.C1715R;

public class DarkUIPreferenceController extends TogglePreferenceController implements LifecycleObserver, OnStart, OnStop {
    public static final String DARK_MODE_PREFS = "dark_mode_prefs";
    public static final int DIALOG_SEEN = 1;
    public static final String PREF_DARK_MODE_DIALOG_SEEN = "dark_mode_dialog_seen";
    private Context mContext;
    private Fragment mFragment;
    private PowerManager mPowerManager;
    Preference mPreference;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DarkUIPreferenceController.this.updateEnabledStateIfNeeded();
        }
    };
    private UiModeManager mUiModeManager;

    public int getAvailabilityStatus() {
        return 0;
    }

    public DarkUIPreferenceController(Context context, String str) {
        super(context, str);
        this.mContext = context;
        this.mUiModeManager = (UiModeManager) context.getSystemService(UiModeManager.class);
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
    }

    public boolean isChecked() {
        return (this.mContext.getResources().getConfiguration().uiMode & 32) != 0;
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mPreference = preferenceScreen.findPreference(getPreferenceKey());
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        updateEnabledStateIfNeeded();
    }

    public boolean setChecked(boolean z) {
        boolean z2 = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), PREF_DARK_MODE_DIALOG_SEEN, 0) == 1) {
            z2 = true;
        }
        if (!z2 && z) {
            showDarkModeDialog();
        }
        return this.mUiModeManager.setNightModeActivated(z);
    }

    private void showDarkModeDialog() {
        DarkUIInfoDialogFragment darkUIInfoDialogFragment = new DarkUIInfoDialogFragment();
        Fragment fragment = this.mFragment;
        if (fragment != null && fragment.getFragmentManager() != null) {
            darkUIInfoDialogFragment.show(this.mFragment.getFragmentManager(), DarkUIPreferenceController.class.getName());
        }
    }

    /* access modifiers changed from: package-private */
    public void updateEnabledStateIfNeeded() {
        if (this.mPreference != null) {
            boolean isPowerSaveMode = isPowerSaveMode();
            this.mPreference.setEnabled(!isPowerSaveMode);
            if (isPowerSaveMode) {
                this.mPreference.setSummary((CharSequence) this.mContext.getString(isChecked() ? C1715R.string.dark_ui_mode_disabled_summary_dark_theme_on : C1715R.string.dark_ui_mode_disabled_summary_dark_theme_off));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPowerSaveMode() {
        return this.mPowerManager.isPowerSaveMode();
    }

    public void onStart() {
        this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGED"));
    }

    public void setParentFragment(Fragment fragment) {
        this.mFragment = fragment;
    }

    public void onStop() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }
}
