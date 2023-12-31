package com.android.systemui.plugins;

import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@DependsOn(target = GlobalActionsManager.class)
@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_GLOBAL_ACTIONS", version = 1)
public interface GlobalActions extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_GLOBAL_ACTIONS";
    public static final int VERSION = 1;

    @ProvidesInterface(version = 1)
    public interface GlobalActionsManager {
        public static final int VERSION = 1;

        void advancedReboot(String str);

        void onGlobalActionsHidden();

        void onGlobalActionsShown();

        void reboot(boolean z);

        void shutdown();
    }

    void destroy() {
    }

    void showGlobalActions(GlobalActionsManager globalActionsManager);

    void showShutdownUi(boolean z, String str) {
    }
}
