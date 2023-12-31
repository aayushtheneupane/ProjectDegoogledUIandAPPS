package kotlin.text;

import java.util.Iterator;
import java.util.NoSuchElementException;
import kotlin.TypeCastException;
import kotlin.jvm.internal.markers.KMappedMarker;
import kotlin.ranges.IntRange;

/* compiled from: Strings.kt */
public final class DelimitedRangesSequence$iterator$1 implements Iterator<IntRange>, KMappedMarker {
    private int counter;
    private int currentStartIndex;
    private IntRange nextItem;
    private int nextSearchIndex;
    private int nextState = -1;
    final /* synthetic */ DelimitedRangesSequence this$0;

    public void remove() {
        throw new UnsupportedOperationException("Operation is not supported for read-only collection");
    }

    DelimitedRangesSequence$iterator$1(DelimitedRangesSequence delimitedRangesSequence) {
        this.this$0 = delimitedRangesSequence;
        this.currentStartIndex = RangesKt___RangesKt.coerceIn(delimitedRangesSequence.startIndex, 0, delimitedRangesSequence.input.length());
        this.nextSearchIndex = this.currentStartIndex;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0023, code lost:
        if (r6.counter < r6.this$0.limit) goto L_0x0025;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void calcNext() {
        /*
            r6 = this;
            int r0 = r6.nextSearchIndex
            r1 = 0
            if (r0 >= 0) goto L_0x000c
            r6.nextState = r1
            r0 = 0
            r6.nextItem = r0
            goto L_0x00a2
        L_0x000c:
            kotlin.text.DelimitedRangesSequence r0 = r6.this$0
            int r0 = r0.limit
            r2 = -1
            r3 = 1
            if (r0 <= 0) goto L_0x0025
            int r0 = r6.counter
            int r0 = r0 + r3
            r6.counter = r0
            int r0 = r6.counter
            kotlin.text.DelimitedRangesSequence r4 = r6.this$0
            int r4 = r4.limit
            if (r0 >= r4) goto L_0x0033
        L_0x0025:
            int r0 = r6.nextSearchIndex
            kotlin.text.DelimitedRangesSequence r4 = r6.this$0
            java.lang.CharSequence r4 = r4.input
            int r4 = r4.length()
            if (r0 <= r4) goto L_0x0049
        L_0x0033:
            int r0 = r6.currentStartIndex
            kotlin.ranges.IntRange r1 = new kotlin.ranges.IntRange
            kotlin.text.DelimitedRangesSequence r4 = r6.this$0
            java.lang.CharSequence r4 = r4.input
            int r4 = kotlin.text.StringsKt__StringsKt.getLastIndex(r4)
            r1.<init>(r0, r4)
            r6.nextItem = r1
            r6.nextSearchIndex = r2
            goto L_0x00a0
        L_0x0049:
            kotlin.text.DelimitedRangesSequence r0 = r6.this$0
            kotlin.jvm.functions.Function2 r0 = r0.getNextMatch
            kotlin.text.DelimitedRangesSequence r4 = r6.this$0
            java.lang.CharSequence r4 = r4.input
            int r5 = r6.nextSearchIndex
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            java.lang.Object r0 = r0.invoke(r4, r5)
            kotlin.Pair r0 = (kotlin.Pair) r0
            if (r0 != 0) goto L_0x0079
            int r0 = r6.currentStartIndex
            kotlin.ranges.IntRange r1 = new kotlin.ranges.IntRange
            kotlin.text.DelimitedRangesSequence r4 = r6.this$0
            java.lang.CharSequence r4 = r4.input
            int r4 = kotlin.text.StringsKt__StringsKt.getLastIndex(r4)
            r1.<init>(r0, r4)
            r6.nextItem = r1
            r6.nextSearchIndex = r2
            goto L_0x00a0
        L_0x0079:
            java.lang.Object r2 = r0.component1()
            java.lang.Number r2 = (java.lang.Number) r2
            int r2 = r2.intValue()
            java.lang.Object r0 = r0.component2()
            java.lang.Number r0 = (java.lang.Number) r0
            int r0 = r0.intValue()
            int r4 = r6.currentStartIndex
            kotlin.ranges.IntRange r4 = kotlin.ranges.RangesKt___RangesKt.until(r4, r2)
            r6.nextItem = r4
            int r2 = r2 + r0
            r6.currentStartIndex = r2
            int r2 = r6.currentStartIndex
            if (r0 != 0) goto L_0x009d
            r1 = r3
        L_0x009d:
            int r2 = r2 + r1
            r6.nextSearchIndex = r2
        L_0x00a0:
            r6.nextState = r3
        L_0x00a2:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlin.text.DelimitedRangesSequence$iterator$1.calcNext():void");
    }

    public IntRange next() {
        if (this.nextState == -1) {
            calcNext();
        }
        if (this.nextState != 0) {
            IntRange intRange = this.nextItem;
            if (intRange != null) {
                this.nextItem = null;
                this.nextState = -1;
                return intRange;
            }
            throw new TypeCastException("null cannot be cast to non-null type kotlin.ranges.IntRange");
        }
        throw new NoSuchElementException();
    }

    public boolean hasNext() {
        if (this.nextState == -1) {
            calcNext();
        }
        return this.nextState == 1;
    }
}
