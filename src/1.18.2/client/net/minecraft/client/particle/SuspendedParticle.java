/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import java.util.Optional;
import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class SuspendedParticle
extends TextureSheetParticle {
    SuspendedParticle(ClientLevel clientLevel, SpriteSet spriteSet, double d, double d2, double d3) {
        super(clientLevel, d, d2 - 0.125, d3);
        this.setSize(0.01f, 0.01f);
        this.pickSprite(spriteSet);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.friction = 1.0f;
        this.gravity = 0.0f;
    }

    SuspendedParticle(ClientLevel clientLevel, SpriteSet spriteSet, double d, double d2, double d3, double d4, double d5, double d6) {
        super(clientLevel, d, d2 - 0.125, d3, d4, d5, d6);
        this.setSize(0.01f, 0.01f);
        this.pickSprite(spriteSet);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.6f;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.friction = 1.0f;
        this.gravity = 0.0f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class WarpedSporeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WarpedSporeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            double d7 = (double)clientLevel.random.nextFloat() * -1.9 * (double)clientLevel.random.nextFloat() * 0.1;
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, this.sprite, d, d2, d3, 0.0, d7, 0.0);
            suspendedParticle.setColor(0.1f, 0.1f, 0.3f);
            suspendedParticle.setSize(0.001f, 0.001f);
            return suspendedParticle;
        }
    }

    public static class CrimsonSporeProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public CrimsonSporeProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            Random random = clientLevel.random;
            double d7 = random.nextGaussian() * (double)1.0E-6f;
            double d8 = random.nextGaussian() * (double)1.0E-4f;
            double d9 = random.nextGaussian() * (double)1.0E-6f;
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, this.sprite, d, d2, d3, d7, d8, d9);
            suspendedParticle.setColor(0.9f, 0.4f, 0.5f);
            return suspendedParticle;
        }
    }

    public static class SporeBlossomAirProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SporeBlossomAirProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, this.sprite, d, d2, d3, 0.0, -0.8f, 0.0){

                @Override
                public Optional<ParticleGroup> getParticleGroup() {
                    return Optional.of(ParticleGroup.SPORE_BLOSSOM);
                }
            };
            suspendedParticle.lifetime = Mth.randomBetweenInclusive(clientLevel.random, 500, 1000);
            suspendedParticle.gravity = 0.01f;
            suspendedParticle.setColor(0.32f, 0.5f, 0.22f);
            return suspendedParticle;
        }
    }

    public static class UnderwaterProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public UnderwaterProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SuspendedParticle suspendedParticle = new SuspendedParticle(clientLevel, this.sprite, d, d2, d3);
            suspendedParticle.setColor(0.4f, 0.4f, 0.7f);
            return suspendedParticle;
        }
    }
}

