package com.android.settings.notification;

import android.content.Context;
import android.util.Log;
import androidx.preference.Preference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.havoc.support.preferences.SwitchPreference;

public class ZenModeEventsPreferenceController extends AbstractZenModePreferenceController implements Preference.OnPreferenceChangeListener {
    public String getPreferenceKey() {
        return "zen_mode_events";
    }

    public boolean isAvailable() {
        return true;
    }

    public ZenModeEventsPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, "zen_mode_events", lifecycle);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        SwitchPreference switchPreference = (SwitchPreference) preference;
        int zenMode = getZenMode();
        if (zenMode == 2 || zenMode == 3) {
            switchPreference.setEnabled(false);
            switchPreference.setChecked(false);
            return;
        }
        switchPreference.setChecked(this.mBackend.isPriorityCategoryEnabled(2));
        switchPreference.setEnabled(true);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean booleanValue = ((Boolean) obj).booleanValue();
        if (ZenModeSettingsBase.DEBUG) {
            Log.d("PrefControllerMixin", "onPrefChange allowEvents=" + booleanValue);
        }
        this.mMetricsFeatureProvider.action(this.mContext, 168, booleanValue);
        this.mBackend.saveSoundPolicy(2, booleanValue);
        return true;
    }
}
