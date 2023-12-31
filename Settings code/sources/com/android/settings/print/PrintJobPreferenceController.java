package com.android.settings.print;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.print.PrintJob;
import android.print.PrintJobInfo;
import android.text.format.DateUtils;
import com.havoc.config.center.C1715R;

public class PrintJobPreferenceController extends PrintJobPreferenceControllerBase {
    public PrintJobPreferenceController(Context context, String str) {
        super(context, str);
    }

    /* access modifiers changed from: protected */
    public void updateUi() {
        PrintJob printJob = getPrintJob();
        if (printJob == null) {
            this.mFragment.finish();
        } else if (printJob.isCancelled() || printJob.isCompleted()) {
            this.mFragment.finish();
        } else {
            PrintJobInfo info = printJob.getInfo();
            int state = info.getState();
            if (state == 1) {
                this.mPreference.setTitle((CharSequence) this.mContext.getString(C1715R.string.print_configuring_state_title_template, new Object[]{info.getLabel()}));
            } else if (state == 2 || state == 3) {
                if (!printJob.getInfo().isCancelling()) {
                    this.mPreference.setTitle((CharSequence) this.mContext.getString(C1715R.string.print_printing_state_title_template, new Object[]{info.getLabel()}));
                } else {
                    this.mPreference.setTitle((CharSequence) this.mContext.getString(C1715R.string.print_cancelling_state_title_template, new Object[]{info.getLabel()}));
                }
            } else if (state != 4) {
                if (state == 6) {
                    this.mPreference.setTitle((CharSequence) this.mContext.getString(C1715R.string.print_failed_state_title_template, new Object[]{info.getLabel()}));
                }
            } else if (!printJob.getInfo().isCancelling()) {
                this.mPreference.setTitle((CharSequence) this.mContext.getString(C1715R.string.print_blocked_state_title_template, new Object[]{info.getLabel()}));
            } else {
                this.mPreference.setTitle((CharSequence) this.mContext.getString(C1715R.string.print_cancelling_state_title_template, new Object[]{info.getLabel()}));
            }
            this.mPreference.setSummary((CharSequence) this.mContext.getString(C1715R.string.print_job_summary, new Object[]{info.getPrinterName(), DateUtils.formatSameDayTime(info.getCreationTime(), info.getCreationTime(), 3, 3)}));
            TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{16843817});
            int color = obtainStyledAttributes.getColor(0, 0);
            obtainStyledAttributes.recycle();
            int state2 = info.getState();
            if (state2 == 2 || state2 == 3) {
                Drawable drawable = this.mContext.getDrawable(17302787);
                drawable.setTint(color);
                this.mPreference.setIcon(drawable);
            } else if (state2 == 4 || state2 == 6) {
                Drawable drawable2 = this.mContext.getDrawable(17302788);
                drawable2.setTint(color);
                this.mPreference.setIcon(drawable2);
            }
        }
    }
}
