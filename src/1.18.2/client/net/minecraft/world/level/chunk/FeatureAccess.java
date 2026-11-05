/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface FeatureAccess {
    @Nullable
    public StructureStart getStartForFeature(ConfiguredStructureFeature<?, ?> var1);

    public void setStartForFeature(ConfiguredStructureFeature<?, ?> var1, StructureStart var2);

    public LongSet getReferencesForFeature(ConfiguredStructureFeature<?, ?> var1);

    public void addReferenceForFeature(ConfiguredStructureFeature<?, ?> var1, long var2);

    public Map<ConfiguredStructureFeature<?, ?>, LongSet> getAllReferences();

    public void setAllReferences(Map<ConfiguredStructureFeature<?, ?>, LongSet> var1);
}

