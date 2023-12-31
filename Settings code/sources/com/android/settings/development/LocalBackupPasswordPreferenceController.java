package com.android.settings.development;

import android.app.backup.IBackupManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.havoc.config.center.C1715R;

public class LocalBackupPasswordPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private final IBackupManager mBackupManager = IBackupManager.Stub.asInterface(ServiceManager.getService("backup"));
    private final UserManager mUserManager;

    public String getPreferenceKey() {
        return "local_backup_password";
    }

    public LocalBackupPasswordPreferenceController(Context context) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public void updateState(Preference preference) {
        updatePasswordSummary(preference);
    }

    private void updatePasswordSummary(Preference preference) {
        preference.setEnabled(isAdminUser() && this.mBackupManager != null);
        IBackupManager iBackupManager = this.mBackupManager;
        if (iBackupManager != null) {
            try {
                if (iBackupManager.hasBackupPassword()) {
                    preference.setSummary((int) C1715R.string.local_backup_password_summary_change);
                } else {
                    preference.setSummary((int) C1715R.string.local_backup_password_summary_none);
                }
            } catch (RemoteException unused) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAdminUser() {
        return this.mUserManager.isAdminUser();
    }
}
