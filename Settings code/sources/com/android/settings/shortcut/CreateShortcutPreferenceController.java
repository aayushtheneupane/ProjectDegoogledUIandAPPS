package com.android.settings.shortcut;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import com.android.settings.Settings;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CreateShortcutPreferenceController extends BasePreferenceController {
    private static final Comparator<ResolveInfo> SHORTCUT_COMPARATOR = C1175x3aa097ca.INSTANCE;
    static final String SHORTCUT_ID_PREFIX = "component-shortcut-";
    static final Intent SHORTCUT_PROBE = new Intent("android.intent.action.MAIN").addCategory("com.android.settings.SHORTCUT").addFlags(268435456);
    private static final String TAG = "CreateShortcutPrefCtrl";
    private final ConnectivityManager mConnectivityManager;
    private Activity mHost;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final PackageManager mPackageManager;
    private final ShortcutManager mShortcutManager;

    public int getAvailabilityStatus() {
        return 1;
    }

    public CreateShortcutPreferenceController(Context context, String str) {
        super(context, str);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mShortcutManager = (ShortcutManager) context.getSystemService(ShortcutManager.class);
        this.mPackageManager = context.getPackageManager();
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public void setActivity(Activity activity) {
        this.mHost = activity;
    }

    public void updateState(Preference preference) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
            preferenceGroup.removeAll();
            List<ResolveInfo> queryShortcuts = queryShortcuts();
            Context context = preference.getContext();
            if (!queryShortcuts.isEmpty()) {
                PreferenceCategory preferenceCategory = new PreferenceCategory(context);
                preferenceGroup.addPreference(preferenceCategory);
                PreferenceCategory preferenceCategory2 = preferenceCategory;
                int i = 0;
                for (ResolveInfo next : queryShortcuts) {
                    int i2 = next.priority / 10;
                    if (i2 != i) {
                        PreferenceCategory preferenceCategory3 = new PreferenceCategory(context);
                        preferenceGroup.addPreference(preferenceCategory3);
                        preferenceCategory2 = preferenceCategory3;
                    }
                    Preference preference2 = new Preference(context);
                    preference2.setTitle(next.loadLabel(this.mPackageManager));
                    preference2.setKey(next.activityInfo.getComponentName().flattenToString());
                    preference2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(next) {
                        private final /* synthetic */ ResolveInfo f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final boolean onPreferenceClick(Preference preference) {
                            return CreateShortcutPreferenceController.this.lambda$updateState$0$CreateShortcutPreferenceController(this.f$1, preference);
                        }
                    });
                    preferenceCategory2.addPreference(preference2);
                    i = i2;
                }
            }
        }
    }

    public /* synthetic */ boolean lambda$updateState$0$CreateShortcutPreferenceController(ResolveInfo resolveInfo, Preference preference) {
        if (this.mHost == null) {
            return false;
        }
        this.mHost.setResult(-1, createResultIntent(buildShortcutIntent(resolveInfo), resolveInfo, preference.getTitle()));
        logCreateShortcut(resolveInfo);
        this.mHost.finish();
        return true;
    }

    /* access modifiers changed from: package-private */
    public Intent createResultIntent(Intent intent, ResolveInfo resolveInfo, CharSequence charSequence) {
        Intent createShortcutResultIntent = this.mShortcutManager.createShortcutResultIntent(createShortcutInfo(this.mContext, intent, resolveInfo, charSequence));
        if (createShortcutResultIntent == null) {
            createShortcutResultIntent = new Intent();
        }
        createShortcutResultIntent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(this.mContext, C1715R.mipmap.ic_launcher_settings)).putExtra("android.intent.extra.shortcut.INTENT", intent).putExtra("android.intent.extra.shortcut.NAME", charSequence);
        ActivityInfo activityInfo = resolveInfo.activityInfo;
        if (activityInfo.icon != 0) {
            createShortcutResultIntent.putExtra("android.intent.extra.shortcut.ICON", createIcon(this.mContext, activityInfo.applicationInfo, activityInfo.icon, C1715R.layout.shortcut_badge, this.mContext.getResources().getDimensionPixelSize(C1715R.dimen.shortcut_size)));
        }
        return createShortcutResultIntent;
    }

    /* access modifiers changed from: package-private */
    public List<ResolveInfo> queryShortcuts() {
        ArrayList arrayList = new ArrayList();
        List<ResolveInfo> queryIntentActivities = this.mPackageManager.queryIntentActivities(SHORTCUT_PROBE, 128);
        if (queryIntentActivities == null) {
            return null;
        }
        for (ResolveInfo next : queryIntentActivities) {
            if (!next.activityInfo.name.endsWith(Settings.TetherSettingsActivity.class.getSimpleName()) || this.mConnectivityManager.isTetheringSupported()) {
                if (!next.activityInfo.applicationInfo.isSystemApp()) {
                    Log.d(TAG, "Skipping non-system app: " + next.activityInfo);
                } else {
                    arrayList.add(next);
                }
            }
        }
        Collections.sort(arrayList, SHORTCUT_COMPARATOR);
        return arrayList;
    }

    private void logCreateShortcut(ResolveInfo resolveInfo) {
        ActivityInfo activityInfo;
        if (resolveInfo != null && (activityInfo = resolveInfo.activityInfo) != null) {
            this.mMetricsFeatureProvider.action(this.mContext, 829, activityInfo.name);
        }
    }

    private static Intent buildShortcutIntent(ResolveInfo resolveInfo) {
        return new Intent(SHORTCUT_PROBE).setFlags(335544320).setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
    }

    private static ShortcutInfo createShortcutInfo(Context context, Intent intent, ResolveInfo resolveInfo, CharSequence charSequence) {
        Icon icon;
        ActivityInfo activityInfo = resolveInfo.activityInfo;
        if (activityInfo.icon == 0 || activityInfo.applicationInfo == null) {
            icon = Icon.createWithResource(context, C1715R.C1717drawable.ic_launcher_settings);
        } else {
            icon = Icon.createWithAdaptiveBitmap(createIcon(context, activityInfo.applicationInfo, activityInfo.icon, C1715R.layout.shortcut_badge_maskable, context.getResources().getDimensionPixelSize(C1715R.dimen.shortcut_size_maskable)));
        }
        return new ShortcutInfo.Builder(context, SHORTCUT_ID_PREFIX + intent.getComponent().flattenToShortString()).setShortLabel(charSequence).setIntent(intent).setIcon(icon).build();
    }

    private static Bitmap createIcon(Context context, ApplicationInfo applicationInfo, int i, int i2, int i3) {
        View inflate = LayoutInflater.from(new ContextThemeWrapper(context, 16974372)).inflate(i2, (ViewGroup) null);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i3, 1073741824);
        inflate.measure(makeMeasureSpec, makeMeasureSpec);
        Bitmap createBitmap = Bitmap.createBitmap(inflate.getMeasuredWidth(), inflate.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        try {
            Drawable drawable = context.getPackageManager().getResourcesForApplication(applicationInfo).getDrawable(i);
            if (drawable instanceof LayerDrawable) {
                drawable = ((LayerDrawable) drawable).getDrawable(1);
            }
            ((ImageView) inflate.findViewById(16908294)).setImageDrawable(drawable);
        } catch (PackageManager.NameNotFoundException unused) {
            Log.w(TAG, "Cannot load icon from app " + applicationInfo + ", returning a default icon");
            ((ImageView) inflate.findViewById(16908294)).setImageIcon(Icon.createWithResource(context, C1715R.C1717drawable.ic_launcher_settings));
        }
        inflate.layout(0, 0, inflate.getMeasuredWidth(), inflate.getMeasuredHeight());
        inflate.draw(canvas);
        return createBitmap;
    }

    public static void updateRestoredShortcuts(Context context) {
        ResolveInfo resolveActivity;
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(ShortcutManager.class);
        ArrayList arrayList = new ArrayList();
        for (ShortcutInfo next : shortcutManager.getPinnedShortcuts()) {
            if (next.getId().startsWith(SHORTCUT_ID_PREFIX) && (resolveActivity = context.getPackageManager().resolveActivity(next.getIntent(), 0)) != null) {
                arrayList.add(createShortcutInfo(context, buildShortcutIntent(resolveActivity), resolveActivity, next.getShortLabel()));
            }
        }
        if (!arrayList.isEmpty()) {
            shortcutManager.updateShortcuts(arrayList);
        }
    }

    static /* synthetic */ int lambda$static$1(ResolveInfo resolveInfo, ResolveInfo resolveInfo2) {
        return resolveInfo.priority - resolveInfo2.priority;
    }
}
