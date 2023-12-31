package com.android.settings.accounts;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.settings.core.TogglePreferenceController;
import java.util.Set;

public class CrossProfileCalendarPreferenceController extends TogglePreferenceController {
    private static final String TAG = "CrossProfileCalendarPreferenceController";
    private UserHandle mManagedUser;

    public CrossProfileCalendarPreferenceController(Context context, String str) {
        super(context, str);
    }

    public void setManagedUser(UserHandle userHandle) {
        this.mManagedUser = userHandle;
    }

    public int getAvailabilityStatus() {
        UserHandle userHandle = this.mManagedUser;
        return (userHandle == null || isCrossProfileCalendarDisallowedByAdmin(this.mContext, userHandle.getIdentifier())) ? 4 : 0;
    }

    public boolean isChecked() {
        if (this.mManagedUser == null) {
            return false;
        }
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "cross_profile_calendar_enabled", 0, this.mManagedUser.getIdentifier()) == 1;
    }

    public boolean setChecked(boolean z) {
        if (this.mManagedUser == null) {
            return false;
        }
        return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "cross_profile_calendar_enabled", z ? 1 : 0, this.mManagedUser.getIdentifier());
    }

    static boolean isCrossProfileCalendarDisallowedByAdmin(Context context, int i) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) createPackageContextAsUser(context, i).getSystemService(DevicePolicyManager.class);
        if (devicePolicyManager == null) {
            return true;
        }
        Set crossProfileCalendarPackages = devicePolicyManager.getCrossProfileCalendarPackages();
        if (crossProfileCalendarPackages == null || !crossProfileCalendarPackages.isEmpty()) {
            return false;
        }
        return true;
    }

    private static Context createPackageContextAsUser(Context context, int i) {
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 0, UserHandle.of(i));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to create user context", e);
            return null;
        }
    }
}
