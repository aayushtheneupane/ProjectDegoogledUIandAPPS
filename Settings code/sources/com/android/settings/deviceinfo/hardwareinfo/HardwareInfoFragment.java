package com.android.settings.deviceinfo.hardwareinfo;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.List;

public class HardwareInfoFragment extends DashboardFragment {
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean z) {
            ArrayList arrayList = new ArrayList();
            SearchIndexableResource searchIndexableResource = new SearchIndexableResource(context);
            searchIndexableResource.xmlResId = C1715R.xml.hardware_info;
            arrayList.add(searchIndexableResource);
            return arrayList;
        }

        /* access modifiers changed from: protected */
        public boolean isPageSearchEnabled(Context context) {
            return context.getResources().getBoolean(C1715R.bool.config_show_device_model);
        }
    };

    /* access modifiers changed from: protected */
    public String getLogTag() {
        return "HardwareInfo";
    }

    public int getMetricsCategory() {
        return 862;
    }

    /* access modifiers changed from: protected */
    public int getPreferenceScreenResId() {
        return C1715R.xml.hardware_info;
    }
}
