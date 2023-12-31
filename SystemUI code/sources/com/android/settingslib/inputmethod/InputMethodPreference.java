package com.android.settingslib.inputmethod;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.widget.Toast;
import androidx.preference.Preference;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.R$string;
import com.android.settingslib.RestrictedSwitchPreference;

public class InputMethodPreference extends RestrictedSwitchPreference implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final String TAG = "InputMethodPreference";
    private AlertDialog mDialog = null;
    private final boolean mHasPriorityInSorting;
    private final InputMethodInfo mImi;
    private final InputMethodSettingValuesWrapper mInputMethodSettingValues;
    private final boolean mIsAllowedByOrganization;
    private final OnSavePreferenceListener mOnSaveListener;

    public interface OnSavePreferenceListener {
        void onSaveInputMethodPreference(InputMethodPreference inputMethodPreference);
    }

    @VisibleForTesting
    InputMethodPreference(Context context, InputMethodInfo inputMethodInfo, CharSequence charSequence, boolean z, OnSavePreferenceListener onSavePreferenceListener) {
        super(context);
        boolean z2 = false;
        setPersistent(false);
        this.mImi = inputMethodInfo;
        this.mIsAllowedByOrganization = z;
        this.mOnSaveListener = onSavePreferenceListener;
        setSwitchTextOn("");
        setSwitchTextOff("");
        setKey(inputMethodInfo.getId());
        setTitle(charSequence);
        String settingsActivity = inputMethodInfo.getSettingsActivity();
        if (TextUtils.isEmpty(settingsActivity)) {
            setIntent((Intent) null);
        } else {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName(inputMethodInfo.getPackageName(), settingsActivity);
            setIntent(intent);
        }
        this.mInputMethodSettingValues = InputMethodSettingValuesWrapper.getInstance(context);
        if (inputMethodInfo.isSystem() && InputMethodAndSubtypeUtil.isValidNonAuxAsciiCapableIme(inputMethodInfo)) {
            z2 = true;
        }
        this.mHasPriorityInSorting = z2;
        setOnPreferenceClickListener(this);
        setOnPreferenceChangeListener(this);
    }

    private boolean isImeEnabler() {
        return getWidgetLayoutResource() != 0;
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        if (!isImeEnabler()) {
            return false;
        }
        if (isChecked()) {
            setCheckedInternal(false);
            return false;
        }
        if (!this.mImi.isSystem()) {
            showSecurityWarnDialog();
        } else if (this.mImi.getServiceInfo().directBootAware || isTv()) {
            setCheckedInternal(true);
        } else if (!isTv()) {
            showDirectBootWarnDialog();
        }
        return false;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (isImeEnabler()) {
            return true;
        }
        Context context = getContext();
        try {
            Intent intent = getIntent();
            if (intent != null) {
                context.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "IME's Settings Activity Not Found", e);
            Toast.makeText(context, context.getString(R$string.failed_to_open_app_settings_toast, new Object[]{this.mImi.loadLabel(context.getPackageManager())}), 1).show();
        }
        return true;
    }

    private void setCheckedInternal(boolean z) {
        super.setChecked(z);
        this.mOnSaveListener.onSaveInputMethodPreference(this);
        notifyChanged();
    }

    private void showSecurityWarnDialog() {
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(17039380);
        CharSequence loadLabel = this.mImi.getServiceInfo().applicationInfo.loadLabel(context.getPackageManager());
        builder.setMessage(context.getString(R$string.ime_security_warning, new Object[]{loadLabel}));
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                InputMethodPreference.this.lambda$showSecurityWarnDialog$0$InputMethodPreference(dialogInterface, i);
            }
        });
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                InputMethodPreference.this.lambda$showSecurityWarnDialog$1$InputMethodPreference(dialogInterface, i);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public final void onCancel(DialogInterface dialogInterface) {
                InputMethodPreference.this.lambda$showSecurityWarnDialog$2$InputMethodPreference(dialogInterface);
            }
        });
        this.mDialog = builder.create();
        this.mDialog.show();
    }

    public /* synthetic */ void lambda$showSecurityWarnDialog$0$InputMethodPreference(DialogInterface dialogInterface, int i) {
        if (this.mImi.getServiceInfo().directBootAware || isTv()) {
            setCheckedInternal(true);
        } else {
            showDirectBootWarnDialog();
        }
    }

    public /* synthetic */ void lambda$showSecurityWarnDialog$1$InputMethodPreference(DialogInterface dialogInterface, int i) {
        setCheckedInternal(false);
    }

    public /* synthetic */ void lambda$showSecurityWarnDialog$2$InputMethodPreference(DialogInterface dialogInterface) {
        setCheckedInternal(false);
    }

    private boolean isTv() {
        return (getContext().getResources().getConfiguration().uiMode & 15) == 4;
    }

    private void showDirectBootWarnDialog() {
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setMessage(context.getText(R$string.direct_boot_unaware_dialog_message));
        builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                InputMethodPreference.this.lambda$showDirectBootWarnDialog$3$InputMethodPreference(dialogInterface, i);
            }
        });
        builder.setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                InputMethodPreference.this.lambda$showDirectBootWarnDialog$4$InputMethodPreference(dialogInterface, i);
            }
        });
        this.mDialog = builder.create();
        this.mDialog.show();
    }

    public /* synthetic */ void lambda$showDirectBootWarnDialog$3$InputMethodPreference(DialogInterface dialogInterface, int i) {
        setCheckedInternal(true);
    }

    public /* synthetic */ void lambda$showDirectBootWarnDialog$4$InputMethodPreference(DialogInterface dialogInterface, int i) {
        setCheckedInternal(false);
    }
}
