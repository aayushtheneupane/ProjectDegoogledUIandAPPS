package com.android.systemui.p006qs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.C1775R$dimen;
import com.android.systemui.C1776R$drawable;
import com.android.systemui.p006qs.tileimpl.QSIconViewImpl;
import com.android.systemui.p006qs.tileimpl.SlashImageView;
import com.android.systemui.plugins.p005qs.QSTile;

/* renamed from: com.android.systemui.qs.SignalTileView */
public class SignalTileView extends QSIconViewImpl {
    private static final long DEFAULT_DURATION = new ValueAnimator().getDuration();
    private static final long SHORT_DURATION = (DEFAULT_DURATION / 3);
    protected FrameLayout mIconFrame;
    private ImageView mIn = addTrafficView(C1776R$drawable.ic_qs_signal_in);
    private ImageView mOut = addTrafficView(C1776R$drawable.ic_qs_signal_out);
    private ImageView mOverlay;
    protected ImageView mSignal;
    private int mSignalIndicatorToIconFrameSpacing;
    private int mWideOverlayIconStartPadding;

    /* access modifiers changed from: protected */
    public int getIconMeasureMode() {
        return Integer.MIN_VALUE;
    }

    public SignalTileView(Context context) {
        super(context);
        setClipChildren(false);
        setClipToPadding(false);
        this.mWideOverlayIconStartPadding = context.getResources().getDimensionPixelSize(C1775R$dimen.wide_type_icon_start_padding_qs);
        this.mSignalIndicatorToIconFrameSpacing = context.getResources().getDimensionPixelSize(C1775R$dimen.signal_indicator_to_icon_frame_spacing);
    }

    private ImageView addTrafficView(int i) {
        ImageView imageView = new ImageView(this.mContext);
        imageView.setImageResource(i);
        imageView.setAlpha(0.0f);
        addView(imageView);
        return imageView;
    }

    /* access modifiers changed from: protected */
    public View createIcon() {
        this.mIconFrame = new FrameLayout(this.mContext);
        this.mSignal = createSlashImageView(this.mContext);
        this.mIconFrame.addView(this.mSignal);
        this.mOverlay = new ImageView(this.mContext);
        this.mIconFrame.addView(this.mOverlay, -2, -2);
        return this.mIconFrame;
    }

    /* access modifiers changed from: protected */
    public SlashImageView createSlashImageView(Context context) {
        return new SlashImageView(context);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mIconFrame.getMeasuredHeight(), 1073741824);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mIconFrame.getMeasuredHeight(), Integer.MIN_VALUE);
        this.mIn.measure(makeMeasureSpec2, makeMeasureSpec);
        this.mOut.measure(makeMeasureSpec2, makeMeasureSpec);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        layoutIndicator(this.mIn);
        layoutIndicator(this.mOut);
    }

    private void layoutIndicator(View view) {
        int i;
        int i2;
        boolean z = true;
        if (getLayoutDirection() != 1) {
            z = false;
        }
        if (z) {
            i2 = getLeft() - this.mSignalIndicatorToIconFrameSpacing;
            i = i2 - view.getMeasuredWidth();
        } else {
            i = this.mSignalIndicatorToIconFrameSpacing + getRight();
            i2 = view.getMeasuredWidth() + i;
        }
        view.layout(i, this.mIconFrame.getBottom() - view.getMeasuredHeight(), i2, this.mIconFrame.getBottom());
    }

    public void setIcon(QSTile.State state, boolean z) {
        QSTile.SignalState signalState = (QSTile.SignalState) state;
        setIcon(this.mSignal, signalState, z);
        boolean z2 = false;
        if (signalState.overlayIconId > 0) {
            this.mOverlay.setVisibility(0);
            this.mOverlay.setImageResource(signalState.overlayIconId);
        } else {
            this.mOverlay.setVisibility(8);
        }
        if (signalState.overlayIconId <= 0 || !signalState.isOverlayIconWide) {
            this.mSignal.setPaddingRelative(0, 0, 0, 0);
        } else {
            this.mSignal.setPaddingRelative(this.mWideOverlayIconStartPadding, 0, 0, 0);
        }
        if (z && isShown()) {
            z2 = true;
        }
        setVisibility(this.mIn, z2, signalState.activityIn);
        setVisibility(this.mOut, z2, signalState.activityOut);
    }

    private void setVisibility(View view, boolean z, boolean z2) {
        float f = (!z || !z2) ? 0.0f : 1.0f;
        if (view.getAlpha() != f) {
            if (z) {
                view.animate().setDuration(z2 ? SHORT_DURATION : DEFAULT_DURATION).alpha(f).start();
            } else {
                view.setAlpha(f);
            }
        }
    }
}
