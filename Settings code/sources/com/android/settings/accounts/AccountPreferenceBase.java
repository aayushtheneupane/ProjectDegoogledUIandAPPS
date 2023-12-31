package com.android.settings.accounts;

import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import androidx.fragment.app.FragmentActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.utils.ThreadUtils;
import java.text.DateFormat;

abstract class AccountPreferenceBase extends SettingsPreferenceFragment implements AuthenticatorHelper.OnAccountsUpdateListener {
    protected static final boolean VERBOSE = Log.isLoggable("AccountPreferenceBase", 2);
    protected AccountTypePreferenceLoader mAccountTypePreferenceLoader;
    protected AuthenticatorHelper mAuthenticatorHelper;
    private DateFormat mDateFormat;
    private Object mStatusChangeListenerHandle;
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public final void onStatusChanged(int i) {
            AccountPreferenceBase.this.lambda$new$1$AccountPreferenceBase(i);
        }
    };
    private DateFormat mTimeFormat;
    private UserManager mUm;
    protected UserHandle mUserHandle;

    public void onAccountsUpdate(UserHandle userHandle) {
    }

    /* access modifiers changed from: protected */
    public void onAuthDescriptionsUpdated() {
    }

    /* access modifiers changed from: protected */
    /* renamed from: onSyncStateUpdated */
    public void lambda$new$0$AccountPreferenceBase() {
    }

    AccountPreferenceBase() {
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mUm = (UserManager) getSystemService("user");
        FragmentActivity activity = getActivity();
        this.mUserHandle = Utils.getSecureTargetUser(activity.getActivityToken(), this.mUm, getArguments(), activity.getIntent().getExtras());
        this.mAuthenticatorHelper = new AuthenticatorHelper(activity, this.mUserHandle, this);
        this.mAccountTypePreferenceLoader = new AccountTypePreferenceLoader(this, this.mAuthenticatorHelper, this.mUserHandle);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        FragmentActivity activity = getActivity();
        this.mDateFormat = android.text.format.DateFormat.getDateFormat(activity);
        this.mTimeFormat = android.text.format.DateFormat.getTimeFormat(activity);
    }

    public void onResume() {
        super.onResume();
        this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(13, this.mSyncStatusObserver);
        lambda$new$0$AccountPreferenceBase();
    }

    public void onPause() {
        super.onPause();
        ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
    }

    public /* synthetic */ void lambda$new$1$AccountPreferenceBase(int i) {
        ThreadUtils.postOnMainThread(new Runnable() {
            public final void run() {
                AccountPreferenceBase.this.lambda$new$0$AccountPreferenceBase();
            }
        });
    }

    public void updateAuthDescriptions() {
        this.mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        onAuthDescriptionsUpdated();
    }

    /* access modifiers changed from: protected */
    public Drawable getDrawableForType(String str) {
        return this.mAuthenticatorHelper.getDrawableForType(getActivity(), str);
    }

    /* access modifiers changed from: protected */
    public CharSequence getLabelForType(String str) {
        return this.mAuthenticatorHelper.getLabelForType(getActivity(), str);
    }
}
