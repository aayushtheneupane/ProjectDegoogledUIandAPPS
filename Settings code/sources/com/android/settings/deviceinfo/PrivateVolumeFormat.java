package com.android.settings.deviceinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.core.InstrumentedFragment;
import com.havoc.config.center.C1715R;

public class PrivateVolumeFormat extends InstrumentedFragment {
    private final View.OnClickListener mConfirmListener = new View.OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(PrivateVolumeFormat.this.getActivity(), StorageWizardFormatProgress.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", PrivateVolumeFormat.this.mDisk.getId());
            intent.putExtra("format_private", false);
            intent.putExtra("format_forget_uuid", PrivateVolumeFormat.this.mVolume.getFsUuid());
            PrivateVolumeFormat.this.startActivity(intent);
            PrivateVolumeFormat.this.getActivity().finish();
        }
    };
    /* access modifiers changed from: private */
    public DiskInfo mDisk;
    /* access modifiers changed from: private */
    public VolumeInfo mVolume;

    public int getMetricsCategory() {
        return 42;
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        StorageManager storageManager = (StorageManager) getActivity().getSystemService(StorageManager.class);
        this.mVolume = storageManager.findVolumeById(getArguments().getString("android.os.storage.extra.VOLUME_ID"));
        this.mDisk = storageManager.findDiskById(this.mVolume.getDiskId());
        View inflate = layoutInflater.inflate(C1715R.layout.storage_internal_format, viewGroup, false);
        ((TextView) inflate.findViewById(C1715R.C1718id.body)).setText(TextUtils.expandTemplate(getText(C1715R.string.storage_internal_format_details), new CharSequence[]{this.mDisk.getDescription()}));
        ((Button) inflate.findViewById(C1715R.C1718id.confirm)).setOnClickListener(this.mConfirmListener);
        return inflate;
    }
}
