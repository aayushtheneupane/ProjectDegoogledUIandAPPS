package androidx.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.ViewCompat;
import com.android.contacts.ContactPhotoManager;

public class Fade extends Visibility {
    public Fade(int i) {
        setMode(i);
    }

    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        transitionValues.values.put("android:fade:transitionAlpha", Float.valueOf(ViewUtils.getTransitionAlpha(transitionValues.view)));
    }

    private Animator createAnimation(final View view, float f, float f2) {
        if (f == f2) {
            return null;
        }
        ViewUtils.setTransitionAlpha(view, f);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, ViewUtils.TRANSITION_ALPHA, new float[]{f2});
        ofFloat.addListener(new FadeAnimatorListener(view));
        addListener(new TransitionListenerAdapter() {
            public void onTransitionEnd(Transition transition) {
                ViewUtils.setTransitionAlpha(view, 1.0f);
                ViewUtils.clearNonTransitionAlpha(view);
                transition.removeListener(this);
            }
        });
        return ofFloat;
    }

    public Animator onAppear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
        float f = ContactPhotoManager.OFFSET_DEFAULT;
        float startAlpha = getStartAlpha(transitionValues, ContactPhotoManager.OFFSET_DEFAULT);
        if (startAlpha != 1.0f) {
            f = startAlpha;
        }
        return createAnimation(view, f, 1.0f);
    }

    public Animator onDisappear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
        ViewUtils.saveNonTransitionAlpha(view);
        return createAnimation(view, getStartAlpha(transitionValues, 1.0f), ContactPhotoManager.OFFSET_DEFAULT);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:1:0x0002, code lost:
        r1 = (java.lang.Float) r1.values.get("android:fade:transitionAlpha");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static float getStartAlpha(androidx.transition.TransitionValues r1, float r2) {
        /*
            if (r1 == 0) goto L_0x0012
            java.util.Map<java.lang.String, java.lang.Object> r1 = r1.values
            java.lang.String r0 = "android:fade:transitionAlpha"
            java.lang.Object r1 = r1.get(r0)
            java.lang.Float r1 = (java.lang.Float) r1
            if (r1 == 0) goto L_0x0012
            float r2 = r1.floatValue()
        L_0x0012:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.transition.Fade.getStartAlpha(androidx.transition.TransitionValues, float):float");
    }

    private static class FadeAnimatorListener extends AnimatorListenerAdapter {
        private boolean mLayerTypeChanged = false;
        private final View mView;

        FadeAnimatorListener(View view) {
            this.mView = view;
        }

        public void onAnimationStart(Animator animator) {
            if (ViewCompat.hasOverlappingRendering(this.mView) && this.mView.getLayerType() == 0) {
                this.mLayerTypeChanged = true;
                this.mView.setLayerType(2, (Paint) null);
            }
        }

        public void onAnimationEnd(Animator animator) {
            ViewUtils.setTransitionAlpha(this.mView, 1.0f);
            if (this.mLayerTypeChanged) {
                this.mView.setLayerType(0, (Paint) null);
            }
        }
    }
}
