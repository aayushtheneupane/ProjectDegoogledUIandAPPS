package com.android.settings.development;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.havoc.support.preferences.SwitchPreference;

public class EnableGpuDebugLayersPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    static final int SETTING_VALUE_OFF = 0;
    static final int SETTING_VALUE_ON = 1;

    public String getPreferenceKey() {
        return "enable_gpu_debug_layers";
    }

    public EnableGpuDebugLayersPreferenceController(Context context) {
        super(context);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "enable_gpu_debug_layers", ((Boolean) obj).booleanValue() ? 1 : 0);
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "enable_gpu_debug_layers", 0);
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (i != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* access modifiers changed from: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Settings.Global.putInt(this.mContext.getContentResolver(), "enable_gpu_debug_layers", 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
