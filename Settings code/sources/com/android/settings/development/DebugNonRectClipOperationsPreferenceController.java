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

public class DebugNonRectClipOperationsPreferenceController extends DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    private final String[] mListSummaries;
    private final String[] mListValues;

    public String getPreferenceKey() {
        return "show_non_rect_clip";
    }

    public DebugNonRectClipOperationsPreferenceController(Context context) {
        super(context);
        this.mListValues = context.getResources().getStringArray(C1715R.array.show_non_rect_clip_values);
        this.mListSummaries = context.getResources().getStringArray(C1715R.array.show_non_rect_clip_entries);
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        writeShowNonRectClipOptions(obj);
        updateShowNonRectClipOptions();
        return true;
    }

    public void updateState(Preference preference) {
        updateShowNonRectClipOptions();
    }

    private void writeShowNonRectClipOptions(Object obj) {
        SystemProperties.set("debug.hwui.show_non_rect_clip", obj == null ? "" : obj.toString());
        SystemPropPoker.getInstance().poke();
    }

    private void updateShowNonRectClipOptions() {
        String str = SystemProperties.get("debug.hwui.show_non_rect_clip", "hide");
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
