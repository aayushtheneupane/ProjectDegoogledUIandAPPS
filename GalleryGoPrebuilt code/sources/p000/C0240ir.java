package p000;

import android.graphics.Path;
import android.util.Log;

/* renamed from: ir */
/* compiled from: PG */
public final class C0240ir {

    /* renamed from: a */
    public char f14812a;

    /* renamed from: b */
    public final float[] f14813b;

    public C0240ir(char c, float[] fArr) {
        this.f14812a = c;
        this.f14813b = fArr;
    }

    public C0240ir(C0240ir irVar) {
        this.f14812a = irVar.f14812a;
        float[] fArr = irVar.f14813b;
        this.f14813b = C0257jh.m14483a(fArr, fArr.length);
    }

    /* renamed from: a */
    public static void m14342a(Path path, float f, float f2, float f3, float f4, float f5, float f6, float f7, boolean z, boolean z2) {
        double d;
        double d2;
        float f8 = f;
        float f9 = f3;
        boolean z3 = z2;
        double radians = Math.toRadians((double) f7);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double d3 = (double) f8;
        double d4 = (double) f2;
        double d5 = radians;
        double d6 = (double) f5;
        Double.isNaN(d3);
        Double.isNaN(d4);
        Double.isNaN(d6);
        double d7 = ((d3 * cos) + (d4 * sin)) / d6;
        double d8 = d3;
        double d9 = (double) f6;
        double d10 = (double) (-f8);
        Double.isNaN(d10);
        Double.isNaN(d4);
        Double.isNaN(d9);
        double d11 = d4;
        double d12 = (double) f4;
        double d13 = ((d10 * sin) + (d4 * cos)) / d9;
        double d14 = (double) f9;
        Double.isNaN(d14);
        Double.isNaN(d12);
        Double.isNaN(d6);
        double d15 = ((d14 * cos) + (d12 * sin)) / d6;
        double d16 = (double) (-f9);
        Double.isNaN(d16);
        Double.isNaN(d12);
        Double.isNaN(d9);
        double d17 = ((d16 * sin) + (d12 * cos)) / d9;
        double d18 = d7 - d15;
        double d19 = d13 - d17;
        double d20 = (d7 + d15) / 2.0d;
        double d21 = (d13 + d17) / 2.0d;
        double d22 = sin;
        double d23 = (d18 * d18) + (d19 * d19);
        if (d23 != 0.0d) {
            double d24 = (1.0d / d23) - 16.0d;
            if (d24 < 0.0d) {
                Log.w("PathParser", "Points are too far apart " + d23);
                float sqrt = (float) (Math.sqrt(d23) / 1.99999d);
                m14342a(path, f, f2, f3, f4, f5 * sqrt, f6 * sqrt, f7, z, z2);
                return;
            }
            double sqrt2 = Math.sqrt(d24);
            double d25 = d18 * sqrt2;
            double d26 = sqrt2 * d19;
            if (z != z3) {
                d2 = d20 + d26;
                d = d21 - d25;
            } else {
                d2 = d20 - d26;
                d = d21 + d25;
            }
            double atan2 = Math.atan2(d13 - d, d7 - d2);
            double atan22 = Math.atan2(d17 - d, d15 - d2) - atan2;
            int i = 0;
            if (z3 != (atan22 >= 0.0d)) {
                atan22 += atan22 > 0.0d ? -6.283185307179586d : 6.283185307179586d;
            }
            Double.isNaN(d6);
            double d27 = d2 * d6;
            Double.isNaN(d9);
            double d28 = d * d9;
            double d29 = (d27 * cos) - (d28 * d22);
            double d30 = (d27 * d22) + (d28 * cos);
            int ceil = (int) Math.ceil(Math.abs((atan22 * 4.0d) / 3.141592653589793d));
            double cos2 = Math.cos(d5);
            double sin2 = Math.sin(d5);
            double cos3 = Math.cos(atan2);
            double sin3 = Math.sin(atan2);
            Double.isNaN(d6);
            double d31 = -d6;
            double d32 = d31 * cos2;
            Double.isNaN(d9);
            double d33 = d9 * sin2;
            double d34 = (d32 * sin3) - (d33 * cos3);
            double d35 = d31 * sin2;
            Double.isNaN(d9);
            double d36 = d9 * cos2;
            double d37 = (sin3 * d35) + (cos3 * d36);
            double d38 = atan2;
            double d39 = (double) ceil;
            Double.isNaN(d39);
            double d40 = atan22 / d39;
            double d41 = d38;
            while (i < ceil) {
                double d42 = d41 + d40;
                double sin4 = Math.sin(d42);
                double cos4 = Math.cos(d42);
                Double.isNaN(d6);
                double d43 = d40;
                double d44 = (d29 + ((d6 * cos2) * cos4)) - (d33 * sin4);
                Double.isNaN(d6);
                int i2 = ceil;
                double d45 = d30 + (d6 * sin2 * cos4) + (d36 * sin4);
                double d46 = (d32 * sin4) - (d33 * cos4);
                double d47 = (sin4 * d35) + (cos4 * d36);
                double d48 = d42 - d41;
                double tan = Math.tan(d48 / 2.0d);
                double sin5 = (Math.sin(d48) * (Math.sqrt(((tan * 3.0d) * tan) + 4.0d) - 4.0d)) / 3.0d;
                path.rLineTo(0.0f, 0.0f);
                path.cubicTo((float) (d8 + (d34 * sin5)), (float) (d11 + (d37 * sin5)), (float) (d44 - (sin5 * d46)), (float) (d45 - (sin5 * d47)), (float) d44, (float) d45);
                i++;
                d29 = d29;
                d35 = d35;
                d8 = d44;
                d11 = d45;
                d41 = d42;
                d37 = d47;
                d34 = d46;
                d6 = d6;
                ceil = i2;
                d40 = d43;
            }
            return;
        }
        Log.w("PathParser", " Points are coincident");
    }
}
