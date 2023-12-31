package androidx.core.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import androidx.core.content.p022a.C0311d;
import androidx.core.content.p022a.C0312e;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class TypefaceCompatApi21Impl extends TypefaceCompatBaseImpl {
    private static final String ADD_FONT_WEIGHT_STYLE_METHOD = "addFontWeightStyle";
    private static final String CREATE_FROM_FAMILIES_WITH_DEFAULT_METHOD = "createFromFamiliesWithDefault";
    private static final String FONT_FAMILY_CLASS = "android.graphics.FontFamily";
    private static final String TAG = "TypefaceCompatApi21Impl";
    private static Method sAddFontWeightStyle = null;
    private static Method sCreateFromFamiliesWithDefault = null;
    private static Class sFontFamily = null;
    private static Constructor sFontFamilyCtor = null;
    private static boolean sHasInitBeenCalled = false;

    TypefaceCompatApi21Impl() {
    }

    private static boolean addFontWeightStyle(Object obj, String str, int i, boolean z) {
        init();
        try {
            return ((Boolean) sAddFontWeightStyle.invoke(obj, new Object[]{str, Integer.valueOf(i), Boolean.valueOf(z)})).booleanValue();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Typeface createFromFamiliesWithDefault(Object obj) {
        init();
        try {
            Object newInstance = Array.newInstance(sFontFamily, 1);
            Array.set(newInstance, 0, obj);
            return (Typeface) sCreateFromFamiliesWithDefault.invoke((Object) null, new Object[]{newInstance});
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(ParcelFileDescriptor parcelFileDescriptor) {
        try {
            String readlink = Os.readlink("/proc/self/fd/" + parcelFileDescriptor.getFd());
            if (OsConstants.S_ISREG(Os.stat(readlink).st_mode)) {
                return new File(readlink);
            }
        } catch (ErrnoException unused) {
        }
        return null;
    }

    private static void init() {
        Method method;
        Class<?> cls;
        Method method2;
        if (!sHasInitBeenCalled) {
            sHasInitBeenCalled = true;
            Constructor<?> constructor = null;
            try {
                cls = Class.forName(FONT_FAMILY_CLASS);
                Constructor<?> constructor2 = cls.getConstructor(new Class[0]);
                method = cls.getMethod(ADD_FONT_WEIGHT_STYLE_METHOD, new Class[]{String.class, Integer.TYPE, Boolean.TYPE});
                method2 = Typeface.class.getMethod(CREATE_FROM_FAMILIES_WITH_DEFAULT_METHOD, new Class[]{Array.newInstance(cls, 1).getClass()});
                constructor = constructor2;
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                Log.e(TAG, e.getClass().getName(), e);
                method2 = null;
                cls = null;
                method = null;
            }
            sFontFamilyCtor = constructor;
            sFontFamily = cls;
            sAddFontWeightStyle = method;
            sCreateFromFamiliesWithDefault = method2;
        }
    }

    private static Object newFamily() {
        init();
        try {
            return sFontFamilyCtor.newInstance(new Object[0]);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Typeface createFromFontFamilyFilesResourceEntry(Context context, C0311d dVar, Resources resources, int i) {
        Object newFamily = newFamily();
        C0312e[] entries = dVar.getEntries();
        int length = entries.length;
        int i2 = 0;
        while (i2 < length) {
            C0312e eVar = entries[i2];
            File tempFile = TypefaceCompatUtil.getTempFile(context);
            if (tempFile == null) {
                return null;
            }
            try {
                if (!TypefaceCompatUtil.copyToFile(tempFile, resources, eVar.getResourceId())) {
                    tempFile.delete();
                    return null;
                } else if (!addFontWeightStyle(newFamily, tempFile.getPath(), eVar.getWeight(), eVar.isItalic())) {
                    return null;
                } else {
                    tempFile.delete();
                    i2++;
                }
            } catch (RuntimeException unused) {
                return null;
            } finally {
                tempFile.delete();
            }
        }
        return createFromFamiliesWithDefault(newFamily);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x004b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r6.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0050, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        r3.addSuppressed(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0054, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0057, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0060, code lost:
        throw r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.graphics.Typeface createFromFontInfo(android.content.Context r4, android.os.CancellationSignal r5, androidx.core.provider.FontsContractCompat.FontInfo[] r6, int r7) {
        /*
            r3 = this;
            int r0 = r6.length
            r1 = 0
            r2 = 1
            if (r0 >= r2) goto L_0x0006
            return r1
        L_0x0006:
            androidx.core.provider.FontsContractCompat$FontInfo r6 = r3.findBestInfo(r6, r7)
            android.content.ContentResolver r7 = r4.getContentResolver()
            android.net.Uri r6 = r6.getUri()     // Catch:{ IOException -> 0x0061 }
            java.lang.String r0 = "r"
            android.os.ParcelFileDescriptor r5 = r7.openFileDescriptor(r6, r0, r5)     // Catch:{ IOException -> 0x0061 }
            if (r5 != 0) goto L_0x0020
            if (r5 == 0) goto L_0x001f
            r5.close()     // Catch:{ IOException -> 0x0061 }
        L_0x001f:
            return r1
        L_0x0020:
            java.io.File r6 = r3.getFile(r5)     // Catch:{ all -> 0x0055 }
            if (r6 == 0) goto L_0x0035
            boolean r7 = r6.canRead()     // Catch:{ all -> 0x0055 }
            if (r7 != 0) goto L_0x002d
            goto L_0x0035
        L_0x002d:
            android.graphics.Typeface r3 = android.graphics.Typeface.createFromFile(r6)     // Catch:{ all -> 0x0055 }
            r5.close()     // Catch:{ IOException -> 0x0061 }
            return r3
        L_0x0035:
            java.io.FileInputStream r6 = new java.io.FileInputStream     // Catch:{ all -> 0x0055 }
            java.io.FileDescriptor r7 = r5.getFileDescriptor()     // Catch:{ all -> 0x0055 }
            r6.<init>(r7)     // Catch:{ all -> 0x0055 }
            android.graphics.Typeface r3 = super.createFromInputStream(r4, r6)     // Catch:{ all -> 0x0049 }
            r6.close()     // Catch:{ all -> 0x0055 }
            r5.close()     // Catch:{ IOException -> 0x0061 }
            return r3
        L_0x0049:
            r3 = move-exception
            throw r3     // Catch:{ all -> 0x004b }
        L_0x004b:
            r4 = move-exception
            r6.close()     // Catch:{ all -> 0x0050 }
            goto L_0x0054
        L_0x0050:
            r6 = move-exception
            r3.addSuppressed(r6)     // Catch:{ all -> 0x0055 }
        L_0x0054:
            throw r4     // Catch:{ all -> 0x0055 }
        L_0x0055:
            r3 = move-exception
            throw r3     // Catch:{ all -> 0x0057 }
        L_0x0057:
            r4 = move-exception
            r5.close()     // Catch:{ all -> 0x005c }
            goto L_0x0060
        L_0x005c:
            r5 = move-exception
            r3.addSuppressed(r5)     // Catch:{ IOException -> 0x0061 }
        L_0x0060:
            throw r4     // Catch:{ IOException -> 0x0061 }
        L_0x0061:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.core.graphics.TypefaceCompatApi21Impl.createFromFontInfo(android.content.Context, android.os.CancellationSignal, androidx.core.provider.FontsContractCompat$FontInfo[], int):android.graphics.Typeface");
    }
}
