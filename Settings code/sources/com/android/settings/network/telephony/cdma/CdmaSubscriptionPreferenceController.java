package com.android.settings.network.telephony.cdma;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.settings.network.telephony.MobileNetworkUtils;

public class CdmaSubscriptionPreferenceController extends CdmaBasePreferenceController implements Preference.OnPreferenceChangeListener {
    private static final String TYPE_NV = "NV";
    private static final String TYPE_RUIM = "RUIM";
    ListPreference mPreference;

    public CdmaSubscriptionPreferenceController(Context context, String str) {
        super(context, str);
    }

    public int getAvailabilityStatus(int i) {
        return (!MobileNetworkUtils.isCdmaOptions(this.mContext, i) || !deviceSupportsNvAndRuim()) ? 2 : 0;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        ListPreference listPreference = (ListPreference) preference;
        listPreference.setVisible(getAvailabilityStatus() == 0);
        int i = Settings.Global.getInt(this.mContext.getContentResolver(), "subscription_mode", 0);
        if (i != -1) {
            listPreference.setValue(Integer.toString(i));
        }
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        int parseInt = Integer.parseInt((String) obj);
        if (!this.mTelephonyManager.setCdmaSubscriptionMode(parseInt)) {
            return false;
        }
        Settings.Global.putInt(this.mContext.getContentResolver(), "subscription_mode", parseInt);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean deviceSupportsNvAndRuim() {
        boolean z;
        boolean z2;
        String str = SystemProperties.get("ril.subscription.types");
        if (!TextUtils.isEmpty(str)) {
            z2 = false;
            z = false;
            for (String trim : str.split(",")) {
                String trim2 = trim.trim();
                if (trim2.equalsIgnoreCase(TYPE_NV)) {
                    z2 = true;
                } else if (trim2.equalsIgnoreCase(TYPE_RUIM)) {
                    z = true;
                }
            }
        } else {
            z2 = false;
            z = false;
        }
        if (!z2 || !z) {
            return false;
        }
        return true;
    }
}
