/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;

public class ServerboundSeenAdvancementsPacket
implements Packet<ServerGamePacketListener> {
    private final Action action;
    @Nullable
    private final ResourceLocation tab;

    public ServerboundSeenAdvancementsPacket(Action action, @Nullable ResourceLocation resourceLocation) {
        this.action = action;
        this.tab = resourceLocation;
    }

    public static ServerboundSeenAdvancementsPacket openedTab(Advancement advancement) {
        return new ServerboundSeenAdvancementsPacket(Action.OPENED_TAB, advancement.getId());
    }

    public static ServerboundSeenAdvancementsPacket closedScreen() {
        return new ServerboundSeenAdvancementsPacket(Action.CLOSED_SCREEN, null);
    }

    public ServerboundSeenAdvancementsPacket(FriendlyByteBuf friendlyByteBuf) {
        this.action = friendlyByteBuf.readEnum(Action.class);
        this.tab = this.action == Action.OPENED_TAB ? friendlyByteBuf.readResourceLocation() : null;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(this.action);
        if (this.action == Action.OPENED_TAB) {
            friendlyByteBuf.writeResourceLocation(this.tab);
        }
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSeenAdvancements(this);
    }

    public Action getAction() {
        return this.action;
    }

    @Nullable
    public ResourceLocation getTab() {
        return this.tab;
    }

    public static enum Action {
        OPENED_TAB,
        CLOSED_SCREEN;

    }
}

