package com.android.settings.location;

import android.content.Context;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.permission.PermissionControllerManager;
import android.provider.Settings;
import androidx.preference.Preference;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.havoc.config.center.C1715R;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AppLocationPermissionPreferenceController extends LocationBasePreferenceController implements PreferenceControllerMixin {
    final AtomicInteger loadingInProgress = new AtomicInteger(0);
    private final LocationManager mLocationManager;
    int mNumHasLocation = -1;
    private int mNumHasLocationLoading = 0;
    int mNumTotal = -1;
    private int mNumTotalLoading = 0;
    private Preference mPreference;

    public String getPreferenceKey() {
        return "app_level_permissions";
    }

    public AppLocationPermissionPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
        this.mLocationManager = (LocationManager) context.getSystemService("location");
    }

    public boolean isAvailable() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "location_settings_link_to_permissions_enabled", 1) == 1;
    }

    public CharSequence getSummary() {
        if (!this.mLocationManager.isLocationEnabled()) {
            return this.mContext.getString(C1715R.string.location_app_permission_summary_location_off);
        }
        if (this.mNumTotal == -1 || this.mNumHasLocation == -1) {
            return this.mContext.getString(C1715R.string.location_settings_loading_app_permission_stats);
        }
        Resources resources = this.mContext.getResources();
        int i = this.mNumHasLocation;
        return resources.getQuantityString(C1715R.plurals.location_app_permission_summary_location_on, i, new Object[]{Integer.valueOf(i), Integer.valueOf(this.mNumTotal)});
    }

    private void setAppCounts(int i, int i2) {
        this.mNumTotal = i;
        this.mNumHasLocation = i2;
        refreshSummary(this.mPreference);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreference = preference;
        refreshSummary(preference);
        if (this.mLocationManager.isLocationEnabled() && this.loadingInProgress.get() == 0) {
            this.mNumTotalLoading = 0;
            this.mNumHasLocationLoading = 0;
            List<UserHandle> userProfiles = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUserProfiles();
            this.loadingInProgress.set(userProfiles.size() * 2);
            for (UserHandle identifier : userProfiles) {
                Context createPackageContextAsUser = Utils.createPackageContextAsUser(this.mContext, identifier.getIdentifier());
                if (createPackageContextAsUser == null) {
                    for (int i = 0; i < 2; i++) {
                        if (this.loadingInProgress.decrementAndGet() == 0) {
                            setAppCounts(this.mNumTotalLoading, this.mNumHasLocationLoading);
                        }
                    }
                } else {
                    PermissionControllerManager permissionControllerManager = (PermissionControllerManager) createPackageContextAsUser.getSystemService(PermissionControllerManager.class);
                    permissionControllerManager.countPermissionApps(Arrays.asList(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}), 0, new PermissionControllerManager.OnCountPermissionAppsResultCallback() {
                        public final void onCountPermissionApps(int i) {
                            AppLocationPermissionPreferenceController.this.lambda$updateState$0$AppLocationPermissionPreferenceController(i);
                        }
                    }, (Handler) null);
                    permissionControllerManager.countPermissionApps(Arrays.asList(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}), 1, new PermissionControllerManager.OnCountPermissionAppsResultCallback() {
                        public final void onCountPermissionApps(int i) {
                            AppLocationPermissionPreferenceController.this.lambda$updateState$1$AppLocationPermissionPreferenceController(i);
                        }
                    }, (Handler) null);
                }
            }
        }
    }

    public /* synthetic */ void lambda$updateState$0$AppLocationPermissionPreferenceController(int i) {
        this.mNumTotalLoading += i;
        if (this.loadingInProgress.decrementAndGet() == 0) {
            setAppCounts(this.mNumTotalLoading, this.mNumHasLocationLoading);
        }
    }

    public /* synthetic */ void lambda$updateState$1$AppLocationPermissionPreferenceController(int i) {
        this.mNumHasLocationLoading += i;
        if (this.loadingInProgress.decrementAndGet() == 0) {
            setAppCounts(this.mNumTotalLoading, this.mNumHasLocationLoading);
        }
    }

    public void onLocationModeChanged(int i, boolean z) {
        Preference preference = this.mPreference;
        if (preference != null) {
            updateState(preference);
        }
    }
}
