package com.android.systemui.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import com.android.systemui.C1777R$id;
import com.android.systemui.C1779R$layout;
import com.android.systemui.C1784R$string;
import com.android.systemui.util.Utils;

public class MediaProjectionPermissionActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private AlertDialog mDialog;
    private String mPackageName;
    private IMediaProjectionManager mService;
    private int mUid;

    public void onCreate(Bundle bundle) {
        CharSequence charSequence;
        super.onCreate(bundle);
        this.mPackageName = getCallingPackage();
        this.mService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        if (this.mPackageName == null) {
            finish();
            return;
        }
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.mPackageName, 0);
            this.mUid = applicationInfo.uid;
            boolean equals = this.mPackageName.equals("com.android.systemui");
            boolean z = Settings.System.getInt(getContentResolver(), "mediaprojection_sysui_ok", 0) == 1;
            try {
                if (this.mService.hasProjectionPermission(this.mUid, this.mPackageName) || (equals && z)) {
                    setResult(-1, getMediaProjectionIntent(this.mUid, this.mPackageName));
                    finish();
                    return;
                }
                if (equals) {
                    Settings.System.putInt(getContentResolver(), "mediaprojection_sysui_ok", 1);
                }
                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(42.0f);
                if (Utils.isHeadlessRemoteDisplayProvider(packageManager, this.mPackageName)) {
                    charSequence = getString(C1784R$string.media_projection_dialog_service_text);
                } else {
                    String charSequence2 = applicationInfo.loadLabel(packageManager).toString();
                    int length = charSequence2.length();
                    int i = 0;
                    while (true) {
                        if (i >= length) {
                            break;
                        }
                        int codePointAt = charSequence2.codePointAt(i);
                        int type = Character.getType(codePointAt);
                        if (type == 13 || type == 15 || type == 14) {
                            charSequence2 = charSequence2.substring(0, i) + "…";
                        } else {
                            i += Character.charCount(codePointAt);
                        }
                    }
                    charSequence2 = charSequence2.substring(0, i) + "…";
                    if (charSequence2.isEmpty()) {
                        charSequence2 = this.mPackageName;
                    }
                    String unicodeWrap = BidiFormatter.getInstance().unicodeWrap(TextUtils.ellipsize(charSequence2, textPaint, 500.0f, TextUtils.TruncateAt.END).toString());
                    String string = getString(C1784R$string.media_projection_dialog_text, new Object[]{unicodeWrap});
                    SpannableString spannableString = new SpannableString(string);
                    int indexOf = string.indexOf(unicodeWrap);
                    if (indexOf >= 0) {
                        spannableString.setSpan(new StyleSpan(1), indexOf, unicodeWrap.length() + indexOf, 0);
                    }
                    charSequence = spannableString;
                }
                String string2 = getString(C1784R$string.media_projection_dialog_title);
                View inflate = View.inflate(this, C1779R$layout.media_projection_dialog_title, (ViewGroup) null);
                ((TextView) inflate.findViewById(C1777R$id.dialog_title)).setText(string2);
                this.mDialog = new AlertDialog.Builder(this).setCustomTitle(inflate).setMessage(charSequence).setPositiveButton(C1784R$string.media_projection_action_text, this).setNegativeButton(17039360, this).setOnCancelListener(this).create();
                this.mDialog.create();
                this.mDialog.getButton(-1).setFilterTouchesWhenObscured(true);
                Window window = this.mDialog.getWindow();
                window.setType(2003);
                window.addSystemFlags(524288);
                this.mDialog.show();
            } catch (RemoteException e) {
                Log.e("MediaProjectionPermissionActivity", "Error checking projection permissions", e);
                finish();
            }
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e("MediaProjectionPermissionActivity", "unable to look up package name", e2);
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        if (r2 == null) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002f, code lost:
        if (r2 != null) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        r2.dismiss();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0034, code lost:
        finish();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0037, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onClick(android.content.DialogInterface r2, int r3) {
        /*
            r1 = this;
            r2 = -1
            if (r3 != r2) goto L_0x002d
            int r3 = r1.mUid     // Catch:{ RemoteException -> 0x0011 }
            java.lang.String r0 = r1.mPackageName     // Catch:{ RemoteException -> 0x0011 }
            android.content.Intent r3 = r1.getMediaProjectionIntent(r3, r0)     // Catch:{ RemoteException -> 0x0011 }
            r1.setResult(r2, r3)     // Catch:{ RemoteException -> 0x0011 }
            goto L_0x002d
        L_0x000f:
            r2 = move-exception
            goto L_0x0022
        L_0x0011:
            r2 = move-exception
            java.lang.String r3 = "MediaProjectionPermissionActivity"
            java.lang.String r0 = "Error granting projection permission"
            android.util.Log.e(r3, r0, r2)     // Catch:{ all -> 0x000f }
            r2 = 0
            r1.setResult(r2)     // Catch:{ all -> 0x000f }
            android.app.AlertDialog r2 = r1.mDialog
            if (r2 == 0) goto L_0x0034
            goto L_0x0031
        L_0x0022:
            android.app.AlertDialog r3 = r1.mDialog
            if (r3 == 0) goto L_0x0029
            r3.dismiss()
        L_0x0029:
            r1.finish()
            throw r2
        L_0x002d:
            android.app.AlertDialog r2 = r1.mDialog
            if (r2 == 0) goto L_0x0034
        L_0x0031:
            r2.dismiss()
        L_0x0034:
            r1.finish()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.MediaProjectionPermissionActivity.onClick(android.content.DialogInterface, int):void");
    }

    private Intent getMediaProjectionIntent(int i, String str) throws RemoteException {
        IMediaProjection createProjection = this.mService.createProjection(i, str, 0, false);
        Intent intent = new Intent();
        intent.putExtra("android.media.projection.extra.EXTRA_MEDIA_PROJECTION", createProjection.asBinder());
        return intent;
    }

    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }
}
