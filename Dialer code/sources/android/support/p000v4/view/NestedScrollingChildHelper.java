package android.support.p000v4.view;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

/* renamed from: android.support.v4.view.NestedScrollingChildHelper */
public class NestedScrollingChildHelper {
    private boolean mIsNestedScrollingEnabled;
    private ViewParent mNestedScrollingParentNonTouch;
    private ViewParent mNestedScrollingParentTouch;
    private int[] mTempNestedScrollConsumed;
    private final View mView;

    public NestedScrollingChildHelper(View view) {
        this.mView = view;
    }

    private ViewParent getNestedScrollingParentForType(int i) {
        if (i == 0) {
            return this.mNestedScrollingParentTouch;
        }
        if (i != 1) {
            return null;
        }
        return this.mNestedScrollingParentNonTouch;
    }

    public boolean dispatchNestedFling(float f, float f2, boolean z) {
        ViewParent nestedScrollingParentForType;
        if (!this.mIsNestedScrollingEnabled || (nestedScrollingParentForType = getNestedScrollingParentForType(0)) == null) {
            return false;
        }
        View view = this.mView;
        int i = Build.VERSION.SDK_INT;
        try {
            return nestedScrollingParentForType.onNestedFling(view, f, f2, z);
        } catch (AbstractMethodError e) {
            Log.e("ViewParentCompat", "ViewParent " + nestedScrollingParentForType + " does not implement interface " + "method onNestedFling", e);
            return false;
        }
    }

    public boolean dispatchNestedPreFling(float f, float f2) {
        ViewParent nestedScrollingParentForType;
        if (!this.mIsNestedScrollingEnabled || (nestedScrollingParentForType = getNestedScrollingParentForType(0)) == null) {
            return false;
        }
        View view = this.mView;
        int i = Build.VERSION.SDK_INT;
        try {
            return nestedScrollingParentForType.onNestedPreFling(view, f, f2);
        } catch (AbstractMethodError e) {
            Log.e("ViewParentCompat", "ViewParent " + nestedScrollingParentForType + " does not implement interface " + "method onNestedPreFling", e);
            return false;
        }
    }

    public boolean dispatchNestedPreScroll(int i, int i2, int[] iArr, int[] iArr2, int i3) {
        ViewParent nestedScrollingParentForType;
        int i4;
        int i5;
        int[] iArr3;
        int i6 = i;
        int i7 = i2;
        int[] iArr4 = iArr2;
        int i8 = i3;
        if (!this.mIsNestedScrollingEnabled || (nestedScrollingParentForType = getNestedScrollingParentForType(i8)) == null) {
            return false;
        }
        if (i6 == 0 && i7 == 0) {
            if (iArr4 != null) {
                iArr4[0] = 0;
                iArr4[1] = 0;
            }
            return false;
        }
        if (iArr4 != null) {
            this.mView.getLocationInWindow(iArr4);
            i5 = iArr4[0];
            i4 = iArr4[1];
        } else {
            i5 = 0;
            i4 = 0;
        }
        if (iArr == null) {
            if (this.mTempNestedScrollConsumed == null) {
                this.mTempNestedScrollConsumed = new int[2];
            }
            iArr3 = this.mTempNestedScrollConsumed;
        } else {
            iArr3 = iArr;
        }
        iArr3[0] = 0;
        iArr3[1] = 0;
        View view = this.mView;
        if (nestedScrollingParentForType instanceof NestedScrollingParent2) {
            ((NestedScrollingParent2) nestedScrollingParentForType).onNestedPreScroll(view, i, i2, iArr3, i3);
        } else if (i8 == 0) {
            int i9 = Build.VERSION.SDK_INT;
            try {
                nestedScrollingParentForType.onNestedPreScroll(view, i, i7, iArr3);
            } catch (AbstractMethodError e) {
                Log.e("ViewParentCompat", "ViewParent " + nestedScrollingParentForType + " does not implement interface " + "method onNestedPreScroll", e);
            }
        }
        if (iArr4 != null) {
            this.mView.getLocationInWindow(iArr4);
            iArr4[0] = iArr4[0] - i5;
            iArr4[1] = iArr4[1] - i4;
        }
        if (iArr3[0] == 0 && iArr3[1] == 0) {
            return false;
        }
        return true;
    }

