package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.BidiFormatter;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import com.android.settings.AccessiblePreferenceCategory;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AccountPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, AuthenticatorHelper.OnAccountsUpdateListener, Preference.OnPreferenceClickListener, LifecycleObserver, OnPause, OnResume {
    private int mAccountProfileOrder;
    private String[] mAuthorities;
    private int mAuthoritiesCount;
    private AccountRestrictionHelper mHelper;
    private ManagedProfileBroadcastReceiver mManagedProfileBroadcastReceiver;
    private MetricsFeatureProvider mMetricsFeatureProvider;
    private SettingsPreferenceFragment mParent;
    private Preference mProfileNotAvailablePreference;
    private SparseArray<ProfileData> mProfiles;
    private UserManager mUm;

    public static class ProfileData {
        public ArrayMap<String, AccountTypePreference> accountPreferences = new ArrayMap<>();
        public RestrictedPreference addAccountPreference;
        public AuthenticatorHelper authenticatorHelper;
        public Preference managedProfilePreference;
        public boolean pendingRemoval;
        public PreferenceGroup preferenceGroup;
        public RestrictedPreference removeWorkProfilePreference;
        public UserInfo userInfo;
    }

    public String getPreferenceKey() {
        return null;
    }

    public AccountPreferenceController(Context context, SettingsPreferenceFragment settingsPreferenceFragment, String[] strArr) {
        this(context, settingsPreferenceFragment, strArr, new AccountRestrictionHelper(context));
    }

    AccountPreferenceController(Context context, SettingsPreferenceFragment settingsPreferenceFragment, String[] strArr, AccountRestrictionHelper accountRestrictionHelper) {
        super(context);
        this.mProfiles = new SparseArray<>();
        this.mManagedProfileBroadcastReceiver = new ManagedProfileBroadcastReceiver();
        this.mAuthoritiesCount = 0;
        this.mAccountProfileOrder = 1;
        this.mUm = (UserManager) context.getSystemService("user");
        this.mAuthorities = strArr;
        this.mParent = settingsPreferenceFragment;
        String[] strArr2 = this.mAuthorities;
        if (strArr2 != null) {
            this.mAuthoritiesCount = strArr2.length;
        }
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(this.mContext).getMetricsFeatureProvider();
        this.mHelper = accountRestrictionHelper;
    }

    public boolean isAvailable() {
        return !this.mUm.isManagedProfile();
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        updateUi();
    }

    public void onResume() {
        updateUi();
        this.mManagedProfileBroadcastReceiver.register(this.mContext);
        listenToAccountUpdates();
    }

    public void onPause() {
        stopListeningToAccountUpdates();
        this.mManagedProfileBroadcastReceiver.unregister(this.mContext);
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        ProfileData profileData = this.mProfiles.get(userHandle.getIdentifier());
        if (profileData != null) {
            updateAccountTypes(profileData);
            return;
        }
        Log.w("AccountPrefController", "Missing Settings screen for: " + userHandle.getIdentifier());
    }

    public boolean onPreferenceClick(Preference preference) {
        int size = this.mProfiles.size();
        int i = 0;
        while (i < size) {
            ProfileData valueAt = this.mProfiles.valueAt(i);
            if (preference == valueAt.addAccountPreference) {
                Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
                intent.putExtra("android.intent.extra.USER", valueAt.userInfo.getUserHandle());
                intent.putExtra("authorities", this.mAuthorities);
                this.mContext.startActivity(intent);
                return true;
            } else if (preference == valueAt.removeWorkProfilePreference) {
                RemoveUserFragment.newInstance(valueAt.userInfo.id).show(this.mParent.getFragmentManager(), "removeUser");
                return true;
            } else if (preference == valueAt.managedProfilePreference) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("android.intent.extra.USER", valueAt.userInfo.getUserHandle());
                new SubSettingLauncher(this.mContext).setSourceMetricsCategory(this.mParent.getMetricsCategory()).setDestination(ManagedProfileSettings.class.getName()).setTitleRes(C1715R.string.managed_profile_settings_title).setArguments(bundle).launch();
                return true;
            } else {
                i++;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updateUi() {
        if (!isAvailable()) {
            Log.e("AccountPrefController", "We should not be showing settings for a managed profile");
            return;
        }
        int size = this.mProfiles.size();
        for (int i = 0; i < size; i++) {
            this.mProfiles.valueAt(i).pendingRemoval = true;
        }
        if (this.mUm.isRestrictedProfile()) {
            updateProfileUi(this.mUm.getUserInfo(UserHandle.myUserId()));
        } else {
            List profiles = this.mUm.getProfiles(UserHandle.myUserId());
            int size2 = profiles.size();
            for (int i2 = 0; i2 < size2; i2++) {
                updateProfileUi((UserInfo) profiles.get(i2));
            }
        }
        cleanUpPreferences();
        int size3 = this.mProfiles.size();
        for (int i3 = 0; i3 < size3; i3++) {
            updateAccountTypes(this.mProfiles.valueAt(i3));
        }
    }

    private void updateProfileUi(UserInfo userInfo) {
        if (this.mParent.getPreferenceManager() != null) {
            ProfileData profileData = this.mProfiles.get(userInfo.id);
            if (profileData != null) {
                profileData.pendingRemoval = false;
                profileData.userInfo = userInfo;
                if (userInfo.isEnabled()) {
                    profileData.authenticatorHelper = new AuthenticatorHelper(this.mContext, userInfo.getUserHandle(), this);
                    return;
                }
                return;
            }
            Context context = this.mContext;
            ProfileData profileData2 = new ProfileData();
            profileData2.userInfo = userInfo;
            AccessiblePreferenceCategory createAccessiblePreferenceCategory = this.mHelper.createAccessiblePreferenceCategory(this.mParent.getPreferenceManager().getContext());
            int i = this.mAccountProfileOrder;
            this.mAccountProfileOrder = i + 1;
            createAccessiblePreferenceCategory.setOrder(i);
            if (isSingleProfile()) {
                createAccessiblePreferenceCategory.setTitle((CharSequence) context.getString(C1715R.string.account_for_section_header, new Object[]{BidiFormatter.getInstance().unicodeWrap(userInfo.name)}));
                createAccessiblePreferenceCategory.setContentDescription(this.mContext.getString(C1715R.string.account_settings));
            } else if (userInfo.isManagedProfile()) {
                createAccessiblePreferenceCategory.setTitle((int) C1715R.string.category_work);
                String workGroupSummary = getWorkGroupSummary(context, userInfo);
                createAccessiblePreferenceCategory.setSummary((CharSequence) workGroupSummary);
                createAccessiblePreferenceCategory.setContentDescription(this.mContext.getString(C1715R.string.accessibility_category_work, new Object[]{workGroupSummary}));
                profileData2.removeWorkProfilePreference = newRemoveWorkProfilePreference();
                this.mHelper.enforceRestrictionOnPreference(profileData2.removeWorkProfilePreference, "no_remove_managed_profile", UserHandle.myUserId());
                profileData2.managedProfilePreference = newManagedProfileSettings();
            } else {
                createAccessiblePreferenceCategory.setTitle((int) C1715R.string.category_personal);
                createAccessiblePreferenceCategory.setContentDescription(this.mContext.getString(C1715R.string.accessibility_category_personal));
            }
            PreferenceScreen preferenceScreen = this.mParent.getPreferenceScreen();
            if (preferenceScreen != null) {
                preferenceScreen.addPreference(createAccessiblePreferenceCategory);
            }
            profileData2.preferenceGroup = createAccessiblePreferenceCategory;
            if (userInfo.isEnabled()) {
                profileData2.authenticatorHelper = new AuthenticatorHelper(context, userInfo.getUserHandle(), this);
                profileData2.addAccountPreference = newAddAccountPreference();
                this.mHelper.enforceRestrictionOnPreference(profileData2.addAccountPreference, "no_modify_accounts", userInfo.id);
            }
            this.mProfiles.put(userInfo.id, profileData2);
        }
    }

    private RestrictedPreference newAddAccountPreference() {
        RestrictedPreference restrictedPreference = new RestrictedPreference(this.mParent.getPreferenceManager().getContext());
        restrictedPreference.setTitle((int) C1715R.string.add_account_label);
        restrictedPreference.setIcon((int) C1715R.C1717drawable.ic_add_24dp);
        restrictedPreference.setOnPreferenceClickListener(this);
        restrictedPreference.setOrder(1000);
        return restrictedPreference;
    }

    private RestrictedPreference newRemoveWorkProfilePreference() {
        RestrictedPreference restrictedPreference = new RestrictedPreference(this.mParent.getPreferenceManager().getContext());
        restrictedPreference.setTitle((int) C1715R.string.remove_managed_profile_label);
        restrictedPreference.setIcon((int) C1715R.C1717drawable.ic_delete);
        restrictedPreference.setOnPreferenceClickListener(this);
        restrictedPreference.setOrder(1002);
        return restrictedPreference;
    }

    private Preference newManagedProfileSettings() {
        Preference preference = new Preference(this.mParent.getPreferenceManager().getContext());
        preference.setTitle((int) C1715R.string.managed_profile_settings_title);
        preference.setIcon((int) C1715R.C1717drawable.ic_settings_24dp);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(1001);
        return preference;
    }

    private String getWorkGroupSummary(Context context, UserInfo userInfo) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo adminApplicationInfo = Utils.getAdminApplicationInfo(context, userInfo.id);
        if (adminApplicationInfo == null) {
            return null;
        }
        CharSequence applicationLabel = packageManager.getApplicationLabel(adminApplicationInfo);
        return this.mContext.getString(C1715R.string.managing_admin, new Object[]{applicationLabel});
    }

    /* access modifiers changed from: package-private */
    public void cleanUpPreferences() {
        PreferenceScreen preferenceScreen = this.mParent.getPreferenceScreen();
        if (preferenceScreen != null) {
            for (int size = this.mProfiles.size() - 1; size >= 0; size--) {
                ProfileData valueAt = this.mProfiles.valueAt(size);
                if (valueAt.pendingRemoval) {
                    preferenceScreen.removePreference(valueAt.preferenceGroup);
                    this.mProfiles.removeAt(size);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void listenToAccountUpdates() {
        int size = this.mProfiles.size();
        for (int i = 0; i < size; i++) {
            AuthenticatorHelper authenticatorHelper = this.mProfiles.valueAt(i).authenticatorHelper;
            if (authenticatorHelper != null) {
                authenticatorHelper.listenToAccountUpdates();
            }
        }
    }

    /* access modifiers changed from: private */
    public void stopListeningToAccountUpdates() {
        int size = this.mProfiles.size();
        for (int i = 0; i < size; i++) {
            AuthenticatorHelper authenticatorHelper = this.mProfiles.valueAt(i).authenticatorHelper;
            if (authenticatorHelper != null) {
                authenticatorHelper.stopListeningToAccountUpdates();
            }
        }
    }

    private void updateAccountTypes(ProfileData profileData) {
        if (this.mParent.getPreferenceManager() != null && profileData.preferenceGroup.getPreferenceManager() != null) {
            if (profileData.userInfo.isEnabled()) {
                ArrayMap arrayMap = new ArrayMap(profileData.accountPreferences);
                ArrayList<AccountTypePreference> accountTypePreferences = getAccountTypePreferences(profileData.authenticatorHelper, profileData.userInfo.getUserHandle(), arrayMap);
                int size = accountTypePreferences.size();
                for (int i = 0; i < size; i++) {
                    AccountTypePreference accountTypePreference = accountTypePreferences.get(i);
                    accountTypePreference.setOrder(i);
                    String key = accountTypePreference.getKey();
                    if (!profileData.accountPreferences.containsKey(key)) {
                        profileData.preferenceGroup.addPreference(accountTypePreference);
                        profileData.accountPreferences.put(key, accountTypePreference);
                    }
                }
                RestrictedPreference restrictedPreference = profileData.addAccountPreference;
                if (restrictedPreference != null) {
                    profileData.preferenceGroup.addPreference(restrictedPreference);
                }
                for (String str : arrayMap.keySet()) {
                    profileData.preferenceGroup.removePreference(profileData.accountPreferences.get(str));
                    profileData.accountPreferences.remove(str);
                }
            } else {
                profileData.preferenceGroup.removeAll();
                if (this.mProfileNotAvailablePreference == null) {
                    this.mProfileNotAvailablePreference = new Preference(this.mParent.getPreferenceManager().getContext());
                }
                this.mProfileNotAvailablePreference.setEnabled(false);
                this.mProfileNotAvailablePreference.setIcon((int) C1715R.C1717drawable.empty_icon);
                this.mProfileNotAvailablePreference.setTitle((CharSequence) null);
                this.mProfileNotAvailablePreference.setSummary((int) C1715R.string.managed_profile_not_available_label);
                profileData.preferenceGroup.addPreference(this.mProfileNotAvailablePreference);
            }
            RestrictedPreference restrictedPreference2 = profileData.removeWorkProfilePreference;
            if (restrictedPreference2 != null) {
                profileData.preferenceGroup.addPreference(restrictedPreference2);
            }
            Preference preference = profileData.managedProfilePreference;
            if (preference != null) {
                profileData.preferenceGroup.addPreference(preference);
            }
        }
    }

    private ArrayList<AccountTypePreference> getAccountTypePreferences(AuthenticatorHelper authenticatorHelper, UserHandle userHandle, ArrayMap<String, AccountTypePreference> arrayMap) {
        String[] strArr;
        CharSequence labelForType;
        int i;
        Account[] accountArr;
        int i2;
        String[] strArr2;
        int i3;
        AuthenticatorHelper authenticatorHelper2 = authenticatorHelper;
        UserHandle userHandle2 = userHandle;
        String[] enabledAccountTypes = authenticatorHelper.getEnabledAccountTypes();
        ArrayList<AccountTypePreference> arrayList = new ArrayList<>(enabledAccountTypes.length);
        int i4 = 0;
        while (i4 < enabledAccountTypes.length) {
            String str = enabledAccountTypes[i4];
            if (accountTypeHasAnyRequestedAuthorities(authenticatorHelper2, str) && (labelForType = authenticatorHelper2.getLabelForType(this.mContext, str)) != null) {
                String packageForType = authenticatorHelper2.getPackageForType(str);
                int labelIdForType = authenticatorHelper2.getLabelIdForType(str);
                Account[] accountsByTypeAsUser = AccountManager.get(this.mContext).getAccountsByTypeAsUser(str, userHandle2);
                Drawable drawableForType = authenticatorHelper2.getDrawableForType(this.mContext, str);
                Context context = this.mParent.getPreferenceManager().getContext();
                int length = accountsByTypeAsUser.length;
                int i5 = 0;
                while (i5 < length) {
                    Account account = accountsByTypeAsUser[i5];
                    AccountTypePreference remove = arrayMap.remove(AccountTypePreference.buildKey(account));
                    if (remove != null) {
                        arrayList.add(remove);
                    } else {
                        if (AccountRestrictionHelper.showAccount(this.mAuthorities, authenticatorHelper2.getAuthoritiesForAccountType(account.type))) {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("account", account);
                            bundle.putParcelable("user_handle", userHandle2);
                            bundle.putString("account_type", str);
                            strArr2 = enabledAccountTypes;
                            bundle.putString("account_label", labelForType.toString());
                            bundle.putInt("account_title_res", labelIdForType);
                            bundle.putParcelable("android.intent.extra.USER", userHandle2);
                            Account account2 = account;
                            int metricsCategory = this.mMetricsFeatureProvider.getMetricsCategory(this.mParent);
                            i3 = i5;
                            i2 = length;
                            accountArr = accountsByTypeAsUser;
                            i = labelIdForType;
                            arrayList.add(new AccountTypePreference(context, metricsCategory, account2, packageForType, labelIdForType, labelForType, AccountDetailDashboardFragment.class.getName(), bundle, drawableForType));
                            i5 = i3 + 1;
                            userHandle2 = userHandle;
                            enabledAccountTypes = strArr2;
                            length = i2;
                            accountsByTypeAsUser = accountArr;
                            labelIdForType = i;
                        }
                    }
                    strArr2 = enabledAccountTypes;
                    i3 = i5;
                    i2 = length;
                    accountArr = accountsByTypeAsUser;
                    i = labelIdForType;
                    i5 = i3 + 1;
                    userHandle2 = userHandle;
                    enabledAccountTypes = strArr2;
                    length = i2;
                    accountsByTypeAsUser = accountArr;
                    labelIdForType = i;
                }
                strArr = enabledAccountTypes;
                authenticatorHelper2.preloadDrawableForType(this.mContext, str);
            } else {
                strArr = enabledAccountTypes;
            }
            i4++;
            userHandle2 = userHandle;
            enabledAccountTypes = strArr;
        }
        Collections.sort(arrayList, new Comparator<AccountTypePreference>() {
            public int compare(AccountTypePreference accountTypePreference, AccountTypePreference accountTypePreference2) {
                int compareTo = accountTypePreference.getSummary().toString().compareTo(accountTypePreference2.getSummary().toString());
                return compareTo != 0 ? compareTo : accountTypePreference.getTitle().toString().compareTo(accountTypePreference2.getTitle().toString());
            }
        });
        return arrayList;
    }

    private boolean accountTypeHasAnyRequestedAuthorities(AuthenticatorHelper authenticatorHelper, String str) {
        if (this.mAuthoritiesCount == 0) {
            return true;
        }
        ArrayList<String> authoritiesForAccountType = authenticatorHelper.getAuthoritiesForAccountType(str);
        if (authoritiesForAccountType == null) {
            Log.d("AccountPrefController", "No sync authorities for account type: " + str);
            return false;
        }
        for (int i = 0; i < this.mAuthoritiesCount; i++) {
            if (authoritiesForAccountType.contains(this.mAuthorities[i])) {
                return true;
            }
        }
        return false;
    }

    private boolean isSingleProfile() {
        return this.mUm.isLinkedUser() || this.mUm.getProfiles(UserHandle.myUserId()).size() == 1;
    }

    private class ManagedProfileBroadcastReceiver extends BroadcastReceiver {
        private boolean mListeningToManagedProfileEvents;

        private ManagedProfileBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("AccountPrefController", "Received broadcast: " + action);
            if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED") || action.equals("android.intent.action.MANAGED_PROFILE_ADDED")) {
                AccountPreferenceController.this.stopListeningToAccountUpdates();
                AccountPreferenceController.this.updateUi();
                AccountPreferenceController.this.listenToAccountUpdates();
                return;
            }
            Log.w("AccountPrefController", "Cannot handle received broadcast: " + intent.getAction());
        }

        public void register(Context context) {
            if (!this.mListeningToManagedProfileEvents) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
                intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
                context.registerReceiver(this, intentFilter);
                this.mListeningToManagedProfileEvents = true;
            }
        }

        public void unregister(Context context) {
            if (this.mListeningToManagedProfileEvents) {
                context.unregisterReceiver(this);
                this.mListeningToManagedProfileEvents = false;
            }
        }
    }
}
