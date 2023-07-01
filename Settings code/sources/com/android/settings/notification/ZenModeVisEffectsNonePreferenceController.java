package com.android.settings.notification;

import android.content.Context;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.notification.ZenCustomRadioButtonPreference;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeVisEffectsNonePreferenceController extends AbstractZenModePreferenceController implements ZenCustomRadioButtonPreference.OnRadioButtonClickListener {
    private ZenCustomRadioButtonPreference mPreference;

    public boolean isAvailable() {
        return true;
    }

    public ZenModeVisEffectsNonePreferenceController(Context context, Lifecycle lifecycle, String str) {
        super(context, str, lifecycle);
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mPreference = (ZenCustomRadioButtonPreference) preferenceScreen.findPreference(getPreferenceKey());
        this.mPreference.setOnRadioButtonClickListener(this);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreference.setChecked(this.mBackend.mPolicy.suppressedVisualEffects == 0);
    }

    public void onRadioButtonClick(ZenCustomRadioButtonPreference zenCustomRadioButtonPreference) {
        this.mMetricsFeatureProvider.action(this.mContext, 1396, true);
        this.mBackend.saveVisualEffectsPolicy(511, false);
    }
}
