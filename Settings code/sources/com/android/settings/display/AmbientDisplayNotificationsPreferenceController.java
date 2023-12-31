package com.android.settings.display;

import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Pair;
import androidx.preference.Preference;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class AmbientDisplayNotificationsPreferenceController extends TogglePreferenceController implements Preference.OnPreferenceChangeListener {
    static final String KEY_AMBIENT_DISPLAY_NOTIFICATIONS = "ambient_display_notification";
    private static final int MY_USER = UserHandle.myUserId();
    private final int OFF = 0;

    /* renamed from: ON */
    private final int f27ON = 1;
    private AmbientDisplayConfiguration mConfig;
    private final MetricsFeatureProvider mMetricsFeatureProvider;

    public AmbientDisplayNotificationsPreferenceController(Context context, String str) {
        super(context, str);
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public AmbientDisplayNotificationsPreferenceController setConfig(AmbientDisplayConfiguration ambientDisplayConfiguration) {
        this.mConfig = ambientDisplayConfiguration;
        return this;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_AMBIENT_DISPLAY_NOTIFICATIONS.equals(preference.getKey())) {
            this.mMetricsFeatureProvider.action(this.mContext, 495, (Pair<Integer, Object>[]) new Pair[0]);
        }
        return false;
    }

    public boolean isChecked() {
        return getAmbientConfig().pulseOnNotificationEnabled(MY_USER);
    }

    public boolean setChecked(boolean z) {
        Settings.Secure.putInt(this.mContext.getContentResolver(), "doze_enabled", z ? 1 : 0);
        return true;
    }

    public int getAvailabilityStatus() {
        return getAmbientConfig().pulseOnNotificationAvailable() ? 0 : 3;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_AMBIENT_DISPLAY_NOTIFICATIONS);
    }

    private AmbientDisplayConfiguration getAmbientConfig() {
        if (this.mConfig == null) {
            this.mConfig = new AmbientDisplayConfiguration(this.mContext);
        }
        return this.mConfig;
    }
}
