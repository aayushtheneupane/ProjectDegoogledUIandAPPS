package com.android.systemui.glwallpaper;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class EglHelper {
    private static final String TAG = "EglHelper";
    private boolean mContextPrioritySupported;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private boolean mEglReady;
    private EGLSurface mEglSurface;
    private final int[] mEglVersion = new int[2];

    public boolean init(SurfaceHolder surfaceHolder) {
        this.mEglDisplay = EGL14.eglGetDisplay(0);
        EGLDisplay eGLDisplay = this.mEglDisplay;
        if (eGLDisplay == EGL14.EGL_NO_DISPLAY) {
            String str = TAG;
            Log.w(str, "eglGetDisplay failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return false;
        }
        int[] iArr = this.mEglVersion;
        if (!EGL14.eglInitialize(eGLDisplay, iArr, 0, iArr, 1)) {
            String str2 = TAG;
            Log.w(str2, "eglInitialize failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return false;
        }
        this.mEglConfig = chooseEglConfig();
        if (this.mEglConfig == null) {
            Log.w(TAG, "eglConfig not initialized!");
            return false;
        } else if (!createEglContext()) {
            Log.w(TAG, "Can't create EGLContext!");
            return false;
        } else if (!createEglSurface(surfaceHolder)) {
            Log.w(TAG, "Can't create EGLSurface!");
            return false;
        } else {
            this.mContextPrioritySupported = isContextPrioritySuppported();
            this.mEglReady = true;
            return true;
        }
    }

    private boolean isContextPrioritySuppported() {
        for (String equals : EGL14.eglQueryString(this.mEglDisplay, 12373).split(" ")) {
            if (equals.equals("EGL_IMG_context_priority")) {
                return true;
            }
        }
        return false;
    }

    private EGLConfig chooseEglConfig() {
        int[] iArr = new int[1];
        EGLConfig[] eGLConfigArr = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(this.mEglDisplay, getConfig(), 0, eGLConfigArr, 0, 1, iArr, 0)) {
            String str = TAG;
            Log.w(str, "eglChooseConfig failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return null;
        } else if (iArr[0] > 0) {
            return eGLConfigArr[0];
        } else {
            String str2 = TAG;
            Log.w(str2, "eglChooseConfig failed, invalid configs count: " + iArr[0]);
            return null;
        }
    }

    private int[] getConfig() {
        return new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 8, 12325, 0, 12326, 0, 12352, 4, 12327, 12344, 12344};
    }

    public boolean createEglSurface(SurfaceHolder surfaceHolder) {
        if (hasEglDisplay()) {
            this.mEglSurface = EGL14.eglCreateWindowSurface(this.mEglDisplay, this.mEglConfig, surfaceHolder, (int[]) null, 0);
            EGLSurface eGLSurface = this.mEglSurface;
            if (eGLSurface == null || eGLSurface == EGL14.EGL_NO_SURFACE) {
                String str = TAG;
                Log.w(str, "createWindowSurface failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                return false;
            } else if (EGL14.eglMakeCurrent(this.mEglDisplay, eGLSurface, eGLSurface, this.mEglContext)) {
                return true;
            } else {
                String str2 = TAG;
                Log.w(str2, "eglMakeCurrent failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                return false;
            }
        } else {
            Log.w(TAG, "mEglDisplay is null");
            return false;
        }
    }

    public void destroyEglSurface() {
        if (hasEglSurface()) {
            EGLDisplay eGLDisplay = this.mEglDisplay;
            EGLSurface eGLSurface = EGL14.EGL_NO_SURFACE;
            EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.mEglDisplay, this.mEglSurface);
            this.mEglSurface = null;
        }
    }

    public boolean hasEglSurface() {
        EGLSurface eGLSurface = this.mEglSurface;
        return (eGLSurface == null || eGLSurface == EGL14.EGL_NO_SURFACE) ? false : true;
    }

    public boolean createEglContext() {
        int[] iArr = new int[5];
        iArr[0] = 12440;
        char c = 2;
        iArr[1] = 2;
        if (this.mContextPrioritySupported) {
            iArr[2] = 12544;
            c = 4;
            iArr[3] = 12547;
        }
        iArr[c] = 12344;
        if (hasEglDisplay()) {
            this.mEglContext = EGL14.eglCreateContext(this.mEglDisplay, this.mEglConfig, EGL14.EGL_NO_CONTEXT, iArr, 0);
            if (this.mEglContext != EGL14.EGL_NO_CONTEXT) {
                return true;
            }
            String str = TAG;
            Log.w(str, "eglCreateContext failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return false;
        }
        Log.w(TAG, "mEglDisplay is null");
        return false;
    }

    public void destroyEglContext() {
        if (hasEglContext()) {
            EGL14.eglDestroyContext(this.mEglDisplay, this.mEglContext);
            this.mEglContext = null;
        }
    }

    public boolean hasEglContext() {
        return this.mEglContext != null;
    }

    public boolean hasEglDisplay() {
        return this.mEglDisplay != null;
    }

    public boolean swapBuffer() {
        boolean eglSwapBuffers = EGL14.eglSwapBuffers(this.mEglDisplay, this.mEglSurface);
        int eglGetError = EGL14.eglGetError();
        if (eglGetError != 12288) {
            String str = TAG;
            Log.w(str, "eglSwapBuffers failed: " + GLUtils.getEGLErrorString(eglGetError));
        }
        return eglSwapBuffers;
    }

    public void finish() {
        if (hasEglSurface()) {
            destroyEglSurface();
        }
        if (hasEglContext()) {
            destroyEglContext();
        }
        if (hasEglDisplay()) {
            EGL14.eglTerminate(this.mEglDisplay);
        }
        this.mEglReady = false;
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mEglVersion[0]);
        sb.append(".");
        sb.append(this.mEglVersion[1]);
        String sb2 = sb.toString();
        printWriter.print(str);
        printWriter.print("EGL version=");
        printWriter.print(sb2);
        printWriter.print(", ");
        printWriter.print("EGL ready=");
        printWriter.print(this.mEglReady);
        printWriter.print(", ");
        printWriter.print("has EglContext=");
        printWriter.print(hasEglContext());
        printWriter.print(", ");
        printWriter.print("has EglSurface=");
        printWriter.println(hasEglSurface());
        int[] config = getConfig();
        StringBuilder sb3 = new StringBuilder();
        sb3.append('{');
        for (int hexString : config) {
            sb3.append("0x");
            sb3.append(Integer.toHexString(hexString));
            sb3.append(",");
        }
        sb3.setCharAt(sb3.length() - 1, '}');
        printWriter.print(str);
        printWriter.print("EglConfig=");
        printWriter.println(sb3.toString());
    }
}
