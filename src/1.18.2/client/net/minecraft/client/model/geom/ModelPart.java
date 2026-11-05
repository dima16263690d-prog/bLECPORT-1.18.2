/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model.geom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;

public final class ModelPart {
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public boolean visible = true;
    private final List<Cube> cubes;
    private final Map<String, ModelPart> children;

    public ModelPart(List<Cube> list, Map<String, ModelPart> map) {
        this.cubes = list;
        this.children = map;
    }

    public PartPose storePose() {
        return PartPose.offsetAndRotation(this.x, this.y, this.z, this.xRot, this.yRot, this.zRot);
    }

    public void loadPose(PartPose partPose) {
        this.x = partPose.x;
        this.y = partPose.y;
        this.z = partPose.z;
        this.xRot = partPose.xRot;
        this.yRot = partPose.yRot;
        this.zRot = partPose.zRot;
    }

    public void copyFrom(ModelPart modelPart) {
        this.xRot = modelPart.xRot;
        this.yRot = modelPart.yRot;
        this.zRot = modelPart.zRot;
        this.x = modelPart.x;
        this.y = modelPart.y;
        this.z = modelPart.z;
    }

    public ModelPart getChild(String string) {
        ModelPart modelPart = this.children.get(string);
        if (modelPart == null) {
            throw new NoSuchElementException("Can't find part " + string);
        }
        return modelPart;
    }

    public void setPos(float f, float f2, float f3) {
        this.x = f;
        this.y = f2;
        this.z = f3;
    }

