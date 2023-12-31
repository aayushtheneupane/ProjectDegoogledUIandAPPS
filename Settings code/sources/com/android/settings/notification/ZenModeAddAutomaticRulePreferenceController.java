package com.android.settings.notification;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.notification.ZenRuleSelectionDialog;
import com.android.settings.utils.ZenServiceListing;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeAddAutomaticRulePreferenceController extends AbstractZenModeAutomaticRulePreferenceController implements Preference.OnPreferenceClickListener {
    private final ZenServiceListing mZenServiceListing;

    public String getPreferenceKey() {
        return "zen_mode_add_automatic_rule";
    }

    public boolean isAvailable() {
        return true;
    }

    public ZenModeAddAutomaticRulePreferenceController(Context context, Fragment fragment, ZenServiceListing zenServiceListing, Lifecycle lifecycle) {
        super(context, "zen_mode_add_automatic_rule", fragment, lifecycle);
        this.mZenServiceListing = zenServiceListing;
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        Preference findPreference = preferenceScreen.findPreference("zen_mode_add_automatic_rule");
        findPreference.setPersistent(false);
        findPreference.setOnPreferenceClickListener(this);
    }

    public boolean onPreferenceClick(Preference preference) {
        ZenRuleSelectionDialog.show(this.mContext, this.mParent, new RuleSelectionListener(), this.mZenServiceListing);
        return true;
    }

    public class RuleSelectionListener implements ZenRuleSelectionDialog.PositiveClickListener {
        public RuleSelectionListener() {
        }

        public void onSystemRuleSelected(ZenRuleInfo zenRuleInfo, Fragment fragment) {
            ZenModeAddAutomaticRulePreferenceController.this.showNameRuleDialog(zenRuleInfo, fragment);
        }

        public void onExternalRuleSelected(ZenRuleInfo zenRuleInfo, Fragment fragment) {
            fragment.startActivity(new Intent().setComponent(zenRuleInfo.configurationActivity));
        }
    }
}
