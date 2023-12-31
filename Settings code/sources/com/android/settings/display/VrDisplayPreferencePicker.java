package com.android.settings.display;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.settings.widget.RadioButtonPickerFragment;
import com.android.settingslib.widget.CandidateInfo;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.List;

public class VrDisplayPreferencePicker extends RadioButtonPickerFragment {
    public int getMetricsCategory() {
        return 921;
    }

    /* access modifiers changed from: protected */
    public int getPreferenceScreenResId() {
        return C1715R.xml.vr_display_settings;
    }

    /* access modifiers changed from: protected */
    public List<VrCandidateInfo> getCandidates() {
        ArrayList arrayList = new ArrayList();
        Context context = getContext();
        arrayList.add(new VrCandidateInfo(context, 0, C1715R.string.display_vr_pref_low_persistence));
        arrayList.add(new VrCandidateInfo(context, 1, C1715R.string.display_vr_pref_off));
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public String getDefaultKey() {
        int intForUser = Settings.Secure.getIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, this.mUserId);
        return "vr_display_pref_" + intForUser;
    }

    /* access modifiers changed from: protected */
    public boolean setDefaultKey(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        char c = 65535;
        switch (str.hashCode()) {
            case 1581655828:
                if (str.equals("vr_display_pref_0")) {
                    c = 0;
                    break;
                }
                break;
            case 1581655829:
                if (str.equals("vr_display_pref_1")) {
                    c = 1;
                    break;
                }
                break;
        }
        if (c == 0) {
            return Settings.Secure.putIntForUser(getContext().getContentResolver(), "vr_display_mode", 0, this.mUserId);
        }
        if (c != 1) {
            return false;
        }
        return Settings.Secure.putIntForUser(getContext().getContentResolver(), "vr_display_mode", 1, this.mUserId);
    }

    static class VrCandidateInfo extends CandidateInfo {
        public final String label;
        public final int value;

        public Drawable loadIcon() {
            return null;
        }

        public VrCandidateInfo(Context context, int i, int i2) {
            super(true);
            this.value = i;
            this.label = context.getString(i2);
        }

        public CharSequence loadLabel() {
            return this.label;
        }

        public String getKey() {
            return "vr_display_pref_" + this.value;
        }
    }
}
