package com.android.settings.development;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.havoc.config.center.C1715R;

public class EnableOemUnlockSettingWarningDialog extends InstrumentedDialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    public int getMetricsCategory() {
        return 1220;
    }

    public static void show(Fragment fragment) {
        FragmentManager supportFragmentManager = fragment.getActivity().getSupportFragmentManager();
        if (supportFragmentManager.findFragmentByTag("EnableOemUnlockDlg") == null) {
            EnableOemUnlockSettingWarningDialog enableOemUnlockSettingWarningDialog = new EnableOemUnlockSettingWarningDialog();
            enableOemUnlockSettingWarningDialog.setTargetFragment(fragment, 0);
            enableOemUnlockSettingWarningDialog.show(supportFragmentManager, "EnableOemUnlockDlg");
        }
    }

    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle((int) C1715R.string.confirm_enable_oem_unlock_title);
        builder.setMessage((int) C1715R.string.confirm_enable_oem_unlock_text);
        builder.setPositiveButton((int) C1715R.string.enable_text, (DialogInterface.OnClickListener) this);
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) this);
        return builder.create();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        OemUnlockDialogHost oemUnlockDialogHost = (OemUnlockDialogHost) getTargetFragment();
        if (oemUnlockDialogHost != null) {
            if (i == -1) {
                oemUnlockDialogHost.onOemUnlockDialogConfirmed();
            } else {
                oemUnlockDialogHost.onOemUnlockDialogDismissed();
            }
        }
    }

    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        OemUnlockDialogHost oemUnlockDialogHost = (OemUnlockDialogHost) getTargetFragment();
        if (oemUnlockDialogHost != null) {
            oemUnlockDialogHost.onOemUnlockDialogDismissed();
        }
    }
}
