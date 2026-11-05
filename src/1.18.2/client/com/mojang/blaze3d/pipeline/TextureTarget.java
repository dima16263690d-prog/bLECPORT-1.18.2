/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

public class TextureTarget
extends RenderTarget {
    public TextureTarget(int n, int n2, boolean bl, boolean bl2) {
        super(bl);
        RenderSystem.assertOnRenderThreadOrInit();
        this.resize(n, n2, bl2);
    }
}

