package com.android.settingslib.graph;

/* compiled from: FullCircleBatteryDrawable.kt */
final class FullCircleBatteryDrawable$postInvalidate$1 implements Runnable {
    final /* synthetic */ FullCircleBatteryDrawable this$0;

    FullCircleBatteryDrawable$postInvalidate$1(FullCircleBatteryDrawable fullCircleBatteryDrawable) {
        this.this$0 = fullCircleBatteryDrawable;
    }

    public final void run() {
        this.this$0.invalidateSelf();
    }
}
