package com.android.settings.wifi;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.wifi.AccessPoint;
import com.havoc.config.center.C1715R;

public class WifiDialog extends AlertDialog implements WifiConfigUiBase, DialogInterface.OnClickListener {
    private final AccessPoint mAccessPoint;
    private WifiConfigController mController;
    private boolean mHideSubmitButton;
    private final WifiDialogListener mListener;
    private final int mMode;
    private View mView;

    public interface WifiDialogListener {
        void onForget(WifiDialog wifiDialog) {
        }

        void onScan(WifiDialog wifiDialog, String str) {
        }

        void onSubmit(WifiDialog wifiDialog) {
        }
    }

    public static WifiDialog createModal(Context context, WifiDialogListener wifiDialogListener, AccessPoint accessPoint, int i) {
        return new WifiDialog(context, wifiDialogListener, accessPoint, i, 0, i == 0);
    }

    public static WifiDialog createModal(Context context, WifiDialogListener wifiDialogListener, AccessPoint accessPoint, int i, int i2) {
        return new WifiDialog(context, wifiDialogListener, accessPoint, i, i2, i == 0);
    }

    WifiDialog(Context context, WifiDialogListener wifiDialogListener, AccessPoint accessPoint, int i, int i2, boolean z) {
        super(context, i2);
        this.mMode = i;
        this.mListener = wifiDialogListener;
        this.mAccessPoint = accessPoint;
        this.mHideSubmitButton = z;
    }

    public WifiConfigController getController() {
        return this.mController;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        this.mView = getLayoutInflater().inflate(C1715R.layout.wifi_dialog, (ViewGroup) null);
        setView(this.mView);
        this.mController = new WifiConfigController(this, this.mView, this.mAccessPoint, this.mMode);
        super.onCreate(bundle);
        if (this.mHideSubmitButton) {
            this.mController.hideSubmitButton();
        } else {
            this.mController.enableSubmitIfAppropriate();
        }
        if (this.mAccessPoint == null) {
            this.mController.hideForgetButton();
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        $$Lambda$WifiDialog$A0XFUDDETwsfRxrVaOXME4wrgzI r0 = new View.OnClickListener() {
            public final void onClick(View view) {
                WifiDialog.this.lambda$onStart$0$WifiDialog(view);
            }
        };
        ImageButton imageButton = (ImageButton) findViewById(C1715R.C1718id.ssid_scanner_button);
        imageButton.setOnClickListener(r0);
        ImageButton imageButton2 = (ImageButton) findViewById(C1715R.C1718id.password_scanner_button);
        imageButton2.setOnClickListener(r0);
        if (this.mHideSubmitButton) {
            imageButton.setVisibility(8);
            imageButton2.setVisibility(8);
        }
    }

    public /* synthetic */ void lambda$onStart$0$WifiDialog(View view) {
        String str;
        if (this.mListener != null) {
            AccessPoint accessPoint = this.mAccessPoint;
            if (accessPoint == null) {
                str = ((TextView) findViewById(C1715R.C1718id.ssid)).getText().toString();
            } else {
                str = accessPoint.getSsidStr();
            }
            this.mListener.onScan(this, str);
        }
    }

    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        this.mController.updatePassword();
    }

    public void dispatchSubmit() {
        WifiDialogListener wifiDialogListener = this.mListener;
        if (wifiDialogListener != null) {
            wifiDialogListener.onSubmit(this);
        }
        dismiss();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        WifiDialogListener wifiDialogListener = this.mListener;
        if (wifiDialogListener == null) {
            return;
        }
        if (i != -3) {
            if (i == -1) {
                wifiDialogListener.onSubmit(this);
            }
        } else if (WifiUtils.isNetworkLockedDown(getContext(), this.mAccessPoint.getConfig())) {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getContext(), RestrictedLockUtilsInternal.getDeviceOwner(getContext()));
        } else {
            this.mListener.onForget(this);
        }
    }

    public Button getSubmitButton() {
        return getButton(-1);
    }

    public Button getForgetButton() {
        return getButton(-3);
    }

    public void setSubmitButton(CharSequence charSequence) {
        setButton(-1, charSequence, this);
    }

    public void setForgetButton(CharSequence charSequence) {
        setButton(-3, charSequence, this);
    }

    public void setCancelButton(CharSequence charSequence) {
        setButton(-2, charSequence, this);
    }
}
