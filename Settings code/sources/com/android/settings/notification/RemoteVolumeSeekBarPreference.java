package com.android.settings.notification;

import android.content.Context;
import android.util.AttributeSet;

public class RemoteVolumeSeekBarPreference extends VolumeSeekBarPreference {
    public void setStream(int i) {
    }

    public RemoteVolumeSeekBarPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public RemoteVolumeSeekBarPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public RemoteVolumeSeekBarPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public RemoteVolumeSeekBarPreference(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void init() {
        if (this.mSeekBar != null) {
            updateIconView();
            updateSuppressionText();
        }
    }
}
