package com.android.settings.password;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.IntentSender;
import android.os.RemoteException;
import android.os.UserManager;
import com.android.internal.widget.LockPatternUtils;

public class ConfirmDeviceCredentialUtils {
    public static void checkForPendingIntent(Activity activity) {
        int intExtra = activity.getIntent().getIntExtra("android.intent.extra.TASK_ID", -1);
        if (intExtra != -1) {
            try {
                ActivityManager.getService().startActivityFromRecents(intExtra, ActivityOptions.makeBasic().toBundle());
                return;
            } catch (RemoteException unused) {
            }
        }
        IntentSender intentSender = (IntentSender) activity.getIntent().getParcelableExtra("android.intent.extra.INTENT");
        if (intentSender != null) {
            try {
                activity.startIntentSenderForResult(intentSender, -1, (Intent) null, 0, 0, 0);
            } catch (IntentSender.SendIntentException unused2) {
            }
        }
    }

    public static void reportSuccessfulAttempt(LockPatternUtils lockPatternUtils, UserManager userManager, int i) {
        lockPatternUtils.reportSuccessfulPasswordAttempt(i);
        if (userManager.isManagedProfile(i)) {
            lockPatternUtils.userPresent(i);
        }
    }
}
