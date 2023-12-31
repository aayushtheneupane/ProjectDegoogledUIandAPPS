package com.android.settings.datetime;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.havoc.support.preferences.SwitchPreference;

public class AutoTimeZonePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {
    private final UpdateTimeAndDateCallback mCallback;
    private final boolean mIsFromSUW;

    public String getPreferenceKey() {
        return "auto_zone";
    }

    public AutoTimeZonePreferenceController(Context context, UpdateTimeAndDateCallback updateTimeAndDateCallback, boolean z) {
        super(context);
        this.mCallback = updateTimeAndDateCallback;
        this.mIsFromSUW = z;
    }

    public boolean isAvailable() {
        return !Utils.isWifiOnly(this.mContext) && !this.mIsFromSUW;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            ((SwitchPreference) preference).setChecked(isEnabled());
        }
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "auto_time_zone", ((Boolean) obj).booleanValue() ? 1 : 0);
        this.mCallback.updateTimeAndDateDisplay(this.mContext);
        return true;
    }

    public boolean isEnabled() {
        return isAvailable() && Settings.Global.getInt(this.mContext.getContentResolver(), "auto_time_zone", 0) > 0;
    }
}
