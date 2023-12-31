package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C1777R$id;
import com.android.systemui.C1779R$layout;
import com.android.systemui.C1784R$string;
import com.android.systemui.CustomSettingsService;
import com.android.systemui.Dependency;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.ReverseLinearLayout;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.tuner.TunerService;
import java.util.Objects;

public class NavigationBarInflaterView extends FrameLayout implements NavigationModeController.ModeChangedListener, TunerService.Tunable, CustomSettingsService.CustomSettingsObserver {
    private boolean mAlternativeOrder;
    @VisibleForTesting
    SparseArray<ButtonDispatcher> mButtonDispatchers;
    private String mCurrentLayout;
    protected FrameLayout mHorizontal;
    private boolean mInverseLayout;
    private boolean mIsVertical;
    protected LayoutInflater mLandscapeInflater;
    private View mLastLandscape;
    private View mLastPortrait;
    protected LayoutInflater mLayoutInflater;
    private int mNavBarMode = 0;
    private OverviewProxyService mOverviewProxyService;
    private boolean mUsingCustomLayout;
    protected FrameLayout mVertical;

    public NavigationBarInflaterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        createInflaters();
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mNavBarMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void createInflaters() {
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        Configuration configuration = new Configuration();
        configuration.setTo(this.mContext.getResources().getConfiguration());
        configuration.orientation = 2;
        this.mLandscapeInflater = LayoutInflater.from(this.mContext.createConfigurationContext(configuration));
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        inflateChildren();
        clearViews();
        inflateLayout(getDefaultLayout());
    }

    private void inflateChildren() {
        removeAllViews();
        this.mHorizontal = (FrameLayout) this.mLayoutInflater.inflate(C1779R$layout.navigation_layout, this, false);
        addView(this.mHorizontal);
        this.mVertical = (FrameLayout) this.mLayoutInflater.inflate(C1779R$layout.navigation_layout_vertical, this, false);
        addView(this.mVertical);
        updateAlternativeOrder();
    }

