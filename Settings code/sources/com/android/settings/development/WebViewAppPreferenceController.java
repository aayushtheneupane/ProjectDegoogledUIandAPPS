package com.android.settings.development;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.webview.WebViewUpdateServiceWrapper;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.havoc.config.center.C1715R;

public class WebViewAppPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private final PackageManager mPackageManager;
    private final WebViewUpdateServiceWrapper mWebViewUpdateServiceWrapper = new WebViewUpdateServiceWrapper();

    public String getPreferenceKey() {
        return "select_webview_provider";
    }

    public WebViewAppPreferenceController(Context context) {
        super(context);
        this.mPackageManager = context.getPackageManager();
    }

    public void updateState(Preference preference) {
        CharSequence defaultAppLabel = getDefaultAppLabel();
        if (!TextUtils.isEmpty(defaultAppLabel)) {
            this.mPreference.setSummary(defaultAppLabel);
            return;
        }
        Log.d("WebViewAppPrefCtrl", "No default app");
        this.mPreference.setSummary((int) C1715R.string.app_list_preference_none);
    }

    /* access modifiers changed from: package-private */
    public DefaultAppInfo getDefaultAppInfo() {
        ApplicationInfo applicationInfo;
        PackageInfo currentWebViewPackage = this.mWebViewUpdateServiceWrapper.getCurrentWebViewPackage();
        Context context = this.mContext;
        PackageManager packageManager = this.mPackageManager;
        int myUserId = UserHandle.myUserId();
        if (currentWebViewPackage == null) {
            applicationInfo = null;
        } else {
            applicationInfo = currentWebViewPackage.applicationInfo;
        }
        return new DefaultAppInfo(context, packageManager, myUserId, (PackageItemInfo) applicationInfo);
    }

    private CharSequence getDefaultAppLabel() {
        return getDefaultAppInfo().loadLabel();
    }
}
