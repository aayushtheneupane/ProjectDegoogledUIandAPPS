package com.android.systemui.tuner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import androidx.preference.CheckBoxPreference;
import com.android.systemui.C1779R$layout;
import com.android.systemui.statusbar.ScalingDrawableWrapper;

public class SelectablePreference extends CheckBoxPreference {
    private final int mSize;

    public String toString() {
        return "";
    }

    public SelectablePreference(Context context) {
        super(context);
        setWidgetLayoutResource(C1779R$layout.preference_widget_radiobutton);
        setSelectable(true);
        this.mSize = (int) TypedValue.applyDimension(1, 32.0f, context.getResources().getDisplayMetrics());
    }

    public void setIcon(Drawable drawable) {
        super.setIcon((Drawable) new ScalingDrawableWrapper(drawable, ((float) this.mSize) / ((float) drawable.getIntrinsicWidth())));
    }
}
