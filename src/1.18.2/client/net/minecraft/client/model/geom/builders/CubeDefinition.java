/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.model.geom.builders;

import com.mojang.math.Vector3f;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.UVPair;

public final class CubeDefinition {
    @Nullable
    private final String comment;
    private final Vector3f origin;
    private final Vector3f dimensions;
    private final CubeDeformation grow;
    private final boolean mirror;
    private final UVPair texCoord;
    private final UVPair texScale;

    protected CubeDefinition(@Nullable String string, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, CubeDeformation cubeDeformation, boolean bl, float f9, float f10) {
        this.comment = string;
        this.texCoord = new UVPair(f, f2);
        this.origin = new Vector3f(f3, f4, f5);
        this.dimensions = new Vector3f(f6, f7, f8);
        this.grow = cubeDeformation;
        this.mirror = bl;
        this.texScale = new UVPair(f9, f10);
    }

    public ModelPart.Cube bake(int n, int n2) {
        return new ModelPart.Cube((int)this.texCoord.u(), (int)this.texCoord.v(), this.origin.x(), this.origin.y(), this.origin.z(), this.dimensions.x(), this.dimensions.y(), this.dimensions.z(), this.grow.growX, this.grow.growY, this.grow.growZ, this.mirror, (float)n * this.texScale.u(), (float)n2 * this.texScale.v());
    }
}

