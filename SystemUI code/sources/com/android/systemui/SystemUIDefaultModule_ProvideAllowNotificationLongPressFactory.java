package com.android.systemui;

import dagger.internal.Factory;

public final class SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory implements Factory<Boolean> {
    private static final SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory INSTANCE = new SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory();

    public Boolean get() {
        return provideInstance();
    }

    public static Boolean provideInstance() {
        return Boolean.valueOf(proxyProvideAllowNotificationLongPress());
    }

    public static SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory create() {
        return INSTANCE;
    }

    public static boolean proxyProvideAllowNotificationLongPress() {
        return SystemUIDefaultModule.provideAllowNotificationLongPress();
    }
}
