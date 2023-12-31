package com.android.systemui.theme;

import android.content.om.OverlayInfo;
import android.content.om.OverlayManager;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ThemeOverlayManager {
    static final String ANDROID_PACKAGE = "android";
    static final String OVERLAY_CATEGORY_COLOR = "android.theme.customization.accent_color";
    static final String OVERLAY_CATEGORY_FONT = "android.theme.customization.font";
    static final String OVERLAY_CATEGORY_ICON_ANDROID = "android.theme.customization.icon_pack.android";
    static final String OVERLAY_CATEGORY_ICON_LAUNCHER = "android.theme.customization.icon_pack.launcher";
    static final String OVERLAY_CATEGORY_ICON_SETTINGS = "android.theme.customization.icon_pack.settings";
    static final String OVERLAY_CATEGORY_ICON_SYSUI = "android.theme.customization.icon_pack.systemui";
    static final String OVERLAY_CATEGORY_ICON_THEME_PICKER = "android.theme.customization.icon_pack.themepicker";
    static final String OVERLAY_CATEGORY_PRIMARY = "android.theme.customization.primary_color";
    static final String OVERLAY_CATEGORY_SHAPE = "android.theme.customization.adaptive_icon_shape";
    static final String SETTINGS_PACKAGE = "com.android.settings";
    static final Set<String> SYSTEM_USER_CATEGORIES = Sets.newHashSet(new String[]{OVERLAY_CATEGORY_COLOR, OVERLAY_CATEGORY_PRIMARY, OVERLAY_CATEGORY_FONT, OVERLAY_CATEGORY_SHAPE, OVERLAY_CATEGORY_ICON_ANDROID, OVERLAY_CATEGORY_ICON_SYSUI});
    static final String SYSUI_PACKAGE = "com.android.systemui";
    static final List<String> THEME_CATEGORIES = Lists.newArrayList(new String[]{OVERLAY_CATEGORY_ICON_LAUNCHER, OVERLAY_CATEGORY_SHAPE, OVERLAY_CATEGORY_FONT, OVERLAY_CATEGORY_COLOR, OVERLAY_CATEGORY_PRIMARY, OVERLAY_CATEGORY_ICON_ANDROID, OVERLAY_CATEGORY_ICON_SYSUI, OVERLAY_CATEGORY_ICON_SETTINGS, OVERLAY_CATEGORY_ICON_THEME_PICKER});
    private final Map<String, String> mCategoryToTargetPackage = new ArrayMap();
    private final Executor mExecutor;
    private final String mLauncherPackage;
    private final OverlayManager mOverlayManager;
    private final Map<String, Set<String>> mTargetPackageToCategories = new ArrayMap();
    private final String mThemePickerPackage;

    ThemeOverlayManager(OverlayManager overlayManager, Executor executor, String str, String str2) {
        this.mOverlayManager = overlayManager;
        this.mExecutor = executor;
        this.mLauncherPackage = str;
        this.mThemePickerPackage = str2;
        this.mTargetPackageToCategories.put(ANDROID_PACKAGE, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_COLOR, OVERLAY_CATEGORY_PRIMARY, OVERLAY_CATEGORY_FONT, OVERLAY_CATEGORY_SHAPE, OVERLAY_CATEGORY_ICON_ANDROID}));
        this.mTargetPackageToCategories.put(SYSUI_PACKAGE, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_ICON_SYSUI}));
        this.mTargetPackageToCategories.put(SETTINGS_PACKAGE, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_ICON_SETTINGS}));
        this.mTargetPackageToCategories.put(this.mLauncherPackage, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_ICON_LAUNCHER}));
        this.mTargetPackageToCategories.put(this.mThemePickerPackage, Sets.newHashSet(new String[]{OVERLAY_CATEGORY_ICON_THEME_PICKER}));
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_COLOR, ANDROID_PACKAGE);
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_PRIMARY, ANDROID_PACKAGE);
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_FONT, ANDROID_PACKAGE);
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_SHAPE, ANDROID_PACKAGE);
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_ICON_ANDROID, ANDROID_PACKAGE);
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_ICON_SYSUI, SYSUI_PACKAGE);
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_ICON_SETTINGS, SETTINGS_PACKAGE);
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_ICON_LAUNCHER, this.mLauncherPackage);
        this.mCategoryToTargetPackage.put(OVERLAY_CATEGORY_ICON_THEME_PICKER, this.mThemePickerPackage);
    }

    /* access modifiers changed from: package-private */
    public void applyCurrentUserOverlays(Map<String, String> map, Set<UserHandle> set) {
        HashSet hashSet = new HashSet(THEME_CATEGORIES);
        hashSet.removeAll(map.keySet());
        ArrayList arrayList = new ArrayList();
        ((Set) hashSet.stream().map(new Function() {
            public final Object apply(Object obj) {
                return ThemeOverlayManager.this.lambda$applyCurrentUserOverlays$0$ThemeOverlayManager((String) obj);
            }
        }).collect(Collectors.toSet())).forEach(new Consumer(arrayList) {
            private final /* synthetic */ List f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ThemeOverlayManager.this.lambda$applyCurrentUserOverlays$1$ThemeOverlayManager(this.f$1, (String) obj);
            }
        });
        Map map2 = (Map) arrayList.stream().filter(new Predicate() {
            public final boolean test(Object obj) {
                return ThemeOverlayManager.this.lambda$applyCurrentUserOverlays$2$ThemeOverlayManager((OverlayInfo) obj);
            }
        }).filter(new Predicate(hashSet) {
            private final /* synthetic */ Set f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return this.f$0.contains(((OverlayInfo) obj).category);
            }
        }).filter($$Lambda$ThemeOverlayManager$vK2aROqMaNCgMb7ixs5bp0NF79c.INSTANCE).collect(Collectors.toMap($$Lambda$ThemeOverlayManager$tpreaivLMVK4R3Uf26BCg27Af8.INSTANCE, $$Lambda$ThemeOverlayManager$GlioDk646gj_04NkaTcsRN_awI4.INSTANCE));
        for (String next : THEME_CATEGORIES) {
            if (map.containsKey(next)) {
                setEnabled(map.get(next), next, set, true);
            } else if (map2.containsKey(next)) {
                setEnabled((String) map2.get(next), next, set, false);
            }
        }
    }

    public /* synthetic */ String lambda$applyCurrentUserOverlays$0$ThemeOverlayManager(String str) {
        return this.mCategoryToTargetPackage.get(str);
    }

    public /* synthetic */ void lambda$applyCurrentUserOverlays$1$ThemeOverlayManager(List list, String str) {
        list.addAll(this.mOverlayManager.getOverlayInfosForTarget(str, UserHandle.SYSTEM));
    }

    public /* synthetic */ boolean lambda$applyCurrentUserOverlays$2$ThemeOverlayManager(OverlayInfo overlayInfo) {
        return this.mTargetPackageToCategories.get(overlayInfo.targetPackageName).contains(overlayInfo.category);
    }

    private void setEnabled(String str, String str2, Set<UserHandle> set, boolean z) {
        for (UserHandle enabledAsync : set) {
            setEnabledAsync(str, enabledAsync, z);
        }
        if (!set.contains(UserHandle.SYSTEM) && SYSTEM_USER_CATEGORIES.contains(str2)) {
            setEnabledAsync(str, UserHandle.SYSTEM, z);
        }
    }

    private void setEnabledAsync(String str, UserHandle userHandle, boolean z) {
        this.mExecutor.execute(new Runnable(str, userHandle, z) {
            private final /* synthetic */ String f$1;
            private final /* synthetic */ UserHandle f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ThemeOverlayManager.this.lambda$setEnabledAsync$7$ThemeOverlayManager(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$setEnabledAsync$7$ThemeOverlayManager(String str, UserHandle userHandle, boolean z) {
        if (z) {
            try {
                this.mOverlayManager.setEnabledExclusiveInCategory(str, userHandle);
            } catch (IllegalStateException e) {
                Log.e("ThemeOverlayManager", String.format("setEnabled failed: %s %s %b", new Object[]{str, userHandle, Boolean.valueOf(z)}), e);
            }
        } else {
            this.mOverlayManager.setEnabled(str, false, userHandle);
        }
    }
}