    public void setRotation(float f, float f2, float f3) {
        this.xRot = f;
        this.yRot = f2;
        this.zRot = f3;
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2) {
        this.render(poseStack, vertexConsumer, n, n2, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
        if (!this.visible) {
            return;
        }
        if (this.cubes.isEmpty() && this.children.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.translateAndRotate(poseStack);
        this.compile(poseStack.last(), vertexConsumer, n, n2, f, f2, f3, f4);
        for (ModelPart modelPart : this.children.values()) {
            modelPart.render(poseStack, vertexConsumer, n, n2, f, f2, f3, f4);
        }
        poseStack.popPose();
    }

    public void visit(PoseStack poseStack, Visitor visitor) {
        this.visit(poseStack, visitor, "");
    }

    private void visit(PoseStack poseStack, Visitor visitor, String string) {
        if (this.cubes.isEmpty() && this.children.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.translateAndRotate(poseStack);
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < this.cubes.size(); ++i) {
            visitor.visit(pose, string, i, this.cubes.get(i));
        }
        String string3 = string + "/";
        this.children.forEach((string2, modelPart) -> modelPart.visit(poseStack, visitor, string3 + string2));
        poseStack.popPose();
    }

    public void translateAndRotate(PoseStack poseStack) {
        poseStack.translate(this.x / 16.0f, this.y / 16.0f, this.z / 16.0f);
        if (this.zRot != 0.0f) {
            poseStack.mulPose(Vector3f.ZP.rotation(this.zRot));
        }
        if (this.yRot != 0.0f) {
            poseStack.mulPose(Vector3f.YP.rotation(this.yRot));
        }
        if (this.xRot != 0.0f) {
            poseStack.mulPose(Vector3f.XP.rotation(this.xRot));
        }
    }

    private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
        for (Cube cube : this.cubes) {
            cube.compile(pose, vertexConsumer, n, n2, f, f2, f3, f4);
        }
    }

    public Cube getRandomCube(Random random) {
        return this.cubes.get(random.nextInt(this.cubes.size()));
    }

    public boolean isEmpty() {
        return this.cubes.isEmpty();
    }

    public Stream<ModelPart> getAllParts() {
        return Stream.concat(Stream.of(this), this.children.values().stream().flatMap(ModelPart::getAllParts));
    }

    @FunctionalInterface
    public static interface Visitor {
        public void visit(PoseStack.Pose var1, String var2, int var3, Cube var4);
    }

    public static class Cube {
        private final Polygon[] polygons;
        public final float minX;
        public final float minY;
        public final float minZ;
        public final float maxX;
        public final float maxY;
        public final float maxZ;

        public Cube(int n, int n2, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, boolean bl, float f10, float f11) {
            this.minX = f;
            this.minY = f2;
            this.minZ = f3;
            this.maxX = f + f4;
            this.maxY = f2 + f5;
            this.maxZ = f3 + f6;
            this.polygons = new Polygon[6];
            float f12 = f + f4;
            float f13 = f2 + f5;
            float f14 = f3 + f6;
            f -= f7;
            f2 -= f8;
            f3 -= f9;
            f12 += f7;
            f13 += f8;
            f14 += f9;
            if (bl) {
                float f15 = f12;
                f12 = f;
                f = f15;
            }
            Vertex vertex = new Vertex(f, f2, f3, 0.0f, 0.0f);
            Vertex vertex2 = new Vertex(f12, f2, f3, 0.0f, 8.0f);
            Vertex vertex3 = new Vertex(f12, f13, f3, 8.0f, 8.0f);
            Vertex vertex4 = new Vertex(f, f13, f3, 8.0f, 0.0f);
            Vertex vertex5 = new Vertex(f, f2, f14, 0.0f, 0.0f);
            Vertex vertex6 = new Vertex(f12, f2, f14, 0.0f, 8.0f);
            Vertex vertex7 = new Vertex(f12, f13, f14, 8.0f, 8.0f);
            Vertex vertex8 = new Vertex(f, f13, f14, 8.0f, 0.0f);
            float f16 = n;
            float f17 = (float)n + f6;
            float f18 = (float)n + f6 + f4;
            float f19 = (float)n + f6 + f4 + f4;
            float f20 = (float)n + f6 + f4 + f6;
            float f21 = (float)n + f6 + f4 + f6 + f4;
            float f22 = n2;
            float f23 = (float)n2 + f6;
            float f24 = (float)n2 + f6 + f5;
            this.polygons[2] = new Polygon(new Vertex[]{vertex6, vertex5, vertex, vertex2}, f17, f22, f18, f23, f10, f11, bl, Direction.DOWN);
            this.polygons[3] = new Polygon(new Vertex[]{vertex3, vertex4, vertex8, vertex7}, f18, f23, f19, f22, f10, f11, bl, Direction.UP);
            this.polygons[1] = new Polygon(new Vertex[]{vertex, vertex5, vertex8, vertex4}, f16, f23, f17, f24, f10, f11, bl, Direction.WEST);
            this.polygons[4] = new Polygon(new Vertex[]{vertex2, vertex, vertex4, vertex3}, f17, f23, f18, f24, f10, f11, bl, Direction.NORTH);
            this.polygons[0] = new Polygon(new Vertex[]{vertex6, vertex2, vertex3, vertex7}, f18, f23, f20, f24, f10, f11, bl, Direction.EAST);
            this.polygons[5] = new Polygon(new Vertex[]{vertex5, vertex6, vertex7, vertex8}, f20, f23, f21, f24, f10, f11, bl, Direction.SOUTH);
        }

        public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();
            for (Polygon polygon : this.polygons) {
                Vector3f vector3f = polygon.normal.copy();
                vector3f.transform(matrix3f);
                float f5 = vector3f.x();
                float f6 = vector3f.y();
                float f7 = vector3f.z();
                for (Vertex vertex : polygon.vertices) {
                    float f8 = vertex.pos.x() / 16.0f;
                    float f9 = vertex.pos.y() / 16.0f;
                    float f10 = vertex.pos.z() / 16.0f;
                    Vector4f vector4f = new Vector4f(f8, f9, f10, 1.0f);
                    vector4f.transform(matrix4f);
                    vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f, f2, f3, f4, vertex.u, vertex.v, n2, n, f5, f6, f7);
                }
            }
        }
    }

    static class Vertex {
        public final Vector3f pos;
        public final float u;
        public final float v;

        public Vertex(float f, float f2, float f3, float f4, float f5) {
            this(new Vector3f(f, f2, f3), f4, f5);
        }

        public Vertex remap(float f, float f2) {
            return new Vertex(this.pos, f, f2);
        }

        public Vertex(Vector3f vector3f, float f, float f2) {
            this.pos = vector3f;
            this.u = f;
            this.v = f2;
        }
    }

    static class Polygon {
        public final Vertex[] vertices;
        public final Vector3f normal;

        public Polygon(Vertex[] vertexArray, float f, float f2, float f3, float f4, float f5, float f6, boolean bl, Direction direction) {
            this.vertices = vertexArray;
            float f7 = 0.0f / f5;
            float f8 = 0.0f / f6;
            vertexArray[0] = vertexArray[0].remap(f3 / f5 - f7, f2 / f6 + f8);
            vertexArray[1] = vertexArray[1].remap(f / f5 + f7, f2 / f6 + f8);
            vertexArray[2] = vertexArray[2].remap(f / f5 + f7, f4 / f6 - f8);
            vertexArray[3] = vertexArray[3].remap(f3 / f5 - f7, f4 / f6 - f8);
            if (bl) {
                int n = vertexArray.length;
                for (int i = 0; i < n / 2; ++i) {
                    Vertex vertex = vertexArray[i];
                    vertexArray[i] = vertexArray[n - 1 - i];
                    vertexArray[n - 1 - i] = vertex;
                }
            }
            this.normal = direction.step();
            if (bl) {
                this.normal.mul(-1.0f, 1.0f, 1.0f);
            }
        }
    }
}

