package androidx.appcompat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class AppCompatToggleButton extends ToggleButton {
    private final AppCompatTextHelper mTextHelper;

    public AppCompatToggleButton(Context context) {
        this(context, (AttributeSet) null);
    }

    public AppCompatToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, 16842827);
        this.mTextHelper = new AppCompatTextHelper(this);
        this.mTextHelper.loadFromAttributes(attributeSet, 16842827);
    }

    public AppCompatToggleButton(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTextHelper = new AppCompatTextHelper(this);
        this.mTextHelper.loadFromAttributes(attributeSet, i);
    }
}
