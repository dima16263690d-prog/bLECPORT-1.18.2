/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public abstract class SkullModelBase
extends Model {
    public SkullModelBase() {
        super(RenderType::entityTranslucent);
    }

    public abstract void setupAnim(float var1, float var2, float var3);
}

