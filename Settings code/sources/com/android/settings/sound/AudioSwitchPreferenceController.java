package com.android.settings.sound;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.os.Handler;
import android.os.Looper;
import android.util.FeatureFlagUtils;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import com.android.settings.bluetooth.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.bluetooth.A2dpProfile;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.HeadsetProfile;
import com.android.settingslib.bluetooth.HearingAidProfile;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class AudioSwitchPreferenceController extends BasePreferenceController implements BluetoothCallback, LifecycleObserver, OnStart, OnStop {
    private static final String TAG = "AudioSwitchPrefCtrl";
    protected final AudioManager mAudioManager;
    private final AudioManagerAudioDeviceCallback mAudioManagerAudioDeviceCallback = new AudioManagerAudioDeviceCallback();
    protected AudioSwitchCallback mAudioSwitchPreferenceCallback;
    protected final List<BluetoothDevice> mConnectedDevices = new ArrayList();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private LocalBluetoothManager mLocalBluetoothManager;
    protected final MediaRouter mMediaRouter;
    protected Preference mPreference;
    protected LocalBluetoothProfileManager mProfileManager;
    private final WiredHeadsetBroadcastReceiver mReceiver = new WiredHeadsetBroadcastReceiver();
    protected int mSelectedIndex;

    public interface AudioSwitchCallback {
        void onPreferenceDataChanged(ListPreference listPreference);
    }

    public abstract BluetoothDevice findActiveDevice();

    public AudioSwitchPreferenceController(Context context, String str) {
        super(context, str);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mMediaRouter = (MediaRouter) context.getSystemService("media_router");
        FutureTask futureTask = new FutureTask(new Callable() {
            public final Object call() {
                return AudioSwitchPreferenceController.this.lambda$new$0$AudioSwitchPreferenceController();
            }
        });
        try {
            futureTask.run();
            this.mLocalBluetoothManager = (LocalBluetoothManager) futureTask.get();
            LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
            if (localBluetoothManager == null) {
                Log.e(TAG, "Bluetooth is not supported on this device");
            } else {
                this.mProfileManager = localBluetoothManager.getProfileManager();
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.w(TAG, "Error getting LocalBluetoothManager.", e);
        }
    }

    public /* synthetic */ LocalBluetoothManager lambda$new$0$AudioSwitchPreferenceController() throws Exception {
        return Utils.getLocalBtManager(this.mContext);
    }

    public final int getAvailabilityStatus() {
        return (!FeatureFlagUtils.isEnabled(this.mContext, "settings_audio_switcher") || !this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) ? 2 : 0;
    }

    public void displayPreference(PreferenceScreen preferenceScreen) {
        super.displayPreference(preferenceScreen);
        this.mPreference = preferenceScreen.findPreference(this.mPreferenceKey);
        this.mPreference.setVisible(false);
    }

    public void onStart() {
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        localBluetoothManager.setForegroundActivity(this.mContext);
        register();
    }

    public void onStop() {
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager == null) {
            Log.e(TAG, "Bluetooth is not supported on this device");
            return;
        }
        localBluetoothManager.setForegroundActivity((Context) null);
        unregister();
    }

    public void onBluetoothStateChanged(int i) {
        updateState(this.mPreference);
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        updateState(this.mPreference);
    }

    public void onAudioModeChanged() {
        updateState(this.mPreference);
    }

    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i, int i2) {
        updateState(this.mPreference);
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
        updateState(this.mPreference);
    }

    public void setCallback(AudioSwitchCallback audioSwitchCallback) {
        this.mAudioSwitchPreferenceCallback = audioSwitchCallback;
    }

    /* access modifiers changed from: protected */
    public boolean isStreamFromOutputDevice(int i, int i2) {
        return (this.mAudioManager.getDevicesForStream(i) & i2) != 0;
    }

    /* access modifiers changed from: protected */
    public List<BluetoothDevice> getConnectedHfpDevices() {
        ArrayList arrayList = new ArrayList();
        HeadsetProfile headsetProfile = this.mProfileManager.getHeadsetProfile();
        if (headsetProfile == null) {
            return arrayList;
        }
        for (BluetoothDevice next : headsetProfile.getConnectedDevices()) {
            if (next.isConnected()) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public List<BluetoothDevice> getConnectedA2dpDevices() {
        A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
        if (a2dpProfile == null) {
            return new ArrayList();
        }
        return a2dpProfile.getConnectedDevices();
    }

    /* access modifiers changed from: protected */
    public List<BluetoothDevice> getConnectedHearingAidDevices() {
        ArrayList arrayList = new ArrayList();
        HearingAidProfile hearingAidProfile = this.mProfileManager.getHearingAidProfile();
        if (hearingAidProfile == null) {
            return arrayList;
        }
        ArrayList arrayList2 = new ArrayList();
        for (BluetoothDevice next : hearingAidProfile.getConnectedDevices()) {
            long hiSyncId = hearingAidProfile.getHiSyncId(next);
            if (!arrayList2.contains(Long.valueOf(hiSyncId)) && next.isConnected()) {
                arrayList2.add(Long.valueOf(hiSyncId));
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public BluetoothDevice findActiveHearingAidDevice() {
        HearingAidProfile hearingAidProfile = this.mProfileManager.getHearingAidProfile();
        if (hearingAidProfile == null) {
            return null;
        }
        for (BluetoothDevice next : hearingAidProfile.getActiveDevices()) {
            if (next != null && this.mConnectedDevices.contains(next)) {
                return next;
            }
        }
        return null;
    }

    private void register() {
        this.mLocalBluetoothManager.getEventManager().registerCallback(this);
        this.mAudioManager.registerAudioDeviceCallback(this.mAudioManagerAudioDeviceCallback, this.mHandler);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
    }

    private void unregister() {
        this.mLocalBluetoothManager.getEventManager().unregisterCallback(this);
        this.mAudioManager.unregisterAudioDeviceCallback(this.mAudioManagerAudioDeviceCallback);
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    private class AudioManagerAudioDeviceCallback extends AudioDeviceCallback {
        private AudioManagerAudioDeviceCallback() {
        }

        public void onAudioDevicesAdded(AudioDeviceInfo[] audioDeviceInfoArr) {
            AudioSwitchPreferenceController audioSwitchPreferenceController = AudioSwitchPreferenceController.this;
            audioSwitchPreferenceController.updateState(audioSwitchPreferenceController.mPreference);
        }

        public void onAudioDevicesRemoved(AudioDeviceInfo[] audioDeviceInfoArr) {
            AudioSwitchPreferenceController audioSwitchPreferenceController = AudioSwitchPreferenceController.this;
            audioSwitchPreferenceController.updateState(audioSwitchPreferenceController.mPreference);
        }
    }

    private class WiredHeadsetBroadcastReceiver extends BroadcastReceiver {
        private WiredHeadsetBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.HEADSET_PLUG".equals(action) || "android.media.STREAM_DEVICES_CHANGED_ACTION".equals(action)) {
                AudioSwitchPreferenceController audioSwitchPreferenceController = AudioSwitchPreferenceController.this;
                audioSwitchPreferenceController.updateState(audioSwitchPreferenceController.mPreference);
            }
        }
    }
}
