package androidx.core.widget;

import android.os.Build;
import android.view.View;
import android.widget.ListPopupWindow;

public final class ListPopupWindowCompat {
    private ListPopupWindowCompat() {
    }

    @Deprecated
    public static View.OnTouchListener createDragToOpenListener(Object obj, View view) {
        return createDragToOpenListener((ListPopupWindow) obj, view);
    }

    public static View.OnTouchListener createDragToOpenListener(ListPopupWindow listPopupWindow, View view) {
        int i = Build.VERSION.SDK_INT;
        return listPopupWindow.createDragToOpenListener(view);
    }
}
