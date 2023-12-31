package com.android.settings.accounts;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.widget.LayoutPreference;
import com.havoc.config.center.C1715R;

public class AccountHeaderPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume {
    private final Account mAccount;
    private final Activity mActivity;
    private LayoutPreference mHeaderPreference;
    private final PreferenceFragmentCompat mHost;
    private final UserHandle mUserHandle;

    public String getPreferenceKey() {
        return "account_header";
    }

    public AccountHeaderPreferenceController(Context context, Lifecycle lifecycle, Activity activity, PreferenceFragmentCompat preferenceFragmentCompat, Bundle bundle) {
        super(context);
        this.mActivity = activity;
        this.mHost = preferenceFragmentCompat;
        if (bundle == null || !bundle.containsKey("account")) {
            this.mAccount = null;
        } else {
            this.mAccount = (Account) bundle.getParcelable("account");
        }
        if (bundle == null || !bundle.containsKey("user_handle")) {
            this.mUserHandle = null;
        } else {
            this.mUserHandle = (UserHandle) bundle.getParcelable("user_handle");
        }
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return (this.mAccount == null || this.mUserHandle == null) ? false : true;
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mHeaderPreference = (LayoutPreference) preferenceScreen.findPreference("account_header");
    }

    public void onResume() {
        EntityHeaderController.newInstance(this.mActivity, this.mHost, this.mHeaderPreference.findViewById(C1715R.C1718id.entity_header)).setLabel((CharSequence) this.mAccount.name).setIcon(new AuthenticatorHelper(this.mContext, this.mUserHandle, (AuthenticatorHelper.OnAccountsUpdateListener) null).getDrawableForType(this.mContext, this.mAccount.type)).done(this.mActivity, true);
    }
}
