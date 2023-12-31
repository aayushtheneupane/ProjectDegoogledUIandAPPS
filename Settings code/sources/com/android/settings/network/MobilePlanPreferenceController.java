package com.android.settings.network;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;
import com.havoc.config.center.C1715R;
import java.util.List;

public class MobilePlanPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnCreate, OnSaveInstanceState {
    private ConnectivityManager mCm;
    private final MobilePlanPreferenceHost mHost;
    private final boolean mIsSecondaryUser = (!this.mUserManager.isAdminUser());
    private String mMobilePlanDialogMessage;
    private TelephonyManager mTm;
    private final UserManager mUserManager;

    public interface MobilePlanPreferenceHost {
        void showMobilePlanMessageDialog();
    }

    public String getPreferenceKey() {
        return "manage_mobile_plan";
    }

    public MobilePlanPreferenceController(Context context, MobilePlanPreferenceHost mobilePlanPreferenceHost) {
        super(context);
        this.mHost = mobilePlanPreferenceHost;
        this.mCm = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTm = (TelephonyManager) context.getSystemService("phone");
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (this.mHost == null || !"manage_mobile_plan".equals(preference.getKey())) {
            return false;
        }
        this.mMobilePlanDialogMessage = null;
        onManageMobilePlanClick();
        return false;
    }

    public void onCreate(Bundle bundle) {
        if (bundle != null) {
            this.mMobilePlanDialogMessage = bundle.getString("mManageMobilePlanMessage");
        }
        Log.d("MobilePlanPrefContr", "onCreate: mMobilePlanDialogMessage=" + this.mMobilePlanDialogMessage);
    }

    public void onSaveInstanceState(Bundle bundle) {
        if (!TextUtils.isEmpty(this.mMobilePlanDialogMessage)) {
            bundle.putString("mManageMobilePlanMessage", this.mMobilePlanDialogMessage);
        }
    }

    public String getMobilePlanDialogMessage() {
        return this.mMobilePlanDialogMessage;
    }

    public void setMobilePlanDialogMessage(String str) {
        this.mMobilePlanDialogMessage = str;
    }

    public boolean isAvailable() {
        boolean z = this.mContext.getResources().getBoolean(C1715R.bool.config_show_mobile_plan);
        if (!(!this.mIsSecondaryUser && !Utils.isWifiOnly(this.mContext) && !RestrictedLockUtilsInternal.hasBaseUserRestriction(this.mContext, "no_config_mobile_networks", UserHandle.myUserId())) || !z) {
            return false;
        }
        return true;
    }

    private void onManageMobilePlanClick() {
        Resources resources = this.mContext.getResources();
        NetworkInfo activeNetworkInfo = this.mCm.getActiveNetworkInfo();
        if (this.mTm.hasIccCard() && activeNetworkInfo != null) {
            Intent intent = new Intent("android.intent.action.CARRIER_SETUP");
            List carrierPackageNamesForIntent = this.mTm.getCarrierPackageNamesForIntent(intent);
            if (carrierPackageNamesForIntent == null || carrierPackageNamesForIntent.isEmpty()) {
                String mobileProvisioningUrl = this.mCm.getMobileProvisioningUrl();
                if (!TextUtils.isEmpty(mobileProvisioningUrl)) {
                    Intent makeMainSelectorActivity = Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_BROWSER");
                    makeMainSelectorActivity.setData(Uri.parse(mobileProvisioningUrl));
                    makeMainSelectorActivity.setFlags(272629760);
                    try {
                        this.mContext.startActivity(makeMainSelectorActivity);
                    } catch (ActivityNotFoundException e) {
                        Log.w("MobilePlanPrefContr", "onManageMobilePlanClick: startActivity failed" + e);
                    }
                } else {
                    String simOperatorName = this.mTm.getSimOperatorName();
                    if (TextUtils.isEmpty(simOperatorName)) {
                        String networkOperatorName = this.mTm.getNetworkOperatorName();
                        if (TextUtils.isEmpty(networkOperatorName)) {
                            this.mMobilePlanDialogMessage = resources.getString(C1715R.string.mobile_unknown_sim_operator);
                        } else {
                            this.mMobilePlanDialogMessage = resources.getString(C1715R.string.mobile_no_provisioning_url, new Object[]{networkOperatorName});
                        }
                    } else {
                        this.mMobilePlanDialogMessage = resources.getString(C1715R.string.mobile_no_provisioning_url, new Object[]{simOperatorName});
                    }
                }
            } else {
                if (carrierPackageNamesForIntent.size() != 1) {
                    Log.w("MobilePlanPrefContr", "Multiple matching carrier apps found, launching the first.");
                }
                intent.setPackage((String) carrierPackageNamesForIntent.get(0));
                this.mContext.startActivity(intent);
                return;
            }
        } else if (!this.mTm.hasIccCard()) {
            this.mMobilePlanDialogMessage = resources.getString(C1715R.string.mobile_insert_sim_card);
        } else {
            this.mMobilePlanDialogMessage = resources.getString(C1715R.string.mobile_connect_to_internet);
        }
        if (!TextUtils.isEmpty(this.mMobilePlanDialogMessage)) {
            Log.d("MobilePlanPrefContr", "onManageMobilePlanClick: message=" + this.mMobilePlanDialogMessage);
            MobilePlanPreferenceHost mobilePlanPreferenceHost = this.mHost;
            if (mobilePlanPreferenceHost != null) {
                mobilePlanPreferenceHost.showMobilePlanMessageDialog();
            } else {
                Log.d("MobilePlanPrefContr", "Missing host fragment, cannot show message dialog.");
            }
        }
    }
}
