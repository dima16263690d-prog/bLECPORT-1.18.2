/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient;

import java.util.Arrays;

public class KeyCombo {
    private final char[] chars;
    private int matchIndex;
    private final Runnable onCompletion;

    public KeyCombo(char[] cArray, Runnable runnable) {
        this.onCompletion = runnable;
        if (cArray.length < 1) {
            throw new IllegalArgumentException("Must have at least one char");
        }
        this.chars = cArray;
    }

    public KeyCombo(char[] cArray) {
        this(cArray, () -> {});
    }

    public boolean keyPressed(char c) {
        if (c == this.chars[this.matchIndex++]) {
            if (this.matchIndex == this.chars.length) {
                this.reset();
                this.onCompletion.run();
                return true;
            }
        } else {
            this.reset();
        }
        return false;
    }

    public void reset() {
        this.matchIndex = 0;
    }

    public String toString() {
        return "KeyCombo{chars=" + Arrays.toString(this.chars) + ", matchIndex=" + this.matchIndex + "}";
    }
}

