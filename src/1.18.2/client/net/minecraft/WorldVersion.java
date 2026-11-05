/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.bridge.game.GameVersion
 */
package net.minecraft;

import com.mojang.bridge.game.GameVersion;
import net.minecraft.world.level.storage.DataVersion;

public interface WorldVersion
extends GameVersion {
    @Deprecated
    default public int getWorldVersion() {
        return this.getDataVersion().getVersion();
    }

    @Deprecated
    default public String getSeriesId() {
        return this.getDataVersion().getSeries();
    }

    public DataVersion getDataVersion();
}

