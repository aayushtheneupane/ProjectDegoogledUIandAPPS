package android.support.p002v7.widget;

import android.view.View;

/* renamed from: android.support.v7.widget.ViewBoundsCheck */
class ViewBoundsCheck {
    BoundFlags mBoundFlags = new BoundFlags();
    final Callback mCallback;

    /* renamed from: android.support.v7.widget.ViewBoundsCheck$BoundFlags */
    static class BoundFlags {
        int mBoundFlags = 0;
        int mChildEnd;
        int mChildStart;
        int mRvEnd;
        int mRvStart;

        BoundFlags() {
        }

        /* access modifiers changed from: package-private */
        public boolean boundsMatch() {
            int i = this.mBoundFlags;
            if ((i & 7) != 0 && (i & (compare(this.mChildStart, this.mRvStart) << 0)) == 0) {
                return false;
            }
            int i2 = this.mBoundFlags;
            if ((i2 & 112) != 0 && (i2 & (compare(this.mChildStart, this.mRvEnd) << 4)) == 0) {
                return false;
            }
            int i3 = this.mBoundFlags;
            if ((i3 & 1792) != 0 && (i3 & (compare(this.mChildEnd, this.mRvStart) << 8)) == 0) {
                return false;
            }
            int i4 = this.mBoundFlags;
            if ((i4 & 28672) == 0 || ((compare(this.mChildEnd, this.mRvEnd) << 12) & i4) != 0) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public int compare(int i, int i2) {
            if (i > i2) {
                return 1;
            }
            return i == i2 ? 2 : 4;
        }
    }

    /* renamed from: android.support.v7.widget.ViewBoundsCheck$Callback */
    interface Callback {
        View getChildAt(int i);

        int getChildEnd(View view);

        int getChildStart(View view);

        int getParentEnd();

        int getParentStart();
    }

    ViewBoundsCheck(Callback callback) {
        this.mCallback = callback;
    }

    /* access modifiers changed from: package-private */
    public View findOneViewWithinBoundFlags(int i, int i2, int i3, int i4) {
        int parentStart = this.mCallback.getParentStart();
        int parentEnd = this.mCallback.getParentEnd();
        int i5 = i2 > i ? 1 : -1;
        View view = null;
        while (i != i2) {
            View childAt = this.mCallback.getChildAt(i);
            int childStart = this.mCallback.getChildStart(childAt);
            int childEnd = this.mCallback.getChildEnd(childAt);
            BoundFlags boundFlags = this.mBoundFlags;
            boundFlags.mRvStart = parentStart;
            boundFlags.mRvEnd = parentEnd;
            boundFlags.mChildStart = childStart;
            boundFlags.mChildEnd = childEnd;
            if (i3 != 0) {
                boundFlags.mBoundFlags = 0;
                boundFlags.mBoundFlags |= i3;
                if (boundFlags.boundsMatch()) {
                    return childAt;
                }
            }
            if (i4 != 0) {
                BoundFlags boundFlags2 = this.mBoundFlags;
                boundFlags2.mBoundFlags = 0;
                boundFlags2.mBoundFlags |= i4;
                if (boundFlags2.boundsMatch()) {
                    view = childAt;
                }
            }
            i += i5;
        }
        return view;
    }

    /* access modifiers changed from: package-private */
    public boolean isViewWithinBoundFlags(View view, int i) {
        BoundFlags boundFlags = this.mBoundFlags;
        int parentStart = this.mCallback.getParentStart();
        int parentEnd = this.mCallback.getParentEnd();
        int childStart = this.mCallback.getChildStart(view);
        int childEnd = this.mCallback.getChildEnd(view);
        boundFlags.mRvStart = parentStart;
        boundFlags.mRvEnd = parentEnd;
        boundFlags.mChildStart = childStart;
        boundFlags.mChildEnd = childEnd;
        if (i == 0) {
            return false;
        }
        BoundFlags boundFlags2 = this.mBoundFlags;
        boundFlags2.mBoundFlags = 0;
        boundFlags2.mBoundFlags |= i;
        return boundFlags2.boundsMatch();
    }
}
