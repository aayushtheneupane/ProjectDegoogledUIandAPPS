package com.android.settings.notification;

import android.app.NotificationManager;
import android.content.Context;
import androidx.preference.Preference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.havoc.config.center.C1715R;

public class ZenFooterPreferenceController extends AbstractZenModePreferenceController {
    public ZenFooterPreferenceController(Context context, Lifecycle lifecycle, String str) {
        super(context, str, lifecycle);
    }

    public boolean isAvailable() {
        int i = this.mBackend.mPolicy.suppressedVisualEffects;
        return i == 0 || NotificationManager.Policy.areAllVisualEffectsSuppressed(i);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        int i = this.mBackend.mPolicy.suppressedVisualEffects;
        if (i == 0) {
            preference.setTitle((int) C1715R.string.zen_mode_restrict_notifications_mute_footer);
        } else if (NotificationManager.Policy.areAllVisualEffectsSuppressed(i)) {
            preference.setTitle((int) C1715R.string.zen_mode_restrict_notifications_hide_footer);
        } else {
            preference.setTitle((CharSequence) null);
        }
    }
}
