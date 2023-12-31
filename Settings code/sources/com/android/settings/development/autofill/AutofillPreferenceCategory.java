package com.android.settings.development.autofill;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.autofill.AutofillManager;
import androidx.preference.PreferenceCategory;
import com.android.settings.development.autofill.AutofillPreferenceCategory;

public final class AutofillPreferenceCategory extends PreferenceCategory {
    private final ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        public /* synthetic */ void lambda$onChange$0$AutofillPreferenceCategory$1() {
            AutofillPreferenceCategory autofillPreferenceCategory = AutofillPreferenceCategory.this;
            autofillPreferenceCategory.notifyDependencyChange(autofillPreferenceCategory.shouldDisableDependents());
        }

        public void onChange(boolean z, Uri uri, int i) {
            AutofillPreferenceCategory.this.mHandler.postDelayed(new Runnable() {
                public final void run() {
                    AutofillPreferenceCategory.C07391.this.lambda$onChange$0$AutofillPreferenceCategory$1();
                }
            }, 2000);
        }
    };

    public AutofillPreferenceCategory(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContentResolver = context.getContentResolver();
    }

    public void onAttached() {
        super.onAttached();
        this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("autofill_service"), false, this.mSettingsObserver);
    }

    public void onDetached() {
        this.mContentResolver.unregisterContentObserver(this.mSettingsObserver);
        super.onDetached();
    }

    private boolean isAutofillEnabled() {
        AutofillManager autofillManager = (AutofillManager) getContext().getSystemService(AutofillManager.class);
        boolean z = autofillManager != null && autofillManager.isEnabled();
        Log.v("AutofillPreferenceCategory", "isAutofillEnabled(): " + z);
        return z;
    }

    public boolean shouldDisableDependents() {
        boolean z = !isAutofillEnabled();
        Log.v("AutofillPreferenceCategory", "shouldDisableDependents(): " + z);
        return z;
    }
}
