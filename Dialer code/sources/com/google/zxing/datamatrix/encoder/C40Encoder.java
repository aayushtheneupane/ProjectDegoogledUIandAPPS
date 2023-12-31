package com.google.zxing.datamatrix.encoder;

class C40Encoder implements Encoder {
    C40Encoder() {
    }

    private int backtrackOneCharacter(EncoderContext encoderContext, StringBuilder sb, StringBuilder sb2, int i) {
        int length = sb.length();
        sb.delete(length - i, length);
        encoderContext.pos--;
        int encodeChar = encodeChar(encoderContext.getCurrentChar(), sb2);
        encoderContext.resetSymbolInfo();
        return encodeChar;
    }

    static void writeNextTriplet(EncoderContext encoderContext, StringBuilder sb) {
        char charAt = sb.charAt(0);
        int charAt2 = (sb.charAt(1) * '(') + (charAt * 1600) + sb.charAt(2) + 1;
        encoderContext.writeCodewords(new String(new char[]{(char) (charAt2 / 256), (char) (charAt2 % 256)}));
        sb.delete(0, 3);
    }

    public void encode(EncoderContext encoderContext) {
        int lookAheadTest;
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (!encoderContext.hasMoreCharacters()) {
                break;
            }
            char currentChar = encoderContext.getCurrentChar();
            encoderContext.pos++;
            int encodeChar = encodeChar(currentChar, sb);
            int codewordCount = encoderContext.getCodewordCount() + ((sb.length() / 3) * 2);
            encoderContext.updateSymbolInfo(codewordCount);
            int dataCapacity = encoderContext.getSymbolInfo().getDataCapacity() - codewordCount;
            if (encoderContext.hasMoreCharacters()) {
                if (sb.length() % 3 == 0 && (lookAheadTest = HighLevelEncoder.lookAheadTest(encoderContext.getMessage(), encoderContext.pos, getEncodingMode())) != getEncodingMode()) {
                    encoderContext.signalEncoderChange(lookAheadTest);
                    break;
                }
            } else {
                StringBuilder sb2 = new StringBuilder();
                if (sb.length() % 3 == 2 && (dataCapacity < 2 || dataCapacity > 2)) {
                    encodeChar = backtrackOneCharacter(encoderContext, sb, sb2, encodeChar);
                }
                while (sb.length() % 3 == 1 && ((encodeChar <= 3 && dataCapacity != 1) || encodeChar > 3)) {
                    encodeChar = backtrackOneCharacter(encoderContext, sb, sb2, encodeChar);
                }
            }
        }
        handleEOD(encoderContext, sb);
    }

    /* access modifiers changed from: package-private */
    public int encodeChar(char c, StringBuilder sb) {
        if (c == ' ') {
            sb.append(3);
            return 1;
        } else if (c >= '0' && c <= '9') {
            sb.append((char) ((c - '0') + 4));
            return 1;
        } else if (c >= 'A' && c <= 'Z') {
            sb.append((char) ((c - 'A') + 14));
            return 1;
        } else if (c >= 0 && c <= 31) {
            sb.append(0);
            sb.append(c);
            return 2;
        } else if (c >= '!' && c <= '/') {
            sb.append(1);
            sb.append((char) (c - '!'));
            return 2;
        } else if (c >= ':' && c <= '@') {
            sb.append(1);
            sb.append((char) ((c - ':') + 15));
            return 2;
        } else if (c >= '[' && c <= '_') {
            sb.append(1);
            sb.append((char) ((c - '[') + 22));
            return 2;
        } else if (c >= '`' && c <= 127) {
            sb.append(2);
            sb.append((char) (c - '`'));
            return 2;
        } else if (c >= 128) {
            sb.append("\u0001\u001e");
            return encodeChar((char) (c - 128), sb) + 2;
        } else {
            throw new IllegalArgumentException("Illegal character: " + c);
        }
    }

    public int getEncodingMode() {
        return 1;
    }

    /* access modifiers changed from: package-private */
    public void handleEOD(EncoderContext encoderContext, StringBuilder sb) {
        int length = sb.length() % 3;
        int codewordCount = encoderContext.getCodewordCount() + ((sb.length() / 3) * 2);
        encoderContext.updateSymbolInfo(codewordCount);
        int dataCapacity = encoderContext.getSymbolInfo().getDataCapacity() - codewordCount;
        if (length == 2) {
            sb.append(0);
            while (sb.length() >= 3) {
                writeNextTriplet(encoderContext, sb);
            }
            if (encoderContext.hasMoreCharacters()) {
                encoderContext.writeCodeword(254);
            }
        } else if (dataCapacity == 1 && length == 1) {
            while (sb.length() >= 3) {
                writeNextTriplet(encoderContext, sb);
            }
            if (encoderContext.hasMoreCharacters()) {
                encoderContext.writeCodeword(254);
            }
            encoderContext.pos--;
        } else if (length == 0) {
            while (sb.length() >= 3) {
                writeNextTriplet(encoderContext, sb);
            }
            if (dataCapacity > 0 || encoderContext.hasMoreCharacters()) {
                encoderContext.writeCodeword(254);
            }
        } else {
            throw new IllegalStateException("Unexpected case. Please report!");
        }
        encoderContext.signalEncoderChange(0);
    }
}
