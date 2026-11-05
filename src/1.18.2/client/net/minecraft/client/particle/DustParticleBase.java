/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.util.Mth;

public class DustParticleBase<T extends DustParticleOptionsBase>
extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected DustParticleBase(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, T t, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, d4, d5, d6);
        this.friction = 0.96f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.xd *= (double)0.1f;
        this.yd *= (double)0.1f;
        this.zd *= (double)0.1f;
        float f = this.random.nextFloat() * 0.4f + 0.6f;
        this.rCol = this.randomizeColor(((DustParticleOptionsBase)t).getColor().x(), f);
        this.gCol = this.randomizeColor(((DustParticleOptionsBase)t).getColor().y(), f);
        this.bCol = this.randomizeColor(((DustParticleOptionsBase)t).getColor().z(), f);
        this.quadSize *= 0.75f * ((DustParticleOptionsBase)t).getScale();
        int n = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.lifetime = (int)Math.max((float)n * ((DustParticleOptionsBase)t).getScale(), 1.0f);
        this.setSpriteFromAge(spriteSet);
    }

    protected float randomizeColor(float f, float f2) {
        return (this.random.nextFloat() * 0.2f + 0.8f) * f * f2;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }
}

