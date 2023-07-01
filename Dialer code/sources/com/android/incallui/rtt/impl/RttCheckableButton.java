package com.android.incallui.rtt.impl;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;

public class RttCheckableButton extends Button implements Checkable {
    private static final int[] CHECKED_STATE_SET = {16842912};
    private boolean broadcasting;
    private CharSequence contentDescriptionChecked;
    private CharSequence contentDescriptionUnchecked;
    private boolean isChecked;
    private OnCheckedChangeListener onCheckedChangeListener;

    public interface OnCheckedChangeListener {
    }

    public RttCheckableButton(Context context) {
        this(context, (AttributeSet) null, 16842866, 0);
    }

    private void performSetChecked(boolean z) {
        if (isChecked() != z) {
            this.isChecked = z;
            CharSequence charSequence = this.isChecked ? this.contentDescriptionChecked : this.contentDescriptionUnchecked;
            setContentDescription(charSequence);
            announceForAccessibility(charSequence);
            refreshDrawableState();
        }
    }

    private CharSequence updateContentDescription() {
        CharSequence charSequence = this.isChecked ? this.contentDescriptionChecked : this.contentDescriptionUnchecked;
        setContentDescription(charSequence);
        return charSequence;
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (isChecked()) {
            Button.mergeDrawableStates(onCreateDrawableState, CHECKED_STATE_SET);
        }
        return onCreateDrawableState;
    }

    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        performSetChecked(savedState.isChecked);
        requestLayout();
    }

    public Parcelable onSaveInstanceState() {
        return new SavedState(isChecked(), super.onSaveInstanceState(), (C07271) null);
    }

    public boolean performClick() {
        if (!(this.onCheckedChangeListener != null)) {
            return super.performClick();
        }
        toggle();
        boolean performClick = super.performClick();
        if (!performClick) {
            playSoundEffect(0);
        }
        return performClick;
    }

    public void setChecked(boolean z) {
        performSetChecked(z);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener2) {
        this.onCheckedChangeListener = onCheckedChangeListener2;
    }

    public void toggle() {
        boolean z = !isChecked();
        if (isChecked() != z && !this.broadcasting) {
            this.broadcasting = true;
            OnCheckedChangeListener onCheckedChangeListener2 = this.onCheckedChangeListener;
            if (onCheckedChangeListener2 != null) {
                ((RttOverflowMenu) onCheckedChangeListener2).onCheckedChanged(this, z);
            }
            this.broadcasting = false;
        }
    }

    private static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public Object createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public Object[] newArray(int i) {
                return new SavedState[i];
            }
        };
        public final boolean isChecked;

        /* synthetic */ SavedState(boolean z, Parcelable parcelable, C07271 r3) {
            super(parcelable);
            this.isChecked = z;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeByte(this.isChecked ? (byte) 1 : 0);
        }

        protected SavedState(Parcel parcel) {
            super(parcel);
            this.isChecked = parcel.readByte() != 0;
        }
    }

    public RttCheckableButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842866, 0);
    }

    public RttCheckableButton(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RttCheckableButton(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.RttCheckableButton);
        setChecked(obtainStyledAttributes.getBoolean(0, false));
        this.contentDescriptionChecked = obtainStyledAttributes.getText(1);
        this.contentDescriptionUnchecked = obtainStyledAttributes.getText(2);
        obtainStyledAttributes.recycle();
        updateContentDescription();
        setClickable(true);
        setFocusable(true);
    }
}
