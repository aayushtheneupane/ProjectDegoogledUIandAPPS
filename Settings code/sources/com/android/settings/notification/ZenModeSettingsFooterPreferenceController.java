package com.android.settings.notification;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.ListFormatter;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.utils.AnnotationSpan;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.havoc.config.center.C1715R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ZenModeSettingsFooterPreferenceController extends AbstractZenModePreferenceController {
    private FragmentManager mFragment;

    public String getPreferenceKey() {
        return "footer_preference";
    }

    public ZenModeSettingsFooterPreferenceController(Context context, Lifecycle lifecycle, FragmentManager fragmentManager) {
        super(context, "footer_preference", lifecycle);
        this.mFragment = fragmentManager;
    }

    public boolean isAvailable() {
        int zenMode = getZenMode();
        return zenMode == 1 || zenMode == 2 || zenMode == 3;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        boolean isAvailable = isAvailable();
        preference.setVisible(isAvailable);
        if (isAvailable) {
            preference.setTitle(getFooterText());
        }
    }

    /* access modifiers changed from: protected */
    public CharSequence getFooterText() {
        ZenModeConfig zenModeConfig = getZenModeConfig();
        if (!Objects.equals(this.mBackend.getConsolidatedPolicy(), zenModeConfig.toNotificationPolicy())) {
            List<ZenModeConfig.ZenRule> activeRules = getActiveRules(zenModeConfig);
            ArrayList arrayList = new ArrayList();
            for (ZenModeConfig.ZenRule zenRule : activeRules) {
                String str = zenRule.name;
                if (str != null) {
                    arrayList.add(str);
                }
            }
            if (arrayList.size() > 0) {
                String format = ListFormatter.getInstance().format(arrayList);
                if (!format.isEmpty()) {
                    AnnotationSpan.LinkInfo linkInfo = new AnnotationSpan.LinkInfo("link", new View.OnClickListener() {
                        public void onClick(View view) {
                            ZenModeSettingsFooterPreferenceController.this.showCustomSettingsDialog();
                        }
                    });
                    return TextUtils.concat(new CharSequence[]{this.mContext.getResources().getString(C1715R.string.zen_mode_settings_dnd_custom_settings_footer, new Object[]{format}), AnnotationSpan.linkify(this.mContext.getResources().getText(C1715R.string.zen_mode_settings_dnd_custom_settings_footer_link), linkInfo)});
                }
            }
        }
        return getDefaultPolicyFooter(zenModeConfig);
    }

    private String getDefaultPolicyFooter(ZenModeConfig zenModeConfig) {
        ZenModeConfig.ZenRule zenRule = zenModeConfig.manualRule;
        String str = "";
        long j = -1;
        if (zenRule != null) {
            Uri uri = zenRule.conditionId;
            String str2 = zenRule.enabler;
            if (str2 != null) {
                String ownerCaption = AbstractZenModePreferenceController.mZenModeConfigWrapper.getOwnerCaption(str2);
                if (!ownerCaption.isEmpty()) {
                    str = this.mContext.getString(C1715R.string.zen_mode_settings_dnd_automatic_rule_app, new Object[]{ownerCaption});
                }
            } else if (uri == null) {
                return this.mContext.getString(C1715R.string.zen_mode_settings_dnd_manual_indefinite);
            } else {
                j = AbstractZenModePreferenceController.mZenModeConfigWrapper.parseManualRuleTime(uri);
                if (j > 0) {
                    CharSequence formattedTime = AbstractZenModePreferenceController.mZenModeConfigWrapper.getFormattedTime(j, this.mContext.getUserId());
                    str = this.mContext.getString(C1715R.string.zen_mode_settings_dnd_manual_end_time, new Object[]{formattedTime});
                }
            }
        }
        for (ZenModeConfig.ZenRule zenRule2 : zenModeConfig.automaticRules.values()) {
            if (zenRule2.isAutomaticActive()) {
                if (!AbstractZenModePreferenceController.mZenModeConfigWrapper.isTimeRule(zenRule2.conditionId)) {
                    return this.mContext.getString(C1715R.string.zen_mode_settings_dnd_automatic_rule, new Object[]{zenRule2.name});
                }
                long parseAutomaticRuleEndTime = AbstractZenModePreferenceController.mZenModeConfigWrapper.parseAutomaticRuleEndTime(zenRule2.conditionId);
                if (parseAutomaticRuleEndTime > j) {
                    str = this.mContext.getString(C1715R.string.zen_mode_settings_dnd_automatic_rule, new Object[]{zenRule2.name});
                    j = parseAutomaticRuleEndTime;
                }
            }
        }
        return str;
    }

    private List<ZenModeConfig.ZenRule> getActiveRules(ZenModeConfig zenModeConfig) {
        ArrayList arrayList = new ArrayList();
        ZenModeConfig.ZenRule zenRule = zenModeConfig.manualRule;
        if (zenRule != null) {
            arrayList.add(zenRule);
        }
        for (ZenModeConfig.ZenRule zenRule2 : zenModeConfig.automaticRules.values()) {
            if (zenRule2.isAutomaticActive()) {
                arrayList.add(zenRule2);
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    public void showCustomSettingsDialog() {
        ZenCustomSettingsDialog zenCustomSettingsDialog = new ZenCustomSettingsDialog();
        zenCustomSettingsDialog.setNotificationPolicy(this.mBackend.getConsolidatedPolicy());
        zenCustomSettingsDialog.show(this.mFragment, ZenCustomSettingsDialog.class.getName());
    }

    public static class ZenCustomSettingsDialog extends InstrumentedDialogFragment {
        private String KEY_POLICY = "policy";
        /* access modifiers changed from: private */
        public NotificationManager.Policy mPolicy;
        /* access modifiers changed from: private */
        public ZenModeSettings.SummaryBuilder mSummaryBuilder;

        /* access modifiers changed from: private */
        public int getAllowRes(boolean z) {
            return z ? C1715R.string.zen_mode_sound_summary_on : C1715R.string.zen_mode_sound_summary_off;
        }

        public int getMetricsCategory() {
            return 1612;
        }

        public void setNotificationPolicy(NotificationManager.Policy policy) {
            this.mPolicy = policy;
        }

        public Dialog onCreateDialog(Bundle bundle) {
            NotificationManager.Policy policy;
            final FragmentActivity activity = getActivity();
            if (!(bundle == null || (policy = (NotificationManager.Policy) bundle.getParcelable(this.KEY_POLICY)) == null)) {
                this.mPolicy = policy;
            }
            this.mSummaryBuilder = new ZenModeSettings.SummaryBuilder(activity);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle((int) C1715R.string.zen_custom_settings_dialog_title);
            builder.setNeutralButton(C1715R.string.zen_custom_settings_dialog_review_schedule, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    new SubSettingLauncher(activity).setDestination(ZenModeAutomationSettings.class.getName()).setSourceMetricsCategory(142).launch();
                }
            });
            builder.setPositiveButton((int) C1715R.string.zen_custom_settings_dialog_ok, (DialogInterface.OnClickListener) null);
            builder.setView(LayoutInflater.from(activity).inflate(activity.getResources().getLayout(C1715R.layout.zen_custom_settings_dialog), (ViewGroup) null, false));
            final AlertDialog create = builder.create();
            create.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialogInterface) {
                    ((TextView) create.findViewById(C1715R.C1718id.zen_custom_settings_dialog_calls_allow)).setText(ZenCustomSettingsDialog.this.mSummaryBuilder.getCallsSettingSummary(ZenCustomSettingsDialog.this.mPolicy));
                    ((TextView) create.findViewById(C1715R.C1718id.zen_custom_settings_dialog_messages_allow)).setText(ZenCustomSettingsDialog.this.mSummaryBuilder.getMessagesSettingSummary(ZenCustomSettingsDialog.this.mPolicy));
                    ZenCustomSettingsDialog zenCustomSettingsDialog = ZenCustomSettingsDialog.this;
                    ((TextView) create.findViewById(C1715R.C1718id.zen_custom_settings_dialog_alarms_allow)).setText(zenCustomSettingsDialog.getAllowRes(zenCustomSettingsDialog.mPolicy.allowAlarms()));
                    ZenCustomSettingsDialog zenCustomSettingsDialog2 = ZenCustomSettingsDialog.this;
                    ((TextView) create.findViewById(C1715R.C1718id.zen_custom_settings_dialog_media_allow)).setText(zenCustomSettingsDialog2.getAllowRes(zenCustomSettingsDialog2.mPolicy.allowMedia()));
                    ZenCustomSettingsDialog zenCustomSettingsDialog3 = ZenCustomSettingsDialog.this;
                    ((TextView) create.findViewById(C1715R.C1718id.zen_custom_settings_dialog_system_allow)).setText(zenCustomSettingsDialog3.getAllowRes(zenCustomSettingsDialog3.mPolicy.allowSystem()));
                    ZenCustomSettingsDialog zenCustomSettingsDialog4 = ZenCustomSettingsDialog.this;
                    ((TextView) create.findViewById(C1715R.C1718id.zen_custom_settings_dialog_reminders_allow)).setText(zenCustomSettingsDialog4.getAllowRes(zenCustomSettingsDialog4.mPolicy.allowReminders()));
                    ZenCustomSettingsDialog zenCustomSettingsDialog5 = ZenCustomSettingsDialog.this;
                    ((TextView) create.findViewById(C1715R.C1718id.zen_custom_settings_dialog_events_allow)).setText(zenCustomSettingsDialog5.getAllowRes(zenCustomSettingsDialog5.mPolicy.allowEvents()));
                    ((TextView) create.findViewById(C1715R.C1718id.zen_custom_settings_dialog_show_notifications)).setText(ZenCustomSettingsDialog.this.mSummaryBuilder.getBlockedEffectsSummary(ZenCustomSettingsDialog.this.mPolicy));
                }
            });
            return create;
        }

        public void onSaveInstanceState(Bundle bundle) {
            super.onSaveInstanceState(bundle);
            bundle.putParcelable(this.KEY_POLICY, this.mPolicy);
        }
    }
}
