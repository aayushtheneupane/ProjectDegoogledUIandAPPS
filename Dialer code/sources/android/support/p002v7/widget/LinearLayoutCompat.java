package android.support.p002v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.p002v7.appcompat.R$styleable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/* renamed from: android.support.v7.widget.LinearLayoutCompat */
public class LinearLayoutCompat extends ViewGroup {
    private boolean mBaselineAligned;
    private int mBaselineAlignedChildIndex;
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    private int mGravity;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    private int mOrientation;
    private int mShowDividers;
    private int mTotalLength;
    private boolean mUseLargestChild;
    private float mWeightSum;

    public LinearLayoutCompat(Context context) {
        this(context, (AttributeSet) null, 0);
    }

    private void forceUniformHeight(int i, int i2) {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824);
        for (int i3 = 0; i3 < i; i3++) {
            View virtualChildAt = getVirtualChildAt(i3);
            if (virtualChildAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (layoutParams.height == -1) {
                    int i4 = layoutParams.width;
                    layoutParams.width = virtualChildAt.getMeasuredWidth();
                    measureChildWithMargins(virtualChildAt, i2, 0, makeMeasureSpec, 0);
                    layoutParams.width = i4;
                }
            }
        }
    }

    private void forceUniformWidth(int i, int i2) {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        for (int i3 = 0; i3 < i; i3++) {
            View virtualChildAt = getVirtualChildAt(i3);
            if (virtualChildAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (layoutParams.width == -1) {
                    int i4 = layoutParams.height;
                    layoutParams.height = virtualChildAt.getMeasuredHeight();
                    measureChildWithMargins(virtualChildAt, makeMeasureSpec, 0, i2, 0);
                    layoutParams.height = i4;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    /* access modifiers changed from: package-private */
    public void drawDividersHorizontal(Canvas canvas) {
        int i;
        int i2;
        int i3;
        int i4;
        int virtualChildCount = getVirtualChildCount();
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        for (int i5 = 0; i5 < virtualChildCount; i5++) {
            View virtualChildAt = getVirtualChildAt(i5);
            if (!(virtualChildAt == null || virtualChildAt.getVisibility() == 8 || !hasDividerBeforeChildAt(i5))) {
                LayoutParams layoutParams = (LayoutParams) virtualChildAt.getLayoutParams();
                if (isLayoutRtl) {
                    i4 = virtualChildAt.getRight() + layoutParams.rightMargin;
                } else {
                    i4 = (virtualChildAt.getLeft() - layoutParams.leftMargin) - this.mDividerWidth;
                }
                drawVerticalDivider(canvas, i4);
            }
        }
        if (hasDividerBeforeChildAt(virtualChildCount)) {
            View virtualChildAt2 = getVirtualChildAt(virtualChildCount - 1);
            if (virtualChildAt2 != null) {
                LayoutParams layoutParams2 = (LayoutParams) virtualChildAt2.getLayoutParams();
                if (isLayoutRtl) {
                    i3 = virtualChildAt2.getLeft() - layoutParams2.leftMargin;
                    i2 = this.mDividerWidth;
                } else {
                    i = virtualChildAt2.getRight() + layoutParams2.rightMargin;
                    drawVerticalDivider(canvas, i);
                }
            } else if (isLayoutRtl) {
                i = getPaddingLeft();
                drawVerticalDivider(canvas, i);
            } else {
                i3 = getWidth() - getPaddingRight();
                i2 = this.mDividerWidth;
            }
            i = i3 - i2;
            drawVerticalDivider(canvas, i);
        }
    }

    /* access modifiers changed from: package-private */
    public void drawDividersVertical(Canvas canvas) {
        int i;
        int virtualChildCount = getVirtualChildCount();
        for (int i2 = 0; i2 < virtualChildCount; i2++) {
            View virtualChildAt = getVirtualChildAt(i2);
            if (!(virtualChildAt == null || virtualChildAt.getVisibility() == 8 || !hasDividerBeforeChildAt(i2))) {
                drawHorizontalDivider(canvas, (virtualChildAt.getTop() - ((LayoutParams) virtualChildAt.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
        }
        if (hasDividerBeforeChildAt(virtualChildCount)) {
            View virtualChildAt2 = getVirtualChildAt(virtualChildCount - 1);
            if (virtualChildAt2 == null) {
                i = (getHeight() - getPaddingBottom()) - this.mDividerHeight;
            } else {
                i = virtualChildAt2.getBottom() + ((LayoutParams) virtualChildAt2.getLayoutParams()).bottomMargin;
            }
            drawHorizontalDivider(canvas, i);
        }
    }

    /* access modifiers changed from: package-private */
    public void drawHorizontalDivider(Canvas canvas, int i) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, i, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + i);
        this.mDivider.draw(canvas);
    }

    /* access modifiers changed from: package-private */
    public void drawVerticalDivider(Canvas canvas, int i) {
        this.mDivider.setBounds(i, getPaddingTop() + this.mDividerPadding, this.mDividerWidth + i, (getHeight() - getPaddingBottom()) - this.mDividerPadding);
        this.mDivider.draw(canvas);
    }

    public int getBaseline() {
        int i;
        if (this.mBaselineAlignedChildIndex < 0) {
            return super.getBaseline();
        }
        int childCount = getChildCount();
        int i2 = this.mBaselineAlignedChildIndex;
        if (childCount > i2) {
            View childAt = getChildAt(i2);
            int baseline = childAt.getBaseline();
            if (baseline != -1) {
                int i3 = this.mBaselineChildTop;
                if (this.mOrientation == 1 && (i = this.mGravity & 112) != 48) {
                    if (i == 16) {
                        i3 += ((((getBottom() - getTop()) - getPaddingTop()) - getPaddingBottom()) - this.mTotalLength) / 2;
                    } else if (i == 80) {
                        i3 = ((getBottom() - getTop()) - getPaddingBottom()) - this.mTotalLength;
                    }
                }
                return i3 + ((LayoutParams) childAt.getLayoutParams()).topMargin + baseline;
            } else if (this.mBaselineAlignedChildIndex == 0) {
                return -1;
            } else {
                throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout points to a View that doesn't know how to get its baseline.");
            }
        } else {
            throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.");
        }
    }

    /* access modifiers changed from: package-private */
    public int getChildrenSkipCount(View view, int i) {
        return 0;
    }

    public Drawable getDividerDrawable() {
        return this.mDivider;
    }

    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    public int getGravity() {
        return this.mGravity;
    }

    /* access modifiers changed from: package-private */
    public int getLocationOffset(View view) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int getNextLocationOffset(View view) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public View getVirtualChildAt(int i) {
        return getChildAt(i);
    }

    /* access modifiers changed from: package-private */
    public int getVirtualChildCount() {
        return getChildCount();
    }

    /* access modifiers changed from: protected */
    public boolean hasDividerBeforeChildAt(int i) {
        if (i == 0) {
            return (this.mShowDividers & 1) != 0;
        }
        if (i == getChildCount()) {
            if ((this.mShowDividers & 4) != 0) {
                return true;
            }
            return false;
        } else if ((this.mShowDividers & 2) == 0) {
            return false;
        } else {
            for (int i2 = i - 1; i2 >= 0; i2--) {
                if (getChildAt(i2).getVisibility() != 8) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ba  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e9  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00fc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void layoutHorizontal(int r23, int r24, int r25, int r26) {
        /*
            r22 = this;
            r0 = r22
            boolean r1 = android.support.p002v7.widget.ViewUtils.isLayoutRtl(r22)
            int r2 = r22.getPaddingTop()
            int r3 = r26 - r24
            int r4 = r22.getPaddingBottom()
            int r4 = r3 - r4
            int r3 = r3 - r2
            int r5 = r22.getPaddingBottom()
            int r3 = r3 - r5
            int r5 = r22.getVirtualChildCount()
            int r6 = r0.mGravity
            r7 = 8388615(0x800007, float:1.1754953E-38)
            r7 = r7 & r6
            r6 = r6 & 112(0x70, float:1.57E-43)
            boolean r8 = r0.mBaselineAligned
            int[] r9 = r0.mMaxAscent
            int[] r10 = r0.mMaxDescent
            int r11 = android.support.p000v4.view.ViewCompat.getLayoutDirection(r22)
            int r7 = android.support.design.R$dimen.getAbsoluteGravity(r7, r11)
            r11 = 1
            r12 = 2
            if (r7 == r11) goto L_0x004a
            r13 = 5
            if (r7 == r13) goto L_0x003e
            int r7 = r22.getPaddingLeft()
            goto L_0x0055
        L_0x003e:
            int r7 = r22.getPaddingLeft()
            int r7 = r7 + r25
            int r7 = r7 - r23
            int r13 = r0.mTotalLength
            int r7 = r7 - r13
            goto L_0x0055
        L_0x004a:
            int r7 = r22.getPaddingLeft()
            int r13 = r25 - r23
            int r14 = r0.mTotalLength
            int r13 = r13 - r14
            int r13 = r13 / r12
            int r7 = r7 + r13
        L_0x0055:
            r13 = 0
            if (r1 == 0) goto L_0x005c
            int r1 = r5 + -1
            r15 = -1
            goto L_0x005e
        L_0x005c:
            r15 = r11
            r1 = r13
        L_0x005e:
            if (r13 >= r5) goto L_0x012c
            int r16 = r15 * r13
            int r11 = r16 + r1
            android.view.View r12 = r0.getVirtualChildAt(r11)
            if (r12 != 0) goto L_0x007b
            int r11 = r0.measureNullChild(r11)
            int r7 = r7 + r11
            r25 = r1
        L_0x0071:
            r17 = r5
            r16 = r6
            r18 = r8
            r19 = r15
            goto L_0x011c
        L_0x007b:
            int r14 = r12.getVisibility()
            r25 = r1
            r1 = 8
            if (r14 == r1) goto L_0x0071
            int r1 = r12.getMeasuredWidth()
            int r14 = r12.getMeasuredHeight()
            android.view.ViewGroup$LayoutParams r16 = r12.getLayoutParams()
            r17 = r5
            r5 = r16
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r5 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r5
            if (r8 == 0) goto L_0x00a7
            r16 = r6
            int r6 = r5.height
            r18 = r8
            r8 = -1
            if (r6 == r8) goto L_0x00ab
            int r6 = r12.getBaseline()
            goto L_0x00ac
        L_0x00a7:
            r16 = r6
            r18 = r8
        L_0x00ab:
            r6 = -1
        L_0x00ac:
            int r8 = r5.gravity
            if (r8 >= 0) goto L_0x00b2
            r8 = r16
        L_0x00b2:
            r8 = r8 & 112(0x70, float:1.57E-43)
            r19 = r15
            r15 = 16
            if (r8 == r15) goto L_0x00e9
            r15 = 48
            if (r8 == r15) goto L_0x00da
            r15 = 80
            if (r8 == r15) goto L_0x00c4
            r8 = r2
            goto L_0x00d8
        L_0x00c4:
            int r8 = r4 - r14
            int r15 = r5.bottomMargin
            int r8 = r8 - r15
            r15 = -1
            if (r6 == r15) goto L_0x00d8
            int r15 = r12.getMeasuredHeight()
            int r15 = r15 - r6
            r6 = 2
            r20 = r10[r6]
            int r20 = r20 - r15
            int r8 = r8 - r20
        L_0x00d8:
            r15 = -1
            goto L_0x00f6
        L_0x00da:
            int r8 = r5.topMargin
            int r8 = r8 + r2
            r15 = -1
            if (r6 == r15) goto L_0x00f6
            r20 = 1
            r21 = r9[r20]
            int r21 = r21 - r6
            int r8 = r21 + r8
            goto L_0x00f6
        L_0x00e9:
            r15 = -1
            int r6 = r3 - r14
            r8 = 2
            int r6 = r6 / r8
            int r6 = r6 + r2
            int r8 = r5.topMargin
            int r6 = r6 + r8
            int r8 = r5.bottomMargin
            int r8 = r6 - r8
        L_0x00f6:
            boolean r6 = r0.hasDividerBeforeChildAt(r11)
            if (r6 == 0) goto L_0x00ff
            int r6 = r0.mDividerWidth
            int r7 = r7 + r6
        L_0x00ff:
            int r6 = r5.leftMargin
            int r6 = r6 + r7
            int r7 = r0.getLocationOffset(r12)
            int r7 = r7 + r6
            int r15 = r1 + r7
            int r14 = r14 + r8
            r12.layout(r7, r8, r15, r14)
            int r5 = r5.rightMargin
            int r1 = r1 + r5
            int r5 = r0.getNextLocationOffset(r12)
            int r1 = r1 + r5
            int r1 = r1 + r6
            int r5 = r0.getChildrenSkipCount(r12, r11)
            int r13 = r13 + r5
            r7 = r1
        L_0x011c:
            r1 = 1
            int r13 = r13 + r1
            r11 = r1
            r6 = r16
            r5 = r17
            r8 = r18
            r15 = r19
            r12 = 2
            r1 = r25
            goto L_0x005e
        L_0x012c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p002v7.widget.LinearLayoutCompat.layoutHorizontal(int, int, int, int):void");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0095  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void layoutVertical(int r12, int r13, int r14, int r15) {
        /*
            r11 = this;
            int r0 = r11.getPaddingLeft()
            int r14 = r14 - r12
            int r12 = r11.getPaddingRight()
            int r12 = r14 - r12
            int r14 = r14 - r0
            int r1 = r11.getPaddingRight()
            int r14 = r14 - r1
            int r1 = r11.getVirtualChildCount()
            int r2 = r11.mGravity
            r3 = r2 & 112(0x70, float:1.57E-43)
            r4 = 8388615(0x800007, float:1.1754953E-38)
            r2 = r2 & r4
            r4 = 16
            if (r3 == r4) goto L_0x0035
            r4 = 80
            if (r3 == r4) goto L_0x002a
            int r13 = r11.getPaddingTop()
            goto L_0x0041
        L_0x002a:
            int r3 = r11.getPaddingTop()
            int r3 = r3 + r15
            int r3 = r3 - r13
            int r13 = r11.mTotalLength
            int r13 = r3 - r13
            goto L_0x0041
        L_0x0035:
            int r3 = r11.getPaddingTop()
            int r15 = r15 - r13
            int r13 = r11.mTotalLength
            int r15 = r15 - r13
            int r15 = r15 / 2
            int r13 = r3 + r15
        L_0x0041:
            r15 = 0
        L_0x0042:
            if (r15 >= r1) goto L_0x00b7
            android.view.View r3 = r11.getVirtualChildAt(r15)
            r4 = 1
            if (r3 != 0) goto L_0x0051
            int r3 = r11.measureNullChild(r15)
            int r13 = r13 + r3
            goto L_0x00b5
        L_0x0051:
            int r5 = r3.getVisibility()
            r6 = 8
            if (r5 == r6) goto L_0x00b5
            int r5 = r3.getMeasuredWidth()
            int r6 = r3.getMeasuredHeight()
            android.view.ViewGroup$LayoutParams r7 = r3.getLayoutParams()
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r7 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r7
            int r8 = r7.gravity
            if (r8 >= 0) goto L_0x006c
            r8 = r2
        L_0x006c:
            int r9 = android.support.p000v4.view.ViewCompat.getLayoutDirection(r11)
            int r8 = android.support.design.R$dimen.getAbsoluteGravity(r8, r9)
            r8 = r8 & 7
            if (r8 == r4) goto L_0x0084
            r9 = 5
            if (r8 == r9) goto L_0x007f
            int r8 = r7.leftMargin
            int r8 = r8 + r0
            goto L_0x008f
        L_0x007f:
            int r8 = r12 - r5
            int r9 = r7.rightMargin
            goto L_0x008e
        L_0x0084:
            int r8 = r14 - r5
            int r8 = r8 / 2
            int r8 = r8 + r0
            int r9 = r7.leftMargin
            int r8 = r8 + r9
            int r9 = r7.rightMargin
        L_0x008e:
            int r8 = r8 - r9
        L_0x008f:
            boolean r9 = r11.hasDividerBeforeChildAt(r15)
            if (r9 == 0) goto L_0x0098
            int r9 = r11.mDividerHeight
            int r13 = r13 + r9
        L_0x0098:
            int r9 = r7.topMargin
            int r13 = r13 + r9
            int r9 = r11.getLocationOffset(r3)
            int r9 = r9 + r13
            int r5 = r5 + r8
            int r10 = r6 + r9
            r3.layout(r8, r9, r5, r10)
            int r5 = r7.bottomMargin
            int r6 = r6 + r5
            int r5 = r11.getNextLocationOffset(r3)
            int r6 = r6 + r5
            int r6 = r6 + r13
            int r13 = r11.getChildrenSkipCount(r3, r15)
            int r15 = r15 + r13
            r13 = r6
        L_0x00b5:
            int r15 = r15 + r4
            goto L_0x0042
        L_0x00b7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p002v7.widget.LinearLayoutCompat.layoutVertical(int, int, int, int):void");
    }

    /* access modifiers changed from: package-private */
    public void measureChildBeforeLayout(View view, int i, int i2, int i3, int i4, int i5) {
        measureChildWithMargins(view, i2, i3, i4, i5);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x0454  */
    /* JADX WARNING: Removed duplicated region for block: B:188:0x0476  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0171  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0176  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0198  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01c6  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x01cd  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01d8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void measureHorizontal(int r39, int r40) {
        /*
            r38 = this;
            r7 = r38
            r8 = r39
            r9 = r40
            r10 = 0
            r7.mTotalLength = r10
            int r11 = r38.getVirtualChildCount()
            int r12 = android.view.View.MeasureSpec.getMode(r39)
            int r13 = android.view.View.MeasureSpec.getMode(r40)
            int[] r0 = r7.mMaxAscent
            r14 = 4
            if (r0 == 0) goto L_0x001e
            int[] r0 = r7.mMaxDescent
            if (r0 != 0) goto L_0x0026
        L_0x001e:
            int[] r0 = new int[r14]
            r7.mMaxAscent = r0
            int[] r0 = new int[r14]
            r7.mMaxDescent = r0
        L_0x0026:
            int[] r15 = r7.mMaxAscent
            int[] r6 = r7.mMaxDescent
            r16 = 3
            r5 = -1
            r15[r16] = r5
            r17 = 2
            r15[r17] = r5
            r18 = 1
            r15[r18] = r5
            r15[r10] = r5
            r6[r16] = r5
            r6[r17] = r5
            r6[r18] = r5
            r6[r10] = r5
            boolean r4 = r7.mBaselineAligned
            boolean r3 = r7.mUseLargestChild
            r2 = 1073741824(0x40000000, float:2.0)
            if (r12 != r2) goto L_0x004c
            r19 = r18
            goto L_0x004e
        L_0x004c:
            r19 = r10
        L_0x004e:
            r20 = 0
            r1 = r10
            r14 = r1
            r21 = r14
            r22 = r21
            r23 = r22
            r24 = r23
            r26 = r24
            r28 = r26
            r27 = r18
            r0 = r20
        L_0x0062:
            r29 = r6
            r5 = 8
            if (r1 >= r11) goto L_0x0205
            android.view.View r6 = r7.getVirtualChildAt(r1)
            if (r6 != 0) goto L_0x007d
            int r5 = r7.mTotalLength
            int r6 = r7.measureNullChild(r1)
            int r5 = r5 + r6
            r7.mTotalLength = r5
        L_0x0077:
            r33 = r3
            r37 = r4
            goto L_0x01f5
        L_0x007d:
            int r10 = r6.getVisibility()
            if (r10 != r5) goto L_0x0089
            int r5 = r7.getChildrenSkipCount(r6, r1)
            int r1 = r1 + r5
            goto L_0x0077
        L_0x0089:
            boolean r5 = r7.hasDividerBeforeChildAt(r1)
            if (r5 == 0) goto L_0x0096
            int r5 = r7.mTotalLength
            int r10 = r7.mDividerWidth
            int r5 = r5 + r10
            r7.mTotalLength = r5
        L_0x0096:
            android.view.ViewGroup$LayoutParams r5 = r6.getLayoutParams()
            r10 = r5
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r10 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r10
            float r5 = r10.weight
            float r32 = r0 + r5
            if (r12 != r2) goto L_0x00ec
            int r0 = r10.width
            if (r0 != 0) goto L_0x00ec
            float r0 = r10.weight
            int r0 = (r0 > r20 ? 1 : (r0 == r20 ? 0 : -1))
            if (r0 <= 0) goto L_0x00ec
            if (r19 == 0) goto L_0x00ba
            int r0 = r7.mTotalLength
            int r5 = r10.leftMargin
            int r2 = r10.rightMargin
            int r5 = r5 + r2
            int r5 = r5 + r0
            r7.mTotalLength = r5
            goto L_0x00c8
        L_0x00ba:
            int r0 = r7.mTotalLength
            int r2 = r10.leftMargin
            int r2 = r2 + r0
            int r5 = r10.rightMargin
            int r2 = r2 + r5
            int r0 = java.lang.Math.max(r0, r2)
            r7.mTotalLength = r0
        L_0x00c8:
            if (r4 == 0) goto L_0x00dd
            r0 = 0
            int r2 = android.view.View.MeasureSpec.makeMeasureSpec(r0, r0)
            r6.measure(r2, r2)
            r35 = r1
            r33 = r3
            r37 = r4
            r3 = r6
            r31 = -2
            goto L_0x0168
        L_0x00dd:
            r35 = r1
            r33 = r3
            r37 = r4
            r3 = r6
            r24 = r18
            r1 = 1073741824(0x40000000, float:2.0)
            r31 = -2
            goto L_0x016a
        L_0x00ec:
            int r0 = r10.width
            if (r0 != 0) goto L_0x00fb
            float r0 = r10.weight
            int r0 = (r0 > r20 ? 1 : (r0 == r20 ? 0 : -1))
            if (r0 <= 0) goto L_0x00fb
            r5 = -2
            r10.width = r5
            r2 = 0
            goto L_0x00fe
        L_0x00fb:
            r5 = -2
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
        L_0x00fe:
            int r0 = (r32 > r20 ? 1 : (r32 == r20 ? 0 : -1))
            if (r0 != 0) goto L_0x0107
            int r0 = r7.mTotalLength
            r30 = r0
            goto L_0x0109
        L_0x0107:
            r30 = 0
        L_0x0109:
            r34 = 0
            r0 = r38
            r35 = r1
            r1 = r6
            r36 = r2
            r2 = r35
            r33 = r3
            r3 = r39
            r37 = r4
            r4 = r30
            r30 = r5
            r9 = -1
            r5 = r40
            r31 = r30
            r9 = -2147483648(0xffffffff80000000, float:-0.0)
            r30 = r6
            r6 = r34
            r0.measureChildBeforeLayout(r1, r2, r3, r4, r5, r6)
            r0 = r36
            if (r0 == r9) goto L_0x0132
            r10.width = r0
        L_0x0132:
            int r0 = r30.getMeasuredWidth()
            if (r19 == 0) goto L_0x014b
            int r1 = r7.mTotalLength
            int r2 = r10.leftMargin
            int r2 = r2 + r0
            int r3 = r10.rightMargin
            int r2 = r2 + r3
            r3 = r30
            int r4 = r7.getNextLocationOffset(r3)
            int r2 = r2 + r4
            int r2 = r2 + r1
            r7.mTotalLength = r2
            goto L_0x0162
        L_0x014b:
            r3 = r30
            int r1 = r7.mTotalLength
            int r2 = r1 + r0
            int r4 = r10.leftMargin
            int r2 = r2 + r4
            int r4 = r10.rightMargin
            int r2 = r2 + r4
            int r4 = r7.getNextLocationOffset(r3)
            int r2 = r2 + r4
            int r1 = java.lang.Math.max(r1, r2)
            r7.mTotalLength = r1
        L_0x0162:
            if (r33 == 0) goto L_0x0168
            int r14 = java.lang.Math.max(r0, r14)
        L_0x0168:
            r1 = 1073741824(0x40000000, float:2.0)
        L_0x016a:
            if (r13 == r1) goto L_0x0176
            int r0 = r10.height
            r2 = -1
            if (r0 != r2) goto L_0x0176
            r0 = r18
            r28 = r0
            goto L_0x0177
        L_0x0176:
            r0 = 0
        L_0x0177:
            int r2 = r10.topMargin
            int r4 = r10.bottomMargin
            int r2 = r2 + r4
            int r4 = r3.getMeasuredHeight()
            int r4 = r4 + r2
            int r5 = r3.getMeasuredState()
            r6 = r26
            int r5 = android.view.View.combineMeasuredStates(r6, r5)
            if (r37 == 0) goto L_0x01b6
            int r6 = r3.getBaseline()
            r9 = -1
            if (r6 == r9) goto L_0x01b6
            int r9 = r10.gravity
            if (r9 >= 0) goto L_0x019a
            int r9 = r7.mGravity
        L_0x019a:
            r9 = r9 & 112(0x70, float:1.57E-43)
            r25 = 4
            int r9 = r9 >> 4
            r9 = r9 & -2
            int r9 = r9 >> 1
            r1 = r15[r9]
            int r1 = java.lang.Math.max(r1, r6)
            r15[r9] = r1
            r1 = r29[r9]
            int r6 = r4 - r6
            int r1 = java.lang.Math.max(r1, r6)
            r29[r9] = r1
        L_0x01b6:
            r1 = r21
            int r1 = java.lang.Math.max(r1, r4)
            if (r27 == 0) goto L_0x01c6
            int r6 = r10.height
            r9 = -1
            if (r6 != r9) goto L_0x01c6
            r6 = r18
            goto L_0x01c7
        L_0x01c6:
            r6 = 0
        L_0x01c7:
            float r9 = r10.weight
            int r9 = (r9 > r20 ? 1 : (r9 == r20 ? 0 : -1))
            if (r9 <= 0) goto L_0x01d8
            if (r0 == 0) goto L_0x01d0
            goto L_0x01d1
        L_0x01d0:
            r2 = r4
        L_0x01d1:
            r10 = r23
            int r23 = java.lang.Math.max(r10, r2)
            goto L_0x01e5
        L_0x01d8:
            r10 = r23
            if (r0 == 0) goto L_0x01dd
            r4 = r2
        L_0x01dd:
            r2 = r22
            int r22 = java.lang.Math.max(r2, r4)
            r23 = r10
        L_0x01e5:
            r10 = r35
            int r0 = r7.getChildrenSkipCount(r3, r10)
            int r0 = r0 + r10
            r21 = r1
            r26 = r5
            r27 = r6
            r1 = r0
            r0 = r32
        L_0x01f5:
            int r1 = r1 + 1
            r9 = r40
            r6 = r29
            r3 = r33
            r4 = r37
            r2 = 1073741824(0x40000000, float:2.0)
            r5 = -1
            r10 = 0
            goto L_0x0062
        L_0x0205:
            r33 = r3
            r37 = r4
            r1 = r21
            r2 = r22
            r10 = r23
            r6 = r26
            r9 = -2147483648(0xffffffff80000000, float:-0.0)
            r31 = -2
            int r3 = r7.mTotalLength
            if (r3 <= 0) goto L_0x0226
            boolean r3 = r7.hasDividerBeforeChildAt(r11)
            if (r3 == 0) goto L_0x0226
            int r3 = r7.mTotalLength
            int r4 = r7.mDividerWidth
            int r3 = r3 + r4
            r7.mTotalLength = r3
        L_0x0226:
            r3 = r15[r18]
            r4 = -1
            if (r3 != r4) goto L_0x023c
            r3 = 0
            r5 = r15[r3]
            if (r5 != r4) goto L_0x023c
            r3 = r15[r17]
            if (r3 != r4) goto L_0x023c
            r3 = r15[r16]
            if (r3 == r4) goto L_0x0239
            goto L_0x023c
        L_0x0239:
            r23 = r6
            goto L_0x026d
        L_0x023c:
            r3 = r15[r16]
            r4 = 0
            r5 = r15[r4]
            r9 = r15[r18]
            r4 = r15[r17]
            int r4 = java.lang.Math.max(r9, r4)
            int r4 = java.lang.Math.max(r5, r4)
            int r3 = java.lang.Math.max(r3, r4)
            r4 = r29[r16]
            r5 = 0
            r9 = r29[r5]
            r5 = r29[r18]
            r23 = r6
            r6 = r29[r17]
            int r5 = java.lang.Math.max(r5, r6)
            int r5 = java.lang.Math.max(r9, r5)
            int r4 = java.lang.Math.max(r4, r5)
            int r4 = r4 + r3
            int r1 = java.lang.Math.max(r1, r4)
        L_0x026d:
            if (r33 == 0) goto L_0x02d0
            r3 = -2147483648(0xffffffff80000000, float:-0.0)
            if (r12 == r3) goto L_0x0275
            if (r12 != 0) goto L_0x02d0
        L_0x0275:
            r3 = 0
            r7.mTotalLength = r3
            r3 = 0
        L_0x0279:
            if (r3 >= r11) goto L_0x02d0
            android.view.View r4 = r7.getVirtualChildAt(r3)
            if (r4 != 0) goto L_0x028b
            int r4 = r7.mTotalLength
            int r5 = r7.measureNullChild(r3)
            int r4 = r4 + r5
            r7.mTotalLength = r4
            goto L_0x0298
        L_0x028b:
            int r5 = r4.getVisibility()
            r6 = 8
            if (r5 != r6) goto L_0x029b
            int r4 = r7.getChildrenSkipCount(r4, r3)
            int r3 = r3 + r4
        L_0x0298:
            r22 = r1
            goto L_0x02cb
        L_0x029b:
            android.view.ViewGroup$LayoutParams r5 = r4.getLayoutParams()
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r5 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r5
            if (r19 == 0) goto L_0x02b4
            int r6 = r7.mTotalLength
            int r9 = r5.leftMargin
            int r9 = r9 + r14
            int r5 = r5.rightMargin
            int r9 = r9 + r5
            int r4 = r7.getNextLocationOffset(r4)
            int r9 = r9 + r4
            int r9 = r9 + r6
            r7.mTotalLength = r9
            goto L_0x0298
        L_0x02b4:
            int r6 = r7.mTotalLength
            int r9 = r6 + r14
            r22 = r1
            int r1 = r5.leftMargin
            int r9 = r9 + r1
            int r1 = r5.rightMargin
            int r9 = r9 + r1
            int r1 = r7.getNextLocationOffset(r4)
            int r9 = r9 + r1
            int r1 = java.lang.Math.max(r6, r9)
            r7.mTotalLength = r1
        L_0x02cb:
            int r3 = r3 + 1
            r1 = r22
            goto L_0x0279
        L_0x02d0:
            r22 = r1
            int r1 = r7.mTotalLength
            int r3 = r38.getPaddingLeft()
            int r4 = r38.getPaddingRight()
            int r4 = r4 + r3
            int r4 = r4 + r1
            r7.mTotalLength = r4
            int r1 = r7.mTotalLength
            int r3 = r38.getSuggestedMinimumWidth()
            int r1 = java.lang.Math.max(r1, r3)
            r3 = 0
            int r1 = android.view.View.resolveSizeAndState(r1, r8, r3)
            r3 = 16777215(0xffffff, float:2.3509886E-38)
            r3 = r3 & r1
            int r4 = r7.mTotalLength
            int r3 = r3 - r4
            if (r24 != 0) goto L_0x0343
            if (r3 == 0) goto L_0x02ff
            int r5 = (r0 > r20 ? 1 : (r0 == r20 ? 0 : -1))
            if (r5 <= 0) goto L_0x02ff
            goto L_0x0343
        L_0x02ff:
            int r0 = java.lang.Math.max(r2, r10)
            if (r33 == 0) goto L_0x033b
            r2 = 1073741824(0x40000000, float:2.0)
            if (r12 == r2) goto L_0x033b
            r2 = 0
        L_0x030a:
            if (r2 >= r11) goto L_0x033b
            android.view.View r3 = r7.getVirtualChildAt(r2)
            if (r3 == 0) goto L_0x0338
            int r5 = r3.getVisibility()
            r6 = 8
            if (r5 != r6) goto L_0x031b
            goto L_0x0338
        L_0x031b:
            android.view.ViewGroup$LayoutParams r5 = r3.getLayoutParams()
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r5 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r5
            float r5 = r5.weight
            int r5 = (r5 > r20 ? 1 : (r5 == r20 ? 0 : -1))
            if (r5 <= 0) goto L_0x0338
            r5 = 1073741824(0x40000000, float:2.0)
            int r6 = android.view.View.MeasureSpec.makeMeasureSpec(r14, r5)
            int r9 = r3.getMeasuredHeight()
            int r9 = android.view.View.MeasureSpec.makeMeasureSpec(r9, r5)
            r3.measure(r6, r9)
        L_0x0338:
            int r2 = r2 + 1
            goto L_0x030a
        L_0x033b:
            r3 = r40
            r26 = r11
            r2 = r22
            goto L_0x04e8
        L_0x0343:
            float r5 = r7.mWeightSum
            int r6 = (r5 > r20 ? 1 : (r5 == r20 ? 0 : -1))
            if (r6 <= 0) goto L_0x034a
            r0 = r5
        L_0x034a:
            r5 = -1
            r15[r16] = r5
            r15[r17] = r5
            r15[r18] = r5
            r6 = 0
            r15[r6] = r5
            r29[r16] = r5
            r29[r17] = r5
            r29[r18] = r5
            r29[r6] = r5
            r7.mTotalLength = r6
            r10 = r2
            r6 = r5
            r9 = r23
            r2 = r0
            r0 = 0
        L_0x0364:
            if (r0 >= r11) goto L_0x048f
            android.view.View r14 = r7.getVirtualChildAt(r0)
            if (r14 == 0) goto L_0x047e
            int r5 = r14.getVisibility()
            r4 = 8
            if (r5 != r4) goto L_0x0376
            goto L_0x047e
        L_0x0376:
            android.view.ViewGroup$LayoutParams r5 = r14.getLayoutParams()
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r5 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r5
            float r4 = r5.weight
            int r23 = (r4 > r20 ? 1 : (r4 == r20 ? 0 : -1))
            if (r23 <= 0) goto L_0x03e0
            float r8 = (float) r3
            float r8 = r8 * r4
            float r8 = r8 / r2
            int r8 = (int) r8
            float r2 = r2 - r4
            int r3 = r3 - r8
            int r4 = r38.getPaddingTop()
            int r23 = r38.getPaddingBottom()
            int r23 = r23 + r4
            int r4 = r5.topMargin
            int r23 = r23 + r4
            int r4 = r5.bottomMargin
            int r4 = r23 + r4
            r23 = r2
            int r2 = r5.height
            r24 = r3
            r26 = r11
            r11 = -1
            r3 = r40
            int r2 = android.view.ViewGroup.getChildMeasureSpec(r3, r4, r2)
            int r4 = r5.width
            if (r4 != 0) goto L_0x03be
            r4 = 1073741824(0x40000000, float:2.0)
            if (r12 == r4) goto L_0x03b2
            goto L_0x03c0
        L_0x03b2:
            if (r8 <= 0) goto L_0x03b5
            goto L_0x03b6
        L_0x03b5:
            r8 = 0
        L_0x03b6:
            int r8 = android.view.View.MeasureSpec.makeMeasureSpec(r8, r4)
            r14.measure(r8, r2)
            goto L_0x03d0
        L_0x03be:
            r4 = 1073741824(0x40000000, float:2.0)
        L_0x03c0:
            int r30 = r14.getMeasuredWidth()
            int r8 = r30 + r8
            if (r8 >= 0) goto L_0x03c9
            r8 = 0
        L_0x03c9:
            int r8 = android.view.View.MeasureSpec.makeMeasureSpec(r8, r4)
            r14.measure(r8, r2)
        L_0x03d0:
            int r2 = r14.getMeasuredState()
            r4 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r2 = r2 & r4
            int r9 = android.view.View.combineMeasuredStates(r9, r2)
            r2 = r23
            r4 = r24
            goto L_0x03e6
        L_0x03e0:
            r4 = r3
            r26 = r11
            r11 = -1
            r3 = r40
        L_0x03e6:
            if (r19 == 0) goto L_0x0403
            int r8 = r7.mTotalLength
            int r23 = r14.getMeasuredWidth()
            int r11 = r5.leftMargin
            int r23 = r23 + r11
            int r11 = r5.rightMargin
            int r23 = r23 + r11
            int r11 = r7.getNextLocationOffset(r14)
            int r23 = r23 + r11
            int r8 = r23 + r8
            r7.mTotalLength = r8
            r23 = r2
            goto L_0x041d
        L_0x0403:
            int r8 = r7.mTotalLength
            int r11 = r14.getMeasuredWidth()
            int r11 = r11 + r8
            r23 = r2
            int r2 = r5.leftMargin
            int r11 = r11 + r2
            int r2 = r5.rightMargin
            int r11 = r11 + r2
            int r2 = r7.getNextLocationOffset(r14)
            int r11 = r11 + r2
            int r2 = java.lang.Math.max(r8, r11)
            r7.mTotalLength = r2
        L_0x041d:
            r2 = 1073741824(0x40000000, float:2.0)
            if (r13 == r2) goto L_0x0429
            int r2 = r5.height
            r8 = -1
            if (r2 != r8) goto L_0x0429
            r2 = r18
            goto L_0x042a
        L_0x0429:
            r2 = 0
        L_0x042a:
            int r8 = r5.topMargin
            int r11 = r5.bottomMargin
            int r8 = r8 + r11
            int r11 = r14.getMeasuredHeight()
            int r11 = r11 + r8
            int r6 = java.lang.Math.max(r6, r11)
            if (r2 == 0) goto L_0x043b
            goto L_0x043c
        L_0x043b:
            r8 = r11
        L_0x043c:
            int r2 = java.lang.Math.max(r10, r8)
            if (r27 == 0) goto L_0x044a
            int r8 = r5.height
            r10 = -1
            if (r8 != r10) goto L_0x044b
            r8 = r18
            goto L_0x044c
        L_0x044a:
            r10 = -1
        L_0x044b:
            r8 = 0
        L_0x044c:
            if (r37 == 0) goto L_0x0476
            int r14 = r14.getBaseline()
            if (r14 == r10) goto L_0x0476
            int r5 = r5.gravity
            if (r5 >= 0) goto L_0x045a
            int r5 = r7.mGravity
        L_0x045a:
            r5 = r5 & 112(0x70, float:1.57E-43)
            r24 = 4
            int r5 = r5 >> 4
            r5 = r5 & -2
            int r5 = r5 >> 1
            r10 = r15[r5]
            int r10 = java.lang.Math.max(r10, r14)
            r15[r5] = r10
            r10 = r29[r5]
            int r11 = r11 - r14
            int r10 = java.lang.Math.max(r10, r11)
            r29[r5] = r10
            goto L_0x0478
        L_0x0476:
            r24 = 4
        L_0x0478:
            r10 = r2
            r27 = r8
            r2 = r23
            goto L_0x0485
        L_0x047e:
            r4 = r3
            r26 = r11
            r24 = 4
            r3 = r40
        L_0x0485:
            int r0 = r0 + 1
            r8 = r39
            r3 = r4
            r11 = r26
            r5 = -1
            goto L_0x0364
        L_0x048f:
            r3 = r40
            r26 = r11
            int r0 = r7.mTotalLength
            int r2 = r38.getPaddingLeft()
            int r4 = r38.getPaddingRight()
            int r4 = r4 + r2
            int r4 = r4 + r0
            r7.mTotalLength = r4
            r0 = r15[r18]
            r2 = -1
            if (r0 != r2) goto L_0x04b6
            r0 = 0
            r4 = r15[r0]
            if (r4 != r2) goto L_0x04b6
            r0 = r15[r17]
            if (r0 != r2) goto L_0x04b6
            r0 = r15[r16]
            if (r0 == r2) goto L_0x04b4
            goto L_0x04b6
        L_0x04b4:
            r0 = r6
            goto L_0x04e4
        L_0x04b6:
            r0 = r15[r16]
            r2 = 0
            r4 = r15[r2]
            r5 = r15[r18]
            r8 = r15[r17]
            int r5 = java.lang.Math.max(r5, r8)
            int r4 = java.lang.Math.max(r4, r5)
            int r0 = java.lang.Math.max(r0, r4)
            r4 = r29[r16]
            r2 = r29[r2]
            r5 = r29[r18]
            r8 = r29[r17]
            int r5 = java.lang.Math.max(r5, r8)
            int r2 = java.lang.Math.max(r2, r5)
            int r2 = java.lang.Math.max(r4, r2)
            int r2 = r2 + r0
            int r0 = java.lang.Math.max(r6, r2)
        L_0x04e4:
            r2 = r0
            r23 = r9
            r0 = r10
        L_0x04e8:
            if (r27 != 0) goto L_0x04ef
            r4 = 1073741824(0x40000000, float:2.0)
            if (r13 == r4) goto L_0x04ef
            goto L_0x04f0
        L_0x04ef:
            r0 = r2
        L_0x04f0:
            int r2 = r38.getPaddingTop()
            int r4 = r38.getPaddingBottom()
            int r4 = r4 + r2
            int r4 = r4 + r0
            int r0 = r38.getSuggestedMinimumHeight()
            int r0 = java.lang.Math.max(r4, r0)
            r2 = -16777216(0xffffffffff000000, float:-1.7014118E38)
            r2 = r23 & r2
            r1 = r1 | r2
            int r2 = r23 << 16
            int r0 = android.view.View.resolveSizeAndState(r0, r3, r2)
            r7.setMeasuredDimension(r1, r0)
            if (r28 == 0) goto L_0x0519
            r0 = r39
            r1 = r26
            r7.forceUniformHeight(r1, r0)
        L_0x0519:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p002v7.widget.LinearLayoutCompat.measureHorizontal(int, int):void");
    }

    /* access modifiers changed from: package-private */
    public int measureNullChild(int i) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x0335  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x0340  */
    /* JADX WARNING: Removed duplicated region for block: B:147:0x0343  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void measureVertical(int r35, int r36) {
        /*
            r34 = this;
            r7 = r34
            r8 = r35
            r9 = r36
            r10 = 0
            r7.mTotalLength = r10
            int r11 = r34.getVirtualChildCount()
            int r12 = android.view.View.MeasureSpec.getMode(r35)
            int r13 = android.view.View.MeasureSpec.getMode(r36)
            int r14 = r7.mBaselineAlignedChildIndex
            boolean r15 = r7.mUseLargestChild
            r16 = 0
            r17 = 1
            r1 = r10
            r2 = r1
            r3 = r2
            r4 = r3
            r5 = r4
            r6 = r5
            r18 = r6
            r20 = r18
            r0 = r16
            r19 = r17
        L_0x002b:
            r10 = 8
            r22 = r4
            if (r6 >= r11) goto L_0x01a2
            android.view.View r4 = r7.getVirtualChildAt(r6)
            if (r4 != 0) goto L_0x0048
            int r4 = r7.mTotalLength
            int r10 = r7.measureNullChild(r6)
            int r4 = r4 + r10
            r7.mTotalLength = r4
            r23 = r11
            r4 = r22
        L_0x0044:
            r22 = r13
            goto L_0x0196
        L_0x0048:
            r24 = r1
            int r1 = r4.getVisibility()
            if (r1 != r10) goto L_0x005c
            int r1 = r7.getChildrenSkipCount(r4, r6)
            int r6 = r6 + r1
            r23 = r11
            r4 = r22
            r1 = r24
            goto L_0x0044
        L_0x005c:
            boolean r1 = r7.hasDividerBeforeChildAt(r6)
            if (r1 == 0) goto L_0x0069
            int r1 = r7.mTotalLength
            int r10 = r7.mDividerHeight
            int r1 = r1 + r10
            r7.mTotalLength = r1
        L_0x0069:
            android.view.ViewGroup$LayoutParams r1 = r4.getLayoutParams()
            r10 = r1
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r10 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r10
            float r1 = r10.weight
            float r25 = r0 + r1
            r1 = 1073741824(0x40000000, float:2.0)
            if (r13 != r1) goto L_0x00a7
            int r0 = r10.height
            if (r0 != 0) goto L_0x00a7
            float r0 = r10.weight
            int r0 = (r0 > r16 ? 1 : (r0 == r16 ? 0 : -1))
            if (r0 <= 0) goto L_0x00a7
            int r0 = r7.mTotalLength
            int r1 = r10.topMargin
            int r1 = r1 + r0
            r26 = r2
            int r2 = r10.bottomMargin
            int r1 = r1 + r2
            int r0 = java.lang.Math.max(r0, r1)
            r7.mTotalLength = r0
            r0 = r3
            r3 = r4
            r31 = r5
            r23 = r11
            r18 = r17
            r8 = r24
            r30 = r26
            r11 = r6
            r32 = r22
            r22 = r13
            r13 = r32
            goto L_0x011b
        L_0x00a7:
            r26 = r2
            int r0 = r10.height
            if (r0 != 0) goto L_0x00b8
            float r0 = r10.weight
            int r0 = (r0 > r16 ? 1 : (r0 == r16 ? 0 : -1))
            if (r0 <= 0) goto L_0x00b8
            r0 = -2
            r10.height = r0
            r2 = 0
            goto L_0x00ba
        L_0x00b8:
            r2 = -2147483648(0xffffffff80000000, float:-0.0)
        L_0x00ba:
            r27 = 0
            int r0 = (r25 > r16 ? 1 : (r25 == r16 ? 0 : -1))
            if (r0 != 0) goto L_0x00c5
            int r0 = r7.mTotalLength
            r28 = r0
            goto L_0x00c7
        L_0x00c5:
            r28 = 0
        L_0x00c7:
            r0 = r34
            r8 = r24
            r23 = 1073741824(0x40000000, float:2.0)
            r1 = r4
            r29 = r2
            r30 = r26
            r2 = r6
            r9 = r3
            r3 = r35
            r24 = r4
            r32 = r23
            r23 = r11
            r11 = r32
            r33 = r22
            r22 = r13
            r13 = r33
            r4 = r27
            r31 = r5
            r5 = r36
            r11 = r6
            r6 = r28
            r0.measureChildBeforeLayout(r1, r2, r3, r4, r5, r6)
            r0 = r29
            r1 = -2147483648(0xffffffff80000000, float:-0.0)
            if (r0 == r1) goto L_0x00f8
            r10.height = r0
        L_0x00f8:
            int r0 = r24.getMeasuredHeight()
            int r1 = r7.mTotalLength
            int r2 = r1 + r0
            int r3 = r10.topMargin
            int r2 = r2 + r3
            int r3 = r10.bottomMargin
            int r2 = r2 + r3
            r3 = r24
            int r4 = r7.getNextLocationOffset(r3)
            int r2 = r2 + r4
            int r1 = java.lang.Math.max(r1, r2)
            r7.mTotalLength = r1
            if (r15 == 0) goto L_0x011a
            int r0 = java.lang.Math.max(r0, r9)
            goto L_0x011b
        L_0x011a:
            r0 = r9
        L_0x011b:
            if (r14 < 0) goto L_0x0125
            int r6 = r11 + 1
            if (r14 != r6) goto L_0x0125
            int r1 = r7.mTotalLength
            r7.mBaselineChildTop = r1
        L_0x0125:
            if (r11 >= r14) goto L_0x0136
            float r1 = r10.weight
            int r1 = (r1 > r16 ? 1 : (r1 == r16 ? 0 : -1))
            if (r1 > 0) goto L_0x012e
            goto L_0x0136
        L_0x012e:
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            java.lang.String r1 = "A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex."
            r0.<init>(r1)
            throw r0
        L_0x0136:
            r1 = 1073741824(0x40000000, float:2.0)
            if (r12 == r1) goto L_0x0144
            int r1 = r10.width
            r2 = -1
            if (r1 != r2) goto L_0x0144
            r1 = r17
            r20 = r1
            goto L_0x0145
        L_0x0144:
            r1 = 0
        L_0x0145:
            int r2 = r10.leftMargin
            int r4 = r10.rightMargin
            int r2 = r2 + r4
            int r4 = r3.getMeasuredWidth()
            int r4 = r4 + r2
            r5 = r30
            int r5 = java.lang.Math.max(r5, r4)
            int r6 = r3.getMeasuredState()
            int r6 = android.view.View.combineMeasuredStates(r8, r6)
            if (r19 == 0) goto L_0x0167
            int r8 = r10.width
            r9 = -1
            if (r8 != r9) goto L_0x0167
            r8 = r17
            goto L_0x0168
        L_0x0167:
            r8 = 0
        L_0x0168:
            float r9 = r10.weight
            int r9 = (r9 > r16 ? 1 : (r9 == r16 ? 0 : -1))
            if (r9 <= 0) goto L_0x017a
            if (r1 == 0) goto L_0x0171
            goto L_0x0172
        L_0x0171:
            r2 = r4
        L_0x0172:
            int r4 = java.lang.Math.max(r13, r2)
            r13 = r4
            r1 = r31
            goto L_0x0184
        L_0x017a:
            if (r1 == 0) goto L_0x017d
            goto L_0x017e
        L_0x017d:
            r2 = r4
        L_0x017e:
            r1 = r31
            int r1 = java.lang.Math.max(r1, r2)
        L_0x0184:
            int r2 = r7.getChildrenSkipCount(r3, r11)
            int r2 = r2 + r11
            r3 = r0
            r19 = r8
            r4 = r13
            r0 = r25
            r32 = r5
            r5 = r1
            r1 = r6
            r6 = r2
            r2 = r32
        L_0x0196:
            int r6 = r6 + 1
            r8 = r35
            r9 = r36
            r13 = r22
            r11 = r23
            goto L_0x002b
        L_0x01a2:
            r8 = r1
            r9 = r3
            r1 = r5
            r23 = r11
            r5 = r2
            r32 = r22
            r22 = r13
            r13 = r32
            int r2 = r7.mTotalLength
            if (r2 <= 0) goto L_0x01c2
            r2 = r23
            boolean r3 = r7.hasDividerBeforeChildAt(r2)
            if (r3 == 0) goto L_0x01c4
            int r3 = r7.mTotalLength
            int r4 = r7.mDividerHeight
            int r3 = r3 + r4
            r7.mTotalLength = r3
            goto L_0x01c4
        L_0x01c2:
            r2 = r23
        L_0x01c4:
            r3 = r22
            if (r15 == 0) goto L_0x0213
            r4 = -2147483648(0xffffffff80000000, float:-0.0)
            if (r3 == r4) goto L_0x01ce
            if (r3 != 0) goto L_0x0213
        L_0x01ce:
            r4 = 0
            r7.mTotalLength = r4
            r4 = 0
        L_0x01d2:
            if (r4 >= r2) goto L_0x0213
            android.view.View r6 = r7.getVirtualChildAt(r4)
            if (r6 != 0) goto L_0x01e4
            int r6 = r7.mTotalLength
            int r11 = r7.measureNullChild(r4)
            int r6 = r6 + r11
            r7.mTotalLength = r6
            goto L_0x020e
        L_0x01e4:
            int r11 = r6.getVisibility()
            if (r11 != r10) goto L_0x01f0
            int r6 = r7.getChildrenSkipCount(r6, r4)
            int r4 = r4 + r6
            goto L_0x020e
        L_0x01f0:
            android.view.ViewGroup$LayoutParams r11 = r6.getLayoutParams()
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r11 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r11
            int r14 = r7.mTotalLength
            int r21 = r14 + r9
            int r10 = r11.topMargin
            int r21 = r21 + r10
            int r10 = r11.bottomMargin
            int r21 = r21 + r10
            int r6 = r7.getNextLocationOffset(r6)
            int r6 = r21 + r6
            int r6 = java.lang.Math.max(r14, r6)
            r7.mTotalLength = r6
        L_0x020e:
            int r4 = r4 + 1
            r10 = 8
            goto L_0x01d2
        L_0x0213:
            int r4 = r7.mTotalLength
            int r6 = r34.getPaddingTop()
            int r10 = r34.getPaddingBottom()
            int r10 = r10 + r6
            int r10 = r10 + r4
            r7.mTotalLength = r10
            int r4 = r7.mTotalLength
            int r6 = r34.getSuggestedMinimumHeight()
            int r4 = java.lang.Math.max(r4, r6)
            r6 = r36
            r10 = r9
            r9 = 0
            int r4 = android.view.View.resolveSizeAndState(r4, r6, r9)
            r9 = 16777215(0xffffff, float:2.3509886E-38)
            r9 = r9 & r4
            int r11 = r7.mTotalLength
            int r9 = r9 - r11
            if (r18 != 0) goto L_0x0284
            if (r9 == 0) goto L_0x0243
            int r11 = (r0 > r16 ? 1 : (r0 == r16 ? 0 : -1))
            if (r11 <= 0) goto L_0x0243
            goto L_0x0284
        L_0x0243:
            int r0 = java.lang.Math.max(r1, r13)
            if (r15 == 0) goto L_0x027f
            r1 = 1073741824(0x40000000, float:2.0)
            if (r3 == r1) goto L_0x027f
            r1 = 0
        L_0x024e:
            if (r1 >= r2) goto L_0x027f
            android.view.View r3 = r7.getVirtualChildAt(r1)
            if (r3 == 0) goto L_0x027c
            int r9 = r3.getVisibility()
            r11 = 8
            if (r9 != r11) goto L_0x025f
            goto L_0x027c
        L_0x025f:
            android.view.ViewGroup$LayoutParams r9 = r3.getLayoutParams()
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r9 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r9
            float r9 = r9.weight
            int r9 = (r9 > r16 ? 1 : (r9 == r16 ? 0 : -1))
            if (r9 <= 0) goto L_0x027c
            int r9 = r3.getMeasuredWidth()
            r11 = 1073741824(0x40000000, float:2.0)
            int r9 = android.view.View.MeasureSpec.makeMeasureSpec(r9, r11)
            int r13 = android.view.View.MeasureSpec.makeMeasureSpec(r10, r11)
            r3.measure(r9, r13)
        L_0x027c:
            int r1 = r1 + 1
            goto L_0x024e
        L_0x027f:
            r11 = r35
            r1 = r8
            goto L_0x037a
        L_0x0284:
            float r10 = r7.mWeightSum
            int r11 = (r10 > r16 ? 1 : (r10 == r16 ? 0 : -1))
            if (r11 <= 0) goto L_0x028b
            r0 = r10
        L_0x028b:
            r10 = 0
            r7.mTotalLength = r10
            r11 = r0
            r0 = r10
            r32 = r8
            r8 = r1
            r1 = r32
        L_0x0295:
            if (r0 >= r2) goto L_0x0369
            android.view.View r13 = r7.getVirtualChildAt(r0)
            int r14 = r13.getVisibility()
            r15 = 8
            if (r14 != r15) goto L_0x02a9
            r21 = r11
            r11 = r35
            goto L_0x0362
        L_0x02a9:
            android.view.ViewGroup$LayoutParams r14 = r13.getLayoutParams()
            android.support.v7.widget.LinearLayoutCompat$LayoutParams r14 = (android.support.p002v7.widget.LinearLayoutCompat.LayoutParams) r14
            float r10 = r14.weight
            int r18 = (r10 > r16 ? 1 : (r10 == r16 ? 0 : -1))
            if (r18 <= 0) goto L_0x030b
            float r15 = (float) r9
            float r15 = r15 * r10
            float r15 = r15 / r11
            int r15 = (int) r15
            float r11 = r11 - r10
            int r9 = r9 - r15
            int r10 = r34.getPaddingLeft()
            int r18 = r34.getPaddingRight()
            int r18 = r18 + r10
            int r10 = r14.leftMargin
            int r18 = r18 + r10
            int r10 = r14.rightMargin
            int r10 = r18 + r10
            r18 = r9
            int r9 = r14.width
            r21 = r11
            r11 = r35
            int r9 = android.view.ViewGroup.getChildMeasureSpec(r11, r10, r9)
            int r10 = r14.height
            if (r10 != 0) goto L_0x02ee
            r10 = 1073741824(0x40000000, float:2.0)
            if (r3 == r10) goto L_0x02e2
            goto L_0x02f0
        L_0x02e2:
            if (r15 <= 0) goto L_0x02e5
            goto L_0x02e6
        L_0x02e5:
            r15 = 0
        L_0x02e6:
            int r15 = android.view.View.MeasureSpec.makeMeasureSpec(r15, r10)
            r13.measure(r9, r15)
            goto L_0x0300
        L_0x02ee:
            r10 = 1073741824(0x40000000, float:2.0)
        L_0x02f0:
            int r23 = r13.getMeasuredHeight()
            int r15 = r23 + r15
            if (r15 >= 0) goto L_0x02f9
            r15 = 0
        L_0x02f9:
            int r15 = android.view.View.MeasureSpec.makeMeasureSpec(r15, r10)
            r13.measure(r9, r15)
        L_0x0300:
            int r9 = r13.getMeasuredState()
            r9 = r9 & -256(0xffffffffffffff00, float:NaN)
            int r1 = android.view.View.combineMeasuredStates(r1, r9)
            goto L_0x0312
        L_0x030b:
            r10 = r11
            r11 = r35
            r18 = r9
            r21 = r10
        L_0x0312:
            int r9 = r14.leftMargin
            int r10 = r14.rightMargin
            int r9 = r9 + r10
            int r10 = r13.getMeasuredWidth()
            int r10 = r10 + r9
            int r5 = java.lang.Math.max(r5, r10)
            r15 = 1073741824(0x40000000, float:2.0)
            if (r12 == r15) goto L_0x032e
            int r15 = r14.width
            r23 = r1
            r1 = -1
            if (r15 != r1) goto L_0x0331
            r15 = r17
            goto L_0x0332
        L_0x032e:
            r23 = r1
            r1 = -1
        L_0x0331:
            r15 = 0
        L_0x0332:
            if (r15 == 0) goto L_0x0335
            goto L_0x0336
        L_0x0335:
            r9 = r10
        L_0x0336:
            int r8 = java.lang.Math.max(r8, r9)
            if (r19 == 0) goto L_0x0343
            int r9 = r14.width
            if (r9 != r1) goto L_0x0343
            r9 = r17
            goto L_0x0344
        L_0x0343:
            r9 = 0
        L_0x0344:
            int r10 = r7.mTotalLength
            int r15 = r13.getMeasuredHeight()
            int r15 = r15 + r10
            int r1 = r14.topMargin
            int r15 = r15 + r1
            int r1 = r14.bottomMargin
            int r15 = r15 + r1
            int r1 = r7.getNextLocationOffset(r13)
            int r15 = r15 + r1
            int r1 = java.lang.Math.max(r10, r15)
            r7.mTotalLength = r1
            r19 = r9
            r9 = r18
            r1 = r23
        L_0x0362:
            int r0 = r0 + 1
            r11 = r21
            r10 = 0
            goto L_0x0295
        L_0x0369:
            r11 = r35
            int r0 = r7.mTotalLength
            int r3 = r34.getPaddingTop()
            int r9 = r34.getPaddingBottom()
            int r9 = r9 + r3
            int r9 = r9 + r0
            r7.mTotalLength = r9
            r0 = r8
        L_0x037a:
            if (r19 != 0) goto L_0x0381
            r3 = 1073741824(0x40000000, float:2.0)
            if (r12 == r3) goto L_0x0381
            goto L_0x0382
        L_0x0381:
            r0 = r5
        L_0x0382:
            int r3 = r34.getPaddingLeft()
            int r5 = r34.getPaddingRight()
            int r5 = r5 + r3
            int r5 = r5 + r0
            int r0 = r34.getSuggestedMinimumWidth()
            int r0 = java.lang.Math.max(r5, r0)
            int r0 = android.view.View.resolveSizeAndState(r0, r11, r1)
            r7.setMeasuredDimension(r0, r4)
            if (r20 == 0) goto L_0x03a0
            r7.forceUniformWidth(r2, r6)
        L_0x03a0:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p002v7.widget.LinearLayoutCompat.measureVertical(int, int):void");
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mDivider != null) {
            if (this.mOrientation == 1) {
                drawDividersVertical(canvas);
            } else {
                drawDividersHorizontal(canvas);
            }
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(LinearLayoutCompat.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(LinearLayoutCompat.class.getName());
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mOrientation == 1) {
            layoutVertical(i, i2, i3, i4);
        } else {
            layoutHorizontal(i, i2, i3, i4);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        if (this.mOrientation == 1) {
            measureVertical(i, i2);
        } else {
            measureHorizontal(i, i2);
        }
    }

    public void setBaselineAligned(boolean z) {
        this.mBaselineAligned = z;
    }

    public void setDividerDrawable(Drawable drawable) {
        if (drawable != this.mDivider) {
            this.mDivider = drawable;
            boolean z = false;
            if (drawable != null) {
                this.mDividerWidth = drawable.getIntrinsicWidth();
                this.mDividerHeight = drawable.getIntrinsicHeight();
            } else {
                this.mDividerWidth = 0;
                this.mDividerHeight = 0;
            }
            if (drawable == null) {
                z = true;
            }
            setWillNotDraw(z);
            requestLayout();
        }
    }

    public void setGravity(int i) {
        if (this.mGravity != i) {
            if ((8388615 & i) == 0) {
                i |= 8388611;
            }
            if ((i & 112) == 0) {
                i |= 48;
            }
            this.mGravity = i;
            requestLayout();
        }
    }

    public void setOrientation(int i) {
        if (this.mOrientation != i) {
            this.mOrientation = i;
            requestLayout();
        }
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public LinearLayoutCompat(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateDefaultLayoutParams() {
        int i = this.mOrientation;
        if (i == 0) {
            return new LayoutParams(-2, -2);
        }
        if (i == 1) {
            return new LayoutParams(-1, -2);
        }
        return null;
    }

    public LinearLayoutCompat(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = 0;
        this.mGravity = 8388659;
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, R$styleable.LinearLayoutCompat, i, 0);
        int i2 = obtainStyledAttributes.getInt(1, -1);
        if (i2 >= 0) {
            setOrientation(i2);
        }
        int i3 = obtainStyledAttributes.getInt(R$styleable.LinearLayoutCompat_android_gravity, -1);
        if (i3 >= 0) {
            setGravity(i3);
        }
        boolean z = obtainStyledAttributes.getBoolean(2, true);
        if (!z) {
            setBaselineAligned(z);
        }
        this.mWeightSum = obtainStyledAttributes.getFloat(4, -1.0f);
        this.mBaselineAlignedChildIndex = obtainStyledAttributes.getInt(3, -1);
        this.mUseLargestChild = obtainStyledAttributes.getBoolean(7, false);
        setDividerDrawable(obtainStyledAttributes.getDrawable(5));
        this.mShowDividers = obtainStyledAttributes.getInt(8, 0);
        this.mDividerPadding = obtainStyledAttributes.getDimensionPixelSize(6, 0);
        obtainStyledAttributes.recycle();
    }

    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    /* renamed from: android.support.v7.widget.LinearLayoutCompat$LayoutParams */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int gravity;
        public float weight;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.gravity = -1;
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.LinearLayoutCompat_Layout);
            this.weight = obtainStyledAttributes.getFloat(3, 0.0f);
            this.gravity = obtainStyledAttributes.getInt(R$styleable.LinearLayoutCompat_Layout_android_layout_gravity, -1);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.gravity = -1;
        }
    }
}