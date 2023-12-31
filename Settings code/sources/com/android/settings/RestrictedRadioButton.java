package com.android.settings;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.RadioButton;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.havoc.config.center.C1715R;

public class RestrictedRadioButton extends RadioButton {
    private Context mContext;
    private boolean mDisabledByAdmin;
    private RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;

    public RestrictedRadioButton(Context context) {
        this(context, (AttributeSet) null);
    }

    public RestrictedRadioButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842878);
    }

    public RestrictedRadioButton(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RestrictedRadioButton(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
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

    public boolean isDisabledByAdmin() {
        return this.mDisabledByAdmin;
    }
}
