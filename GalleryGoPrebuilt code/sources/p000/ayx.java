package p000;

import android.content.Context;
import android.net.Uri;

/* renamed from: ayx */
/* compiled from: PG */
public final class ayx implements axo {

    /* renamed from: a */
    private final Context f1877a;

    public ayx(Context context) {
        this.f1877a = context.getApplicationContext();
    }

    /* renamed from: a */
    public final /* bridge */ /* synthetic */ axn mo1697a(Object obj, int i, int i2, aqz aqz) {
        Uri uri = (Uri) obj;
        if (!abj.m109a(i, i2)) {
            return null;
        }
        bfa bfa = new bfa(uri);
        Context context = this.f1877a;
        return new axn(bfa, asc.m1549a(context, uri, new asa(context.getContentResolver())));
    }

    /* renamed from: a */
    public final /* bridge */ /* synthetic */ boolean mo1698a(Object obj) {
        Uri uri = (Uri) obj;
        return abj.m110a(uri) && !abj.m118b(uri);
    }
}
