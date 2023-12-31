package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.C1784R$string;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.policy.DateView;
import java.util.Date;
import java.util.Locale;

public class DateView extends TextView {
    private final Date mCurrentTime = new Date();
    /* access modifiers changed from: private */
    public DateFormat mDateFormat;
    private String mDatePattern;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.TIME_TICK".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.LOCALE_CHANGED".equals(action)) {
                if ("android.intent.action.LOCALE_CHANGED".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                    DateView.this.getHandler().post(new Runnable() {
                        public final void run() {
                            DateView.C15211.this.lambda$onReceive$0$DateView$1();
                        }
                    });
                }
                DateView.this.getHandler().post(new Runnable() {
                    public final void run() {
                        DateView.C15211.this.lambda$onReceive$1$DateView$1();
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onReceive$0$DateView$1() {
            DateFormat unused = DateView.this.mDateFormat = null;
        }

        public /* synthetic */ void lambda$onReceive$1$DateView$1() {
            DateView.this.updateClock();
        }
    };
    private String mLastText;

    /* JADX INFO: finally extract failed */
    public DateView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.DateView, 0, 0);
        try {
            this.mDatePattern = obtainStyledAttributes.getString(R$styleable.DateView_datePattern);
            obtainStyledAttributes.recycle();
            if (this.mDatePattern == null) {
                this.mDatePattern = getContext().getString(C1784R$string.system_ui_date_pattern);
            }
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        getContext().registerReceiver(this.mIntentReceiver, intentFilter, (String) null, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
        updateClock();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mDateFormat = null;
        getContext().unregisterReceiver(this.mIntentReceiver);
    }

    /* access modifiers changed from: protected */
    public void updateClock() {
        if (this.mDateFormat == null) {
            DateFormat instanceForSkeleton = DateFormat.getInstanceForSkeleton(this.mDatePattern, Locale.getDefault());
            instanceForSkeleton.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
            this.mDateFormat = instanceForSkeleton;
        }
        this.mCurrentTime.setTime(System.currentTimeMillis());
        String format = this.mDateFormat.format(this.mCurrentTime);
        if (!format.equals(this.mLastText)) {
            setText(format);
            this.mLastText = format;
        }
    }
}
