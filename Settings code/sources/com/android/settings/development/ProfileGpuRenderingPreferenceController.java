package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.SystemPropPoker;
import com.havoc.config.center.C1715R;

public class ProfileGpuRenderingPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    private final String[] mListSummaries;
    private final String[] mListValues;

    public String getPreferenceKey() {
        return "track_frame_time";
    }

    public ProfileGpuRenderingPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(C1715R.array.track_frame_time_values);
        this.mListSummaries = context.getResources().getStringArray(C1715R.array.track_frame_time_entries);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        writeTrackFrameTimeOptions(obj);
        updateTrackFrameTimeOptions();
        return true;
    }

    public void updateState(Preference preference) {
        updateTrackFrameTimeOptions();
    }

    private void writeTrackFrameTimeOptions(Object obj) {
        SystemProperties.set("debug.hwui.profile", obj == null ? "" : obj.toString());
        SystemPropPoker.getInstance().poke();
    }

    private void updateTrackFrameTimeOptions() {
        String str = SystemProperties.get("debug.hwui.profile", "");
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
        ListPreference listPreference = (ListPreference) this.mPreference;
        listPreference.setValue(this.mListValues[i]);
        listPreference.setSummary(this.mListSummaries[i]);
    }
}
