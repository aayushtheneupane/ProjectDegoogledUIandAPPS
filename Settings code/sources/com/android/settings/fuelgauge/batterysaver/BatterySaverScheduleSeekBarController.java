package com.android.settings.fuelgauge.batterysaver;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.widget.SeekBarPreference;
import com.android.settingslib.Utils;
import com.havoc.config.center.C1715R;

public class BatterySaverScheduleSeekBarController implements Preference.OnPreferenceChangeListener {
    private Context mContext;
    @VisibleForTesting
    public SeekBarPreference mSeekBarPreference;

    public BatterySaverScheduleSeekBarController(Context context) {
        this.mContext = context;
        this.mSeekBarPreference = new SeekBarPreference(context);
        this.mSeekBarPreference.setOnPreferenceChangeListener(this);
        this.mSeekBarPreference.setContinuousUpdates(true);
        this.mSeekBarPreference.setMax(15);
        this.mSeekBarPreference.setMin(1);
        this.mSeekBarPreference.setKey("battery_saver_seek_bar");
        updateSeekBar();
    }

    public boolean onPreferenceChange(Preference preference, Object obj) {
        int intValue = ((Integer) obj).intValue() * 5;
        Settings.Global.putInt(this.mContext.getContentResolver(), "low_power_trigger_level", intValue);
        preference.setTitle((CharSequence) this.mContext.getString(C1715R.string.battery_saver_seekbar_title, new Object[]{Utils.formatPercentage(intValue)}));
        return true;
    }

    public void updateSeekBar() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        if (Settings.Global.getInt(contentResolver, "automatic_power_save_mode", 0) == 0) {
            int i = Settings.Global.getInt(contentResolver, "low_power_trigger_level", 0);
            if (i <= 0) {
                this.mSeekBarPreference.setVisible(false);
                return;
            }
            int max = Math.max(i / 5, 1);
            this.mSeekBarPreference.setVisible(true);
            this.mSeekBarPreference.setProgress(max);
            this.mSeekBarPreference.setTitle((CharSequence) this.mContext.getString(C1715R.string.battery_saver_seekbar_title, new Object[]{Utils.formatPercentage(max * 5)}));
            return;
        }
        this.mSeekBarPreference.setVisible(false);
    }

    public void addToScreen(PreferenceScreen preferenceScreen) {
        this.mSeekBarPreference.setOrder(100);
        preferenceScreen.addPreference(this.mSeekBarPreference);
    }
}
