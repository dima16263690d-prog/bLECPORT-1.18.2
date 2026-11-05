/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.TypeAdapterFactory
 */
package net.minecraft.network.protocol.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public class ClientboundStatusResponsePacket
implements Packet<ClientStatusPacketListener> {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ServerStatus.Version.class, (Object)new ServerStatus.Version.Serializer()).registerTypeAdapter(ServerStatus.Players.class, (Object)new ServerStatus.Players.Serializer()).registerTypeAdapter(ServerStatus.class, (Object)new ServerStatus.Serializer()).registerTypeHierarchyAdapter(Component.class, (Object)new Component.Serializer()).registerTypeHierarchyAdapter(Style.class, (Object)new Style.Serializer()).registerTypeAdapterFactory((TypeAdapterFactory)new LowerCaseEnumTypeAdapterFactory()).create();
    private final ServerStatus status;

    public ClientboundStatusResponsePacket(ServerStatus serverStatus) {
        this.status = serverStatus;
    }

    public ClientboundStatusResponsePacket(FriendlyByteBuf friendlyByteBuf) {
        this.status = GsonHelper.fromJson(GSON, friendlyByteBuf.readUtf(Short.MAX_VALUE), ServerStatus.class);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(GSON.toJson((Object)this.status));
    }

    @Override
    public void handle(ClientStatusPacketListener clientStatusPacketListener) {
        clientStatusPacketListener.handleStatusResponse(this);
    }

    public ServerStatus getStatus() {
        return this.status;
    }
}

