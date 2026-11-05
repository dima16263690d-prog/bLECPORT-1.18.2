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
import net.minecraft.core.particles.SimpleParticleType;

public class SnowflakeParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SnowflakeParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3);
        this.gravity = 0.225f;
        this.friction = 1.0f;
        this.sprites = spriteSet;
        this.xd = d4 + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.yd = d5 + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.zd = d6 + (Math.random() * 2.0 - 1.0) * (double)0.05f;
        this.quadSize = 0.1f * (this.random.nextFloat() * this.random.nextFloat() * 1.0f + 1.0f);
        this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.xd *= (double)0.95f;
        this.yd *= (double)0.9f;
        this.zd *= (double)0.95f;
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SnowflakeParticle snowflakeParticle = new SnowflakeParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprites);
            snowflakeParticle.setColor(0.923f, 0.964f, 0.999f);
            return snowflakeParticle;
        }
    }
}

