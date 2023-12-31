package com.android.settings.homepage.contextualcards.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.ColorDisplayManager;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import com.android.settings.homepage.contextualcards.ContextualCard;
import com.android.settings.homepage.contextualcards.conditional.ConditionalContextualCard;
import com.havoc.config.center.C1715R;
import java.net.URISyntaxException;
import java.util.Objects;

public class GrayscaleConditionController implements ConditionalCardController {
    private static final IntentFilter GRAYSCALE_CHANGED_FILTER = new IntentFilter("android.settings.action.GRAYSCALE_CHANGED");

    /* renamed from: ID */
    static final int f43ID = Objects.hash(new Object[]{"GrayscaleConditionController"});
    private final Context mAppContext;
    private final ColorDisplayManager mColorDisplayManager = ((ColorDisplayManager) this.mAppContext.getSystemService(ColorDisplayManager.class));
    /* access modifiers changed from: private */
    public final ConditionManager mConditionManager;
    private Intent mIntent;
    private final Receiver mReceiver = new Receiver();

    public GrayscaleConditionController(Context context, ConditionManager conditionManager) {
        this.mAppContext = context;
        this.mConditionManager = conditionManager;
    }

    public long getId() {
        return (long) f43ID;
    }

    public boolean isDisplayable() {
        try {
            this.mIntent = Intent.parseUri(this.mAppContext.getString(C1715R.string.config_grayscale_settings_intent), 1);
            return this.mColorDisplayManager.isSaturationActivated();
        } catch (URISyntaxException e) {
            Log.w("GrayscaleCondition", "Failure parsing grayscale settings intent, skipping", e);
            return false;
        }
    }

    public void onPrimaryClick(Context context) {
        this.mAppContext.startActivity(this.mIntent);
    }

    public void onActionClick() {
        this.mColorDisplayManager.setSaturationLevel(100);
        sendBroadcast();
        this.mConditionManager.onConditionChanged();
    }

    public ContextualCard buildContextualCard() {
        ConditionalContextualCard.Builder builder = new ConditionalContextualCard.Builder();
        builder.setConditionId((long) f43ID);
        builder.setMetricsConstant(1683);
        builder.setActionText(this.mAppContext.getText(C1715R.string.condition_turn_off));
        builder.setName(this.mAppContext.getPackageName() + "/" + this.mAppContext.getText(C1715R.string.condition_grayscale_title));
        builder.setTitleText(this.mAppContext.getText(C1715R.string.condition_grayscale_title).toString());
        builder.setSummaryText(this.mAppContext.getText(C1715R.string.condition_grayscale_summary).toString());
        builder.setIconDrawable(this.mAppContext.getDrawable(C1715R.C1717drawable.ic_gray_scale_24dp));
        builder.setViewType(C1715R.layout.conditional_card_half_tile);
        return builder.build();
    }

    public void startMonitoringStateChange() {
        this.mAppContext.registerReceiver(this.mReceiver, GRAYSCALE_CHANGED_FILTER, "android.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS", (Handler) null);
    }

    public void stopMonitoringStateChange() {
        this.mAppContext.unregisterReceiver(this.mReceiver);
    }

    private void sendBroadcast() {
        Intent intent = new Intent("android.settings.action.GRAYSCALE_CHANGED");
        intent.addFlags(16777216);
        this.mAppContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, "android.permission.CONTROL_DISPLAY_COLOR_TRANSFORMS");
    }

    public class Receiver extends BroadcastReceiver {
        public Receiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.settings.action.GRAYSCALE_CHANGED".equals(intent.getAction())) {
                GrayscaleConditionController.this.mConditionManager.onConditionChanged();
            }
        }
    }
}
