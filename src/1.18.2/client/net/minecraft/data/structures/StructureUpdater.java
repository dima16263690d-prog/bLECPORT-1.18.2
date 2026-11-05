/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

public class StructureUpdater
implements SnbtToNbt.Filter {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public CompoundTag apply(String string, CompoundTag compoundTag) {
        if (string.startsWith("data/minecraft/structures/")) {
            return StructureUpdater.update(string, compoundTag);
        }
        return compoundTag;
    }

    public static CompoundTag update(String string, CompoundTag compoundTag) {
        return StructureUpdater.updateStructure(string, StructureUpdater.patchVersion(compoundTag));
    }

    private static CompoundTag patchVersion(CompoundTag compoundTag) {
        if (!compoundTag.contains("DataVersion", 99)) {
            compoundTag.putInt("DataVersion", 500);
        }
        return compoundTag;
    }

    private static CompoundTag updateStructure(String string, CompoundTag compoundTag) {
        StructureTemplate structureTemplate = new StructureTemplate();
        int n = compoundTag.getInt("DataVersion");
        int n2 = 2965;
        if (n < 2965) {
            LOGGER.warn("SNBT Too old, do not forget to update: {} < {}: {}", new Object[]{n, 2965, string});
        }
        CompoundTag compoundTag2 = NbtUtils.update(DataFixers.getDataFixer(), DataFixTypes.STRUCTURE, compoundTag, n);
        structureTemplate.load(compoundTag2);
        return structureTemplate.save(new CompoundTag());
    }
}

