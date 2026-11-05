/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClientboundUpdateRecipesPacket
implements Packet<ClientGamePacketListener> {
    private final List<Recipe<?>> recipes;

    public ClientboundUpdateRecipesPacket(Collection<Recipe<?>> collection) {
        this.recipes = Lists.newArrayList(collection);
    }

    public ClientboundUpdateRecipesPacket(FriendlyByteBuf friendlyByteBuf) {
        this.recipes = friendlyByteBuf.readList(ClientboundUpdateRecipesPacket::fromNetwork);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeCollection(this.recipes, ClientboundUpdateRecipesPacket::toNetwork);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateRecipes(this);
    }

    public List<Recipe<?>> getRecipes() {
        return this.recipes;
    }

    public static Recipe<?> fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
        ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
        return Registry.RECIPE_SERIALIZER.getOptional(resourceLocation).orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + resourceLocation)).fromNetwork(resourceLocation2, friendlyByteBuf);
    }

    public static <T extends Recipe<?>> void toNetwork(FriendlyByteBuf friendlyByteBuf, T t) {
        friendlyByteBuf.writeResourceLocation(Registry.RECIPE_SERIALIZER.getKey(t.getSerializer()));
        friendlyByteBuf.writeResourceLocation(t.getId());
        t.getSerializer().toNetwork(friendlyByteBuf, t);
    }
}

