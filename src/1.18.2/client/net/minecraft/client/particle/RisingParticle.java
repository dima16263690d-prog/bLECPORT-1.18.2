/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;

public abstract class RisingParticle
extends TextureSheetParticle {
    protected RisingParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        super(clientLevel, d, d2, d3, d4, d5, d6);
        this.friction = 0.96f;
        this.xd = this.xd * (double)0.01f + d4;
        this.yd = this.yd * (double)0.01f + d5;
        this.zd = this.zd * (double)0.01f + d6;
        this.x += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05f);
        this.y += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05f);
        this.z += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05f);
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
    }
}

