package p000;

import android.graphics.PointF;
import android.util.Property;
import android.view.View;

/* renamed from: aet */
/* compiled from: PG */
final class aet extends Property {
    public aet(Class cls, String str) {
        super(cls, str);
    }

    public final /* bridge */ /* synthetic */ Object get(Object obj) {
        View view = (View) obj;
        return null;
    }

    public final /* bridge */ /* synthetic */ void set(Object obj, Object obj2) {
        View view = (View) obj;
        PointF pointF = (PointF) obj2;
        agb.m423a(view, Math.round(pointF.x), Math.round(pointF.y), view.getRight(), view.getBottom());
    }
}
