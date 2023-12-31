package com.android.systemui;

import dagger.internal.Factory;

public final class SystemUIDefaultModule_ProvideLeakReportEmailFactory implements Factory<String> {
    private static final SystemUIDefaultModule_ProvideLeakReportEmailFactory INSTANCE = new SystemUIDefaultModule_ProvideLeakReportEmailFactory();

    public String get() {
        return provideInstance();
    }

    public static String provideInstance() {
        return proxyProvideLeakReportEmail();
    }

    public static SystemUIDefaultModule_ProvideLeakReportEmailFactory create() {
        return INSTANCE;
    }

    public static String proxyProvideLeakReportEmail() {
        return SystemUIDefaultModule.provideLeakReportEmail();
    }
}
