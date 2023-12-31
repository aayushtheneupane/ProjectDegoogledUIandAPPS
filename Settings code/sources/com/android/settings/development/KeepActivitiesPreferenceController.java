package com.android.settings.development;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.havoc.support.preferences.SwitchPreference;

public class KeepActivitiesPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    static final int SETTING_VALUE_OFF = 0;
    private IActivityManager mActivityManager;

    public String getPreferenceKey() {
        return "immediately_destroy_activities";
    }

    public KeepActivitiesPreferenceController(Context context) {
        super(context);
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mActivityManager = getActivityManager();
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        writeImmediatelyDestroyActivitiesOptions(((Boolean) obj).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "always_finish_activities", 0);
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (i != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* access modifiers changed from: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeImmediatelyDestroyActivitiesOptions(false);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    private void writeImmediatelyDestroyActivitiesOptions(boolean z) {
        try {
            this.mActivityManager.setAlwaysFinish(z);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: package-private */
    public IActivityManager getActivityManager() {
        return ActivityManager.getService();
    }
}
