/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundCommandSuggestionPacket
implements Packet<ServerGamePacketListener> {
    private final int id;
    private final String command;

    public ServerboundCommandSuggestionPacket(int n, String string) {
        this.id = n;
        this.command = string;
    }

    public ServerboundCommandSuggestionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readVarInt();
        this.command = friendlyByteBuf.readUtf(32500);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.id);
        friendlyByteBuf.writeUtf(this.command, 32500);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleCustomCommandSuggestions(this);
    }

    public int getId() {
        return this.id;
    }

    public String getCommand() {
        return this.command;
    }
}

