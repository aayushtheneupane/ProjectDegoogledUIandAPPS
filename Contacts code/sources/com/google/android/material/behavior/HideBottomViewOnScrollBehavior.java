package com.google.android.material.behavior;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.animation.AnimationUtils;

public class HideBottomViewOnScrollBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    /* access modifiers changed from: private */
    public ViewPropertyAnimator currentAnimator;
    private int currentState = 2;
    private int height = 0;

    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V v, View view, View view2, int i) {
        return i == 2;
    }

    public HideBottomViewOnScrollBehavior() {
    }

    public HideBottomViewOnScrollBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public boolean onLayoutChild(CoordinatorLayout coordinatorLayout, V v, int i) {
        this.height = v.getMeasuredHeight() + ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).bottomMargin;
        return super.onLayoutChild(coordinatorLayout, v, i);
    }

    public void onNestedScroll(CoordinatorLayout coordinatorLayout, V v, View view, int i, int i2, int i3, int i4) {
        if (this.currentState != 1 && i2 > 0) {
            slideDown(v);
        } else if (this.currentState != 2 && i2 < 0) {
            slideUp(v);
        }
    }

    public void slideUp(V v) {
        ViewPropertyAnimator viewPropertyAnimator = this.currentAnimator;
        if (viewPropertyAnimator != null) {
            viewPropertyAnimator.cancel();
            v.clearAnimation();
        }
        this.currentState = 2;
        animateChildTo(v, 0, 225, AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
    }

    public void slideDown(V v) {
        ViewPropertyAnimator viewPropertyAnimator = this.currentAnimator;
        if (viewPropertyAnimator != null) {
            viewPropertyAnimator.cancel();
            v.clearAnimation();
        }
        this.currentState = 1;
        animateChildTo(v, this.height, 175, AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR);
    }

    private void animateChildTo(V v, int i, long j, TimeInterpolator timeInterpolator) {
        this.currentAnimator = v.animate().translationY((float) i).setInterpolator(timeInterpolator).setDuration(j).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                ViewPropertyAnimator unused = HideBottomViewOnScrollBehavior.this.currentAnimator = null;
            }
        });
    }
}
