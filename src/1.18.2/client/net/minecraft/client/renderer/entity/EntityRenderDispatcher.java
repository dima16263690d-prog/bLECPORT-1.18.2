/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EntityRenderDispatcher
implements ResourceManagerReloadListener {
    private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));
    private Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
    private Map<String, EntityRenderer<? extends Player>> playerRenderers = ImmutableMap.of();
    public final TextureManager textureManager;
    private Level level;
    public Camera camera;
    private Quaternion cameraOrientation;
    public Entity crosshairPickEntity;
    private final ItemRenderer itemRenderer;
    private final Font font;
    public final Options options;
    private final EntityModelSet entityModels;
    private boolean shouldRenderShadow = true;
    private boolean renderHitBoxes;

    public <E extends Entity> int getPackedLightCoords(E e, float f) {
        return this.getRenderer(e).getPackedLightCoords(e, f);
    }

    public EntityRenderDispatcher(TextureManager textureManager, ItemRenderer itemRenderer, Font font, Options options, EntityModelSet entityModelSet) {
        this.textureManager = textureManager;
        this.itemRenderer = itemRenderer;
        this.font = font;
        this.options = options;
        this.entityModels = entityModelSet;
    }

    public <T extends Entity> EntityRenderer<? super T> getRenderer(T t) {
        if (t instanceof AbstractClientPlayer) {
            String string = ((AbstractClientPlayer)t).getModelName();
            EntityRenderer<? extends Player> entityRenderer = this.playerRenderers.get(string);
            if (entityRenderer != null) {
                return entityRenderer;
            }
            return this.playerRenderers.get("default");
        }
        return this.renderers.get(t.getType());
    }

    public void prepare(Level level, Camera camera, Entity entity) {
        this.level = level;
        this.camera = camera;
        this.cameraOrientation = camera.rotation();
        this.crosshairPickEntity = entity;
    }

    public void overrideCameraOrientation(Quaternion quaternion) {
        this.cameraOrientation = quaternion;
    }

    public void setRenderShadow(boolean bl) {
        this.shouldRenderShadow = bl;
    }

    public void setRenderHitBoxes(boolean bl) {
        this.renderHitBoxes = bl;
    }

    public boolean shouldRenderHitBoxes() {
        return this.renderHitBoxes;
    }

    public <E extends Entity> boolean shouldRender(E e, Frustum frustum, double d, double d2, double d3) {
        EntityRenderer<E> entityRenderer = this.getRenderer(e);
        return entityRenderer.shouldRender(e, frustum, d, d2, d3);
    }

    public <E extends Entity> void render(E e, double d, double d2, double d3, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        EntityRenderer<E> entityRenderer = this.getRenderer(e);
        try {
            double d4;
            float f3;
            Vec3 vec3 = entityRenderer.getRenderOffset(e, f2);
            double d5 = d + vec3.x();
            double d6 = d2 + vec3.y();
            double d7 = d3 + vec3.z();
            poseStack.pushPose();
            poseStack.translate(d5, d6, d7);
            entityRenderer.render(e, f, f2, poseStack, multiBufferSource, n);
            if (e.displayFireAnimation()) {
                this.renderFlame(poseStack, multiBufferSource, e);
            }
            poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            if (this.options.entityShadows && this.shouldRenderShadow && entityRenderer.shadowRadius > 0.0f && !e.isInvisible() && (f3 = (float)((1.0 - (d4 = this.distanceToSqr(e.getX(), e.getY(), e.getZ())) / 256.0) * (double)entityRenderer.shadowStrength)) > 0.0f) {
                EntityRenderDispatcher.renderShadow(poseStack, multiBufferSource, e, f3, f2, this.level, entityRenderer.shadowRadius);
            }
            if (this.renderHitBoxes && !e.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
                EntityRenderDispatcher.renderHitbox(poseStack, multiBufferSource.getBuffer(RenderType.lines()), e, f2);
            }
            poseStack.popPose();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being rendered");
            e.fillCrashReportCategory(crashReportCategory);
            CrashReportCategory crashReportCategory2 = crashReport.addCategory("Renderer details");
            crashReportCategory2.setDetail("Assigned renderer", entityRenderer);
            crashReportCategory2.setDetail("Location", CrashReportCategory.formatLocation((LevelHeightAccessor)this.level, d, d2, d3));
            crashReportCategory2.setDetail("Rotation", Float.valueOf(f));
            crashReportCategory2.setDetail("Delta", Float.valueOf(f2));
            throw new ReportedException(crashReport);
        }
    }

    private static void renderHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, float f) {
        AABB aABB = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
        LevelRenderer.renderLineBox(poseStack, vertexConsumer, aABB, 1.0f, 1.0f, 1.0f, 1.0f);
        if (entity instanceof EnderDragon) {
            double d = -Mth.lerp((double)f, entity.xOld, entity.getX());
            double d2 = -Mth.lerp((double)f, entity.yOld, entity.getY());
            double d3 = -Mth.lerp((double)f, entity.zOld, entity.getZ());
            for (EnderDragonPart enderDragonPart : ((EnderDragon)entity).getSubEntities()) {
                poseStack.pushPose();
                double d4 = d + Mth.lerp((double)f, enderDragonPart.xOld, enderDragonPart.getX());
                double d5 = d2 + Mth.lerp((double)f, enderDragonPart.yOld, enderDragonPart.getY());
                double d6 = d3 + Mth.lerp((double)f, enderDragonPart.zOld, enderDragonPart.getZ());
                poseStack.translate(d4, d5, d6);
                LevelRenderer.renderLineBox(poseStack, vertexConsumer, enderDragonPart.getBoundingBox().move(-enderDragonPart.getX(), -enderDragonPart.getY(), -enderDragonPart.getZ()), 0.25f, 1.0f, 0.0f, 1.0f);
                poseStack.popPose();
            }
        }
        if (entity instanceof LivingEntity) {
            float f2 = 0.01f;
            LevelRenderer.renderLineBox(poseStack, vertexConsumer, aABB.minX, entity.getEyeHeight() - 0.01f, aABB.minZ, aABB.maxX, entity.getEyeHeight() + 0.01f, aABB.maxZ, 1.0f, 0.0f, 0.0f, 1.0f);
        }
        Vec3 vec3 = entity.getViewVector(f);
        Matrix4f matrix4f = poseStack.last().pose();
        Matrix3f matrix3f = poseStack.last().normal();
        vertexConsumer.vertex(matrix4f, 0.0f, entity.getEyeHeight(), 0.0f).color(0, 0, 255, 255).normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z).endVertex();
        vertexConsumer.vertex(matrix4f, (float)(vec3.x * 2.0), (float)((double)entity.getEyeHeight() + vec3.y * 2.0), (float)(vec3.z * 2.0)).color(0, 0, 255, 255).normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z).endVertex();
    }

    private void renderFlame(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity) {
        TextureAtlasSprite textureAtlasSprite = ModelBakery.FIRE_0.sprite();
        TextureAtlasSprite textureAtlasSprite2 = ModelBakery.FIRE_1.sprite();
        poseStack.pushPose();
        float f = entity.getBbWidth() * 1.4f;
        poseStack.scale(f, f, f);
        float f2 = 0.5f;
        float f3 = 0.0f;
        float f4 = entity.getBbHeight() / f;
        float f5 = 0.0f;
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-this.camera.getYRot()));
        poseStack.translate(0.0, 0.0, -0.3f + (float)((int)f4) * 0.02f);
        float f6 = 0.0f;
        int n = 0;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(Sheets.cutoutBlockSheet());
        PoseStack.Pose pose = poseStack.last();
        while (f4 > 0.0f) {
            TextureAtlasSprite textureAtlasSprite3 = n % 2 == 0 ? textureAtlasSprite : textureAtlasSprite2;
            float f7 = textureAtlasSprite3.getU0();
            float f8 = textureAtlasSprite3.getV0();
            float f9 = textureAtlasSprite3.getU1();
            float f10 = textureAtlasSprite3.getV1();
            if (n / 2 % 2 == 0) {
                float f11 = f9;
                f9 = f7;
                f7 = f11;
            }
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, f2 - 0.0f, 0.0f - f5, f6, f9, f10);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, -f2 - 0.0f, 0.0f - f5, f6, f7, f10);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, -f2 - 0.0f, 1.4f - f5, f6, f7, f8);
            EntityRenderDispatcher.fireVertex(pose, vertexConsumer, f2 - 0.0f, 1.4f - f5, f6, f9, f8);
            f4 -= 0.45f;
            f5 -= 0.45f;
            f2 *= 0.9f;
            f6 += 0.03f;
            ++n;
        }
        poseStack.popPose();
    }

    private static void fireVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float f2, float f3, float f4, float f5) {
        vertexConsumer.vertex(pose.pose(), f, f2, f3).color(255, 255, 255, 255).uv(f4, f5).overlayCoords(0, 10).uv2(240).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();
    }

    private static void renderShadow(PoseStack poseStack, MultiBufferSource multiBufferSource, Entity entity, float f, float f2, LevelReader levelReader, float f3) {
        Mob mob;
        float f4 = f3;
        if (entity instanceof Mob && (mob = (Mob)entity).isBaby()) {
            f4 *= 0.5f;
        }
        double d = Mth.lerp((double)f2, entity.xOld, entity.getX());
        double d2 = Mth.lerp((double)f2, entity.yOld, entity.getY());
        double d3 = Mth.lerp((double)f2, entity.zOld, entity.getZ());
        int n = Mth.floor(d - (double)f4);
        int n2 = Mth.floor(d + (double)f4);
        int n3 = Mth.floor(d2 - (double)f4);
        int n4 = Mth.floor(d2);
        int n5 = Mth.floor(d3 - (double)f4);
        int n6 = Mth.floor(d3 + (double)f4);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SHADOW_RENDER_TYPE);
        for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(n, n3, n5), new BlockPos(n2, n4, n6))) {
            EntityRenderDispatcher.renderBlockShadow(pose, vertexConsumer, levelReader, blockPos, d, d2, d3, f4, f);
        }
    }

    private static void renderBlockShadow(PoseStack.Pose pose, VertexConsumer vertexConsumer, LevelReader levelReader, BlockPos blockPos, double d, double d2, double d3, float f, float f2) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState = levelReader.getBlockState(blockPos2);
        if (blockState.getRenderShape() == RenderShape.INVISIBLE || levelReader.getMaxLocalRawBrightness(blockPos) <= 3) {
            return;
        }
        if (!blockState.isCollisionShapeFullBlock(levelReader, blockPos2)) {
            return;
        }
        VoxelShape voxelShape = blockState.getShape(levelReader, blockPos.below());
        if (voxelShape.isEmpty()) {
            return;
        }
        float f3 = (float)(((double)f2 - (d2 - (double)blockPos.getY()) / 2.0) * 0.5 * (double)levelReader.getBrightness(blockPos));
        if (f3 >= 0.0f) {
            if (f3 > 1.0f) {
                f3 = 1.0f;
            }
            AABB aABB = voxelShape.bounds();
            double d4 = (double)blockPos.getX() + aABB.minX;
            double d5 = (double)blockPos.getX() + aABB.maxX;
            double d6 = (double)blockPos.getY() + aABB.minY;
            double d7 = (double)blockPos.getZ() + aABB.minZ;
            double d8 = (double)blockPos.getZ() + aABB.maxZ;
            float f4 = (float)(d4 - d);
            float f5 = (float)(d5 - d);
            float f6 = (float)(d6 - d2);
            float f7 = (float)(d7 - d3);
            float f8 = (float)(d8 - d3);
            float f9 = -f4 / 2.0f / f + 0.5f;
            float f10 = -f5 / 2.0f / f + 0.5f;
            float f11 = -f7 / 2.0f / f + 0.5f;
            float f12 = -f8 / 2.0f / f + 0.5f;
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, f3, f4, f6, f7, f9, f11);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, f3, f4, f6, f8, f9, f12);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, f3, f5, f6, f8, f10, f12);
            EntityRenderDispatcher.shadowVertex(pose, vertexConsumer, f3, f5, f6, f7, f10, f11);
        }
    }

    private static void shadowVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float f2, float f3, float f4, float f5, float f6) {
        vertexConsumer.vertex(pose.pose(), f2, f3, f4).color(1.0f, 1.0f, 1.0f, f).uv(f5, f6).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(pose.normal(), 0.0f, 1.0f, 0.0f).endVertex();
    }

    public void setLevel(@Nullable Level level) {
        this.level = level;
        if (level == null) {
            this.camera = null;
        }
    }

    public double distanceToSqr(Entity entity) {
        return this.camera.getPosition().distanceToSqr(entity.position());
    }

    public double distanceToSqr(double d, double d2, double d3) {
        return this.camera.getPosition().distanceToSqr(d, d2, d3);
    }

    public Quaternion cameraOrientation() {
        return this.cameraOrientation;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(this, this.itemRenderer, resourceManager, this.entityModels, this.font);
        this.renderers = EntityRenderers.createEntityRenderers(context);
        this.playerRenderers = EntityRenderers.createPlayerRenderers(context);
    }
}

