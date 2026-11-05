/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;

public class BubbleColumnUpParticle
extends TextureSheetParticle {
    BubbleColumnUpParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        super(clientLevel, d, d2, d3);
        this.gravity = -0.125f;
        this.friction = 0.85f;
        this.setSize(0.02f, 0.02f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.xd = d4 * (double)0.2f + (Math.random() * 2.0 - 1.0) * (double)0.02f;
        this.yd = d5 * (double)0.2f + (Math.random() * 2.0 - 1.0) * (double)0.02f;
        this.zd = d6 * (double)0.2f + (Math.random() * 2.0 - 1.0) * (double)0.02f;
        this.lifetime = (int)(40.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed && !this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER)) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            BubbleColumnUpParticle bubbleColumnUpParticle = new BubbleColumnUpParticle(clientLevel, d, d2, d3, d4, d5, d6);
            bubbleColumnUpParticle.pickSprite(this.sprite);
            return bubbleColumnUpParticle;
        }
    }
}

