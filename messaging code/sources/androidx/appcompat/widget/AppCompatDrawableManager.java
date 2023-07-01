package androidx.appcompat.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.appcompat.C0126R;
import androidx.appcompat.p018a.p019a.C0130a;
import androidx.appcompat.widget.ResourceManagerInternal;
import androidx.core.graphics.ColorUtils;

public final class AppCompatDrawableManager {
    private static final boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static final PorterDuff.Mode DEFAULT_MODE = PorterDuff.Mode.SRC_IN;
    private static AppCompatDrawableManager INSTANCE = null;
    private static final String TAG = "AppCompatDrawableManag";
    private ResourceManagerInternal mResourceManager;

    public static synchronized AppCompatDrawableManager get() {
        AppCompatDrawableManager appCompatDrawableManager;
        synchronized (AppCompatDrawableManager.class) {
            if (INSTANCE == null) {
                preload();
            }
            appCompatDrawableManager = INSTANCE;
        }
        return appCompatDrawableManager;
    }

    public static synchronized PorterDuffColorFilter getPorterDuffColorFilter(int i, PorterDuff.Mode mode) {
        PorterDuffColorFilter porterDuffColorFilter;
        synchronized (AppCompatDrawableManager.class) {
            porterDuffColorFilter = ResourceManagerInternal.getPorterDuffColorFilter(i, mode);
        }
        return porterDuffColorFilter;
    }

