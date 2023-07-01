package com.android.settings.applications.appinfo;

import android.content.Context;
import android.text.BidiFormatter;
import com.havoc.config.center.C1715R;

public class AppVersionPreferenceController extends AppInfoPreferenceControllerBase {
    public AppVersionPreferenceController(Context context, String str) {
        super(context, str);
    }

    public CharSequence getSummary() {
        return this.mContext.getString(C1715R.string.version_text, new Object[]{BidiFormatter.getInstance().unicodeWrap(this.mParent.getPackageInfo().versionName)});
    }
}
