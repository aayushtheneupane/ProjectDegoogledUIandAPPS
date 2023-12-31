package p000a.p005b;

import java.util.ConcurrentModificationException;
import java.util.Map;

/* renamed from: a.b.n */
public class C0027n {
    static Object[] mBaseCache;
    static int mBaseCacheSize;
    static Object[] mTwiceBaseCache;
    static int mTwiceBaseCacheSize;
    Object[] mArray;
    int[] mHashes;
    int mSize;

    public C0027n() {
        this.mHashes = C0018e.f9Wn;
        this.mArray = C0018e.f11Yn;
        this.mSize = 0;
    }

    /* renamed from: a */
    private static void m28a(int[] iArr, Object[] objArr, int i) {
        if (iArr.length == 8) {
            synchronized (C0027n.class) {
                if (mTwiceBaseCacheSize < 10) {
                    objArr[0] = mTwiceBaseCache;
                    objArr[1] = iArr;
                    for (int i2 = (i << 1) - 1; i2 >= 2; i2--) {
                        objArr[i2] = null;
                    }
                    mTwiceBaseCache = objArr;
                    mTwiceBaseCacheSize++;
                }
            }
        } else if (iArr.length == 4) {
            synchronized (C0027n.class) {
                if (mBaseCacheSize < 10) {
                    objArr[0] = mBaseCache;
                    objArr[1] = iArr;
                    for (int i3 = (i << 1) - 1; i3 >= 2; i3--) {
                        objArr[i3] = null;
                    }
                    mBaseCache = objArr;
                    mBaseCacheSize++;
                }
            }
        }
    }

    /* renamed from: ob */
    private void m29ob(int i) {
        if (i == 8) {
            synchronized (C0027n.class) {
                if (mTwiceBaseCache != null) {
                    Object[] objArr = mTwiceBaseCache;
                    this.mArray = objArr;
                    mTwiceBaseCache = (Object[]) objArr[0];
                    this.mHashes = (int[]) objArr[1];
                    objArr[1] = null;
                    objArr[0] = null;
                    mTwiceBaseCacheSize--;
                    return;
                }
            }
        } else if (i == 4) {
            synchronized (C0027n.class) {
                if (mBaseCache != null) {
                    Object[] objArr2 = mBaseCache;
                    this.mArray = objArr2;
                    mBaseCache = (Object[]) objArr2[0];
                    this.mHashes = (int[]) objArr2[1];
                    objArr2[1] = null;
                    objArr2[0] = null;
                    mBaseCacheSize--;
                    return;
                }
            }
        }
        this.mHashes = new int[i];
        this.mArray = new Object[(i << 1)];
    }

    public void clear() {
        int i = this.mSize;
        if (i > 0) {
            int[] iArr = this.mHashes;
            Object[] objArr = this.mArray;
            this.mHashes = C0018e.f9Wn;
            this.mArray = C0018e.f11Yn;
            this.mSize = 0;
            m28a(iArr, objArr, i);
        }
        if (this.mSize > 0) {
            throw new ConcurrentModificationException();
        }
    }

    public boolean containsKey(Object obj) {
        return indexOfKey(obj) >= 0;
    }

    public boolean containsValue(Object obj) {
        return indexOfValue(obj) >= 0;
    }

    public void ensureCapacity(int i) {
        int i2 = this.mSize;
        int[] iArr = this.mHashes;
        if (iArr.length < i) {
            Object[] objArr = this.mArray;
            m29ob(i);
            if (this.mSize > 0) {
                System.arraycopy(iArr, 0, this.mHashes, 0, i2);
                System.arraycopy(objArr, 0, this.mArray, 0, i2 << 1);
            }
            m28a(iArr, objArr, i2);
        }
        if (this.mSize != i2) {
            throw new ConcurrentModificationException();
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof C0027n) {
            C0027n nVar = (C0027n) obj;
            if (this.mSize != nVar.mSize) {
                return false;
            }
            int i = 0;
            while (i < this.mSize) {
                try {
                    Object keyAt = keyAt(i);
                    Object valueAt = valueAt(i);
                    Object obj2 = nVar.get(keyAt);
                    if (valueAt == null) {
                        if (obj2 != null || !nVar.containsKey(keyAt)) {
                            return false;
                        }
                    } else if (!valueAt.equals(obj2)) {
                        return false;
                    }
                    i++;
                } catch (ClassCastException | NullPointerException unused) {
                    return false;
                }
            }
            return true;
        }
        if (obj instanceof Map) {
            Map map = (Map) obj;
            if (this.mSize != map.size()) {
                return false;
            }
            int i2 = 0;
            while (i2 < this.mSize) {
                try {
                    Object keyAt2 = keyAt(i2);
                    Object valueAt2 = valueAt(i2);
                    Object obj3 = map.get(keyAt2);
                    if (valueAt2 == null) {
                        if (obj3 != null || !map.containsKey(keyAt2)) {
                            return false;
                        }
                    } else if (!valueAt2.equals(obj3)) {
                        return false;
                    }
                    i2++;
                } catch (ClassCastException | NullPointerException unused2) {
                }
            }
            return true;
        }
        return false;
    }

