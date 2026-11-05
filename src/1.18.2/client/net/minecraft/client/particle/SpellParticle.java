/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class SpellParticle
extends TextureSheetParticle {
    private static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    SpellParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, 0.5 - RANDOM.nextDouble(), d5, 0.5 - RANDOM.nextDouble());
        this.friction = 0.96f;
        this.gravity = -0.1f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.yd *= (double)0.2f;
        if (d4 == 0.0 && d6 == 0.0) {
            this.xd *= (double)0.1f;
            this.zd *= (double)0.1f;
        }
        this.quadSize *= 0.75f;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0f);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0f);
        } else {
            this.setAlpha(Mth.lerp(0.05f, this.alpha, 1.0f));
        }
    }

    private boolean isCloseToScopingPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        return localPlayer != null && localPlayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0 && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping();
    }

    public static class InstantProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public InstantProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
        }
    }

    public static class WitchProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WitchProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
            float f = clientLevel.random.nextFloat() * 0.5f + 0.35f;
            spellParticle.setColor(1.0f * f, 0.0f * f, 1.0f * f);
            return spellParticle;
        }
    }

    public static class AmbientMobProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public AmbientMobProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
            spellParticle.setAlpha(0.15f);
            spellParticle.setColor((float)d4, (float)d5, (float)d6);
            return spellParticle;
        }
    }

    public static class MobProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public MobProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
            spellParticle.setColor((float)d4, (float)d5, (float)d6);
            return spellParticle;
        }
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new SpellParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
        }
    }
}

