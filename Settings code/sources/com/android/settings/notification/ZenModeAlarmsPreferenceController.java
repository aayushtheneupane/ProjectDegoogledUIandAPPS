package com.android.settings.notification;

import android.content.Context;
import android.util.Log;
import androidx.preference.Preference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.havoc.support.preferences.SwitchPreference;

public class ZenModeAlarmsPreferenceController extends AbstractZenModePreferenceController implements Preference.OnPreferenceChangeListener {
    private final String KEY;

    public boolean isAvailable() {
        return true;
    }

    public ZenModeAlarmsPreferenceController(Context context, Lifecycle lifecycle, String str) {
        super(context, str, lifecycle);
        this.KEY = str;
    }

    public String getPreferenceKey() {
        return this.KEY;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        SwitchPreference switchPreference = (SwitchPreference) preference;
        int zenMode = getZenMode();
        if (zenMode == 2) {
            switchPreference.setEnabled(false);
            switchPreference.setChecked(false);
        } else if (zenMode != 3) {
            switchPreference.setEnabled(true);
            switchPreference.setChecked(this.mBackend.isPriorityCategoryEnabled(32));
        } else {
            switchPreference.setEnabled(false);
            switchPreference.setChecked(true);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean booleanValue = ((Boolean) obj).booleanValue();
        if (ZenModeSettingsBase.DEBUG) {
            Log.d("PrefControllerMixin", "onPrefChange allowAlarms=" + booleanValue);
        }
        this.mMetricsFeatureProvider.action(this.mContext, 1226, booleanValue);
        this.mBackend.saveSoundPolicy(32, booleanValue);
        return true;
    }
}
