/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMarker
extends TextureSheetParticle {
    BlockMarker(ClientLevel clientLevel, double d, double d2, double d3, BlockState blockState) {
        super(clientLevel, d, d2, d3);
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState));
        this.gravity = 0.0f;
        this.lifetime = 80;
        this.hasPhysics = false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    public float getQuadSize(float f) {
        return 0.5f;
    }

    public static class Provider
    implements ParticleProvider<BlockParticleOption> {
        @Override
        public Particle createParticle(BlockParticleOption blockParticleOption, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new BlockMarker(clientLevel, d, d2, d3, blockParticleOption.getState());
        }
    }
}

