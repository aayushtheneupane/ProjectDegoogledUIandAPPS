package com.google.protobuf;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ByteString implements Iterable<Byte>, Serializable {
    public static final ByteString EMPTY = new LiteralByteString(Internal.EMPTY_BYTE_ARRAY);
    private static final ByteArrayCopier byteArrayCopier;
    private int hash = 0;

    private interface ByteArrayCopier {
        byte[] copyFrom(byte[] bArr, int i, int i2);
    }

    public interface ByteIterator extends Iterator<Byte> {
    }

    public abstract byte byteAt(int i);

    /* access modifiers changed from: protected */
    public abstract void copyToInternal(byte[] bArr, int i, int i2, int i3);

    public abstract boolean equals(Object obj);

    public abstract CodedInputStream newCodedInput();

    /* access modifiers changed from: protected */
    public abstract int partialHash(int i, int i2, int i3);

    public abstract int size();

    public abstract ByteString substring(int i, int i2);

    /* access modifiers changed from: package-private */
    public abstract void writeTo(ByteOutput byteOutput) throws IOException;

    static {
        boolean z = true;
        try {
            Class.forName("android.content.Context");
        } catch (ClassNotFoundException unused) {
            z = false;
        }
        byteArrayCopier = z ? new SystemByteArrayCopier() : new ArraysByteArrayCopier();
    }

    private static final class SystemByteArrayCopier implements ByteArrayCopier {
        private SystemByteArrayCopier() {
        }

        public byte[] copyFrom(byte[] bArr, int i, int i2) {
            byte[] bArr2 = new byte[i2];
            System.arraycopy(bArr, i, bArr2, 0, i2);
            return bArr2;
        }
    }

    private static final class ArraysByteArrayCopier implements ByteArrayCopier {
        private ArraysByteArrayCopier() {
        }

        public byte[] copyFrom(byte[] bArr, int i, int i2) {
            return Arrays.copyOfRange(bArr, i, i2 + i);
        }
    }

    ByteString() {
    }

    public final ByteIterator iterator() {
        return new ByteIterator() {
            private final int limit = ByteString.this.size();
            private int position = 0;

            public boolean hasNext() {
                return this.position < this.limit;
            }

            public Byte next() {
                return Byte.valueOf(nextByte());
            }

            public byte nextByte() {
                try {
                    ByteString byteString = ByteString.this;
                    int i = this.position;
                    this.position = i + 1;
                    return byteString.byteAt(i);
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException(e.getMessage());
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static ByteString copyFrom(byte[] bArr, int i, int i2) {
        return new LiteralByteString(byteArrayCopier.copyFrom(bArr, i, i2));
    }

    static ByteString wrap(byte[] bArr) {
        return new LiteralByteString(bArr);
    }

    static ByteString wrap(byte[] bArr, int i, int i2) {
        return new BoundedByteString(bArr, i, i2);
    }

    public static ByteString copyFromUtf8(String str) {
        return new LiteralByteString(str.getBytes(Internal.UTF_8));
    }

    public final byte[] toByteArray() {
        int size = size();
        if (size == 0) {
            return Internal.EMPTY_BYTE_ARRAY;
        }
        byte[] bArr = new byte[size];
        copyToInternal(bArr, 0, 0, size);
        return bArr;
    }

    static abstract class LeafByteString extends ByteString {
        LeafByteString() {
        }
    }

    public final int hashCode() {
        int i = this.hash;
        if (i == 0) {
            int size = size();
            i = partialHash(size, 0, size);
            if (i == 0) {
                i = 1;
            }
            this.hash = i;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public final int peekCachedHashCode() {
        return this.hash;
    }

    static void checkIndex(int i, int i2) {
        if (((i2 - (i + 1)) | i) >= 0) {
            return;
        }
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Index < 0: " + i);
        }
        throw new ArrayIndexOutOfBoundsException("Index > length: " + i + ", " + i2);
    }

    static int checkRange(int i, int i2, int i3) {
        int i4 = i2 - i;
        if ((i | i2 | i4 | (i3 - i2)) >= 0) {
            return i4;
        }
        if (i < 0) {
            throw new IndexOutOfBoundsException("Beginning index: " + i + " < 0");
        } else if (i2 < i) {
            throw new IndexOutOfBoundsException("Beginning index larger than ending index: " + i + ", " + i2);
        } else {
            throw new IndexOutOfBoundsException("End index: " + i2 + " >= " + i3);
        }
    }

    public final String toString() {
        return String.format("<ByteString@%s size=%d>", new Object[]{Integer.toHexString(System.identityHashCode(this)), Integer.valueOf(size())});
    }

    private static class LiteralByteString extends LeafByteString {
        private static final long serialVersionUID = 1;
        protected final byte[] bytes;

        /* access modifiers changed from: protected */
        public int getOffsetIntoBytes() {
            return 0;
        }

        LiteralByteString(byte[] bArr) {
            this.bytes = bArr;
        }

        public byte byteAt(int i) {
            return this.bytes[i];
        }

        public int size() {
            return this.bytes.length;
        }

        public final ByteString substring(int i, int i2) {
            int checkRange = ByteString.checkRange(i, i2, size());
            if (checkRange == 0) {
                return ByteString.EMPTY;
            }
            return new BoundedByteString(this.bytes, getOffsetIntoBytes() + i, checkRange);
        }

        /* access modifiers changed from: protected */
        public void copyToInternal(byte[] bArr, int i, int i2, int i3) {
            System.arraycopy(this.bytes, i, bArr, i2, i3);
        }

        /* access modifiers changed from: package-private */
        public final void writeTo(ByteOutput byteOutput) throws IOException {
            byteOutput.writeLazy(this.bytes, getOffsetIntoBytes(), size());
        }

        public final boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ByteString) || size() != ((ByteString) obj).size()) {
                return false;
            }
            if (size() == 0) {
                return true;
            }
            if (!(obj instanceof LiteralByteString)) {
                return obj.equals(this);
            }
            LiteralByteString literalByteString = (LiteralByteString) obj;
            int peekCachedHashCode = peekCachedHashCode();
            int peekCachedHashCode2 = literalByteString.peekCachedHashCode();
            if (peekCachedHashCode == 0 || peekCachedHashCode2 == 0 || peekCachedHashCode == peekCachedHashCode2) {
                return equalsRange(literalByteString, 0, size());
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public final boolean equalsRange(ByteString byteString, int i, int i2) {
            if (i2 <= byteString.size()) {
                int i3 = i + i2;
                if (i3 > byteString.size()) {
                    throw new IllegalArgumentException("Ran off end of other: " + i + ", " + i2 + ", " + byteString.size());
                } else if (!(byteString instanceof LiteralByteString)) {
                    return byteString.substring(i, i3).equals(substring(0, i2));
                } else {
                    LiteralByteString literalByteString = (LiteralByteString) byteString;
                    byte[] bArr = this.bytes;
                    byte[] bArr2 = literalByteString.bytes;
                    int offsetIntoBytes = getOffsetIntoBytes() + i2;
                    int offsetIntoBytes2 = getOffsetIntoBytes();
                    int offsetIntoBytes3 = literalByteString.getOffsetIntoBytes() + i;
                    while (offsetIntoBytes2 < offsetIntoBytes) {
                        if (bArr[offsetIntoBytes2] != bArr2[offsetIntoBytes3]) {
                            return false;
                        }
                        offsetIntoBytes2++;
                        offsetIntoBytes3++;
                    }
                    return true;
                }
            } else {
                throw new IllegalArgumentException("Length too large: " + i2 + size());
            }
        }

        /* access modifiers changed from: protected */
        public final int partialHash(int i, int i2, int i3) {
            return Internal.partialHash(i, this.bytes, getOffsetIntoBytes() + i2, i3);
        }

        public final CodedInputStream newCodedInput() {
            return CodedInputStream.newInstance(this.bytes, getOffsetIntoBytes(), size(), true);
        }
    }

    private static final class BoundedByteString extends LiteralByteString {
        private static final long serialVersionUID = 1;
        private final int bytesLength;
        private final int bytesOffset;

        BoundedByteString(byte[] bArr, int i, int i2) {
            super(bArr);
            ByteString.checkRange(i, i + i2, bArr.length);
            this.bytesOffset = i;
            this.bytesLength = i2;
        }

        public byte byteAt(int i) {
            ByteString.checkIndex(i, size());
            return this.bytes[this.bytesOffset + i];
        }

        public int size() {
            return this.bytesLength;
        }

        /* access modifiers changed from: protected */
        public int getOffsetIntoBytes() {
            return this.bytesOffset;
        }

        /* access modifiers changed from: protected */
        public void copyToInternal(byte[] bArr, int i, int i2, int i3) {
            System.arraycopy(this.bytes, getOffsetIntoBytes() + i, bArr, i2, i3);
        }

        /* access modifiers changed from: package-private */
        public Object writeReplace() {
            return ByteString.wrap(toByteArray());
        }

        private void readObject(ObjectInputStream objectInputStream) throws IOException {
            throw new InvalidObjectException("BoundedByteStream instances are not to be serialized directly");
        }
    }
}
