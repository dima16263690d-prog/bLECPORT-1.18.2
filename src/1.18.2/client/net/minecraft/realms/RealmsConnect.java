/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.realms;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.RealmsServer;
import java.net.InetSocketAddress;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import org.slf4j.Logger;

public class RealmsConnect {
    static final Logger LOGGER = LogUtils.getLogger();
    final Screen onlineScreen;
    volatile boolean aborted;
    @Nullable
    Connection connection;

    public RealmsConnect(Screen screen) {
        this.onlineScreen = screen;
    }

    public void connect(final RealmsServer realmsServer, ServerAddress serverAddress) {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.setConnectedToRealms(true);
        minecraft.prepareForMultiplayer();
        NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("mco.connect.success"));
        final String string = serverAddress.getHost();
        final int n = serverAddress.getPort();
        new Thread("Realms-connect-task"){

            @Override
            public void run() {
                InetSocketAddress inetSocketAddress = null;
                try {
                    inetSocketAddress = new InetSocketAddress(string, n);
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection = Connection.connectToServer(inetSocketAddress, minecraft.options.useNativeTransport());
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection.setListener(new ClientHandshakePacketListenerImpl(RealmsConnect.this.connection, minecraft, RealmsConnect.this.onlineScreen, component -> {}));
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection.send(new ClientIntentionPacket(string, n, ConnectionProtocol.LOGIN));
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    RealmsConnect.this.connection.send(new ServerboundHelloPacket(minecraft.getUser().getGameProfile()));
                    minecraft.setCurrentServer(realmsServer.toServerData(string));
                }
                catch (Exception exception) {
                    Object object;
                    minecraft.getClientPackSource().clearServerPack();
                    if (RealmsConnect.this.aborted) {
                        return;
                    }
                    LOGGER.error("Couldn't connect to world", (Throwable)exception);
                    String string2 = exception.toString();
                    if (inetSocketAddress != null) {
                        object = inetSocketAddress + ":" + n;
                        string2 = string2.replaceAll((String)object, "");
                    }
                    object = new DisconnectedRealmsScreen(RealmsConnect.this.onlineScreen, CommonComponents.CONNECT_FAILED, new TranslatableComponent("disconnect.genericReason", string2));
                    minecraft.execute(() -> 1.lambda$run$1(minecraft, (DisconnectedRealmsScreen)object));
                }
            }

            private static /* synthetic */ void lambda$run$1(Minecraft minecraft2, DisconnectedRealmsScreen disconnectedRealmsScreen) {
                minecraft2.setScreen(disconnectedRealmsScreen);
            }
        }.start();
    }

    public void abort() {
        this.aborted = true;
        if (this.connection != null && this.connection.isConnected()) {
            this.connection.disconnect(new TranslatableComponent("disconnect.genericReason"));
            this.connection.handleDisconnection();
        }
    }

    public void tick() {
        if (this.connection != null) {
            if (this.connection.isConnected()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }
    }
}

