/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum LevelType {
    DEFAULT(0, new TranslatableComponent("generator.default")),
    FLAT(1, new TranslatableComponent("generator.flat")),
    LARGE_BIOMES(2, new TranslatableComponent("generator.large_biomes")),
    AMPLIFIED(3, new TranslatableComponent("generator.amplified"));

    private final int index;
    private final Component name;

    private LevelType(int n2, Component component) {
        this.index = n2;
        this.name = component;
    }

    public Component getName() {
        return this.name;
    }

    public int getDtoIndex() {
        return this.index;
    }
}

