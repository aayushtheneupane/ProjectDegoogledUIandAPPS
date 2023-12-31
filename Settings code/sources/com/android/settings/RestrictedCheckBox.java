package com.android.settings;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.CheckBox;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.havoc.config.center.C1715R;

public class RestrictedCheckBox extends CheckBox {
    private Context mContext;
    private boolean mDisabledByAdmin;
    private RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;

    public RestrictedCheckBox(Context context) {
        this(context, (AttributeSet) null);
    }

    public RestrictedCheckBox(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    public boolean performClick() {
        if (!this.mDisabledByAdmin) {
            return super.performClick();
        }
        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mEnforcedAdmin);
        return true;
    }

    public void setDisabledByAdmin(RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        boolean z = enforcedAdmin != null;
        this.mEnforcedAdmin = enforcedAdmin;
        if (this.mDisabledByAdmin != z) {
            this.mDisabledByAdmin = z;
            RestrictedLockUtilsInternal.setTextViewAsDisabledByAdmin(this.mContext, this, this.mDisabledByAdmin);
            if (this.mDisabledByAdmin) {
                getButtonDrawable().setColorFilter(this.mContext.getColor(C1715R.C1716color.disabled_text_color), PorterDuff.Mode.MULTIPLY);
            } else {
                getButtonDrawable().clearColorFilter();
            }
        }
    }
}
