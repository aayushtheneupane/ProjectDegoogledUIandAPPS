package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import java.util.Arrays;
import java.util.Map;

public final class Code93Reader extends OneDReader {
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*".toCharArray();
    private static final int ASTERISK_ENCODING = CHARACTER_ENCODINGS[47];
    private static final int[] CHARACTER_ENCODINGS = {276, 328, 324, 322, 296, 292, 290, 336, 274, 266, 424, 420, 418, 404, 402, 394, 360, 356, 354, 308, 282, 344, 332, 326, 300, 278, 436, 434, 428, 422, 406, 410, 364, 358, 310, 314, 302, 468, 466, 458, 366, 374, 430, 294, 474, 470, 306, 350};
    private final int[] counters = new int[6];
    private final StringBuilder decodeRowResult = new StringBuilder(20);

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException {
        int[] findAsteriskPattern = findAsteriskPattern(bitArray);
        int nextSet = bitArray.getNextSet(findAsteriskPattern[1]);
        int size = bitArray.getSize();
        int[] iArr = this.counters;
        Arrays.fill(iArr, 0);
        StringBuilder sb = this.decodeRowResult;
        sb.setLength(0);
        while (true) {
            OneDReader.recordPattern(bitArray, nextSet, iArr);
            int pattern = toPattern(iArr);
            if (pattern >= 0) {
                char patternToChar = patternToChar(pattern);
                sb.append(patternToChar);
                int i2 = nextSet;
                for (int i3 : iArr) {
                    i2 += i3;
                }
                int nextSet2 = bitArray.getNextSet(i2);
                if (patternToChar == '*') {
                    sb.deleteCharAt(sb.length() - 1);
                    if (nextSet2 == size || !bitArray.get(nextSet2)) {
                        throw NotFoundException.getNotFoundInstance();
                    } else if (sb.length() >= 2) {
                        checkChecksums(sb);
                        sb.setLength(sb.length() - 2);
                        float f = (float) i;
                        return new Result(decodeExtended(sb), (byte[]) null, new ResultPoint[]{new ResultPoint(((float) (findAsteriskPattern[1] + findAsteriskPattern[0])) / 2.0f, f), new ResultPoint(((float) (nextSet2 + nextSet)) / 2.0f, f)}, BarcodeFormat.CODE_93);
                    } else {
                        throw NotFoundException.getNotFoundInstance();
                    }
                } else {
                    nextSet = nextSet2;
                }
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
    }

    private int[] findAsteriskPattern(BitArray bitArray) throws NotFoundException {
        int size = bitArray.getSize();
        int nextSet = bitArray.getNextSet(0);
        Arrays.fill(this.counters, 0);
        int[] iArr = this.counters;
        int length = iArr.length;
        boolean z = false;
        int i = 0;
        int i2 = nextSet;
        while (nextSet < size) {
            if (bitArray.get(nextSet) ^ z) {
                iArr[i] = iArr[i] + 1;
            } else {
                int i3 = length - 1;
                if (i != i3) {
                    i++;
                } else if (toPattern(iArr) == ASTERISK_ENCODING) {
                    return new int[]{i2, nextSet};
                } else {
                    i2 += iArr[0] + iArr[1];
                    int i4 = length - 2;
                    System.arraycopy(iArr, 2, iArr, 0, i4);
                    iArr[i4] = 0;
                    iArr[i3] = 0;
                    i--;
                }
                iArr[i] = 1;
                z = !z;
            }
            nextSet++;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int toPattern(int[] iArr) {
        int length = iArr.length;
        int i = 0;
        for (int i2 : iArr) {
            i += i2;
        }
        int i3 = 0;
        for (int i4 = 0; i4 < length; i4++) {
            int i5 = ((iArr[i4] << 8) * 9) / i;
            int i6 = i5 >> 8;
            if ((i5 & 255) > 127) {
                i6++;
            }
            if (i6 < 1 || i6 > 4) {
                return -1;
            }
            if ((i4 & 1) == 0) {
                int i7 = i3;
                for (int i8 = 0; i8 < i6; i8++) {
                    i7 = (i7 << 1) | 1;
                }
                i3 = i7;
            } else {
                i3 <<= i6;
            }
        }
        return i3;
    }

    private static char patternToChar(int i) throws NotFoundException {
        int i2 = 0;
        while (true) {
            int[] iArr = CHARACTER_ENCODINGS;
            if (i2 >= iArr.length) {
                throw NotFoundException.getNotFoundInstance();
            } else if (iArr[i2] == i) {
                return ALPHABET[i2];
            } else {
                i2++;
            }
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.lang.String decodeExtended(java.lang.CharSequence r8) throws com.google.zxing.FormatException {
        /*
            int r0 = r8.length()
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>(r0)
            r2 = 0
            r3 = r2
        L_0x000b:
            if (r3 >= r0) goto L_0x0080
            char r4 = r8.charAt(r3)
            r5 = 97
            if (r4 < r5) goto L_0x007a
            r5 = 100
            if (r4 > r5) goto L_0x007a
            int r5 = r0 + -1
            if (r3 >= r5) goto L_0x0075
            int r3 = r3 + 1
            char r5 = r8.charAt(r3)
            r6 = 90
            r7 = 65
            switch(r4) {
                case 97: goto L_0x0064;
                case 98: goto L_0x004b;
                case 99: goto L_0x0038;
                case 100: goto L_0x002c;
                default: goto L_0x002a;
            }
        L_0x002a:
            r4 = r2
            goto L_0x0071
        L_0x002c:
            if (r5 < r7) goto L_0x0033
            if (r5 > r6) goto L_0x0033
            int r5 = r5 + 32
            goto L_0x006a
        L_0x0033:
            com.google.zxing.FormatException r8 = com.google.zxing.FormatException.getFormatInstance()
            throw r8
        L_0x0038:
            if (r5 < r7) goto L_0x0041
            r4 = 79
            if (r5 > r4) goto L_0x0041
            int r5 = r5 + -32
            goto L_0x006a
        L_0x0041:
            if (r5 != r6) goto L_0x0046
            r4 = 58
            goto L_0x0071
        L_0x0046:
            com.google.zxing.FormatException r8 = com.google.zxing.FormatException.getFormatInstance()
            throw r8
        L_0x004b:
            if (r5 < r7) goto L_0x0054
            r4 = 69
            if (r5 > r4) goto L_0x0054
            int r5 = r5 + -38
            goto L_0x006a
        L_0x0054:
            r4 = 70
            if (r5 < r4) goto L_0x005f
            r4 = 87
            if (r5 > r4) goto L_0x005f
            int r5 = r5 + -11
            goto L_0x006a
        L_0x005f:
            com.google.zxing.FormatException r8 = com.google.zxing.FormatException.getFormatInstance()
            throw r8
        L_0x0064:
            if (r5 < r7) goto L_0x006c
            if (r5 > r6) goto L_0x006c
            int r5 = r5 + -64
        L_0x006a:
            char r4 = (char) r5
            goto L_0x0071
        L_0x006c:
            com.google.zxing.FormatException r8 = com.google.zxing.FormatException.getFormatInstance()
            throw r8
        L_0x0071:
            r1.append(r4)
            goto L_0x007d
        L_0x0075:
            com.google.zxing.FormatException r8 = com.google.zxing.FormatException.getFormatInstance()
            throw r8
        L_0x007a:
            r1.append(r4)
        L_0x007d:
            int r3 = r3 + 1
            goto L_0x000b
        L_0x0080:
            java.lang.String r8 = r1.toString()
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.oned.Code93Reader.decodeExtended(java.lang.CharSequence):java.lang.String");
    }

    private static void checkChecksums(CharSequence charSequence) throws ChecksumException {
        int length = charSequence.length();
        checkOneChecksum(charSequence, length - 2, 20);
        checkOneChecksum(charSequence, length - 1, 15);
    }

    private static void checkOneChecksum(CharSequence charSequence, int i, int i2) throws ChecksumException {
        int i3 = 0;
        int i4 = 1;
        for (int i5 = i - 1; i5 >= 0; i5--) {
            i3 += "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*".indexOf(charSequence.charAt(i5)) * i4;
            i4++;
            if (i4 > i2) {
                i4 = 1;
            }
        }
        if (charSequence.charAt(i) != ALPHABET[i3 % 47]) {
            throw ChecksumException.getChecksumInstance();
        }
    }
}
