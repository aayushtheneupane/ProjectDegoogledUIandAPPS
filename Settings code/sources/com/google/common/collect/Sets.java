package com.google.common.collect;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class Sets {

    /* renamed from: com.google.common.collect.Sets$1 */
    class C16701 extends SetView<E> {
    }

    static abstract class ImprovedAbstractSet<E> extends AbstractSet<E> {
        ImprovedAbstractSet() {
        }

        public boolean removeAll(Collection<?> collection) {
            return Sets.removeAllImpl((Set<?>) this, collection);
        }

        public boolean retainAll(Collection<?> collection) {
            Preconditions.checkNotNull(collection);
            return super.retainAll(collection);
        }
    }

    public static <E> HashSet<E> newHashSet() {
        return new HashSet<>();
    }

    public static <E> HashSet<E> newHashSetWithExpectedSize(int i) {
        return new HashSet<>(Maps.capacity(i));
    }

    public static abstract class SetView<E> extends AbstractSet<E> {
        /* synthetic */ SetView(C16701 r1) {
            this();
        }

        private SetView() {
        }
    }

    public static <E> SetView<E> difference(final Set<E> set, final Set<?> set2) {
        Preconditions.checkNotNull(set, "set1");
        Preconditions.checkNotNull(set2, "set2");
        final Predicate<T> not = Predicates.not(Predicates.m61in(set2));
        return new SetView<E>() {
            public Iterator<E> iterator() {
                return Iterators.filter(set.iterator(), not);
            }

            public int size() {
                return Iterators.size(iterator());
            }

            public boolean isEmpty() {
                return set2.containsAll(set);
            }

            public boolean contains(Object obj) {
                return set.contains(obj) && !set2.contains(obj);
            }
        };
    }

    static int hashCodeImpl(Set<?> set) {
        Iterator<?> it = set.iterator();
        int i = 0;
        while (it.hasNext()) {
            Object next = it.next();
            i = ~(~(i + (next != null ? next.hashCode() : 0)));
        }
        return i;
    }

    static boolean equalsImpl(Set<?> set, Object obj) {
        if (set == obj) {
            return true;
        }
        if (obj instanceof Set) {
            Set set2 = (Set) obj;
            try {
                if (set.size() != set2.size() || !set.containsAll(set2)) {
                    return false;
                }
                return true;
            } catch (ClassCastException | NullPointerException unused) {
            }
        }
        return false;
    }

    static boolean removeAllImpl(Set<?> set, Iterator<?> it) {
        boolean z = false;
        while (it.hasNext()) {
            z |= set.remove(it.next());
        }
        return z;
    }

    static boolean removeAllImpl(Set<?> set, Collection<?> collection) {
        Preconditions.checkNotNull(collection);
        if (collection instanceof Multiset) {
            collection = ((Multiset) collection).elementSet();
        }
        if (!(collection instanceof Set) || collection.size() <= set.size()) {
            return removeAllImpl(set, collection.iterator());
        }
        return Iterators.removeAll(set.iterator(), collection);
    }
}
