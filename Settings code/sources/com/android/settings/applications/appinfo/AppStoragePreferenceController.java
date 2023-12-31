package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.format.Formatter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.Preference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.applications.AppStorageSettings;
import com.android.settings.applications.FetchPackageStorageAsyncLoader;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.StorageStatsSource;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.havoc.config.center.C1715R;

public class AppStoragePreferenceController extends AppInfoPreferenceControllerBase implements LoaderManager.LoaderCallbacks<StorageStatsSource.AppStorageStats>, LifecycleObserver, OnResume, OnPause {
    private StorageStatsSource.AppStorageStats mLastResult;

    public void onLoaderReset(Loader<StorageStatsSource.AppStorageStats> loader) {
    }

    public AppStoragePreferenceController(Context context, String str) {
        super(context, str);
    }

    public void updateState(Preference preference) {
        ApplicationInfo applicationInfo;
        ApplicationsState.AppEntry appEntry = this.mParent.getAppEntry();
        if (appEntry != null && (applicationInfo = appEntry.info) != null) {
            preference.setSummary(getStorageSummary(this.mLastResult, (applicationInfo.flags & 262144) != 0));
        }
    }

    public void onResume() {
        this.mParent.getLoaderManager().restartLoader(3, Bundle.EMPTY, this);
    }

    public void onPause() {
        this.mParent.getLoaderManager().destroyLoader(3);
    }

    /* access modifiers changed from: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return AppStorageSettings.class;
    }

    /* access modifiers changed from: package-private */
    public CharSequence getStorageSummary(StorageStatsSource.AppStorageStats appStorageStats, boolean z) {
        if (appStorageStats == null) {
            return this.mContext.getText(C1715R.string.computing_size);
        }
        String string = this.mContext.getString(z ? C1715R.string.storage_type_external : C1715R.string.storage_type_internal);
        Context context = this.mContext;
        return context.getString(C1715R.string.storage_summary_format, new Object[]{Formatter.formatFileSize(context, appStorageStats.getTotalBytes()), Utils.normalizeTitleCaseIfRequired(this.mContext, string.toString())});
    }

    public Loader<StorageStatsSource.AppStorageStats> onCreateLoader(int i, Bundle bundle) {
        Context context = this.mContext;
        return new FetchPackageStorageAsyncLoader(context, new StorageStatsSource(context), this.mParent.getAppEntry().info, UserHandle.of(UserHandle.myUserId()));
    }

    public void onLoadFinished(Loader<StorageStatsSource.AppStorageStats> loader, StorageStatsSource.AppStorageStats appStorageStats) {
        this.mLastResult = appStorageStats;
        updateState(this.mPreference);
    }
}
