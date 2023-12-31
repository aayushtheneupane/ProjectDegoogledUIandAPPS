package com.android.settings.datetime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.DatePicker;
import androidx.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.Calendar;

public class DatePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, DatePickerDialog.OnDateSetListener {
    private final AutoTimePreferenceController mAutoTimePreferenceController;
    private final DatePreferenceHost mHost;

    public interface DatePreferenceHost extends UpdateTimeAndDateCallback {
        void showDatePicker();
    }

    public String getPreferenceKey() {
        return "date";
    }

    public boolean isAvailable() {
        return true;
    }

    public DatePreferenceController(Context context, DatePreferenceHost datePreferenceHost, AutoTimePreferenceController autoTimePreferenceController) {
        super(context);
        this.mHost = datePreferenceHost;
        this.mAutoTimePreferenceController = autoTimePreferenceController;
    }

    public void updateState(Preference preference) {
        if (preference instanceof RestrictedPreference) {
            preference.setSummary((CharSequence) DateFormat.getLongDateFormat(this.mContext).format(Calendar.getInstance().getTime()));
            if (!((RestrictedPreference) preference).isDisabledByAdmin()) {
                preference.setEnabled(!this.mAutoTimePreferenceController.isEnabled());
            }
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), "date")) {
            return false;
        }
        this.mHost.showDatePicker();
        return true;
    }

    public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
        setDate(i, i2, i3);
        this.mHost.updateTimeAndDateDisplay(this.mContext);
    }

    public DatePickerDialog buildDatePicker(Activity activity) {
        Calendar instance = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity, this, instance.get(1), instance.get(2), instance.get(5));
        instance.clear();
        instance.set(2007, 0, 1);
        datePickerDialog.getDatePicker().setMinDate(instance.getTimeInMillis());
        instance.clear();
        instance.set(2037, 11, 31);
        datePickerDialog.getDatePicker().setMaxDate(instance.getTimeInMillis());
        return datePickerDialog;
    }

    /* access modifiers changed from: package-private */
    public void setDate(int i, int i2, int i3) {
        Calendar instance = Calendar.getInstance();
        instance.set(1, i);
        instance.set(2, i2);
        instance.set(5, i3);
        long max = Math.max(instance.getTimeInMillis(), 1194220800000L);
        if (max / 1000 < 2147483647L) {
            ((AlarmManager) this.mContext.getSystemService("alarm")).setTime(max);
        }
    }
}
