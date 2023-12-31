package com.android.settings.wifi.calling;

import android.content.Context;
import android.os.PersistableBundle;
import com.android.internal.annotations.VisibleForTesting;
import com.havoc.config.center.C1715R;

class LocationPolicyDisclaimer extends DisclaimerItem {
    @VisibleForTesting
    static final String KEY_HAS_AGREED_LOCATION_DISCLAIMER = "key_has_agreed_location_disclaimer";

    /* access modifiers changed from: protected */
    public int getMessageId() {
        return C1715R.string.wfc_disclaimer_location_desc_text;
    }

    /* access modifiers changed from: protected */
    public String getName() {
        return "LocationPolicyDisclaimer";
    }

    /* access modifiers changed from: protected */
    public String getPrefKey() {
        return KEY_HAS_AGREED_LOCATION_DISCLAIMER;
    }

    /* access modifiers changed from: protected */
    public int getTitleId() {
        return C1715R.string.wfc_disclaimer_location_title_text;
    }

    LocationPolicyDisclaimer(Context context, int i) {
        super(context, i);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShow() {
        PersistableBundle carrierConfig = getCarrierConfig();
        if (!carrierConfig.getBoolean("show_wfc_location_privacy_policy_bool")) {
            logd("shouldShow: false due to carrier config is false.");
            return false;
        } else if (!carrierConfig.getBoolean("carrier_default_wfc_ims_enabled_bool")) {
            return super.shouldShow();
        } else {
            logd("shouldShow: false due to WFC is on as default.");
            return false;
        }
    }
}
