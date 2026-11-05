/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.HitResult;

public class BlockEntityRenderDispatcher
implements ResourceManagerReloadListener {
    private Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();
    private final Font font;
    private final EntityModelSet entityModelSet;
    public Level level;
    public Camera camera;
    public HitResult cameraHitResult;
    private final Supplier<BlockRenderDispatcher> blockRenderDispatcher;

    public BlockEntityRenderDispatcher(Font font, EntityModelSet entityModelSet, Supplier<BlockRenderDispatcher> supplier) {
        this.font = font;
        this.entityModelSet = entityModelSet;
        this.blockRenderDispatcher = supplier;
    }

    @Nullable
    public <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E e) {
        return this.renderers.get(e.getType());
    }

    public void prepare(Level level, Camera camera, HitResult hitResult) {
        if (this.level != level) {
            this.setLevel(level);
        }
        this.camera = camera;
        this.cameraHitResult = hitResult;
    }

    public <E extends BlockEntity> void render(E e, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        BlockEntityRenderer blockEntityRenderer = this.getRenderer(e);
        if (blockEntityRenderer == null) {
            return;
        }
        if (!e.hasLevel() || !e.getType().isValid(e.getBlockState())) {
            return;
        }
        if (!blockEntityRenderer.shouldRender(e, this.camera.getPosition())) {
            return;
        }
        BlockEntityRenderDispatcher.tryRender(e, () -> BlockEntityRenderDispatcher.setupAndRender(blockEntityRenderer, e, f, poseStack, multiBufferSource));
    }

    private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> blockEntityRenderer, T t, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        Level level = t.getLevel();
        int n = level != null ? LevelRenderer.getLightColor(level, t.getBlockPos()) : 0xF000F0;
        blockEntityRenderer.render(t, f, poseStack, multiBufferSource, n, OverlayTexture.NO_OVERLAY);
    }

    public <E extends BlockEntity> boolean renderItem(E e, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
        BlockEntityRenderer blockEntityRenderer = this.getRenderer(e);
        if (blockEntityRenderer == null) {
            return true;
        }
        BlockEntityRenderDispatcher.tryRender(e, () -> blockEntityRenderer.render(e, 0.0f, poseStack, multiBufferSource, n, n2));
        return false;
    }

    private static void tryRender(BlockEntity blockEntity, Runnable runnable) {
        try {
            runnable.run();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering Block Entity");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block Entity Details");
            blockEntity.fillCrashReportCategory(crashReportCategory);
            throw new ReportedException(crashReport);
        }
    }

    public void setLevel(@Nullable Level level) {
        this.level = level;
        if (level == null) {
            this.camera = null;
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        BlockEntityRendererProvider.Context context = new BlockEntityRendererProvider.Context(this, this.blockRenderDispatcher.get(), this.entityModelSet, this.font);
        this.renderers = BlockEntityRenderers.createEntityRenderers(context);
    }
}

