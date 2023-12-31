package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.content.res.Resources;
import android.icu.text.ListFormatter;
import android.os.Parcel;
import android.os.Parcelable;
import com.android.settings.Utils;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RestrictAppTip extends BatteryTip {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public BatteryTip createFromParcel(Parcel parcel) {
            return new RestrictAppTip(parcel);
        }

        public BatteryTip[] newArray(int i) {
            return new RestrictAppTip[i];
        }
    };
    private List<AppInfo> mRestrictAppList;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RestrictAppTip(int i, List<AppInfo> list) {
        super(1, i, i == 0);
        this.mRestrictAppList = list;
        this.mNeedUpdate = false;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RestrictAppTip(int i, AppInfo appInfo) {
        super(1, i, i == 0);
        this.mRestrictAppList = new ArrayList();
        this.mRestrictAppList.add(appInfo);
        this.mNeedUpdate = false;
    }

    RestrictAppTip(Parcel parcel) {
        super(parcel);
        this.mRestrictAppList = parcel.createTypedArrayList(AppInfo.CREATOR);
    }

    public CharSequence getTitle(Context context) {
        int size = this.mRestrictAppList.size();
        String applicationLabel = size > 0 ? Utils.getApplicationLabel(context, this.mRestrictAppList.get(0).packageName) : "";
        Resources resources = context.getResources();
        if (this.mState == 1) {
            return resources.getQuantityString(C1715R.plurals.battery_tip_restrict_handled_title, size, new Object[]{applicationLabel, Integer.valueOf(size)});
        }
        return resources.getQuantityString(C1715R.plurals.battery_tip_restrict_title, size, new Object[]{Integer.valueOf(size)});
    }

    public CharSequence getSummary(Context context) {
        int size = this.mRestrictAppList.size();
        return context.getResources().getQuantityString(this.mState == 1 ? C1715R.plurals.battery_tip_restrict_handled_summary : C1715R.plurals.battery_tip_restrict_summary, size, new Object[]{size > 0 ? Utils.getApplicationLabel(context, this.mRestrictAppList.get(0).packageName) : "", Integer.valueOf(size)});
    }

    public int getIconId() {
        return this.mState == 1 ? C1715R.C1717drawable.ic_perm_device_information_green_24dp : C1715R.C1717drawable.ic_battery_alert_24dp;
    }

    public void updateState(BatteryTip batteryTip) {
        int i = batteryTip.mState;
        if (i == 0) {
            this.mState = 0;
            this.mRestrictAppList = ((RestrictAppTip) batteryTip).mRestrictAppList;
            this.mShowDialog = true;
        } else if (this.mState == 0 && i == 2) {
            this.mState = 1;
            this.mShowDialog = false;
        } else {
            this.mState = batteryTip.getState();
            this.mShowDialog = batteryTip.shouldShowDialog();
            this.mRestrictAppList = ((RestrictAppTip) batteryTip).mRestrictAppList;
        }
    }

    public void sanityCheck(Context context) {
        super.sanityCheck(context);
        this.mRestrictAppList.removeIf(AppLabelPredicate.getInstance(context));
        if (this.mRestrictAppList.isEmpty()) {
            this.mState = 2;
        }
    }

    public void log(Context context, MetricsFeatureProvider metricsFeatureProvider) {
        metricsFeatureProvider.action(context, 1347, this.mState);
        if (this.mState == 0) {
            int size = this.mRestrictAppList.size();
            for (int i = 0; i < size; i++) {
                AppInfo appInfo = this.mRestrictAppList.get(i);
                Iterator<Integer> it = appInfo.anomalyTypes.iterator();
                while (it.hasNext()) {
                    metricsFeatureProvider.action(0, 1353, 0, appInfo.packageName, it.next().intValue());
                }
            }
        }
    }

    public List<AppInfo> getRestrictAppList() {
        return this.mRestrictAppList;
    }

    public CharSequence getRestrictAppsString(Context context) {
        ArrayList arrayList = new ArrayList();
        int size = this.mRestrictAppList.size();
        for (int i = 0; i < size; i++) {
            arrayList.add(Utils.getApplicationLabel(context, this.mRestrictAppList.get(i).packageName));
        }
        return ListFormatter.getInstance().format(arrayList);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" {");
        int size = this.mRestrictAppList.size();
        for (int i = 0; i < size; i++) {
            sb.append(" " + this.mRestrictAppList.get(i).toString() + " ");
        }
        sb.append('}');
        return sb.toString();
    }

    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeTypedList(this.mRestrictAppList);
    }
}
