package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.C1774R$color;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class NotificationCustomViewWrapper extends NotificationViewWrapper {
    private boolean mIsLegacy;
    private int mLegacyColor;

    /* access modifiers changed from: protected */
    public boolean shouldClearBackgroundOnReapply() {
        return false;
    }

    public boolean shouldClipToRounding(boolean z, boolean z2) {
        return true;
    }

    protected NotificationCustomViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mLegacyColor = expandableNotificationRow.getContext().getColor(C1774R$color.notification_legacy_background_color);
    }

    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mView.setAlpha(z ? 1.0f : 0.0f);
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        if (needsInversion(this.mBackgroundColor, this.mView)) {
            invertViewLuminosity(this.mView);
            float[] fArr = {0.0f, 0.0f, 0.0f};
            ColorUtils.colorToHSL(this.mBackgroundColor, fArr);
            if (this.mBackgroundColor != 0 && ((double) fArr[2]) > 0.5d) {
                fArr[2] = 1.0f - fArr[2];
                this.mBackgroundColor = ColorUtils.HSLToColor(fArr);
            }
        }
    }

    public int getCustomBackgroundColor() {
        int customBackgroundColor = super.getCustomBackgroundColor();
        return (customBackgroundColor != 0 || !this.mIsLegacy) ? customBackgroundColor : this.mLegacyColor;
    }

    public void setLegacy(boolean z) {
        super.setLegacy(z);
        this.mIsLegacy = z;
    }
}
