package com.android.settings.network;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.ArraySet;
import androidx.collection.ArrayMap;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import com.android.settings.network.MobileDataEnabledListener;
import com.android.settings.network.SubscriptionsChangeListener;
import com.android.settings.network.telephony.DataConnectivityListener;
import com.android.settings.network.telephony.MobileNetworkActivity;
import com.android.settings.network.telephony.MobileNetworkUtils;
import com.android.settings.network.telephony.SignalStrengthListener;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.net.SignalStrengthUtil;
import com.havoc.config.center.C1715R;
import java.util.Collections;
import java.util.Map;

public class SubscriptionsPreferenceController extends AbstractPreferenceController implements LifecycleObserver, SubscriptionsChangeListener.SubscriptionsChangeListenerClient, MobileDataEnabledListener.Client, DataConnectivityListener.Client, SignalStrengthListener.Callback {
    private DataConnectivityListener mConnectivityListener;
    private ConnectivityManager mConnectivityManager = ((ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class));
    private MobileDataEnabledListener mDataEnabledListener;
    private SubscriptionManager mManager;
    private PreferenceGroup mPreferenceGroup;
    private String mPreferenceGroupKey;
    private SignalStrengthListener mSignalStrengthListener;
    private int mStartOrder;
    private Map<Integer, Preference> mSubscriptionPreferences = new ArrayMap();
    private SubscriptionsChangeListener mSubscriptionsListener;
    private UpdateListener mUpdateListener;

    public interface UpdateListener {
        void onChildrenUpdated();
    }

    public String getPreferenceKey() {
        return null;
    }

    public SubscriptionsPreferenceController(Context context, Lifecycle lifecycle, UpdateListener updateListener, String str, int i) {
        super(context);
        this.mUpdateListener = updateListener;
        this.mPreferenceGroupKey = str;
        this.mStartOrder = i;
        this.mManager = (SubscriptionManager) context.getSystemService(SubscriptionManager.class);
        this.mSubscriptionsListener = new SubscriptionsChangeListener(context, this);
        this.mDataEnabledListener = new MobileDataEnabledListener(context, this);
        this.mConnectivityListener = new DataConnectivityListener(context, this);
        this.mSignalStrengthListener = new SignalStrengthListener(context, this);
        lifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        this.mSubscriptionsListener.start();
        this.mDataEnabledListener.start(SubscriptionManager.getDefaultDataSubscriptionId());
        this.mConnectivityListener.start();
        this.mSignalStrengthListener.resume();
        update();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        this.mSubscriptionsListener.stop();
        this.mDataEnabledListener.stop();
        this.mConnectivityListener.stop();
        this.mSignalStrengthListener.pause();
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        this.mPreferenceGroup = (PreferenceGroup) preferenceScreen.findPreference(this.mPreferenceGroupKey);
        update();
    }

