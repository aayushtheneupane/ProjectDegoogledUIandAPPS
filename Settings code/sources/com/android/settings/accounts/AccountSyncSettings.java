package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncStatusInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import com.android.settings.Utils;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.widget.LayoutPreference;
import com.google.android.collect.Lists;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class AccountSyncSettings extends AccountPreferenceBase {
    private Account mAccount;
    private ArrayList<SyncAdapterType> mInvisibleAdapters = Lists.newArrayList();

    public int getDialogMetricsCategory(int i) {
        return i != 102 ? 0 : 587;
    }

    public int getHelpResource() {
        return C1715R.string.help_url_accounts;
    }

    public int getMetricsCategory() {
        return 9;
    }

    public /* bridge */ /* synthetic */ void updateAuthDescriptions() {
        super.updateAuthDescriptions();
    }

    public Dialog onCreateDialog(int i) {
        if (i != 102) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle((int) C1715R.string.cant_sync_dialog_title);
        builder.setMessage((int) C1715R.string.cant_sync_dialog_message);
        builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        return builder.create();
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(C1715R.xml.account_sync_settings);
        getPreferenceScreen().setOrderingAsAdded(false);
        setAccessibilityTitle();
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Bundle arguments = getArguments();
        if (arguments == null) {
            Log.e("AccountPreferenceBase", "No arguments provided when starting intent. ACCOUNT_KEY needed.");
            finish();
            return;
        }
        this.mAccount = (Account) arguments.getParcelable("account");
        if (!accountExists(this.mAccount)) {
            Log.e("AccountPreferenceBase", "Account provided does not exist: " + this.mAccount);
            finish();
            return;
        }
        if (Log.isLoggable("AccountPreferenceBase", 2)) {
            Log.v("AccountPreferenceBase", "Got account: " + this.mAccount);
        }
        FragmentActivity activity = getActivity();
        LayoutPreference done = EntityHeaderController.newInstance(activity, this, (View) null).setRecyclerView(getListView(), getSettingsLifecycle()).setIcon(getDrawableForType(this.mAccount.type)).setLabel((CharSequence) this.mAccount.name).setSummary(getLabelForType(this.mAccount.type)).done((Activity) activity, getPrefContext());
        done.setOrder(0);
        getPreferenceScreen().addPreference(done);
    }

    private void setAccessibilityTitle() {
        UserInfo userInfo = ((UserManager) getSystemService("user")).getUserInfo(this.mUserHandle.getIdentifier());
        boolean isManagedProfile = userInfo != null ? userInfo.isManagedProfile() : false;
        CharSequence title = getActivity().getTitle();
        getActivity().setTitle(Utils.createAccessibleSequence(title, getString(isManagedProfile ? C1715R.string.accessibility_work_account_title : C1715R.string.accessibility_personal_account_title, title)));
    }

    public void onResume() {
        this.mAuthenticatorHelper.listenToAccountUpdates();
        updateAuthDescriptions();
        onAccountsUpdate(Binder.getCallingUserHandle());
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        this.mAuthenticatorHelper.stopListeningToAccountUpdates();
    }

    private void addSyncStateSwitch(Account account, String str, String str2, int i) {
        SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) getCachedPreference(str);
        if (syncStateSwitchPreference == null) {
            syncStateSwitchPreference = new SyncStateSwitchPreference(getPrefContext(), account, str, str2, i);
            getPreferenceScreen().addPreference(syncStateSwitchPreference);
        } else {
            syncStateSwitchPreference.setup(account, str, str2, i);
        }
        PackageManager packageManager = getPackageManager();
        syncStateSwitchPreference.setPersistent(false);
        ProviderInfo resolveContentProviderAsUser = packageManager.resolveContentProviderAsUser(str, 0, this.mUserHandle.getIdentifier());
        if (resolveContentProviderAsUser != null) {
            CharSequence loadLabel = resolveContentProviderAsUser.loadLabel(packageManager);
            if (TextUtils.isEmpty(loadLabel)) {
                Log.e("AccountPreferenceBase", "Provider needs a label for authority '" + str + "'");
                return;
            }
            syncStateSwitchPreference.setTitle(loadLabel);
            syncStateSwitchPreference.setKey(str);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        MenuItem icon = menu.add(0, 1, 0, getString(C1715R.string.sync_menu_sync_now)).setIcon(C1715R.C1717drawable.ic_menu_refresh_holo_dark);
        MenuItem icon2 = menu.add(0, 2, 0, getString(C1715R.string.sync_menu_sync_cancel)).setIcon(17301560);
        icon.setShowAsAction(4);
        icon2.setShowAsAction(4);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean z = true;
        boolean z2 = !ContentResolver.getCurrentSyncsAsUser(this.mUserHandle.getIdentifier()).isEmpty();
        MenuItem findItem = menu.findItem(1);
        if (z2) {
            z = false;
        }
        findItem.setVisible(z);
        menu.findItem(2).setVisible(z2);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == 1) {
            startSyncForEnabledProviders();
            return true;
        } else if (itemId != 2) {
            return super.onOptionsItemSelected(menuItem);
        } else {
            cancelSyncForEnabledProviders();
            return true;
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == -1) {
            int preferenceCount = getPreferenceScreen().getPreferenceCount();
            for (int i3 = 0; i3 < preferenceCount; i3++) {
                Preference preference = getPreferenceScreen().getPreference(i3);
                if (preference instanceof SyncStateSwitchPreference) {
                    SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) preference;
                    if (syncStateSwitchPreference.getUid() == i) {
                        onPreferenceTreeClick(syncStateSwitchPreference);
                        return;
                    }
                }
            }
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (getActivity() == null) {
            return false;
        }
        if (!(preference instanceof SyncStateSwitchPreference)) {
            return super.onPreferenceTreeClick(preference);
        }
        SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) preference;
        String authority = syncStateSwitchPreference.getAuthority();
        if (TextUtils.isEmpty(authority)) {
            return false;
        }
        Account account = syncStateSwitchPreference.getAccount();
        int identifier = this.mUserHandle.getIdentifier();
        String packageName = syncStateSwitchPreference.getPackageName();
        boolean syncAutomaticallyAsUser = ContentResolver.getSyncAutomaticallyAsUser(account, authority, identifier);
        if (!syncStateSwitchPreference.isOneTimeSyncMode()) {
            boolean isChecked = syncStateSwitchPreference.isChecked();
            if (isChecked == syncAutomaticallyAsUser || (isChecked && requestAccountAccessIfNeeded(packageName))) {
                return true;
            }
            ContentResolver.setSyncAutomaticallyAsUser(account, authority, isChecked, identifier);
            if (!ContentResolver.getMasterSyncAutomaticallyAsUser(identifier) || !isChecked) {
                requestOrCancelSync(account, authority, isChecked);
            }
        } else if (requestAccountAccessIfNeeded(packageName)) {
            return true;
        } else {
            requestOrCancelSync(account, authority, true);
        }
        return true;
    }

    private boolean requestAccountAccessIfNeeded(String str) {
        IntentSender createRequestAccountAccessIntentSenderAsUser;
        if (str == null) {
            return false;
        }
        try {
            int packageUidAsUser = getContext().getPackageManager().getPackageUidAsUser(str, this.mUserHandle.getIdentifier());
            AccountManager accountManager = (AccountManager) getContext().getSystemService(AccountManager.class);
            if (!accountManager.hasAccountAccess(this.mAccount, str, this.mUserHandle) && (createRequestAccountAccessIntentSenderAsUser = accountManager.createRequestAccountAccessIntentSenderAsUser(this.mAccount, str, this.mUserHandle)) != null) {
                try {
                    startIntentSenderForResult(createRequestAccountAccessIntentSenderAsUser, packageUidAsUser, (Intent) null, 0, 0, 0, (Bundle) null);
                    return true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e("AccountPreferenceBase", "Error requesting account access", e);
                }
            }
            return false;
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e("AccountPreferenceBase", "Invalid sync ", e2);
            return false;
        }
    }

    private void startSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(true);
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void cancelSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(false);
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void requestOrCancelSyncForEnabledProviders(boolean z) {
        int preferenceCount = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof SyncStateSwitchPreference) {
                SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) preference;
                if (syncStateSwitchPreference.isChecked()) {
                    requestOrCancelSync(syncStateSwitchPreference.getAccount(), syncStateSwitchPreference.getAuthority(), z);
                }
            }
        }
        if (this.mAccount != null) {
            Iterator<SyncAdapterType> it = this.mInvisibleAdapters.iterator();
            while (it.hasNext()) {
                requestOrCancelSync(this.mAccount, it.next().authority, z);
            }
        }
    }

    private void requestOrCancelSync(Account account, String str, boolean z) {
        if (z) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("force", true);
            ContentResolver.requestSyncAsUser(account, str, this.mUserHandle.getIdentifier(), bundle);
            return;
        }
        ContentResolver.cancelSyncAsUser(account, str, this.mUserHandle.getIdentifier());
    }

    private boolean isSyncing(List<SyncInfo> list, Account account, String str) {
        for (SyncInfo next : list) {
            if (next.account.equals(account) && next.authority.equals(str)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onSyncStateUpdated() {
        if (isResumed()) {
            setFeedsState();
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
    }

    private void setFeedsState() {
        int i;
        List list;
        int i2;
        Date date;
        boolean z;
        boolean z2;
        long j;
        boolean z3;
        Date date2 = new Date();
        int identifier = this.mUserHandle.getIdentifier();
        List currentSyncsAsUser = ContentResolver.getCurrentSyncsAsUser(identifier);
        updateAccountSwitches();
        int preferenceCount = getPreferenceScreen().getPreferenceCount();
        int i3 = 0;
        boolean z4 = false;
        while (i3 < preferenceCount) {
            Preference preference = getPreferenceScreen().getPreference(i3);
            if (!(preference instanceof SyncStateSwitchPreference)) {
                date = date2;
                list = currentSyncsAsUser;
                i = preferenceCount;
                i2 = i3;
            } else {
                SyncStateSwitchPreference syncStateSwitchPreference = (SyncStateSwitchPreference) preference;
                String authority = syncStateSwitchPreference.getAuthority();
                Account account = syncStateSwitchPreference.getAccount();
                SyncStatusInfo syncStatusAsUser = ContentResolver.getSyncStatusAsUser(account, authority, identifier);
                boolean syncAutomaticallyAsUser = ContentResolver.getSyncAutomaticallyAsUser(account, authority, identifier);
                if (syncStatusAsUser == null) {
                    z = false;
                } else {
                    z = syncStatusAsUser.pending;
                }
                if (syncStatusAsUser == null) {
                    z2 = false;
                } else {
                    z2 = syncStatusAsUser.initialize;
                }
                boolean isSyncing = isSyncing(currentSyncsAsUser, account, authority);
                i2 = i3;
                boolean z5 = (syncStatusAsUser == null || syncStatusAsUser.lastFailureTime == 0 || syncStatusAsUser.getLastFailureMesgAsInt(0) == 1) ? false : true;
                if (!syncAutomaticallyAsUser) {
                    z5 = false;
                }
                if (z5 && !isSyncing && !z) {
                    z4 = true;
                }
                list = currentSyncsAsUser;
                if (Log.isLoggable("AccountPreferenceBase", 3)) {
                    StringBuilder sb = new StringBuilder();
                    i = preferenceCount;
                    sb.append("Update sync status: ");
                    sb.append(account);
                    sb.append(" ");
                    sb.append(authority);
                    sb.append(" active = ");
                    sb.append(isSyncing);
                    sb.append(" pend =");
                    sb.append(z);
                    Log.d("AccountPreferenceBase", sb.toString());
                } else {
                    i = preferenceCount;
                }
                if (syncStatusAsUser == null) {
                    j = 0;
                } else {
                    j = syncStatusAsUser.lastSuccessTime;
                }
                if (!syncAutomaticallyAsUser) {
                    syncStateSwitchPreference.setSummary((int) C1715R.string.sync_disabled);
                } else if (isSyncing) {
                    syncStateSwitchPreference.setSummary((int) C1715R.string.sync_in_progress);
                } else {
                    if (j != 0) {
                        date2.setTime(j);
                        date = date2;
                        z3 = false;
                        syncStateSwitchPreference.setSummary((CharSequence) getResources().getString(C1715R.string.last_synced, new Object[]{formatSyncDate(getContext(), date2)}));
                    } else {
                        date = date2;
                        z3 = false;
                        syncStateSwitchPreference.setSummary((CharSequence) "");
                    }
                    int isSyncableAsUser = ContentResolver.getIsSyncableAsUser(account, authority, identifier);
                    syncStateSwitchPreference.setActive((isSyncing || isSyncableAsUser < 0 || z2) ? z3 : true);
                    syncStateSwitchPreference.setPending((z || isSyncableAsUser < 0 || z2) ? z3 : true);
                    syncStateSwitchPreference.setFailed(z5);
                    boolean z6 = true;
                    boolean z7 = !ContentResolver.getMasterSyncAutomaticallyAsUser(identifier);
                    syncStateSwitchPreference.setOneTimeSyncMode(z7);
                    if (!z7 && !syncAutomaticallyAsUser) {
                        z6 = z3;
                    }
                    syncStateSwitchPreference.setChecked(z6);
                }
                date = date2;
                z3 = false;
                int isSyncableAsUser2 = ContentResolver.getIsSyncableAsUser(account, authority, identifier);
                syncStateSwitchPreference.setActive((isSyncing || isSyncableAsUser2 < 0 || z2) ? z3 : true);
                syncStateSwitchPreference.setPending((z || isSyncableAsUser2 < 0 || z2) ? z3 : true);
                syncStateSwitchPreference.setFailed(z5);
                boolean z62 = true;
                boolean z72 = !ContentResolver.getMasterSyncAutomaticallyAsUser(identifier);
                syncStateSwitchPreference.setOneTimeSyncMode(z72);
                z62 = z3;
                syncStateSwitchPreference.setChecked(z62);
            }
            i3 = i2 + 1;
            date2 = date;
            currentSyncsAsUser = list;
            preferenceCount = i;
        }
        if (z4) {
            this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) C1715R.string.sync_is_failing);
        }
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        super.onAccountsUpdate(userHandle);
        if (!accountExists(this.mAccount)) {
            finish();
            return;
        }
        updateAccountSwitches();
        onSyncStateUpdated();
    }

    private boolean accountExists(Account account) {
        if (account == null) {
            return false;
        }
        for (Account equals : AccountManager.get(getActivity()).getAccountsByTypeAsUser(account.type, this.mUserHandle)) {
            if (equals.equals(account)) {
                return true;
            }
        }
        return false;
    }

    private void updateAccountSwitches() {
        this.mInvisibleAdapters.clear();
        SyncAdapterType[] syncAdapterTypesAsUser = ContentResolver.getSyncAdapterTypesAsUser(this.mUserHandle.getIdentifier());
        ArrayList arrayList = new ArrayList();
        for (SyncAdapterType syncAdapterType : syncAdapterTypesAsUser) {
            if (syncAdapterType.accountType.equals(this.mAccount.type)) {
                if (syncAdapterType.isUserVisible()) {
                    if (Log.isLoggable("AccountPreferenceBase", 3)) {
                        Log.d("AccountPreferenceBase", "updateAccountSwitches: added authority " + syncAdapterType.authority + " to accountType " + syncAdapterType.accountType);
                    }
                    arrayList.add(syncAdapterType);
                } else {
                    this.mInvisibleAdapters.add(syncAdapterType);
                }
            }
        }
        if (Log.isLoggable("AccountPreferenceBase", 3)) {
            Log.d("AccountPreferenceBase", "looking for sync adapters that match account " + this.mAccount);
        }
        cacheRemoveAllPrefs(getPreferenceScreen());
        getCachedPreference("pref_app_header");
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            SyncAdapterType syncAdapterType2 = (SyncAdapterType) arrayList.get(i);
            int isSyncableAsUser = ContentResolver.getIsSyncableAsUser(this.mAccount, syncAdapterType2.authority, this.mUserHandle.getIdentifier());
            if (Log.isLoggable("AccountPreferenceBase", 3)) {
                Log.d("AccountPreferenceBase", "  found authority " + syncAdapterType2.authority + " " + isSyncableAsUser);
            }
            if (isSyncableAsUser > 0) {
                try {
                    addSyncStateSwitch(this.mAccount, syncAdapterType2.authority, syncAdapterType2.getPackageName(), getContext().getPackageManager().getPackageUidAsUser(syncAdapterType2.getPackageName(), this.mUserHandle.getIdentifier()));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("AccountPreferenceBase", "No uid for package" + syncAdapterType2.getPackageName(), e);
                }
            }
        }
        removeCachedPrefs(getPreferenceScreen());
    }

    private static String formatSyncDate(Context context, Date date) {
        return DateUtils.formatDateTime(context, date.getTime(), 21);
    }
}
