package com.android.systemui.recents;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Binder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.R$styleable;
import com.android.systemui.C1774R$color;
import com.android.systemui.C1777R$id;
import com.android.systemui.C1779R$layout;
import com.android.systemui.C1784R$string;
import com.android.systemui.Dependency;
import com.android.systemui.SysUiServiceProvider;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.leak.RotationUtils;
import java.util.ArrayList;

public class ScreenPinningRequest implements View.OnClickListener, NavigationModeController.ModeChangedListener {
    /* access modifiers changed from: private */
    public final AccessibilityManager mAccessibilityService = ((AccessibilityManager) this.mContext.getSystemService("accessibility"));
    private final Context mContext;
    /* access modifiers changed from: private */
    public int mNavBarMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
    private final OverviewProxyService mOverviewProxyService = ((OverviewProxyService) Dependency.get(OverviewProxyService.class));
    private RequestWindowView mRequestWindow;
    /* access modifiers changed from: private */
    public final WindowManager mWindowManager = ((WindowManager) this.mContext.getSystemService("window"));
    private int taskId;

    public ScreenPinningRequest(Context context) {
        this.mContext = context;
    }

    public void clearPrompt() {
        RequestWindowView requestWindowView = this.mRequestWindow;
        if (requestWindowView != null) {
            this.mWindowManager.removeView(requestWindowView);
            this.mRequestWindow = null;
        }
    }

    public void showPrompt(int i, boolean z) {
        try {
            clearPrompt();
        } catch (IllegalArgumentException unused) {
        }
        this.taskId = i;
        this.mRequestWindow = new RequestWindowView(this.mContext, z);
        this.mRequestWindow.setSystemUiVisibility(256);
        this.mWindowManager.addView(this.mRequestWindow, getWindowLayoutParams());
    }

    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    public void onConfigurationChanged() {
        RequestWindowView requestWindowView = this.mRequestWindow;
        if (requestWindowView != null) {
            requestWindowView.onConfigurationChanged();
        }
    }

