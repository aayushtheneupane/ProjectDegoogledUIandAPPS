package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.service.notification.ZenModeConfig;
import android.view.View;
import android.widget.CheckBox;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.android.settings.utils.ManagedServiceSettings;
import com.android.settings.utils.ZenServiceListing;
import com.android.settingslib.TwoTargetPreference;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.havoc.config.center.C1715R;
import java.util.Map;

public class ZenRulePreference extends TwoTargetPreference {
    private static final ManagedServiceSettings.Config CONFIG = ZenModeAutomationSettings.getConditionProviderConfig();
    final ZenModeBackend mBackend;
    private CheckBox mCheckBox;
    /* access modifiers changed from: private */
    public boolean mChecked;
    final Context mContext;
    final String mId;
    /* access modifiers changed from: private */
    public Intent mIntent;
    final MetricsFeatureProvider mMetricsFeatureProvider;
    CharSequence mName;
    private View.OnClickListener mOnCheckBoxClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            ZenRulePreference zenRulePreference = ZenRulePreference.this;
            zenRulePreference.mRule.setEnabled(!zenRulePreference.mChecked);
            ZenRulePreference zenRulePreference2 = ZenRulePreference.this;
            zenRulePreference2.mBackend.updateZenRule(zenRulePreference2.mId, zenRulePreference2.mRule);
            ZenRulePreference zenRulePreference3 = ZenRulePreference.this;
            zenRulePreference3.setChecked(zenRulePreference3.mRule.isEnabled());
            ZenRulePreference zenRulePreference4 = ZenRulePreference.this;
            zenRulePreference4.setAttributes(zenRulePreference4.mRule);
        }
    };
    final Fragment mParent;
    final PackageManager mPm;
    final Preference mPref;
    AutomaticZenRule mRule;
    final ZenServiceListing mServiceListing;

    public ZenRulePreference(Context context, Map.Entry<String, AutomaticZenRule> entry, Fragment fragment, MetricsFeatureProvider metricsFeatureProvider) {
        super(context);
        setLayoutResource(C1715R.layout.preference_checkable_two_target);
        this.mBackend = ZenModeBackend.getInstance(context);
        this.mContext = context;
        this.mRule = entry.getValue();
        this.mName = this.mRule.getName();
        this.mId = entry.getKey();
        this.mParent = fragment;
        this.mPm = this.mContext.getPackageManager();
        this.mServiceListing = new ZenServiceListing(this.mContext, CONFIG);
        this.mServiceListing.reloadApprovedServices();
        this.mPref = this;
        this.mMetricsFeatureProvider = metricsFeatureProvider;
        this.mChecked = this.mRule.isEnabled();
        setAttributes(this.mRule);
        setWidgetLayoutResource(getSecondTargetResId());
    }

    /* access modifiers changed from: protected */
    public int getSecondTargetResId() {
        if (this.mIntent != null) {
            return C1715R.layout.zen_rule_widget;
        }
        return 0;
    }

    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(16908312);
        View findViewById2 = preferenceViewHolder.findViewById(C1715R.C1718id.two_target_divider);
        if (this.mIntent != null) {
            findViewById2.setVisibility(0);
            findViewById.setVisibility(0);
            findViewById.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    ZenRulePreference zenRulePreference = ZenRulePreference.this;
                    zenRulePreference.mContext.startActivity(zenRulePreference.mIntent);
                }
            });
        } else {
            findViewById2.setVisibility(8);
            findViewById.setVisibility(8);
            findViewById.setOnClickListener((View.OnClickListener) null);
        }
        View findViewById3 = preferenceViewHolder.findViewById(C1715R.C1718id.checkbox_container);
        if (findViewById3 != null) {
            findViewById3.setOnClickListener(this.mOnCheckBoxClickListener);
        }
        this.mCheckBox = (CheckBox) preferenceViewHolder.findViewById(16908289);
        CheckBox checkBox = this.mCheckBox;
        if (checkBox != null) {
            checkBox.setChecked(this.mChecked);
        }
    }

    public void updatePreference(AutomaticZenRule automaticZenRule) {
        if (!this.mRule.getName().equals(automaticZenRule.getName())) {
            this.mName = automaticZenRule.getName();
            setTitle(this.mName);
        }
        if (this.mRule.isEnabled() != automaticZenRule.isEnabled()) {
            setChecked(this.mRule.isEnabled());
            setSummary((CharSequence) computeRuleSummary(this.mRule));
        }
        this.mRule = automaticZenRule;
    }

    public void onClick() {
        this.mOnCheckBoxClickListener.onClick((View) null);
    }

    /* access modifiers changed from: private */
    public void setChecked(boolean z) {
        this.mChecked = z;
        CheckBox checkBox = this.mCheckBox;
        if (checkBox != null) {
            checkBox.setChecked(z);
        }
    }

    /* access modifiers changed from: protected */
    public void setAttributes(AutomaticZenRule automaticZenRule) {
        boolean isValidScheduleConditionId = ZenModeConfig.isValidScheduleConditionId(automaticZenRule.getConditionId(), true);
        boolean isValidEventConditionId = ZenModeConfig.isValidEventConditionId(automaticZenRule.getConditionId());
        setSummary((CharSequence) computeRuleSummary(automaticZenRule));
        setTitle(this.mName);
        setPersistent(false);
        this.mIntent = AbstractZenModeAutomaticRulePreferenceController.getRuleIntent(isValidScheduleConditionId ? "android.settings.ZEN_MODE_SCHEDULE_RULE_SETTINGS" : isValidEventConditionId ? "android.settings.ZEN_MODE_EVENT_RULE_SETTINGS" : "", AbstractZenModeAutomaticRulePreferenceController.getSettingsActivity(automaticZenRule, this.mServiceListing.findService(automaticZenRule.getOwner())), this.mId);
        if (this.mIntent.resolveActivity(this.mPm) == null) {
            this.mIntent = null;
        }
        setKey(this.mId);
    }

    private String computeRuleSummary(AutomaticZenRule automaticZenRule) {
        if (automaticZenRule == null || !automaticZenRule.isEnabled()) {
            return this.mContext.getResources().getString(C1715R.string.switch_off_text);
        }
        return this.mContext.getResources().getString(C1715R.string.switch_on_text);
    }
}
