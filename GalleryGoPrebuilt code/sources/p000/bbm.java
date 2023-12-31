package p000;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import java.util.List;

/* renamed from: bbm */
/* compiled from: PG */
public final class bbm implements arb {

    /* renamed from: a */
    private final Context f1996a;

    public bbm(Context context) {
        this.f1996a = context.getApplicationContext();
    }

    /* renamed from: a */
    public final aua mo1777a(Uri uri) {
        Context context;
        int i;
        String authority = uri.getAuthority();
        if (!authority.equals(this.f1996a.getPackageName())) {
            try {
                context = this.f1996a.createPackageContext(authority, 0);
            } catch (PackageManager.NameNotFoundException e) {
                if (authority.contains(this.f1996a.getPackageName())) {
                    context = this.f1996a;
                } else {
                    String valueOf = String.valueOf(uri);
                    StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 57);
                    sb.append("Failed to obtain context or unrecognized Uri format for: ");
                    sb.append(valueOf);
                    throw new IllegalArgumentException(sb.toString(), e);
                }
            }
        } else {
            context = this.f1996a;
        }
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 2) {
            List<String> pathSegments2 = uri.getPathSegments();
            String authority2 = uri.getAuthority();
            String str = pathSegments2.get(0);
            String str2 = pathSegments2.get(1);
            int identifier = context.getResources().getIdentifier(str2, str, authority2);
            int identifier2 = identifier == 0 ? Resources.getSystem().getIdentifier(str2, str, "android") : identifier;
            if (identifier2 != 0) {
                i = identifier2;
            } else {
                String valueOf2 = String.valueOf(uri);
                StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 32);
                sb2.append("Failed to find resource id for: ");
                sb2.append(valueOf2);
                throw new IllegalArgumentException(sb2.toString());
            }
        } else if (pathSegments.size() == 1) {
            try {
                i = Integer.parseInt(uri.getPathSegments().get(0));
            } catch (NumberFormatException e2) {
                String valueOf3 = String.valueOf(uri);
                StringBuilder sb3 = new StringBuilder(String.valueOf(valueOf3).length() + 25);
                sb3.append("Unrecognized Uri format: ");
                sb3.append(valueOf3);
                throw new IllegalArgumentException(sb3.toString(), e2);
            }
        } else {
            String valueOf4 = String.valueOf(uri);
            StringBuilder sb4 = new StringBuilder(String.valueOf(valueOf4).length() + 25);
            sb4.append("Unrecognized Uri format: ");
            sb4.append(valueOf4);
            throw new IllegalArgumentException(sb4.toString());
        }
        return bbl.m2101a(bbj.m2098a(this.f1996a, context, i));
    }

    /* renamed from: a */
    public final /* bridge */ /* synthetic */ aua mo1507a(Object obj, int i, int i2, aqz aqz) {
        return mo1777a((Uri) obj);
    }

    /* renamed from: a */
    public final /* bridge */ /* synthetic */ boolean mo1508a(Object obj, aqz aqz) {
        return ((Uri) obj).getScheme().equals("android.resource");
    }
}