    private WindowManager.LayoutParams getWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2024, 264, -3);
        layoutParams.token = new Binder();
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("ScreenPinningConfirmation");
        layoutParams.gravity = R$styleable.AppCompatTheme_windowFixedHeightMinor;
        return layoutParams;
    }

    public void onClick(View view) {
        if (view.getId() == C1777R$id.screen_pinning_ok_button || this.mRequestWindow == view) {
            try {
                ActivityTaskManager.getService().startSystemLockTaskMode(this.taskId);
            } catch (RemoteException unused) {
            }
        }
        clearPrompt();
    }

    public FrameLayout.LayoutParams getRequestLayoutParams(int i) {
        return new FrameLayout.LayoutParams(-2, -2, i == 2 ? 19 : i == 1 ? 21 : 81);
    }

    private class RequestWindowView extends FrameLayout {
        /* access modifiers changed from: private */
        public final ColorDrawable mColor = new ColorDrawable(0);
        private ValueAnimator mColorAnim;
        /* access modifiers changed from: private */
        public ViewGroup mLayout;
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    RequestWindowView requestWindowView = RequestWindowView.this;
                    requestWindowView.post(requestWindowView.mUpdateLayoutRunnable);
                } else if (intent.getAction().equals("android.intent.action.USER_SWITCHED") || intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    ScreenPinningRequest.this.clearPrompt();
                }
            }
        };
        private boolean mShowCancel;
        /* access modifiers changed from: private */
        public final Runnable mUpdateLayoutRunnable = new Runnable() {
            public void run() {
                if (RequestWindowView.this.mLayout != null && RequestWindowView.this.mLayout.getParent() != null) {
                    ViewGroup access$400 = RequestWindowView.this.mLayout;
                    RequestWindowView requestWindowView = RequestWindowView.this;
                    access$400.setLayoutParams(ScreenPinningRequest.this.getRequestLayoutParams(RotationUtils.getRotation(requestWindowView.mContext)));
                }
            }
        };

        public RequestWindowView(Context context, boolean z) {
            super(context);
            setClickable(true);
            setOnClickListener(ScreenPinningRequest.this);
            setBackground(this.mColor);
            this.mShowCancel = z;
        }

        public void onAttachedToWindow() {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ScreenPinningRequest.this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            float f = displayMetrics.density;
            int rotation = RotationUtils.getRotation(this.mContext);
            inflateView(rotation);
            int color = this.mContext.getColor(C1774R$color.screen_pinning_request_window_bg);
            if (ActivityManager.isHighEndGfx()) {
                this.mLayout.setAlpha(0.0f);
                if (rotation == 2) {
                    this.mLayout.setTranslationX(f * -96.0f);
                } else if (rotation == 1) {
                    this.mLayout.setTranslationX(f * 96.0f);
                } else {
                    this.mLayout.setTranslationY(f * 96.0f);
                }
                this.mLayout.animate().alpha(1.0f).translationX(0.0f).translationY(0.0f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
                this.mColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{0, Integer.valueOf(color)});
                this.mColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        RequestWindowView.this.mColor.setColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                this.mColorAnim.setDuration(1000);
                this.mColorAnim.start();
            } else {
                this.mColor.setColor(color);
            }
            IntentFilter intentFilter = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }

        private void inflateView(int i) {
            int i2;
            int i3;
            Context context = getContext();
            boolean z = true;
            if (i == 2) {
                i2 = C1779R$layout.screen_pinning_request_sea_phone;
            } else if (i == 1) {
                i2 = C1779R$layout.screen_pinning_request_land_phone;
            } else {
                i2 = C1779R$layout.screen_pinning_request;
            }
            NavigationBarView navigationBarView = null;
            this.mLayout = (ViewGroup) View.inflate(context, i2, (ViewGroup) null);
            this.mLayout.setClickable(true);
            int i4 = 0;
            this.mLayout.setLayoutDirection(0);
            this.mLayout.findViewById(C1777R$id.screen_pinning_text_area).setLayoutDirection(3);
            View findViewById = this.mLayout.findViewById(C1777R$id.screen_pinning_buttons);
            WindowManagerWrapper instance = WindowManagerWrapper.getInstance();
            if (QuickStepContract.isGesturalMode(ScreenPinningRequest.this.mNavBarMode) || !instance.hasSoftNavigationBar(this.mContext.getDisplayId())) {
                findViewById.setVisibility(8);
            } else {
                findViewById.setLayoutDirection(3);
                swapChildrenIfRtlAndVertical(findViewById);
            }
            ((Button) this.mLayout.findViewById(C1777R$id.screen_pinning_ok_button)).setOnClickListener(ScreenPinningRequest.this);
            if (this.mShowCancel) {
                ((Button) this.mLayout.findViewById(C1777R$id.screen_pinning_cancel_button)).setOnClickListener(ScreenPinningRequest.this);
            } else {
                ((Button) this.mLayout.findViewById(C1777R$id.screen_pinning_cancel_button)).setVisibility(4);
            }
            StatusBar statusBar = (StatusBar) SysUiServiceProvider.getComponent(this.mContext, StatusBar.class);
            if (statusBar != null) {
                navigationBarView = statusBar.getNavigationBarView();
            }
            if (navigationBarView == null || !navigationBarView.isRecentsButtonVisible()) {
                z = false;
            }
            boolean isTouchExplorationEnabled = ScreenPinningRequest.this.mAccessibilityService.isTouchExplorationEnabled();
            if (QuickStepContract.isGesturalMode(ScreenPinningRequest.this.mNavBarMode)) {
                i3 = C1784R$string.screen_pinning_description_gestural;
            } else if (z) {
                this.mLayout.findViewById(C1777R$id.screen_pinning_recents_group).setVisibility(0);
                this.mLayout.findViewById(C1777R$id.screen_pinning_home_bg_light).setVisibility(4);
                this.mLayout.findViewById(C1777R$id.screen_pinning_home_bg).setVisibility(4);
                if (isTouchExplorationEnabled) {
                    i3 = C1784R$string.screen_pinning_description_accessible_custom;
                } else {
                    i3 = C1784R$string.screen_pinning_description_custom;
                }
            } else {
                this.mLayout.findViewById(C1777R$id.screen_pinning_recents_group).setVisibility(4);
                this.mLayout.findViewById(C1777R$id.screen_pinning_home_bg_light).setVisibility(4);
                this.mLayout.findViewById(C1777R$id.screen_pinning_home_bg).setVisibility(4);
                if (isTouchExplorationEnabled) {
                    i3 = C1784R$string.screen_pinning_description_recents_invisible_accessible_custom;
                } else {
                    i3 = C1784R$string.screen_pinning_description_recents_invisible_custom;
                }
            }
            if (navigationBarView != null) {
                ((ImageView) this.mLayout.findViewById(C1777R$id.screen_pinning_back_icon)).setImageDrawable(navigationBarView.getBackDrawable());
            }
            ((TextView) this.mLayout.findViewById(C1777R$id.screen_pinning_description)).setText(i3);
            if (isTouchExplorationEnabled) {
                i4 = 4;
            }
            this.mLayout.findViewById(C1777R$id.screen_pinning_back_bg).setVisibility(i4);
            this.mLayout.findViewById(C1777R$id.screen_pinning_back_bg_light).setVisibility(i4);
            addView(this.mLayout, ScreenPinningRequest.this.getRequestLayoutParams(i));
        }

        private void swapChildrenIfRtlAndVertical(View view) {
            if (this.mContext.getResources().getConfiguration().getLayoutDirection() == 1) {
                LinearLayout linearLayout = (LinearLayout) view;
                if (linearLayout.getOrientation() == 1) {
                    int childCount = linearLayout.getChildCount();
                    ArrayList arrayList = new ArrayList(childCount);
                    for (int i = 0; i < childCount; i++) {
                        arrayList.add(linearLayout.getChildAt(i));
                    }
                    linearLayout.removeAllViews();
                    for (int i2 = childCount - 1; i2 >= 0; i2--) {
                        linearLayout.addView((View) arrayList.get(i2));
                    }
                }
            }
        }

        public void onDetachedFromWindow() {
            this.mContext.unregisterReceiver(this.mReceiver);
        }

        /* access modifiers changed from: protected */
        public void onConfigurationChanged() {
            removeAllViews();
            inflateView(RotationUtils.getRotation(this.mContext));
        }
    }
}
