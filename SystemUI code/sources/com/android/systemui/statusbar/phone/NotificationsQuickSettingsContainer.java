package com.android.systemui.statusbar.phone;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.systemui.C1775R$dimen;
import com.android.systemui.C1777R$id;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.p005qs.C0862QS;
import com.android.systemui.statusbar.notification.AboveShelfObserver;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.ToIntFunction;

public class NotificationsQuickSettingsContainer extends FrameLayout implements ViewStub.OnInflateListener, FragmentHostManager.FragmentListener, AboveShelfObserver.HasViewAboveShelfChangedListener {
    private int mBottomPadding;
    private boolean mCustomizerAnimating;
    private ArrayList<View> mDrawingOrderedChildren = new ArrayList<>();
    private boolean mHasViewsAboveShelf;
    private final Comparator<View> mIndexComparator = Comparator.comparingInt(new ToIntFunction() {
        public final int applyAsInt(Object obj) {
            return NotificationsQuickSettingsContainer.this.indexOfChild((View) obj);
        }
    });
    private boolean mInflated;
    private View mKeyguardStatusBar;
    private ArrayList<View> mLayoutDrawingOrder = new ArrayList<>();
    private boolean mQsExpanded;
    private FrameLayout mQsFrame;
    private NotificationStackScrollLayout mStackScroller;
    private int mStackScrollerMargin;
    private View mUserSwitcher;

    public NotificationsQuickSettingsContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mQsFrame = (FrameLayout) findViewById(C1777R$id.qs_frame);
        this.mStackScroller = (NotificationStackScrollLayout) findViewById(C1777R$id.notification_stack_scroller);
        this.mStackScrollerMargin = ((FrameLayout.LayoutParams) this.mStackScroller.getLayoutParams()).bottomMargin;
        this.mKeyguardStatusBar = findViewById(C1777R$id.keyguard_header);
        ViewStub viewStub = (ViewStub) findViewById(C1777R$id.keyguard_user_switcher);
        viewStub.setOnInflateListener(this);
        this.mUserSwitcher = viewStub;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        FragmentHostManager.get(this).addTagListener(C0862QS.TAG, this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        FragmentHostManager.get(this).removeTagListener(C0862QS.TAG, this);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        reloadWidth(this.mQsFrame, C1775R$dimen.qs_panel_width);
        reloadWidth(this.mStackScroller, C1775R$dimen.notification_panel_width);
    }

    private void reloadWidth(View view, int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = getResources().getDimensionPixelSize(i);
        view.setLayoutParams(layoutParams);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mBottomPadding = windowInsets.getStableInsetBottom();
        setPadding(0, 0, 0, this.mBottomPadding);
        return windowInsets;
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        this.mDrawingOrderedChildren.clear();
        this.mLayoutDrawingOrder.clear();
        if (this.mInflated && this.mUserSwitcher.getVisibility() == 0) {
            this.mDrawingOrderedChildren.add(this.mUserSwitcher);
            this.mLayoutDrawingOrder.add(this.mUserSwitcher);
        }
        if (this.mKeyguardStatusBar.getVisibility() == 0) {
            this.mDrawingOrderedChildren.add(this.mKeyguardStatusBar);
            this.mLayoutDrawingOrder.add(this.mKeyguardStatusBar);
        }
        if (this.mStackScroller.getVisibility() == 0) {
            this.mDrawingOrderedChildren.add(this.mStackScroller);
            this.mLayoutDrawingOrder.add(this.mStackScroller);
        }
        if (this.mQsFrame.getVisibility() == 0) {
            this.mDrawingOrderedChildren.add(this.mQsFrame);
            this.mLayoutDrawingOrder.add(this.mQsFrame);
        }
        if (this.mHasViewsAboveShelf) {
            this.mDrawingOrderedChildren.remove(this.mStackScroller);
            this.mDrawingOrderedChildren.add(this.mStackScroller);
        }
        this.mLayoutDrawingOrder.sort(this.mIndexComparator);
        super.dispatchDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View view, long j) {
        int indexOf = this.mLayoutDrawingOrder.indexOf(view);
        if (indexOf >= 0) {
            return super.drawChild(canvas, this.mDrawingOrderedChildren.get(indexOf), j);
        }
        return super.drawChild(canvas, view, j);
    }

    public void onInflate(ViewStub viewStub, View view) {
        if (viewStub == this.mUserSwitcher) {
            this.mUserSwitcher = view;
            this.mInflated = true;
        }
    }

    public void onFragmentViewCreated(String str, Fragment fragment) {
        ((C0862QS) fragment).setContainer(this);
    }

    public void setQsExpanded(boolean z) {
        if (this.mQsExpanded != z) {
            this.mQsExpanded = z;
            invalidate();
        }
    }

    public void setCustomizerAnimating(boolean z) {
        if (this.mCustomizerAnimating != z) {
            this.mCustomizerAnimating = z;
            invalidate();
        }
    }

    public void setCustomizerShowing(boolean z) {
        if (z) {
            setPadding(0, 0, 0, 0);
            setBottomMargin(this.mStackScroller, 0);
        } else {
            setPadding(0, 0, 0, this.mBottomPadding);
            setBottomMargin(this.mStackScroller, this.mStackScrollerMargin);
        }
        this.mStackScroller.setQsCustomizerShowing(z);
    }

    private void setBottomMargin(View view, int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.bottomMargin = i;
        view.setLayoutParams(layoutParams);
    }

    public void onHasViewsAboveShelfChanged(boolean z) {
        this.mHasViewsAboveShelf = z;
        invalidate();
    }
}
