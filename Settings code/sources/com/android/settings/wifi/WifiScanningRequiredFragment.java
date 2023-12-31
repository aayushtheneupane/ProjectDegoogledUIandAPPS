package com.android.settings.wifi;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.HelpUtils;
import com.havoc.config.center.C1715R;

public class WifiScanningRequiredFragment extends InstrumentedDialogFragment implements DialogInterface.OnClickListener {
    public int getMetricsCategory() {
        return 1373;
    }

    public static WifiScanningRequiredFragment newInstance() {
        return new WifiScanningRequiredFragment();
    }

    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle((int) C1715R.string.wifi_settings_scanning_required_title);
        builder.setView((int) C1715R.layout.wifi_settings_scanning_required_view);
        builder.setPositiveButton((int) C1715R.string.wifi_settings_scanning_required_turn_on, (DialogInterface.OnClickListener) this);
        builder.setNegativeButton((int) C1715R.string.cancel, (DialogInterface.OnClickListener) null);
        addButtonIfNeeded(builder);
        return builder.create();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        Context context = getContext();
        ContentResolver contentResolver = context.getContentResolver();
        if (i == -3) {
            openHelpPage();
        } else if (i == -1) {
            Settings.Global.putInt(contentResolver, "wifi_scan_always_enabled", 1);
            Toast.makeText(context, context.getString(C1715R.string.wifi_settings_scanning_required_enabled), 0).show();
            getTargetFragment().onActivityResult(getTargetRequestCode(), -1, (Intent) null);
        }
    }

    /* access modifiers changed from: package-private */
    public void addButtonIfNeeded(AlertDialog.Builder builder) {
        if (!TextUtils.isEmpty(getContext().getString(C1715R.string.help_uri_wifi_scanning_required))) {
            builder.setNeutralButton(C1715R.string.learn_more, this);
        }
    }

    private void openHelpPage() {
        Intent helpIntent = getHelpIntent(getContext());
        if (helpIntent != null) {
            try {
                startActivity(helpIntent);
            } catch (ActivityNotFoundException unused) {
                Log.e("WifiScanReqFrag", "Activity was not found for intent, " + helpIntent.toString());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Intent getHelpIntent(Context context) {
        return HelpUtils.getHelpIntent(context, context.getString(C1715R.string.help_uri_wifi_scanning_required), context.getClass().getName());
    }
}