    public Object get(Object obj) {
        int indexOfKey = indexOfKey(obj);
        if (indexOfKey >= 0) {
            return this.mArray[(indexOfKey << 1) + 1];
        }
        return null;
    }

    public Object getOrDefault(Object obj, Object obj2) {
        int indexOfKey = indexOfKey(obj);
        return indexOfKey >= 0 ? this.mArray[(indexOfKey << 1) + 1] : obj2;
    }

    public int hashCode() {
        int[] iArr = this.mHashes;
        Object[] objArr = this.mArray;
        int i = this.mSize;
        int i2 = 1;
        int i3 = 0;
        int i4 = 0;
        while (i3 < i) {
            Object obj = objArr[i2];
            i4 += (obj == null ? 0 : obj.hashCode()) ^ iArr[i3];
            i3++;
            i2 += 2;
        }
        return i4;
    }

    /* access modifiers changed from: package-private */
    public int indexOf(Object obj, int i) {
        int i2 = this.mSize;
        if (i2 == 0) {
            return -1;
        }
        try {
            int binarySearch = C0018e.binarySearch(this.mHashes, i2, i);
            if (binarySearch < 0 || obj.equals(this.mArray[binarySearch << 1])) {
                return binarySearch;
            }
            int i3 = binarySearch + 1;
            while (i3 < i2 && this.mHashes[i3] == i) {
                if (obj.equals(this.mArray[i3 << 1])) {
                    return i3;
                }
                i3++;
            }
            int i4 = binarySearch - 1;
            while (i4 >= 0 && this.mHashes[i4] == i) {
                if (obj.equals(this.mArray[i4 << 1])) {
                    return i4;
                }
                i4--;
            }
            return ~i3;
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new ConcurrentModificationException();
        }
    }

    public int indexOfKey(Object obj) {
        return obj == null ? indexOfNull() : indexOf(obj, obj.hashCode());
    }

    /* access modifiers changed from: package-private */
    public int indexOfNull() {
        int i = this.mSize;
        if (i == 0) {
            return -1;
        }
        try {
            int binarySearch = C0018e.binarySearch(this.mHashes, i, 0);
            if (binarySearch < 0 || this.mArray[binarySearch << 1] == null) {
                return binarySearch;
            }
            int i2 = binarySearch + 1;
            while (i2 < i && this.mHashes[i2] == 0) {
                if (this.mArray[i2 << 1] == null) {
                    return i2;
                }
                i2++;
            }
            int i3 = binarySearch - 1;
            while (i3 >= 0 && this.mHashes[i3] == 0) {
                if (this.mArray[i3 << 1] == null) {
                    return i3;
                }
                i3--;
            }
            return ~i2;
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new ConcurrentModificationException();
        }
    }

    /* access modifiers changed from: package-private */
    public int indexOfValue(Object obj) {
        int i = this.mSize * 2;
        Object[] objArr = this.mArray;
        if (obj == null) {
            for (int i2 = 1; i2 < i; i2 += 2) {
                if (objArr[i2] == null) {
                    return i2 >> 1;
                }
            }
            return -1;
        }
        for (int i3 = 1; i3 < i; i3 += 2) {
            if (obj.equals(objArr[i3])) {
                return i3 >> 1;
            }
        }
        return -1;
    }

    public boolean isEmpty() {
        return this.mSize <= 0;
    }

    public Object keyAt(int i) {
        return this.mArray[i << 1];
    }

    public Object put(Object obj, Object obj2) {
        int i;
        int i2;
        int i3 = this.mSize;
        if (obj == null) {
            i2 = indexOfNull();
            i = 0;
        } else {
            int hashCode = obj.hashCode();
            i = hashCode;
            i2 = indexOf(obj, hashCode);
        }
        if (i2 >= 0) {
            int i4 = (i2 << 1) + 1;
            Object[] objArr = this.mArray;
            Object obj3 = objArr[i4];
            objArr[i4] = obj2;
            return obj3;
        }
        int i5 = ~i2;
        if (i3 >= this.mHashes.length) {
            int i6 = 4;
            if (i3 >= 8) {
                i6 = (i3 >> 1) + i3;
            } else if (i3 >= 4) {
                i6 = 8;
            }
            int[] iArr = this.mHashes;
            Object[] objArr2 = this.mArray;
            m29ob(i6);
            if (i3 == this.mSize) {
                int[] iArr2 = this.mHashes;
                if (iArr2.length > 0) {
                    System.arraycopy(iArr, 0, iArr2, 0, iArr.length);
                    System.arraycopy(objArr2, 0, this.mArray, 0, objArr2.length);
                }
                m28a(iArr, objArr2, i3);
            } else {
                throw new ConcurrentModificationException();
            }
        }
        if (i5 < i3) {
            int[] iArr3 = this.mHashes;
            int i7 = i5 + 1;
            System.arraycopy(iArr3, i5, iArr3, i7, i3 - i5);
            Object[] objArr3 = this.mArray;
            System.arraycopy(objArr3, i5 << 1, objArr3, i7 << 1, (this.mSize - i5) << 1);
        }
        int i8 = this.mSize;
        if (i3 == i8) {
            int[] iArr4 = this.mHashes;
            if (i5 < iArr4.length) {
                iArr4[i5] = i;
                Object[] objArr4 = this.mArray;
                int i9 = i5 << 1;
                objArr4[i9] = obj;
                objArr4[i9 + 1] = obj2;
                this.mSize = i8 + 1;
                return null;
            }
        }
        throw new ConcurrentModificationException();
    }

