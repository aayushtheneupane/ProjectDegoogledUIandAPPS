package com.android.settings.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.Preference;
import com.havoc.config.center.C1715R;

public class CardPreference extends Preference {
    public CardPreference(Context context) {
        this(context, (AttributeSet) null);
    }

    public CardPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, C1715R.attr.cardPreferenceStyle);
    }
}
