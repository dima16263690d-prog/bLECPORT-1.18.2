/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.context.StringRange
 *  com.mojang.brigadier.suggestion.Suggestion
 *  com.mojang.brigadier.suggestion.Suggestions
 */
package net.minecraft.network.protocol.game;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundCommandSuggestionsPacket
implements Packet<ClientGamePacketListener> {
    private final int id;
    private final Suggestions suggestions;

    public ClientboundCommandSuggestionsPacket(int n, Suggestions suggestions) {
        this.id = n;
        this.suggestions = suggestions;
    }

    public ClientboundCommandSuggestionsPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.id = friendlyByteBuf2.readVarInt();
        int n = friendlyByteBuf2.readVarInt();
        int n2 = friendlyByteBuf2.readVarInt();
        StringRange stringRange = StringRange.between((int)n, (int)(n + n2));
        List<Suggestion> list = friendlyByteBuf2.readList(friendlyByteBuf -> {
            String string = friendlyByteBuf.readUtf();
            Component component = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readComponent() : null;
            return new Suggestion(stringRange, string, (Message)component);
        });
        this.suggestions = new Suggestions(stringRange, list);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeVarInt(this.id);
        friendlyByteBuf2.writeVarInt(this.suggestions.getRange().getStart());
        friendlyByteBuf2.writeVarInt(this.suggestions.getRange().getLength());
        friendlyByteBuf2.writeCollection(this.suggestions.getList(), (friendlyByteBuf, suggestion) -> {
            friendlyByteBuf.writeUtf(suggestion.getText());
            friendlyByteBuf.writeBoolean(suggestion.getTooltip() != null);
            if (suggestion.getTooltip() != null) {
                friendlyByteBuf.writeComponent(ComponentUtils.fromMessage(suggestion.getTooltip()));
            }
        });
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleCommandSuggestions(this);
    }

    public int getId() {
        return this.id;
    }

    public Suggestions getSuggestions() {
        return this.suggestions;
    }
}

