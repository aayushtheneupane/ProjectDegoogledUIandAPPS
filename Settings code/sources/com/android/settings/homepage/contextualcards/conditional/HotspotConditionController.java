package com.android.settings.homepage.contextualcards.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import com.android.settings.TetherSettings;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.homepage.contextualcards.ContextualCard;
import com.android.settings.homepage.contextualcards.conditional.ConditionalContextualCard;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.havoc.config.center.C1715R;
import java.util.Objects;

public class HotspotConditionController implements ConditionalCardController {

    /* renamed from: ID */
    static final int f44ID = Objects.hash(new Object[]{"HotspotConditionController"});
    private static final IntentFilter WIFI_AP_STATE_FILTER = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
    private final Context mAppContext;
    /* access modifiers changed from: private */
    public final ConditionManager mConditionManager;
    private final Receiver mReceiver = new Receiver();
    private final WifiManager mWifiManager;

    public HotspotConditionController(Context context, ConditionManager conditionManager) {
        this.mAppContext = context;
        this.mConditionManager = conditionManager;
        this.mWifiManager = (WifiManager) context.getSystemService(WifiManager.class);
    }

    public long getId() {
        return (long) f44ID;
    }

    public boolean isDisplayable() {
        return this.mWifiManager.isWifiApEnabled();
    }

    public void onPrimaryClick(Context context) {
        new SubSettingLauncher(context).setDestination(TetherSettings.class.getName()).setSourceMetricsCategory(35).setTitleRes(C1715R.string.tether_settings_title_all).launch();
    }

    public void onActionClick() {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(this.mAppContext, "no_config_tethering", UserHandle.myUserId());
        if (checkIfRestrictionEnforced != null) {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mAppContext, checkIfRestrictionEnforced);
        } else {
            ((ConnectivityManager) this.mAppContext.getSystemService("connectivity")).stopTethering(0);
        }
    }

    public ContextualCard buildContextualCard() {
        ConditionalContextualCard.Builder builder = new ConditionalContextualCard.Builder();
        builder.setConditionId((long) f44ID);
        builder.setMetricsConstant(382);
        builder.setActionText(this.mAppContext.getText(C1715R.string.condition_turn_off));
        builder.setName(this.mAppContext.getPackageName() + "/" + this.mAppContext.getText(C1715R.string.condition_hotspot_title));
        builder.setTitleText(this.mAppContext.getText(C1715R.string.condition_hotspot_title).toString());
        builder.setSummaryText(getSsid().toString());
        builder.setIconDrawable(this.mAppContext.getDrawable(C1715R.C1717drawable.ic_hotspot));
        builder.setViewType(C1715R.layout.conditional_card_half_tile);
        return builder.build();
    }

    public void startMonitoringStateChange() {
        this.mAppContext.registerReceiver(this.mReceiver, WIFI_AP_STATE_FILTER);
    }

    public void stopMonitoringStateChange() {
        this.mAppContext.unregisterReceiver(this.mReceiver);
    }

    private CharSequence getSsid() {
        WifiConfiguration wifiApConfiguration = this.mWifiManager.getWifiApConfiguration();
        if (wifiApConfiguration == null) {
            return this.mAppContext.getText(17041402);
        }
        return wifiApConfiguration.SSID;
    }

    public class Receiver extends BroadcastReceiver {
        public Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                HotspotConditionController.this.mConditionManager.onConditionChanged();
            }
        }
    }
}