    /* access modifiers changed from: protected */
    public String getDefaultLayout() {
        int i;
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            if (showDpadArrowKeys()) {
                i = C1784R$string.config_navBarLayoutHandleArrows;
            } else {
                i = C1784R$string.config_navBarLayoutHandle;
            }
        } else if (this.mOverviewProxyService.shouldShowSwipeUpUI()) {
            i = C1784R$string.config_navBarLayoutQuickstep;
        } else {
            i = C1784R$string.config_navBarLayout;
        }
        return getContext().getString(i);
    }

    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
        onLikelyDefaultLayoutChange();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_nav_bar_inverse");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_nav_bar");
        ((CustomSettingsService) Dependency.get(CustomSettingsService.class)).addIntObserver(this, "navigation_bar_menu_arrow_keys");
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        ((NavigationModeController) Dependency.get(NavigationModeController.class)).removeListener(this);
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        ((CustomSettingsService) Dependency.get(CustomSettingsService.class)).removeObserver(this);
        super.onDetachedFromWindow();
    }

    public void onTuningChanged(String str, String str2) {
        if ("sysui_nav_bar_inverse".equals(str)) {
            this.mInverseLayout = TunerService.parseIntegerSwitch(str2, false);
            updateLayoutInversion();
        }
        if ("sysui_nav_bar".equals(str)) {
            setNavigationBarLayout(str2);
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateLayoutInversion();
    }

    public void setNavigationBarLayout(String str) {
        if (!Objects.equals(this.mCurrentLayout, str)) {
            this.mUsingCustomLayout = str != null;
            clearViews();
            inflateLayout(str);
        }
    }

    public void onLikelyDefaultLayoutChange() {
        if (!this.mUsingCustomLayout) {
            String defaultLayout = getDefaultLayout();
            if (!Objects.equals(this.mCurrentLayout, defaultLayout)) {
                clearViews();
                inflateLayout(defaultLayout);
            }
        }
    }

    public void setButtonDispatchers(SparseArray<ButtonDispatcher> sparseArray) {
        this.mButtonDispatchers = sparseArray;
        for (int i = 0; i < sparseArray.size(); i++) {
            initiallyFill(sparseArray.valueAt(i));
        }
    }

    /* access modifiers changed from: package-private */
    public void updateButtonDispatchersCurrentView() {
        if (this.mButtonDispatchers != null) {
            FrameLayout frameLayout = this.mIsVertical ? this.mVertical : this.mHorizontal;
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).setCurrentView(frameLayout);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setVertical(boolean z) {
        if (z != this.mIsVertical) {
            this.mIsVertical = z;
        }
    }

    /* access modifiers changed from: package-private */
    public void setAlternativeOrder(boolean z) {
        if (z != this.mAlternativeOrder) {
            this.mAlternativeOrder = z;
            updateAlternativeOrder();
        }
    }

    private void updateAlternativeOrder() {
        updateAlternativeOrder(this.mHorizontal.findViewById(C1777R$id.ends_group));
        updateAlternativeOrder(this.mHorizontal.findViewById(C1777R$id.center_group));
        updateAlternativeOrder(this.mVertical.findViewById(C1777R$id.ends_group));
        updateAlternativeOrder(this.mVertical.findViewById(C1777R$id.center_group));
    }

    private void updateAlternativeOrder(View view) {
        if (view instanceof ReverseLinearLayout) {
            ((ReverseLinearLayout) view).setAlternativeOrder(this.mAlternativeOrder);
        }
    }

    private void initiallyFill(ButtonDispatcher buttonDispatcher) {
        addAll(buttonDispatcher, (ViewGroup) this.mHorizontal.findViewById(C1777R$id.ends_group));
        addAll(buttonDispatcher, (ViewGroup) this.mHorizontal.findViewById(C1777R$id.center_group));
        addAll(buttonDispatcher, (ViewGroup) this.mVertical.findViewById(C1777R$id.ends_group));
        addAll(buttonDispatcher, (ViewGroup) this.mVertical.findViewById(C1777R$id.center_group));
    }

    private void addAll(ButtonDispatcher buttonDispatcher, ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i).getId() == buttonDispatcher.getId()) {
                buttonDispatcher.addView(viewGroup.getChildAt(i));
            }
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                addAll(buttonDispatcher, (ViewGroup) viewGroup.getChildAt(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void inflateLayout(String str) {
        this.mCurrentLayout = str;
        if (str == null) {
            str = getDefaultLayout();
        }
        String[] split = str.split(";", 3);
        if (split.length != 3) {
            Log.d("NavBarInflater", "Invalid layout.");
            split = getDefaultLayout().split(";", 3);
        }
        String[] split2 = split[0].split(",");
        String[] split3 = split[1].split(",");
        String[] split4 = split[2].split(",");
        inflateButtons(split2, (ViewGroup) this.mHorizontal.findViewById(C1777R$id.ends_group), false, true);
        inflateButtons(split2, (ViewGroup) this.mVertical.findViewById(C1777R$id.ends_group), true, true);
        inflateButtons(split3, (ViewGroup) this.mHorizontal.findViewById(C1777R$id.center_group), false, false);
        inflateButtons(split3, (ViewGroup) this.mVertical.findViewById(C1777R$id.center_group), true, false);
        addGravitySpacer((LinearLayout) this.mHorizontal.findViewById(C1777R$id.ends_group));
        addGravitySpacer((LinearLayout) this.mVertical.findViewById(C1777R$id.ends_group));
        inflateButtons(split4, (ViewGroup) this.mHorizontal.findViewById(C1777R$id.ends_group), false, false);
        inflateButtons(split4, (ViewGroup) this.mVertical.findViewById(C1777R$id.ends_group), true, false);
        updateButtonDispatchersCurrentView();
    }

    private void updateLayoutInversion() {
        if (!this.mInverseLayout) {
            setLayoutDirection(2);
        } else if (this.mContext.getResources().getConfiguration().getLayoutDirection() == 1) {
            setLayoutDirection(0);
        } else {
            setLayoutDirection(1);
        }
    }

    private void addGravitySpacer(LinearLayout linearLayout) {
        linearLayout.addView(new Space(this.mContext), new LinearLayout.LayoutParams(0, 0, 1.0f));
    }

    private void inflateButtons(String[] strArr, ViewGroup viewGroup, boolean z, boolean z2) {
        for (String inflateButton : strArr) {
            inflateButton(inflateButton, viewGroup, z, z2);
        }
    }

    /* access modifiers changed from: protected */
    public View inflateButton(String str, ViewGroup viewGroup, boolean z, boolean z2) {
        View createView = createView(str, viewGroup, z ? this.mLandscapeInflater : this.mLayoutInflater);
        if (createView == null) {
            return null;
        }
        View applySize = applySize(createView, str, z, z2);
        viewGroup.addView(applySize);
        addToDispatchers(applySize);
        View view = z ? this.mLastLandscape : this.mLastPortrait;
        View childAt = applySize instanceof ReverseLinearLayout.ReverseRelativeLayout ? ((ReverseLinearLayout.ReverseRelativeLayout) applySize).getChildAt(0) : applySize;
        if (view != null) {
            childAt.setAccessibilityTraversalAfter(view.getId());
        }
        if (z) {
            this.mLastLandscape = childAt;
        } else {
            this.mLastPortrait = childAt;
        }
        return applySize;
    }

    private View applySize(View view, String str, boolean z, boolean z2) {
        String extractSize = extractSize(str);
        if (extractSize == null) {
            return view;
        }
        if (extractSize.contains("W") || extractSize.contains("A")) {
            ReverseLinearLayout.ReverseRelativeLayout reverseRelativeLayout = new ReverseLinearLayout.ReverseRelativeLayout(this.mContext);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.getLayoutParams());
            int i = z ? z2 ? 48 : 80 : z2 ? 8388611 : 8388613;
            if (extractSize.endsWith("WC")) {
                i = 17;
            } else if (extractSize.endsWith("C")) {
                i = 16;
            }
            reverseRelativeLayout.setDefaultGravity(i);
            reverseRelativeLayout.setGravity(i);
            reverseRelativeLayout.addView(view, layoutParams);
            if (extractSize.contains("W")) {
                reverseRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams(0, -1, Float.parseFloat(extractSize.substring(0, extractSize.indexOf("W")))));
            } else {
                reverseRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams((int) convertDpToPx(this.mContext, Float.parseFloat(extractSize.substring(0, extractSize.indexOf("A")))), -1));
            }
            reverseRelativeLayout.setClipChildren(false);
            reverseRelativeLayout.setClipToPadding(false);
            return reverseRelativeLayout;
        }
        float parseFloat = Float.parseFloat(extractSize);
        ViewGroup.LayoutParams layoutParams2 = view.getLayoutParams();
        layoutParams2.width = (int) (((float) layoutParams2.width) * parseFloat);
        return view;
    }

    private View createView(String str, ViewGroup viewGroup, LayoutInflater layoutInflater) {
        String extractButton = extractButton(str);
        if ("left".equals(extractButton)) {
            extractButton = extractButton("space");
        } else if ("right".equals(extractButton)) {
            extractButton = extractButton("menu_ime");
        }
        if ("home".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.home, viewGroup, false);
        }
        if ("back".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.back, viewGroup, false);
        }
        if ("recent".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.recent_apps, viewGroup, false);
        }
        if ("menu_ime".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.menu_ime, viewGroup, false);
        }
        if ("space".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.nav_key_space, viewGroup, false);
        }
        if ("clipboard".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.clipboard, viewGroup, false);
        }
        if ("contextual".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.contextual, viewGroup, false);
        }
        if ("home_handle".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.home_handle, viewGroup, false);
        }
        if ("ime_switcher".equals(extractButton)) {
            return layoutInflater.inflate(C1779R$layout.ime_switcher, viewGroup, false);
        }
        if (!extractButton.startsWith("key")) {
            return null;
        }
        String extractImage = extractImage(extractButton);
        int extractKeycode = extractKeycode(extractButton);
        View inflate = layoutInflater.inflate(C1779R$layout.custom_key, viewGroup, false);
        KeyButtonView keyButtonView = (KeyButtonView) inflate;
        keyButtonView.setCode(extractKeycode);
        if (extractImage != null) {
            if (extractImage.contains(":")) {
                keyButtonView.loadAsync(Icon.createWithContentUri(extractImage));
            } else if (extractImage.contains("/")) {
                int indexOf = extractImage.indexOf(47);
                keyButtonView.loadAsync(Icon.createWithResource(extractImage.substring(0, indexOf), Integer.parseInt(extractImage.substring(indexOf + 1))));
            }
        }
        return inflate;
    }

    public static String extractImage(String str) {
        if (!str.contains(":")) {
            return null;
        }
        return str.substring(str.indexOf(":") + 1, str.indexOf(")"));
    }

    public static int extractKeycode(String str) {
        if (!str.contains("(")) {
            return 1;
        }
        return Integer.parseInt(str.substring(str.indexOf("(") + 1, str.indexOf(":")));
    }

    public static String extractSize(String str) {
        if (!str.contains("[")) {
            return null;
        }
        return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
    }

    public static String extractButton(String str) {
        if (!str.contains("[")) {
            return str;
        }
        return str.substring(0, str.indexOf("["));
    }

    private void addToDispatchers(View view) {
        SparseArray<ButtonDispatcher> sparseArray = this.mButtonDispatchers;
        if (sparseArray != null) {
            int indexOfKey = sparseArray.indexOfKey(view.getId());
            if (indexOfKey >= 0) {
                this.mButtonDispatchers.valueAt(indexOfKey).addView(view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    addToDispatchers(viewGroup.getChildAt(i));
                }
            }
        }
    }

    private void clearViews() {
        if (this.mButtonDispatchers != null) {
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).clear();
            }
        }
        clearAllChildren((ViewGroup) this.mHorizontal.findViewById(C1777R$id.nav_buttons));
        clearAllChildren((ViewGroup) this.mVertical.findViewById(C1777R$id.nav_buttons));
    }

    private void clearAllChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i).getId() != C1777R$id.dpad_group) {
                ((ViewGroup) viewGroup.getChildAt(i)).removeAllViews();
            }
        }
    }

    private static float convertDpToPx(Context context, float f) {
        return f * context.getResources().getDisplayMetrics().density;
    }

    private boolean showDpadArrowKeys() {
        return Settings.System.getIntForUser(getContext().getContentResolver(), "navigation_bar_menu_arrow_keys", 0, -2) != 0;
    }

    public void onIntSettingChanged(String str, Integer num) {
        onLikelyDefaultLayoutChange();
    }
}
