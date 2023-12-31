package com.android.settings.wifi.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class WifiP2pPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {
    private final IntentFilter mFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
    private final IntentFilter mLocationFilter = new IntentFilter("android.location.MODE_CHANGED");
    private final LocationManager mLocationManager;
    private final BroadcastReceiver mLocationReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WifiP2pPreferenceController.this.mWifiDirectPref != null) {
                WifiP2pPreferenceController wifiP2pPreferenceController = WifiP2pPreferenceController.this;
                wifiP2pPreferenceController.updateState(wifiP2pPreferenceController.mWifiDirectPref);
            }
        }
    };
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            WifiP2pPreferenceController.this.togglePreferences();
        }
    };
    /* access modifiers changed from: private */
    public Preference mWifiDirectPref;
    private final WifiManager mWifiManager;

    public String getPreferenceKey() {
        return "wifi_direct";
    }

    public boolean isAvailable() {
        return true;
    }

    public WifiP2pPreferenceController(Context context, Lifecycle lifecycle, WifiManager wifiManager) {
        super(context);
        this.mWifiManager = wifiManager;
        lifecycle.addObserver(this);
        this.mLocationManager = (LocationManager) context.getSystemService("location");
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mWifiDirectPref = preferenceScreen.findPreference("wifi_direct");
        togglePreferences();
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(this.mLocationManager.isLocationEnabled() && this.mWifiManager.isWifiEnabled());
    }

    public void onResume() {
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
        this.mContext.registerReceiver(this.mLocationReceiver, this.mLocationFilter);
    }

    public void onPause() {
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mContext.unregisterReceiver(this.mLocationReceiver);
    }

    /* access modifiers changed from: private */
    public void togglePreferences() {
        Preference preference = this.mWifiDirectPref;
        if (preference != null) {
            preference.setEnabled(this.mWifiManager.isWifiEnabled() && this.mLocationManager.isLocationEnabled());
        }
    }
}