    public static synchronized void preload() {
        synchronized (AppCompatDrawableManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new AppCompatDrawableManager();
                INSTANCE.mResourceManager = ResourceManagerInternal.get();
                INSTANCE.mResourceManager.setHooks(new ResourceManagerInternal.ResourceManagerHooks() {
                    private final int[] COLORFILTER_COLOR_BACKGROUND_MULTIPLY = {C0126R.C0127drawable.abc_popup_background_mtrl_mult, C0126R.C0127drawable.abc_cab_background_internal_bg, C0126R.C0127drawable.abc_menu_hardkey_panel_mtrl_mult};
                    private final int[] COLORFILTER_COLOR_CONTROL_ACTIVATED = {C0126R.C0127drawable.abc_textfield_activated_mtrl_alpha, C0126R.C0127drawable.abc_textfield_search_activated_mtrl_alpha, C0126R.C0127drawable.abc_cab_background_top_mtrl_alpha, C0126R.C0127drawable.abc_text_cursor_material, C0126R.C0127drawable.abc_text_select_handle_left_mtrl_dark, C0126R.C0127drawable.abc_text_select_handle_middle_mtrl_dark, C0126R.C0127drawable.abc_text_select_handle_right_mtrl_dark, C0126R.C0127drawable.abc_text_select_handle_left_mtrl_light, C0126R.C0127drawable.abc_text_select_handle_middle_mtrl_light, C0126R.C0127drawable.abc_text_select_handle_right_mtrl_light};
                    private final int[] COLORFILTER_TINT_COLOR_CONTROL_NORMAL = {C0126R.C0127drawable.abc_textfield_search_default_mtrl_alpha, C0126R.C0127drawable.abc_textfield_default_mtrl_alpha, C0126R.C0127drawable.abc_ab_share_pack_mtrl_alpha};
                    private final int[] TINT_CHECKABLE_BUTTON_LIST = {C0126R.C0127drawable.abc_btn_check_material, C0126R.C0127drawable.abc_btn_radio_material, C0126R.C0127drawable.abc_btn_check_material_anim, C0126R.C0127drawable.abc_btn_radio_material_anim};
                    private final int[] TINT_COLOR_CONTROL_NORMAL = {C0126R.C0127drawable.abc_ic_commit_search_api_mtrl_alpha, C0126R.C0127drawable.abc_seekbar_tick_mark_material, C0126R.C0127drawable.abc_ic_menu_share_mtrl_alpha, C0126R.C0127drawable.abc_ic_menu_copy_mtrl_am_alpha, C0126R.C0127drawable.abc_ic_menu_cut_mtrl_alpha, C0126R.C0127drawable.abc_ic_menu_selectall_mtrl_alpha, C0126R.C0127drawable.abc_ic_menu_paste_mtrl_am_alpha};
                    private final int[] TINT_COLOR_CONTROL_STATE_LIST = {C0126R.C0127drawable.abc_tab_indicator_material, C0126R.C0127drawable.abc_textfield_search_material};

                    private boolean arrayContains(int[] iArr, int i) {
                        for (int i2 : iArr) {
                            if (i2 == i) {
                                return true;
                            }
                        }
                        return false;
                    }

                    private ColorStateList createBorderlessButtonColorStateList(Context context) {
                        return createButtonColorStateList(context, 0);
                    }

                    private ColorStateList createButtonColorStateList(Context context, int i) {
                        int themeAttrColor = ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorControlHighlight);
                        int disabledThemeAttrColor = ThemeUtils.getDisabledThemeAttrColor(context, C0126R.attr.colorButtonNormal);
                        return new ColorStateList(new int[][]{ThemeUtils.DISABLED_STATE_SET, ThemeUtils.PRESSED_STATE_SET, ThemeUtils.FOCUSED_STATE_SET, ThemeUtils.EMPTY_STATE_SET}, new int[]{disabledThemeAttrColor, ColorUtils.compositeColors(themeAttrColor, i), ColorUtils.compositeColors(themeAttrColor, i), i});
                    }

                    private ColorStateList createColoredButtonColorStateList(Context context) {
                        return createButtonColorStateList(context, ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorAccent));
                    }

                    private ColorStateList createDefaultButtonColorStateList(Context context) {
                        return createButtonColorStateList(context, ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorButtonNormal));
                    }

                    private ColorStateList createSwitchThumbColorStateList(Context context) {
                        int[][] iArr = new int[3][];
                        int[] iArr2 = new int[3];
                        ColorStateList themeAttrColorStateList = ThemeUtils.getThemeAttrColorStateList(context, C0126R.attr.colorSwitchThumbNormal);
                        if (themeAttrColorStateList == null || !themeAttrColorStateList.isStateful()) {
                            iArr[0] = ThemeUtils.DISABLED_STATE_SET;
                            iArr2[0] = ThemeUtils.getDisabledThemeAttrColor(context, C0126R.attr.colorSwitchThumbNormal);
                            iArr[1] = ThemeUtils.CHECKED_STATE_SET;
                            iArr2[1] = ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorControlActivated);
                            iArr[2] = ThemeUtils.EMPTY_STATE_SET;
                            iArr2[2] = ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorSwitchThumbNormal);
                        } else {
                            iArr[0] = ThemeUtils.DISABLED_STATE_SET;
                            iArr2[0] = themeAttrColorStateList.getColorForState(iArr[0], 0);
                            iArr[1] = ThemeUtils.CHECKED_STATE_SET;
                            iArr2[1] = ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorControlActivated);
                            iArr[2] = ThemeUtils.EMPTY_STATE_SET;
                            iArr2[2] = themeAttrColorStateList.getDefaultColor();
                        }
                        return new ColorStateList(iArr, iArr2);
                    }

                    private void setPorterDuffColorFilter(Drawable drawable, int i, PorterDuff.Mode mode) {
                        if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                            drawable = drawable.mutate();
                        }
                        if (mode == null) {
                            mode = AppCompatDrawableManager.DEFAULT_MODE;
                        }
                        drawable.setColorFilter(AppCompatDrawableManager.getPorterDuffColorFilter(i, mode));
                    }

                    public Drawable createDrawableFor(ResourceManagerInternal resourceManagerInternal, Context context, int i) {
                        if (i != C0126R.C0127drawable.abc_cab_background_top_material) {
                            return null;
                        }
                        return new LayerDrawable(new Drawable[]{resourceManagerInternal.getDrawable(context, C0126R.C0127drawable.abc_cab_background_internal_bg), resourceManagerInternal.getDrawable(context, C0126R.C0127drawable.abc_cab_background_top_mtrl_alpha)});
                    }

                    public ColorStateList getTintListForDrawableRes(Context context, int i) {
                        if (i == C0126R.C0127drawable.abc_edit_text_material) {
                            return C0130a.getColorStateList(context, C0126R.color.abc_tint_edittext);
                        }
                        if (i == C0126R.C0127drawable.abc_switch_track_mtrl_alpha) {
                            return C0130a.getColorStateList(context, C0126R.color.abc_tint_switch_track);
                        }
                        if (i == C0126R.C0127drawable.abc_switch_thumb_material) {
                            return createSwitchThumbColorStateList(context);
                        }
                        if (i == C0126R.C0127drawable.abc_btn_default_mtrl_shape) {
                            return createDefaultButtonColorStateList(context);
                        }
                        if (i == C0126R.C0127drawable.abc_btn_borderless_material) {
                            return createButtonColorStateList(context, 0);
                        }
                        if (i == C0126R.C0127drawable.abc_btn_colored_material) {
                            return createColoredButtonColorStateList(context);
                        }
                        if (i == C0126R.C0127drawable.abc_spinner_mtrl_am_alpha || i == C0126R.C0127drawable.abc_spinner_textfield_background_material) {
                            return C0130a.getColorStateList(context, C0126R.color.abc_tint_spinner);
                        }
                        if (arrayContains(this.TINT_COLOR_CONTROL_NORMAL, i)) {
                            return ThemeUtils.getThemeAttrColorStateList(context, C0126R.attr.colorControlNormal);
                        }
                        if (arrayContains(this.TINT_COLOR_CONTROL_STATE_LIST, i)) {
                            return C0130a.getColorStateList(context, C0126R.color.abc_tint_default);
                        }
                        if (arrayContains(this.TINT_CHECKABLE_BUTTON_LIST, i)) {
                            return C0130a.getColorStateList(context, C0126R.color.abc_tint_btn_checkable);
                        }
                        if (i == C0126R.C0127drawable.abc_seekbar_thumb_material) {
                            return C0130a.getColorStateList(context, C0126R.color.abc_tint_seek_thumb);
                        }
                        return null;
                    }

                    public PorterDuff.Mode getTintModeForDrawableRes(int i) {
                        if (i == C0126R.C0127drawable.abc_switch_thumb_material) {
                            return PorterDuff.Mode.MULTIPLY;
                        }
                        return null;
                    }

                    public boolean tintDrawable(Context context, int i, Drawable drawable) {
                        if (i == C0126R.C0127drawable.abc_seekbar_track_material) {
                            LayerDrawable layerDrawable = (LayerDrawable) drawable;
                            setPorterDuffColorFilter(layerDrawable.findDrawableByLayerId(16908288), ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorControlNormal), AppCompatDrawableManager.DEFAULT_MODE);
                            setPorterDuffColorFilter(layerDrawable.findDrawableByLayerId(16908303), ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorControlNormal), AppCompatDrawableManager.DEFAULT_MODE);
                            setPorterDuffColorFilter(layerDrawable.findDrawableByLayerId(16908301), ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorControlActivated), AppCompatDrawableManager.DEFAULT_MODE);
                            return true;
                        } else if (i != C0126R.C0127drawable.abc_ratingbar_material && i != C0126R.C0127drawable.abc_ratingbar_indicator_material && i != C0126R.C0127drawable.abc_ratingbar_small_material) {
                            return false;
                        } else {
                            LayerDrawable layerDrawable2 = (LayerDrawable) drawable;
                            setPorterDuffColorFilter(layerDrawable2.findDrawableByLayerId(16908288), ThemeUtils.getDisabledThemeAttrColor(context, C0126R.attr.colorControlNormal), AppCompatDrawableManager.DEFAULT_MODE);
                            setPorterDuffColorFilter(layerDrawable2.findDrawableByLayerId(16908303), ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorControlActivated), AppCompatDrawableManager.DEFAULT_MODE);
                            setPorterDuffColorFilter(layerDrawable2.findDrawableByLayerId(16908301), ThemeUtils.getThemeAttrColor(context, C0126R.attr.colorControlActivated), AppCompatDrawableManager.DEFAULT_MODE);
                            return true;
                        }
                    }

                    /* JADX WARNING: Removed duplicated region for block: B:18:0x0047  */
                    /* JADX WARNING: Removed duplicated region for block: B:25:0x0062 A[RETURN] */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public boolean tintDrawableUsingColorFilter(android.content.Context r7, int r8, android.graphics.drawable.Drawable r9) {
                        /*
                            r6 = this;
                            android.graphics.PorterDuff$Mode r0 = androidx.appcompat.widget.AppCompatDrawableManager.DEFAULT_MODE
                            int[] r1 = r6.COLORFILTER_TINT_COLOR_CONTROL_NORMAL
                            boolean r1 = r6.arrayContains(r1, r8)
                            r2 = 16842801(0x1010031, float:2.3693695E-38)
                            r3 = -1
                            r4 = 0
                            r5 = 1
                            if (r1 == 0) goto L_0x0017
                            int r2 = androidx.appcompat.C0126R.attr.colorControlNormal
                        L_0x0014:
                            r8 = r3
                        L_0x0015:
                            r6 = r5
                            goto L_0x0045
                        L_0x0017:
                            int[] r1 = r6.COLORFILTER_COLOR_CONTROL_ACTIVATED
                            boolean r1 = r6.arrayContains(r1, r8)
                            if (r1 == 0) goto L_0x0022
                            int r2 = androidx.appcompat.C0126R.attr.colorControlActivated
                            goto L_0x0014
                        L_0x0022:
                            int[] r1 = r6.COLORFILTER_COLOR_BACKGROUND_MULTIPLY
                            boolean r6 = r6.arrayContains(r1, r8)
                            if (r6 == 0) goto L_0x002d
                            android.graphics.PorterDuff$Mode r0 = android.graphics.PorterDuff.Mode.MULTIPLY
                            goto L_0x0014
                        L_0x002d:
                            int r6 = androidx.appcompat.C0126R.C0127drawable.abc_list_divider_mtrl_alpha
                            if (r8 != r6) goto L_0x003d
                            r2 = 16842800(0x1010030, float:2.3693693E-38)
                            r6 = 1109603123(0x42233333, float:40.8)
                            int r6 = java.lang.Math.round(r6)
                            r8 = r6
                            goto L_0x0015
                        L_0x003d:
                            int r6 = androidx.appcompat.C0126R.C0127drawable.abc_dialog_material_background
                            if (r8 != r6) goto L_0x0042
                            goto L_0x0014
                        L_0x0042:
                            r8 = r3
                            r6 = r4
                            r2 = r6
                        L_0x0045:
                            if (r6 == 0) goto L_0x0062
                            boolean r6 = androidx.appcompat.widget.DrawableUtils.canSafelyMutateDrawable(r9)
                            if (r6 == 0) goto L_0x0051
                            android.graphics.drawable.Drawable r9 = r9.mutate()
                        L_0x0051:
                            int r6 = androidx.appcompat.widget.ThemeUtils.getThemeAttrColor(r7, r2)
                            android.graphics.PorterDuffColorFilter r6 = androidx.appcompat.widget.AppCompatDrawableManager.getPorterDuffColorFilter(r6, r0)
                            r9.setColorFilter(r6)
                            if (r8 == r3) goto L_0x0061
                            r9.setAlpha(r8)
                        L_0x0061:
                            return r5
                        L_0x0062:
                            return r4
                        */
                        throw new UnsupportedOperationException("Method not decompiled: androidx.appcompat.widget.AppCompatDrawableManager.C02601.tintDrawableUsingColorFilter(android.content.Context, int, android.graphics.drawable.Drawable):boolean");
                    }
                });
            }
        }
    }

    static void tintDrawable(Drawable drawable, TintInfo tintInfo, int[] iArr) {
        ResourceManagerInternal.tintDrawable(drawable, tintInfo, iArr);
    }

    public synchronized Drawable getDrawable(Context context, int i) {
        return this.mResourceManager.getDrawable(context, i);
    }

    /* access modifiers changed from: package-private */
    public synchronized ColorStateList getTintList(Context context, int i) {
        return this.mResourceManager.getTintList(context, i);
    }

    public synchronized void onConfigurationChanged(Context context) {
        this.mResourceManager.onConfigurationChanged(context);
    }

    /* access modifiers changed from: package-private */
    public synchronized Drawable onDrawableLoadedFromResources(Context context, VectorEnabledTintResources vectorEnabledTintResources, int i) {
        return this.mResourceManager.onDrawableLoadedFromResources(context, vectorEnabledTintResources, i);
    }

    /* access modifiers changed from: package-private */
    public boolean tintDrawableUsingColorFilter(Context context, int i, Drawable drawable) {
        return this.mResourceManager.tintDrawableUsingColorFilter(context, i, drawable);
    }

    /* access modifiers changed from: package-private */
    public synchronized Drawable getDrawable(Context context, int i, boolean z) {
        return this.mResourceManager.getDrawable(context, i, z);
    }
}