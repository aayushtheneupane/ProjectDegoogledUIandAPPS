package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.SystemPropPoker;
import com.havoc.support.preferences.SwitchPreference;

public class HardwareLayersUpdatesPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    public String getPreferenceKey() {
        return "show_hw_layers_updates";
    }

    public HardwareLayersUpdatesPreferenceController(Context context) {
        super(context);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        SystemProperties.set("debug.hwui.show_layers_updates", ((Boolean) obj).booleanValue() ? "true" : null);
        SystemPropPoker.getInstance().poke();
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean("debug.hwui.show_layers_updates", false));
    }

    /* access modifiers changed from: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set("debug.hwui.show_layers_updates", (String) null);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
