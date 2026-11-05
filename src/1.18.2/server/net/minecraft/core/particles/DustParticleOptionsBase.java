/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.core.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Vector3f;
import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

public abstract class DustParticleOptionsBase
implements ParticleOptions {
    public static final float MIN_SCALE = 0.01f;
    public static final float MAX_SCALE = 4.0f;
    protected final Vector3f color;
    protected final float scale;

    public DustParticleOptionsBase(Vector3f vector3f, float f) {
        this.color = vector3f;
        this.scale = Mth.clamp(f, 0.01f, 4.0f);
    }

    public static Vector3f readVector3f(StringReader stringReader) throws CommandSyntaxException {
        stringReader.expect(' ');
        float f = stringReader.readFloat();
        stringReader.expect(' ');
        float f2 = stringReader.readFloat();
        stringReader.expect(' ');
        float f3 = stringReader.readFloat();
        return new Vector3f(f, f2, f3);
    }

    public static Vector3f readVector3f(FriendlyByteBuf friendlyByteBuf) {
        return new Vector3f(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(this.color.x());
        friendlyByteBuf.writeFloat(this.color.y());
        friendlyByteBuf.writeFloat(this.color.z());
        friendlyByteBuf.writeFloat(this.scale);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), Float.valueOf(this.color.x()), Float.valueOf(this.color.y()), Float.valueOf(this.color.z()), Float.valueOf(this.scale));
    }

    public Vector3f getColor() {
        return this.color;
    }

    public float getScale() {
        return this.scale;
    }
}

