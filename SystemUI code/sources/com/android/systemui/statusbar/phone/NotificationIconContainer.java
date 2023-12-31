package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.util.AttributeSet;
import android.view.View;
import androidx.collection.ArrayMap;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.C1775R$dimen;
import com.android.systemui.C1778R$integer;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.AlphaOptimizedFrameLayout;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;
import java.util.ArrayList;
import java.util.HashMap;

public class NotificationIconContainer extends AlphaOptimizedFrameLayout {
    /* access modifiers changed from: private */
    public static final AnimationProperties ADD_ICON_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties DOT_ANIMATION_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties ICON_ANIMATION_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties UNISOLATION_PROPERTY;
    /* access modifiers changed from: private */
    public static final AnimationProperties UNISOLATION_PROPERTY_OTHERS;
    /* access modifiers changed from: private */
    public static final AnimationProperties sTempProperties = new AnimationProperties() {
        private AnimationFilter mAnimationFilter = new AnimationFilter();

        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    };
    public final int MAX_STATIC_ICONS = getResources().getInteger(C1778R$integer.config_maxVisibleNotificationIcons);
    private final int MAX_VISIBLE_ICONS_ON_LOCK = getResources().getInteger(C1778R$integer.config_maxVisibleNotificationIconsWhenDark);
    private int[] mAbsolutePosition = new int[2];
    private int mActualLayoutWidth = Integer.MIN_VALUE;
    private float mActualPaddingEnd = -2.14748365E9f;
    private float mActualPaddingStart = -2.14748365E9f;
    /* access modifiers changed from: private */
    public int mAddAnimationStartIndex = -1;
    private boolean mAnimationsEnabled = true;
    /* access modifiers changed from: private */
    public int mCannedAnimationStartIndex = -1;
    private boolean mChangingViewPositions;
    /* access modifiers changed from: private */
    public boolean mDisallowNextAnimation;
    private int mDotPadding;
    private boolean mDozing;
    private IconState mFirstVisibleIconState;
    private int mIconSize;
    private final HashMap<View, IconState> mIconStates = new HashMap<>();
    private boolean mIsStaticLayout = true;
    /* access modifiers changed from: private */
    public StatusBarIconView mIsolatedIcon;
    /* access modifiers changed from: private */
    public View mIsolatedIconForAnimation;
    private Rect mIsolatedIconLocation;
    private IconState mLastVisibleIconState;
    private int mNumDots;
    private boolean mOnLockScreen;
    private float mOpenedAmount = 0.0f;
    private int mOverflowWidth;
    private ArrayMap<String, ArrayList<StatusBarIcon>> mReplacingIcons;
    private int mSpeedBumpIndex = -1;
    private int mStaticDotDiameter;
    private int mStaticDotRadius;
    private float mVisualOverflowStart;

    static {
        C13931 r0 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateX();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r0.setDuration(200);
        DOT_ANIMATION_PROPERTIES = r0;
        C13942 r02 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                animationFilter.animateScale();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r02.setDuration(100);
        r02.setCustomInterpolator(View.TRANSLATION_Y, Interpolators.ICON_OVERSHOT);
        ICON_ANIMATION_PROPERTIES = r02;
        C13964 r03 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateAlpha();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r03.setDuration(200);
        r03.setDelay(50);
        ADD_ICON_PROPERTIES = r03;
        C13975 r04 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateAlpha();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r04.setDuration(110);
        UNISOLATION_PROPERTY_OTHERS = r04;
        C13986 r05 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateX();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r05.setDuration(110);
        UNISOLATION_PROPERTY = r05;
    }

