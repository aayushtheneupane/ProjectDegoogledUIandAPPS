package com.google.common.collect;

final class RegularImmutableSet<E> extends ImmutableSet<E> {
    private final Object[] elements;
    private final transient int hashCode;
    private final transient int mask;
    final transient Object[] table;

    /* access modifiers changed from: package-private */
    public boolean isHashCodeFast() {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isPartialView() {
        return false;
    }

    RegularImmutableSet(Object[] objArr, int i, Object[] objArr2, int i2) {
        this.elements = objArr;
        this.table = objArr2;
        this.mask = i2;
        this.hashCode = i;
    }

    public boolean contains(Object obj) {
        if (obj == null) {
            return false;
        }
        int smear = Hashing.smear(obj.hashCode());
        while (true) {
            Object obj2 = this.table[this.mask & smear];
            if (obj2 == null) {
                return false;
            }
            if (obj2.equals(obj)) {
                return true;
            }
            smear++;
        }
    }

    public int size() {
        return this.elements.length;
    }

    public UnmodifiableIterator<E> iterator() {
        return Iterators.forArray(this.elements);
    }

    /* access modifiers changed from: package-private */
    public int copyIntoArray(Object[] objArr, int i) {
        Object[] objArr2 = this.elements;
        System.arraycopy(objArr2, 0, objArr, i, objArr2.length);
        return i + this.elements.length;
    }

    /* access modifiers changed from: package-private */
    public ImmutableList<E> createAsList() {
        return new RegularImmutableAsList(this, this.elements);
    }

    public int hashCode() {
        return this.hashCode;
    }
}
