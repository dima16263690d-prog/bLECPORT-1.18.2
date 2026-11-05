/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.VisibleForDebug;

public final class DataLayer {
    public static final int LAYER_COUNT = 16;
    public static final int LAYER_SIZE = 128;
    public static final int SIZE = 2048;
    private static final int NIBBLE_SIZE = 4;
    @Nullable
    protected byte[] data;

    public DataLayer() {
    }

    public DataLayer(byte[] byArray) {
        this.data = byArray;
        if (byArray.length != 2048) {
            throw Util.pauseInIde(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + byArray.length));
        }
    }

    protected DataLayer(int n) {
        this.data = new byte[n];
    }

    public int get(int n, int n2, int n3) {
        return this.get(DataLayer.getIndex(n, n2, n3));
    }

    public void set(int n, int n2, int n3, int n4) {
        this.set(DataLayer.getIndex(n, n2, n3), n4);
    }

    private static int getIndex(int n, int n2, int n3) {
        return n2 << 8 | n3 << 4 | n;
    }

    private int get(int n) {
        if (this.data == null) {
            return 0;
        }
        int n2 = DataLayer.getByteIndex(n);
        int n3 = DataLayer.getNibbleIndex(n);
        return this.data[n2] >> 4 * n3 & 0xF;
    }

    private void set(int n, int n2) {
        if (this.data == null) {
            this.data = new byte[2048];
        }
        int n3 = DataLayer.getByteIndex(n);
        int n4 = DataLayer.getNibbleIndex(n);
        int n5 = ~(15 << 4 * n4);
        int n6 = (n2 & 0xF) << 4 * n4;
        this.data[n3] = (byte)(this.data[n3] & n5 | n6);
    }

    private static int getNibbleIndex(int n) {
        return n & 1;
    }

    private static int getByteIndex(int n) {
        return n >> 1;
    }

    public byte[] getData() {
        if (this.data == null) {
            this.data = new byte[2048];
        }
        return this.data;
    }

    public DataLayer copy() {
        if (this.data == null) {
            return new DataLayer();
        }
        return new DataLayer((byte[])this.data.clone());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4096; ++i) {
            stringBuilder.append(Integer.toHexString(this.get(i)));
            if ((i & 0xF) == 15) {
                stringBuilder.append("\n");
            }
            if ((i & 0xFF) != 255) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    @VisibleForDebug
    public String layerToString(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 256; ++i) {
            stringBuilder.append(Integer.toHexString(this.get(i)));
            if ((i & 0xF) != 15) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public boolean isEmpty() {
        return this.data == null;
    }
}

