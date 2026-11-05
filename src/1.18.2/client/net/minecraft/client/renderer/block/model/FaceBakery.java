/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FaceBakery {
    public static final int VERTEX_INT_SIZE = 8;
    private static final float RESCALE_22_5 = 1.0f / (float)Math.cos(0.3926991f) - 1.0f;
    private static final float RESCALE_45 = 1.0f / (float)Math.cos(0.7853981852531433) - 1.0f;
    public static final int VERTEX_COUNT = 4;
    private static final int COLOR_INDEX = 3;
    public static final int UV_INDEX = 4;

    public BakedQuad bakeQuad(Vector3f vector3f, Vector3f vector3f2, BlockElementFace blockElementFace, TextureAtlasSprite textureAtlasSprite, Direction direction, ModelState modelState, @Nullable BlockElementRotation blockElementRotation, boolean bl, ResourceLocation resourceLocation) {
        BlockFaceUV blockFaceUV = blockElementFace.uv;
        if (modelState.isUvLocked()) {
            blockFaceUV = FaceBakery.recomputeUVs(blockElementFace.uv, direction, modelState.getRotation(), resourceLocation);
        }
        float[] fArray = new float[blockFaceUV.uvs.length];
        System.arraycopy(blockFaceUV.uvs, 0, fArray, 0, fArray.length);
        float f = textureAtlasSprite.uvShrinkRatio();
        float f2 = (blockFaceUV.uvs[0] + blockFaceUV.uvs[0] + blockFaceUV.uvs[2] + blockFaceUV.uvs[2]) / 4.0f;
        float f3 = (blockFaceUV.uvs[1] + blockFaceUV.uvs[1] + blockFaceUV.uvs[3] + blockFaceUV.uvs[3]) / 4.0f;
        blockFaceUV.uvs[0] = Mth.lerp(f, blockFaceUV.uvs[0], f2);
        blockFaceUV.uvs[2] = Mth.lerp(f, blockFaceUV.uvs[2], f2);
        blockFaceUV.uvs[1] = Mth.lerp(f, blockFaceUV.uvs[1], f3);
        blockFaceUV.uvs[3] = Mth.lerp(f, blockFaceUV.uvs[3], f3);
        int[] nArray = this.makeVertices(blockFaceUV, textureAtlasSprite, direction, this.setupShape(vector3f, vector3f2), modelState.getRotation(), blockElementRotation, bl);
        Direction direction2 = FaceBakery.calculateFacing(nArray);
        System.arraycopy(fArray, 0, blockFaceUV.uvs, 0, fArray.length);
        if (blockElementRotation == null) {
            this.recalculateWinding(nArray, direction2);
        }
        return new BakedQuad(nArray, blockElementFace.tintIndex, direction2, textureAtlasSprite, bl);
    }

    public static BlockFaceUV recomputeUVs(BlockFaceUV blockFaceUV, Direction direction, Transformation transformation, ResourceLocation resourceLocation) {
        float f;
        float f2;
        float f3;
        float f4;
        Matrix4f matrix4f = BlockMath.getUVLockTransform(transformation, direction, () -> "Unable to resolve UVLock for model: " + resourceLocation).getMatrix();
        float f5 = blockFaceUV.getU(blockFaceUV.getReverseIndex(0));
        float f6 = blockFaceUV.getV(blockFaceUV.getReverseIndex(0));
        Vector4f vector4f = new Vector4f(f5 / 16.0f, f6 / 16.0f, 0.0f, 1.0f);
        vector4f.transform(matrix4f);
        float f7 = 16.0f * vector4f.x();
        float f8 = 16.0f * vector4f.y();
        float f9 = blockFaceUV.getU(blockFaceUV.getReverseIndex(2));
        float f10 = blockFaceUV.getV(blockFaceUV.getReverseIndex(2));
        Vector4f vector4f2 = new Vector4f(f9 / 16.0f, f10 / 16.0f, 0.0f, 1.0f);
        vector4f2.transform(matrix4f);
        float f11 = 16.0f * vector4f2.x();
        float f12 = 16.0f * vector4f2.y();
        if (Math.signum(f9 - f5) == Math.signum(f11 - f7)) {
            f4 = f7;
            f3 = f11;
        } else {
            f4 = f11;
            f3 = f7;
        }
        if (Math.signum(f10 - f6) == Math.signum(f12 - f8)) {
            f2 = f8;
            f = f12;
        } else {
            f2 = f12;
            f = f8;
        }
        float f13 = (float)Math.toRadians(blockFaceUV.rotation);
        Vector3f vector3f = new Vector3f(Mth.cos(f13), Mth.sin(f13), 0.0f);
        Matrix3f matrix3f = new Matrix3f(matrix4f);
        vector3f.transform(matrix3f);
        int n = Math.floorMod(-((int)Math.round(Math.toDegrees(Math.atan2(vector3f.y(), vector3f.x())) / 90.0)) * 90, 360);
        return new BlockFaceUV(new float[]{f4, f2, f3, f}, n);
    }

    private int[] makeVertices(BlockFaceUV blockFaceUV, TextureAtlasSprite textureAtlasSprite, Direction direction, float[] fArray, Transformation transformation, @Nullable BlockElementRotation blockElementRotation, boolean bl) {
        int[] nArray = new int[32];
        for (int i = 0; i < 4; ++i) {
            this.bakeVertex(nArray, i, direction, blockFaceUV, fArray, textureAtlasSprite, transformation, blockElementRotation, bl);
        }
        return nArray;
    }

    private float[] setupShape(Vector3f vector3f, Vector3f vector3f2) {
        float[] fArray = new float[Direction.values().length];
        fArray[FaceInfo.Constants.MIN_X] = vector3f.x() / 16.0f;
        fArray[FaceInfo.Constants.MIN_Y] = vector3f.y() / 16.0f;
        fArray[FaceInfo.Constants.MIN_Z] = vector3f.z() / 16.0f;
        fArray[FaceInfo.Constants.MAX_X] = vector3f2.x() / 16.0f;
        fArray[FaceInfo.Constants.MAX_Y] = vector3f2.y() / 16.0f;
        fArray[FaceInfo.Constants.MAX_Z] = vector3f2.z() / 16.0f;
        return fArray;
    }

    private void bakeVertex(int[] nArray, int n, Direction direction, BlockFaceUV blockFaceUV, float[] fArray, TextureAtlasSprite textureAtlasSprite, Transformation transformation, @Nullable BlockElementRotation blockElementRotation, boolean bl) {
        FaceInfo.VertexInfo vertexInfo = FaceInfo.fromFacing(direction).getVertexInfo(n);
        Vector3f vector3f = new Vector3f(fArray[vertexInfo.xFace], fArray[vertexInfo.yFace], fArray[vertexInfo.zFace]);
        this.applyElementRotation(vector3f, blockElementRotation);
        this.applyModelRotation(vector3f, transformation);
        this.fillVertex(nArray, n, vector3f, textureAtlasSprite, blockFaceUV);
    }

    private void fillVertex(int[] nArray, int n, Vector3f vector3f, TextureAtlasSprite textureAtlasSprite, BlockFaceUV blockFaceUV) {
        int n2 = n * 8;
        nArray[n2] = Float.floatToRawIntBits(vector3f.x());
        nArray[n2 + 1] = Float.floatToRawIntBits(vector3f.y());
        nArray[n2 + 2] = Float.floatToRawIntBits(vector3f.z());
        nArray[n2 + 3] = -1;
        nArray[n2 + 4] = Float.floatToRawIntBits(textureAtlasSprite.getU(blockFaceUV.getU(n)));
        nArray[n2 + 4 + 1] = Float.floatToRawIntBits(textureAtlasSprite.getV(blockFaceUV.getV(n)));
    }

    private void applyElementRotation(Vector3f vector3f, @Nullable BlockElementRotation blockElementRotation) {
        Vector3f vector3f2;
        if (blockElementRotation == null) {
            return;
        }
        Vector3f vector3f3 = switch (blockElementRotation.axis) {
            case Direction.Axis.X -> {
                vector3f2 = Vector3f.XP;
                yield new Vector3f(0.0f, 1.0f, 1.0f);
            }
            case Direction.Axis.Y -> {
                vector3f2 = Vector3f.YP;
                yield new Vector3f(1.0f, 0.0f, 1.0f);
            }
            case Direction.Axis.Z -> {
                vector3f2 = Vector3f.ZP;
                yield new Vector3f(1.0f, 1.0f, 0.0f);
            }
            default -> throw new IllegalArgumentException("There are only 3 axes");
        };
        Quaternion quaternion = vector3f2.rotationDegrees(blockElementRotation.angle);
        if (blockElementRotation.rescale) {
            if (Math.abs(blockElementRotation.angle) == 22.5f) {
                vector3f3.mul(RESCALE_22_5);
            } else {
                vector3f3.mul(RESCALE_45);
            }
            vector3f3.add(1.0f, 1.0f, 1.0f);
        } else {
            vector3f3.set(1.0f, 1.0f, 1.0f);
        }
        this.rotateVertexBy(vector3f, blockElementRotation.origin.copy(), new Matrix4f(quaternion), vector3f3);
    }

    public void applyModelRotation(Vector3f vector3f, Transformation transformation) {
        if (transformation == Transformation.identity()) {
            return;
        }
        this.rotateVertexBy(vector3f, new Vector3f(0.5f, 0.5f, 0.5f), transformation.getMatrix(), new Vector3f(1.0f, 1.0f, 1.0f));
    }

    private void rotateVertexBy(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f, Vector3f vector3f3) {
        Vector4f vector4f = new Vector4f(vector3f.x() - vector3f2.x(), vector3f.y() - vector3f2.y(), vector3f.z() - vector3f2.z(), 1.0f);
        vector4f.transform(matrix4f);
        vector4f.mul(vector3f3);
        vector3f.set(vector4f.x() + vector3f2.x(), vector4f.y() + vector3f2.y(), vector4f.z() + vector3f2.z());
    }

    public static Direction calculateFacing(int[] nArray) {
        Vector3f vector3f = new Vector3f(Float.intBitsToFloat(nArray[0]), Float.intBitsToFloat(nArray[1]), Float.intBitsToFloat(nArray[2]));
        Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(nArray[8]), Float.intBitsToFloat(nArray[9]), Float.intBitsToFloat(nArray[10]));
        Vector3f vector3f3 = new Vector3f(Float.intBitsToFloat(nArray[16]), Float.intBitsToFloat(nArray[17]), Float.intBitsToFloat(nArray[18]));
        Vector3f vector3f4 = vector3f.copy();
        vector3f4.sub(vector3f2);
        Vector3f vector3f5 = vector3f3.copy();
        vector3f5.sub(vector3f2);
        Vector3f vector3f6 = vector3f5.copy();
        vector3f6.cross(vector3f4);
        vector3f6.normalize();
        Direction direction = null;
        float f = 0.0f;
        for (Direction direction2 : Direction.values()) {
            Vec3i vec3i = direction2.getNormal();
            Vector3f vector3f7 = new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
            float f2 = vector3f6.dot(vector3f7);
            if (!(f2 >= 0.0f) || !(f2 > f)) continue;
            f = f2;
            direction = direction2;
        }
        if (direction == null) {
            return Direction.UP;
        }
        return direction;
    }

    private void recalculateWinding(int[] nArray, Direction direction) {
        float f;
        int n;
        int[] nArray2 = new int[nArray.length];
        System.arraycopy(nArray, 0, nArray2, 0, nArray.length);
        float[] fArray = new float[Direction.values().length];
        fArray[FaceInfo.Constants.MIN_X] = 999.0f;
        fArray[FaceInfo.Constants.MIN_Y] = 999.0f;
        fArray[FaceInfo.Constants.MIN_Z] = 999.0f;
        fArray[FaceInfo.Constants.MAX_X] = -999.0f;
        fArray[FaceInfo.Constants.MAX_Y] = -999.0f;
        fArray[FaceInfo.Constants.MAX_Z] = -999.0f;
        for (int i = 0; i < 4; ++i) {
            n = 8 * i;
            float f2 = Float.intBitsToFloat(nArray2[n]);
            float f3 = Float.intBitsToFloat(nArray2[n + 1]);
            f = Float.intBitsToFloat(nArray2[n + 2]);
            if (f2 < fArray[FaceInfo.Constants.MIN_X]) {
                fArray[FaceInfo.Constants.MIN_X] = f2;
            }
            if (f3 < fArray[FaceInfo.Constants.MIN_Y]) {
                fArray[FaceInfo.Constants.MIN_Y] = f3;
            }
            if (f < fArray[FaceInfo.Constants.MIN_Z]) {
                fArray[FaceInfo.Constants.MIN_Z] = f;
            }
            if (f2 > fArray[FaceInfo.Constants.MAX_X]) {
                fArray[FaceInfo.Constants.MAX_X] = f2;
            }
            if (f3 > fArray[FaceInfo.Constants.MAX_Y]) {
                fArray[FaceInfo.Constants.MAX_Y] = f3;
            }
            if (!(f > fArray[FaceInfo.Constants.MAX_Z])) continue;
            fArray[FaceInfo.Constants.MAX_Z] = f;
        }
        FaceInfo faceInfo = FaceInfo.fromFacing(direction);
        for (n = 0; n < 4; ++n) {
            int n2 = 8 * n;
            FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(n);
            f = fArray[vertexInfo.xFace];
            float f4 = fArray[vertexInfo.yFace];
            float f5 = fArray[vertexInfo.zFace];
            nArray[n2] = Float.floatToRawIntBits(f);
            nArray[n2 + 1] = Float.floatToRawIntBits(f4);
            nArray[n2 + 2] = Float.floatToRawIntBits(f5);
            for (int i = 0; i < 4; ++i) {
                int n3 = 8 * i;
                float f6 = Float.intBitsToFloat(nArray2[n3]);
                float f7 = Float.intBitsToFloat(nArray2[n3 + 1]);
                float f8 = Float.intBitsToFloat(nArray2[n3 + 2]);
                if (!Mth.equal(f, f6) || !Mth.equal(f4, f7) || !Mth.equal(f5, f8)) continue;
                nArray[n2 + 4] = nArray2[n3 + 4];
                nArray[n2 + 4 + 1] = nArray2[n3 + 4 + 1];
            }
        }
    }
}

