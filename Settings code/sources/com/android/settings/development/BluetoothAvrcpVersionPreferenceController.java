package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.havoc.config.center.C1715R;

public class BluetoothAvrcpVersionPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    static final String BLUETOOTH_AVRCP_VERSION_PROPERTY = "persist.bluetooth.avrcpversion";
    private final String[] mListSummaries;
    private final String[] mListValues;

    public String getPreferenceKey() {
        return "bluetooth_select_avrcp_version";
    }

    public BluetoothAvrcpVersionPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(C1715R.array.bluetooth_avrcp_version_values);
        this.mListSummaries = context.getResources().getStringArray(C1715R.array.bluetooth_avrcp_versions);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        SystemProperties.set(BLUETOOTH_AVRCP_VERSION_PROPERTY, obj.toString());
        updateState(this.mPreference);
        return true;
    }

    public void updateState(Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        String str = SystemProperties.get(BLUETOOTH_AVRCP_VERSION_PROPERTY);
        int i = 0;
        int i2 = 0;
        while (true) {
            String[] strArr = this.mListValues;
            if (i2 >= strArr.length) {
                break;
            } else if (TextUtils.equals(str, strArr[i2])) {
                i = i2;
                break;
            } else {
                i2++;
            }
        }
        listPreference.setValue(this.mListValues[i]);
        listPreference.setSummary(this.mListSummaries[i]);
    }
}
