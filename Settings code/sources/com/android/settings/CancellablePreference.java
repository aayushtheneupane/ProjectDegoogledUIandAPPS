package com.android.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.havoc.config.center.C1715R;

public class CancellablePreference extends Preference implements View.OnClickListener {
    private boolean mCancellable;
    private OnCancelListener mListener;

    public interface OnCancelListener {
        void onCancel(CancellablePreference cancellablePreference);
    }

    public CancellablePreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setWidgetLayoutResource(C1715R.layout.cancel_pref_widget);
    }

    public void setCancellable(boolean z) {
        this.mCancellable = z;
        notifyChanged();
    }

    public void setOnCancelListener(OnCancelListener onCancelListener) {
        this.mListener = onCancelListener;
    }

    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        ImageView imageView = (ImageView) preferenceViewHolder.findViewById(C1715R.C1718id.cancel);
        imageView.setVisibility(this.mCancellable ? 0 : 4);
        imageView.setOnClickListener(this);
    }

    public void onClick(View view) {
        OnCancelListener onCancelListener = this.mListener;
        if (onCancelListener != null) {
            onCancelListener.onCancel(this);
        }
    }
}