    public boolean dispatchNestedScroll(int i, int i2, int i3, int i4, int[] iArr, int i5) {
        ViewParent nestedScrollingParentForType;
        int i6;
        int i7;
        int[] iArr2 = iArr;
        int i8 = i5;
        if (!this.mIsNestedScrollingEnabled || (nestedScrollingParentForType = getNestedScrollingParentForType(i8)) == null) {
            return false;
        }
        if (i == 0 && i2 == 0 && i3 == 0 && i4 == 0) {
            if (iArr2 != null) {
                iArr2[0] = 0;
                iArr2[1] = 0;
            }
            return false;
        }
        if (iArr2 != null) {
            this.mView.getLocationInWindow(iArr2);
            i7 = iArr2[0];
            i6 = iArr2[1];
        } else {
            i7 = 0;
            i6 = 0;
        }
        View view = this.mView;
        if (nestedScrollingParentForType instanceof NestedScrollingParent2) {
            ((NestedScrollingParent2) nestedScrollingParentForType).onNestedScroll(view, i, i2, i3, i4, i5);
        } else if (i8 == 0) {
            int i9 = Build.VERSION.SDK_INT;
            try {
                nestedScrollingParentForType.onNestedScroll(view, i, i2, i3, i4);
            } catch (AbstractMethodError e) {
                Log.e("ViewParentCompat", "ViewParent " + nestedScrollingParentForType + " does not implement interface " + "method onNestedScroll", e);
            }
        }
        if (iArr2 != null) {
            this.mView.getLocationInWindow(iArr2);
            iArr2[0] = iArr2[0] - i7;
            iArr2[1] = iArr2[1] - i6;
        }
        return true;
    }

    public boolean hasNestedScrollingParent(int i) {
        return getNestedScrollingParentForType(i) != null;
    }

    public boolean isNestedScrollingEnabled() {
        return this.mIsNestedScrollingEnabled;
    }

    public void setNestedScrollingEnabled(boolean z) {
        if (this.mIsNestedScrollingEnabled) {
            ViewCompat.stopNestedScroll(this.mView);
        }
        this.mIsNestedScrollingEnabled = z;
    }

    public boolean startNestedScroll(int i, int i2) {
        boolean z;
        if (getNestedScrollingParentForType(i2) != null) {
            return true;
        }
        if (this.mIsNestedScrollingEnabled) {
            View view = this.mView;
            for (ViewParent parent = this.mView.getParent(); parent != null; parent = parent.getParent()) {
                View view2 = this.mView;
                boolean z2 = parent instanceof NestedScrollingParent2;
                if (z2) {
                    z = ((NestedScrollingParent2) parent).onStartNestedScroll(view, view2, i, i2);
                } else {
                    if (i2 == 0) {
                        int i3 = Build.VERSION.SDK_INT;
                        try {
                            z = parent.onStartNestedScroll(view, view2, i);
                        } catch (AbstractMethodError e) {
                            Log.e("ViewParentCompat", "ViewParent " + parent + " does not implement interface " + "method onStartNestedScroll", e);
                        }
                    }
                    z = false;
                }
                if (z) {
                    if (i2 == 0) {
                        this.mNestedScrollingParentTouch = parent;
                    } else if (i2 == 1) {
                        this.mNestedScrollingParentNonTouch = parent;
                    }
                    View view3 = this.mView;
                    if (z2) {
                        ((NestedScrollingParent2) parent).onNestedScrollAccepted(view, view3, i, i2);
                    } else if (i2 == 0) {
                        int i4 = Build.VERSION.SDK_INT;
                        try {
                            parent.onNestedScrollAccepted(view, view3, i);
                        } catch (AbstractMethodError e2) {
                            Log.e("ViewParentCompat", "ViewParent " + parent + " does not implement interface " + "method onNestedScrollAccepted", e2);
                        }
                    }
                    return true;
                }
                if (parent instanceof View) {
                    view = (View) parent;
                }
            }
        }
        return false;
    }

    public void stopNestedScroll(int i) {
        ViewParent nestedScrollingParentForType = getNestedScrollingParentForType(i);
        if (nestedScrollingParentForType != null) {
            View view = this.mView;
            if (nestedScrollingParentForType instanceof NestedScrollingParent2) {
                ((NestedScrollingParent2) nestedScrollingParentForType).onStopNestedScroll(view, i);
            } else if (i == 0) {
                int i2 = Build.VERSION.SDK_INT;
                try {
                    nestedScrollingParentForType.onStopNestedScroll(view);
                } catch (AbstractMethodError e) {
                    Log.e("ViewParentCompat", "ViewParent " + nestedScrollingParentForType + " does not implement interface " + "method onStopNestedScroll", e);
                }
            }
            if (i == 0) {
                this.mNestedScrollingParentTouch = null;
            } else if (i == 1) {
                this.mNestedScrollingParentNonTouch = null;
            }
        }
    }
}
