package com.android.settings.notification;

import android.app.NotificationChannel;
import android.content.Context;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.notification.NotificationSettingsBase;
import com.android.settingslib.RestrictedSwitchPreference;

public class HighImportancePreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {
    private NotificationSettingsBase.ImportanceListener mImportanceListener;

    public String getPreferenceKey() {
        return "high_importance";
    }

    public HighImportancePreferenceController(Context context, NotificationSettingsBase.ImportanceListener importanceListener, NotificationBackend notificationBackend) {
        super(context, notificationBackend);
        this.mImportanceListener = importanceListener;
    }

    public boolean isAvailable() {
        if (super.isAvailable() && this.mChannel != null && !isDefaultChannel() && this.mChannel.getImportance() >= 3) {
            return true;
        }
        return false;
    }

    public void updateState(Preference preference) {
        NotificationChannel notificationChannel;
        if (this.mAppRow != null && (notificationChannel = this.mChannel) != null) {
            boolean z = true;
            preference.setEnabled(this.mAdmin == null && !notificationChannel.isImportanceLockedByOEM());
            RestrictedSwitchPreference restrictedSwitchPreference = (RestrictedSwitchPreference) preference;
            if (this.mChannel.getImportance() < 4) {
                z = false;
            }
            restrictedSwitchPreference.setChecked(z);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (this.mChannel == null) {
            return true;
        }
        this.mChannel.setImportance(((Boolean) obj).booleanValue() ? 4 : 3);
        this.mChannel.lockFields(4);
        saveChannel();
        this.mImportanceListener.onImportanceChanged();
        return true;
    }
}
