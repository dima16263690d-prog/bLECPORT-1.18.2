/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationPath;

public class VibrationParticleOption
implements ParticleOptions {
    public static final Codec<VibrationParticleOption> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)VibrationPath.CODEC.fieldOf("vibration").forGetter(vibrationParticleOption -> vibrationParticleOption.vibrationPath)).apply((Applicative)instance, VibrationParticleOption::new));
    public static final ParticleOptions.Deserializer<VibrationParticleOption> DESERIALIZER = new ParticleOptions.Deserializer<VibrationParticleOption>(){

        @Override
        public VibrationParticleOption fromCommand(ParticleType<VibrationParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            float f = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float f2 = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float f3 = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float f4 = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float f5 = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float f6 = (float)stringReader.readDouble();
            stringReader.expect(' ');
            int n = stringReader.readInt();
            BlockPos blockPos = new BlockPos(f, f2, f3);
            BlockPos blockPos2 = new BlockPos(f4, f5, f6);
            return new VibrationParticleOption(new VibrationPath(blockPos, new BlockPositionSource(blockPos2), n));
        }

        @Override
        public VibrationParticleOption fromNetwork(ParticleType<VibrationParticleOption> particleType, FriendlyByteBuf friendlyByteBuf) {
            VibrationPath vibrationPath = VibrationPath.read(friendlyByteBuf);
            return new VibrationParticleOption(vibrationPath);
        }

        @Override
        public /* synthetic */ ParticleOptions fromNetwork(ParticleType particleType, FriendlyByteBuf friendlyByteBuf) {
            return this.fromNetwork(particleType, friendlyByteBuf);
        }

        @Override
        public /* synthetic */ ParticleOptions fromCommand(ParticleType particleType, StringReader stringReader) throws CommandSyntaxException {
            return this.fromCommand(particleType, stringReader);
        }
    };
    private final VibrationPath vibrationPath;

    public VibrationParticleOption(VibrationPath vibrationPath) {
        this.vibrationPath = vibrationPath;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        VibrationPath.write(friendlyByteBuf, this.vibrationPath);
    }

    @Override
    public String writeToString() {
        BlockPos blockPos = this.vibrationPath.getOrigin();
        double d = blockPos.getX();
        double d2 = blockPos.getY();
        double d3 = blockPos.getZ();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %d", Registry.PARTICLE_TYPE.getKey(this.getType()), d, d2, d3, d, d2, d3, this.vibrationPath.getArrivalInTicks());
    }

    public ParticleType<VibrationParticleOption> getType() {
        return ParticleTypes.VIBRATION;
    }

    public VibrationPath getVibrationPath() {
        return this.vibrationPath;
    }
}

