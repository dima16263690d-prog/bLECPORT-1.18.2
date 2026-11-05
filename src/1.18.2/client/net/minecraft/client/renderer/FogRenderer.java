/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

public class FogRenderer {
    private static final int WATER_FOG_DISTANCE = 96;
    public static final float BIOME_FOG_TRANSITION_TIME = 5000.0f;
    private static float fogRed;
    private static float fogGreen;
    private static float fogBlue;
    private static int targetBiomeFog;
    private static int previousBiomeFog;
    private static long biomeChangedTime;

    public static void setupColor(Camera camera, float f, ClientLevel clientLevel, int n4, float f2) {
        FogType fogType = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        if (fogType == FogType.WATER) {
            long l = Util.getMillis();
            int n5 = clientLevel.getBiome(new BlockPos(camera.getPosition())).value().getWaterFogColor();
            if (biomeChangedTime < 0L) {
                targetBiomeFog = n5;
                previousBiomeFog = n5;
                biomeChangedTime = l;
            }
            int n6 = targetBiomeFog >> 16 & 0xFF;
            int n7 = targetBiomeFog >> 8 & 0xFF;
            int n8 = targetBiomeFog & 0xFF;
            int n9 = previousBiomeFog >> 16 & 0xFF;
            int n10 = previousBiomeFog >> 8 & 0xFF;
            int n11 = previousBiomeFog & 0xFF;
            float f3 = Mth.clamp((float)(l - biomeChangedTime) / 5000.0f, 0.0f, 1.0f);
            float f4 = Mth.lerp(f3, n9, n6);
            float f5 = Mth.lerp(f3, n10, n7);
            float f6 = Mth.lerp(f3, n11, n8);
            fogRed = f4 / 255.0f;
            fogGreen = f5 / 255.0f;
            fogBlue = f6 / 255.0f;
            if (targetBiomeFog != n5) {
                targetBiomeFog = n5;
                previousBiomeFog = Mth.floor(f4) << 16 | Mth.floor(f5) << 8 | Mth.floor(f6);
                biomeChangedTime = l;
            }
        } else if (fogType == FogType.LAVA) {
            fogRed = 0.6f;
            fogGreen = 0.1f;
            fogBlue = 0.0f;
            biomeChangedTime = -1L;
        } else if (fogType == FogType.POWDER_SNOW) {
            fogRed = 0.623f;
            fogGreen = 0.734f;
            fogBlue = 0.785f;
            biomeChangedTime = -1L;
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
        } else {
            float f7;
            float f8;
            float f9;
            float f10 = 0.25f + 0.75f * (float)n4 / 32.0f;
            f10 = 1.0f - (float)Math.pow(f10, 0.25);
            Vec3 vec3 = clientLevel.getSkyColor(camera.getPosition(), f);
            float f11 = (float)vec3.x;
            float f12 = (float)vec3.y;
            float f13 = (float)vec3.z;
            float f14 = Mth.clamp(Mth.cos(clientLevel.getTimeOfDay(f) * ((float)Math.PI * 2)) * 2.0f + 0.5f, 0.0f, 1.0f);
            BiomeManager biomeManager = clientLevel.getBiomeManager();
            Vec3 vec32 = camera.getPosition().subtract(2.0, 2.0, 2.0).scale(0.25);
            Vec3 vec33 = CubicSampler.gaussianSampleVec3(vec32, (n, n2, n3) -> clientLevel.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(n, n2, n3).value().getFogColor()), f14));
            fogRed = (float)vec33.x();
            fogGreen = (float)vec33.y();
            fogBlue = (float)vec33.z();
            if (n4 >= 4) {
                float[] fArray;
                f9 = Mth.sin(clientLevel.getSunAngle(f)) > 0.0f ? -1.0f : 1.0f;
                Vector3f vector3f = new Vector3f(f9, 0.0f, 0.0f);
                f8 = camera.getLookVector().dot(vector3f);
                if (f8 < 0.0f) {
                    f8 = 0.0f;
                }
                if (f8 > 0.0f && (fArray = clientLevel.effects().getSunriseColor(clientLevel.getTimeOfDay(f), f)) != null) {
                    fogRed = fogRed * (1.0f - (f8 *= fArray[3])) + fArray[0] * f8;
                    fogGreen = fogGreen * (1.0f - f8) + fArray[1] * f8;
                    fogBlue = fogBlue * (1.0f - f8) + fArray[2] * f8;
                }
            }
            fogRed += (f11 - fogRed) * f10;
            fogGreen += (f12 - fogGreen) * f10;
            fogBlue += (f13 - fogBlue) * f10;
            f9 = clientLevel.getRainLevel(f);
            if (f9 > 0.0f) {
                float f15 = 1.0f - f9 * 0.5f;
                f8 = 1.0f - f9 * 0.4f;
                fogRed *= f15;
                fogGreen *= f15;
                fogBlue *= f8;
            }
            if ((f7 = clientLevel.getThunderLevel(f)) > 0.0f) {
                f8 = 1.0f - f7 * 0.5f;
                fogRed *= f8;
                fogGreen *= f8;
                fogBlue *= f8;
            }
            biomeChangedTime = -1L;
        }
        float f16 = ((float)camera.getPosition().y - (float)clientLevel.getMinBuildHeight()) * clientLevel.getLevelData().getClearColorScale();
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity)camera.getEntity()).hasEffect(MobEffects.BLINDNESS)) {
            int n12 = ((LivingEntity)camera.getEntity()).getEffect(MobEffects.BLINDNESS).getDuration();
            f16 = n12 < 20 ? 1.0f - (float)n12 / 20.0f : 0.0f;
        }
        if (f16 < 1.0f && fogType != FogType.LAVA && fogType != FogType.POWDER_SNOW) {
            if (f16 < 0.0f) {
                f16 = 0.0f;
            }
            f16 *= f16;
            fogRed *= f16;
            fogGreen *= f16;
            fogBlue *= f16;
        }
        if (f2 > 0.0f) {
            fogRed = fogRed * (1.0f - f2) + fogRed * 0.7f * f2;
            fogGreen = fogGreen * (1.0f - f2) + fogGreen * 0.6f * f2;
            fogBlue = fogBlue * (1.0f - f2) + fogBlue * 0.6f * f2;
        }
        float f17 = fogType == FogType.WATER ? (entity instanceof LocalPlayer ? ((LocalPlayer)entity).getWaterVision() : 1.0f) : (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.NIGHT_VISION) ? GameRenderer.getNightVisionScale((LivingEntity)entity, f) : 0.0f);
        if (fogRed != 0.0f && fogGreen != 0.0f && fogBlue != 0.0f) {
            float f18 = Math.min(1.0f / fogRed, Math.min(1.0f / fogGreen, 1.0f / fogBlue));
            fogRed = fogRed * (1.0f - f17) + fogRed * f18 * f17;
            fogGreen = fogGreen * (1.0f - f17) + fogGreen * f18 * f17;
            fogBlue = fogBlue * (1.0f - f17) + fogBlue * f18 * f17;
        }
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0f);
    }

    public static void setupNoFog() {
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
    }

    public static void setupFog(Camera camera, FogMode fogMode, float f, boolean bl) {
        float f2;
        float f3;
        FogType fogType = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        FogShape fogShape = FogShape.SPHERE;
        if (fogType == FogType.LAVA) {
            if (entity.isSpectator()) {
                f3 = -8.0f;
                f2 = f * 0.5f;
            } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                f3 = 0.0f;
                f2 = 3.0f;
            } else {
                f3 = 0.25f;
                f2 = 1.0f;
            }
        } else if (fogType == FogType.POWDER_SNOW) {
            if (entity.isSpectator()) {
                f3 = -8.0f;
                f2 = f * 0.5f;
            } else {
                f3 = 0.0f;
                f2 = 2.0f;
            }
        } else if (entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.BLINDNESS)) {
            int n = ((LivingEntity)entity).getEffect(MobEffects.BLINDNESS).getDuration();
            float f4 = Mth.lerp(Math.min(1.0f, (float)n / 20.0f), f, 5.0f);
            if (fogMode == FogMode.FOG_SKY) {
                f3 = 0.0f;
                f2 = f4 * 0.8f;
            } else {
                f3 = fogType == FogType.WATER ? -4.0f : f4 * 0.25f;
                f2 = f4;
            }
        } else if (fogType == FogType.WATER) {
            f3 = -8.0f;
            f2 = 96.0f;
            if (entity instanceof LocalPlayer) {
                LocalPlayer localPlayer = (LocalPlayer)entity;
                f2 *= Math.max(0.25f, localPlayer.getWaterVision());
                Holder<Biome> holder = localPlayer.level.getBiome(localPlayer.blockPosition());
                if (Biome.getBiomeCategory(holder) == Biome.BiomeCategory.SWAMP) {
                    f2 *= 0.85f;
                }
            }
            if (f2 > f) {
                f2 = f;
                fogShape = FogShape.CYLINDER;
            }
        } else if (bl) {
            f3 = f * 0.05f;
            f2 = Math.min(f, 192.0f) * 0.5f;
        } else if (fogMode == FogMode.FOG_SKY) {
            f3 = 0.0f;
            f2 = f;
            fogShape = FogShape.CYLINDER;
        } else {
            float f5 = Mth.clamp(f / 10.0f, 4.0f, 64.0f);
            f3 = f - f5;
            f2 = f;
            fogShape = FogShape.CYLINDER;
        }
        RenderSystem.setShaderFogStart(f3);
        RenderSystem.setShaderFogEnd(f2);
        RenderSystem.setShaderFogShape(fogShape);
    }

    public static void levelFogColor() {
        RenderSystem.setShaderFogColor(fogRed, fogGreen, fogBlue);
    }

    static {
        targetBiomeFog = -1;
        previousBiomeFog = -1;
        biomeChangedTime = -1L;
    }

    public static enum FogMode {
        FOG_SKY,
        FOG_TERRAIN;

    }
}

