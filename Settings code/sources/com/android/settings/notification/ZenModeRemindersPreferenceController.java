package com.android.settings.notification;

import android.content.Context;
import android.util.Log;
import androidx.preference.Preference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.havoc.support.preferences.SwitchPreference;

public class ZenModeRemindersPreferenceController extends AbstractZenModePreferenceController implements Preference.OnPreferenceChangeListener {
    public String getPreferenceKey() {
        return "zen_mode_reminders";
    }

    public boolean isAvailable() {
        return true;
    }

    public ZenModeRemindersPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, "zen_mode_reminders", lifecycle);
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
        switchPreference.setEnabled(true);
        switchPreference.setChecked(this.mBackend.isPriorityCategoryEnabled(1));
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean booleanValue = ((Boolean) obj).booleanValue();
        if (ZenModeSettingsBase.DEBUG) {
            Log.d("PrefControllerMixin", "onPrefChange allowReminders=" + booleanValue);
        }
        this.mMetricsFeatureProvider.action(this.mContext, 167, booleanValue);
        this.mBackend.saveSoundPolicy(1, booleanValue);
        return true;
    }
}
