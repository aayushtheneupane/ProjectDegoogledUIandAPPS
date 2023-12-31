package com.google.zxing.aztec.encoder;

import androidx.appcompat.R$styleable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class HighLevelEncoder {
    private static final int[][] CHAR_MAP = ((int[][]) Array.newInstance(int.class, new int[]{5, 256}));
    static final int[][] LATCH_TABLE = {new int[]{0, 327708, 327710, 327709, 656318}, new int[]{590318, 0, 327710, 327709, 656318}, new int[]{262158, 590300, 0, 590301, 932798}, new int[]{327709, 327708, 656318, 0, 327710}, new int[]{327711, 656380, 656382, 656381, 0}};
    static final String[] MODE_NAMES = {"UPPER", "LOWER", "DIGIT", "MIXED", "PUNCT"};
    static final int[][] SHIFT_TABLE = ((int[][]) Array.newInstance(int.class, new int[]{6, 6}));
    private final byte[] text;

    static {
        CHAR_MAP[0][32] = 1;
        for (int i = 65; i <= 90; i++) {
            CHAR_MAP[0][i] = (i - 65) + 2;
        }
        CHAR_MAP[1][32] = 1;
        for (int i2 = 97; i2 <= 122; i2++) {
            CHAR_MAP[1][i2] = (i2 - 97) + 2;
        }
        CHAR_MAP[2][32] = 1;
        for (int i3 = 48; i3 <= 57; i3++) {
            CHAR_MAP[2][i3] = (i3 - 48) + 2;
        }
        int[][] iArr = CHAR_MAP;
        iArr[2][44] = 12;
        iArr[2][46] = 13;
        int[] iArr2 = {0, 32, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 27, 28, 29, 30, 31, 64, 92, 94, 95, 96, R$styleable.AppCompatTheme_windowNoTitle, 126, 127};
        for (int i4 = 0; i4 < iArr2.length; i4++) {
            CHAR_MAP[3][iArr2[i4]] = i4;
        }
        int[] iArr3 = {0, 13, 0, 0, 0, 0, 33, 39, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 58, 59, 60, 61, 62, 63, 91, 93, R$styleable.AppCompatTheme_windowMinWidthMinor, 125};
        for (int i5 = 0; i5 < iArr3.length; i5++) {
            if (iArr3[i5] > 0) {
                CHAR_MAP[4][iArr3[i5]] = i5;
            }
        }
        for (int[] fill : SHIFT_TABLE) {
            Arrays.fill(fill, -1);
        }
        int[][] iArr4 = SHIFT_TABLE;
        iArr4[0][4] = 0;
        iArr4[1][4] = 0;
        iArr4[1][0] = 28;
        iArr4[3][4] = 0;
        iArr4[2][4] = 0;
        iArr4[2][0] = 15;
    }

    public HighLevelEncoder(byte[] bArr) {
        this.text = bArr;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0042  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0049  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.google.zxing.common.BitArray encode() {
        /*
            r8 = this;
            com.google.zxing.aztec.encoder.State r0 = com.google.zxing.aztec.encoder.State.INITIAL_STATE
            java.util.List r0 = java.util.Collections.singletonList(r0)
            r1 = 0
            r2 = r0
            r0 = r1
        L_0x0009:
            byte[] r3 = r8.text
            int r4 = r3.length
            if (r0 >= r4) goto L_0x0050
            int r4 = r0 + 1
            int r5 = r3.length
            if (r4 >= r5) goto L_0x0016
            byte r3 = r3[r4]
            goto L_0x0017
        L_0x0016:
            r3 = r1
        L_0x0017:
            byte[] r5 = r8.text
            byte r5 = r5[r0]
            r6 = 13
            if (r5 == r6) goto L_0x003b
            r6 = 44
            r7 = 32
            if (r5 == r6) goto L_0x0037
            r6 = 46
            if (r5 == r6) goto L_0x0033
            r6 = 58
            if (r5 == r6) goto L_0x002f
        L_0x002d:
            r3 = r1
            goto L_0x0040
        L_0x002f:
            if (r3 != r7) goto L_0x002d
            r3 = 5
            goto L_0x0040
        L_0x0033:
            if (r3 != r7) goto L_0x002d
            r3 = 3
            goto L_0x0040
        L_0x0037:
            if (r3 != r7) goto L_0x002d
            r3 = 4
            goto L_0x0040
        L_0x003b:
            r5 = 10
            if (r3 != r5) goto L_0x002d
            r3 = 2
        L_0x0040:
            if (r3 <= 0) goto L_0x0049
            java.util.List r0 = updateStateListForPair(r2, r0, r3)
            r2 = r0
            r0 = r4
            goto L_0x004d
        L_0x0049:
            java.util.List r2 = r8.updateStateListForChar(r2, r0)
        L_0x004d:
            int r0 = r0 + 1
            goto L_0x0009
        L_0x0050:
            com.google.zxing.aztec.encoder.HighLevelEncoder$1 r0 = new com.google.zxing.aztec.encoder.HighLevelEncoder$1
            r0.<init>()
            java.lang.Object r0 = java.util.Collections.min(r2, r0)
            com.google.zxing.aztec.encoder.State r0 = (com.google.zxing.aztec.encoder.State) r0
            byte[] r8 = r8.text
            com.google.zxing.common.BitArray r8 = r0.toBitArray(r8)
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.zxing.aztec.encoder.HighLevelEncoder.encode():com.google.zxing.common.BitArray");
    }

    private List<State> updateStateListForChar(Iterable<State> iterable, int i) {
        LinkedList linkedList = new LinkedList();
        for (State updateStateForChar : iterable) {
            updateStateForChar(updateStateForChar, i, linkedList);
        }
        return simplifyStates(linkedList);
    }

    private void updateStateForChar(State state, int i, Collection<State> collection) {
        char c = (char) (this.text[i] & 255);
        boolean z = CHAR_MAP[state.getMode()][c] > 0;
        State state2 = null;
        for (int i2 = 0; i2 <= 4; i2++) {
            int i3 = CHAR_MAP[i2][c];
            if (i3 > 0) {
                if (state2 == null) {
                    state2 = state.endBinaryShift(i);
                }
                if (!z || i2 == state.getMode() || i2 == 2) {
                    collection.add(state2.latchAndAppend(i2, i3));
                }
                if (!z && SHIFT_TABLE[state.getMode()][i2] >= 0) {
                    collection.add(state2.shiftAndAppend(i2, i3));
                }
            }
        }
        if (state.getBinaryShiftByteCount() > 0 || CHAR_MAP[state.getMode()][c] == 0) {
            collection.add(state.addBinaryShiftChar(i));
        }
    }

    private static List<State> updateStateListForPair(Iterable<State> iterable, int i, int i2) {
        LinkedList linkedList = new LinkedList();
        for (State updateStateForPair : iterable) {
            updateStateForPair(updateStateForPair, i, i2, linkedList);
        }
        return simplifyStates(linkedList);
    }

    private static void updateStateForPair(State state, int i, int i2, Collection<State> collection) {
        State endBinaryShift = state.endBinaryShift(i);
        collection.add(endBinaryShift.latchAndAppend(4, i2));
        if (state.getMode() != 4) {
            collection.add(endBinaryShift.shiftAndAppend(4, i2));
        }
        if (i2 == 3 || i2 == 4) {
            collection.add(endBinaryShift.latchAndAppend(2, 16 - i2).latchAndAppend(2, 1));
        }
        if (state.getBinaryShiftByteCount() > 0) {
            collection.add(state.addBinaryShiftChar(i).addBinaryShiftChar(i + 1));
        }
    }

    private static List<State> simplifyStates(Iterable<State> iterable) {
        LinkedList linkedList = new LinkedList();
        for (State next : iterable) {
            boolean z = true;
            Iterator it = linkedList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                State state = (State) it.next();
                if (state.isBetterThanOrEqualTo(next)) {
                    z = false;
                    break;
                } else if (next.isBetterThanOrEqualTo(state)) {
                    it.remove();
                }
            }
            if (z) {
                linkedList.add(next);
            }
        }
        return linkedList;
    }
}
