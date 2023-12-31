package com.android.settingslib.notification;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.policy.PhoneWindow;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;
import java.util.Arrays;

public class ZenDurationDialog {
    protected static final int ALWAYS_ASK_CONDITION_INDEX = 2;
    protected static final int COUNTDOWN_CONDITION_INDEX = 1;
    private static final int DEFAULT_BUCKET_INDEX;
    protected static final int FOREVER_CONDITION_INDEX = 0;
    protected static final int MAX_BUCKET_MINUTES;
    private static final int[] MINUTE_BUCKETS = ZenModeConfig.MINUTE_BUCKETS;
    protected static final int MIN_BUCKET_MINUTES;
    private int MAX_MANUAL_DND_OPTIONS = 3;
    protected int mBucketIndex = -1;
    protected Context mContext;
    protected LayoutInflater mLayoutInflater;
    private RadioGroup mZenRadioGroup;
    protected LinearLayout mZenRadioGroupContent;

    static {
        int[] iArr = MINUTE_BUCKETS;
        MIN_BUCKET_MINUTES = iArr[0];
        MAX_BUCKET_MINUTES = iArr[iArr.length - 1];
        DEFAULT_BUCKET_INDEX = Arrays.binarySearch(iArr, 60);
    }

    public ZenDurationDialog(Context context) {
        this.mContext = context;
    }

