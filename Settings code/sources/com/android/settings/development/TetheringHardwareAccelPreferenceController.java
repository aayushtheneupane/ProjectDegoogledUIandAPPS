package com.android.settings.development;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.havoc.support.preferences.SwitchPreference;

public class TetheringHardwareAccelPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    static final int SETTING_VALUE_OFF = 1;
    static final int SETTING_VALUE_ON = 0;

    public String getPreferenceKey() {
        return "tethering_hardware_offload";
    }

    public TetheringHardwareAccelPreferenceController(Context context) {
        super(context);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "tether_offload_disabled", ((Boolean) obj).booleanValue() ^ true ? 1 : 0);
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "tether_offload_disabled", 0);
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (i != 1) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* access modifiers changed from: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Settings.Global.putInt(this.mContext.getContentResolver(), "tether_offload_disabled", 1);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
