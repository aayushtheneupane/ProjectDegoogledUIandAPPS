package com.android.settingslib.license;

import android.content.Context;
import android.util.Log;
import com.android.settingslib.R$string;
import com.android.settingslib.utils.AsyncLoaderCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LicenseHtmlLoaderCompat extends AsyncLoaderCompat<File> {
    static final String[] DEFAULT_LICENSE_XML_PATHS = {"/system/etc/NOTICE.xml.gz", "/vendor/etc/NOTICE.xml.gz", "/odm/etc/NOTICE.xml.gz", "/oem/etc/NOTICE.xml.gz", "/product/etc/NOTICE.xml.gz", "/product_services/etc/NOTICE.xml.gz"};
    private final Context mContext;

    /* access modifiers changed from: protected */
    public void onDiscardResult(File file) {
    }

    public LicenseHtmlLoaderCompat(Context context) {
        super(context);
        this.mContext = context;
    }

    public File loadInBackground() {
        return generateHtmlFromDefaultXmlFiles();
    }

    private File generateHtmlFromDefaultXmlFiles() {
        List<File> vaildXmlFiles = getVaildXmlFiles();
        if (vaildXmlFiles.isEmpty()) {
            Log.e("LicenseHtmlLoaderCompat", "No notice file exists.");
            return null;
        }
        File cachedHtmlFile = getCachedHtmlFile(this.mContext);
        if (!isCachedHtmlFileOutdated(vaildXmlFiles, cachedHtmlFile) || generateHtmlFile(this.mContext, vaildXmlFiles, cachedHtmlFile)) {
            return cachedHtmlFile;
        }
        return null;
    }

    private List<File> getVaildXmlFiles() {
        ArrayList arrayList = new ArrayList();
        for (String file : DEFAULT_LICENSE_XML_PATHS) {
            File file2 = new File(file);
            if (file2.exists() && file2.length() != 0) {
                arrayList.add(file2);
            }
        }
        return arrayList;
    }

    private File getCachedHtmlFile(Context context) {
        return new File(context.getCacheDir(), "NOTICE.html");
    }

    private boolean isCachedHtmlFileOutdated(List<File> list, File file) {
        if (!file.exists() || file.length() == 0) {
            return true;
        }
        for (File lastModified : list) {
            if (file.lastModified() < lastModified.lastModified()) {
                return true;
            }
        }
        return false;
    }

    private boolean generateHtmlFile(Context context, List<File> list, File file) {
        return LicenseHtmlGeneratorFromXml.generateHtml(list, file, context.getString(R$string.notice_header));
    }
}
