package com.android.settings.deviceinfo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeRecord;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.search.actionbar.SearchMenuController;
import com.havoc.config.center.C1715R;

public class PrivateVolumeForget extends InstrumentedFragment {
    static final String TAG_FORGET_CONFIRM = "forget_confirm";
    private final View.OnClickListener mConfirmListener = new View.OnClickListener() {
        public void onClick(View view) {
            PrivateVolumeForget privateVolumeForget = PrivateVolumeForget.this;
            ForgetConfirmFragment.show(privateVolumeForget, privateVolumeForget.mRecord.getFsUuid());
        }
    };
    /* access modifiers changed from: private */
    public VolumeRecord mRecord;

    public int getMetricsCategory() {
        return 42;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        SearchMenuController.init((InstrumentedFragment) this);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        StorageManager storageManager = (StorageManager) getActivity().getSystemService(StorageManager.class);
        String string = getArguments().getString("android.os.storage.extra.FS_UUID");
        if (string == null) {
            getActivity().finish();
            return null;
        }
        this.mRecord = storageManager.findRecordByUuid(string);
        if (this.mRecord == null) {
            getActivity().finish();
            return null;
        }
        View inflate = layoutInflater.inflate(C1715R.layout.storage_internal_forget, viewGroup, false);
        ((TextView) inflate.findViewById(C1715R.C1718id.body)).setText(TextUtils.expandTemplate(getText(C1715R.string.storage_internal_forget_details), new CharSequence[]{this.mRecord.getNickname()}));
        ((Button) inflate.findViewById(C1715R.C1718id.confirm)).setOnClickListener(this.mConfirmListener);
        return inflate;
    }

    public static class ForgetConfirmFragment extends InstrumentedDialogFragment {
        public int getMetricsCategory() {
            return 559;
        }

        public static void show(Fragment fragment, String str) {
            Bundle bundle = new Bundle();
            bundle.putString("android.os.storage.extra.FS_UUID", str);
            ForgetConfirmFragment forgetConfirmFragment = new ForgetConfirmFragment();
            forgetConfirmFragment.setArguments(bundle);
            forgetConfirmFragment.setTargetFragment(fragment, 0);
            forgetConfirmFragment.show(fragment.getFragmentManager(), PrivateVolumeForget.TAG_FORGET_CONFIRM);
        }

        public Dialog onCreateDialog(Bundle bundle) {
            FragmentActivity activity = getActivity();
            final StorageManager storageManager = (StorageManager) activity.getSystemService(StorageManager.class);
            final String string = getArguments().getString("android.os.storage.extra.FS_UUID");
            VolumeRecord findRecordByUuid = storageManager.findRecordByUuid(string);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(TextUtils.expandTemplate(getText(C1715R.string.storage_internal_forget_confirm_title), new CharSequence[]{findRecordByUuid.getNickname()}));
            builder.setMessage(TextUtils.expandTemplate(getText(C1715R.string.storage_internal_forget_confirm), new CharSequence[]{findRecordByUuid.getNickname()}));
            builder.setPositiveButton((int) C1715R.string.storage_menu_forget, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    storageManager.forgetVolume(string);
                    ForgetConfirmFragment.this.getActivity().finish();
                }
            });
            builder.setNegativeButton((int) C1715R.string.cancel, (DialogInterface.OnClickListener) null);
            return builder.create();
        }
    }
}
