package com.android.settings.development;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.havoc.support.preferences.SwitchPreference;

public class EnableGnssRawMeasFullTrackingPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    public String getPreferenceKey() {
        return "enable_gnss_raw_meas_full_tracking";
    }

    public EnableGnssRawMeasFullTrackingPreferenceController(Context context) {
        super(context);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "enable_gnss_raw_meas_full_tracking", ((Boolean) obj).booleanValue() ? 1 : 0);
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "enable_gnss_raw_meas_full_tracking", 0);
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (i != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* access modifiers changed from: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Settings.Global.putInt(this.mContext.getContentResolver(), "enable_gnss_raw_meas_full_tracking", 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
