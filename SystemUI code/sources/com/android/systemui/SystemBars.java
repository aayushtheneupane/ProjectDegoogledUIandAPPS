package com.android.systemui;

import android.util.Log;
import com.android.systemui.statusbar.phone.StatusBar;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SystemBars extends SystemUI {
    private SystemUI mStatusBar;

    public void start() {
        createStatusBarFromConfig();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        SystemUI systemUI = this.mStatusBar;
        if (systemUI != null) {
            systemUI.dump(fileDescriptor, printWriter, strArr);
        }
    }

    private void createStatusBarFromConfig() {
        String string = this.mContext.getString(C1784R$string.config_statusBarComponent);
        if (string == null || string.length() == 0) {
            andLog("No status bar component configured", (Throwable) null);
            throw null;
        }
        try {
            try {
                this.mStatusBar = (SystemUI) this.mContext.getClassLoader().loadClass(string).newInstance();
                SystemUI systemUI = this.mStatusBar;
                systemUI.mContext = this.mContext;
                systemUI.mComponents = this.mComponents;
                if (systemUI instanceof StatusBar) {
                    SystemUIFactory.getInstance().getRootComponent().getStatusBarInjector().createStatusBar((StatusBar) this.mStatusBar);
                }
                this.mStatusBar.start();
            } catch (Throwable th) {
                andLog("Error creating status bar component: " + string, th);
                throw null;
            }
        } catch (Throwable th2) {
            andLog("Error loading status bar component: " + string, th2);
            throw null;
        }
    }

    private RuntimeException andLog(String str, Throwable th) {
        Log.w("SystemBars", str, th);
        throw new RuntimeException(str, th);
    }
}
