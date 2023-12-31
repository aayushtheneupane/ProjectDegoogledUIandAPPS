package com.android.settings.nfc;

import android.content.Context;
import android.nfc.NfcAdapter;
import androidx.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AndroidBeamPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    public static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
    private AndroidBeamEnabler mAndroidBeamEnabler;
    private final NfcAdapter mNfcAdapter;

    public AndroidBeamPreferenceController(Context context, String str) {
        super(context, str);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        if (!isAvailable()) {
            this.mAndroidBeamEnabler = null;
            return;
        }
        this.mAndroidBeamEnabler = new AndroidBeamEnabler(this.mContext, (RestrictedPreference) preferenceScreen.findPreference(getPreferenceKey()));
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.sofware.nfc.beam") && this.mNfcAdapter != null) {
            return 0;
        }
        return 3;
    }

    public void onResume() {
        AndroidBeamEnabler androidBeamEnabler = this.mAndroidBeamEnabler;
        if (androidBeamEnabler != null) {
            androidBeamEnabler.resume();
        }
    }

    public void onPause() {
        AndroidBeamEnabler androidBeamEnabler = this.mAndroidBeamEnabler;
        if (androidBeamEnabler != null) {
            androidBeamEnabler.pause();
        }
    }
}
