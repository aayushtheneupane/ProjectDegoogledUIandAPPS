package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.havoc.config.center.C1715R;
import java.util.Objects;

public class StorageWizardMigrateConfirm extends StorageWizardBase {
    private MigrateEstimateTask mEstimate;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C1715R.layout.storage_wizard_generic);
        if (this.mVolume == null) {
            this.mVolume = findFirstVolume(1);
        }
        if (getPackageManager().getPrimaryStorageCurrentVolume() == null || this.mVolume == null) {
            Log.d("StorageSettings", "Missing either source or target volume");
            finish();
            return;
        }
        setIcon(C1715R.C1717drawable.ic_swap_horiz);
        setHeaderText(C1715R.string.storage_wizard_migrate_v2_title, getDiskShortDescription());
        setBodyText(C1715R.string.memory_calculating_size, new CharSequence[0]);
        setAuxChecklist();
        this.mEstimate = new MigrateEstimateTask(this) {
            public void onPostExecute(String str, String str2) {
                StorageWizardMigrateConfirm storageWizardMigrateConfirm = StorageWizardMigrateConfirm.this;
                storageWizardMigrateConfirm.setBodyText(C1715R.string.storage_wizard_migrate_v2_body, storageWizardMigrateConfirm.getDiskDescription(), str, str2);
            }
        };
        this.mEstimate.copyFrom(getIntent());
        this.mEstimate.execute(new Void[0]);
        setBackButtonText(C1715R.string.storage_wizard_migrate_v2_later, new CharSequence[0]);
        setNextButtonText(C1715R.string.storage_wizard_migrate_v2_now, new CharSequence[0]);
    }

    public void onNavigateBack(View view) {
        FeatureFactory.getFactory(this).getMetricsFeatureProvider().action((Context) this, 1413, (Pair<Integer, Object>[]) new Pair[0]);
        if (this.mDisk != null) {
            Intent intent = new Intent(this, StorageWizardReady.class);
            intent.putExtra("migrate_skip", true);
            startActivity(intent);
            return;
        }
        finishAffinity();
    }

    public void onNavigateNext(View view) {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            for (UserInfo userInfo : ((UserManager) getSystemService(UserManager.class)).getUsers()) {
                if (!StorageManager.isUserKeyUnlocked(userInfo.id)) {
                    Log.d("StorageSettings", "User " + userInfo.id + " is currently locked; requesting unlock");
                    new ChooseLockSettingsHelper(this).launchConfirmationActivityForAnyUser(100, (CharSequence) null, (CharSequence) null, TextUtils.expandTemplate(getText(C1715R.string.storage_wizard_move_unlock), new CharSequence[]{userInfo.name}), userInfo.id);
                    return;
                }
            }
        }
        try {
            int movePrimaryStorage = getPackageManager().movePrimaryStorage(this.mVolume);
            FeatureFactory.getFactory(this).getMetricsFeatureProvider().action((Context) this, 1412, (Pair<Integer, Object>[]) new Pair[0]);
            Intent intent = new Intent(this, StorageWizardMigrateProgress.class);
            intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
            intent.putExtra("android.content.pm.extra.MOVE_ID", movePrimaryStorage);
            startActivity(intent);
            finishAffinity();
        } catch (IllegalArgumentException e) {
            if (Objects.equals(this.mVolume.getFsUuid(), ((StorageManager) getSystemService("storage")).getPrimaryStorageVolume().getUuid())) {
                Intent intent2 = new Intent(this, StorageWizardReady.class);
                intent2.putExtra("android.os.storage.extra.DISK_ID", getIntent().getStringExtra("android.os.storage.extra.DISK_ID"));
                startActivity(intent2);
                finishAffinity();
                return;
            }
            throw e;
        } catch (IllegalStateException unused) {
            Toast.makeText(this, getString(C1715R.string.another_migration_already_in_progress), 1).show();
            finishAffinity();
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i != 100) {
            super.onActivityResult(i, i2, intent);
        } else if (i2 == -1) {
            onNavigateNext((View) null);
        } else {
            Log.w("StorageSettings", "Failed to confirm credentials");
        }
    }
}
