/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.gameevent;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;

public class EntityPositionSource
implements PositionSource {
    public static final Codec<EntityPositionSource> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("source_entity_id").forGetter(entityPositionSource -> entityPositionSource.sourceEntityId)).apply((Applicative)instance, EntityPositionSource::new));
    final int sourceEntityId;
    private Optional<Entity> sourceEntity = Optional.empty();

    public EntityPositionSource(int n) {
        this.sourceEntityId = n;
    }

    @Override
    public Optional<BlockPos> getPosition(Level level) {
        if (!this.sourceEntity.isPresent()) {
            this.sourceEntity = Optional.ofNullable(level.getEntity(this.sourceEntityId));
        }
        return this.sourceEntity.map(Entity::blockPosition);
    }

    @Override
    public PositionSourceType<?> getType() {
        return PositionSourceType.ENTITY;
    }

    public static class Type
    implements PositionSourceType<EntityPositionSource> {
        @Override
        public EntityPositionSource read(FriendlyByteBuf friendlyByteBuf) {
            return new EntityPositionSource(friendlyByteBuf.readVarInt());
        }

        @Override
        public void write(FriendlyByteBuf friendlyByteBuf, EntityPositionSource entityPositionSource) {
            friendlyByteBuf.writeVarInt(entityPositionSource.sourceEntityId);
        }

        @Override
        public Codec<EntityPositionSource> codec() {
            return CODEC;
        }

        @Override
        public /* synthetic */ PositionSource read(FriendlyByteBuf friendlyByteBuf) {
            return this.read(friendlyByteBuf);
        }
    }
}

