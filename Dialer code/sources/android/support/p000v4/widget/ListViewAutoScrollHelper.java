package android.support.p000v4.widget;

import android.os.Build;
import android.widget.ListView;

/* renamed from: android.support.v4.widget.ListViewAutoScrollHelper */
public class ListViewAutoScrollHelper extends AutoScrollHelper {
    private final ListView mTarget;

    public ListViewAutoScrollHelper(ListView listView) {
        super(listView);
        this.mTarget = listView;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0038 A[RETURN] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canTargetScrollVertically(int r7) {
        /*
            r6 = this;
            android.widget.ListView r6 = r6.mTarget
            int r0 = r6.getCount()
            r1 = 0
            if (r0 != 0) goto L_0x000a
            return r1
        L_0x000a:
            int r2 = r6.getChildCount()
            int r3 = r6.getFirstVisiblePosition()
            int r4 = r3 + r2
            r5 = 1
            if (r7 <= 0) goto L_0x0029
            if (r4 < r0) goto L_0x0038
            int r2 = r2 - r5
            android.view.View r7 = r6.getChildAt(r2)
            int r7 = r7.getBottom()
            int r6 = r6.getHeight()
            if (r7 > r6) goto L_0x0038
            return r1
        L_0x0029:
            if (r7 >= 0) goto L_0x0039
            if (r3 > 0) goto L_0x0038
            android.view.View r6 = r6.getChildAt(r1)
            int r6 = r6.getTop()
            if (r6 < 0) goto L_0x0038
            return r1
        L_0x0038:
            return r5
        L_0x0039:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.widget.ListViewAutoScrollHelper.canTargetScrollVertically(int):boolean");
    }

    public void scrollTargetBy(int i, int i2) {
        ListView listView = this.mTarget;
        int i3 = Build.VERSION.SDK_INT;
        listView.scrollListBy(i2);
    }
}
