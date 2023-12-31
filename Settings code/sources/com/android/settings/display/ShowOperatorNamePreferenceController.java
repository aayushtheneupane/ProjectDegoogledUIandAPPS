package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.havoc.config.center.C1715R;
import com.havoc.support.preferences.SwitchPreference;

public class ShowOperatorNamePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {
    public String getPreferenceKey() {
        return "show_operator_name";
    }

    public ShowOperatorNamePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(C1715R.bool.config_showOperatorNameInStatusBar);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        Settings.Secure.putInt(this.mContext.getContentResolver(), "show_operator_name", ((Boolean) obj).booleanValue() ? 1 : 0);
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = true;
        SwitchPreference switchPreference = (SwitchPreference) preference;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "show_operator_name", 1) == 0) {
            z = false;
        }
        switchPreference.setChecked(z);
    }
}