    public NotificationIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initDimens();
        setWillNotDraw(true);
    }

    private void initDimens() {
        this.mDotPadding = getResources().getDimensionPixelSize(C1775R$dimen.overflow_icon_dot_padding);
        this.mStaticDotRadius = getResources().getDimensionPixelSize(C1775R$dimen.overflow_dot_radius);
        this.mStaticDotDiameter = this.mStaticDotRadius * 2;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(-65536);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(getActualPaddingStart(), 0.0f, getLayoutEnd(), (float) getHeight(), paint);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        initDimens();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float height = ((float) getHeight()) / 2.0f;
        this.mIconSize = 0;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            int measuredWidth = childAt.getMeasuredWidth();
            int measuredHeight = childAt.getMeasuredHeight();
            int i6 = (int) (height - (((float) measuredHeight) / 2.0f));
            childAt.layout(0, i6, measuredWidth, measuredHeight + i6);
            if (i5 == 0) {
                setIconSize(childAt.getWidth());
            }
        }
        getLocationOnScreen(this.mAbsolutePosition);
        if (this.mIsStaticLayout) {
            updateState();
        }
    }

    private void setIconSize(int i) {
        this.mIconSize = i;
        this.mOverflowWidth = this.mIconSize + ((this.mStaticDotDiameter + this.mDotPadding) * 0);
    }

    private void updateState() {
        resetViewStates();
        calculateIconTranslations();
        applyIconStates();
    }

    public void applyIconStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            ViewState viewState = this.mIconStates.get(childAt);
            if (viewState != null) {
                viewState.applyToView(childAt);
            }
        }
        this.mAddAnimationStartIndex = -1;
        this.mCannedAnimationStartIndex = -1;
        this.mDisallowNextAnimation = false;
        this.mIsolatedIconForAnimation = null;
    }

    public void onViewAdded(View view) {
        super.onViewAdded(view);
        boolean isReplacingIcon = isReplacingIcon(view);
        if (!this.mChangingViewPositions) {
            IconState iconState = new IconState();
            if (isReplacingIcon) {
                iconState.justAdded = false;
                boolean unused = iconState.justReplaced = true;
            }
            this.mIconStates.put(view, iconState);
        }
        int indexOfChild = indexOfChild(view);
        if (indexOfChild < getChildCount() - 1 && !isReplacingIcon && this.mIconStates.get(getChildAt(indexOfChild + 1)).iconAppearAmount > 0.0f) {
            int i = this.mAddAnimationStartIndex;
            if (i < 0) {
                this.mAddAnimationStartIndex = indexOfChild;
            } else {
                this.mAddAnimationStartIndex = Math.min(i, indexOfChild);
            }
        }
        if (view instanceof StatusBarIconView) {
            ((StatusBarIconView) view).setDozing(this.mDozing, false, 0);
        }
    }

    private boolean isReplacingIcon(View view) {
        if (this.mReplacingIcons == null || !(view instanceof StatusBarIconView)) {
            return false;
        }
        StatusBarIconView statusBarIconView = (StatusBarIconView) view;
        Icon sourceIcon = statusBarIconView.getSourceIcon();
        ArrayList arrayList = this.mReplacingIcons.get(statusBarIconView.getNotification().getGroupKey());
        if (arrayList == null || !sourceIcon.sameAs(((StatusBarIcon) arrayList.get(0)).icon)) {
            return false;
        }
        return true;
    }

    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (view instanceof StatusBarIconView) {
            boolean isReplacingIcon = isReplacingIcon(view);
            StatusBarIconView statusBarIconView = (StatusBarIconView) view;
            if (areAnimationsEnabled(statusBarIconView) && statusBarIconView.getVisibleState() != 2 && view.getVisibility() == 0 && isReplacingIcon) {
                int findFirstViewIndexAfter = findFirstViewIndexAfter(statusBarIconView.getTranslationX());
                int i = this.mAddAnimationStartIndex;
                if (i < 0) {
                    this.mAddAnimationStartIndex = findFirstViewIndexAfter;
                } else {
                    this.mAddAnimationStartIndex = Math.min(i, findFirstViewIndexAfter);
                }
            }
            if (!this.mChangingViewPositions) {
                this.mIconStates.remove(view);
                if (areAnimationsEnabled(statusBarIconView) && !isReplacingIcon) {
                    boolean z = false;
                    addTransientView(statusBarIconView, 0);
                    if (view == this.mIsolatedIcon) {
                        z = true;
                    }
                    statusBarIconView.setVisibleState(2, true, new Runnable(statusBarIconView) {
                        private final /* synthetic */ StatusBarIconView f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            NotificationIconContainer.this.lambda$onViewRemoved$0$NotificationIconContainer(this.f$1);
                        }
                    }, z ? 110 : 0);
                }
            }
        }
    }

    public /* synthetic */ void lambda$onViewRemoved$0$NotificationIconContainer(StatusBarIconView statusBarIconView) {
        removeTransientView(statusBarIconView);
    }

    /* access modifiers changed from: private */
    public boolean areAnimationsEnabled(StatusBarIconView statusBarIconView) {
        return this.mAnimationsEnabled || statusBarIconView == this.mIsolatedIcon;
    }

    private int findFirstViewIndexAfter(float f) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getTranslationX() > f) {
                return i;
            }
        }
        return getChildCount();
    }

    public void resetViewStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            ViewState viewState = this.mIconStates.get(childAt);
            viewState.initFrom(childAt);
            StatusBarIconView statusBarIconView = this.mIsolatedIcon;
            viewState.alpha = (statusBarIconView == null || childAt == statusBarIconView) ? 1.0f : 0.0f;
            viewState.hidden = false;
        }
    }

    public void calculateIconTranslations() {
        int i;
        IconState iconState;
        float f;
        boolean z;
        int i2;
        float actualPaddingStart = getActualPaddingStart();
        int childCount = getChildCount();
        if (this.mOnLockScreen) {
            i = this.MAX_VISIBLE_ICONS_ON_LOCK;
        } else {
            i = this.mIsStaticLayout ? this.MAX_STATIC_ICONS : childCount;
        }
        float layoutEnd = getLayoutEnd();
        float maxOverflowStart = getMaxOverflowStart();
        float f2 = 0.0f;
        this.mVisualOverflowStart = 0.0f;
        this.mFirstVisibleIconState = null;
        int i3 = this.mSpeedBumpIndex;
        int i4 = -1;
        boolean z2 = i3 != -1 && i3 < getChildCount();
        float f3 = actualPaddingStart;
        int i5 = -1;
        int i6 = 0;
        while (i6 < childCount) {
            View childAt = getChildAt(i6);
            IconState iconState2 = this.mIconStates.get(childAt);
            iconState2.xTranslation = f3;
            if (this.mFirstVisibleIconState == null) {
                this.mFirstVisibleIconState = iconState2;
            }
            int i7 = this.mSpeedBumpIndex;
            boolean z3 = (i7 != i4 && i6 >= i7 && iconState2.iconAppearAmount > f2) || i6 >= i;
            boolean z4 = i6 == childCount + -1;
            float iconScaleIncreased = (!this.mOnLockScreen || !(childAt instanceof StatusBarIconView)) ? 1.0f : ((StatusBarIconView) childAt).getIconScaleIncreased();
            if (this.mOpenedAmount != f2) {
                z4 = z4 && !z2 && !z3;
            }
            iconState2.visibleState = 0;
            if (z4) {
                f = layoutEnd - ((float) this.mIconSize);
            } else {
                f = maxOverflowStart - ((float) this.mIconSize);
            }
            if (f3 > f) {
                i2 = -1;
                z = true;
            } else {
                z = false;
                i2 = -1;
            }
            if (i5 == i2 && (z3 || z)) {
                int i8 = (!z4 || z3) ? i6 : i6 - 1;
                this.mVisualOverflowStart = layoutEnd - ((float) this.mOverflowWidth);
                if (z3 || this.mIsStaticLayout) {
                    this.mVisualOverflowStart = Math.min(f3, this.mVisualOverflowStart);
                }
                i5 = i8;
            }
            f3 += iconState2.iconAppearAmount * ((float) childAt.getWidth()) * iconScaleIncreased;
            i6++;
            f2 = 0.0f;
            i4 = -1;
        }
        this.mNumDots = 0;
        if (i5 != -1) {
            f3 = this.mVisualOverflowStart;
            for (int i9 = i5; i9 < childCount; i9++) {
                IconState iconState3 = this.mIconStates.get(getChildAt(i9));
                int i10 = this.mStaticDotDiameter + this.mDotPadding;
                iconState3.xTranslation = f3;
                int i11 = this.mNumDots;
                if (i11 < 1) {
                    if (i11 != 0 || iconState3.iconAppearAmount >= 0.8f) {
                        iconState3.visibleState = 1;
                        this.mNumDots++;
                    } else {
                        iconState3.visibleState = 0;
                    }
                    if (this.mNumDots == 1) {
                        i10 *= 1;
                    }
                    f3 += ((float) i10) * iconState3.iconAppearAmount;
                    this.mLastVisibleIconState = iconState3;
                } else {
                    iconState3.visibleState = 2;
                }
            }
        } else if (childCount > 0) {
            this.mLastVisibleIconState = this.mIconStates.get(getChildAt(childCount - 1));
            this.mFirstVisibleIconState = this.mIconStates.get(getChildAt(0));
        }
        if (this.mOnLockScreen && f3 < getLayoutEnd()) {
            IconState iconState4 = this.mFirstVisibleIconState;
            float f4 = iconState4 == null ? 0.0f : iconState4.xTranslation;
            IconState iconState5 = this.mLastVisibleIconState;
            float layoutEnd2 = ((getLayoutEnd() - getActualPaddingStart()) - (iconState5 != null ? Math.min((float) getWidth(), iconState5.xTranslation + ((float) this.mIconSize)) - f4 : 0.0f)) / 2.0f;
            if (i5 != -1) {
                layoutEnd2 = (((getLayoutEnd() - this.mVisualOverflowStart) / 2.0f) + layoutEnd2) / 2.0f;
            }
            for (int i12 = 0; i12 < childCount; i12++) {
                this.mIconStates.get(getChildAt(i12)).xTranslation += layoutEnd2;
            }
        }
        if (isLayoutRtl()) {
            for (int i13 = 0; i13 < childCount; i13++) {
                View childAt2 = getChildAt(i13);
                IconState iconState6 = this.mIconStates.get(childAt2);
                iconState6.xTranslation = (((float) getWidth()) - iconState6.xTranslation) - ((float) childAt2.getWidth());
            }
        }
        StatusBarIconView statusBarIconView = this.mIsolatedIcon;
        if (statusBarIconView != null && (iconState = this.mIconStates.get(statusBarIconView)) != null) {
            iconState.xTranslation = ((float) (this.mIsolatedIconLocation.left - this.mAbsolutePosition[0])) - (((1.0f - this.mIsolatedIcon.getIconScale()) * ((float) this.mIsolatedIcon.getWidth())) / 2.0f);
            iconState.visibleState = 0;
        }
    }

    private float getLayoutEnd() {
        return ((float) getActualWidth()) - getActualPaddingEnd();
    }

    private float getActualPaddingEnd() {
        float f = this.mActualPaddingEnd;
        return f == -2.14748365E9f ? (float) getPaddingEnd() : f;
    }

    private float getActualPaddingStart() {
        float f = this.mActualPaddingStart;
        return f == -2.14748365E9f ? (float) getPaddingStart() : f;
    }

    public void setIsStaticLayout(boolean z) {
        this.mIsStaticLayout = z;
    }

    public void setActualLayoutWidth(int i) {
        this.mActualLayoutWidth = i;
    }

    public void setActualPaddingEnd(float f) {
        this.mActualPaddingEnd = f;
    }

    public void setActualPaddingStart(float f) {
        this.mActualPaddingStart = f;
    }

    public int getActualWidth() {
        int i = this.mActualLayoutWidth;
        return i == Integer.MIN_VALUE ? getWidth() : i;
    }

    public int getFinalTranslationX() {
        float f;
        if (this.mLastVisibleIconState == null) {
            return 0;
        }
        if (isLayoutRtl()) {
            f = ((float) getWidth()) - this.mLastVisibleIconState.xTranslation;
        } else {
            f = this.mLastVisibleIconState.xTranslation + ((float) this.mIconSize);
        }
        return Math.min(getWidth(), (int) f);
    }

    private float getMaxOverflowStart() {
        return getLayoutEnd() - ((float) this.mOverflowWidth);
    }

    public void setChangingViewPositions(boolean z) {
        this.mChangingViewPositions = z;
    }

    public void setDozing(boolean z, boolean z2, long j) {
        this.mDozing = z;
        this.mDisallowNextAnimation |= !z2;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof StatusBarIconView) {
                ((StatusBarIconView) childAt).setDozing(z, z2, j);
            }
        }
    }

    public IconState getIconState(StatusBarIconView statusBarIconView) {
        return this.mIconStates.get(statusBarIconView);
    }

    public void setSpeedBumpIndex(int i) {
        this.mSpeedBumpIndex = i;
    }

    public void setOpenedAmount(float f) {
        this.mOpenedAmount = f;
    }

    public boolean hasOverflow() {
        return this.mNumDots > 0;
    }

    public boolean hasPartialOverflow() {
        int i = this.mNumDots;
        return i > 0 && i < 1;
    }

    public int getPartialOverflowExtraPadding() {
        if (!hasPartialOverflow()) {
            return 0;
        }
        int i = (1 - this.mNumDots) * (this.mStaticDotDiameter + this.mDotPadding);
        return getFinalTranslationX() + i > getWidth() ? getWidth() - getFinalTranslationX() : i;
    }

    public int getNoOverflowExtraPadding() {
        if (this.mNumDots != 0) {
            return 0;
        }
        int i = this.mOverflowWidth;
        return getFinalTranslationX() + i > getWidth() ? getWidth() - getFinalTranslationX() : i;
    }

    public void setAnimationsEnabled(boolean z) {
        if (!z && this.mAnimationsEnabled) {
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                ViewState viewState = this.mIconStates.get(childAt);
                if (viewState != null) {
                    viewState.cancelAnimations(childAt);
                    viewState.applyToView(childAt);
                }
            }
        }
        this.mAnimationsEnabled = z;
    }

    public void setReplacingIcons(ArrayMap<String, ArrayList<StatusBarIcon>> arrayMap) {
        this.mReplacingIcons = arrayMap;
    }

    public void showIconIsolated(StatusBarIconView statusBarIconView, boolean z) {
        if (z) {
            this.mIsolatedIconForAnimation = statusBarIconView != null ? statusBarIconView : this.mIsolatedIcon;
        }
        this.mIsolatedIcon = statusBarIconView;
        updateState();
    }

    public void setIsolatedIconLocation(Rect rect, boolean z) {
        this.mIsolatedIconLocation = rect;
        if (z) {
            updateState();
        }
    }

    public void setOnLockScreen(boolean z) {
        this.mOnLockScreen = z;
    }

    public class IconState extends ViewState {
        public float clampedAppearAmount = 1.0f;
        public int customTransformHeight = Integer.MIN_VALUE;
        public float iconAppearAmount = 1.0f;
        public int iconColor = 0;
        public boolean isLastExpandIcon;
        public boolean justAdded = true;
        /* access modifiers changed from: private */
        public boolean justReplaced;
        public boolean needsCannedAnimation;
        public boolean noAnimations;
        public boolean translateContent;
        public boolean useFullTransitionAmount;
        public boolean useLinearTransitionAmount;
        public int visibleState;

        public IconState() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:37:0x0084  */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x00ef  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x010d  */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x0118  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void applyToView(android.view.View r13) {
            /*
                r12 = this;
                boolean r0 = r13 instanceof com.android.systemui.statusbar.StatusBarIconView
                r1 = 0
                if (r0 == 0) goto L_0x016f
                r0 = r13
                com.android.systemui.statusbar.StatusBarIconView r0 = (com.android.systemui.statusbar.StatusBarIconView) r0
                r2 = 0
                com.android.systemui.statusbar.phone.NotificationIconContainer r3 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                boolean r3 = r3.areAnimationsEnabled(r0)
                r4 = 1
                if (r3 == 0) goto L_0x0020
                com.android.systemui.statusbar.phone.NotificationIconContainer r3 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                boolean r3 = r3.mDisallowNextAnimation
                if (r3 != 0) goto L_0x0020
                boolean r3 = r12.noAnimations
                if (r3 != 0) goto L_0x0020
                r3 = r4
                goto L_0x0021
            L_0x0020:
                r3 = r1
            L_0x0021:
                if (r3 == 0) goto L_0x0145
                boolean r5 = r12.justAdded
                r6 = 2
                if (r5 != 0) goto L_0x003a
                boolean r5 = r12.justReplaced
                if (r5 == 0) goto L_0x002d
                goto L_0x003a
            L_0x002d:
                int r5 = r12.visibleState
                int r7 = r0.getVisibleState()
                if (r5 == r7) goto L_0x0055
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.DOT_ANIMATION_PROPERTIES
                goto L_0x0052
            L_0x003a:
                super.applyToView(r0)
                boolean r5 = r12.justAdded
                if (r5 == 0) goto L_0x0055
                float r5 = r12.iconAppearAmount
                r7 = 0
                int r5 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
                if (r5 == 0) goto L_0x0055
                r0.setAlpha(r7)
                r0.setVisibleState(r6, r1)
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.ADD_ICON_PROPERTIES
            L_0x0052:
                r5 = r2
                r2 = r4
                goto L_0x0057
            L_0x0055:
                r5 = r2
                r2 = r1
            L_0x0057:
                if (r2 != 0) goto L_0x007e
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.mAddAnimationStartIndex
                if (r7 < 0) goto L_0x007e
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.indexOfChild(r13)
                com.android.systemui.statusbar.phone.NotificationIconContainer r8 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r8 = r8.mAddAnimationStartIndex
                if (r7 < r8) goto L_0x007e
                int r7 = r0.getVisibleState()
                if (r7 != r6) goto L_0x0079
                int r7 = r12.visibleState
                if (r7 == r6) goto L_0x007e
            L_0x0079:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.DOT_ANIMATION_PROPERTIES
                r2 = r4
            L_0x007e:
                boolean r7 = r12.needsCannedAnimation
                r8 = 100
                if (r7 == 0) goto L_0x00cd
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                com.android.systemui.statusbar.notification.stack.AnimationFilter r2 = r2.getAnimationFilter()
                r2.reset()
                com.android.systemui.statusbar.notification.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.ICON_ANIMATION_PROPERTIES
                com.android.systemui.statusbar.notification.stack.AnimationFilter r7 = r7.getAnimationFilter()
                r2.combineFilter(r7)
                com.android.systemui.statusbar.notification.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r7.resetCustomInterpolators()
                com.android.systemui.statusbar.notification.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                com.android.systemui.statusbar.notification.stack.AnimationProperties r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.ICON_ANIMATION_PROPERTIES
                r7.combineCustomInterpolators(r10)
                if (r5 == 0) goto L_0x00bc
                com.android.systemui.statusbar.notification.stack.AnimationFilter r7 = r5.getAnimationFilter()
                r2.combineFilter(r7)
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r2.combineCustomInterpolators(r5)
            L_0x00bc:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r5.setDuration(r8)
                com.android.systemui.statusbar.phone.NotificationIconContainer r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r2.indexOfChild(r13)
                int unused = r2.mCannedAnimationStartIndex = r7
                r2 = r4
            L_0x00cd:
                if (r2 != 0) goto L_0x010d
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.mCannedAnimationStartIndex
                if (r7 < 0) goto L_0x010d
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.indexOfChild(r13)
                com.android.systemui.statusbar.phone.NotificationIconContainer r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r10 = r10.mCannedAnimationStartIndex
                if (r7 <= r10) goto L_0x010d
                int r7 = r0.getVisibleState()
                if (r7 != r6) goto L_0x00ef
                int r7 = r12.visibleState
                if (r7 == r6) goto L_0x010d
            L_0x00ef:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                com.android.systemui.statusbar.notification.stack.AnimationFilter r2 = r2.getAnimationFilter()
                r2.reset()
                r2.animateX()
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r2.resetCustomInterpolators()
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r2.setDuration(r8)
                r5 = r4
                goto L_0x0110
            L_0x010d:
                r11 = r5
                r5 = r2
                r2 = r11
            L_0x0110:
                com.android.systemui.statusbar.phone.NotificationIconContainer r6 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                android.view.View r6 = r6.mIsolatedIconForAnimation
                if (r6 == 0) goto L_0x0146
                com.android.systemui.statusbar.phone.NotificationIconContainer r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                android.view.View r2 = r2.mIsolatedIconForAnimation
                r5 = 0
                if (r13 != r2) goto L_0x0133
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.UNISOLATION_PROPERTY
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                com.android.systemui.statusbar.StatusBarIconView r7 = r7.mIsolatedIcon
                if (r7 == 0) goto L_0x012f
                r5 = r8
            L_0x012f:
                r2.setDelay(r5)
                goto L_0x0143
            L_0x0133:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.UNISOLATION_PROPERTY_OTHERS
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                com.android.systemui.statusbar.StatusBarIconView r7 = r7.mIsolatedIcon
                if (r7 != 0) goto L_0x0140
                r5 = r8
            L_0x0140:
                r2.setDelay(r5)
            L_0x0143:
                r5 = r4
                goto L_0x0146
            L_0x0145:
                r5 = r1
            L_0x0146:
                int r6 = r12.visibleState
                r0.setVisibleState(r6, r3)
                int r6 = r12.iconColor
                boolean r7 = r12.needsCannedAnimation
                if (r7 == 0) goto L_0x0155
                if (r3 == 0) goto L_0x0155
                r3 = r4
                goto L_0x0156
            L_0x0155:
                r3 = r1
            L_0x0156:
                r0.setIconColor(r6, r3)
                if (r5 == 0) goto L_0x015f
                r12.animateTo(r0, r2)
                goto L_0x0162
            L_0x015f:
                super.applyToView(r13)
            L_0x0162:
                float r13 = r12.iconAppearAmount
                r2 = 1065353216(0x3f800000, float:1.0)
                int r13 = (r13 > r2 ? 1 : (r13 == r2 ? 0 : -1))
                if (r13 != 0) goto L_0x016b
                goto L_0x016c
            L_0x016b:
                r4 = r1
            L_0x016c:
                r0.setIsInShelf(r4)
            L_0x016f:
                r12.justAdded = r1
                r12.justReplaced = r1
                r12.needsCannedAnimation = r1
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationIconContainer.IconState.applyToView(android.view.View):void");
        }

        public boolean hasCustomTransformHeight() {
            return this.isLastExpandIcon && this.customTransformHeight != Integer.MIN_VALUE;
        }

        public void initFrom(View view) {
            super.initFrom(view);
            if (view instanceof StatusBarIconView) {
                this.iconColor = ((StatusBarIconView) view).getStaticDrawableColor();
            }
        }
    }
}
