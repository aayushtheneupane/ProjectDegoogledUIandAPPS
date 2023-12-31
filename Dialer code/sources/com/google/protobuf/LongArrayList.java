package com.google.protobuf;

import com.google.protobuf.Internal;
import java.util.Arrays;
import java.util.Collection;
import java.util.RandomAccess;

final class LongArrayList extends AbstractProtobufList<Long> implements Internal.LongList, RandomAccess {
    private static final LongArrayList EMPTY_LIST = new LongArrayList(new long[10], 0);
    private long[] array;
    private int size;

    static {
        EMPTY_LIST.makeImmutable();
    }

    LongArrayList() {
        this.array = new long[10];
        this.size = 0;
    }

    public static LongArrayList emptyList() {
        return EMPTY_LIST;
    }

    private void ensureIndexInRange(int i) {
        if (i < 0 || i >= this.size) {
            throw new IndexOutOfBoundsException(makeOutOfBoundsExceptionMessage(i));
        }
    }

    private String makeOutOfBoundsExceptionMessage(int i) {
        return "Index:" + i + ", Size:" + this.size;
    }

    public void add(int i, Object obj) {
        addLong(i, ((Long) obj).longValue());
    }

    public boolean addAll(Collection<? extends Long> collection) {
        ensureIsMutable();
        if (collection == null) {
            throw new NullPointerException();
        } else if (!(collection instanceof LongArrayList)) {
            return super.addAll(collection);
        } else {
            LongArrayList longArrayList = (LongArrayList) collection;
            int i = longArrayList.size;
            if (i == 0) {
                return false;
            }
            int i2 = this.size;
            if (Integer.MAX_VALUE - i2 >= i) {
                int i3 = i2 + i;
                long[] jArr = this.array;
                if (i3 > jArr.length) {
                    this.array = Arrays.copyOf(jArr, i3);
                }
                System.arraycopy(longArrayList.array, 0, this.array, this.size, longArrayList.size);
                this.size = i3;
                this.modCount++;
                return true;
            }
            throw new OutOfMemoryError();
        }
    }

    public void addLong(long j) {
        addLong(this.size, j);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IntArrayList)) {
            return super.equals(obj);
        }
        LongArrayList longArrayList = (LongArrayList) obj;
        if (this.size != longArrayList.size) {
            return false;
        }
        long[] jArr = longArrayList.array;
        for (int i = 0; i < this.size; i++) {
            if (this.array[i] != jArr[i]) {
                return false;
            }
        }
        return true;
    }

    public Object get(int i) {
        ensureIndexInRange(i);
        return Long.valueOf(this.array[i]);
    }

    public long getLong(int i) {
        ensureIndexInRange(i);
        return this.array[i];
    }

    public int hashCode() {
        int i = 1;
        for (int i2 = 0; i2 < this.size; i2++) {
            i = (i * 31) + Internal.hashLong(this.array[i2]);
        }
        return i;
    }

    public boolean remove(Object obj) {
        ensureIsMutable();
        for (int i = 0; i < this.size; i++) {
            if (obj.equals(Long.valueOf(this.array[i]))) {
                long[] jArr = this.array;
                System.arraycopy(jArr, i + 1, jArr, i, this.size - i);
                this.size--;
                this.modCount++;
                return true;
            }
        }
        return false;
    }

    public Object set(int i, Object obj) {
        long longValue = ((Long) obj).longValue();
        ensureIsMutable();
        ensureIndexInRange(i);
        long[] jArr = this.array;
        long j = jArr[i];
        jArr[i] = longValue;
        return Long.valueOf(j);
    }

    public int size() {
        return this.size;
    }

    private void addLong(int i, long j) {
        int i2;
        ensureIsMutable();
        if (i < 0 || i > (i2 = this.size)) {
            throw new IndexOutOfBoundsException(makeOutOfBoundsExceptionMessage(i));
        }
        long[] jArr = this.array;
        if (i2 < jArr.length) {
            System.arraycopy(jArr, i, jArr, i + 1, i2 - i);
        } else {
            long[] jArr2 = new long[(((i2 * 3) / 2) + 1)];
            System.arraycopy(jArr, 0, jArr2, 0, i);
            System.arraycopy(this.array, i, jArr2, i + 1, this.size - i);
            this.array = jArr2;
        }
        this.array[i] = j;
        this.size++;
        this.modCount++;
    }

    public Internal.LongList mutableCopyWithCapacity(int i) {
        if (i >= this.size) {
            return new LongArrayList(Arrays.copyOf(this.array, i), this.size);
        }
        throw new IllegalArgumentException();
    }

    private LongArrayList(long[] jArr, int i) {
        this.array = jArr;
        this.size = i;
    }

    public Object remove(int i) {
        ensureIsMutable();
        ensureIndexInRange(i);
        long[] jArr = this.array;
        long j = jArr[i];
        System.arraycopy(jArr, i + 1, jArr, i, this.size - i);
        this.size--;
        this.modCount++;
        return Long.valueOf(j);
    }
}
