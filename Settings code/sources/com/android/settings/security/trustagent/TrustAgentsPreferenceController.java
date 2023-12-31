package com.android.settings.security.trustagent;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.security.SecurityFeatureProvider;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.havoc.support.preferences.SwitchPreference;
import java.util.List;

public class TrustAgentsPreferenceController extends BasePreferenceController implements Preference.OnPreferenceChangeListener, LifecycleObserver, OnStart {
    private static final Intent TRUST_AGENT_INTENT = new Intent("android.service.trust.TrustAgentService");
    private final ArraySet<ComponentName> mActiveAgents = new ArraySet<>();
    private final ArrayMap<ComponentName, TrustAgentInfo> mAvailableAgents = new ArrayMap<>();
    private final DevicePolicyManager mDevicePolicyManager;
    private final IconDrawableFactory mIconDrawableFactory;
    private final LockPatternUtils mLockPatternUtils;
    private final PackageManager mPackageManager;
    private PreferenceScreen mScreen;
    private final TrustAgentManager mTrustAgentManager;

    public int getAvailabilityStatus() {
        return 0;
    }

    public TrustAgentsPreferenceController(Context context, String str) {
        super(context, str);
        this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class);
        this.mIconDrawableFactory = IconDrawableFactory.newInstance(context);
        SecurityFeatureProvider securityFeatureProvider = FeatureFactory.getFactory(context).getSecurityFeatureProvider();
        this.mTrustAgentManager = securityFeatureProvider.getTrustAgentManager();
        this.mLockPatternUtils = securityFeatureProvider.getLockPatternUtils(context);
        this.mPackageManager = context.getPackageManager();
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mScreen = preferenceScreen;
    }

    public void onStart() {
        updateAgents();
    }

    private void updateAgents() {
        findAvailableTrustAgents();
        loadActiveAgents();
        removeUselessExistingPreferences();
        RestrictedLockUtils.EnforcedAdmin checkIfKeyguardFeaturesDisabled = RestrictedLockUtilsInternal.checkIfKeyguardFeaturesDisabled(this.mContext, 16, UserHandle.myUserId());
        for (TrustAgentInfo next : this.mAvailableAgents.values()) {
            ComponentName componentName = next.getComponentName();
            RestrictedSwitchPreference restrictedSwitchPreference = (RestrictedSwitchPreference) this.mScreen.findPreference(componentName.flattenToString());
            if (restrictedSwitchPreference == null) {
                restrictedSwitchPreference = new RestrictedSwitchPreference(this.mScreen.getContext());
            }
            restrictedSwitchPreference.setKey(componentName.flattenToString());
            restrictedSwitchPreference.useAdminDisabledSummary(true);
            restrictedSwitchPreference.setTitle(next.getLabel());
            restrictedSwitchPreference.setIcon(next.getIcon());
            restrictedSwitchPreference.setOnPreferenceChangeListener(this);
            restrictedSwitchPreference.setChecked(this.mActiveAgents.contains(componentName));
            if (checkIfKeyguardFeaturesDisabled != null && this.mDevicePolicyManager.getTrustAgentConfiguration((ComponentName) null, componentName) == null) {
                restrictedSwitchPreference.setChecked(false);
                restrictedSwitchPreference.setDisabledByAdmin(checkIfKeyguardFeaturesDisabled);
            }
            this.mScreen.addPreference(restrictedSwitchPreference);
        }
    }

    private void loadActiveAgents() {
        List enabledTrustAgents = this.mLockPatternUtils.getEnabledTrustAgents(UserHandle.myUserId());
        if (enabledTrustAgents != null) {
            this.mActiveAgents.addAll(enabledTrustAgents);
        }
    }

    private void saveActiveAgents() {
        this.mLockPatternUtils.setEnabledTrustAgents(this.mActiveAgents, UserHandle.myUserId());
    }

    private void findAvailableTrustAgents() {
        List<ResolveInfo> queryIntentServices = this.mPackageManager.queryIntentServices(TRUST_AGENT_INTENT, 128);
        this.mAvailableAgents.clear();
        for (ResolveInfo next : queryIntentServices) {
            if (next.serviceInfo != null && this.mTrustAgentManager.shouldProvideTrust(next, this.mPackageManager)) {
                CharSequence loadLabel = next.loadLabel(this.mPackageManager);
                ComponentName componentName = this.mTrustAgentManager.getComponentName(next);
                this.mAvailableAgents.put(componentName, new TrustAgentInfo(loadLabel, componentName, this.mIconDrawableFactory.getBadgedIcon(next.getComponentInfo().applicationInfo)));
            }
        }
    }

    private void removeUselessExistingPreferences() {
        int preferenceCount = this.mScreen.getPreferenceCount();
        if (preferenceCount > 0) {
            for (int i = preferenceCount - 1; i >= 0; i--) {
                Preference preference = this.mScreen.getPreference(i);
                String[] split = TextUtils.split(preference.getKey(), "/");
                ComponentName componentName = new ComponentName(split[0], split[1]);
                if (!this.mAvailableAgents.containsKey(componentName)) {
                    this.mScreen.removePreference(preference);
                    this.mActiveAgents.remove(componentName);
                }
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (!(preference instanceof SwitchPreference)) {
            return false;
        }
        for (TrustAgentInfo componentName : this.mAvailableAgents.values()) {
            ComponentName componentName2 = componentName.getComponentName();
            if (TextUtils.equals(preference.getKey(), componentName2.flattenToString())) {
                if (!((Boolean) obj).booleanValue() || this.mActiveAgents.contains(componentName2)) {
                    this.mActiveAgents.remove(componentName2);
                } else {
                    this.mActiveAgents.add(componentName2);
                }
                saveActiveAgents();
                return true;
            }
        }
        return false;
    }
}
