package com.android.settingslib.core.instrumentation;

import android.content.Context;
import android.metrics.LogMaker;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.logging.MetricsLogger;

public class EventLogWriter implements LogWriter {
    public void visible(Context context, int i, int i2) {
        MetricsLogger.action(new LogMaker(i2).setType(1).addTaggedData(833, Integer.valueOf(i)));
    }

    public void hidden(Context context, int i) {
        MetricsLogger.hidden(context, i);
    }

    public void action(Context context, int i, Pair<Integer, Object>... pairArr) {
        LogMaker type = new LogMaker(i).setType(4);
        if (pairArr != null) {
            for (Pair<Integer, Object> pair : pairArr) {
                type.addTaggedData(((Integer) pair.first).intValue(), pair.second);
            }
        }
        MetricsLogger.action(type);
    }

    public void action(Context context, int i, int i2) {
        MetricsLogger.action(context, i, i2);
    }

    public void action(Context context, int i, boolean z) {
        MetricsLogger.action(context, i, z);
    }

    public void action(Context context, int i, String str) {
        MetricsLogger.action(new LogMaker(i).setType(4).setPackageName(str));
    }

    public void action(int i, int i2, int i3, String str, int i4) {
        LogMaker type = new LogMaker(i2).setType(4);
        if (i != 0) {
            type.addTaggedData(833, Integer.valueOf(i3));
        }
        if (!TextUtils.isEmpty(str)) {
            type.addTaggedData(854, str);
            type.addTaggedData(1089, Integer.valueOf(i4));
        }
        MetricsLogger.action(type);
    }
}
