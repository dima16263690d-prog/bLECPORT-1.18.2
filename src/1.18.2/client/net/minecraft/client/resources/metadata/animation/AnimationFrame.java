/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.metadata.animation;

public class AnimationFrame {
    public static final int UNKNOWN_FRAME_TIME = -1;
    private final int index;
    private final int time;

    public AnimationFrame(int n) {
        this(n, -1);
    }

    public AnimationFrame(int n, int n2) {
        this.index = n;
        this.time = n2;
    }

    public int getTime(int n) {
        return this.time == -1 ? n : this.time;
    }

    public int getIndex() {
        return this.index;
    }
}

