package com.android.systemui.statusbar.phone;

import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import com.android.systemui.util.Assert;

/* compiled from: KeyguardLiftController.kt */
public final class KeyguardLiftController$listener$1 extends TriggerEventListener {
    final /* synthetic */ KeyguardLiftController this$0;

    KeyguardLiftController$listener$1(KeyguardLiftController keyguardLiftController) {
        this.this$0 = keyguardLiftController;
    }

    public void onTrigger(TriggerEvent triggerEvent) {
        Assert.isMainThread();
        this.this$0.isListening = false;
        this.this$0.updateListeningState();
        this.this$0.keyguardUpdateMonitor.requestFaceAuth();
    }
}
