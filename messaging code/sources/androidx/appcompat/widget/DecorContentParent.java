package androidx.appcompat.widget;

import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.Menu;
import android.view.Window;
import androidx.appcompat.view.menu.C0211D;

public interface DecorContentParent {
    boolean canShowOverflowMenu();

    void dismissPopups();

    CharSequence getTitle();

    boolean hasIcon();

    boolean hasLogo();

    boolean hideOverflowMenu();

    void initFeature(int i);

    boolean isOverflowMenuShowPending();

    boolean isOverflowMenuShowing();

    void restoreToolbarHierarchyState(SparseArray sparseArray);

    void saveToolbarHierarchyState(SparseArray sparseArray);

    void setIcon(int i);

    void setIcon(Drawable drawable);

    void setLogo(int i);

    void setMenu(Menu menu, C0211D d);

    void setMenuPrepared();

    void setUiOptions(int i);

    void setWindowCallback(Window.Callback callback);

    void setWindowTitle(CharSequence charSequence);

    boolean showOverflowMenu();
}
