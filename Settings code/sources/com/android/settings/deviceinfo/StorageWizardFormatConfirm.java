package com.android.settings.deviceinfo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.havoc.config.center.C1715R;

public class StorageWizardFormatConfirm extends InstrumentedDialogFragment {
    public int getMetricsCategory() {
        return 1375;
    }

    public static void showPublic(FragmentActivity fragmentActivity, String str) {
        show(fragmentActivity, str, (String) null, false);
    }

    public static void showPrivate(FragmentActivity fragmentActivity, String str) {
        show(fragmentActivity, str, (String) null, true);
    }

    private static void show(FragmentActivity fragmentActivity, String str, String str2, boolean z) {
        Bundle bundle = new Bundle();
        bundle.putString("android.os.storage.extra.DISK_ID", str);
        bundle.putString("format_forget_uuid", str2);
        bundle.putBoolean("format_private", z);
        StorageWizardFormatConfirm storageWizardFormatConfirm = new StorageWizardFormatConfirm();
        storageWizardFormatConfirm.setArguments(bundle);
        storageWizardFormatConfirm.show(fragmentActivity.getSupportFragmentManager(), "format_warning");
    }

    public Dialog onCreateDialog(Bundle bundle) {
        Context context = getContext();
        Bundle arguments = getArguments();
        String string = arguments.getString("android.os.storage.extra.DISK_ID");
        String string2 = arguments.getString("format_forget_uuid");
        boolean z = arguments.getBoolean("format_private", false);
        DiskInfo findDiskById = ((StorageManager) context.getSystemService(StorageManager.class)).findDiskById(string);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(TextUtils.expandTemplate(getText(C1715R.string.storage_wizard_format_confirm_v2_title), new CharSequence[]{findDiskById.getShortDescription()}));
        builder.setMessage(TextUtils.expandTemplate(getText(C1715R.string.storage_wizard_format_confirm_v2_body), new CharSequence[]{findDiskById.getDescription(), findDiskById.getShortDescription(), findDiskById.getShortDescription()}));
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null);
        builder.setPositiveButton(TextUtils.expandTemplate(getText(C1715R.string.storage_wizard_format_confirm_v2_action), new CharSequence[]{findDiskById.getShortDescription()}), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener(context, string, string2, z) {
            private final /* synthetic */ Context f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                StorageWizardFormatConfirm.lambda$onCreateDialog$0(this.f$0, this.f$1, this.f$2, this.f$3, dialogInterface, i);
            }
        });
        return builder.create();
    }

    static /* synthetic */ void lambda$onCreateDialog$0(Context context, String str, String str2, boolean z, DialogInterface dialogInterface, int i) {
        Intent intent = new Intent(context, StorageWizardFormatProgress.class);
        intent.putExtra("android.os.storage.extra.DISK_ID", str);
        intent.putExtra("format_forget_uuid", str2);
        intent.putExtra("format_private", z);
        context.startActivity(intent);
    }
}
