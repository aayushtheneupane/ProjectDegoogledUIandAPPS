package com.google.common.collect;

import android.support.p002v7.appcompat.R$style;
import java.lang.Comparable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

final class TreeRangeSet$RangesByUpperBound<C extends Comparable<?>> extends AbstractNavigableMap<Cut<C>, Range<C>> {
    private final NavigableMap<Cut<C>, Range<C>> rangesByLowerBound;
    /* access modifiers changed from: private */
    public final Range<Cut<C>> upperBoundWindow;

    private TreeRangeSet$RangesByUpperBound(NavigableMap<Cut<C>, Range<C>> navigableMap, Range<Cut<C>> range) {
        this.rangesByLowerBound = navigableMap;
        this.upperBoundWindow = range;
    }

    public Comparator<? super Cut<C>> comparator() {
        return Ordering.natural();
    }

    public boolean containsKey(Object obj) {
        return get(obj) != null;
    }

    /* access modifiers changed from: package-private */
    public Iterator<Map.Entry<Cut<C>, Range<C>>> descendingEntryIterator() {
        Collection collection;
        if (this.upperBoundWindow.hasUpperBound()) {
            collection = this.rangesByLowerBound.headMap(this.upperBoundWindow.upperEndpoint(), false).descendingMap().values();
        } else {
            collection = this.rangesByLowerBound.descendingMap().values();
        }
        final PeekingIterator peekingIterator = Collections2.peekingIterator(collection.iterator());
        if (peekingIterator.hasNext()) {
            Iterators$PeekingImpl iterators$PeekingImpl = (Iterators$PeekingImpl) peekingIterator;
            if (this.upperBoundWindow.upperBound.isLessThan(((Range) iterators$PeekingImpl.peek()).upperBound)) {
                iterators$PeekingImpl.next();
            }
        }
        return new AbstractIterator<Map.Entry<Cut<C>, Range<C>>>() {
            /* access modifiers changed from: protected */
            public Object computeNext() {
                if (!peekingIterator.hasNext()) {
                    endOfData();
                    return null;
                }
                Range range = (Range) ((Iterators$PeekingImpl) peekingIterator).next();
                if (TreeRangeSet$RangesByUpperBound.this.upperBoundWindow.lowerBound.isLessThan(range.upperBound)) {
                    return new ImmutableEntry(range.upperBound, range);
                }
                endOfData();
                return null;
            }
        };
    }

    /* access modifiers changed from: package-private */
    public Iterator<Map.Entry<Cut<C>, Range<C>>> entryIterator() {
        final Iterator it;
        if (!this.upperBoundWindow.hasLowerBound()) {
            it = this.rangesByLowerBound.values().iterator();
        } else {
            Map.Entry<Cut<C>, Range<C>> lowerEntry = this.rangesByLowerBound.lowerEntry(this.upperBoundWindow.lowerEndpoint());
            if (lowerEntry == null) {
                it = this.rangesByLowerBound.values().iterator();
            } else if (this.upperBoundWindow.lowerBound.isLessThan(lowerEntry.getValue().upperBound)) {
                it = this.rangesByLowerBound.tailMap(lowerEntry.getKey(), true).values().iterator();
            } else {
                it = this.rangesByLowerBound.tailMap(this.upperBoundWindow.lowerEndpoint(), true).values().iterator();
            }
        }
        return new AbstractIterator<Map.Entry<Cut<C>, Range<C>>>() {
            /* access modifiers changed from: protected */
            public Object computeNext() {
                if (!it.hasNext()) {
                    endOfData();
                    return null;
                }
                Range range = (Range) it.next();
                if (!TreeRangeSet$RangesByUpperBound.this.upperBoundWindow.upperBound.isLessThan(range.upperBound)) {
                    return new ImmutableEntry(range.upperBound, range);
                }
                endOfData();
                return null;
            }
        };
    }

    public boolean isEmpty() {
        if (this.upperBoundWindow.equals(Range.all())) {
            return this.rangesByLowerBound.isEmpty();
        }
        return !entryIterator().hasNext();
    }

    public int size() {
        if (this.upperBoundWindow.equals(Range.all())) {
            return this.rangesByLowerBound.size();
        }
        Iterator entryIterator = entryIterator();
        long j = 0;
        while (entryIterator.hasNext()) {
            entryIterator.next();
            j++;
        }
        return R$style.saturatedCast(j);
    }

    private NavigableMap<Cut<C>, Range<C>> subMap(Range<Cut<C>> range) {
        if (range.isConnected(this.upperBoundWindow)) {
            return new TreeRangeSet$RangesByUpperBound(this.rangesByLowerBound, range.intersection(this.upperBoundWindow));
        }
        return ImmutableSortedMap.m90of();
    }

    public Range<C> get(Object obj) {
        Map.Entry<Cut<C>, Range<C>> lowerEntry;
        if (obj instanceof Cut) {
            try {
                Cut cut = (Cut) obj;
                if (this.upperBoundWindow.contains(cut) && (lowerEntry = this.rangesByLowerBound.lowerEntry(cut)) != null && lowerEntry.getValue().upperBound.equals(cut)) {
                    return lowerEntry.getValue();
                }
            } catch (ClassCastException unused) {
            }
        }
        return null;
    }

    public NavigableMap<Cut<C>, Range<C>> headMap(Cut<C> cut, boolean z) {
        return subMap(Range.upTo(cut, BoundType.forBoolean(z)));
    }

    public NavigableMap<Cut<C>, Range<C>> tailMap(Cut<C> cut, boolean z) {
        return subMap(Range.downTo(cut, BoundType.forBoolean(z)));
    }

    public NavigableMap<Cut<C>, Range<C>> subMap(Cut<C> cut, boolean z, Cut<C> cut2, boolean z2) {
        return subMap(Range.range(cut, BoundType.forBoolean(z), cut2, BoundType.forBoolean(z2)));
    }
}
