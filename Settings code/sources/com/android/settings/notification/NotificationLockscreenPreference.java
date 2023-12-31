package com.android.settings.notification;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.appcompat.app.AlertDialog;
import com.android.settings.RestrictedListPreference;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.havoc.config.center.C1715R;

public class NotificationLockscreenPreference extends RestrictedListPreference {
    /* access modifiers changed from: private */
    public RestrictedLockUtils.EnforcedAdmin mAdminRestrictingRemoteInput;
    /* access modifiers changed from: private */
    public boolean mAllowRemoteInput;
    private Listener mListener;
    private boolean mRemoteInputCheckBoxEnabled = true;
    /* access modifiers changed from: private */
    public boolean mShowRemoteInput;
    private int mUserId = UserHandle.myUserId();

    /* access modifiers changed from: protected */
    public boolean isAutoClosePreference() {
        return false;
    }

    public NotificationLockscreenPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onClick() {
        Context context = getContext();
        if (!Utils.startQuietModeDialogIfNecessary(context, UserManager.get(context), this.mUserId)) {
            super.onClick();
        }
    }

    /* access modifiers changed from: protected */
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener onClickListener) {
        this.mListener = new Listener(onClickListener);
        builder.setSingleChoiceItems(createListAdapter(builder.getContext()), getSelectedValuePos(), (DialogInterface.OnClickListener) this.mListener);
        boolean z = true;
        this.mShowRemoteInput = getEntryValues().length == 3;
        if (Settings.Secure.getInt(getContext().getContentResolver(), "lock_screen_allow_remote_input", 0) == 0) {
            z = false;
        }
        this.mAllowRemoteInput = z;
        builder.setView((int) C1715R.layout.lockscreen_remote_input);
    }

    /* access modifiers changed from: protected */
    public void onDialogCreated(Dialog dialog) {
        super.onDialogCreated(dialog);
        dialog.create();
        CheckBox checkBox = (CheckBox) dialog.findViewById(C1715R.C1718id.lockscreen_remote_input);
        boolean z = true;
        checkBox.setChecked(!this.mAllowRemoteInput);
        checkBox.setOnCheckedChangeListener(this.mListener);
        if (this.mAdminRestrictingRemoteInput != null) {
            z = false;
        }
        checkBox.setEnabled(z);
        dialog.findViewById(C1715R.C1718id.restricted_lock_icon_remote_input).setVisibility(this.mAdminRestrictingRemoteInput == null ? 8 : 0);
        if (this.mAdminRestrictingRemoteInput != null) {
            checkBox.setClickable(false);
            dialog.findViewById(16908855).setOnClickListener(this.mListener);
        }
    }

    /* access modifiers changed from: protected */
    public void onDialogStateRestored(Dialog dialog, Bundle bundle) {
        super.onDialogStateRestored(dialog, bundle);
        int checkedItemPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
        View findViewById = dialog.findViewById(16908855);
        findViewById.setVisibility(checkboxVisibilityForSelectedIndex(checkedItemPosition, this.mShowRemoteInput));
        this.mListener.setView(findViewById);
    }

    /* access modifiers changed from: protected */
    public void onDialogClosed(boolean z) {
        super.onDialogClosed(z);
        Settings.Secure.putInt(getContext().getContentResolver(), "lock_screen_allow_remote_input", this.mAllowRemoteInput ? 1 : 0);
    }

    /* access modifiers changed from: private */
    public int checkboxVisibilityForSelectedIndex(int i, boolean z) {
        return (i != 1 || !z || !this.mRemoteInputCheckBoxEnabled) ? 8 : 0;
    }

    private class Listener implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
        private final DialogInterface.OnClickListener mInner;
        private View mView;

        public Listener(DialogInterface.OnClickListener onClickListener) {
            this.mInner = onClickListener;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            this.mInner.onClick(dialogInterface, i);
            int checkedItemPosition = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
            View view = this.mView;
            if (view != null) {
                NotificationLockscreenPreference notificationLockscreenPreference = NotificationLockscreenPreference.this;
                view.setVisibility(notificationLockscreenPreference.checkboxVisibilityForSelectedIndex(checkedItemPosition, notificationLockscreenPreference.mShowRemoteInput));
            }
        }

        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            boolean unused = NotificationLockscreenPreference.this.mAllowRemoteInput = !z;
        }

        public void setView(View view) {
            this.mView = view;
        }

        public void onClick(View view) {
            if (view.getId() == 16908855) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(NotificationLockscreenPreference.this.getContext(), NotificationLockscreenPreference.this.mAdminRestrictingRemoteInput);
            }
        }
    }
}
