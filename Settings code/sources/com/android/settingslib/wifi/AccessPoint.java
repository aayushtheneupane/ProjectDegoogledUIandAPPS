package com.android.settingslib.wifi;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkKey;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppData;
import android.net.ScoredNetwork;
import android.net.wifi.IWifiManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkScoreCache;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.hotspot2.ProvisioningCallback;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.CollectionUtils;
import com.android.settingslib.R$array;
import com.android.settingslib.R$string;
import com.android.settingslib.utils.ThreadUtils;
import com.android.settingslib.wifi.AccessPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AccessPoint implements Comparable<AccessPoint> {
    static final AtomicInteger sLastId = new AtomicInteger(0);
    private String bssid;
    AccessPointListener mAccessPointListener;
    private int mCarrierApEapType;
    private String mCarrierName;
    private WifiConfiguration mConfig;
    /* access modifiers changed from: private */
    public WifiManager.ActionListener mConnectListener;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mEapType;
    private final ArraySet<ScanResult> mExtraScanResults;
    private String mFqdn;
    private WifiInfo mInfo;
    private boolean mIsCarrierAp;
    private boolean mIsOweTransitionMode;
    private boolean mIsPskSaeTransitionMode;
    private boolean mIsRoaming;
    private boolean mIsScoredNetworkMetered;
    private String mKey;
    private final Object mLock;
    private NetworkInfo mNetworkInfo;
    /* access modifiers changed from: private */
    public String mOsuFailure;
    /* access modifiers changed from: private */
    public OsuProvider mOsuProvider;
    /* access modifiers changed from: private */
    public boolean mOsuProvisioningComplete;
    /* access modifiers changed from: private */
    public String mOsuStatus;
    private String mProviderFriendlyName;
    private int mRssi;
    private final ArraySet<ScanResult> mScanResults;
    private final Map<String, TimestampedScoredNetwork> mScoredNetworkCache;
    private int mSpeed;
    private Object mTag;
    private WifiManager mWifiManager;
    private int networkId;
    private int pskType;
    private int security;
    private String ssid;

    public interface AccessPointListener {
        void onAccessPointChanged(AccessPoint accessPoint);

        void onLevelChanged(AccessPoint accessPoint);
    }

    private static int roundToClosestSpeedEnum(int i) {
        if (i < 5) {
            return 0;
        }
        if (i < 7) {
            return 5;
        }
        if (i < 15) {
            return 10;
        }
        return i < 25 ? 20 : 30;
    }

    public static String securityToString(int i, int i2) {
        return i == 1 ? "WEP" : i == 2 ? i2 == 1 ? "WPA" : i2 == 2 ? "WPA2" : i2 == 3 ? "WPA_WPA2" : "PSK" : i == 3 ? "EAP" : i == 5 ? "SAE" : i == 6 ? "SUITE_B" : i == 4 ? "OWE" : "NONE";
    }

    public AccessPoint(Context context, Bundle bundle) {
        this.mLock = new Object();
        this.mScanResults = new ArraySet<>();
        this.mExtraScanResults = new ArraySet<>();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mEapType = 0;
        this.mRssi = Integer.MIN_VALUE;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsRoaming = false;
        this.mIsCarrierAp = false;
        this.mOsuProvisioningComplete = false;
        this.mIsPskSaeTransitionMode = false;
        this.mIsOweTransitionMode = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        if (bundle.containsKey("key_config")) {
            this.mConfig = (WifiConfiguration) bundle.getParcelable("key_config");
        }
        WifiConfiguration wifiConfiguration = this.mConfig;
        if (wifiConfiguration != null) {
            loadConfig(wifiConfiguration);
        }
        if (bundle.containsKey("key_ssid")) {
            this.ssid = bundle.getString("key_ssid");
        }
        if (bundle.containsKey("key_security")) {
            this.security = bundle.getInt("key_security");
        }
        if (bundle.containsKey("key_speed")) {
            this.mSpeed = bundle.getInt("key_speed");
        }
        if (bundle.containsKey("key_psktype")) {
            this.pskType = bundle.getInt("key_psktype");
        }
        if (bundle.containsKey("eap_psktype")) {
            this.mEapType = bundle.getInt("eap_psktype");
        }
        this.mInfo = (WifiInfo) bundle.getParcelable("key_wifiinfo");
        if (bundle.containsKey("key_networkinfo")) {
            this.mNetworkInfo = (NetworkInfo) bundle.getParcelable("key_networkinfo");
        }
        if (bundle.containsKey("key_scanresults")) {
            Parcelable[] parcelableArray = bundle.getParcelableArray("key_scanresults");
            this.mScanResults.clear();
            for (Parcelable parcelable : parcelableArray) {
                this.mScanResults.add((ScanResult) parcelable);
            }
        }
        if (bundle.containsKey("key_scorednetworkcache")) {
            Iterator it = bundle.getParcelableArrayList("key_scorednetworkcache").iterator();
            while (it.hasNext()) {
                TimestampedScoredNetwork timestampedScoredNetwork = (TimestampedScoredNetwork) it.next();
                this.mScoredNetworkCache.put(timestampedScoredNetwork.getScore().networkKey.wifiKey.bssid, timestampedScoredNetwork);
            }
        }
        if (bundle.containsKey("key_fqdn")) {
            this.mFqdn = bundle.getString("key_fqdn");
        }
        if (bundle.containsKey("key_provider_friendly_name")) {
            this.mProviderFriendlyName = bundle.getString("key_provider_friendly_name");
        }
        if (bundle.containsKey("key_is_carrier_ap")) {
            this.mIsCarrierAp = bundle.getBoolean("key_is_carrier_ap");
        }
        if (bundle.containsKey("key_carrier_ap_eap_type")) {
            this.mCarrierApEapType = bundle.getInt("key_carrier_ap_eap_type");
        }
        if (bundle.containsKey("key_carrier_name")) {
            this.mCarrierName = bundle.getString("key_carrier_name");
        }
        if (bundle.containsKey("key_is_psk_sae_transition_mode")) {
            this.mIsPskSaeTransitionMode = bundle.getBoolean("key_is_psk_sae_transition_mode");
        }
        if (bundle.containsKey("key_is_owe_transition_mode")) {
            this.mIsOweTransitionMode = bundle.getBoolean("key_is_owe_transition_mode");
        }
        update(this.mConfig, this.mInfo, this.mNetworkInfo);
        updateKey();
        updateBestRssiInfo();
    }

    public AccessPoint(Context context, WifiConfiguration wifiConfiguration) {
        this.mLock = new Object();
        this.mScanResults = new ArraySet<>();
        this.mExtraScanResults = new ArraySet<>();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mEapType = 0;
        this.mRssi = Integer.MIN_VALUE;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsRoaming = false;
        this.mIsCarrierAp = false;
        this.mOsuProvisioningComplete = false;
        this.mIsPskSaeTransitionMode = false;
        this.mIsOweTransitionMode = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        loadConfig(wifiConfiguration);
        updateKey();
    }

    public AccessPoint(Context context, PasspointConfiguration passpointConfiguration) {
        this.mLock = new Object();
        this.mScanResults = new ArraySet<>();
        this.mExtraScanResults = new ArraySet<>();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mEapType = 0;
        this.mRssi = Integer.MIN_VALUE;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsRoaming = false;
        this.mIsCarrierAp = false;
        this.mOsuProvisioningComplete = false;
        this.mIsPskSaeTransitionMode = false;
        this.mIsOweTransitionMode = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        this.mFqdn = passpointConfiguration.getHomeSp().getFqdn();
        this.mProviderFriendlyName = passpointConfiguration.getHomeSp().getFriendlyName();
        updateKey();
    }

    public AccessPoint(Context context, WifiConfiguration wifiConfiguration, Collection<ScanResult> collection, Collection<ScanResult> collection2) {
        this.mLock = new Object();
        this.mScanResults = new ArraySet<>();
        this.mExtraScanResults = new ArraySet<>();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mEapType = 0;
        this.mRssi = Integer.MIN_VALUE;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsRoaming = false;
        this.mIsCarrierAp = false;
        this.mOsuProvisioningComplete = false;
        this.mIsPskSaeTransitionMode = false;
        this.mIsOweTransitionMode = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        this.networkId = wifiConfiguration.networkId;
        this.mConfig = wifiConfiguration;
        this.mFqdn = wifiConfiguration.FQDN;
        setScanResultsPasspoint(collection, collection2);
        updateKey();
    }

    public AccessPoint(Context context, OsuProvider osuProvider, Collection<ScanResult> collection) {
        this.mLock = new Object();
        this.mScanResults = new ArraySet<>();
        this.mExtraScanResults = new ArraySet<>();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mEapType = 0;
        this.mRssi = Integer.MIN_VALUE;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsRoaming = false;
        this.mIsCarrierAp = false;
        this.mOsuProvisioningComplete = false;
        this.mIsPskSaeTransitionMode = false;
        this.mIsOweTransitionMode = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        this.mOsuProvider = osuProvider;
        setScanResults(collection);
        updateKey();
    }

    AccessPoint(Context context, Collection<ScanResult> collection) {
        this.mLock = new Object();
        this.mScanResults = new ArraySet<>();
        this.mExtraScanResults = new ArraySet<>();
        this.mScoredNetworkCache = new HashMap();
        this.networkId = -1;
        this.pskType = 0;
        this.mEapType = 0;
        this.mRssi = Integer.MIN_VALUE;
        this.mSpeed = 0;
        this.mIsScoredNetworkMetered = false;
        this.mIsRoaming = false;
        this.mIsCarrierAp = false;
        this.mOsuProvisioningComplete = false;
        this.mIsPskSaeTransitionMode = false;
        this.mIsOweTransitionMode = false;
        this.mCarrierApEapType = -1;
        this.mCarrierName = null;
        this.mContext = context;
        setScanResults(collection);
        updateKey();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void loadConfig(WifiConfiguration wifiConfiguration) {
        String str = wifiConfiguration.SSID;
        this.ssid = str == null ? "" : removeDoubleQuotes(str);
        this.bssid = wifiConfiguration.BSSID;
        this.security = getSecurity(wifiConfiguration);
        this.networkId = wifiConfiguration.networkId;
        this.mConfig = wifiConfiguration;
    }

    private void updateKey() {
        if (isPasspoint()) {
            this.mKey = getKey(this.mConfig);
        } else if (isPasspointConfig()) {
            this.mKey = getKey(this.mFqdn);
        } else if (isOsuProvider()) {
            this.mKey = getKey(this.mOsuProvider);
        } else {
            this.mKey = getKey(getSsidStr(), getBssid(), getSecurity());
        }
    }

    public int compareTo(AccessPoint accessPoint) {
        if (isActive() && !accessPoint.isActive()) {
            return -1;
        }
        if (!isActive() && accessPoint.isActive()) {
            return 1;
        }
        if (isReachable() && !accessPoint.isReachable()) {
            return -1;
        }
        if (!isReachable() && accessPoint.isReachable()) {
            return 1;
        }
        if (isSaved() && !accessPoint.isSaved()) {
            return -1;
        }
        if (!isSaved() && accessPoint.isSaved()) {
            return 1;
        }
        if (getSpeed() != accessPoint.getSpeed()) {
            return accessPoint.getSpeed() - getSpeed();
        }
        int calculateSignalLevel = WifiManager.calculateSignalLevel(accessPoint.mRssi, 5) - WifiManager.calculateSignalLevel(this.mRssi, 5);
        if (calculateSignalLevel != 0) {
            return calculateSignalLevel;
        }
        int compareToIgnoreCase = getTitle().compareToIgnoreCase(accessPoint.getTitle());
        if (compareToIgnoreCase != 0) {
            return compareToIgnoreCase;
        }
        return getSsidStr().compareTo(accessPoint.getSsidStr());
    }

    public boolean equals(Object obj) {
        if ((obj instanceof AccessPoint) && compareTo((AccessPoint) obj) == 0) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        WifiInfo wifiInfo = this.mInfo;
        int i = 0;
        if (wifiInfo != null) {
            i = 0 + (wifiInfo.hashCode() * 13);
        }
        return i + (this.mRssi * 19) + (this.networkId * 23) + (this.ssid.hashCode() * 29);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AccessPoint(");
        sb.append(this.ssid);
        if (this.bssid != null) {
            sb.append(":");
            sb.append(this.bssid);
        }
        if (isSaved()) {
            sb.append(',');
            sb.append("saved");
        }
        if (isActive()) {
            sb.append(',');
            sb.append("active");
        }
        if (isEphemeral()) {
            sb.append(',');
            sb.append("ephemeral");
        }
        if (isConnectable()) {
            sb.append(',');
            sb.append("connectable");
        }
        int i = this.security;
        if (!(i == 0 || i == 4)) {
            sb.append(',');
            sb.append(securityToString(this.security, this.pskType));
        }
        sb.append(",level=");
        sb.append(getLevel());
        if (this.mSpeed != 0) {
            sb.append(",speed=");
            sb.append(this.mSpeed);
        }
        sb.append(",metered=");
        sb.append(isMetered());
        if (isVerboseLoggingEnabled()) {
            sb.append(",rssi=");
            sb.append(this.mRssi);
            synchronized (this.mLock) {
                sb.append(",scan cache size=");
                sb.append(this.mScanResults.size() + this.mExtraScanResults.size());
            }
        }
        sb.append(')');
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public boolean update(WifiNetworkScoreCache wifiNetworkScoreCache, boolean z, long j) {
        boolean updateScores = z ? updateScores(wifiNetworkScoreCache, j) : false;
        if (updateMetered(wifiNetworkScoreCache) || updateScores) {
            return true;
        }
        return false;
    }

    private boolean updateScores(WifiNetworkScoreCache wifiNetworkScoreCache, long j) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        synchronized (this.mLock) {
            Iterator<ScanResult> it = this.mScanResults.iterator();
            while (it.hasNext()) {
                ScanResult next = it.next();
                ScoredNetwork scoredNetwork = wifiNetworkScoreCache.getScoredNetwork(next);
                if (scoredNetwork != null) {
                    TimestampedScoredNetwork timestampedScoredNetwork = this.mScoredNetworkCache.get(next.BSSID);
                    if (timestampedScoredNetwork == null) {
                        this.mScoredNetworkCache.put(next.BSSID, new TimestampedScoredNetwork(scoredNetwork, elapsedRealtime));
                    } else {
                        timestampedScoredNetwork.update(scoredNetwork, elapsedRealtime);
                    }
                }
            }
        }
        Iterator<TimestampedScoredNetwork> it2 = this.mScoredNetworkCache.values().iterator();
        it2.forEachRemaining(new Consumer(elapsedRealtime - j, it2) {
            private final /* synthetic */ long f$0;
            private final /* synthetic */ Iterator f$1;

            {
                this.f$0 = r1;
                this.f$1 = r3;
            }

            public final void accept(Object obj) {
                AccessPoint.lambda$updateScores$0(this.f$0, this.f$1, (TimestampedScoredNetwork) obj);
            }
        });
        return updateSpeed();
    }

    static /* synthetic */ void lambda$updateScores$0(long j, Iterator it, TimestampedScoredNetwork timestampedScoredNetwork) {
        if (timestampedScoredNetwork.getUpdatedTimestampMillis() < j) {
            it.remove();
        }
    }

    private boolean updateSpeed() {
        int i = this.mSpeed;
        this.mSpeed = generateAverageSpeedForSsid();
        boolean z = i != this.mSpeed;
        if (isVerboseLoggingEnabled() && z) {
            Log.i("SettingsLib.AccessPoint", String.format("%s: Set speed to %d", new Object[]{this.ssid, Integer.valueOf(this.mSpeed)}));
        }
        return z;
    }

    private int generateAverageSpeedForSsid() {
        int i;
        if (this.mScoredNetworkCache.isEmpty()) {
            return 0;
        }
        if (Log.isLoggable("SettingsLib.AccessPoint", 3)) {
            Log.d("SettingsLib.AccessPoint", String.format("Generating fallbackspeed for %s using cache: %s", new Object[]{getSsidStr(), this.mScoredNetworkCache}));
        }
        int i2 = 0;
        int i3 = 0;
        for (TimestampedScoredNetwork score : this.mScoredNetworkCache.values()) {
            int calculateBadge = score.getScore().calculateBadge(this.mRssi);
            if (calculateBadge != 0) {
                i2++;
                i3 += calculateBadge;
            }
        }
        if (i2 == 0) {
            i = 0;
        } else {
            i = i3 / i2;
        }
        if (isVerboseLoggingEnabled()) {
            Log.i("SettingsLib.AccessPoint", String.format("%s generated fallback speed is: %d", new Object[]{getSsidStr(), Integer.valueOf(i)}));
        }
        return roundToClosestSpeedEnum(i);
    }

    private boolean updateMetered(WifiNetworkScoreCache wifiNetworkScoreCache) {
        WifiInfo wifiInfo;
        boolean z = this.mIsScoredNetworkMetered;
        this.mIsScoredNetworkMetered = false;
        if (!isActive() || (wifiInfo = this.mInfo) == null) {
            synchronized (this.mLock) {
                Iterator<ScanResult> it = this.mScanResults.iterator();
                while (it.hasNext()) {
                    ScoredNetwork scoredNetwork = wifiNetworkScoreCache.getScoredNetwork(it.next());
                    if (scoredNetwork != null) {
                        this.mIsScoredNetworkMetered = scoredNetwork.meteredHint | this.mIsScoredNetworkMetered;
                    }
                }
            }
        } else {
            ScoredNetwork scoredNetwork2 = wifiNetworkScoreCache.getScoredNetwork(NetworkKey.createFromWifiInfo(wifiInfo));
            if (scoredNetwork2 != null) {
                this.mIsScoredNetworkMetered = scoredNetwork2.meteredHint | this.mIsScoredNetworkMetered;
            }
        }
        if (z == this.mIsScoredNetworkMetered) {
            return true;
        }
        return false;
    }

    public static String getKey(Context context, ScanResult scanResult) {
        return getKey(scanResult.SSID, scanResult.BSSID, getSecurity(context, scanResult));
    }

    public static String getKey(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.isPasspoint()) {
            return getKey(wifiConfiguration.FQDN);
        }
        return getKey(removeDoubleQuotes(wifiConfiguration.SSID), wifiConfiguration.BSSID, getSecurity(wifiConfiguration));
    }

    public static String getKey(String str) {
        return "FQDN:" + str;
    }

    public static String getKey(OsuProvider osuProvider) {
        return "OSU:" + osuProvider.getFriendlyName() + ',' + osuProvider.getServerUri();
    }

    private static String getKey(String str, String str2, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("AP:");
        if (TextUtils.isEmpty(str)) {
            sb.append(str2);
        } else {
            sb.append(str);
        }
        sb.append(',');
        sb.append(i);
        return sb.toString();
    }

    public String getKey() {
        return this.mKey;
    }

    public boolean matches(AccessPoint accessPoint) {
        if (isPasspoint() || isPasspointConfig() || isOsuProvider()) {
            return getKey().equals(accessPoint.getKey());
        }
        if (!isSameSsidOrBssid(accessPoint)) {
            return false;
        }
        int security2 = accessPoint.getSecurity();
        if (!this.mIsPskSaeTransitionMode) {
            int i = this.security;
            if ((i == 5 || i == 2) && accessPoint.isPskSaeTransitionMode()) {
                return true;
            }
        } else if ((security2 == 5 && getWifiManager().isWpa3SaeSupported()) || security2 == 2) {
            return true;
        }
        if (!this.mIsOweTransitionMode) {
            int i2 = this.security;
            if ((i2 == 4 || i2 == 0) && accessPoint.isOweTransitionMode()) {
                return true;
            }
        } else if ((security2 == 4 && getWifiManager().isEnhancedOpenSupported()) || security2 == 0) {
            return true;
        }
        if (this.security == accessPoint.getSecurity()) {
            return true;
        }
        return false;
    }

    public boolean matches(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.isPasspoint()) {
            if (!isPasspoint() || !wifiConfiguration.FQDN.equals(this.mConfig.FQDN)) {
                return false;
            }
            return true;
        } else if (!this.ssid.equals(removeDoubleQuotes(wifiConfiguration.SSID))) {
            return false;
        } else {
            WifiConfiguration wifiConfiguration2 = this.mConfig;
            if (wifiConfiguration2 != null && wifiConfiguration2.shared != wifiConfiguration.shared) {
                return false;
            }
            int security2 = getSecurity(wifiConfiguration);
            if (this.mIsPskSaeTransitionMode && ((security2 == 5 && getWifiManager().isWpa3SaeSupported()) || security2 == 2)) {
                return true;
            }
            if (this.mIsOweTransitionMode && ((security2 == 4 && getWifiManager().isEnhancedOpenSupported()) || security2 == 0)) {
                return true;
            }
            if (this.security == getSecurity(wifiConfiguration)) {
                return true;
            }
            return false;
        }
    }

    private boolean matches(WifiConfiguration wifiConfiguration, WifiInfo wifiInfo) {
        if (wifiConfiguration == null || wifiInfo == null) {
            return false;
        }
        if (wifiConfiguration.isPasspoint() || isSameSsidOrBssid(wifiInfo)) {
            return matches(wifiConfiguration);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean matches(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        if (isPasspoint() || isOsuProvider()) {
            throw new IllegalStateException("Should not matches a Passpoint by ScanResult");
        } else if (!isSameSsidOrBssid(scanResult)) {
            return false;
        } else {
            if (!this.mIsPskSaeTransitionMode) {
                int i = this.security;
                if ((i == 5 || i == 2) && isPskSaeTransitionMode(scanResult)) {
                    return true;
                }
            } else if ((scanResult.capabilities.contains("SAE") && getWifiManager().isWpa3SaeSupported()) || scanResult.capabilities.contains("PSK")) {
                return true;
            }
            if (this.mIsOweTransitionMode) {
                int security2 = getSecurity(this.mContext, scanResult);
                if ((security2 == 4 && getWifiManager().isEnhancedOpenSupported()) || security2 == 0) {
                    return true;
                }
            } else {
                int i2 = this.security;
                if ((i2 == 4 || i2 == 0) && isOweTransitionMode(scanResult)) {
                    return true;
                }
            }
            if (this.security == getSecurity(this.mContext, scanResult)) {
                return true;
            }
            return false;
        }
    }

    public WifiConfiguration getConfig() {
        return this.mConfig;
    }

    public String getPasspointFqdn() {
        return this.mFqdn;
    }

    public WifiInfo getInfo() {
        return this.mInfo;
    }

    public int getLevel() {
        return WifiManager.calculateSignalLevel(this.mRssi, 5);
    }

    public Set<ScanResult> getScanResults() {
        ArraySet arraySet = new ArraySet();
        synchronized (this.mLock) {
            arraySet.addAll(this.mScanResults);
            arraySet.addAll(this.mExtraScanResults);
        }
        return arraySet;
    }

    public Map<String, TimestampedScoredNetwork> getScoredNetworkCache() {
        return this.mScoredNetworkCache;
    }

    private void updateBestRssiInfo() {
        ScanResult scanResult;
        int i;
        int i2;
        if (!isActive()) {
            synchronized (this.mLock) {
                Iterator<ScanResult> it = this.mScanResults.iterator();
                scanResult = null;
                i = Integer.MIN_VALUE;
                while (it.hasNext()) {
                    ScanResult next = it.next();
                    if (next.level > i) {
                        i = next.level;
                        scanResult = next;
                    }
                }
            }
            if (i == Integer.MIN_VALUE || (i2 = this.mRssi) == Integer.MIN_VALUE) {
                this.mRssi = i;
            } else {
                this.mRssi = (i2 + i) / 2;
            }
            if (scanResult != null) {
                this.ssid = scanResult.SSID;
                this.bssid = scanResult.BSSID;
                this.security = getSecurity(this.mContext, scanResult);
                int i3 = this.security;
                if (i3 == 2 || i3 == 5) {
                    this.pskType = getPskType(scanResult);
                }
                if (this.security == 3) {
                    this.mEapType = getEapType(scanResult);
                }
                this.mIsPskSaeTransitionMode = isPskSaeTransitionMode(scanResult);
                this.mIsOweTransitionMode = isOweTransitionMode(scanResult);
                this.mIsCarrierAp = scanResult.isCarrierAp;
                this.mCarrierApEapType = scanResult.carrierApEapType;
                this.mCarrierName = scanResult.carrierName;
            }
            if (isPasspoint()) {
                this.mConfig.SSID = convertToQuotedString(this.ssid);
            }
        }
    }

    public boolean isMetered() {
        return this.mIsScoredNetworkMetered || WifiConfiguration.isMetered(this.mConfig, this.mInfo);
    }

    public NetworkInfo getNetworkInfo() {
        return this.mNetworkInfo;
    }

    public int getSecurity() {
        return this.security;
    }

    public String getSecurityString(boolean z) {
        Context context = this.mContext;
        if (isPasspoint() || isPasspointConfig()) {
            if (z) {
                return context.getString(R$string.wifi_security_short_eap);
            }
            return context.getString(R$string.wifi_security_eap);
        } else if (!this.mIsPskSaeTransitionMode) {
            switch (this.security) {
                case 1:
                    if (z) {
                        return context.getString(R$string.wifi_security_short_wep);
                    }
                    return context.getString(R$string.wifi_security_wep);
                case 2:
                    int i = this.pskType;
                    if (i != 1) {
                        if (i != 2) {
                            if (i != 3) {
                                if (z) {
                                    return context.getString(R$string.wifi_security_short_psk_generic);
                                }
                                return context.getString(R$string.wifi_security_psk_generic);
                            } else if (z) {
                                return context.getString(R$string.wifi_security_short_wpa_wpa2);
                            } else {
                                return context.getString(R$string.wifi_security_wpa_wpa2);
                            }
                        } else if (z) {
                            return context.getString(R$string.wifi_security_short_wpa2);
                        } else {
                            return context.getString(R$string.wifi_security_wpa2);
                        }
                    } else if (z) {
                        return context.getString(R$string.wifi_security_short_wpa);
                    } else {
                        return context.getString(R$string.wifi_security_wpa);
                    }
                case 3:
                    int i2 = this.mEapType;
                    if (i2 != 1) {
                        if (i2 != 2) {
                            if (z) {
                                return context.getString(R$string.wifi_security_short_eap);
                            }
                            return context.getString(R$string.wifi_security_eap);
                        } else if (z) {
                            return context.getString(R$string.wifi_security_short_eap_wpa2_wpa3);
                        } else {
                            return context.getString(R$string.wifi_security_eap_wpa2_wpa3);
                        }
                    } else if (z) {
                        return context.getString(R$string.wifi_security_short_eap_wpa);
                    } else {
                        return context.getString(R$string.wifi_security_eap_wpa);
                    }
                case 4:
                    if (z) {
                        return context.getString(R$string.wifi_security_short_owe);
                    }
                    return context.getString(R$string.wifi_security_owe);
                case 5:
                    if (z) {
                        return context.getString(R$string.wifi_security_short_sae);
                    }
                    return context.getString(R$string.wifi_security_sae);
                case 6:
                    if (z) {
                        return context.getString(R$string.wifi_security_short_eap_suiteb);
                    }
                    return context.getString(R$string.wifi_security_eap_suiteb);
                default:
                    if (z) {
                        return "";
                    }
                    return context.getString(R$string.wifi_security_none);
            }
        } else if (z) {
            return context.getString(R$string.wifi_security_short_psk_sae);
        } else {
            return context.getString(R$string.wifi_security_psk_sae);
        }
    }

    public String getSsidStr() {
        return this.ssid;
    }

    public String getBssid() {
        return this.bssid;
    }

    public CharSequence getSsid() {
        return this.ssid;
    }

    @Deprecated
    public String getConfigName() {
        WifiConfiguration wifiConfiguration = this.mConfig;
        if (wifiConfiguration != null && wifiConfiguration.isPasspoint()) {
            return this.mConfig.providerFriendlyName;
        }
        if (this.mFqdn != null) {
            return this.mProviderFriendlyName;
        }
        return this.ssid;
    }

    public NetworkInfo.DetailedState getDetailedState() {
        NetworkInfo networkInfo = this.mNetworkInfo;
        if (networkInfo != null) {
            return networkInfo.getDetailedState();
        }
        Log.w("SettingsLib.AccessPoint", "NetworkInfo is null, cannot return detailed state");
        return null;
    }

    public boolean isCarrierAp() {
        return this.mIsCarrierAp;
    }

    public int getCarrierApEapType() {
        return this.mCarrierApEapType;
    }

    public String getCarrierName() {
        return this.mCarrierName;
    }

    public String getSavedNetworkSummary() {
        WifiConfiguration wifiConfiguration = this.mConfig;
        if (wifiConfiguration == null) {
            return "";
        }
        PackageManager packageManager = this.mContext.getPackageManager();
        String nameForUid = packageManager.getNameForUid(1000);
        int userId = UserHandle.getUserId(wifiConfiguration.creatorUid);
        ApplicationInfo applicationInfo = null;
        String str = wifiConfiguration.creatorName;
        if (str == null || !str.equals(nameForUid)) {
            try {
                applicationInfo = AppGlobals.getPackageManager().getApplicationInfo(wifiConfiguration.creatorName, 0, userId);
            } catch (RemoteException unused) {
            }
        } else {
            applicationInfo = this.mContext.getApplicationInfo();
        }
        if (applicationInfo == null || applicationInfo.packageName.equals(this.mContext.getString(R$string.settings_package)) || applicationInfo.packageName.equals(this.mContext.getString(R$string.certinstaller_package))) {
            return "";
        }
        return this.mContext.getString(R$string.saved_network, new Object[]{applicationInfo.loadLabel(packageManager)});
    }

    public String getTitle() {
        if (isPasspoint()) {
            return this.mConfig.providerFriendlyName;
        }
        if (isPasspointConfig()) {
            return this.mProviderFriendlyName;
        }
        if (isOsuProvider()) {
            return this.mOsuProvider.getFriendlyName();
        }
        return getSsidStr();
    }

    public String getSettingsSummary() {
        return getSettingsSummary(false);
    }

    public String getSettingsSummary(boolean z) {
        int i;
        StringBuilder sb = new StringBuilder();
        if (isOsuProvider()) {
            if (this.mOsuProvisioningComplete) {
                sb.append(this.mContext.getString(R$string.osu_sign_up_complete));
            } else {
                String str = this.mOsuFailure;
                if (str != null) {
                    sb.append(str);
                } else {
                    String str2 = this.mOsuStatus;
                    if (str2 != null) {
                        sb.append(str2);
                    } else {
                        sb.append(this.mContext.getString(R$string.tap_to_sign_up));
                    }
                }
            }
        } else if (!isActive()) {
            WifiConfiguration wifiConfiguration = this.mConfig;
            if (wifiConfiguration == null || !wifiConfiguration.hasNoInternetAccess()) {
                WifiConfiguration wifiConfiguration2 = this.mConfig;
                if (wifiConfiguration2 == null || wifiConfiguration2.getNetworkSelectionStatus().isNetworkEnabled()) {
                    WifiConfiguration wifiConfiguration3 = this.mConfig;
                    if (wifiConfiguration3 != null && wifiConfiguration3.getNetworkSelectionStatus().isNotRecommended()) {
                        sb.append(this.mContext.getString(R$string.wifi_disabled_by_recommendation_provider));
                    } else if (this.mIsCarrierAp) {
                        sb.append(String.format(this.mContext.getString(R$string.available_via_carrier), new Object[]{this.mCarrierName}));
                    } else if (!isReachable()) {
                        sb.append(this.mContext.getString(R$string.wifi_not_in_range));
                    } else {
                        WifiConfiguration wifiConfiguration4 = this.mConfig;
                        if (wifiConfiguration4 != null) {
                            if (wifiConfiguration4.recentFailure.getAssociationStatus() == 17) {
                                sb.append(this.mContext.getString(R$string.wifi_ap_unable_to_handle_new_sta));
                            } else if (z) {
                                sb.append(this.mContext.getString(R$string.wifi_disconnected));
                            } else {
                                sb.append(this.mContext.getString(R$string.wifi_remembered));
                            }
                        }
                    }
                } else {
                    int networkSelectionDisableReason = this.mConfig.getNetworkSelectionStatus().getNetworkSelectionDisableReason();
                    if (networkSelectionDisableReason == 2) {
                        sb.append(this.mContext.getString(R$string.wifi_disabled_generic));
                    } else if (networkSelectionDisableReason == 3) {
                        sb.append(this.mContext.getString(R$string.wifi_disabled_password_failure));
                    } else if (networkSelectionDisableReason == 4 || networkSelectionDisableReason == 5) {
                        sb.append(this.mContext.getString(R$string.wifi_disabled_network_failure));
                    } else if (networkSelectionDisableReason == 13) {
                        sb.append(this.mContext.getString(R$string.wifi_check_password_try_again));
                    }
                }
            } else {
                if (this.mConfig.getNetworkSelectionStatus().isNetworkPermanentlyDisabled()) {
                    i = R$string.wifi_no_internet_no_reconnect;
                } else {
                    i = R$string.wifi_no_internet;
                }
                sb.append(this.mContext.getString(i));
            }
        } else if (getDetailedState() != NetworkInfo.DetailedState.CONNECTED || !this.mIsCarrierAp) {
            Context context = this.mContext;
            NetworkInfo.DetailedState detailedState = getDetailedState();
            WifiInfo wifiInfo = this.mInfo;
            boolean z2 = wifiInfo != null && wifiInfo.isEphemeral();
            WifiInfo wifiInfo2 = this.mInfo;
            sb.append(getSummary(context, (String) null, detailedState, z2, wifiInfo2 != null ? wifiInfo2.getNetworkSuggestionOrSpecifierPackageName() : null));
        } else {
            sb.append(String.format(this.mContext.getString(R$string.connected_via_carrier), new Object[]{this.mCarrierName}));
        }
        if (isVerboseLoggingEnabled()) {
            sb.append(WifiUtils.buildLoggingSummary(this, this.mConfig));
        }
        WifiConfiguration wifiConfiguration5 = this.mConfig;
        if (wifiConfiguration5 != null && (WifiUtils.isMeteredOverridden(wifiConfiguration5) || this.mConfig.meteredHint)) {
            return this.mContext.getResources().getString(R$string.preference_summary_default_combination, new Object[]{WifiUtils.getMeteredLabel(this.mContext, this.mConfig), sb.toString()});
        } else if (getSpeedLabel() != null && sb.length() != 0) {
            return this.mContext.getResources().getString(R$string.preference_summary_default_combination, new Object[]{getSpeedLabel(), sb.toString()});
        } else if (getSpeedLabel() != null) {
            return getSpeedLabel();
        } else {
            return sb.toString();
        }
    }

    public boolean isActive() {
        NetworkInfo networkInfo = this.mNetworkInfo;
        return (networkInfo == null || (this.networkId == -1 && networkInfo.getState() == NetworkInfo.State.DISCONNECTED)) ? false : true;
    }

    public boolean isConnectable() {
        return getLevel() != -1 && getDetailedState() == null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000a, code lost:
        r1 = r1.mNetworkInfo;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isEphemeral() {
        /*
            r1 = this;
            android.net.wifi.WifiInfo r0 = r1.mInfo
            if (r0 == 0) goto L_0x0018
            boolean r0 = r0.isEphemeral()
            if (r0 == 0) goto L_0x0018
            android.net.NetworkInfo r1 = r1.mNetworkInfo
            if (r1 == 0) goto L_0x0018
            android.net.NetworkInfo$State r1 = r1.getState()
            android.net.NetworkInfo$State r0 = android.net.NetworkInfo.State.DISCONNECTED
            if (r1 == r0) goto L_0x0018
            r1 = 1
            goto L_0x0019
        L_0x0018:
            r1 = 0
        L_0x0019:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.wifi.AccessPoint.isEphemeral():boolean");
    }

    public boolean isPasspoint() {
        WifiConfiguration wifiConfiguration = this.mConfig;
        return wifiConfiguration != null && wifiConfiguration.isPasspoint();
    }

    public boolean isPasspointConfig() {
        return this.mFqdn != null && this.mConfig == null;
    }

    public boolean isOsuProvider() {
        return this.mOsuProvider != null;
    }

    public void startOsuProvisioning(WifiManager.ActionListener actionListener) {
        this.mConnectListener = actionListener;
        getWifiManager().startSubscriptionProvisioning(this.mOsuProvider, this.mContext.getMainExecutor(), new AccessPointProvisioningCallback());
    }

    private boolean isInfoForThisAccessPoint(WifiConfiguration wifiConfiguration, WifiInfo wifiInfo) {
        if (wifiInfo.isOsuAp() || this.mOsuStatus != null) {
            if (!wifiInfo.isOsuAp() || this.mOsuStatus == null) {
                return false;
            }
            return true;
        } else if (!wifiInfo.isPasspointAp() && !isPasspoint()) {
            int i = this.networkId;
            if (i != -1) {
                if (i == wifiInfo.getNetworkId()) {
                    return true;
                }
                return false;
            } else if (wifiConfiguration != null) {
                return matches(wifiConfiguration, wifiInfo);
            } else {
                return TextUtils.equals(removeDoubleQuotes(wifiInfo.getSSID()), this.ssid);
            }
        } else if (!wifiInfo.isPasspointAp() || !isPasspoint() || !TextUtils.equals(wifiInfo.getPasspointFqdn(), this.mConfig.FQDN)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isSaved() {
        return this.mConfig != null;
    }

    public Object getTag() {
        return this.mTag;
    }

    public void setTag(Object obj) {
        this.mTag = obj;
    }

    public void generateOpenNetworkConfig() {
        int i = this.security;
        if (i != 0 && i != 4) {
            throw new IllegalStateException();
        } else if (this.mConfig == null) {
            this.mConfig = new WifiConfiguration();
            this.mConfig.SSID = convertToQuotedString(this.ssid);
            if (this.security == 0 || !getWifiManager().isEasyConnectSupported()) {
                this.mConfig.allowedKeyManagement.set(0);
                return;
            }
            this.mConfig.allowedKeyManagement.set(9);
            this.mConfig.requirePMF = true;
        }
    }

    public void saveWifiState(Bundle bundle) {
        if (this.ssid != null) {
            bundle.putString("key_ssid", getSsidStr());
        }
        bundle.putInt("key_security", this.security);
        bundle.putInt("key_speed", this.mSpeed);
        bundle.putInt("key_psktype", this.pskType);
        bundle.putInt("eap_psktype", this.mEapType);
        WifiConfiguration wifiConfiguration = this.mConfig;
        if (wifiConfiguration != null) {
            bundle.putParcelable("key_config", wifiConfiguration);
        }
        bundle.putParcelable("key_wifiinfo", this.mInfo);
        synchronized (this.mLock) {
            bundle.putParcelableArray("key_scanresults", (Parcelable[]) this.mScanResults.toArray(new Parcelable[(this.mScanResults.size() + this.mExtraScanResults.size())]));
        }
        bundle.putParcelableArrayList("key_scorednetworkcache", new ArrayList(this.mScoredNetworkCache.values()));
        NetworkInfo networkInfo = this.mNetworkInfo;
        if (networkInfo != null) {
            bundle.putParcelable("key_networkinfo", networkInfo);
        }
        String str = this.mFqdn;
        if (str != null) {
            bundle.putString("key_fqdn", str);
        }
        String str2 = this.mProviderFriendlyName;
        if (str2 != null) {
            bundle.putString("key_provider_friendly_name", str2);
        }
        bundle.putBoolean("key_is_carrier_ap", this.mIsCarrierAp);
        bundle.putInt("key_carrier_ap_eap_type", this.mCarrierApEapType);
        bundle.putString("key_carrier_name", this.mCarrierName);
        bundle.putBoolean("key_is_psk_sae_transition_mode", this.mIsPskSaeTransitionMode);
        bundle.putBoolean("key_is_owe_transition_mode", this.mIsOweTransitionMode);
    }

    public void setListener(AccessPointListener accessPointListener) {
        this.mAccessPointListener = accessPointListener;
    }

    /* access modifiers changed from: package-private */
    public void setScanResults(Collection<ScanResult> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            Log.d("SettingsLib.AccessPoint", "Cannot set scan results to empty list");
            return;
        }
        if (this.mKey != null && !isPasspoint() && !isOsuProvider()) {
            for (ScanResult next : collection) {
                if (!matches(next)) {
                    Log.d("SettingsLib.AccessPoint", String.format("ScanResult %s\nkey of %s did not match current AP key %s", new Object[]{next, getKey(this.mContext, next), this.mKey}));
                    return;
                }
            }
        }
        int level = getLevel();
        synchronized (this.mLock) {
            this.mScanResults.clear();
            this.mScanResults.addAll(collection);
        }
        updateBestRssiInfo();
        int level2 = getLevel();
        if (level2 > 0 && level2 != level) {
            updateSpeed();
            ThreadUtils.postOnMainThread(new Runnable() {
                public final void run() {
                    AccessPoint.this.lambda$setScanResults$1$AccessPoint();
                }
            });
        }
        ThreadUtils.postOnMainThread(new Runnable() {
            public final void run() {
                AccessPoint.this.lambda$setScanResults$2$AccessPoint();
            }
        });
    }

    public /* synthetic */ void lambda$setScanResults$1$AccessPoint() {
        AccessPointListener accessPointListener = this.mAccessPointListener;
        if (accessPointListener != null) {
            accessPointListener.onLevelChanged(this);
        }
    }

    public /* synthetic */ void lambda$setScanResults$2$AccessPoint() {
        AccessPointListener accessPointListener = this.mAccessPointListener;
        if (accessPointListener != null) {
            accessPointListener.onAccessPointChanged(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setScanResultsPasspoint(Collection<ScanResult> collection, Collection<ScanResult> collection2) {
        synchronized (this.mLock) {
            this.mExtraScanResults.clear();
            if (!CollectionUtils.isEmpty(collection)) {
                this.mIsRoaming = false;
                if (!CollectionUtils.isEmpty(collection2)) {
                    this.mExtraScanResults.addAll(collection2);
                }
                setScanResults(collection);
            } else if (!CollectionUtils.isEmpty(collection2)) {
                this.mIsRoaming = true;
                setScanResults(collection2);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
        if (r5.getDetailedState() != r7.getDetailedState()) goto L_0x0036;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean update(android.net.wifi.WifiConfiguration r5, android.net.wifi.WifiInfo r6, android.net.NetworkInfo r7) {
        /*
            r4 = this;
            int r0 = r4.getLevel()
            r1 = 0
            r2 = 1
            if (r6 == 0) goto L_0x004e
            boolean r3 = r4.isInfoForThisAccessPoint(r5, r6)
            if (r3 == 0) goto L_0x004e
            android.net.wifi.WifiInfo r3 = r4.mInfo
            if (r3 != 0) goto L_0x0013
            r1 = r2
        L_0x0013:
            boolean r3 = r4.isPasspoint()
            if (r3 != 0) goto L_0x0020
            android.net.wifi.WifiConfiguration r3 = r4.mConfig
            if (r3 == r5) goto L_0x0020
            r4.update(r5)
        L_0x0020:
            int r5 = r4.mRssi
            int r3 = r6.getRssi()
            if (r5 == r3) goto L_0x0038
            int r5 = r6.getRssi()
            r3 = -127(0xffffffffffffff81, float:NaN)
            if (r5 == r3) goto L_0x0038
            int r5 = r6.getRssi()
            r4.mRssi = r5
        L_0x0036:
            r1 = r2
            goto L_0x0049
        L_0x0038:
            android.net.NetworkInfo r5 = r4.mNetworkInfo
            if (r5 == 0) goto L_0x0049
            if (r7 == 0) goto L_0x0049
            android.net.NetworkInfo$DetailedState r5 = r5.getDetailedState()
            android.net.NetworkInfo$DetailedState r3 = r7.getDetailedState()
            if (r5 == r3) goto L_0x0049
            goto L_0x0036
        L_0x0049:
            r4.mInfo = r6
            r4.mNetworkInfo = r7
            goto L_0x0058
        L_0x004e:
            android.net.wifi.WifiInfo r5 = r4.mInfo
            if (r5 == 0) goto L_0x0058
            r5 = 0
            r4.mInfo = r5
            r4.mNetworkInfo = r5
            r1 = r2
        L_0x0058:
            if (r1 == 0) goto L_0x0074
            com.android.settingslib.wifi.AccessPoint$AccessPointListener r5 = r4.mAccessPointListener
            if (r5 == 0) goto L_0x0074
            com.android.settingslib.wifi.-$$Lambda$AccessPoint$S7H59e_8IxpVPy0V68Oc2-zX-rg r5 = new com.android.settingslib.wifi.-$$Lambda$AccessPoint$S7H59e_8IxpVPy0V68Oc2-zX-rg
            r5.<init>()
            com.android.settingslib.utils.ThreadUtils.postOnMainThread(r5)
            int r5 = r4.getLevel()
            if (r0 == r5) goto L_0x0074
            com.android.settingslib.wifi.-$$Lambda$AccessPoint$QW-1Uw0oxoaKqUtEtPO0oPvH5ng r5 = new com.android.settingslib.wifi.-$$Lambda$AccessPoint$QW-1Uw0oxoaKqUtEtPO0oPvH5ng
            r5.<init>()
            com.android.settingslib.utils.ThreadUtils.postOnMainThread(r5)
        L_0x0074:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.wifi.AccessPoint.update(android.net.wifi.WifiConfiguration, android.net.wifi.WifiInfo, android.net.NetworkInfo):boolean");
    }

    public /* synthetic */ void lambda$update$3$AccessPoint() {
        AccessPointListener accessPointListener = this.mAccessPointListener;
        if (accessPointListener != null) {
            accessPointListener.onAccessPointChanged(this);
        }
    }

    public /* synthetic */ void lambda$update$4$AccessPoint() {
        AccessPointListener accessPointListener = this.mAccessPointListener;
        if (accessPointListener != null) {
            accessPointListener.onLevelChanged(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void update(WifiConfiguration wifiConfiguration) {
        this.mConfig = wifiConfiguration;
        if (this.mConfig != null && !isPasspoint()) {
            this.ssid = removeDoubleQuotes(this.mConfig.SSID);
        }
        this.networkId = wifiConfiguration != null ? wifiConfiguration.networkId : -1;
        ThreadUtils.postOnMainThread(new Runnable() {
            public final void run() {
                AccessPoint.this.lambda$update$5$AccessPoint();
            }
        });
    }

    public /* synthetic */ void lambda$update$5$AccessPoint() {
        AccessPointListener accessPointListener = this.mAccessPointListener;
        if (accessPointListener != null) {
            accessPointListener.onAccessPointChanged(this);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setRssi(int i) {
        this.mRssi = i;
    }

    /* access modifiers changed from: package-private */
    public int getSpeed() {
        return this.mSpeed;
    }

    /* access modifiers changed from: package-private */
    public String getSpeedLabel() {
        return getSpeedLabel(this.mSpeed);
    }

    /* access modifiers changed from: package-private */
    public String getSpeedLabel(int i) {
        return getSpeedLabel(this.mContext, i);
    }

    private static String getSpeedLabel(Context context, int i) {
        if (i == 5) {
            return context.getString(R$string.speed_label_slow);
        }
        if (i == 10) {
            return context.getString(R$string.speed_label_okay);
        }
        if (i == 20) {
            return context.getString(R$string.speed_label_fast);
        }
        if (i != 30) {
            return null;
        }
        return context.getString(R$string.speed_label_very_fast);
    }

    public static String getSpeedLabel(Context context, ScoredNetwork scoredNetwork, int i) {
        return getSpeedLabel(context, roundToClosestSpeedEnum(scoredNetwork.calculateBadge(i)));
    }

    public boolean isReachable() {
        return this.mRssi != Integer.MIN_VALUE;
    }

    private static CharSequence getAppLabel(String str, PackageManager packageManager) {
        try {
            ApplicationInfo applicationInfoAsUser = packageManager.getApplicationInfoAsUser(str, 0, UserHandle.getUserId(-2));
            if (applicationInfoAsUser != null) {
                return applicationInfoAsUser.loadLabel(packageManager);
            }
            return "";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SettingsLib.AccessPoint", "Failed to get app info", e);
            return "";
        }
    }

    public static String getSummary(Context context, String str, NetworkInfo.DetailedState detailedState, boolean z, String str2) {
        if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
            if (z && !TextUtils.isEmpty(str2)) {
                return context.getString(R$string.connected_via_app, new Object[]{getAppLabel(str2, context.getPackageManager())});
            } else if (z) {
                NetworkScorerAppData activeScorer = ((NetworkScoreManager) context.getSystemService(NetworkScoreManager.class)).getActiveScorer();
                if (activeScorer == null || activeScorer.getRecommendationServiceLabel() == null) {
                    return context.getString(R$string.connected_via_network_scorer_default);
                }
                return String.format(context.getString(R$string.connected_via_network_scorer), new Object[]{activeScorer.getRecommendationServiceLabel()});
            }
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (detailedState == NetworkInfo.DetailedState.CONNECTED) {
            NetworkCapabilities networkCapabilities = null;
            try {
                networkCapabilities = connectivityManager.getNetworkCapabilities(IWifiManager.Stub.asInterface(ServiceManager.getService("wifi")).getCurrentNetwork());
            } catch (RemoteException unused) {
            }
            if (networkCapabilities != null) {
                if (networkCapabilities.hasCapability(17)) {
                    return context.getString(context.getResources().getIdentifier("network_available_sign_in", "string", "android"));
                }
                if (networkCapabilities.hasCapability(24)) {
                    return context.getString(R$string.wifi_limited_connection);
                }
                if (!networkCapabilities.hasCapability(16)) {
                    return context.getString(R$string.wifi_connected_no_internet);
                }
            }
        }
        if (detailedState == null) {
            Log.w("SettingsLib.AccessPoint", "state is null, returning empty summary");
            return "";
        }
        String[] stringArray = context.getResources().getStringArray(str == null ? R$array.wifi_status : R$array.wifi_status_with_ssid);
        int ordinal = detailedState.ordinal();
        if (ordinal >= stringArray.length || stringArray[ordinal].length() == 0) {
            return "";
        }
        return String.format(stringArray[ordinal], new Object[]{str});
    }

    public static String convertToQuotedString(String str) {
        return "\"" + str + "\"";
    }

    private static int getPskType(ScanResult scanResult) {
        boolean contains = scanResult.capabilities.contains("WPA-PSK");
        boolean contains2 = scanResult.capabilities.contains("RSN-PSK");
        boolean contains3 = scanResult.capabilities.contains("RSN-SAE");
        if (contains2 && contains) {
            return 3;
        }
        if (contains2) {
            return 2;
        }
        if (contains) {
            return 1;
        }
        if (contains3) {
            return 0;
        }
        Log.w("SettingsLib.AccessPoint", "Received abnormal flag string: " + scanResult.capabilities);
        return 0;
    }

    private static int getEapType(ScanResult scanResult) {
        if (scanResult.capabilities.contains("RSN-EAP")) {
            return 2;
        }
        return scanResult.capabilities.contains("WPA-EAP") ? 1 : 0;
    }

    private static int getSecurity(Context context, ScanResult scanResult) {
        boolean contains = scanResult.capabilities.contains("WEP");
        boolean contains2 = scanResult.capabilities.contains("SAE");
        boolean contains3 = scanResult.capabilities.contains("PSK");
        boolean contains4 = scanResult.capabilities.contains("EAP_SUITE_B_192");
        boolean contains5 = scanResult.capabilities.contains("EAP");
        boolean contains6 = scanResult.capabilities.contains("OWE");
        boolean contains7 = scanResult.capabilities.contains("OWE_TRANSITION");
        if (!contains2 || !contains3) {
            if (contains7) {
                if (((WifiManager) context.getSystemService("wifi")).isEnhancedOpenSupported()) {
                    return 4;
                }
                return 0;
            } else if (contains) {
                return 1;
            } else {
                if (contains2) {
                    return 5;
                }
                if (contains3) {
                    return 2;
                }
                if (contains4) {
                    return 6;
                }
                if (contains5) {
                    return 3;
                }
                return contains6 ? 4 : 0;
            }
        } else if (((WifiManager) context.getSystemService("wifi")).isWpa3SaeSupported()) {
            return 5;
        } else {
            return 2;
        }
    }

    static int getSecurity(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.allowedKeyManagement.get(8)) {
            return 5;
        }
        if (wifiConfiguration.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (wifiConfiguration.allowedKeyManagement.get(10)) {
            return 6;
        }
        if (wifiConfiguration.allowedKeyManagement.get(2) || wifiConfiguration.allowedKeyManagement.get(3)) {
            return 3;
        }
        if (wifiConfiguration.allowedKeyManagement.get(9)) {
            return 4;
        }
        return wifiConfiguration.wepKeys[0] != null ? 1 : 0;
    }

    static String removeDoubleQuotes(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        int length = str.length();
        if (length <= 1 || str.charAt(0) != '\"') {
            return str;
        }
        int i = length - 1;
        return str.charAt(i) == '\"' ? str.substring(1, i) : str;
    }

    /* access modifiers changed from: private */
    public WifiManager getWifiManager() {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        return this.mWifiManager;
    }

    private static boolean isVerboseLoggingEnabled() {
        return WifiTracker.sVerboseLogging || Log.isLoggable("SettingsLib.AccessPoint", 2);
    }

    @VisibleForTesting
    class AccessPointProvisioningCallback extends ProvisioningCallback {
        AccessPointProvisioningCallback() {
        }

        public void onProvisioningFailure(int i) {
            if (TextUtils.equals(AccessPoint.this.mOsuStatus, AccessPoint.this.mContext.getString(R$string.osu_completing_sign_up))) {
                AccessPoint accessPoint = AccessPoint.this;
                String unused = accessPoint.mOsuFailure = accessPoint.mContext.getString(R$string.osu_sign_up_failed);
            } else {
                AccessPoint accessPoint2 = AccessPoint.this;
                String unused2 = accessPoint2.mOsuFailure = accessPoint2.mContext.getString(R$string.osu_connect_failed);
            }
            String unused3 = AccessPoint.this.mOsuStatus = null;
            boolean unused4 = AccessPoint.this.mOsuProvisioningComplete = false;
            ThreadUtils.postOnMainThread(new Runnable() {
                public final void run() {
                    AccessPoint.AccessPointProvisioningCallback.this.mo13834x1703172f();
                }
            });
        }

        /* renamed from: lambda$onProvisioningFailure$0$AccessPoint$AccessPointProvisioningCallback */
        public /* synthetic */ void mo13834x1703172f() {
            AccessPoint accessPoint = AccessPoint.this;
            AccessPointListener accessPointListener = accessPoint.mAccessPointListener;
            if (accessPointListener != null) {
                accessPointListener.onAccessPointChanged(accessPoint);
            }
        }

        public void onProvisioningStatus(int i) {
            String str;
            switch (i) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    str = String.format(AccessPoint.this.mContext.getString(R$string.osu_opening_provider), new Object[]{AccessPoint.this.mOsuProvider.getFriendlyName()});
                    break;
                case 8:
                case 9:
                case 10:
                case 11:
                    str = AccessPoint.this.mContext.getString(R$string.osu_completing_sign_up);
                    break;
                default:
                    str = null;
                    break;
            }
            boolean equals = true ^ TextUtils.equals(AccessPoint.this.mOsuStatus, str);
            String unused = AccessPoint.this.mOsuStatus = str;
            String unused2 = AccessPoint.this.mOsuFailure = null;
            boolean unused3 = AccessPoint.this.mOsuProvisioningComplete = false;
            if (equals) {
                ThreadUtils.postOnMainThread(new Runnable() {
                    public final void run() {
                        AccessPoint.AccessPointProvisioningCallback.this.mo13835x8b585e0a();
                    }
                });
            }
        }

        /* renamed from: lambda$onProvisioningStatus$1$AccessPoint$AccessPointProvisioningCallback */
        public /* synthetic */ void mo13835x8b585e0a() {
            AccessPoint accessPoint = AccessPoint.this;
            AccessPointListener accessPointListener = accessPoint.mAccessPointListener;
            if (accessPointListener != null) {
                accessPointListener.onAccessPointChanged(accessPoint);
            }
        }

        public void onProvisioningComplete() {
            boolean unused = AccessPoint.this.mOsuProvisioningComplete = true;
            String unused2 = AccessPoint.this.mOsuFailure = null;
            String unused3 = AccessPoint.this.mOsuStatus = null;
            ThreadUtils.postOnMainThread(new Runnable() {
                public final void run() {
                    AccessPoint.AccessPointProvisioningCallback.this.mo13833x4793df52();
                }
            });
            WifiManager access$500 = AccessPoint.this.getWifiManager();
            PasspointConfiguration passpointConfiguration = (PasspointConfiguration) access$500.getMatchingPasspointConfigsForOsuProviders(Collections.singleton(AccessPoint.this.mOsuProvider)).get(AccessPoint.this.mOsuProvider);
            if (passpointConfiguration == null) {
                Log.e("SettingsLib.AccessPoint", "Missing PasspointConfiguration for newly provisioned network!");
                if (AccessPoint.this.mConnectListener != null) {
                    AccessPoint.this.mConnectListener.onFailure(0);
                    return;
                }
                return;
            }
            String fqdn = passpointConfiguration.getHomeSp().getFqdn();
            for (Pair pair : access$500.getAllMatchingWifiConfigs(access$500.getScanResults())) {
                WifiConfiguration wifiConfiguration = (WifiConfiguration) pair.first;
                if (TextUtils.equals(wifiConfiguration.FQDN, fqdn)) {
                    access$500.connect(new AccessPoint(AccessPoint.this.mContext, wifiConfiguration, (List) ((Map) pair.second).get(0), (List) ((Map) pair.second).get(1)).getConfig(), AccessPoint.this.mConnectListener);
                    return;
                }
            }
            if (AccessPoint.this.mConnectListener != null) {
                AccessPoint.this.mConnectListener.onFailure(0);
            }
        }

        /* renamed from: lambda$onProvisioningComplete$2$AccessPoint$AccessPointProvisioningCallback */
        public /* synthetic */ void mo13833x4793df52() {
            AccessPoint accessPoint = AccessPoint.this;
            AccessPointListener accessPointListener = accessPoint.mAccessPointListener;
            if (accessPointListener != null) {
                accessPointListener.onAccessPointChanged(accessPoint);
            }
        }
    }

    public boolean isPskSaeTransitionMode() {
        return this.mIsPskSaeTransitionMode;
    }

    public boolean isOweTransitionMode() {
        return this.mIsOweTransitionMode;
    }

    private static boolean isPskSaeTransitionMode(ScanResult scanResult) {
        return scanResult.capabilities.contains("PSK") && scanResult.capabilities.contains("SAE");
    }

    private static boolean isOweTransitionMode(ScanResult scanResult) {
        return scanResult.capabilities.contains("OWE_TRANSITION");
    }

    private boolean isSameSsidOrBssid(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }
        if (TextUtils.equals(this.ssid, scanResult.SSID)) {
            return true;
        }
        String str = scanResult.BSSID;
        return str != null && TextUtils.equals(this.bssid, str);
    }

    private boolean isSameSsidOrBssid(WifiInfo wifiInfo) {
        if (wifiInfo == null) {
            return false;
        }
        if (TextUtils.equals(this.ssid, removeDoubleQuotes(wifiInfo.getSSID()))) {
            return true;
        }
        return wifiInfo.getBSSID() != null && TextUtils.equals(this.bssid, wifiInfo.getBSSID());
    }

    private boolean isSameSsidOrBssid(AccessPoint accessPoint) {
        if (accessPoint == null) {
            return false;
        }
        if (TextUtils.equals(this.ssid, accessPoint.getSsid())) {
            return true;
        }
        return accessPoint.getBssid() != null && TextUtils.equals(this.bssid, accessPoint.getBssid());
    }
}
