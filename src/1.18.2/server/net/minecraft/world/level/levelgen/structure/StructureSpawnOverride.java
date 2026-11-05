/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.biome.MobSpawnSettings;

public record StructureSpawnOverride(BoundingBoxType boundingBox, WeightedRandomList<MobSpawnSettings.SpawnerData> spawns) {
    public static final Codec<StructureSpawnOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BoundingBoxType.CODEC.fieldOf("bounding_box").forGetter(StructureSpawnOverride::boundingBox), (App)WeightedRandomList.codec(MobSpawnSettings.SpawnerData.CODEC).fieldOf("spawns").forGetter(StructureSpawnOverride::spawns)).apply((Applicative)instance, StructureSpawnOverride::new));

    public static enum BoundingBoxType implements StringRepresentable
    {
        PIECE("piece"),
        STRUCTURE("full");

        public static final BoundingBoxType[] VALUES;
        public static final Codec<BoundingBoxType> CODEC;
        private final String id;

        private BoundingBoxType(String string2) {
            this.id = string2;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        @Nullable
        public static BoundingBoxType byName(@Nullable String string) {
            if (string == null) {
                return null;
            }
            for (BoundingBoxType boundingBoxType : VALUES) {
                if (!boundingBoxType.id.equals(string)) continue;
                return boundingBoxType;
            }
            return null;
        }

        static {
            VALUES = BoundingBoxType.values();
            CODEC = StringRepresentable.fromEnum(() -> VALUES, BoundingBoxType::byName);
        }
    }
}

