/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ServerboundPlaceRecipePacket
implements Packet<ServerGamePacketListener> {
    private final int containerId;
    private final ResourceLocation recipe;
    private final boolean shiftDown;

    public ServerboundPlaceRecipePacket(int n, Recipe<?> recipe, boolean bl) {
        this.containerId = n;
        this.recipe = recipe.getId();
        this.shiftDown = bl;
    }

    public ServerboundPlaceRecipePacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readByte();
        this.recipe = friendlyByteBuf.readResourceLocation();
        this.shiftDown = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeResourceLocation(this.recipe);
        friendlyByteBuf.writeBoolean(this.shiftDown);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handlePlaceRecipe(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public ResourceLocation getRecipe() {
        return this.recipe;
    }

    public boolean isShiftDown() {
        return this.shiftDown;
    }
}