    public Object putIfAbsent(Object obj, Object obj2) {
        Object obj3 = get(obj);
        return obj3 == null ? put(obj, obj2) : obj3;
    }

    public Object remove(Object obj) {
        int indexOfKey = indexOfKey(obj);
        if (indexOfKey >= 0) {
            return removeAt(indexOfKey);
        }
        return null;
    }

    public Object removeAt(int i) {
        int i2;
        Object[] objArr = this.mArray;
        int i3 = i << 1;
        Object obj = objArr[i3 + 1];
        int i4 = this.mSize;
        if (i4 <= 1) {
            m28a(this.mHashes, objArr, i4);
            this.mHashes = C0018e.f9Wn;
            this.mArray = C0018e.f11Yn;
            i2 = 0;
        } else {
            i2 = i4 - 1;
            int[] iArr = this.mHashes;
            int i5 = 8;
            if (iArr.length <= 8 || i4 >= iArr.length / 3) {
                if (i < i2) {
                    int[] iArr2 = this.mHashes;
                    int i6 = i + 1;
                    int i7 = i2 - i;
                    System.arraycopy(iArr2, i6, iArr2, i, i7);
                    Object[] objArr2 = this.mArray;
                    System.arraycopy(objArr2, i6 << 1, objArr2, i3, i7 << 1);
                }
                Object[] objArr3 = this.mArray;
                int i8 = i2 << 1;
                objArr3[i8] = null;
                objArr3[i8 + 1] = null;
            } else {
                if (i4 > 8) {
                    i5 = i4 + (i4 >> 1);
                }
                int[] iArr3 = this.mHashes;
                Object[] objArr4 = this.mArray;
                m29ob(i5);
                if (i4 == this.mSize) {
                    if (i > 0) {
                        System.arraycopy(iArr3, 0, this.mHashes, 0, i);
                        System.arraycopy(objArr4, 0, this.mArray, 0, i3);
                    }
                    if (i < i2) {
                        int i9 = i + 1;
                        int i10 = i2 - i;
                        System.arraycopy(iArr3, i9, this.mHashes, i, i10);
                        System.arraycopy(objArr4, i9 << 1, this.mArray, i3, i10 << 1);
                    }
                } else {
                    throw new ConcurrentModificationException();
                }
            }
        }
        if (i4 == this.mSize) {
            this.mSize = i2;
            return obj;
        }
        throw new ConcurrentModificationException();
    }

    public Object replace(Object obj, Object obj2) {
        int indexOfKey = indexOfKey(obj);
        if (indexOfKey >= 0) {
            return setValueAt(indexOfKey, obj2);
        }
        return null;
    }

    public Object setValueAt(int i, Object obj) {
        int i2 = (i << 1) + 1;
        Object[] objArr = this.mArray;
        Object obj2 = objArr[i2];
        objArr[i2] = obj;
        return obj2;
    }

    public int size() {
        return this.mSize;
    }

    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder(this.mSize * 28);
        sb.append('{');
        for (int i = 0; i < this.mSize; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object keyAt = keyAt(i);
            if (keyAt != this) {
                sb.append(keyAt);
            } else {
                sb.append("(this Map)");
            }
            sb.append('=');
            Object valueAt = valueAt(i);
            if (valueAt != this) {
                sb.append(valueAt);
            } else {
                sb.append("(this Map)");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    public Object valueAt(int i) {
        return this.mArray[(i << 1) + 1];
    }

    public boolean remove(Object obj, Object obj2) {
        int indexOfKey = indexOfKey(obj);
        if (indexOfKey < 0) {
            return false;
        }
        Object valueAt = valueAt(indexOfKey);
        if (obj2 != valueAt && (obj2 == null || !obj2.equals(valueAt))) {
            return false;
        }
        removeAt(indexOfKey);
        return true;
    }

    public boolean replace(Object obj, Object obj2, Object obj3) {
        int indexOfKey = indexOfKey(obj);
        if (indexOfKey < 0) {
            return false;
        }
        Object valueAt = valueAt(indexOfKey);
        if (valueAt != obj2 && (obj2 == null || !obj2.equals(valueAt))) {
            return false;
        }
        setValueAt(indexOfKey, obj3);
        return true;
    }

    public C0027n(int i) {
        if (i == 0) {
            this.mHashes = C0018e.f9Wn;
            this.mArray = C0018e.f11Yn;
        } else {
            m29ob(i);
        }
        this.mSize = 0;
    }
}
