/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SimpleAnimatedParticle
extends TextureSheetParticle {
    protected final SpriteSet sprites;
    private float fadeR;
    private float fadeG;
    private float fadeB;
    private boolean hasFade;

    protected SimpleAnimatedParticle(ClientLevel clientLevel, double d, double d2, double d3, SpriteSet spriteSet, float f) {
        super(clientLevel, d, d2, d3);
        this.friction = 0.91f;
        this.gravity = f;
        this.sprites = spriteSet;
    }

    public void setColor(int n) {
        float f = (float)((n & 0xFF0000) >> 16) / 255.0f;
        float f2 = (float)((n & 0xFF00) >> 8) / 255.0f;
        float f3 = (float)((n & 0xFF) >> 0) / 255.0f;
        float f4 = 1.0f;
        this.setColor(f * 1.0f, f2 * 1.0f, f3 * 1.0f);
    }

    public void setFadeColor(int n) {
        this.fadeR = (float)((n & 0xFF0000) >> 16) / 255.0f;
        this.fadeG = (float)((n & 0xFF00) >> 8) / 255.0f;
        this.fadeB = (float)((n & 0xFF) >> 0) / 255.0f;
        this.hasFade = true;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.age > this.lifetime / 2) {
            this.setAlpha(1.0f - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            if (this.hasFade) {
                this.rCol += (this.fadeR - this.rCol) * 0.2f;
                this.gCol += (this.fadeG - this.gCol) * 0.2f;
                this.bCol += (this.fadeB - this.bCol) * 0.2f;
            }
        }
    }

    @Override
    public int getLightColor(float f) {
        return 0xF000F0;
    }
}

