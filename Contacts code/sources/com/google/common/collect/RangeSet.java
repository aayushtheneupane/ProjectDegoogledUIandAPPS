package com.google.common.collect;

import java.lang.Comparable;
import java.util.Set;

public interface RangeSet<C extends Comparable> {
    void add(Range<C> range);

    void addAll(RangeSet<C> rangeSet);

    Set<Range<C>> asRanges();

    void clear();

    RangeSet<C> complement();

    boolean contains(C c);

    boolean encloses(Range<C> range);

    boolean enclosesAll(RangeSet<C> rangeSet);

    boolean equals(Object obj);

    int hashCode();

    boolean isEmpty();

    Range<C> rangeContaining(C c);

    void remove(Range<C> range);

    void removeAll(RangeSet<C> rangeSet);

    Range<C> span();

    RangeSet<C> subRangeSet(Range<C> range);

    String toString();
}