    private void update() {
        if (this.mPreferenceGroup != null) {
            if (!isAvailable()) {
                for (Preference removePreference : this.mSubscriptionPreferences.values()) {
                    this.mPreferenceGroup.removePreference(removePreference);
                }
                this.mSubscriptionPreferences.clear();
                this.mSignalStrengthListener.updateSubscriptionIds(Collections.emptySet());
                this.mUpdateListener.onChildrenUpdated();
                return;
            }
            Map<Integer, Preference> map = this.mSubscriptionPreferences;
            this.mSubscriptionPreferences = new ArrayMap();
            int i = this.mStartOrder;
            ArraySet arraySet = new ArraySet();
            int defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
            for (SubscriptionInfo next : SubscriptionUtil.getActiveSubscriptions(this.mManager)) {
                int subscriptionId = next.getSubscriptionId();
                arraySet.add(Integer.valueOf(subscriptionId));
                Preference remove = map.remove(Integer.valueOf(subscriptionId));
                if (remove == null) {
                    remove = new Preference(this.mPreferenceGroup.getContext());
                    this.mPreferenceGroup.addPreference(remove);
                }
                remove.setTitle(next.getDisplayName());
                boolean z = subscriptionId == defaultDataSubscriptionId;
                remove.setSummary((CharSequence) getSummary(subscriptionId, z));
                setIcon(remove, subscriptionId, z);
                remove.setOrder(i);
                remove.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(subscriptionId) {
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final boolean onPreferenceClick(Preference preference) {
                        return SubscriptionsPreferenceController.this.lambda$update$0$SubscriptionsPreferenceController(this.f$1, preference);
                    }
                });
                this.mSubscriptionPreferences.put(Integer.valueOf(subscriptionId), remove);
                i++;
            }
            this.mSignalStrengthListener.updateSubscriptionIds(arraySet);
            for (Preference removePreference2 : map.values()) {
                this.mPreferenceGroup.removePreference(removePreference2);
            }
            this.mUpdateListener.onChildrenUpdated();
        }
    }

    public /* synthetic */ boolean lambda$update$0$SubscriptionsPreferenceController(int i, Preference preference) {
        Intent intent = new Intent(this.mContext, MobileNetworkActivity.class);
        intent.putExtra("android.provider.extra.SUB_ID", i);
        this.mContext.startActivity(intent);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldInflateSignalStrength(int i) {
        return SignalStrengthUtil.shouldInflateSignalStrength(this.mContext, i);
    }

    /* access modifiers changed from: package-private */
    public void setIcon(Preference preference, int i, boolean z) {
        int i2;
        TelephonyManager createForSubscriptionId = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).createForSubscriptionId(i);
        SignalStrength signalStrength = createForSubscriptionId.getSignalStrength();
        boolean z2 = false;
        if (signalStrength == null) {
            i2 = 0;
        } else {
            i2 = signalStrength.getLevel();
        }
        int i3 = 5;
        if (shouldInflateSignalStrength(i)) {
            i2++;
            i3 = 6;
        }
        if (!z || !createForSubscriptionId.isDataEnabled()) {
            z2 = true;
        }
        preference.setIcon(getIcon(i2, i3, z2));
    }

    /* access modifiers changed from: package-private */
    public Drawable getIcon(int i, int i2, boolean z) {
        return MobileNetworkUtils.getSignalStrengthIcon(this.mContext, i, i2, 0, z);
    }

    private boolean activeNetworkIsCellular() {
        NetworkCapabilities networkCapabilities;
        Network activeNetwork = this.mConnectivityManager.getActiveNetwork();
        if (activeNetwork == null || (networkCapabilities = this.mConnectivityManager.getNetworkCapabilities(activeNetwork)) == null) {
            return false;
        }
        return networkCapabilities.hasTransport(0);
    }

    /* access modifiers changed from: protected */
    public String getSummary(int i, boolean z) {
        String str;
        int defaultVoiceSubscriptionId = SubscriptionManager.getDefaultVoiceSubscriptionId();
        int defaultSmsSubscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId();
        String str2 = null;
        if (i == defaultVoiceSubscriptionId && i == defaultSmsSubscriptionId) {
            str = this.mContext.getString(C1715R.string.default_for_calls_and_sms);
        } else if (i == defaultVoiceSubscriptionId) {
            str = this.mContext.getString(C1715R.string.default_for_calls);
        } else {
            str = i == defaultSmsSubscriptionId ? this.mContext.getString(C1715R.string.default_for_sms) : null;
        }
        if (z) {
            boolean isDataEnabled = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).createForSubscriptionId(i).isDataEnabled();
            if (isDataEnabled && activeNetworkIsCellular()) {
                str2 = this.mContext.getString(C1715R.string.mobile_data_active);
            } else if (!isDataEnabled) {
                str2 = this.mContext.getString(C1715R.string.mobile_data_off);
            } else {
                str2 = this.mContext.getString(C1715R.string.default_for_mobile_data);
            }
        }
        if (str != null && str2 != null) {
            return String.join(System.lineSeparator(), new CharSequence[]{str, str2});
        } else if (str != null) {
            return str;
        } else {
            if (str2 != null) {
                return str2;
            }
            return this.mContext.getString(C1715R.string.subscription_available);
        }
    }

    public boolean isAvailable() {
        if (!this.mSubscriptionsListener.isAirplaneModeOn() && SubscriptionUtil.getActiveSubscriptions(this.mManager).size() >= 2) {
            return true;
        }
        return false;
    }

    public void onAirplaneModeChanged(boolean z) {
        update();
    }

    public void onSubscriptionsChanged() {
        int defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (defaultDataSubscriptionId != this.mDataEnabledListener.getSubId()) {
            this.mDataEnabledListener.stop();
            this.mDataEnabledListener.start(defaultDataSubscriptionId);
        }
        update();
    }

    public void onMobileDataEnabledChange() {
        update();
    }

    public void onDataConnectivityChange() {
        update();
    }

    public void onSignalStrengthChanged() {
        update();
    }
}
