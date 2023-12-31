package com.android.settings.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.permission.PermissionControllerManager;
import androidx.preference.Preference;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.havoc.config.center.C1715R;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TopLevelLocationPreferenceController extends BasePreferenceController implements LifecycleObserver, OnStart, OnStop {
    private static final IntentFilter INTENT_FILTER_LOCATION_MODE_CHANGED = new IntentFilter("android.location.MODE_CHANGED");
    private AtomicInteger loadingInProgress = new AtomicInteger(0);
    private final LocationManager mLocationManager;
    private int mNumTotal = -1;
    private int mNumTotalLoading = 0;
    private Preference mPreference;
    private BroadcastReceiver mReceiver;

    public int getAvailabilityStatus() {
        return 0;
    }

    public TopLevelLocationPreferenceController(Context context, String str) {
        super(context, str);
        this.mLocationManager = (LocationManager) context.getSystemService("location");
    }

    public CharSequence getSummary() {
        if (!this.mLocationManager.isLocationEnabled()) {
            return this.mContext.getString(C1715R.string.location_settings_summary_location_off);
        }
        if (this.mNumTotal == -1) {
            return this.mContext.getString(C1715R.string.location_settings_loading_app_permission_stats);
        }
        Resources resources = this.mContext.getResources();
        int i = this.mNumTotal;
        return resources.getQuantityString(C1715R.plurals.location_settings_summary_location_on, i, new Object[]{Integer.valueOf(i)});
    }

    /* access modifiers changed from: package-private */
    public void setLocationAppCount(int i) {
        this.mNumTotal = i;
        refreshSummary(this.mPreference);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        this.mPreference = preference;
        refreshSummary(preference);
        if (this.mLocationManager.isLocationEnabled() && this.loadingInProgress.get() == 0) {
            this.mNumTotalLoading = 0;
            List<UserHandle> userProfiles = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUserProfiles();
            this.loadingInProgress.set(userProfiles.size());
            for (UserHandle identifier : userProfiles) {
                Context createPackageContextAsUser = Utils.createPackageContextAsUser(this.mContext, identifier.getIdentifier());
                if (createPackageContextAsUser != null) {
                    ((PermissionControllerManager) createPackageContextAsUser.getSystemService(PermissionControllerManager.class)).countPermissionApps(Arrays.asList(new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}), 1, new PermissionControllerManager.OnCountPermissionAppsResultCallback() {
                        public final void onCountPermissionApps(int i) {
                            TopLevelLocationPreferenceController.this.lambda$updateState$0$TopLevelLocationPreferenceController(i);
                        }
                    }, (Handler) null);
                } else if (this.loadingInProgress.decrementAndGet() == 0) {
                    setLocationAppCount(this.mNumTotalLoading);
                }
            }
        }
    }

    public /* synthetic */ void lambda$updateState$0$TopLevelLocationPreferenceController(int i) {
        this.mNumTotalLoading += i;
        if (this.loadingInProgress.decrementAndGet() == 0) {
            setLocationAppCount(this.mNumTotalLoading);
        }
    }

    public void onStart() {
        if (this.mReceiver == null) {
            this.mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    TopLevelLocationPreferenceController.this.refreshLocationMode();
                }
            };
        }
        this.mContext.registerReceiver(this.mReceiver, INTENT_FILTER_LOCATION_MODE_CHANGED);
        refreshLocationMode();
    }

    public void onStop() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    /* access modifiers changed from: private */
    public void refreshLocationMode() {
        Preference preference = this.mPreference;
        if (preference != null) {
            updateState(preference);
        }
    }
}
