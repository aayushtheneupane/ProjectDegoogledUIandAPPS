package com.android.systemui.p006qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.ViewAnimationUtils;

/* renamed from: com.android.systemui.qs.QSDetailClipper */
public class QSDetailClipper {
    /* access modifiers changed from: private */
    public Animator mAnimator;
    /* access modifiers changed from: private */
    public final TransitionDrawable mBackground;
    /* access modifiers changed from: private */
    public final View mDetail;
    private final AnimatorListenerAdapter mGoneOnEnd = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            QSDetailClipper.this.mDetail.setVisibility(8);
            QSDetailClipper.this.mBackground.resetTransition();
            Animator unused = QSDetailClipper.this.mAnimator = null;
        }
    };
    private final Runnable mReverseBackground = new Runnable() {
        public void run() {
            if (QSDetailClipper.this.mAnimator != null) {
                QSDetailClipper.this.mBackground.reverseTransition((int) (((double) QSDetailClipper.this.mAnimator.getDuration()) * 0.35d));
            }
        }
    };
    private final AnimatorListenerAdapter mVisibleOnStart = new AnimatorListenerAdapter() {
        public void onAnimationStart(Animator animator) {
            QSDetailClipper.this.mDetail.setVisibility(0);
        }

        public void onAnimationEnd(Animator animator) {
            Animator unused = QSDetailClipper.this.mAnimator = null;
        }
    };

    public QSDetailClipper(View view) {
        this.mDetail = view;
        this.mBackground = (TransitionDrawable) view.getBackground();
    }

    public void animateCircularClip(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        Animator animator = this.mAnimator;
        if (animator != null) {
            animator.cancel();
        }
        int width = this.mDetail.getWidth() - i;
        int height = this.mDetail.getHeight() - i2;
        int i3 = 0;
        if (i < 0 || width < 0 || i2 < 0 || height < 0) {
            i3 = Math.min(Math.min(Math.min(Math.abs(i), Math.abs(i2)), Math.abs(width)), Math.abs(height));
        }
        int i4 = i * i;
        int i5 = i2 * i2;
        int i6 = width * width;
        int i7 = height * height;
        int max = (int) Math.max((double) ((int) Math.max((double) ((int) Math.max((double) ((int) Math.ceil(Math.sqrt((double) (i4 + i5)))), Math.ceil(Math.sqrt((double) (i5 + i6))))), Math.ceil(Math.sqrt((double) (i6 + i7))))), Math.ceil(Math.sqrt((double) (i4 + i7))));
        if (z) {
            this.mAnimator = ViewAnimationUtils.createCircularReveal(this.mDetail, i, i2, (float) i3, (float) max);
        } else {
            this.mAnimator = ViewAnimationUtils.createCircularReveal(this.mDetail, i, i2, (float) max, (float) i3);
        }
        Animator animator2 = this.mAnimator;
        animator2.setDuration((long) (((double) animator2.getDuration()) * 1.5d));
        if (animatorListener != null) {
            this.mAnimator.addListener(animatorListener);
        }
        if (z) {
            this.mBackground.startTransition((int) (((double) this.mAnimator.getDuration()) * 0.6d));
            this.mAnimator.addListener(this.mVisibleOnStart);
        } else {
            this.mDetail.postDelayed(this.mReverseBackground, (long) (((double) this.mAnimator.getDuration()) * 0.65d));
            this.mAnimator.addListener(this.mGoneOnEnd);
        }
        this.mAnimator.start();
    }

    public void showBackground() {
        this.mBackground.showSecondLayer();
    }
}
