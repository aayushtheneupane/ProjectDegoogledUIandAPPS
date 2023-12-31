package com.android.settings.deviceinfo.imei;

import android.content.Context;
import android.content.res.Resources;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TtsSpan;
import android.util.Log;
import com.havoc.config.center.C1715R;

public class ImeiInfoDialogController {
    static final int ID_CDMA_SETTINGS = 2131362028;
    static final int ID_GSM_SETTINGS = 2131362389;
    static final int ID_IMEI_SV_VALUE = 2131362438;
    static final int ID_IMEI_VALUE = 2131362439;
    static final int ID_MEID_NUMBER_VALUE = 2131362535;
    static final int ID_MIN_NUMBER_VALUE = 2131362548;
    static final int ID_PRL_VERSION_VALUE = 2131362752;
    private final ImeiInfoDialogFragment mDialog;
    private final int mSlotId;
    private final SubscriptionInfo mSubscriptionInfo;
    private final TelephonyManager mTelephonyManager;

    private static CharSequence getTextAsDigits(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            return "";
        }
        if (!TextUtils.isDigitsOnly(charSequence)) {
            return charSequence;
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(charSequence);
        spannableStringBuilder.setSpan(new TtsSpan.DigitsBuilder(charSequence.toString()).build(), 0, spannableStringBuilder.length(), 33);
        return spannableStringBuilder;
    }

    public ImeiInfoDialogController(ImeiInfoDialogFragment imeiInfoDialogFragment, int i) {
        this.mDialog = imeiInfoDialogFragment;
        this.mSlotId = i;
        Context context = imeiInfoDialogFragment.getContext();
        this.mSubscriptionInfo = ((SubscriptionManager) context.getSystemService(SubscriptionManager.class)).getActiveSubscriptionInfoForSimSlotIndex(i);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TelephonyManager.class);
        if (this.mSubscriptionInfo != null) {
            this.mTelephonyManager = ((TelephonyManager) context.getSystemService(TelephonyManager.class)).createForSubscriptionId(this.mSubscriptionInfo.getSubscriptionId());
        } else if (isValidSlotIndex(i, telephonyManager)) {
            this.mTelephonyManager = telephonyManager;
        } else {
            this.mTelephonyManager = null;
        }
    }

    public void populateImeiInfo() {
        TelephonyManager telephonyManager = this.mTelephonyManager;
        if (telephonyManager == null) {
            Log.w("ImeiInfoDialog", "TelephonyManager for this slot is null. Invalid slot? id=" + this.mSlotId);
        } else if (telephonyManager.getPhoneType() == 2) {
            updateDialogForCdmaPhone();
        } else {
            updateDialogForGsmPhone();
        }
    }

    private void updateDialogForCdmaPhone() {
        Resources resources = this.mDialog.getContext().getResources();
        this.mDialog.setText(C1715R.C1718id.meid_number_value, getMeid());
        ImeiInfoDialogFragment imeiInfoDialogFragment = this.mDialog;
        SubscriptionInfo subscriptionInfo = this.mSubscriptionInfo;
        imeiInfoDialogFragment.setText(C1715R.C1718id.min_number_value, subscriptionInfo != null ? this.mTelephonyManager.getCdmaMin(subscriptionInfo.getSubscriptionId()) : "");
        if (resources.getBoolean(C1715R.bool.config_msid_enable)) {
            this.mDialog.setText(C1715R.C1718id.min_number_label, resources.getString(C1715R.string.status_msid_number));
        }
        this.mDialog.setText(C1715R.C1718id.prl_version_value, getCdmaPrlVersion());
        if ((this.mSubscriptionInfo == null || !isCdmaLteEnabled()) && (this.mSubscriptionInfo != null || !isSimPresent(this.mSlotId))) {
            this.mDialog.removeViewFromScreen(C1715R.C1718id.gsm_settings);
            return;
        }
        this.mDialog.setText(C1715R.C1718id.imei_value, getTextAsDigits(this.mTelephonyManager.getImei(this.mSlotId)));
        this.mDialog.setText(C1715R.C1718id.imei_sv_value, getTextAsDigits(this.mTelephonyManager.getDeviceSoftwareVersion(this.mSlotId)));
    }

    private void updateDialogForGsmPhone() {
        this.mDialog.setText(C1715R.C1718id.imei_value, getTextAsDigits(this.mTelephonyManager.getImei(this.mSlotId)));
        this.mDialog.setText(C1715R.C1718id.imei_sv_value, getTextAsDigits(this.mTelephonyManager.getDeviceSoftwareVersion(this.mSlotId)));
        this.mDialog.removeViewFromScreen(C1715R.C1718id.cdma_settings);
    }

    /* access modifiers changed from: package-private */
    public String getCdmaPrlVersion() {
        return this.mSubscriptionInfo != null ? this.mTelephonyManager.getCdmaPrlVersion() : "";
    }

    /* access modifiers changed from: package-private */
    public boolean isCdmaLteEnabled() {
        return this.mTelephonyManager.getLteOnCdmaMode(this.mSubscriptionInfo.getSubscriptionId()) == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean isSimPresent(int i) {
        int simState = this.mTelephonyManager.getSimState(i);
        return (simState == 1 || simState == 0) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public String getMeid() {
        return this.mTelephonyManager.getMeid(this.mSlotId);
    }

    private boolean isValidSlotIndex(int i, TelephonyManager telephonyManager) {
        return i >= 0 && i < telephonyManager.getPhoneCount();
    }
}
