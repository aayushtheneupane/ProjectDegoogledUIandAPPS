package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.notification.ZenModeSettings;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class ZenModePreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    private SettingObserver mSettingObserver;
    private ZenModeSettings.SummaryBuilder mSummaryBuilder;

    public int getAvailabilityStatus() {
        return 1;
    }

    public ZenModePreferenceController(Context context, String str) {
        super(context, str);
        this.mSummaryBuilder = new ZenModeSettings.SummaryBuilder(context);
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        Preference findPreference = preferenceScreen.findPreference(getPreferenceKey());
        if (findPreference != null) {
            updateState(findPreference);
            this.mSettingObserver = new SettingObserver(findPreference);
        }
    }

    public void onResume() {
        SettingObserver settingObserver = this.mSettingObserver;
        if (settingObserver != null) {
            settingObserver.register(this.mContext.getContentResolver());
        }
    }

    public void onPause() {
        SettingObserver settingObserver = this.mSettingObserver;
        if (settingObserver != null) {
            settingObserver.unregister(this.mContext.getContentResolver());
        }
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference.isEnabled()) {
            preference.setSummary((CharSequence) this.mSummaryBuilder.getSoundSummary());
        }
    }

    class SettingObserver extends ContentObserver {
        private final Uri ZEN_MODE_CONFIG_ETAG_URI = Settings.Global.getUriFor("zen_mode_config_etag");
        private final Uri ZEN_MODE_URI = Settings.Global.getUriFor("zen_mode");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver contentResolver) {
            contentResolver.registerContentObserver(this.ZEN_MODE_URI, false, this, -1);
            contentResolver.registerContentObserver(this.ZEN_MODE_CONFIG_ETAG_URI, false, this, -1);
        }

        public void unregister(ContentResolver contentResolver) {
            contentResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            if (this.ZEN_MODE_URI.equals(uri)) {
                ZenModePreferenceController.this.updateState(this.mPreference);
            }
            if (this.ZEN_MODE_CONFIG_ETAG_URI.equals(uri)) {
                ZenModePreferenceController.this.updateState(this.mPreference);
            }
        }
    }
}
