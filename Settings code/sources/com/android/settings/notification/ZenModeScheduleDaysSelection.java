package com.android.settings.notification;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.havoc.config.center.C1715R;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class ZenModeScheduleDaysSelection extends ScrollView {
    private final SimpleDateFormat mDayFormat = new SimpleDateFormat("EEEE");
    /* access modifiers changed from: private */
    public final SparseBooleanArray mDays = new SparseBooleanArray();
    private final LinearLayout mLayout = new LinearLayout(this.mContext);

    /* access modifiers changed from: protected */
    public void onChanged(int[] iArr) {
    }

    public ZenModeScheduleDaysSelection(Context context, int[] iArr) {
        super(context);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C1715R.dimen.zen_schedule_day_margin);
        this.mLayout.setPadding(dimensionPixelSize, 0, dimensionPixelSize, 0);
        addView(this.mLayout);
        if (iArr != null) {
            for (int put : iArr) {
                this.mDays.put(put, true);
            }
        }
        this.mLayout.setOrientation(1);
        Calendar instance = Calendar.getInstance();
        int[] daysOfWeekForLocale = getDaysOfWeekForLocale(instance);
        LayoutInflater from = LayoutInflater.from(context);
        for (final int i : daysOfWeekForLocale) {
            CheckBox checkBox = (CheckBox) from.inflate(C1715R.layout.zen_schedule_rule_day, this, false);
            instance.set(7, i);
            checkBox.setText(this.mDayFormat.format(instance.getTime()));
            checkBox.setChecked(this.mDays.get(i));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    ZenModeScheduleDaysSelection.this.mDays.put(i, z);
                    ZenModeScheduleDaysSelection zenModeScheduleDaysSelection = ZenModeScheduleDaysSelection.this;
                    zenModeScheduleDaysSelection.onChanged(zenModeScheduleDaysSelection.getDays());
                }
            });
            this.mLayout.addView(checkBox);
        }
    }

    /* access modifiers changed from: private */
    public int[] getDays() {
        SparseBooleanArray sparseBooleanArray = new SparseBooleanArray(this.mDays.size());
        for (int i = 0; i < this.mDays.size(); i++) {
            int keyAt = this.mDays.keyAt(i);
            if (this.mDays.valueAt(i)) {
                sparseBooleanArray.put(keyAt, true);
            }
        }
        int[] iArr = new int[sparseBooleanArray.size()];
        for (int i2 = 0; i2 < iArr.length; i2++) {
            iArr[i2] = sparseBooleanArray.keyAt(i2);
        }
        Arrays.sort(iArr);
        return iArr;
    }

    protected static int[] getDaysOfWeekForLocale(Calendar calendar) {
        int[] iArr = new int[7];
        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        for (int i = 0; i < iArr.length; i++) {
            if (firstDayOfWeek > 7) {
                firstDayOfWeek = 1;
            }
            iArr[i] = firstDayOfWeek;
            firstDayOfWeek++;
        }
        return iArr;
    }
}
