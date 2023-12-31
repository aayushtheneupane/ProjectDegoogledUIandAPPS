package com.android.settings.applications.appops;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import com.android.settings.applications.appops.AppOpsState;
import com.havoc.config.center.C1715R;
import java.util.List;

public class AppOpsCategory extends ListFragment implements LoaderManager.LoaderCallbacks<List<AppOpsState.AppOpEntry>> {
    AppListAdapter mAdapter;
    AppOpsState mState;

    public AppOpsCategory() {
    }

    public AppOpsCategory(AppOpsState.OpsTemplate opsTemplate) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("template", opsTemplate);
        setArguments(bundle);
    }

    public static class InterestingConfigChanges {
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        /* access modifiers changed from: package-private */
        public boolean applyNewConfig(Resources resources) {
            int updateFrom = this.mLastConfiguration.updateFrom(resources.getConfiguration());
            if (!(this.mLastDensity != resources.getDisplayMetrics().densityDpi) && (updateFrom & 772) == 0) {
                return false;
            }
            this.mLastDensity = resources.getDisplayMetrics().densityDpi;
            return true;
        }
    }

    public static class PackageIntentReceiver extends BroadcastReceiver {
        final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader appListLoader) {
            this.mLoader = appListLoader;
            IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            intentFilter.addDataScheme("package");
            this.mLoader.getContext().registerReceiver(this, intentFilter);
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
            intentFilter2.addAction("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
            this.mLoader.getContext().registerReceiver(this, intentFilter2);
        }

        public void onReceive(Context context, Intent intent) {
            this.mLoader.onContentChanged();
        }
    }

    public static class AppListLoader extends AsyncTaskLoader<List<AppOpsState.AppOpEntry>> {
        List<AppOpsState.AppOpEntry> mApps;
        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        PackageIntentReceiver mPackageObserver;
        final AppOpsState mState;
        final AppOpsState.OpsTemplate mTemplate;

        /* access modifiers changed from: protected */
        public void onReleaseResources(List<AppOpsState.AppOpEntry> list) {
        }

        public AppListLoader(Context context, AppOpsState appOpsState, AppOpsState.OpsTemplate opsTemplate) {
            super(context);
            this.mState = appOpsState;
            this.mTemplate = opsTemplate;
        }

        public List<AppOpsState.AppOpEntry> loadInBackground() {
            return this.mState.buildState(this.mTemplate, 0, (String) null, AppOpsState.LABEL_COMPARATOR);
        }

        public void deliverResult(List<AppOpsState.AppOpEntry> list) {
            if (isReset() && list != null) {
                onReleaseResources(list);
            }
            this.mApps = list;
            if (isStarted()) {
                super.deliverResult(list);
            }
            if (list != null) {
                onReleaseResources(list);
            }
        }

        /* access modifiers changed from: protected */
        public void onStartLoading() {
            onContentChanged();
            List<AppOpsState.AppOpEntry> list = this.mApps;
            if (list != null) {
                deliverResult(list);
            }
            if (this.mPackageObserver == null) {
                this.mPackageObserver = new PackageIntentReceiver(this);
            }
            boolean applyNewConfig = this.mLastConfig.applyNewConfig(getContext().getResources());
            if (takeContentChanged() || this.mApps == null || applyNewConfig) {
                forceLoad();
            }
        }

        /* access modifiers changed from: protected */
        public void onStopLoading() {
            cancelLoad();
        }

        public void onCanceled(List<AppOpsState.AppOpEntry> list) {
            super.onCanceled(list);
            onReleaseResources(list);
        }

        /* access modifiers changed from: protected */
        public void onReset() {
            super.onReset();
            onStopLoading();
            List<AppOpsState.AppOpEntry> list = this.mApps;
            if (list != null) {
                onReleaseResources(list);
                this.mApps = null;
            }
            if (this.mPackageObserver != null) {
                getContext().unregisterReceiver(this.mPackageObserver);
                this.mPackageObserver = null;
            }
        }
    }

    public static class AppListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        List<AppOpsState.AppOpEntry> mList;
        private final Resources mResources;
        private final AppOpsState mState;

        public long getItemId(int i) {
            return (long) i;
        }

        public AppListAdapter(Context context, AppOpsState appOpsState) {
            this.mResources = context.getResources();
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mState = appOpsState;
        }

        public void setData(List<AppOpsState.AppOpEntry> list) {
            this.mList = list;
            notifyDataSetChanged();
        }

        public int getCount() {
            List<AppOpsState.AppOpEntry> list = this.mList;
            if (list != null) {
                return list.size();
            }
            return 0;
        }

        public AppOpsState.AppOpEntry getItem(int i) {
            return this.mList.get(i);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            boolean z = false;
            if (view == null) {
                view = this.mInflater.inflate(C1715R.layout.app_ops_item, viewGroup, false);
            }
            AppOpsState.AppOpEntry item = getItem(i);
            ((ImageView) view.findViewById(C1715R.C1718id.app_icon)).setImageDrawable(item.getAppEntry().getIcon());
            ((TextView) view.findViewById(C1715R.C1718id.app_name)).setText(item.getAppEntry().getLabel());
            ((TextView) view.findViewById(C1715R.C1718id.op_name)).setText(item.getTimeText(this.mResources, false));
            view.findViewById(C1715R.C1718id.op_time).setVisibility(8);
            Switch switchR = (Switch) view.findViewById(C1715R.C1718id.op_switch);
            if (item.getPrimaryOpMode() == 0) {
                z = true;
            }
            switchR.setChecked(z);
            return view;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mState = new AppOpsState(getActivity());
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setEmptyText("No applications");
        setHasOptionsMenu(true);
        this.mAdapter = new AppListAdapter(getActivity(), this.mState);
        setListAdapter(this.mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, (Bundle) null, this);
    }

    public void onListItemClick(ListView listView, View view, int i, long j) {
        AppOpsState.AppOpEntry item = this.mAdapter.getItem(i);
        if (item != null) {
            Switch switchR = (Switch) view.findViewById(C1715R.C1718id.op_switch);
            boolean z = !switchR.isChecked();
            switchR.setChecked(z);
            int i2 = 0;
            AppOpsManager.OpEntry opEntry = item.getOpEntry(0);
            if (!z) {
                i2 = 1;
            }
            this.mState.getAppOpsManager().setMode(opEntry.getOp(), item.getAppEntry().getApplicationInfo().uid, item.getAppEntry().getApplicationInfo().packageName, i2);
            item.overridePrimaryOpMode(i2);
        }
    }

    public Loader<List<AppOpsState.AppOpEntry>> onCreateLoader(int i, Bundle bundle) {
        Bundle arguments = getArguments();
        return new AppListLoader(getActivity(), this.mState, arguments != null ? (AppOpsState.OpsTemplate) arguments.getParcelable("template") : null);
    }

    public void onLoadFinished(Loader<List<AppOpsState.AppOpEntry>> loader, List<AppOpsState.AppOpEntry> list) {
        this.mAdapter.setData(list);
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    public void onLoaderReset(Loader<List<AppOpsState.AppOpEntry>> loader) {
        this.mAdapter.setData((List<AppOpsState.AppOpEntry>) null);
    }
}
