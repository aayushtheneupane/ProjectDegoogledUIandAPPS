package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.preference.Preference;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.DeviceInfoUtils;

public class SecurityPatchLevelPreferenceController extends BasePreferenceController {
    private static final Uri INTENT_URI_DATA = Uri.parse("https://source.android.com/security/bulletin/");
    private static final String TAG = "SecurityPatchCtrl";
    private final String mCurrentPatch = DeviceInfoUtils.getSecurityPatch();
    private final PackageManager mPackageManager = this.mContext.getPackageManager();

    public SecurityPatchLevelPreferenceController(Context context, String str) {
        super(context, str);
    }

    public int getAvailabilityStatus() {
        return !TextUtils.isEmpty(this.mCurrentPatch) ? 0 : 2;
    }

    public CharSequence getSummary() {
        return this.mCurrentPatch;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(INTENT_URI_DATA);
        if (this.mPackageManager.queryIntentActivities(intent, 0).isEmpty()) {
            Log.w(TAG, "queryIntentActivities() returns empty");
            return true;
        }
        this.mContext.startActivity(intent);
        return true;
    }
}
