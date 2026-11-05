/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.shaders;

public enum FogShape {
    SPHERE(0),
    CYLINDER(1);

    private final int index;

    private FogShape(int n2) {
        this.index = n2;
    }

    public int getIndex() {
        return this.index;
    }
}

