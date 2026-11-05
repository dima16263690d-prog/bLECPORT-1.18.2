/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.math;

import com.mojang.math.Vector3f;
import net.minecraft.util.Mth;

public final class Quaternion {
    public static final Quaternion ONE = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
    private float i;
    private float j;
    private float k;
    private float r;

    public Quaternion(float f, float f2, float f3, float f4) {
        this.i = f;
        this.j = f2;
        this.k = f3;
        this.r = f4;
    }

    public Quaternion(Vector3f vector3f, float f, boolean bl) {
        if (bl) {
            f *= (float)Math.PI / 180;
        }
        float f2 = Quaternion.sin(f / 2.0f);
        this.i = vector3f.x() * f2;
        this.j = vector3f.y() * f2;
        this.k = vector3f.z() * f2;
        this.r = Quaternion.cos(f / 2.0f);
    }

    public Quaternion(float f, float f2, float f3, boolean bl) {
        if (bl) {
            f *= (float)Math.PI / 180;
            f2 *= (float)Math.PI / 180;
            f3 *= (float)Math.PI / 180;
        }
        float f4 = Quaternion.sin(0.5f * f);
        float f5 = Quaternion.cos(0.5f * f);
        float f6 = Quaternion.sin(0.5f * f2);
        float f7 = Quaternion.cos(0.5f * f2);
        float f8 = Quaternion.sin(0.5f * f3);
        float f9 = Quaternion.cos(0.5f * f3);
        this.i = f4 * f7 * f9 + f5 * f6 * f8;
        this.j = f5 * f6 * f9 - f4 * f7 * f8;
        this.k = f4 * f6 * f9 + f5 * f7 * f8;
        this.r = f5 * f7 * f9 - f4 * f6 * f8;
    }

    public Quaternion(Quaternion quaternion) {
        this.i = quaternion.i;
        this.j = quaternion.j;
        this.k = quaternion.k;
        this.r = quaternion.r;
    }

    public static Quaternion fromYXZ(float f, float f2, float f3) {
        Quaternion quaternion = ONE.copy();
        quaternion.mul(new Quaternion(0.0f, (float)Math.sin(f / 2.0f), 0.0f, (float)Math.cos(f / 2.0f)));
        quaternion.mul(new Quaternion((float)Math.sin(f2 / 2.0f), 0.0f, 0.0f, (float)Math.cos(f2 / 2.0f)));
        quaternion.mul(new Quaternion(0.0f, 0.0f, (float)Math.sin(f3 / 2.0f), (float)Math.cos(f3 / 2.0f)));
        return quaternion;
    }

    public static Quaternion fromXYZDegrees(Vector3f vector3f) {
        return Quaternion.fromXYZ((float)Math.toRadians(vector3f.x()), (float)Math.toRadians(vector3f.y()), (float)Math.toRadians(vector3f.z()));
    }

    public static Quaternion fromXYZ(Vector3f vector3f) {
        return Quaternion.fromXYZ(vector3f.x(), vector3f.y(), vector3f.z());
    }

    public static Quaternion fromXYZ(float f, float f2, float f3) {
        Quaternion quaternion = ONE.copy();
        quaternion.mul(new Quaternion((float)Math.sin(f / 2.0f), 0.0f, 0.0f, (float)Math.cos(f / 2.0f)));
        quaternion.mul(new Quaternion(0.0f, (float)Math.sin(f2 / 2.0f), 0.0f, (float)Math.cos(f2 / 2.0f)));
        quaternion.mul(new Quaternion(0.0f, 0.0f, (float)Math.sin(f3 / 2.0f), (float)Math.cos(f3 / 2.0f)));
        return quaternion;
    }

    public Vector3f toXYZ() {
        float f = this.r() * this.r();
        float f2 = this.i() * this.i();
        float f3 = this.j() * this.j();
        float f4 = this.k() * this.k();
        float f5 = f + f2 + f3 + f4;
        float f6 = 2.0f * this.r() * this.i() - 2.0f * this.j() * this.k();
        float f7 = (float)Math.asin(f6 / f5);
        if (Math.abs(f6) > 0.999f * f5) {
            return new Vector3f(2.0f * (float)Math.atan2(this.i(), this.r()), f7, 0.0f);
        }
        return new Vector3f((float)Math.atan2(2.0f * this.j() * this.k() + 2.0f * this.i() * this.r(), f - f2 - f3 + f4), f7, (float)Math.atan2(2.0f * this.i() * this.j() + 2.0f * this.r() * this.k(), f + f2 - f3 - f4));
    }

