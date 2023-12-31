package com.android.settings.applications.appinfo;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import com.android.settings.applications.AppInfoWithHeader;
import com.android.settings.applications.AppStateAppOpsBridge;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.AppStateWriteSettingsBridge;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.ApplicationsState;
import com.havoc.config.center.C1715R;
import com.havoc.support.preferences.SwitchPreference;

public class WriteSettingsDetails extends AppInfoWithHeader implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final int[] APP_OPS_OP_CODE = {23};
    private AppStateWriteSettingsBridge mAppBridge;
    private AppOpsManager mAppOpsManager;
    private Intent mSettingsIntent;
    private SwitchPreference mSwitchPref;
    private AppStateWriteSettingsBridge.WriteSettingsState mWriteSettingsState;

    /* access modifiers changed from: protected */
    public AlertDialog createDialog(int i, int i2) {
        return null;
    }

    public int getMetricsCategory() {
        return 221;
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        FragmentActivity activity = getActivity();
        this.mAppBridge = new AppStateWriteSettingsBridge(activity, this.mState, (AppStateBaseBridge.Callback) null);
        this.mAppOpsManager = (AppOpsManager) activity.getSystemService("appops");
        addPreferencesFromResource(C1715R.xml.write_system_settings_permissions_details);
        this.mSwitchPref = (SwitchPreference) findPreference("app_ops_settings_switch");
        this.mSwitchPref.setOnPreferenceChangeListener(this);
        this.mSettingsIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.USAGE_ACCESS_CONFIG").setPackage(this.mPackageName);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mAppBridge.release();
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        boolean z = false;
        if (preference != this.mSwitchPref) {
            return false;
        }
        if (!(this.mWriteSettingsState == null || ((Boolean) obj).booleanValue() == this.mWriteSettingsState.isPermissible())) {
            if (!this.mWriteSettingsState.isPermissible()) {
                z = true;
            }
            setCanWriteSettings(z);
            refreshUi();
        }
        return true;
    }

    private void setCanWriteSettings(boolean z) {
        logSpecialPermissionChange(z, this.mPackageName);
        this.mAppOpsManager.setMode(23, this.mPackageInfo.applicationInfo.uid, this.mPackageName, z ? 0 : 2);
    }

    /* access modifiers changed from: package-private */
    public void logSpecialPermissionChange(boolean z, String str) {
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), z ? 774 : 775, str);
    }

    /* access modifiers changed from: protected */
    public boolean refreshUi() {
        this.mWriteSettingsState = this.mAppBridge.getWriteSettingsInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        this.mSwitchPref.setChecked(this.mWriteSettingsState.isPermissible());
        this.mSwitchPref.setEnabled(this.mWriteSettingsState.permissionDeclared);
        this.mPm.resolveActivityAsUser(this.mSettingsIntent, 128, this.mUserId);
        return true;
    }

    public static CharSequence getSummary(Context context, ApplicationsState.AppEntry appEntry) {
        AppStateWriteSettingsBridge.WriteSettingsState writeSettingsState;
        Object obj = appEntry.extraInfo;
        if (obj instanceof AppStateWriteSettingsBridge.WriteSettingsState) {
            writeSettingsState = (AppStateWriteSettingsBridge.WriteSettingsState) obj;
        } else if (obj instanceof AppStateAppOpsBridge.PermissionState) {
            writeSettingsState = new AppStateWriteSettingsBridge.WriteSettingsState((AppStateAppOpsBridge.PermissionState) obj);
        } else {
            writeSettingsState = new AppStateWriteSettingsBridge(context, (ApplicationsState) null, (AppStateBaseBridge.Callback) null).getWriteSettingsInfo(appEntry.info.packageName, appEntry.info.uid);
        }
        return getSummary(context, writeSettingsState);
    }

    public static CharSequence getSummary(Context context, AppStateWriteSettingsBridge.WriteSettingsState writeSettingsState) {
        return context.getString(writeSettingsState.isPermissible() ? C1715R.string.app_permission_summary_allowed : C1715R.string.app_permission_summary_not_allowed);
    }
}
