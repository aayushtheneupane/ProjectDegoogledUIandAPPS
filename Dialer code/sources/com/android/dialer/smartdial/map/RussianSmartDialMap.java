package com.android.dialer.smartdial.map;

import android.support.p000v4.util.SimpleArrayMap;
import com.android.dialer.dialpadview.DialpadCharMappings;
import com.google.common.base.Optional;

final class RussianSmartDialMap extends SmartDialMap {
    private static RussianSmartDialMap instance;

    private RussianSmartDialMap() {
    }

    static RussianSmartDialMap getInstance() {
        if (instance == null) {
            instance = new RussianSmartDialMap();
        }
        return instance;
    }

    /* access modifiers changed from: package-private */
    public SimpleArrayMap<Character, Character> getCharToKeyMap() {
        return DialpadCharMappings.getCharToKeyMap("rus");
    }

    /* access modifiers changed from: package-private */
    public Optional<Character> normalizeCharacter(char c) {
        char lowerCase = Character.toLowerCase(c);
        return isValidDialpadAlphabeticChar(lowerCase) ? Optional.m67of(Character.valueOf(lowerCase)) : Optional.absent();
    }
}
