package com.android.settings.fuelgauge;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.fuelgauge.PowerWhitelistBackend;
import com.havoc.config.center.C1715R;

public class HighPowerDetail extends InstrumentedDialogFragment implements DialogInterface.OnClickListener, View.OnClickListener {
    PowerWhitelistBackend mBackend;
    BatteryUtils mBatteryUtils;
    private boolean mDefaultOn;
    boolean mIsEnabled;
    private CharSequence mLabel;
    private Checkable mOptionOff;
    private Checkable mOptionOn;
    String mPackageName;
    int mPackageUid;

    public int getMetricsCategory() {
        return 540;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Context context = getContext();
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        this.mBackend = PowerWhitelistBackend.getInstance(context);
        this.mPackageName = getArguments().getString("package");
        this.mPackageUid = getArguments().getInt("uid");
        PackageManager packageManager = context.getPackageManager();
        boolean z = false;
        try {
            this.mLabel = packageManager.getApplicationInfo(this.mPackageName, 0).loadLabel(packageManager);
        } catch (PackageManager.NameNotFoundException unused) {
            this.mLabel = this.mPackageName;
        }
        this.mDefaultOn = getArguments().getBoolean("default_on");
        if (this.mDefaultOn || this.mBackend.isWhitelisted(this.mPackageName)) {
            z = true;
        }
        this.mIsEnabled = z;
    }

    public Checkable setup(View view, boolean z) {
        ((TextView) view.findViewById(16908310)).setText(z ? C1715R.string.ignore_optimizations_on : C1715R.string.ignore_optimizations_off);
        ((TextView) view.findViewById(16908304)).setText(z ? C1715R.string.ignore_optimizations_on_desc : C1715R.string.ignore_optimizations_off_desc);
        view.setClickable(true);
        view.setOnClickListener(this);
        if (!z && this.mBackend.isSysWhitelisted(this.mPackageName)) {
            view.setEnabled(false);
        }
        return (Checkable) view;
    }

    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(this.mLabel);
        builder.setNegativeButton((int) C1715R.string.cancel, (DialogInterface.OnClickListener) null);
        builder.setView((int) C1715R.layout.ignore_optimizations_content);
        if (!this.mBackend.isSysWhitelisted(this.mPackageName)) {
            builder.setPositiveButton((int) C1715R.string.done, (DialogInterface.OnClickListener) this);
        }
        return builder.create();
    }

    public void onStart() {
        super.onStart();
        this.mOptionOn = setup(getDialog().findViewById(C1715R.C1718id.ignore_on), true);
        this.mOptionOff = setup(getDialog().findViewById(C1715R.C1718id.ignore_off), false);
        updateViews();
    }

    private void updateViews() {
        this.mOptionOn.setChecked(this.mIsEnabled);
        this.mOptionOff.setChecked(!this.mIsEnabled);
    }

    public void onClick(View view) {
        if (view == this.mOptionOn) {
            this.mIsEnabled = true;
            updateViews();
        } else if (view == this.mOptionOff) {
            this.mIsEnabled = false;
            updateViews();
        }
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        boolean z;
        if (i == -1 && (z = this.mIsEnabled) != this.mBackend.isWhitelisted(this.mPackageName)) {
            logSpecialPermissionChange(z, this.mPackageName, getContext());
            if (z) {
                this.mBatteryUtils.setForceAppStandby(this.mPackageUid, this.mPackageName, 0);
                this.mBackend.addApp(this.mPackageName);
                return;
            }
            this.mBackend.removeApp(this.mPackageName);
        }
    }

    static void logSpecialPermissionChange(boolean z, String str, Context context) {
        FeatureFactory.getFactory(context).getMetricsFeatureProvider().action(context, z ? 765 : 764, str);
    }

    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null && targetFragment.getActivity() != null) {
            targetFragment.onActivityResult(getTargetRequestCode(), 0, (Intent) null);
        }
    }

    public static CharSequence getSummary(Context context, ApplicationsState.AppEntry appEntry) {
        return getSummary(context, appEntry.info.packageName);
    }

    public static CharSequence getSummary(Context context, String str) {
        return getSummary(context, PowerWhitelistBackend.getInstance(context), str);
    }

    static CharSequence getSummary(Context context, PowerWhitelistBackend powerWhitelistBackend, String str) {
        return context.getString((powerWhitelistBackend.isSysWhitelisted(str) || powerWhitelistBackend.isDefaultActiveApp(str)) ? C1715R.string.high_power_system : powerWhitelistBackend.isWhitelisted(str) ? C1715R.string.high_power_on : C1715R.string.high_power_off);
    }

    public static void show(Fragment fragment, int i, String str, int i2) {
        HighPowerDetail highPowerDetail = new HighPowerDetail();
        Bundle bundle = new Bundle();
        bundle.putString("package", str);
        bundle.putInt("uid", i);
        highPowerDetail.setArguments(bundle);
        highPowerDetail.setTargetFragment(fragment, i2);
        highPowerDetail.show(fragment.getFragmentManager(), HighPowerDetail.class.getSimpleName());
    }
}
