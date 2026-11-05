/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class GameEventListenerRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int LISTENER_RENDER_DIST = 32;
    private static final float BOX_HEIGHT = 1.0f;
    private final List<TrackedGameEvent> trackedGameEvents = Lists.newArrayList();
    private final List<TrackedListener> trackedListeners = Lists.newArrayList();

    public GameEventListenerRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double d2, double d3) {
        ClientLevel clientLevel = this.minecraft.level;
        if (clientLevel == null) {
            this.trackedGameEvents.clear();
            this.trackedListeners.clear();
            return;
        }
        BlockPos blockPos2 = new BlockPos(d, 0.0, d3);
        this.trackedGameEvents.removeIf(TrackedGameEvent::isExpired);
        this.trackedListeners.removeIf(trackedListener -> trackedListener.isExpired(clientLevel, blockPos2));
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lines());
        for (TrackedListener object2 : this.trackedListeners) {
            object2.getPosition(clientLevel).ifPresent(blockPos -> {
                int n = blockPos.getX() - object2.getListenerRadius();
                int n2 = blockPos.getY() - object2.getListenerRadius();
                int n3 = blockPos.getZ() - object2.getListenerRadius();
                int n4 = blockPos.getX() + object2.getListenerRadius();
                int n5 = blockPos.getY() + object2.getListenerRadius();
                int n6 = blockPos.getZ() + object2.getListenerRadius();
                Vector3f vector3f = new Vector3f(1.0f, 1.0f, 0.0f);
                LevelRenderer.renderVoxelShape(poseStack, vertexConsumer, Shapes.create(new AABB(n, n2, n3, n4, n5, n6)), -d, -d2, -d3, vector3f.x(), vector3f.y(), vector3f.z(), 0.35f);
            });
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (TrackedListener trackedListener2 : this.trackedListeners) {
            trackedListener2.getPosition(clientLevel).ifPresent(blockPos -> {
                Vector3f vector3f = new Vector3f(1.0f, 1.0f, 0.0f);
                LevelRenderer.addChainedFilledBoxVertices(bufferBuilder, (double)((float)blockPos.getX() - 0.25f) - d, (double)blockPos.getY() - d2, (double)((float)blockPos.getZ() - 0.25f) - d3, (double)((float)blockPos.getX() + 0.25f) - d, (double)blockPos.getY() - d2 + 1.0, (double)((float)blockPos.getZ() + 0.25f) - d3, vector3f.x(), vector3f.y(), vector3f.z(), 0.35f);
            });
        }
        tesselator.end();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.depthMask(false);
        for (TrackedListener trackedListener3 : this.trackedListeners) {
            trackedListener3.getPosition(clientLevel).ifPresent(blockPos -> {
                DebugRenderer.renderFloatingText("Listener Origin", blockPos.getX(), (float)blockPos.getY() + 1.8f, blockPos.getZ(), -1, 0.025f);
                DebugRenderer.renderFloatingText(new BlockPos((Vec3i)blockPos).toString(), blockPos.getX(), (float)blockPos.getY() + 1.5f, blockPos.getZ(), -6959665, 0.025f);
            });
        }
        for (TrackedGameEvent trackedGameEvent : this.trackedGameEvents) {
            Vec3 vec3 = trackedGameEvent.position;
            double d4 = 0.2f;
            double d5 = vec3.x - (double)0.2f;
            double d6 = vec3.y - (double)0.2f;
            double d7 = vec3.z - (double)0.2f;
            double d8 = vec3.x + (double)0.2f;
            double d9 = vec3.y + (double)0.2f + 0.5;
            double d10 = vec3.z + (double)0.2f;
            GameEventListenerRenderer.renderTransparentFilledBox(new AABB(d5, d6, d7, d8, d9, d10), 1.0f, 1.0f, 1.0f, 0.2f);
            DebugRenderer.renderFloatingText(trackedGameEvent.gameEvent.getName(), vec3.x, vec3.y + (double)0.85f, vec3.z, -7564911, 0.0075f);
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void renderTransparentFilledBox(AABB aABB, float f, float f2, float f3, float f4) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Vec3 vec3 = camera.getPosition().reverse();
        DebugRenderer.renderFilledBox(aABB.move(vec3), f, f2, f3, f4);
    }

    public void trackGameEvent(GameEvent gameEvent, BlockPos blockPos) {
        this.trackedGameEvents.add(new TrackedGameEvent(Util.getMillis(), gameEvent, Vec3.atBottomCenterOf(blockPos)));
    }

    public void trackListener(PositionSource positionSource, int n) {
        this.trackedListeners.add(new TrackedListener(positionSource, n));
    }

    static class TrackedListener
    implements GameEventListener {
        public final PositionSource listenerSource;
        public final int listenerRange;

        public TrackedListener(PositionSource positionSource, int n) {
            this.listenerSource = positionSource;
            this.listenerRange = n;
        }

        public boolean isExpired(Level level, BlockPos blockPos) {
            Optional<BlockPos> optional = this.listenerSource.getPosition(level);
            return !optional.isPresent() || optional.get().distSqr(blockPos) <= 1024.0;
        }

        public Optional<BlockPos> getPosition(Level level) {
            return this.listenerSource.getPosition(level);
        }

        @Override
        public PositionSource getListenerSource() {
            return this.listenerSource;
        }

        @Override
        public int getListenerRadius() {
            return this.listenerRange;
        }

        @Override
        public boolean handleGameEvent(Level level, GameEvent gameEvent, @Nullable Entity entity, BlockPos blockPos) {
            return false;
        }
    }

    static class TrackedGameEvent {
        public final long timeStamp;
        public final GameEvent gameEvent;
        public final Vec3 position;

        public TrackedGameEvent(long l, GameEvent gameEvent, Vec3 vec3) {
            this.timeStamp = l;
            this.gameEvent = gameEvent;
            this.position = vec3;
        }

        public boolean isExpired() {
            return Util.getMillis() - this.timeStamp > 3000L;
        }
    }
}

