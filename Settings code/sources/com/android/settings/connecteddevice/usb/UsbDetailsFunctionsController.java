package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.net.ConnectivityManager;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import com.android.settings.Utils;
import com.android.settings.widget.RadioButtonPreference;
import com.havoc.config.center.C1715R;
import java.util.LinkedHashMap;
import java.util.Map;

public class UsbDetailsFunctionsController extends UsbDetailsController implements RadioButtonPreference.OnClickListener {
    static final Map<Long, Integer> FUNCTIONS_MAP = new LinkedHashMap();
    private ConnectivityManager mConnectivityManager;
    OnStartTetheringCallback mOnStartTetheringCallback = new OnStartTetheringCallback();
    long mPreviousFunction = this.mUsbBackend.getCurrentFunctions();
    private PreferenceCategory mProfilesContainer;

    public String getPreferenceKey() {
        return "usb_details_functions";
    }

    static {
        FUNCTIONS_MAP.put(4L, Integer.valueOf(C1715R.string.usb_use_file_transfers));
        FUNCTIONS_MAP.put(32L, Integer.valueOf(C1715R.string.usb_use_tethering));
        FUNCTIONS_MAP.put(8L, Integer.valueOf(C1715R.string.usb_use_MIDI));
        FUNCTIONS_MAP.put(16L, Integer.valueOf(C1715R.string.usb_use_photo_transfers));
        FUNCTIONS_MAP.put(0L, Integer.valueOf(C1715R.string.usb_use_charging_only));
    }

    public UsbDetailsFunctionsController(Context context, UsbDetailsFragment usbDetailsFragment, UsbBackend usbBackend) {
        super(context, usbDetailsFragment, usbBackend);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mProfilesContainer = (PreferenceCategory) preferenceScreen.findPreference(getPreferenceKey());
    }

    private RadioButtonPreference getProfilePreference(String str, int i) {
        RadioButtonPreference radioButtonPreference = (RadioButtonPreference) this.mProfilesContainer.findPreference(str);
        if (radioButtonPreference != null) {
            return radioButtonPreference;
        }
        RadioButtonPreference radioButtonPreference2 = new RadioButtonPreference(this.mProfilesContainer.getContext());
        radioButtonPreference2.setKey(str);
        radioButtonPreference2.setTitle(i);
        radioButtonPreference2.setOnClickListener(this);
        this.mProfilesContainer.addPreference(radioButtonPreference2);
        return radioButtonPreference2;
    }

    /* access modifiers changed from: protected */
    public void refresh(boolean z, long j, int i, int i2) {
        if (!z || i2 != 2) {
            this.mProfilesContainer.setEnabled(false);
        } else {
            this.mProfilesContainer.setEnabled(true);
        }
        for (Long longValue : FUNCTIONS_MAP.keySet()) {
            long longValue2 = longValue.longValue();
            RadioButtonPreference profilePreference = getProfilePreference(UsbBackend.usbFunctionsToString(longValue2), FUNCTIONS_MAP.get(Long.valueOf(longValue2)).intValue());
            if (this.mUsbBackend.areFunctionsSupported(longValue2)) {
                profilePreference.setChecked(j == longValue2);
            } else {
                this.mProfilesContainer.removePreference(profilePreference);
            }
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference radioButtonPreference) {
        long usbFunctionsFromString = UsbBackend.usbFunctionsFromString(radioButtonPreference.getKey());
        long currentFunctions = this.mUsbBackend.getCurrentFunctions();
        if (usbFunctionsFromString != currentFunctions && !Utils.isMonkeyRunning()) {
            this.mPreviousFunction = currentFunctions;
            if (usbFunctionsFromString == 32) {
                RadioButtonPreference radioButtonPreference2 = (RadioButtonPreference) this.mProfilesContainer.findPreference(UsbBackend.usbFunctionsToString(this.mPreviousFunction));
                if (radioButtonPreference2 != null) {
                    radioButtonPreference2.setChecked(false);
                    radioButtonPreference.setChecked(true);
                }
                this.mConnectivityManager.startTethering(1, true, this.mOnStartTetheringCallback);
                return;
            }
            this.mUsbBackend.setCurrentFunctions(usbFunctionsFromString);
        }
    }

    public boolean isAvailable() {
        return !Utils.isMonkeyRunning();
    }

    final class OnStartTetheringCallback extends ConnectivityManager.OnStartTetheringCallback {
        OnStartTetheringCallback() {
        }

        public void onTetheringFailed() {
            UsbDetailsFunctionsController.super.onTetheringFailed();
            UsbDetailsFunctionsController usbDetailsFunctionsController = UsbDetailsFunctionsController.this;
            usbDetailsFunctionsController.mUsbBackend.setCurrentFunctions(usbDetailsFunctionsController.mPreviousFunction);
        }
    }
}