    public Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        setupDialog(builder);
        return builder.create();
    }

    public void setupDialog(AlertDialog.Builder builder) {
        final int i = Settings.Secure.getInt(this.mContext.getContentResolver(), "zen_duration", 0);
        builder.setTitle(R$string.zen_mode_duration_settings_title);
        builder.setNegativeButton(R$string.cancel, (DialogInterface.OnClickListener) null);
        builder.setPositiveButton(R$string.okay, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ZenDurationDialog.this.updateZenDuration(i);
            }
        });
        View contentView = getContentView();
        setupRadioButtons(i);
        builder.setView(contentView);
    }

    /* access modifiers changed from: protected */
    public void updateZenDuration(int i) {
        int checkedRadioButtonId = this.mZenRadioGroup.getCheckedRadioButtonId();
        int i2 = 0;
        int i3 = Settings.Secure.getInt(this.mContext.getContentResolver(), "zen_duration", 0);
        if (checkedRadioButtonId == 0) {
            MetricsLogger.action(this.mContext, 1343);
        } else if (checkedRadioButtonId == 1) {
            i2 = getConditionTagAt(checkedRadioButtonId).countdownZenDuration;
            MetricsLogger.action(this.mContext, 1342, i2);
        } else if (checkedRadioButtonId != 2) {
            i2 = i3;
        } else {
            i2 = -1;
            MetricsLogger.action(this.mContext, 1344);
        }
        if (i != i2) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "zen_duration", i2);
        }
    }

    /* access modifiers changed from: protected */
    public View getContentView() {
        if (this.mLayoutInflater == null) {
            this.mLayoutInflater = new PhoneWindow(this.mContext).getLayoutInflater();
        }
        View inflate = this.mLayoutInflater.inflate(R$layout.zen_mode_duration_dialog, (ViewGroup) null);
        ScrollView scrollView = (ScrollView) inflate.findViewById(R$id.zen_duration_container);
        this.mZenRadioGroup = (RadioGroup) scrollView.findViewById(R$id.zen_radio_buttons);
        this.mZenRadioGroupContent = (LinearLayout) scrollView.findViewById(R$id.zen_radio_buttons_content);
        for (int i = 0; i < this.MAX_MANUAL_DND_OPTIONS; i++) {
            View inflate2 = this.mLayoutInflater.inflate(R$layout.zen_mode_radio_button, this.mZenRadioGroup, false);
            this.mZenRadioGroup.addView(inflate2);
            inflate2.setId(i);
            View inflate3 = this.mLayoutInflater.inflate(R$layout.zen_mode_condition, this.mZenRadioGroupContent, false);
            inflate3.setId(this.MAX_MANUAL_DND_OPTIONS + i);
            this.mZenRadioGroupContent.addView(inflate3);
        }
        return inflate;
    }

    /* access modifiers changed from: protected */
    public void setupRadioButtons(int i) {
        int i2 = i == 0 ? 0 : i > 0 ? 1 : 2;
        bindTag(i, this.mZenRadioGroupContent.getChildAt(0), 0);
        bindTag(i, this.mZenRadioGroupContent.getChildAt(1), 1);
        bindTag(i, this.mZenRadioGroupContent.getChildAt(2), 2);
        getConditionTagAt(i2).f66rb.setChecked(true);
    }

    private void bindTag(int i, View view, int i2) {
        final ConditionTag conditionTag;
        if (view.getTag() != null) {
            conditionTag = (ConditionTag) view.getTag();
        } else {
            conditionTag = new ConditionTag();
        }
        view.setTag(conditionTag);
        if (conditionTag.f66rb == null) {
            conditionTag.f66rb = (RadioButton) this.mZenRadioGroup.getChildAt(i2);
        }
        if (i <= 0) {
            conditionTag.countdownZenDuration = MINUTE_BUCKETS[DEFAULT_BUCKET_INDEX];
        } else {
            conditionTag.countdownZenDuration = i;
        }
        conditionTag.f66rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    conditionTag.f66rb.setChecked(true);
                }
            }
        });
        updateUi(conditionTag, view, i2);
    }

    /* access modifiers changed from: protected */
    public ConditionTag getConditionTagAt(int i) {
        return (ConditionTag) this.mZenRadioGroupContent.getChildAt(i).getTag();
    }

    private void setupUi(final ConditionTag conditionTag, View view) {
        if (conditionTag.lines == null) {
            conditionTag.lines = view.findViewById(16908290);
        }
        if (conditionTag.line1 == null) {
            conditionTag.line1 = (TextView) view.findViewById(16908308);
        }
        view.findViewById(16908309).setVisibility(8);
        conditionTag.lines.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                conditionTag.f66rb.setChecked(true);
            }
        });
    }

    private void updateButtons(final ConditionTag conditionTag, final View view, final int i) {
        ImageView imageView = (ImageView) view.findViewById(16908313);
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ZenDurationDialog.this.onClickTimeButton(view, conditionTag, false, i);
            }
        });
        ImageView imageView2 = (ImageView) view.findViewById(16908314);
        imageView2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ZenDurationDialog.this.onClickTimeButton(view, conditionTag, true, i);
            }
        });
        long j = (long) conditionTag.countdownZenDuration;
        boolean z = true;
        if (i == 1) {
            imageView.setVisibility(0);
            imageView2.setVisibility(0);
            imageView.setEnabled(j > ((long) MIN_BUCKET_MINUTES));
            if (conditionTag.countdownZenDuration == MAX_BUCKET_MINUTES) {
                z = false;
            }
            imageView2.setEnabled(z);
            float f = 1.0f;
            imageView.setAlpha(imageView.isEnabled() ? 1.0f : 0.5f);
            if (!imageView2.isEnabled()) {
                f = 0.5f;
            }
            imageView2.setAlpha(f);
            return;
        }
        imageView.setVisibility(8);
        imageView2.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void updateUi(ConditionTag conditionTag, View view, int i) {
        String str;
        if (conditionTag.lines == null) {
            setupUi(conditionTag, view);
        }
        updateButtons(conditionTag, view, i);
        if (i == 0) {
            str = this.mContext.getString(R$string.zen_mode_forever);
        } else if (i == 1) {
            str = ZenModeConfig.toTimeCondition(this.mContext, conditionTag.countdownZenDuration, ActivityManager.getCurrentUser(), false).line1;
        } else if (i != 2) {
            str = "";
        } else {
            str = this.mContext.getString(R$string.zen_mode_duration_always_prompt_title);
        }
        conditionTag.line1.setText(str);
    }

    /* access modifiers changed from: protected */
    public void onClickTimeButton(View view, ConditionTag conditionTag, boolean z, int i) {
        int i2;
        int i3;
        int length = MINUTE_BUCKETS.length;
        int i4 = this.mBucketIndex;
        int i5 = 0;
        int i6 = -1;
        if (i4 == -1) {
            long j = (long) conditionTag.countdownZenDuration;
            while (true) {
                if (i5 >= length) {
                    i2 = -1;
                    break;
                }
                i3 = z ? i5 : (length - 1) - i5;
                i2 = MINUTE_BUCKETS[i3];
                if ((!z || ((long) i2) <= j) && (z || ((long) i2) >= j)) {
                    i5++;
                }
            }
            this.mBucketIndex = i3;
            if (i2 == -1) {
                this.mBucketIndex = DEFAULT_BUCKET_INDEX;
                i2 = MINUTE_BUCKETS[this.mBucketIndex];
            }
        } else {
            int i7 = length - 1;
            if (z) {
                i6 = 1;
            }
            this.mBucketIndex = Math.max(0, Math.min(i7, i4 + i6));
            i2 = MINUTE_BUCKETS[this.mBucketIndex];
        }
        conditionTag.countdownZenDuration = i2;
        bindTag(i2, view, i);
        conditionTag.f66rb.setChecked(true);
    }

    protected static class ConditionTag {
        public int countdownZenDuration;
        public TextView line1;
        public View lines;

        /* renamed from: rb */
        public RadioButton f66rb;

        protected ConditionTag() {
        }
    }
}
