package com.android.systemui.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.provider.Settings;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C1784R$string;
import com.android.systemui.SystemUI;
import java.util.Arrays;

public class NotificationChannels extends SystemUI {
    public static String ALERTS = "ALR";
    public static String BATTERY = "BAT";
    public static String GENERAL = "GEN";
    public static String HINTS = "HNT";
    public static String SCREENSHOTS_HEADSUP = "SCN_HEADSUP";
    public static String SCREENSHOTS_LEGACY = "SCN";
    public static String STORAGE = "DSK";
    public static String TVPIP = "TPP";

    public static void createAll(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        NotificationChannel notificationChannel = new NotificationChannel(BATTERY, context.getString(C1784R$string.notification_channel_battery), 5);
        String string = Settings.Global.getString(context.getContentResolver(), "low_battery_sound");
        notificationChannel.setSound(Uri.parse("file://" + string), new AudioAttributes.Builder().setContentType(4).setUsage(10).build());
        notificationChannel.setBlockableSystem(true);
        notificationManager.createNotificationChannels(Arrays.asList(new NotificationChannel[]{new NotificationChannel(ALERTS, context.getString(C1784R$string.notification_channel_alerts), 4), new NotificationChannel(GENERAL, context.getString(C1784R$string.notification_channel_general), 1), new NotificationChannel(STORAGE, context.getString(C1784R$string.notification_channel_storage), isTv(context) ? 3 : 2), createScreenshotChannel(context.getString(C1784R$string.notification_channel_screenshot), notificationManager.getNotificationChannel(SCREENSHOTS_LEGACY)), notificationChannel, new NotificationChannel(HINTS, context.getString(C1784R$string.notification_channel_hints), 3)}));
        notificationManager.deleteNotificationChannel(SCREENSHOTS_LEGACY);
        if (isTv(context)) {
            notificationManager.createNotificationChannel(new NotificationChannel(TVPIP, context.getString(C1784R$string.notification_channel_tv_pip), 5));
        }
    }

    @VisibleForTesting
    static NotificationChannel createScreenshotChannel(String str, NotificationChannel notificationChannel) {
        NotificationChannel notificationChannel2 = new NotificationChannel(SCREENSHOTS_HEADSUP, str, 4);
        notificationChannel2.setSound((Uri) null, new AudioAttributes.Builder().setUsage(5).build());
        notificationChannel2.setBlockableSystem(true);
        if (notificationChannel != null) {
            int userLockedFields = notificationChannel.getUserLockedFields();
            if ((userLockedFields & 4) != 0) {
                notificationChannel2.setImportance(notificationChannel.getImportance());
            }
            if ((userLockedFields & 32) != 0) {
                notificationChannel2.setSound(notificationChannel.getSound(), notificationChannel.getAudioAttributes());
            }
            if ((userLockedFields & 16) != 0) {
                notificationChannel2.setVibrationPattern(notificationChannel.getVibrationPattern());
            }
            if ((userLockedFields & 8) != 0) {
                notificationChannel2.setLightColor(notificationChannel.getLightColor());
            }
        }
        return notificationChannel2;
    }

    public void start() {
        createAll(this.mContext);
    }

    private static boolean isTv(Context context) {
        return context.getPackageManager().hasSystemFeature("android.software.leanback");
    }
}
