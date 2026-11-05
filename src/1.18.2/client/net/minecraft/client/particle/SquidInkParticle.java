/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;

public class SquidInkParticle
extends SimpleAnimatedParticle {
    SquidInkParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, int n, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, spriteSet, 0.0f);
        this.friction = 0.92f;
        this.quadSize = 0.5f;
        this.setAlpha(1.0f);
        this.setColor(FastColor.ARGB32.red(n), FastColor.ARGB32.green(n), FastColor.ARGB32.blue(n));
        this.lifetime = (int)((double)(this.quadSize * 12.0f) / (Math.random() * (double)0.8f + (double)0.2f));
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = false;
        this.xd = d4;
        this.yd = d5;
        this.zd = d6;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
            if (this.age > this.lifetime / 2) {
                this.setAlpha(1.0f - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            }
            if (this.level.getBlockState(new BlockPos(this.x, this.y, this.z)).isAir()) {
                this.yd -= (double)0.0074f;
            }
        }
    }

    public static class GlowInkProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public GlowInkProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new SquidInkParticle(clientLevel, d, d2, d3, d4, d5, d6, FastColor.ARGB32.color(255, 204, 31, 102), this.sprites);
        }
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new SquidInkParticle(clientLevel, d, d2, d3, d4, d5, d6, FastColor.ARGB32.color(255, 255, 255, 255), this.sprites);
        }
    }
}

