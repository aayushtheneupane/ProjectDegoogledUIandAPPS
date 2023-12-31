package com.android.settings.notification;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.service.notification.ZenModeConfig;
import androidx.preference.Preference;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.havoc.config.center.C1715R;

public class ZenModeBehaviorFooterPreferenceController extends AbstractZenModePreferenceController {
    private final int mTitleRes;

    private boolean isDeprecatedZenMode(int i) {
        return i == 2 || i == 3;
    }

    public String getPreferenceKey() {
        return "footer_preference";
    }

    public boolean isAvailable() {
        return true;
    }

    public ZenModeBehaviorFooterPreferenceController(Context context, Lifecycle lifecycle, int i) {
        super(context, "footer_preference", lifecycle);
        this.mTitleRes = i;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setTitle((CharSequence) getFooterText());
    }

    /* access modifiers changed from: protected */
    public String getFooterText() {
        ComponentName componentName;
        if (!isDeprecatedZenMode(getZenMode())) {
            return this.mContext.getString(this.mTitleRes);
        }
        ZenModeConfig zenModeConfig = getZenModeConfig();
        ZenModeConfig.ZenRule zenRule = zenModeConfig.manualRule;
        if (zenRule != null && isDeprecatedZenMode(zenRule.zenMode)) {
            ZenModeConfig.ZenRule zenRule2 = zenModeConfig.manualRule;
            Uri uri = zenRule2.conditionId;
            String str = zenRule2.enabler;
            if (str == null) {
                return this.mContext.getString(C1715R.string.zen_mode_qs_set_behavior);
            }
            String ownerCaption = AbstractZenModePreferenceController.mZenModeConfigWrapper.getOwnerCaption(str);
            if (!ownerCaption.isEmpty()) {
                return this.mContext.getString(C1715R.string.zen_mode_app_set_behavior, new Object[]{ownerCaption});
            }
        }
        for (ZenModeConfig.ZenRule zenRule3 : zenModeConfig.automaticRules.values()) {
            if (zenRule3.isAutomaticActive() && isDeprecatedZenMode(zenRule3.zenMode) && (componentName = zenRule3.component) != null) {
                return this.mContext.getString(C1715R.string.zen_mode_app_set_behavior, new Object[]{componentName.getPackageName()});
            }
        }
        return this.mContext.getString(C1715R.string.zen_mode_unknown_app_set_behavior);
    }
}
