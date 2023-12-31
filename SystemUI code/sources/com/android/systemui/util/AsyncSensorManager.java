package com.android.systemui.util;

import android.content.Context;
import android.hardware.HardwareBuffer;
import android.hardware.Sensor;
import android.hardware.SensorAdditionalInfo;
import android.hardware.SensorDirectChannel;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.MemoryFile;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.shared.plugins.PluginManager;
import java.util.ArrayList;
import java.util.List;

public class AsyncSensorManager extends SensorManager implements PluginListener<SensorManagerPlugin> {
    @VisibleForTesting
    final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private final SensorManager mInner;
    private final List<SensorManagerPlugin> mPlugins;
    private final List<Sensor> mSensorCache;

    public AsyncSensorManager(Context context, PluginManager pluginManager) {
        this((SensorManager) context.getSystemService(SensorManager.class), pluginManager);
    }

    @VisibleForTesting
    AsyncSensorManager(SensorManager sensorManager, PluginManager pluginManager) {
        this.mHandlerThread = new HandlerThread("async_sensor");
        this.mInner = sensorManager;
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mSensorCache = this.mInner.getSensorList(-1);
        this.mPlugins = new ArrayList();
        pluginManager.addPluginListener(this, (Class<?>) SensorManagerPlugin.class, true);
    }

    /* access modifiers changed from: protected */
    public List<Sensor> getFullSensorList() {
        return this.mSensorCache;
    }

    /* access modifiers changed from: protected */
    public List<Sensor> getFullDynamicSensorList() {
        return this.mInner.getSensorList(-1);
    }

