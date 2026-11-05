/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class GlowParticle
extends TextureSheetParticle {
    static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    GlowParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, d4, d5, d6);
        this.friction = 0.96f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.quadSize *= 0.75f;
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float f) {
        float f2 = ((float)this.age + f) / (float)this.lifetime;
        f2 = Mth.clamp(f2, 0.0f, 1.0f);
        int n = super.getLightColor(f);
        int n2 = n & 0xFF;
        int n3 = n >> 16 & 0xFF;
        if ((n2 += (int)(f2 * 15.0f * 16.0f)) > 240) {
            n2 = 240;
        }
        return n2 | n3 << 16;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    public static class ScrapeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public ScrapeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, d2, d3, 0.0, 0.0, 0.0, this.sprite);
            if (clientLevel.random.nextBoolean()) {
                glowParticle.setColor(0.29f, 0.58f, 0.51f);
            } else {
                glowParticle.setColor(0.43f, 0.77f, 0.62f);
            }
            glowParticle.setParticleSpeed(d4 * 0.01, d5 * 0.01, d6 * 0.01);
            int n = 10;
            int n2 = 40;
            glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    public static class ElectricSparkProvider
    implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.25;
        private final SpriteSet sprite;

        public ElectricSparkProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, d2, d3, 0.0, 0.0, 0.0, this.sprite);
            glowParticle.setColor(1.0f, 0.9f, 1.0f);
            glowParticle.setParticleSpeed(d4 * 0.25, d5 * 0.25, d6 * 0.25);
            int n = 2;
            int n2 = 4;
            glowParticle.setLifetime(clientLevel.random.nextInt(2) + 2);
            return glowParticle;
        }
    }

    public static class WaxOffProvider
    implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public WaxOffProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, d2, d3, 0.0, 0.0, 0.0, this.sprite);
            glowParticle.setColor(1.0f, 0.9f, 1.0f);
            glowParticle.setParticleSpeed(d4 * 0.01 / 2.0, d5 * 0.01, d6 * 0.01 / 2.0);
            int n = 10;
            int n2 = 40;
            glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    public static class WaxOnProvider
    implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public WaxOnProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, d2, d3, 0.0, 0.0, 0.0, this.sprite);
            glowParticle.setColor(0.91f, 0.55f, 0.08f);
            glowParticle.setParticleSpeed(d4 * 0.01 / 2.0, d5 * 0.01, d6 * 0.01 / 2.0);
            int n = 10;
            int n2 = 40;
            glowParticle.setLifetime(clientLevel.random.nextInt(30) + 10);
            return glowParticle;
        }
    }

    public static class GlowSquidProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public GlowSquidProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            GlowParticle glowParticle = new GlowParticle(clientLevel, d, d2, d3, 0.5 - RANDOM.nextDouble(), d5, 0.5 - RANDOM.nextDouble(), this.sprite);
            if (clientLevel.random.nextBoolean()) {
                glowParticle.setColor(0.6f, 1.0f, 0.8f);
            } else {
                glowParticle.setColor(0.08f, 0.4f, 0.4f);
            }
            glowParticle.yd *= (double)0.2f;
            if (d4 == 0.0 && d6 == 0.0) {
                glowParticle.xd *= (double)0.1f;
                glowParticle.zd *= (double)0.1f;
            }
            glowParticle.setLifetime((int)(8.0 / (clientLevel.random.nextDouble() * 0.8 + 0.2)));
            return glowParticle;
        }
    }
}

