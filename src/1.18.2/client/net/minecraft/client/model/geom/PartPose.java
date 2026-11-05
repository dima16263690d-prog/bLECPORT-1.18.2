/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.geom;

public class PartPose {
    public static final PartPose ZERO = PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    public final float x;
    public final float y;
    public final float z;
    public final float xRot;
    public final float yRot;
    public final float zRot;

    private PartPose(float f, float f2, float f3, float f4, float f5, float f6) {
        this.x = f;
        this.y = f2;
        this.z = f3;
        this.xRot = f4;
        this.yRot = f5;
        this.zRot = f6;
    }

    public static PartPose offset(float f, float f2, float f3) {
        return PartPose.offsetAndRotation(f, f2, f3, 0.0f, 0.0f, 0.0f);
    }

    public static PartPose rotation(float f, float f2, float f3) {
        return PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, f, f2, f3);
    }

    public static PartPose offsetAndRotation(float f, float f2, float f3, float f4, float f5, float f6) {
        return new PartPose(f, f2, f3, f4, f5, f6);
    }
}