    /* access modifiers changed from: protected */
    public boolean registerListenerImpl(SensorEventListener sensorEventListener, Sensor sensor, int i, Handler handler, int i2, int i3) {
        this.mHandler.post(new Runnable(sensorEventListener, sensor, i, i2, handler) {
            private final /* synthetic */ SensorEventListener f$1;
            private final /* synthetic */ Sensor f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ int f$4;
            private final /* synthetic */ Handler f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$registerListenerImpl$0$AsyncSensorManager(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$registerListenerImpl$0$AsyncSensorManager(SensorEventListener sensorEventListener, Sensor sensor, int i, int i2, Handler handler) {
        if (!this.mInner.registerListener(sensorEventListener, sensor, i, i2, handler)) {
            Log.e("AsyncSensorManager", "Registering " + sensorEventListener + " for " + sensor + " failed.");
        }
    }

    /* access modifiers changed from: protected */
    public boolean flushImpl(SensorEventListener sensorEventListener) {
        return this.mInner.flush(sensorEventListener);
    }

    /* access modifiers changed from: protected */
    public SensorDirectChannel createDirectChannelImpl(MemoryFile memoryFile, HardwareBuffer hardwareBuffer) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* access modifiers changed from: protected */
    public void destroyDirectChannelImpl(SensorDirectChannel sensorDirectChannel) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* access modifiers changed from: protected */
    public int configureDirectChannelImpl(SensorDirectChannel sensorDirectChannel, Sensor sensor, int i) {
        throw new UnsupportedOperationException("not implemented");
    }

    public /* synthetic */ void lambda$registerDynamicSensorCallbackImpl$1$AsyncSensorManager(SensorManager.DynamicSensorCallback dynamicSensorCallback, Handler handler) {
        this.mInner.registerDynamicSensorCallback(dynamicSensorCallback, handler);
    }

    /* access modifiers changed from: protected */
    public void registerDynamicSensorCallbackImpl(SensorManager.DynamicSensorCallback dynamicSensorCallback, Handler handler) {
        this.mHandler.post(new Runnable(dynamicSensorCallback, handler) {
            private final /* synthetic */ SensorManager.DynamicSensorCallback f$1;
            private final /* synthetic */ Handler f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$registerDynamicSensorCallbackImpl$1$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterDynamicSensorCallbackImpl$2$AsyncSensorManager(SensorManager.DynamicSensorCallback dynamicSensorCallback) {
        this.mInner.unregisterDynamicSensorCallback(dynamicSensorCallback);
    }

    /* access modifiers changed from: protected */
    public void unregisterDynamicSensorCallbackImpl(SensorManager.DynamicSensorCallback dynamicSensorCallback) {
        this.mHandler.post(new Runnable(dynamicSensorCallback) {
            private final /* synthetic */ SensorManager.DynamicSensorCallback f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$unregisterDynamicSensorCallbackImpl$2$AsyncSensorManager(this.f$1);
            }
        });
    }

    /* access modifiers changed from: protected */
    public boolean requestTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor) {
        this.mHandler.post(new Runnable(triggerEventListener, sensor) {
            private final /* synthetic */ TriggerEventListener f$1;
            private final /* synthetic */ Sensor f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$requestTriggerSensorImpl$3$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$requestTriggerSensorImpl$3$AsyncSensorManager(TriggerEventListener triggerEventListener, Sensor sensor) {
        if (!this.mInner.requestTriggerSensor(triggerEventListener, sensor)) {
            Log.e("AsyncSensorManager", "Requesting " + triggerEventListener + " for " + sensor + " failed.");
        }
    }

    /* access modifiers changed from: protected */
    public boolean cancelTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor, boolean z) {
        Preconditions.checkArgument(z);
        this.mHandler.post(new Runnable(triggerEventListener, sensor) {
            private final /* synthetic */ TriggerEventListener f$1;
            private final /* synthetic */ Sensor f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$cancelTriggerSensorImpl$4$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$cancelTriggerSensorImpl$4$AsyncSensorManager(TriggerEventListener triggerEventListener, Sensor sensor) {
        if (!this.mInner.cancelTriggerSensor(triggerEventListener, sensor)) {
            Log.e("AsyncSensorManager", "Canceling " + triggerEventListener + " for " + sensor + " failed.");
        }
    }

    public boolean registerPluginListener(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        if (this.mPlugins.isEmpty()) {
            Log.w("AsyncSensorManager", "No plugins registered");
            return false;
        }
        this.mHandler.post(new Runnable(sensor, sensorEventListener) {
            private final /* synthetic */ SensorManagerPlugin.Sensor f$1;
            private final /* synthetic */ SensorManagerPlugin.SensorEventListener f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$registerPluginListener$5$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
        return true;
    }

    public /* synthetic */ void lambda$registerPluginListener$5$AsyncSensorManager(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        for (int i = 0; i < this.mPlugins.size(); i++) {
            this.mPlugins.get(i).registerListener(sensor, sensorEventListener);
        }
    }

    public void unregisterPluginListener(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        this.mHandler.post(new Runnable(sensor, sensorEventListener) {
            private final /* synthetic */ SensorManagerPlugin.Sensor f$1;
            private final /* synthetic */ SensorManagerPlugin.SensorEventListener f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$unregisterPluginListener$6$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterPluginListener$6$AsyncSensorManager(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        for (int i = 0; i < this.mPlugins.size(); i++) {
            this.mPlugins.get(i).unregisterListener(sensor, sensorEventListener);
        }
    }

    /* access modifiers changed from: protected */
    public boolean initDataInjectionImpl(boolean z) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* access modifiers changed from: protected */
    public boolean injectSensorDataImpl(Sensor sensor, float[] fArr, int i, long j) {
        throw new UnsupportedOperationException("not implemented");
    }

    public /* synthetic */ void lambda$setOperationParameterImpl$7$AsyncSensorManager(SensorAdditionalInfo sensorAdditionalInfo) {
        this.mInner.setOperationParameter(sensorAdditionalInfo);
    }

    /* access modifiers changed from: protected */
    public boolean setOperationParameterImpl(SensorAdditionalInfo sensorAdditionalInfo) {
        this.mHandler.post(new Runnable(sensorAdditionalInfo) {
            private final /* synthetic */ SensorAdditionalInfo f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$setOperationParameterImpl$7$AsyncSensorManager(this.f$1);
            }
        });
        return true;
    }

    /* access modifiers changed from: protected */
    public void unregisterListenerImpl(SensorEventListener sensorEventListener, Sensor sensor) {
        this.mHandler.post(new Runnable(sensor, sensorEventListener) {
            private final /* synthetic */ Sensor f$1;
            private final /* synthetic */ SensorEventListener f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$unregisterListenerImpl$8$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$unregisterListenerImpl$8$AsyncSensorManager(Sensor sensor, SensorEventListener sensorEventListener) {
        if (sensor == null) {
            this.mInner.unregisterListener(sensorEventListener);
        } else {
            this.mInner.unregisterListener(sensorEventListener, sensor);
        }
    }

    public void onPluginConnected(SensorManagerPlugin sensorManagerPlugin, Context context) {
        this.mPlugins.add(sensorManagerPlugin);
    }

    public void onPluginDisconnected(SensorManagerPlugin sensorManagerPlugin) {
        this.mPlugins.remove(sensorManagerPlugin);
    }
}
