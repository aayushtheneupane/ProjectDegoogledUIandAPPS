package com.android.settingslib.bluetooth;

import android.content.Context;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import java.lang.ref.WeakReference;

public class LocalBluetoothManager {
    private static LocalBluetoothManager sInstance;
    private final CachedBluetoothDeviceManager mCachedDeviceManager = new CachedBluetoothDeviceManager(this.mContext, this);
    private final Context mContext;
    private final BluetoothEventManager mEventManager;
    private WeakReference<Context> mForegroundActivity;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final LocalBluetoothProfileManager mProfileManager;

    public interface BluetoothManagerCallback {
        void onBluetoothManagerInitialized(Context context, LocalBluetoothManager localBluetoothManager);
    }

    public static synchronized LocalBluetoothManager getInstance(Context context, BluetoothManagerCallback bluetoothManagerCallback) {
        synchronized (LocalBluetoothManager.class) {
            if (sInstance == null) {
                LocalBluetoothAdapter instance = LocalBluetoothAdapter.getInstance();
                if (instance == null) {
                    return null;
                }
                sInstance = new LocalBluetoothManager(instance, context, (Handler) null, (UserHandle) null);
                if (bluetoothManagerCallback != null) {
                    bluetoothManagerCallback.onBluetoothManagerInitialized(context.getApplicationContext(), sInstance);
                }
            }
            LocalBluetoothManager localBluetoothManager = sInstance;
            return localBluetoothManager;
        }
    }

    private LocalBluetoothManager(LocalBluetoothAdapter localBluetoothAdapter, Context context, Handler handler, UserHandle userHandle) {
        this.mContext = context.getApplicationContext();
        this.mLocalAdapter = localBluetoothAdapter;
        this.mEventManager = new BluetoothEventManager(this.mLocalAdapter, this.mCachedDeviceManager, this.mContext, handler, userHandle);
        this.mProfileManager = new LocalBluetoothProfileManager(this.mContext, this.mLocalAdapter, this.mCachedDeviceManager, this.mEventManager);
        this.mProfileManager.updateLocalProfiles();
        this.mEventManager.readPairedDevices();
    }

    public LocalBluetoothAdapter getBluetoothAdapter() {
        return this.mLocalAdapter;
    }

    public Context getForegroundActivity() {
        WeakReference<Context> weakReference = this.mForegroundActivity;
        if (weakReference == null) {
            return null;
        }
        return (Context) weakReference.get();
    }

    public boolean isForegroundActivity() {
        WeakReference<Context> weakReference = this.mForegroundActivity;
        return (weakReference == null || weakReference.get() == null) ? false : true;
    }

    public synchronized void setForegroundActivity(Context context) {
        if (context != null) {
            Log.d("LocalBluetoothManager", "setting foreground activity to non-null context");
            this.mForegroundActivity = new WeakReference<>(context);
        } else if (this.mForegroundActivity != null) {
            Log.d("LocalBluetoothManager", "setting foreground activity to null");
            this.mForegroundActivity = null;
        }
    }

    public CachedBluetoothDeviceManager getCachedDeviceManager() {
        return this.mCachedDeviceManager;
    }

    public BluetoothEventManager getEventManager() {
        return this.mEventManager;
    }

    public LocalBluetoothProfileManager getProfileManager() {
        return this.mProfileManager;
    }
}
