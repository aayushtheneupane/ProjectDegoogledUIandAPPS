package com.android.settings.security;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.Utils;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class VisiblePatternProfilePreferenceController extends TogglePreferenceController implements LifecycleObserver, OnResume {
    private static final String KEY_VISIBLE_PATTERN_PROFILE = "visiblepattern_profile";
    private static final String TAG = "VisPtnProfPrefCtrl";
    private final LockPatternUtils mLockPatternUtils;
    private Preference mPreference;
    private final int mProfileChallengeUserId;
    private final UserManager mUm;
    private final int mUserId;

    public VisiblePatternProfilePreferenceController(Context context) {
        this(context, (Lifecycle) null);
    }

    public VisiblePatternProfilePreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_VISIBLE_PATTERN_PROFILE);
        this.mUserId = UserHandle.myUserId();
        this.mUm = (UserManager) context.getSystemService("user");
        this.mLockPatternUtils = FeatureFactory.getFactory(context).getSecurityFeatureProvider().getLockPatternUtils(context);
        this.mProfileChallengeUserId = Utils.getManagedProfileId(this.mUm, this.mUserId);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public int getAvailabilityStatus() {
        FutureTask futureTask = new FutureTask(new Callable() {
            public final Object call() {
                return VisiblePatternProfilePreferenceController.this.mo11595x7f007b50();
            }
        });
        try {
            futureTask.run();
            return ((Integer) futureTask.get()).intValue();
        } catch (InterruptedException | ExecutionException unused) {
            Log.w(TAG, "Error getting lock pattern state.");
            return 4;
        }
    }

    /* renamed from: lambda$getAvailabilityStatus$0$VisiblePatternProfilePreferenceController */
    public /* synthetic */ Integer mo11595x7f007b50() throws Exception {
        boolean isSecure = this.mLockPatternUtils.isSecure(this.mProfileChallengeUserId);
        boolean z = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(this.mProfileChallengeUserId) == 65536;
        if (!isSecure || !z) {
            return 4;
        }
        return 0;
    }

    public boolean isChecked() {
        return this.mLockPatternUtils.isVisiblePatternEnabled(this.mProfileChallengeUserId);
    }

    public boolean setChecked(boolean z) {
        if (Utils.startQuietModeDialogIfNecessary(this.mContext, this.mUm, this.mProfileChallengeUserId)) {
            return false;
        }
        this.mLockPatternUtils.setVisiblePatternEnabled(z, this.mProfileChallengeUserId);
        return true;
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mPreference = preferenceScreen.findPreference(getPreferenceKey());
    }

    public void onResume() {
        this.mPreference.setVisible(isAvailable());
    }
}
