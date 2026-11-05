/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.culling;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.AABB;

public class Frustum {
    public static final int OFFSET_STEP = 4;
    private final Vector4f[] frustumData = new Vector4f[6];
    private Vector4f viewVector;
    private double camX;
    private double camY;
    private double camZ;

    public Frustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
        this.calculateFrustum(matrix4f, matrix4f2);
    }

    public Frustum(Frustum frustum) {
        System.arraycopy(frustum.frustumData, 0, this.frustumData, 0, frustum.frustumData.length);
        this.camX = frustum.camX;
        this.camY = frustum.camY;
        this.camZ = frustum.camZ;
        this.viewVector = frustum.viewVector;
    }

    public Frustum offsetToFullyIncludeCameraCube(int n) {
        double d = Math.floor(this.camX / (double)n) * (double)n;
        double d2 = Math.floor(this.camY / (double)n) * (double)n;
        double d3 = Math.floor(this.camZ / (double)n) * (double)n;
        double d4 = Math.ceil(this.camX / (double)n) * (double)n;
        double d5 = Math.ceil(this.camY / (double)n) * (double)n;
        double d6 = Math.ceil(this.camZ / (double)n) * (double)n;
        while (!this.cubeCompletelyInFrustum((float)(d - this.camX), (float)(d2 - this.camY), (float)(d3 - this.camZ), (float)(d4 - this.camX), (float)(d5 - this.camY), (float)(d6 - this.camZ))) {
            this.camX -= (double)(this.viewVector.x() * 4.0f);
            this.camY -= (double)(this.viewVector.y() * 4.0f);
            this.camZ -= (double)(this.viewVector.z() * 4.0f);
        }
        return this;
    }

    public void prepare(double d, double d2, double d3) {
        this.camX = d;
        this.camY = d2;
        this.camZ = d3;
    }

    private void calculateFrustum(Matrix4f matrix4f, Matrix4f matrix4f2) {
        Matrix4f matrix4f3 = matrix4f2.copy();
        matrix4f3.multiply(matrix4f);
        matrix4f3.transpose();
        this.viewVector = new Vector4f(0.0f, 0.0f, 1.0f, 0.0f);
        this.viewVector.transform(matrix4f3);
        this.getPlane(matrix4f3, -1, 0, 0, 0);
        this.getPlane(matrix4f3, 1, 0, 0, 1);
        this.getPlane(matrix4f3, 0, -1, 0, 2);
        this.getPlane(matrix4f3, 0, 1, 0, 3);
        this.getPlane(matrix4f3, 0, 0, -1, 4);
        this.getPlane(matrix4f3, 0, 0, 1, 5);
    }

    private void getPlane(Matrix4f matrix4f, int n, int n2, int n3, int n4) {
        Vector4f vector4f = new Vector4f(n, n2, n3, 1.0f);
        vector4f.transform(matrix4f);
        vector4f.normalize();
        this.frustumData[n4] = vector4f;
    }

    public boolean isVisible(AABB aABB) {
        return this.cubeInFrustum(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
    }

    private boolean cubeInFrustum(double d, double d2, double d3, double d4, double d5, double d6) {
        float f = (float)(d - this.camX);
        float f2 = (float)(d2 - this.camY);
        float f3 = (float)(d3 - this.camZ);
        float f4 = (float)(d4 - this.camX);
        float f5 = (float)(d5 - this.camY);
        float f6 = (float)(d6 - this.camZ);
        return this.cubeInFrustum(f, f2, f3, f4, f5, f6);
    }

    private boolean cubeInFrustum(float f, float f2, float f3, float f4, float f5, float f6) {
        for (int i = 0; i < 6; ++i) {
            Vector4f vector4f = this.frustumData[i];
            if (vector4f.dot(new Vector4f(f, f2, f3, 1.0f)) > 0.0f) continue;
            if (vector4f.dot(new Vector4f(f4, f2, f3, 1.0f)) > 0.0f) continue;
            if (vector4f.dot(new Vector4f(f, f5, f3, 1.0f)) > 0.0f) continue;
            if (vector4f.dot(new Vector4f(f4, f5, f3, 1.0f)) > 0.0f) continue;
            if (vector4f.dot(new Vector4f(f, f2, f6, 1.0f)) > 0.0f) continue;
            if (vector4f.dot(new Vector4f(f4, f2, f6, 1.0f)) > 0.0f) continue;
            if (vector4f.dot(new Vector4f(f, f5, f6, 1.0f)) > 0.0f) continue;
            if (vector4f.dot(new Vector4f(f4, f5, f6, 1.0f)) > 0.0f) continue;
            return false;
        }
        return true;
    }

    private boolean cubeCompletelyInFrustum(float f, float f2, float f3, float f4, float f5, float f6) {
        for (int i = 0; i < 6; ++i) {
            Vector4f vector4f = this.frustumData[i];
            Vector4f vector4f2 = new Vector4f(f, f2, f3, 1.0f);
            if (vector4f.dot(vector4f2) <= 0.0f) {
                return false;
            }
            Vector4f vector4f3 = new Vector4f(f4, f2, f3, 1.0f);
            if (vector4f.dot(vector4f3) <= 0.0f) {
                return false;
            }
            Vector4f vector4f4 = new Vector4f(f, f5, f3, 1.0f);
            if (vector4f.dot(vector4f4) <= 0.0f) {
                return false;
            }
            Vector4f vector4f5 = new Vector4f(f4, f5, f3, 1.0f);
            if (vector4f.dot(vector4f5) <= 0.0f) {
                return false;
            }
            Vector4f vector4f6 = new Vector4f(f, f2, f6, 1.0f);
            if (vector4f.dot(vector4f6) <= 0.0f) {
                return false;
            }
            Vector4f vector4f7 = new Vector4f(f4, f2, f6, 1.0f);
            if (vector4f.dot(vector4f7) <= 0.0f) {
                return false;
            }
            Vector4f vector4f8 = new Vector4f(f, f5, f6, 1.0f);
            if (vector4f.dot(vector4f8) <= 0.0f) {
                return false;
            }
            Vector4f vector4f9 = new Vector4f(f4, f5, f6, 1.0f);
            if (!(vector4f.dot(vector4f9) <= 0.0f)) continue;
            return false;
        }
        return true;
    }
}

