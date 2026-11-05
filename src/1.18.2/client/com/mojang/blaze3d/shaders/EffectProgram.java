/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Effect;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;

public class EffectProgram
extends Program {
    private static final GlslPreprocessor PREPROCESSOR = new GlslPreprocessor(){

        @Override
        public String applyImport(boolean bl, String string) {
            return "#error Import statement not supported";
        }
    };
    private int references;

    private EffectProgram(Program.Type type, int n, String string) {
        super(type, n, string);
    }

    public void attachToEffect(Effect effect) {
        RenderSystem.assertOnRenderThread();
        ++this.references;
        this.attachToShader(effect);
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        --this.references;
        if (this.references <= 0) {
            super.close();
        }
    }

    public static EffectProgram compileShader(Program.Type type, String string, InputStream inputStream, String string2) throws IOException {
        RenderSystem.assertOnRenderThread();
        int n = EffectProgram.compileShaderInternal(type, string, inputStream, string2, PREPROCESSOR);
        EffectProgram effectProgram = new EffectProgram(type, n, string);
        type.getPrograms().put(string, effectProgram);
        return effectProgram;
    }
}

