package androidx.core.view.animation;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.animation.Interpolator;
import com.android.contacts.ContactPhotoManager;

class PathInterpolatorApi14 implements Interpolator {

    /* renamed from: mX */
    private final float[] f4mX;

    /* renamed from: mY */
    private final float[] f5mY;

    PathInterpolatorApi14(Path path) {
        PathMeasure pathMeasure = new PathMeasure(path, false);
        float length = pathMeasure.getLength();
        int i = ((int) (length / 0.002f)) + 1;
        this.f4mX = new float[i];
        this.f5mY = new float[i];
        float[] fArr = new float[2];
        for (int i2 = 0; i2 < i; i2++) {
            pathMeasure.getPosTan((((float) i2) * length) / ((float) (i - 1)), fArr, (float[]) null);
            this.f4mX[i2] = fArr[0];
            this.f5mY[i2] = fArr[1];
        }
    }

    PathInterpolatorApi14(float f, float f2, float f3, float f4) {
        this(createCubic(f, f2, f3, f4));
    }

    public float getInterpolation(float f) {
        if (f <= ContactPhotoManager.OFFSET_DEFAULT) {
            return ContactPhotoManager.OFFSET_DEFAULT;
        }
        if (f >= 1.0f) {
            return 1.0f;
        }
        int i = 0;
        int length = this.f4mX.length - 1;
        while (length - i > 1) {
            int i2 = (i + length) / 2;
            if (f < this.f4mX[i2]) {
                length = i2;
            } else {
                i = i2;
            }
        }
        float[] fArr = this.f4mX;
        float f2 = fArr[length] - fArr[i];
        if (f2 == ContactPhotoManager.OFFSET_DEFAULT) {
            return this.f5mY[i];
        }
        float[] fArr2 = this.f5mY;
        float f3 = fArr2[i];
        return f3 + (((f - fArr[i]) / f2) * (fArr2[length] - f3));
    }

    private static Path createCubic(float f, float f2, float f3, float f4) {
        Path path = new Path();
        path.moveTo(ContactPhotoManager.OFFSET_DEFAULT, ContactPhotoManager.OFFSET_DEFAULT);
        path.cubicTo(f, f2, f3, f4, 1.0f, 1.0f);
        return path;
    }
}
