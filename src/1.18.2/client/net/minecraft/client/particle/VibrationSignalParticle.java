/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;
import net.minecraft.world.phys.Vec3;

public class VibrationSignalParticle
extends TextureSheetParticle {
    private final VibrationPath vibrationPath;
    private float yRot;
    private float yRotO;

    VibrationSignalParticle(ClientLevel clientLevel, VibrationPath vibrationPath, int n) {
        super(clientLevel, (float)vibrationPath.getOrigin().getX() + 0.5f, (float)vibrationPath.getOrigin().getY() + 0.5f, (float)vibrationPath.getOrigin().getZ() + 0.5f, 0.0, 0.0, 0.0);
        this.quadSize = 0.3f;
        this.vibrationPath = vibrationPath;
        this.lifetime = n;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        float f2 = Mth.sin(((float)this.age + f - (float)Math.PI * 2) * 0.05f) * 2.0f;
        float f3 = Mth.lerp(f, this.yRotO, this.yRot);
        float f4 = 1.0472f;
        this.renderSignal(vertexConsumer, camera, f, quaternion -> {
            quaternion.mul(Vector3f.YP.rotation(f3));
            quaternion.mul(Vector3f.XP.rotation(-1.0472f));
            quaternion.mul(Vector3f.YP.rotation(f2));
        });
        this.renderSignal(vertexConsumer, camera, f, quaternion -> {
            quaternion.mul(Vector3f.YP.rotation((float)(-Math.PI) + f3));
            quaternion.mul(Vector3f.XP.rotation(1.0472f));
            quaternion.mul(Vector3f.YP.rotation(f2));
        });
    }

    private void renderSignal(VertexConsumer vertexConsumer, Camera camera, float f, Consumer<Quaternion> consumer) {
        Vec3 vec3 = camera.getPosition();
        float f2 = (float)(Mth.lerp((double)f, this.xo, this.x) - vec3.x());
        float f3 = (float)(Mth.lerp((double)f, this.yo, this.y) - vec3.y());
        float f4 = (float)(Mth.lerp((double)f, this.zo, this.z) - vec3.z());
        Vector3f vector3f = new Vector3f(0.5f, 0.5f, 0.5f);
        vector3f.normalize();
        Quaternion quaternion = new Quaternion(vector3f, 0.0f, true);
        consumer.accept(quaternion);
        Vector3f vector3f2 = new Vector3f(-1.0f, -1.0f, 0.0f);
        vector3f2.transform(quaternion);
        Vector3f[] vector3fArray = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        float f5 = this.getQuadSize(f);
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f3 = vector3fArray[i];
            vector3f3.transform(quaternion);
            vector3f3.mul(f5);
            vector3f3.add(f2, f3, f4);
        }
        float f6 = this.getU0();
        float f7 = this.getU1();
        float f8 = this.getV0();
        float f9 = this.getV1();
        int n = this.getLightColor(f);
        vertexConsumer.vertex(vector3fArray[0].x(), vector3fArray[0].y(), vector3fArray[0].z()).uv(f7, f9).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(n).endVertex();
        vertexConsumer.vertex(vector3fArray[1].x(), vector3fArray[1].y(), vector3fArray[1].z()).uv(f7, f8).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(n).endVertex();
        vertexConsumer.vertex(vector3fArray[2].x(), vector3fArray[2].y(), vector3fArray[2].z()).uv(f6, f8).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(n).endVertex();
        vertexConsumer.vertex(vector3fArray[3].x(), vector3fArray[3].y(), vector3fArray[3].z()).uv(f6, f9).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(n).endVertex();
    }

    @Override
    public int getLightColor(float f) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        Optional<BlockPos> optional = this.vibrationPath.getDestination().getPosition(this.level);
        if (!optional.isPresent()) {
            this.remove();
            return;
        }
        double d = (double)this.age / (double)this.lifetime;
        BlockPos blockPos = this.vibrationPath.getOrigin();
        BlockPos blockPos2 = optional.get();
        this.x = Mth.lerp(d, (double)blockPos.getX() + 0.5, (double)blockPos2.getX() + 0.5);
        this.y = Mth.lerp(d, (double)blockPos.getY() + 0.5, (double)blockPos2.getY() + 0.5);
        this.z = Mth.lerp(d, (double)blockPos.getZ() + 0.5, (double)blockPos2.getZ() + 0.5);
        this.yRotO = this.yRot;
        this.yRot = (float)Mth.atan2(this.x - (double)blockPos2.getX(), this.z - (double)blockPos2.getZ());
    }

    public static class Provider
    implements ParticleProvider<VibrationParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(VibrationParticleOption vibrationParticleOption, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            VibrationSignalParticle vibrationSignalParticle = new VibrationSignalParticle(clientLevel, vibrationParticleOption.getVibrationPath(), vibrationParticleOption.getVibrationPath().getArrivalInTicks());
            vibrationSignalParticle.pickSprite(this.sprite);
            vibrationSignalParticle.setAlpha(1.0f);
            return vibrationSignalParticle;
        }
    }
}

