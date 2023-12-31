package com.android.systemui.volume;

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.notification.Condition;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.volume.Util;
import com.android.systemui.C1777R$id;
import com.android.systemui.C1778R$integer;
import com.android.systemui.C1779R$layout;
import com.android.systemui.C1784R$string;
import com.android.systemui.Prefs;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.Interaction;
import com.android.systemui.volume.SegmentedButtons;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

public class ZenModePanel extends FrameLayout {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("ZenModePanel", 3);
    /* access modifiers changed from: private */
    public static final int DEFAULT_BUCKET_INDEX;
    private static final int MAX_BUCKET_MINUTES;
    /* access modifiers changed from: private */
    public static final int[] MINUTE_BUCKETS = ZenModeConfig.MINUTE_BUCKETS;
    private static final int MIN_BUCKET_MINUTES;
    public static final Intent ZEN_PRIORITY_SETTINGS = new Intent("android.settings.ZEN_MODE_PRIORITY_SETTINGS");
    public static final Intent ZEN_SETTINGS = new Intent("android.settings.ZEN_MODE_SETTINGS");
    private boolean mAttached;
    private int mAttachedZen;
    private View mAutoRule;
    private TextView mAutoTitle;
    private int mBucketIndex = -1;
    private Callback mCallback;
    private final ConfigurableTexts mConfigurableTexts;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public ZenModeController mController;
    private ViewGroup mEdit;
    private View mEmpty;
    private ImageView mEmptyIcon;
    private TextView mEmptyText;
    private Condition mExitCondition;
    /* access modifiers changed from: private */
    public boolean mExpanded;
    private final Uri mForeverId;
    /* access modifiers changed from: private */
    public final C1661H mHandler = new C1661H();
    private boolean mHidden;
    protected final LayoutInflater mInflater;
    private final Interaction.Callback mInteractionCallback = new Interaction.Callback() {
        public void onInteraction() {
            ZenModePanel.this.fireInteraction();
        }
    };
    private final ZenPrefs mPrefs;
    /* access modifiers changed from: private */
    public Condition mSessionExitCondition;
    /* access modifiers changed from: private */
    public int mSessionZen;
    private int mState = 0;
    /* access modifiers changed from: private */
    public String mTag = ("ZenModePanel/" + Integer.toHexString(System.identityHashCode(this)));
    private final TransitionHelper mTransitionHelper = new TransitionHelper();
    private boolean mVoiceCapable;
    private TextView mZenAlarmWarning;
    protected SegmentedButtons mZenButtons;
    protected final SegmentedButtons.Callback mZenButtonsCallback = new SegmentedButtons.Callback() {
        public void onSelected(Object obj, boolean z) {
            if (obj != null && ZenModePanel.this.mZenButtons.isShown() && ZenModePanel.this.isAttachedToWindow()) {
                final int intValue = ((Integer) obj).intValue();
                if (z) {
                    MetricsLogger.action(ZenModePanel.this.mContext, 165, intValue);
                }
                if (ZenModePanel.DEBUG) {
                    String access$800 = ZenModePanel.this.mTag;
                    Log.d(access$800, "mZenButtonsCallback selected=" + intValue);
                }
                ZenModePanel zenModePanel = ZenModePanel.this;
                final Uri access$2000 = zenModePanel.getRealConditionId(zenModePanel.mSessionExitCondition);
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        ZenModePanel.this.mController.setZen(intValue, access$2000, "ZenModePanel.selectZen");
                        if (intValue != 0) {
                            Prefs.putInt(ZenModePanel.this.mContext, "DndFavoriteZen", intValue);
                        }
                    }
                });
            }
        }

        public void onInteraction() {
            ZenModePanel.this.fireInteraction();
        }
    };
    private final ZenModeController.Callback mZenCallback = new ZenModeController.Callback() {
        public void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
            ZenModePanel.this.mHandler.obtainMessage(2, zenRule).sendToTarget();
        }
    };
    protected LinearLayout mZenConditions;
    private View mZenIntroduction;
    private View mZenIntroductionConfirm;
    private TextView mZenIntroductionCustomize;
    private TextView mZenIntroductionMessage;
    protected int mZenModeButtonLayoutId;
    protected int mZenModeConditionLayoutId;
    private RadioGroup mZenRadioGroup;
    private LinearLayout mZenRadioGroupContent;

    public interface Callback {
        void onExpanded(boolean z);

        void onInteraction();

        void onPrioritySettings();
    }

    private static String prefKeyForConfirmation(int i) {
        if (i == 1) {
            return "DndConfirmedPriorityIntroduction";
        }
        if (i == 2) {
            return "DndConfirmedSilenceIntroduction";
        }
        if (i != 3) {
            return null;
        }
        return "DndConfirmedAlarmIntroduction";
    }

    static {
        int[] iArr = MINUTE_BUCKETS;
        MIN_BUCKET_MINUTES = iArr[0];
        MAX_BUCKET_MINUTES = iArr[iArr.length - 1];
        DEFAULT_BUCKET_INDEX = Arrays.binarySearch(iArr, 60);
    }

    public ZenModePanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mPrefs = new ZenPrefs();
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mForeverId = Condition.newId(this.mContext).appendPath("forever").build();
        this.mConfigurableTexts = new ConfigurableTexts(this.mContext);
        this.mVoiceCapable = Util.isVoiceCapable(this.mContext);
        this.mZenModeConditionLayoutId = C1779R$layout.zen_mode_condition;
        this.mZenModeButtonLayoutId = C1779R$layout.zen_mode_button;
        if (DEBUG) {
            Log.d(this.mTag, "new ZenModePanel");
        }
    }

    /* access modifiers changed from: protected */
    public void createZenButtons() {
        this.mZenButtons = (SegmentedButtons) findViewById(C1777R$id.zen_buttons);
        this.mZenButtons.addButton(C1784R$string.interruption_level_none_twoline, C1784R$string.interruption_level_none_with_warning, 2);
        this.mZenButtons.addButton(C1784R$string.interruption_level_alarms_twoline, C1784R$string.interruption_level_alarms, 3);
        this.mZenButtons.addButton(C1784R$string.interruption_level_priority_twoline, C1784R$string.interruption_level_priority, 1);
        this.mZenButtons.setCallback(this.mZenButtonsCallback);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        createZenButtons();
        this.mZenIntroduction = findViewById(C1777R$id.zen_introduction);
        this.mZenIntroductionMessage = (TextView) findViewById(C1777R$id.zen_introduction_message);
        this.mZenIntroductionConfirm = findViewById(C1777R$id.zen_introduction_confirm);
        this.mZenIntroductionConfirm.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                ZenModePanel.this.lambda$onFinishInflate$0$ZenModePanel(view);
            }
        });
        this.mZenIntroductionCustomize = (TextView) findViewById(C1777R$id.zen_introduction_customize);
        this.mZenIntroductionCustomize.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                ZenModePanel.this.lambda$onFinishInflate$1$ZenModePanel(view);
            }
        });
        this.mConfigurableTexts.add(this.mZenIntroductionCustomize, C1784R$string.zen_priority_customize_button);
        this.mZenConditions = (LinearLayout) findViewById(C1777R$id.zen_conditions);
        this.mZenAlarmWarning = (TextView) findViewById(C1777R$id.zen_alarm_warning);
        this.mZenRadioGroup = (RadioGroup) findViewById(C1777R$id.zen_radio_buttons);
        this.mZenRadioGroupContent = (LinearLayout) findViewById(C1777R$id.zen_radio_buttons_content);
        this.mEdit = (ViewGroup) findViewById(C1777R$id.edit_container);
        this.mEmpty = findViewById(16908292);
        this.mEmpty.setVisibility(4);
        this.mEmptyText = (TextView) this.mEmpty.findViewById(16908310);
        this.mEmptyIcon = (ImageView) this.mEmpty.findViewById(16908294);
        this.mAutoRule = findViewById(C1777R$id.auto_rule);
        this.mAutoTitle = (TextView) this.mAutoRule.findViewById(16908310);
        this.mAutoRule.setVisibility(4);
    }

    public /* synthetic */ void lambda$onFinishInflate$0$ZenModePanel(View view) {
        confirmZenIntroduction();
    }

    public /* synthetic */ void lambda$onFinishInflate$1$ZenModePanel(View view) {
        confirmZenIntroduction();
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onPrioritySettings();
        }
    }

    public void setEmptyState(int i, int i2) {
        this.mEmptyIcon.post(new Runnable(i, i2) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                ZenModePanel.this.lambda$setEmptyState$2$ZenModePanel(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setEmptyState$2$ZenModePanel(int i, int i2) {
        this.mEmptyIcon.setImageResource(i);
        this.mEmptyText.setText(i2);
    }

    public /* synthetic */ void lambda$setAutoText$3$ZenModePanel(CharSequence charSequence) {
        this.mAutoTitle.setText(charSequence);
    }

    public void setAutoText(CharSequence charSequence) {
        this.mAutoTitle.post(new Runnable(charSequence) {
            private final /* synthetic */ CharSequence f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ZenModePanel.this.lambda$setAutoText$3$ZenModePanel(this.f$1);
            }
        });
    }

    public void setState(int i) {
        int i2 = this.mState;
        if (i2 != i) {
            transitionFrom(getView(i2), getView(i));
            this.mState = i;
        }
    }

    private void transitionFrom(View view, View view2) {
        view.post(new Runnable(view2, view) {
            private final /* synthetic */ View f$0;
            private final /* synthetic */ View f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                ZenModePanel.lambda$transitionFrom$5(this.f$0, this.f$1);
            }
        });
    }

    static /* synthetic */ void lambda$transitionFrom$5(View view, View view2) {
        view.setAlpha(0.0f);
        view.setVisibility(0);
        view.bringToFront();
        view.animate().cancel();
        view.animate().alpha(1.0f).setDuration(300).withEndAction(new Runnable(view2) {
            private final /* synthetic */ View f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.setVisibility(4);
            }
        }).start();
    }

    private View getView(int i) {
        if (i == 1) {
            return this.mAutoRule;
        }
        if (i != 2) {
            return this.mEdit;
        }
        return this.mEmpty;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mConfigurableTexts.update();
        SegmentedButtons segmentedButtons = this.mZenButtons;
        if (segmentedButtons != null) {
            segmentedButtons.update();
        }
    }

    private void confirmZenIntroduction() {
        String prefKeyForConfirmation = prefKeyForConfirmation(getSelectedZen(0));
        if (prefKeyForConfirmation != null) {
            if (DEBUG) {
                Log.d("ZenModePanel", "confirmZenIntroduction " + prefKeyForConfirmation);
            }
            Prefs.putBoolean(this.mContext, prefKeyForConfirmation, true);
            this.mHandler.sendEmptyMessage(3);
        }
    }

    private void onAttach() {
        setExpanded(true);
        this.mAttachedZen = this.mController.getZen();
        ZenModeConfig.ZenRule manualRule = this.mController.getManualRule();
        this.mExitCondition = manualRule != null ? manualRule.condition : null;
        if (DEBUG) {
            String str = this.mTag;
            Log.d(str, "onAttach " + this.mAttachedZen + " " + manualRule);
        }
        handleUpdateManualRule(manualRule);
        this.mZenButtons.setSelectedValue(Integer.valueOf(this.mAttachedZen), false);
        this.mSessionZen = this.mAttachedZen;
        this.mTransitionHelper.clear();
        this.mController.addCallback(this.mZenCallback);
        setSessionExitCondition(copy(this.mExitCondition));
        updateWidgets();
        setAttached(true);
    }

    private void onDetach() {
        if (DEBUG) {
            Log.d(this.mTag, "onDetach");
        }
        setExpanded(false);
        checkForAttachedZenChange();
        setAttached(false);
        this.mAttachedZen = -1;
        this.mSessionZen = -1;
        this.mController.removeCallback(this.mZenCallback);
        setSessionExitCondition((Condition) null);
        this.mTransitionHelper.clear();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setAttached(boolean z) {
        this.mAttached = z;
    }

    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        if (z != this.mAttached) {
            if (z) {
                onAttach();
            } else {
                onDetach();
            }
        }
    }

    private void setSessionExitCondition(Condition condition) {
        if (!Objects.equals(condition, this.mSessionExitCondition)) {
            if (DEBUG) {
                String str = this.mTag;
                Log.d(str, "mSessionExitCondition=" + getConditionId(condition));
            }
            this.mSessionExitCondition = condition;
        }
    }

    private void checkForAttachedZenChange() {
        int selectedZen = getSelectedZen(-1);
        if (DEBUG) {
            String str = this.mTag;
            Log.d(str, "selectedZen=" + selectedZen);
        }
        if (selectedZen != this.mAttachedZen) {
            if (DEBUG) {
                String str2 = this.mTag;
                Log.d(str2, "attachedZen: " + this.mAttachedZen + " -> " + selectedZen);
            }
            if (selectedZen == 2) {
                this.mPrefs.trackNoneSelected();
            }
        }
    }

    private void setExpanded(boolean z) {
        if (z != this.mExpanded) {
            if (DEBUG) {
                String str = this.mTag;
                Log.d(str, "setExpanded " + z);
            }
            this.mExpanded = z;
            updateWidgets();
            fireExpanded();
        }
    }

    /* access modifiers changed from: protected */
    public void addZenConditions(int i) {
        for (int i2 = 0; i2 < i; i2++) {
            View inflate = this.mInflater.inflate(this.mZenModeButtonLayoutId, this.mEdit, false);
            inflate.setId(i2);
            this.mZenRadioGroup.addView(inflate);
            View inflate2 = this.mInflater.inflate(this.mZenModeConditionLayoutId, this.mEdit, false);
            inflate2.setId(i2 + i);
            this.mZenRadioGroupContent.addView(inflate2);
        }
    }

    public void init(ZenModeController zenModeController) {
        this.mController = zenModeController;
        addZenConditions(3);
        this.mSessionZen = getSelectedZen(-1);
        handleUpdateManualRule(this.mController.getManualRule());
        if (DEBUG) {
            String str = this.mTag;
            Log.d(str, "init mExitCondition=" + this.mExitCondition);
        }
        hideAllConditions();
    }

    private void setExitCondition(Condition condition) {
        if (!Objects.equals(this.mExitCondition, condition)) {
            this.mExitCondition = condition;
            if (DEBUG) {
                String str = this.mTag;
                Log.d(str, "mExitCondition=" + getConditionId(this.mExitCondition));
            }
            updateWidgets();
        }
    }

    private static Uri getConditionId(Condition condition) {
        if (condition != null) {
            return condition.id;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public Uri getRealConditionId(Condition condition) {
        if (isForever(condition)) {
            return null;
        }
        return getConditionId(condition);
    }

    private static Condition copy(Condition condition) {
        if (condition == null) {
            return null;
        }
        return condition.copy();
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleUpdateManualRule(ZenModeConfig.ZenRule zenRule) {
        Condition condition;
        handleUpdateZen(zenRule != null ? zenRule.zenMode : 0);
        if (zenRule == null) {
            condition = null;
        } else {
            Condition condition2 = zenRule.condition;
            if (condition2 != null) {
                condition = condition2;
            } else {
                condition = createCondition(zenRule.conditionId);
            }
        }
        handleUpdateConditions(condition);
        setExitCondition(condition);
    }

    private Condition createCondition(Uri uri) {
        if (ZenModeConfig.isValidCountdownToAlarmConditionId(uri)) {
            return ZenModeConfig.toNextAlarmCondition(this.mContext, ZenModeConfig.tryParseCountdownConditionId(uri), ActivityManager.getCurrentUser());
        } else if (!ZenModeConfig.isValidCountdownConditionId(uri)) {
            return forever();
        } else {
            long tryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(uri);
            return ZenModeConfig.toTimeCondition(this.mContext, tryParseCountdownConditionId, (int) (((tryParseCountdownConditionId - System.currentTimeMillis()) + 30000) / 60000), ActivityManager.getCurrentUser(), false);
        }
    }

    private void handleUpdateZen(int i) {
        int i2 = this.mSessionZen;
        if (!(i2 == -1 || i2 == i)) {
            this.mSessionZen = i;
        }
        this.mZenButtons.setSelectedValue(Integer.valueOf(i), false);
        updateWidgets();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getSelectedZen(int i) {
        Object selectedValue = this.mZenButtons.getSelectedValue();
        return selectedValue != null ? ((Integer) selectedValue).intValue() : i;
    }

    /* access modifiers changed from: private */
    public void updateWidgets() {
        int i;
        if (this.mTransitionHelper.isTransitioning()) {
            this.mTransitionHelper.pendingUpdateWidgets();
            return;
        }
        int i2 = 0;
        int selectedZen = getSelectedZen(0);
        boolean z = true;
        boolean z2 = selectedZen == 1;
        boolean z3 = selectedZen == 2;
        boolean z4 = selectedZen == 3;
        boolean z5 = (z2 && !this.mPrefs.mConfirmedPriorityIntroduction) || (z3 && !this.mPrefs.mConfirmedSilenceIntroduction) || (z4 && !this.mPrefs.mConfirmedAlarmIntroduction);
        this.mZenButtons.setVisibility(this.mHidden ? 8 : 0);
        this.mZenIntroduction.setVisibility(z5 ? 0 : 8);
        if (z5) {
            if (z2) {
                i = C1784R$string.zen_priority_introduction;
            } else if (z4) {
                i = C1784R$string.zen_alarms_introduction;
            } else if (this.mVoiceCapable) {
                i = C1784R$string.zen_silence_introduction_voice;
            } else {
                i = C1784R$string.zen_silence_introduction;
            }
            this.mConfigurableTexts.add(this.mZenIntroductionMessage, i);
            this.mConfigurableTexts.update();
            this.mZenIntroductionCustomize.setVisibility(z2 ? 0 : 8);
        }
        if (z3 || (z2 && !this.mController.areAlarmsAllowedInPriority())) {
            z = false;
        }
        String computeAlarmWarningText = computeAlarmWarningText(z);
        TextView textView = this.mZenAlarmWarning;
        if (computeAlarmWarningText == null) {
            i2 = 8;
        }
        textView.setVisibility(i2);
        this.mZenAlarmWarning.setText(computeAlarmWarningText);
    }

    private String computeAlarmWarningText(boolean z) {
        int i;
        if (z) {
            return null;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long nextAlarm = this.mController.getNextAlarm();
        if (nextAlarm < currentTimeMillis) {
            return null;
        }
        Condition condition = this.mSessionExitCondition;
        if (condition == null || isForever(condition)) {
            i = C1784R$string.zen_alarm_warning_indef;
        } else {
            long tryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(this.mSessionExitCondition.id);
            i = (tryParseCountdownConditionId <= currentTimeMillis || nextAlarm >= tryParseCountdownConditionId) ? 0 : C1784R$string.zen_alarm_warning;
        }
        if (i == 0) {
            return null;
        }
        boolean z2 = nextAlarm - currentTimeMillis < 86400000;
        boolean is24HourFormat = DateFormat.is24HourFormat(this.mContext, ActivityManager.getCurrentUser());
        return getResources().getString(i, new Object[]{getResources().getString(z2 ? C1784R$string.alarm_template : C1784R$string.alarm_template_far, new Object[]{DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), z2 ? is24HourFormat ? "Hm" : "hma" : is24HourFormat ? "EEEHm" : "EEEhma"), nextAlarm)})});
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleUpdateConditions(Condition condition) {
        if (!this.mTransitionHelper.isTransitioning()) {
            int i = 0;
            bind(forever(), this.mZenRadioGroupContent.getChildAt(0), 0);
            if (condition == null) {
                bindGenericCountdown();
                bindNextAlarm(getTimeUntilNextAlarmCondition());
            } else if (isForever(condition)) {
                getConditionTagAt(0).f79rb.setChecked(true);
                bindGenericCountdown();
                bindNextAlarm(getTimeUntilNextAlarmCondition());
            } else if (isAlarm(condition)) {
                bindGenericCountdown();
                bindNextAlarm(condition);
                getConditionTagAt(2).f79rb.setChecked(true);
            } else if (isCountdown(condition)) {
                bindNextAlarm(getTimeUntilNextAlarmCondition());
                bind(condition, this.mZenRadioGroupContent.getChildAt(1), 1);
                getConditionTagAt(1).f79rb.setChecked(true);
            } else {
                Slog.wtf("ZenModePanel", "Invalid manual condition: " + condition);
            }
            LinearLayout linearLayout = this.mZenConditions;
            if (this.mSessionZen == 0) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
    }

    private void bindGenericCountdown() {
        this.mBucketIndex = DEFAULT_BUCKET_INDEX;
        Condition timeCondition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[this.mBucketIndex], ActivityManager.getCurrentUser());
        if (!this.mAttached || getConditionTagAt(1).condition == null) {
            bind(timeCondition, this.mZenRadioGroupContent.getChildAt(1), 1);
        }
    }

    private void bindNextAlarm(Condition condition) {
        View childAt = this.mZenRadioGroupContent.getChildAt(2);
        ConditionTag conditionTag = (ConditionTag) childAt.getTag();
        if (condition != null && (!this.mAttached || conditionTag == null || conditionTag.condition == null)) {
            bind(condition, childAt, 2);
        }
        ConditionTag conditionTag2 = (ConditionTag) childAt.getTag();
        boolean z = (conditionTag2 == null || conditionTag2.condition == null) ? false : true;
        int i = 4;
        this.mZenRadioGroup.getChildAt(2).setVisibility(z ? 0 : 4);
        if (z) {
            i = 0;
        }
        childAt.setVisibility(i);
    }

    private Condition forever() {
        return new Condition(this.mForeverId, foreverSummary(this.mContext), "", "", 0, 1, 0);
    }

    private static String foreverSummary(Context context) {
        return context.getString(17041429);
    }

    private Condition getTimeUntilNextAlarmCondition() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        setToMidnight(gregorianCalendar);
        gregorianCalendar.add(5, 6);
        long nextAlarm = this.mController.getNextAlarm();
        if (nextAlarm <= 0) {
            return null;
        }
        GregorianCalendar gregorianCalendar2 = new GregorianCalendar();
        gregorianCalendar2.setTimeInMillis(nextAlarm);
        setToMidnight(gregorianCalendar2);
        if (gregorianCalendar.compareTo(gregorianCalendar2) >= 0) {
            return ZenModeConfig.toNextAlarmCondition(this.mContext, nextAlarm, ActivityManager.getCurrentUser());
        }
        return null;
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ConditionTag getConditionTagAt(int i) {
        return (ConditionTag) this.mZenRadioGroupContent.getChildAt(i).getTag();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getVisibleConditions() {
        int childCount = this.mZenRadioGroupContent.getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            i += this.mZenRadioGroupContent.getChildAt(i2).getVisibility() == 0 ? 1 : 0;
        }
        return i;
    }

    private void hideAllConditions() {
        int childCount = this.mZenRadioGroupContent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mZenRadioGroupContent.getChildAt(i).setVisibility(8);
        }
    }

    private static boolean isAlarm(Condition condition) {
        return condition != null && ZenModeConfig.isValidCountdownToAlarmConditionId(condition.id);
    }

    private static boolean isCountdown(Condition condition) {
        return condition != null && ZenModeConfig.isValidCountdownConditionId(condition.id);
    }

    private boolean isForever(Condition condition) {
        return condition != null && this.mForeverId.equals(condition.id);
    }

    private void bind(Condition condition, View view, int i) {
        String str;
        Condition condition2 = condition;
        final View view2 = view;
        final int i2 = i;
        if (condition2 != null) {
            boolean z = true;
            boolean z2 = condition2.state == 1;
            final ConditionTag conditionTag = view.getTag() != null ? (ConditionTag) view.getTag() : new ConditionTag();
            view2.setTag(conditionTag);
            boolean z3 = conditionTag.f79rb == null;
            if (conditionTag.f79rb == null) {
                conditionTag.f79rb = (RadioButton) this.mZenRadioGroup.getChildAt(i2);
            }
            conditionTag.condition = condition2;
            final Uri conditionId = getConditionId(conditionTag.condition);
            if (DEBUG) {
                Log.d(this.mTag, "bind i=" + this.mZenRadioGroupContent.indexOfChild(view2) + " first=" + z3 + " condition=" + conditionId);
            }
            conditionTag.f79rb.setEnabled(z2);
            conditionTag.f79rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    if (ZenModePanel.this.mExpanded && z) {
                        conditionTag.f79rb.setChecked(true);
                        if (ZenModePanel.DEBUG) {
                            String access$800 = ZenModePanel.this.mTag;
                            Log.d(access$800, "onCheckedChanged " + conditionId);
                        }
                        MetricsLogger.action(ZenModePanel.this.mContext, 164);
                        ZenModePanel.this.select(conditionTag.condition);
                        ZenModePanel.this.announceConditionSelection(conditionTag);
                    }
                }
            });
            if (conditionTag.lines == null) {
                conditionTag.lines = view2.findViewById(16908290);
            }
            if (conditionTag.line1 == null) {
                conditionTag.line1 = (TextView) view2.findViewById(16908308);
                this.mConfigurableTexts.add(conditionTag.line1);
            }
            if (conditionTag.line2 == null) {
                conditionTag.line2 = (TextView) view2.findViewById(16908309);
                this.mConfigurableTexts.add(conditionTag.line2);
            }
            if (!TextUtils.isEmpty(condition2.line1)) {
                str = condition2.line1;
            } else {
                str = condition2.summary;
            }
            String str2 = condition2.line2;
            conditionTag.line1.setText(str);
            if (TextUtils.isEmpty(str2)) {
                conditionTag.line2.setVisibility(8);
            } else {
                conditionTag.line2.setVisibility(0);
                conditionTag.line2.setText(str2);
            }
            conditionTag.lines.setEnabled(z2);
            conditionTag.lines.setAlpha(z2 ? 1.0f : 0.4f);
            ImageView imageView = (ImageView) view2.findViewById(16908313);
            imageView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    ZenModePanel.this.onClickTimeButton(view2, conditionTag, false, i2);
                }
            });
            ImageView imageView2 = (ImageView) view2.findViewById(16908314);
            imageView2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    ZenModePanel.this.onClickTimeButton(view2, conditionTag, true, i2);
                }
            });
            conditionTag.lines.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    conditionTag.f79rb.setChecked(true);
                }
            });
            long tryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(conditionId);
            if (i2 == 2 || tryParseCountdownConditionId <= 0) {
                imageView.setVisibility(8);
                imageView2.setVisibility(8);
            } else {
                imageView.setVisibility(0);
                imageView2.setVisibility(0);
                int i3 = this.mBucketIndex;
                if (i3 > -1) {
                    imageView.setEnabled(i3 > 0);
                    if (this.mBucketIndex >= MINUTE_BUCKETS.length - 1) {
                        z = false;
                    }
                    imageView2.setEnabled(z);
                } else {
                    imageView.setEnabled(tryParseCountdownConditionId - System.currentTimeMillis() > ((long) (MIN_BUCKET_MINUTES * 60000)));
                    imageView2.setEnabled(!Objects.equals(condition2.summary, ZenModeConfig.toTimeCondition(this.mContext, MAX_BUCKET_MINUTES, ActivityManager.getCurrentUser()).summary));
                }
                imageView.setAlpha(imageView.isEnabled() ? 1.0f : 0.5f);
                imageView2.setAlpha(imageView2.isEnabled() ? 1.0f : 0.5f);
            }
            if (z3) {
                Interaction.register(conditionTag.f79rb, this.mInteractionCallback);
                Interaction.register(conditionTag.lines, this.mInteractionCallback);
                Interaction.register(imageView, this.mInteractionCallback);
                Interaction.register(imageView2, this.mInteractionCallback);
            }
            view2.setVisibility(0);
            return;
        }
        throw new IllegalArgumentException("condition must not be null");
    }

    /* access modifiers changed from: private */
    public void announceConditionSelection(ConditionTag conditionTag) {
        String str;
        int selectedZen = getSelectedZen(0);
        if (selectedZen == 1) {
            str = this.mContext.getString(C1784R$string.interruption_level_priority);
        } else if (selectedZen == 2) {
            str = this.mContext.getString(C1784R$string.interruption_level_none);
        } else if (selectedZen == 3) {
            str = this.mContext.getString(C1784R$string.interruption_level_alarms);
        } else {
            return;
        }
        announceForAccessibility(this.mContext.getString(C1784R$string.zen_mode_and_condition, new Object[]{str, conditionTag.line1.getText()}));
    }

    /* access modifiers changed from: private */
    public void onClickTimeButton(View view, ConditionTag conditionTag, boolean z, int i) {
        Condition condition;
        int i2;
        int i3;
        long j;
        ConditionTag conditionTag2 = conditionTag;
        boolean z2 = z;
        MetricsLogger.action(this.mContext, 163, z2);
        int length = MINUTE_BUCKETS.length;
        int i4 = this.mBucketIndex;
        int i5 = 0;
        int i6 = -1;
        if (i4 == -1) {
            long tryParseCountdownConditionId = ZenModeConfig.tryParseCountdownConditionId(getConditionId(conditionTag2.condition));
            long currentTimeMillis = System.currentTimeMillis();
            while (true) {
                if (i5 >= length) {
                    condition = null;
                    break;
                }
                i2 = z2 ? i5 : (length - 1) - i5;
                i3 = MINUTE_BUCKETS[i2];
                j = currentTimeMillis + ((long) (60000 * i3));
                if ((!z2 || j <= tryParseCountdownConditionId) && (z2 || j >= tryParseCountdownConditionId)) {
                    i5++;
                }
            }
            this.mBucketIndex = i2;
            condition = ZenModeConfig.toTimeCondition(this.mContext, j, i3, ActivityManager.getCurrentUser(), false);
            if (condition == null) {
                this.mBucketIndex = DEFAULT_BUCKET_INDEX;
                condition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[this.mBucketIndex], ActivityManager.getCurrentUser());
            }
        } else {
            int i7 = length - 1;
            if (z2) {
                i6 = 1;
            }
            this.mBucketIndex = Math.max(0, Math.min(i7, i4 + i6));
            condition = ZenModeConfig.toTimeCondition(this.mContext, MINUTE_BUCKETS[this.mBucketIndex], ActivityManager.getCurrentUser());
        }
        Condition condition2 = condition;
        bind(condition2, view, i);
        conditionTag2.f79rb.setChecked(true);
        select(condition2);
        announceConditionSelection(conditionTag2);
    }

    /* access modifiers changed from: private */
    public void select(Condition condition) {
        int i;
        if (DEBUG) {
            String str = this.mTag;
            Log.d(str, "select " + condition);
        }
        int i2 = this.mSessionZen;
        if (i2 != -1 && i2 != 0) {
            final Uri realConditionId = getRealConditionId(condition);
            if (this.mController != null) {
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        ZenModePanel.this.mController.setZen(ZenModePanel.this.mSessionZen, realConditionId, "ZenModePanel.selectCondition");
                    }
                });
            }
            setExitCondition(condition);
            if (realConditionId == null) {
                this.mPrefs.setMinuteIndex(-1);
            } else if ((isAlarm(condition) || isCountdown(condition)) && (i = this.mBucketIndex) != -1) {
                this.mPrefs.setMinuteIndex(i);
            }
            setSessionExitCondition(copy(condition));
        } else if (DEBUG) {
            Log.d(this.mTag, "Ignoring condition selection outside of manual zen");
        }
    }

    /* access modifiers changed from: private */
    public void fireInteraction() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onInteraction();
        }
    }

    private void fireExpanded() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onExpanded(this.mExpanded);
        }
    }

    /* renamed from: com.android.systemui.volume.ZenModePanel$H */
    private final class C1661H extends Handler {
        private C1661H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 2) {
                ZenModePanel.this.handleUpdateManualRule((ZenModeConfig.ZenRule) message.obj);
            } else if (i == 3) {
                ZenModePanel.this.updateWidgets();
            }
        }
    }

    @VisibleForTesting
    static class ConditionTag {
        Condition condition;
        TextView line1;
        TextView line2;
        View lines;

        /* renamed from: rb */
        RadioButton f79rb;

        ConditionTag() {
        }
    }

    private final class ZenPrefs implements SharedPreferences.OnSharedPreferenceChangeListener {
        /* access modifiers changed from: private */
        public boolean mConfirmedAlarmIntroduction;
        /* access modifiers changed from: private */
        public boolean mConfirmedPriorityIntroduction;
        /* access modifiers changed from: private */
        public boolean mConfirmedSilenceIntroduction;
        private int mMinuteIndex;
        private final int mNoneDangerousThreshold;
        private int mNoneSelected;

        private ZenPrefs() {
            this.mNoneDangerousThreshold = ZenModePanel.this.mContext.getResources().getInteger(C1778R$integer.zen_mode_alarm_warning_threshold);
            Prefs.registerListener(ZenModePanel.this.mContext, this);
            updateMinuteIndex();
            updateNoneSelected();
            updateConfirmedPriorityIntroduction();
            updateConfirmedSilenceIntroduction();
            updateConfirmedAlarmIntroduction();
        }

        public void trackNoneSelected() {
            this.mNoneSelected = clampNoneSelected(this.mNoneSelected + 1);
            if (ZenModePanel.DEBUG) {
                String access$800 = ZenModePanel.this.mTag;
                Log.d(access$800, "Setting none selected: " + this.mNoneSelected + " threshold=" + this.mNoneDangerousThreshold);
            }
            Prefs.putInt(ZenModePanel.this.mContext, "DndNoneSelected", this.mNoneSelected);
        }

        public void setMinuteIndex(int i) {
            int clampIndex = clampIndex(i);
            if (clampIndex != this.mMinuteIndex) {
                this.mMinuteIndex = clampIndex(clampIndex);
                if (ZenModePanel.DEBUG) {
                    String access$800 = ZenModePanel.this.mTag;
                    Log.d(access$800, "Setting favorite minute index: " + this.mMinuteIndex);
                }
                Prefs.putInt(ZenModePanel.this.mContext, "DndCountdownMinuteIndex", this.mMinuteIndex);
            }
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            updateMinuteIndex();
            updateNoneSelected();
            updateConfirmedPriorityIntroduction();
            updateConfirmedSilenceIntroduction();
            updateConfirmedAlarmIntroduction();
        }

        private void updateMinuteIndex() {
            this.mMinuteIndex = clampIndex(Prefs.getInt(ZenModePanel.this.mContext, "DndCountdownMinuteIndex", ZenModePanel.DEFAULT_BUCKET_INDEX));
            if (ZenModePanel.DEBUG) {
                String access$800 = ZenModePanel.this.mTag;
                Log.d(access$800, "Favorite minute index: " + this.mMinuteIndex);
            }
        }

        private int clampIndex(int i) {
            return MathUtils.constrain(i, -1, ZenModePanel.MINUTE_BUCKETS.length - 1);
        }

        private void updateNoneSelected() {
            this.mNoneSelected = clampNoneSelected(Prefs.getInt(ZenModePanel.this.mContext, "DndNoneSelected", 0));
            if (ZenModePanel.DEBUG) {
                String access$800 = ZenModePanel.this.mTag;
                Log.d(access$800, "None selected: " + this.mNoneSelected);
            }
        }

        private int clampNoneSelected(int i) {
            return MathUtils.constrain(i, 0, Integer.MAX_VALUE);
        }

        private void updateConfirmedPriorityIntroduction() {
            boolean z = Prefs.getBoolean(ZenModePanel.this.mContext, "DndConfirmedPriorityIntroduction", false);
            if (z != this.mConfirmedPriorityIntroduction) {
                this.mConfirmedPriorityIntroduction = z;
                if (ZenModePanel.DEBUG) {
                    String access$800 = ZenModePanel.this.mTag;
                    Log.d(access$800, "Confirmed priority introduction: " + this.mConfirmedPriorityIntroduction);
                }
            }
        }

        private void updateConfirmedSilenceIntroduction() {
            boolean z = Prefs.getBoolean(ZenModePanel.this.mContext, "DndConfirmedSilenceIntroduction", false);
            if (z != this.mConfirmedSilenceIntroduction) {
                this.mConfirmedSilenceIntroduction = z;
                if (ZenModePanel.DEBUG) {
                    String access$800 = ZenModePanel.this.mTag;
                    Log.d(access$800, "Confirmed silence introduction: " + this.mConfirmedSilenceIntroduction);
                }
            }
        }

        private void updateConfirmedAlarmIntroduction() {
            boolean z = Prefs.getBoolean(ZenModePanel.this.mContext, "DndConfirmedAlarmIntroduction", false);
            if (z != this.mConfirmedAlarmIntroduction) {
                this.mConfirmedAlarmIntroduction = z;
                if (ZenModePanel.DEBUG) {
                    String access$800 = ZenModePanel.this.mTag;
                    Log.d(access$800, "Confirmed alarm introduction: " + this.mConfirmedAlarmIntroduction);
                }
            }
        }
    }

    private final class TransitionHelper implements LayoutTransition.TransitionListener, Runnable {
        private boolean mPendingUpdateWidgets;
        private boolean mTransitioning;
        private final ArraySet<View> mTransitioningViews;

        private TransitionHelper() {
            this.mTransitioningViews = new ArraySet<>();
        }

        public void clear() {
            this.mTransitioningViews.clear();
            this.mPendingUpdateWidgets = false;
        }

        public void pendingUpdateWidgets() {
            this.mPendingUpdateWidgets = true;
        }

        public boolean isTransitioning() {
            return !this.mTransitioningViews.isEmpty();
        }

        public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            this.mTransitioningViews.add(view);
            updateTransitioning();
        }

        public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            this.mTransitioningViews.remove(view);
            updateTransitioning();
        }

        public void run() {
            if (ZenModePanel.DEBUG) {
                String access$800 = ZenModePanel.this.mTag;
                Log.d(access$800, "TransitionHelper run mPendingUpdateWidgets=" + this.mPendingUpdateWidgets);
            }
            if (this.mPendingUpdateWidgets) {
                ZenModePanel.this.updateWidgets();
            }
            this.mPendingUpdateWidgets = false;
        }

        private void updateTransitioning() {
            boolean isTransitioning = isTransitioning();
            if (this.mTransitioning != isTransitioning) {
                this.mTransitioning = isTransitioning;
                if (ZenModePanel.DEBUG) {
                    String access$800 = ZenModePanel.this.mTag;
                    Log.d(access$800, "TransitionHelper mTransitioning=" + this.mTransitioning);
                }
                if (this.mTransitioning) {
                    return;
                }
                if (this.mPendingUpdateWidgets) {
                    ZenModePanel.this.mHandler.post(this);
                } else {
                    this.mPendingUpdateWidgets = false;
                }
            }
        }
    }
}
