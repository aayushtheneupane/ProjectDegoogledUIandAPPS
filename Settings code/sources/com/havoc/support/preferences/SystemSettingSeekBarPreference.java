package com.havoc.support.preferences;

import android.content.Context;
import android.util.AttributeSet;

public class SystemSettingSeekBarPreference extends CustomSeekBarPreference {
    public SystemSettingSeekBarPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
    }

    public SystemSettingSeekBarPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
    }

    public SystemSettingSeekBarPreference(Context context) {
        super(context, (AttributeSet) null);
        setPreferenceDataStore(new SystemSettingsStore(context.getContentResolver()));
    }
}
