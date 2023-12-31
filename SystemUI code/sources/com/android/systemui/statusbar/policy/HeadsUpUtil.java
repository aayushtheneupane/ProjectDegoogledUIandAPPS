package com.android.systemui.statusbar.policy;

import android.view.View;
import com.android.systemui.C1777R$id;

public final class HeadsUpUtil {
    private static final int TAG_CLICKED_NOTIFICATION = C1777R$id.is_clicked_heads_up_tag;

    public static void setIsClickedHeadsUpNotification(View view, boolean z) {
        view.setTag(TAG_CLICKED_NOTIFICATION, z ? true : null);
    }

    public static boolean isClickedHeadsUpNotification(View view) {
        Boolean bool = (Boolean) view.getTag(TAG_CLICKED_NOTIFICATION);
        return bool != null && bool.booleanValue();
    }
}
