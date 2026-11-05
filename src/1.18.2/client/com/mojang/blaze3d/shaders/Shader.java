/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.shaders.Program;

public interface Shader {
    public int getId();

    public void markDirty();

    public Program getVertexProgram();

    public Program getFragmentProgram();

    public void attachToProgram();
}

