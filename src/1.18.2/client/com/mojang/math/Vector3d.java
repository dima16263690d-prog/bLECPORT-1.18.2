/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.math;

public class Vector3d {
    public double x;
    public double y;
    public double z;

    public Vector3d(double d, double d2, double d3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
    }

    public void set(Vector3d vector3d) {
        this.x = vector3d.x;
        this.y = vector3d.y;
        this.z = vector3d.z;
    }

    public void set(double d, double d2, double d3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
    }

    public void scale(double d) {
        this.x *= d;
        this.y *= d;
        this.z *= d;
    }

    public void add(Vector3d vector3d) {
        this.x += vector3d.x;
        this.y += vector3d.y;
        this.z += vector3d.z;
    }
}

