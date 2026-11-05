/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class TextureAtlasSprite
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final TextureAtlas atlas;
    private final ResourceLocation name;
    final int width;
    final int height;
    protected final NativeImage[] mainImage;
    @Nullable
    private final AnimatedTexture animatedTexture;
    private final int x;
    private final int y;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;

    protected TextureAtlasSprite(TextureAtlas textureAtlas, Info info, int n, int n2, int n3, int n4, int n5, NativeImage nativeImage) {
        this.atlas = textureAtlas;
        this.width = info.width;
        this.height = info.height;
        this.name = info.name;
        this.x = n4;
        this.y = n5;
        this.u0 = (float)n4 / (float)n2;
        this.u1 = (float)(n4 + this.width) / (float)n2;
        this.v0 = (float)n5 / (float)n3;
        this.v1 = (float)(n5 + this.height) / (float)n3;
        this.animatedTexture = this.createTicker(info, nativeImage.getWidth(), nativeImage.getHeight(), n);
        try {
            try {
                this.mainImage = MipmapGenerator.generateMipLevels(nativeImage, n);
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Generating mipmaps for frame");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Frame being iterated");
                crashReportCategory.setDetail("First frame", () -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(", ");
                    }
                    stringBuilder.append(nativeImage.getWidth()).append("x").append(nativeImage.getHeight());
                    return stringBuilder.toString();
                });
                throw new ReportedException(crashReport);
            }
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Applying mipmap");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Sprite being mipmapped");
            crashReportCategory.setDetail("Sprite name", this.name::toString);
            crashReportCategory.setDetail("Sprite size", () -> this.width + " x " + this.height);
            crashReportCategory.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
            crashReportCategory.setDetail("Mipmap levels", n);
            throw new ReportedException(crashReport);
        }
    }

    private int getFrameCount() {
        return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
    }

    @Nullable
    private AnimatedTexture createTicker(Info info, int n3, int n4, int n5) {
        AnimationMetadataSection animationMetadataSection = info.metadata;
        int n6 = n3 / animationMetadataSection.getFrameWidth(info.width);
        int n7 = n4 / animationMetadataSection.getFrameHeight(info.height);
        int n8 = n6 * n7;
        ArrayList arrayList = Lists.newArrayList();
        animationMetadataSection.forEachFrame((n, n2) -> arrayList.add(new FrameInfo(n, n2)));
        if (arrayList.isEmpty()) {
            for (var10_10 = 0; var10_10 < n8; ++var10_10) {
                arrayList.add(new FrameInfo(var10_10, animationMetadataSection.getDefaultFrameTime()));
            }
        } else {
            Object object;
            var10_10 = 0;
            IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
            Iterator iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                object = (FrameInfo)iterator.next();
                boolean bl = true;
                if (((FrameInfo)object).time <= 0) {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", new Object[]{this.name, var10_10, ((FrameInfo)object).time});
                    bl = false;
                }
                if (((FrameInfo)object).index < 0 || ((FrameInfo)object).index >= n8) {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", new Object[]{this.name, var10_10, ((FrameInfo)object).index});
                    bl = false;
                }
                if (bl) {
                    intOpenHashSet.add(((FrameInfo)object).index);
                } else {
                    iterator.remove();
                }
                ++var10_10;
            }
            object = IntStream.range(0, n8).filter(arg_0 -> TextureAtlasSprite.lambda$createTicker$4((IntSet)intOpenHashSet, arg_0)).toArray();
            if (((Object)object).length > 0) {
                LOGGER.warn("Unused frames in sprite {}: {}", (Object)this.name, (Object)Arrays.toString((int[])object));
            }
        }
        if (arrayList.size() <= 1) {
            return null;
        }
        InterpolationData interpolationData = animationMetadataSection.isInterpolatedFrames() ? new InterpolationData(info, n5) : null;
        return new AnimatedTexture((List<FrameInfo>)ImmutableList.copyOf((Collection)arrayList), n6, interpolationData);
    }

    void upload(int n, int n2, NativeImage[] nativeImageArray) {
        for (int i = 0; i < this.mainImage.length; ++i) {
            nativeImageArray[i].upload(i, this.x >> i, this.y >> i, n >> i, n2 >> i, this.width >> i, this.height >> i, this.mainImage.length > 1, false);
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public float getU0() {
        return this.u0;
    }

    public float getU1() {
        return this.u1;
    }

    public float getU(double d) {
        float f = this.u1 - this.u0;
        return this.u0 + f * (float)d / 16.0f;
    }

    public float getUOffset(float f) {
        float f2 = this.u1 - this.u0;
        return (f - this.u0) / f2 * 16.0f;
    }

    public float getV0() {
        return this.v0;
    }

    public float getV1() {
        return this.v1;
    }

    public float getV(double d) {
        float f = this.v1 - this.v0;
        return this.v0 + f * (float)d / 16.0f;
    }

    public float getVOffset(float f) {
        float f2 = this.v1 - this.v0;
        return (f - this.v0) / f2 * 16.0f;
    }

    public ResourceLocation getName() {
        return this.name;
    }

    public TextureAtlas atlas() {
        return this.atlas;
    }

    public IntStream getUniqueFrames() {
        return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
    }

    @Override
    public void close() {
        for (NativeImage nativeImage : this.mainImage) {
            if (nativeImage == null) continue;
            nativeImage.close();
        }
        if (this.animatedTexture != null) {
            this.animatedTexture.close();
        }
    }

    public String toString() {
        return "TextureAtlasSprite{name='" + this.name + "', frameCount=" + this.getFrameCount() + ", x=" + this.x + ", y=" + this.y + ", height=" + this.height + ", width=" + this.width + ", u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + "}";
    }

    public boolean isTransparent(int n, int n2, int n3) {
        int n4 = n2;
        int n5 = n3;
        if (this.animatedTexture != null) {
            n4 += this.animatedTexture.getFrameX(n) * this.width;
            n5 += this.animatedTexture.getFrameY(n) * this.height;
        }
        return (this.mainImage[0].getPixelRGBA(n4, n5) >> 24 & 0xFF) == 0;
    }

    public void uploadFirstFrame() {
        if (this.animatedTexture != null) {
            this.animatedTexture.uploadFirstFrame();
        } else {
            this.upload(0, 0, this.mainImage);
        }
    }

    private float atlasSize() {
        float f = (float)this.width / (this.u1 - this.u0);
        float f2 = (float)this.height / (this.v1 - this.v0);
        return Math.max(f2, f);
    }

    public float uvShrinkRatio() {
        return 4.0f / this.atlasSize();
    }

    @Nullable
    public Tickable getAnimationTicker() {
        return this.animatedTexture;
    }

    public VertexConsumer wrap(VertexConsumer vertexConsumer) {
        return new SpriteCoordinateExpander(vertexConsumer, this);
    }

    private static /* synthetic */ boolean lambda$createTicker$4(IntSet intSet, int n) {
        return !intSet.contains(n);
    }

    public static final class Info {
        final ResourceLocation name;
        final int width;
        final int height;
        final AnimationMetadataSection metadata;

        public Info(ResourceLocation resourceLocation, int n, int n2, AnimationMetadataSection animationMetadataSection) {
            this.name = resourceLocation;
            this.width = n;
            this.height = n2;
            this.metadata = animationMetadataSection;
        }

        public ResourceLocation name() {
            return this.name;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }
    }

    class AnimatedTexture
    implements Tickable,
    AutoCloseable {
        int frame;
        int subFrame;
        final List<FrameInfo> frames;
        private final int frameRowSize;
        @Nullable
        private final InterpolationData interpolationData;

        AnimatedTexture(List<FrameInfo> list, @Nullable int n, InterpolationData interpolationData) {
            this.frames = list;
            this.frameRowSize = n;
            this.interpolationData = interpolationData;
        }

        int getFrameX(int n) {
            return n % this.frameRowSize;
        }

        int getFrameY(int n) {
            return n / this.frameRowSize;
        }

        private void uploadFrame(int n) {
            int n2 = this.getFrameX(n) * TextureAtlasSprite.this.width;
            int n3 = this.getFrameY(n) * TextureAtlasSprite.this.height;
            TextureAtlasSprite.this.upload(n2, n3, TextureAtlasSprite.this.mainImage);
        }

        @Override
        public void close() {
            if (this.interpolationData != null) {
                this.interpolationData.close();
            }
        }

        @Override
        public void tick() {
            ++this.subFrame;
            FrameInfo frameInfo = this.frames.get(this.frame);
            if (this.subFrame >= frameInfo.time) {
                int n = frameInfo.index;
                this.frame = (this.frame + 1) % this.frames.size();
                this.subFrame = 0;
                int n2 = this.frames.get((int)this.frame).index;
                if (n != n2) {
                    this.uploadFrame(n2);
                }
            } else if (this.interpolationData != null) {
                if (!RenderSystem.isOnRenderThread()) {
                    RenderSystem.recordRenderCall(() -> this.interpolationData.uploadInterpolatedFrame(this));
                } else {
                    this.interpolationData.uploadInterpolatedFrame(this);
                }
            }
        }

        public void uploadFirstFrame() {
            this.uploadFrame(this.frames.get((int)0).index);
        }

        public IntStream getUniqueFrames() {
            return this.frames.stream().mapToInt(frameInfo -> frameInfo.index).distinct();
        }
    }

    static class FrameInfo {
        final int index;
        final int time;

        FrameInfo(int n, int n2) {
            this.index = n;
            this.time = n2;
        }
    }

    final class InterpolationData
    implements AutoCloseable {
        private final NativeImage[] activeFrame;

        InterpolationData(Info info, int n) {
            this.activeFrame = new NativeImage[n + 1];
            for (int i = 0; i < this.activeFrame.length; ++i) {
                int n2 = info.width >> i;
                int n3 = info.height >> i;
                if (this.activeFrame[i] != null) continue;
                this.activeFrame[i] = new NativeImage(n2, n3, false);
            }
        }

        void uploadInterpolatedFrame(AnimatedTexture animatedTexture) {
            FrameInfo frameInfo = animatedTexture.frames.get(animatedTexture.frame);
            double d = 1.0 - (double)animatedTexture.subFrame / (double)frameInfo.time;
            int n = frameInfo.index;
            int n2 = animatedTexture.frames.get((int)((animatedTexture.frame + 1) % animatedTexture.frames.size())).index;
            if (n != n2) {
                for (int i = 0; i < this.activeFrame.length; ++i) {
                    int n3 = TextureAtlasSprite.this.width >> i;
                    int n4 = TextureAtlasSprite.this.height >> i;
                    for (int j = 0; j < n4; ++j) {
                        for (int k = 0; k < n3; ++k) {
                            int n5 = this.getPixel(animatedTexture, n, i, k, j);
                            int n6 = this.getPixel(animatedTexture, n2, i, k, j);
                            int n7 = this.mix(d, n5 >> 16 & 0xFF, n6 >> 16 & 0xFF);
                            int n8 = this.mix(d, n5 >> 8 & 0xFF, n6 >> 8 & 0xFF);
                            int n9 = this.mix(d, n5 & 0xFF, n6 & 0xFF);
                            this.activeFrame[i].setPixelRGBA(k, j, n5 & 0xFF000000 | n7 << 16 | n8 << 8 | n9);
                        }
                    }
                }
                TextureAtlasSprite.this.upload(0, 0, this.activeFrame);
            }
        }

        private int getPixel(AnimatedTexture animatedTexture, int n, int n2, int n3, int n4) {
            return TextureAtlasSprite.this.mainImage[n2].getPixelRGBA(n3 + (animatedTexture.getFrameX(n) * TextureAtlasSprite.this.width >> n2), n4 + (animatedTexture.getFrameY(n) * TextureAtlasSprite.this.height >> n2));
        }

        private int mix(double d, int n, int n2) {
            return (int)(d * (double)n + (1.0 - d) * (double)n2);
        }

        @Override
        public void close() {
            for (NativeImage nativeImage : this.activeFrame) {
                if (nativeImage == null) continue;
                nativeImage.close();
            }
        }
    }
}

