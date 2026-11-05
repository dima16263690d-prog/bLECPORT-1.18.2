/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;

public class VertexMultiConsumer {
    public static VertexConsumer create() {
        throw new IllegalArgumentException();
    }

    public static VertexConsumer create(VertexConsumer vertexConsumer) {
        return vertexConsumer;
    }

    public static VertexConsumer create(VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2) {
        return new Double(vertexConsumer, vertexConsumer2);
    }

    public static VertexConsumer create(VertexConsumer ... vertexConsumerArray) {
        return new Multiple(vertexConsumerArray);
    }

    static class Double
    implements VertexConsumer {
        private final VertexConsumer first;
        private final VertexConsumer second;

        public Double(VertexConsumer vertexConsumer, VertexConsumer vertexConsumer2) {
            if (vertexConsumer == vertexConsumer2) {
                throw new IllegalArgumentException("Duplicate delegates");
            }
            this.first = vertexConsumer;
            this.second = vertexConsumer2;
        }

        @Override
        public VertexConsumer vertex(double d, double d2, double d3) {
            this.first.vertex(d, d2, d3);
            this.second.vertex(d, d2, d3);
            return this;
        }

        @Override
        public VertexConsumer color(int n, int n2, int n3, int n4) {
            this.first.color(n, n2, n3, n4);
            this.second.color(n, n2, n3, n4);
            return this;
        }

        @Override
        public VertexConsumer uv(float f, float f2) {
            this.first.uv(f, f2);
            this.second.uv(f, f2);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int n, int n2) {
            this.first.overlayCoords(n, n2);
            this.second.overlayCoords(n, n2);
            return this;
        }

        @Override
        public VertexConsumer uv2(int n, int n2) {
            this.first.uv2(n, n2);
            this.second.uv2(n, n2);
            return this;
        }

        @Override
        public VertexConsumer normal(float f, float f2, float f3) {
            this.first.normal(f, f2, f3);
            this.second.normal(f, f2, f3);
            return this;
        }

        @Override
        public void vertex(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int n, int n2, float f10, float f11, float f12) {
            this.first.vertex(f, f2, f3, f4, f5, f6, f7, f8, f9, n, n2, f10, f11, f12);
            this.second.vertex(f, f2, f3, f4, f5, f6, f7, f8, f9, n, n2, f10, f11, f12);
        }

        @Override
        public void endVertex() {
            this.first.endVertex();
            this.second.endVertex();
        }

        @Override
        public void defaultColor(int n, int n2, int n3, int n4) {
            this.first.defaultColor(n, n2, n3, n4);
            this.second.defaultColor(n, n2, n3, n4);
        }

        @Override
        public void unsetDefaultColor() {
            this.first.unsetDefaultColor();
            this.second.unsetDefaultColor();
        }
    }

    static class Multiple
    implements VertexConsumer {
        private final VertexConsumer[] delegates;

        public Multiple(VertexConsumer[] vertexConsumerArray) {
            for (int i = 0; i < vertexConsumerArray.length; ++i) {
                for (int j = i + 1; j < vertexConsumerArray.length; ++j) {
                    if (vertexConsumerArray[i] != vertexConsumerArray[j]) continue;
                    throw new IllegalArgumentException("Duplicate delegates");
                }
            }
            this.delegates = vertexConsumerArray;
        }

        private void forEach(Consumer<VertexConsumer> consumer) {
            for (VertexConsumer vertexConsumer : this.delegates) {
                consumer.accept(vertexConsumer);
            }
        }

        @Override
        public VertexConsumer vertex(double d, double d2, double d3) {
            this.forEach(vertexConsumer -> vertexConsumer.vertex(d, d2, d3));
            return this;
        }

        @Override
        public VertexConsumer color(int n, int n2, int n3, int n4) {
            this.forEach(vertexConsumer -> vertexConsumer.color(n, n2, n3, n4));
            return this;
        }

        @Override
        public VertexConsumer uv(float f, float f2) {
            this.forEach(vertexConsumer -> vertexConsumer.uv(f, f2));
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int n, int n2) {
            this.forEach(vertexConsumer -> vertexConsumer.overlayCoords(n, n2));
            return this;
        }

        @Override
        public VertexConsumer uv2(int n, int n2) {
            this.forEach(vertexConsumer -> vertexConsumer.uv2(n, n2));
            return this;
        }

        @Override
        public VertexConsumer normal(float f, float f2, float f3) {
            this.forEach(vertexConsumer -> vertexConsumer.normal(f, f2, f3));
            return this;
        }

        @Override
        public void vertex(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, int n, int n2, float f10, float f11, float f12) {
            this.forEach(vertexConsumer -> vertexConsumer.vertex(f, f2, f3, f4, f5, f6, f7, f8, f9, n, n2, f10, f11, f12));
        }

        @Override
        public void endVertex() {
            this.forEach(VertexConsumer::endVertex);
        }

        @Override
        public void defaultColor(int n, int n2, int n3, int n4) {
            this.forEach(vertexConsumer -> vertexConsumer.defaultColor(n, n2, n3, n4));
        }

        @Override
        public void unsetDefaultColor() {
            this.forEach(VertexConsumer::unsetDefaultColor);
        }
    }
}

