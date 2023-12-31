package com.android.settings.display;

import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.android.settings.RestrictedListPreference;
import com.android.settingslib.RestrictedLockUtils;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;

public class TimeoutListPreference extends RestrictedListPreference {
    /* access modifiers changed from: private */
    public RestrictedLockUtils.EnforcedAdmin mAdmin;
    private final CharSequence[] mInitialEntries = getEntries();
    private final CharSequence[] mInitialValues = getEntryValues();

    public TimeoutListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener onClickListener) {
        super.onPrepareDialogBuilder(builder, onClickListener);
        if (this.mAdmin != null) {
            builder.setView((int) C1715R.layout.admin_disabled_other_options_footer);
        } else {
            builder.setView((View) null);
        }
    }

    /* access modifiers changed from: protected */
    public void onDialogCreated(Dialog dialog) {
        super.onDialogCreated(dialog);
        dialog.create();
        if (this.mAdmin != null) {
            dialog.findViewById(C1715R.C1718id.admin_disabled_other_options).findViewById(C1715R.C1718id.admin_more_details_link).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(TimeoutListPreference.this.getContext(), TimeoutListPreference.this.mAdmin);
                }
            });
        }
    }

    public void removeUnusableTimeouts(long j, RestrictedLockUtils.EnforcedAdmin enforcedAdmin) {
        if (((DevicePolicyManager) getContext().getSystemService("device_policy")) != null) {
            if (enforcedAdmin != null || this.mAdmin != null || isDisabledByAdmin()) {
                if (enforcedAdmin == null) {
                    j = Long.MAX_VALUE;
                }
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                int i = 0;
                while (true) {
                    CharSequence[] charSequenceArr = this.mInitialValues;
                    if (i >= charSequenceArr.length) {
                        break;
                    }
                    if (Long.parseLong(charSequenceArr[i].toString()) <= j) {
                        arrayList.add(this.mInitialEntries[i]);
                        arrayList2.add(this.mInitialValues[i]);
                    }
                    i++;
                }
                if (arrayList2.size() == 0) {
                    setDisabledByAdmin(enforcedAdmin);
                    return;
                }
                setDisabledByAdmin((RestrictedLockUtils.EnforcedAdmin) null);
                if (arrayList.size() != getEntries().length) {
                    int parseInt = Integer.parseInt(getValue());
                    setEntries((CharSequence[]) arrayList.toArray(new CharSequence[0]));
                    setEntryValues((CharSequence[]) arrayList2.toArray(new CharSequence[0]));
                    this.mAdmin = enforcedAdmin;
                    if (((long) parseInt) <= j) {
                        setValue(String.valueOf(parseInt));
                    } else if (arrayList2.size() <= 0 || Long.parseLong(((CharSequence) arrayList2.get(arrayList2.size() - 1)).toString()) != j) {
                        Log.w("TimeoutListPreference", "Default to longest timeout. Value disabled by admin:" + parseInt);
                        setValue(((CharSequence) arrayList2.get(arrayList2.size() + -1)).toString());
                    } else {
                        setValue(String.valueOf(j));
                    }
                }
            }
        }
    }
}
