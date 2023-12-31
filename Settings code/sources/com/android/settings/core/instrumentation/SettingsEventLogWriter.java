package com.android.settings.core.instrumentation;

import android.content.Context;
import android.provider.DeviceConfig;
import com.android.settingslib.core.instrumentation.EventLogWriter;

public class SettingsEventLogWriter extends EventLogWriter {
    public void visible(Context context, int i, int i2) {
        if (!shouldDisableGenericEventLogging()) {
            super.visible(context, i, i2);
        }
    }

    public void hidden(Context context, int i) {
        if (!shouldDisableGenericEventLogging()) {
            super.hidden(context, i);
        }
    }

    public void action(Context context, int i, String str) {
        if (!shouldDisableGenericEventLogging()) {
            super.action(context, i, str);
        }
    }

    public void action(Context context, int i, int i2) {
        if (!shouldDisableGenericEventLogging()) {
            super.action(context, i, i2);
        }
    }

    public void action(Context context, int i, boolean z) {
        if (!shouldDisableGenericEventLogging()) {
            super.action(context, i, z);
        }
    }

    private static boolean shouldDisableGenericEventLogging() {
        return true ^ DeviceConfig.getBoolean("settings_ui", "event_logging_enabled", true);
    }
}
