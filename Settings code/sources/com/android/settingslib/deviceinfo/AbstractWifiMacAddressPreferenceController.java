package com.android.settingslib.deviceinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settingslib.R$string;
import com.android.settingslib.core.lifecycle.Lifecycle;

public abstract class AbstractWifiMacAddressPreferenceController extends AbstractConnectivityPreferenceController {
    private static final String[] CONNECTIVITY_INTENTS = {"android.net.conn.CONNECTIVITY_CHANGE", "android.net.wifi.LINK_CONFIGURATION_CHANGED", "android.net.wifi.STATE_CHANGE"};
    static final String KEY_WIFI_MAC_ADDRESS = "wifi_mac_address";
    static final int OFF = 0;

    /* renamed from: ON */
    static final int f64ON = 1;
    private Preference mWifiMacAddress;
    private final WifiManager mWifiManager;

    public String getPreferenceKey() {
        return KEY_WIFI_MAC_ADDRESS;
    }

    public boolean isAvailable() {
        return true;
    }

    public AbstractWifiMacAddressPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
        this.mWifiManager = (WifiManager) context.getSystemService(WifiManager.class);
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mWifiMacAddress = preferenceScreen.findPreference(KEY_WIFI_MAC_ADDRESS);
        updateConnectivity();
    }

    /* access modifiers changed from: protected */
    public String[] getConnectivityIntents() {
        return CONNECTIVITY_INTENTS;
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"HardwareIds"})
    public void updateConnectivity() {
        String[] factoryMacAddresses = this.mWifiManager.getFactoryMacAddresses();
        String str = (factoryMacAddresses == null || factoryMacAddresses.length <= 0) ? null : factoryMacAddresses[0];
        if (TextUtils.isEmpty(str)) {
            this.mWifiMacAddress.setSummary(R$string.status_unavailable);
        } else {
            this.mWifiMacAddress.setSummary((CharSequence) str);
        }
    }
}
