package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import java.util.List;

public class HeadsetProfile implements LocalBluetoothProfile {
    static final ParcelUuid[] UUIDS = {BluetoothUuid.HSP, BluetoothUuid.Handsfree};
    /* access modifiers changed from: private */
    public final CachedBluetoothDeviceManager mDeviceManager;
    /* access modifiers changed from: private */
    public boolean mIsProfileReady;
    /* access modifiers changed from: private */
    public final LocalBluetoothProfileManager mProfileManager;
    /* access modifiers changed from: private */
    public BluetoothHeadset mService;

    public boolean accessProfileEnabled() {
        return true;
    }

    public int getProfileId() {
        return 1;
    }

    public boolean isAutoConnectable() {
        return true;
    }

    public String toString() {
        return "HEADSET";
    }

    private final class HeadsetServiceListener implements BluetoothProfile.ServiceListener {
        private HeadsetServiceListener() {
        }

        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            BluetoothHeadset unused = HeadsetProfile.this.mService = (BluetoothHeadset) bluetoothProfile;
            List<BluetoothDevice> connectedDevices = HeadsetProfile.this.mService.getConnectedDevices();
            while (!connectedDevices.isEmpty()) {
                BluetoothDevice remove = connectedDevices.remove(0);
                CachedBluetoothDevice findDevice = HeadsetProfile.this.mDeviceManager.findDevice(remove);
                if (findDevice == null) {
                    Log.w("HeadsetProfile", "HeadsetProfile found new device: " + remove);
                    findDevice = HeadsetProfile.this.mDeviceManager.addDevice(remove);
                }
                findDevice.onProfileStateChanged(HeadsetProfile.this, 2);
                findDevice.refresh();
            }
            boolean unused2 = HeadsetProfile.this.mIsProfileReady = true;
            HeadsetProfile.this.mProfileManager.callServiceConnectedListeners();
        }

        public void onServiceDisconnected(int i) {
            HeadsetProfile.this.mProfileManager.callServiceDisconnectedListeners();
            boolean unused = HeadsetProfile.this.mIsProfileReady = false;
        }
    }

    HeadsetProfile(Context context, CachedBluetoothDeviceManager cachedBluetoothDeviceManager, LocalBluetoothProfileManager localBluetoothProfileManager) {
        this.mDeviceManager = cachedBluetoothDeviceManager;
        this.mProfileManager = localBluetoothProfileManager;
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, new HeadsetServiceListener(), 1);
    }

    public boolean connect(BluetoothDevice bluetoothDevice) {
        BluetoothHeadset bluetoothHeadset = this.mService;
        if (bluetoothHeadset == null) {
            return false;
        }
        return bluetoothHeadset.connect(bluetoothDevice);
    }

    public boolean disconnect(BluetoothDevice bluetoothDevice) {
        BluetoothHeadset bluetoothHeadset = this.mService;
        if (bluetoothHeadset == null) {
            return false;
        }
        if (bluetoothHeadset.getPriority(bluetoothDevice) > 100) {
            this.mService.setPriority(bluetoothDevice, 100);
        }
        return this.mService.disconnect(bluetoothDevice);
    }

    public int getConnectionStatus(BluetoothDevice bluetoothDevice) {
        BluetoothHeadset bluetoothHeadset = this.mService;
        if (bluetoothHeadset == null) {
            return 0;
        }
        return bluetoothHeadset.getConnectionState(bluetoothDevice);
    }

    public BluetoothDevice getActiveDevice() {
        BluetoothHeadset bluetoothHeadset = this.mService;
        if (bluetoothHeadset == null) {
            return null;
        }
        return bluetoothHeadset.getActiveDevice();
    }

    public boolean isPreferred(BluetoothDevice bluetoothDevice) {
        BluetoothHeadset bluetoothHeadset = this.mService;
        if (bluetoothHeadset != null && bluetoothHeadset.getPriority(bluetoothDevice) > 0) {
            return true;
        }
        return false;
    }

    public void setPreferred(BluetoothDevice bluetoothDevice, boolean z) {
        BluetoothHeadset bluetoothHeadset = this.mService;
        if (bluetoothHeadset != null) {
            if (!z) {
                bluetoothHeadset.setPriority(bluetoothDevice, 0);
            } else if (bluetoothHeadset.getPriority(bluetoothDevice) < 100) {
                this.mService.setPriority(bluetoothDevice, 100);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        Log.d("HeadsetProfile", "finalize()");
        if (this.mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(1, this.mService);
                this.mService = null;
            } catch (Throwable th) {
                Log.w("HeadsetProfile", "Error cleaning up HID proxy", th);
            }
        }
    }
}
