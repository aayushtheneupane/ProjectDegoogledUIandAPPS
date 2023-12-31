package com.android.systemui.statusbar.phone;

import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.View;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;

public class RotationContextButton extends ContextualButton implements NavigationModeController.ModeChangedListener, RotationButton {
    private boolean mCanShow = true;
    private int mNavBarMode = 0;
    private RotationButtonController mRotationButtonController;

    public RotationContextButton(int i, int i2) {
        super(i, i2);
    }

    public void setRotationButtonController(RotationButtonController rotationButtonController) {
        this.mRotationButtonController = rotationButtonController;
    }

    public void setVisibility(int i) {
        super.setVisibility(i);
        KeyButtonDrawable imageDrawable = getImageDrawable();
        if (i == 0 && imageDrawable != null) {
            imageDrawable.resetAnimation();
            imageDrawable.startAnimation();
        }
    }

    /* access modifiers changed from: protected */
    public KeyButtonDrawable getNewDrawable() {
        return KeyButtonDrawable.create(new ContextThemeWrapper(getContext().getApplicationContext(), this.mRotationButtonController.getStyleRes()), this.mIconResId, false, (Color) null);
    }

    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    public boolean acceptRotationProposal() {
        View currentView = getCurrentView();
        return currentView != null && currentView.isAttachedToWindow();
    }

    public boolean show() {
        if (!this.mCanShow) {
            return false;
        }
        return super.show();
    }

    public void setCanShowRotationButton(boolean z) {
        this.mCanShow = z;
        if (!this.mCanShow) {
            hide();
        }
    }
}
