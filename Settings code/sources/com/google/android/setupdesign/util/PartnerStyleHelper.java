package com.google.android.setupdesign.util;

import android.content.Context;
import com.google.android.setupcompat.partnerconfig.PartnerConfig;
import com.google.android.setupcompat.partnerconfig.PartnerConfigHelper;
import java.util.Locale;

public class PartnerStyleHelper {
    public static int getLayoutGravity(Context context) {
        String string = PartnerConfigHelper.get(context).getString(context, PartnerConfig.CONFIG_LAYOUT_GRAVITY);
        if (string == null) {
            return 0;
        }
        String lowerCase = string.toLowerCase(Locale.ROOT);
        char c = 65535;
        int hashCode = lowerCase.hashCode();
        if (hashCode != -1364013995) {
            if (hashCode == 109757538 && lowerCase.equals("start")) {
                c = 1;
            }
        } else if (lowerCase.equals("center")) {
            c = 0;
        }
        if (c == 0) {
            return 17;
        }
        if (c != 1) {
            return 0;
        }
        return 8388611;
    }
}
