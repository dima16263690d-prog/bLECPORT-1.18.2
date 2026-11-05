/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.util;

import com.mojang.realmsclient.util.LevelType;

public class WorldGenerationInfo {
    private final String seed;
    private final LevelType levelType;
    private final boolean generateStructures;

    public WorldGenerationInfo(String string, LevelType levelType, boolean bl) {
        this.seed = string;
        this.levelType = levelType;
        this.generateStructures = bl;
    }

    public String getSeed() {
        return this.seed;
    }

    public LevelType getLevelType() {
        return this.levelType;
    }

    public boolean shouldGenerateStructures() {
        return this.generateStructures;
    }
}