    public Vector3f toXYZDegrees() {
        Vector3f vector3f = this.toXYZ();
        return new Vector3f((float)Math.toDegrees(vector3f.x()), (float)Math.toDegrees(vector3f.y()), (float)Math.toDegrees(vector3f.z()));
    }

    public Vector3f toYXZ() {
        float f = this.r() * this.r();
        float f2 = this.i() * this.i();
        float f3 = this.j() * this.j();
        float f4 = this.k() * this.k();
        float f5 = f + f2 + f3 + f4;
        float f6 = 2.0f * this.r() * this.i() - 2.0f * this.j() * this.k();
        float f7 = (float)Math.asin(f6 / f5);
        if (Math.abs(f6) > 0.999f * f5) {
            return new Vector3f(f7, 2.0f * (float)Math.atan2(this.j(), this.r()), 0.0f);
        }
        return new Vector3f(f7, (float)Math.atan2(2.0f * this.i() * this.k() + 2.0f * this.j() * this.r(), f - f2 - f3 + f4), (float)Math.atan2(2.0f * this.i() * this.j() + 2.0f * this.r() * this.k(), f - f2 + f3 - f4));
    }

    public Vector3f toYXZDegrees() {
        Vector3f vector3f = this.toYXZ();
        return new Vector3f((float)Math.toDegrees(vector3f.x()), (float)Math.toDegrees(vector3f.y()), (float)Math.toDegrees(vector3f.z()));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Quaternion quaternion = (Quaternion)object;
        if (Float.compare(quaternion.i, this.i) != 0) {
            return false;
        }
        if (Float.compare(quaternion.j, this.j) != 0) {
            return false;
        }
        if (Float.compare(quaternion.k, this.k) != 0) {
            return false;
        }
        return Float.compare(quaternion.r, this.r) == 0;
    }

    public int hashCode() {
        int n = Float.floatToIntBits(this.i);
        n = 31 * n + Float.floatToIntBits(this.j);
        n = 31 * n + Float.floatToIntBits(this.k);
        n = 31 * n + Float.floatToIntBits(this.r);
        return n;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Quaternion[").append(this.r()).append(" + ");
        stringBuilder.append(this.i()).append("i + ");
        stringBuilder.append(this.j()).append("j + ");
        stringBuilder.append(this.k()).append("k]");
        return stringBuilder.toString();
    }

    public float i() {
        return this.i;
    }

    public float j() {
        return this.j;
    }

    public float k() {
        return this.k;
    }

    public float r() {
        return this.r;
    }

    public void mul(Quaternion quaternion) {
        float f = this.i();
        float f2 = this.j();
        float f3 = this.k();
        float f4 = this.r();
        float f5 = quaternion.i();
        float f6 = quaternion.j();
        float f7 = quaternion.k();
        float f8 = quaternion.r();
        this.i = f4 * f5 + f * f8 + f2 * f7 - f3 * f6;
        this.j = f4 * f6 - f * f7 + f2 * f8 + f3 * f5;
        this.k = f4 * f7 + f * f6 - f2 * f5 + f3 * f8;
        this.r = f4 * f8 - f * f5 - f2 * f6 - f3 * f7;
    }

    public void mul(float f) {
        this.i *= f;
        this.j *= f;
        this.k *= f;
        this.r *= f;
    }

    public void conj() {
        this.i = -this.i;
        this.j = -this.j;
        this.k = -this.k;
    }

    public void set(float f, float f2, float f3, float f4) {
        this.i = f;
        this.j = f2;
        this.k = f3;
        this.r = f4;
    }

    private static float cos(float f) {
        return (float)Math.cos(f);
    }

    private static float sin(float f) {
        return (float)Math.sin(f);
    }

    public void normalize() {
        float f = this.i() * this.i() + this.j() * this.j() + this.k() * this.k() + this.r() * this.r();
        if (f > 1.0E-6f) {
            float f2 = Mth.fastInvSqrt(f);
            this.i *= f2;
            this.j *= f2;
            this.k *= f2;
            this.r *= f2;
        } else {
            this.i = 0.0f;
            this.j = 0.0f;
            this.k = 0.0f;
            this.r = 0.0f;
        }
    }

    public void slerp(Quaternion quaternion, float f) {
        throw new UnsupportedOperationException();
    }

    public Quaternion copy() {
        return new Quaternion(this);
    }
}

