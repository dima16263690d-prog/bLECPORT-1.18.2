/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.hash.Hashing
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.Validate
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerSelectionList
extends ObjectSelectionList<Entry> {
    static final Logger LOGGER = LogUtils.getLogger();
    static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder().setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler((Thread.UncaughtExceptionHandler)new DefaultUncaughtExceptionHandler(LOGGER)).build());
    static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");
    static final Component SCANNING_LABEL = new TranslatableComponent("lanServer.scanning");
    static final Component CANT_RESOLVE_TEXT = new TranslatableComponent("multiplayer.status.cannot_resolve").withStyle(ChatFormatting.DARK_RED);
    static final Component CANT_CONNECT_TEXT = new TranslatableComponent("multiplayer.status.cannot_connect").withStyle(ChatFormatting.DARK_RED);
    static final Component INCOMPATIBLE_TOOLTIP = new TranslatableComponent("multiplayer.status.incompatible");
    static final Component NO_CONNECTION_TOOLTIP = new TranslatableComponent("multiplayer.status.no_connection");
    static final Component PINGING_TOOLTIP = new TranslatableComponent("multiplayer.status.pinging");
    private final JoinMultiplayerScreen screen;
    private final List<OnlineServerEntry> onlineServers = Lists.newArrayList();
    private final Entry lanHeader = new LANHeader();
    private final List<NetworkServerEntry> networkServers = Lists.newArrayList();

    public ServerSelectionList(JoinMultiplayerScreen joinMultiplayerScreen, Minecraft minecraft, int n, int n2, int n3, int n4, int n5) {
        super(minecraft, n, n2, n3, n4, n5);
        this.screen = joinMultiplayerScreen;
    }

    private void refreshEntries() {
        this.clearEntries();
        this.onlineServers.forEach(entry -> this.addEntry(entry));
        this.addEntry(this.lanHeader);
        this.networkServers.forEach(entry -> this.addEntry(entry));
    }

    @Override
    public void setSelected(@Nullable Entry entry) {
        super.setSelected(entry);
        this.screen.onSelectedChange();
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        Entry entry = (Entry)this.getSelected();
        return entry != null && entry.keyPressed(n, n2, n3) || super.keyPressed(n, n2, n3);
    }

    @Override
    protected void moveSelection(AbstractSelectionList.SelectionDirection selectionDirection) {
        this.moveSelection(selectionDirection, entry -> !(entry instanceof LANHeader));
    }

    public void updateOnlineServers(ServerList serverList) {
        this.onlineServers.clear();
        for (int i = 0; i < serverList.size(); ++i) {
            this.onlineServers.add(new OnlineServerEntry(this.screen, serverList.get(i)));
        }
        this.refreshEntries();
    }

    public void updateNetworkServers(List<LanServer> list) {
        this.networkServers.clear();
        for (LanServer lanServer : list) {
            this.networkServers.add(new NetworkServerEntry(this.screen, lanServer));
        }
        this.refreshEntries();
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 30;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    @Override
    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    public static class LANHeader
    extends Entry {
        private final Minecraft minecraft = Minecraft.getInstance();

        @Override
        public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            int n8 = n2 + n5 / 2 - this.minecraft.font.lineHeight / 2;
            this.minecraft.font.draw(poseStack, SCANNING_LABEL, (float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(SCANNING_LABEL) / 2), (float)n8, 0xFFFFFF);
            String string = switch ((int)(Util.getMillis() / 300L % 4L)) {
                default -> "O o o";
                case 1, 3 -> "o O o";
                case 2 -> "o o O";
            };
            this.minecraft.font.draw(poseStack, string, (float)(this.minecraft.screen.width / 2 - this.minecraft.font.width(string) / 2), (float)(n8 + this.minecraft.font.lineHeight), 0x808080);
        }

        @Override
        public Component getNarration() {
            return TextComponent.EMPTY;
        }
    }

    public static abstract class Entry
    extends ObjectSelectionList.Entry<Entry> {
    }

    public class OnlineServerEntry
    extends Entry {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
        private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
        private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
        private static final int ICON_OVERLAY_X_MOVE_UP = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        private final JoinMultiplayerScreen screen;
        private final Minecraft minecraft;
        private final ServerData serverData;
        private final ResourceLocation iconLocation;
        @Nullable
        private String lastIconB64;
        @Nullable
        private DynamicTexture icon;
        private long lastClickTime;

        protected OnlineServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, ServerData serverData) {
            this.screen = joinMultiplayerScreen;
            this.serverData = serverData;
            this.minecraft = Minecraft.getInstance();
            this.iconLocation = new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars((CharSequence)serverData.ip) + "/icon");
            AbstractTexture abstractTexture = this.minecraft.getTextureManager().getTexture(this.iconLocation, MissingTextureAtlasSprite.getTexture());
            if (abstractTexture != MissingTextureAtlasSprite.getTexture() && abstractTexture instanceof DynamicTexture) {
                this.icon = (DynamicTexture)abstractTexture;
            }
        }

        @Override
        public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            List<Component> list;
            Component component;
            int n8;
            if (!this.serverData.pinged) {
                this.serverData.pinged = true;
                this.serverData.ping = -2L;
                this.serverData.motd = TextComponent.EMPTY;
                this.serverData.status = TextComponent.EMPTY;
                THREAD_POOL.submit(() -> {
                    try {
                        this.screen.getPinger().pingServer(this.serverData, () -> this.minecraft.execute(this::updateServerList));
                    }
                    catch (UnknownHostException unknownHostException) {
                        this.serverData.ping = -1L;
                        this.serverData.motd = CANT_RESOLVE_TEXT;
                    }
                    catch (Exception exception) {
                        this.serverData.ping = -1L;
                        this.serverData.motd = CANT_CONNECT_TEXT;
                    }
                });
            }
            boolean bl2 = this.serverData.protocol != SharedConstants.getCurrentVersion().getProtocolVersion();
            this.minecraft.font.draw(poseStack, this.serverData.name, (float)(n3 + 32 + 3), (float)(n2 + 1), 0xFFFFFF);
            List<FormattedCharSequence> list2 = this.minecraft.font.split(this.serverData.motd, n4 - 32 - 2);
            for (int i = 0; i < Math.min(list2.size(), 2); ++i) {
                this.minecraft.font.draw(poseStack, list2.get(i), (float)(n3 + 32 + 3), (float)(n2 + 12 + this.minecraft.font.lineHeight * i), 0x808080);
            }
            Component component2 = bl2 ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status;
            int n9 = this.minecraft.font.width(component2);
            this.minecraft.font.draw(poseStack, component2, (float)(n3 + n4 - n9 - 15 - 2), (float)(n2 + 1), 0x808080);
            int n10 = 0;
            if (bl2) {
                n8 = 5;
                component = INCOMPATIBLE_TOOLTIP;
                list = this.serverData.playerList;
            } else if (this.serverData.pinged && this.serverData.ping != -2L) {
                n8 = this.serverData.ping < 0L ? 5 : (this.serverData.ping < 150L ? 0 : (this.serverData.ping < 300L ? 1 : (this.serverData.ping < 600L ? 2 : (this.serverData.ping < 1000L ? 3 : 4))));
                if (this.serverData.ping < 0L) {
                    component = NO_CONNECTION_TOOLTIP;
                    list = Collections.emptyList();
                } else {
                    component = new TranslatableComponent("multiplayer.status.ping", this.serverData.ping);
                    list = this.serverData.playerList;
                }
            } else {
                n10 = 1;
                n8 = (int)(Util.getMillis() / 100L + (long)(n * 2) & 7L);
                if (n8 > 4) {
                    n8 = 8 - n8;
                }
                component = PINGING_TOOLTIP;
                list = Collections.emptyList();
            }
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            GuiComponent.blit(poseStack, n3 + n4 - 15, n2, n10 * 10, 176 + n8 * 8, 10, 8, 256, 256);
            String string = this.serverData.getIconB64();
            if (!Objects.equals(string, this.lastIconB64)) {
                if (this.uploadServerIcon(string)) {
                    this.lastIconB64 = string;
                } else {
                    this.serverData.setIconB64(null);
                    this.updateServerList();
                }
            }
            if (this.icon == null) {
                this.drawIcon(poseStack, n3, n2, ICON_MISSING);
            } else {
                this.drawIcon(poseStack, n3, n2, this.iconLocation);
            }
            int n11 = n6 - n3;
            int n12 = n7 - n2;
            if (n11 >= n4 - 15 && n11 <= n4 - 5 && n12 >= 0 && n12 <= 8) {
                this.screen.setToolTip(Collections.singletonList(component));
            } else if (n11 >= n4 - n9 - 15 - 2 && n11 <= n4 - 15 - 2 && n12 >= 0 && n12 <= 8) {
                this.screen.setToolTip(list);
            }
            if (this.minecraft.options.touchscreen || bl) {
                RenderSystem.setShaderTexture(0, ICON_OVERLAY_LOCATION);
                GuiComponent.fill(poseStack, n3, n2, n3 + 32, n2 + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                int n13 = n6 - n3;
                int n14 = n7 - n2;
                if (this.canJoin()) {
                    if (n13 < 32 && n13 > 16) {
                        GuiComponent.blit(poseStack, n3, n2, 0.0f, 32.0f, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(poseStack, n3, n2, 0.0f, 0.0f, 32, 32, 256, 256);
                    }
                }
                if (n > 0) {
                    if (n13 < 16 && n14 < 16) {
                        GuiComponent.blit(poseStack, n3, n2, 96.0f, 32.0f, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(poseStack, n3, n2, 96.0f, 0.0f, 32, 32, 256, 256);
                    }
                }
                if (n < this.screen.getServers().size() - 1) {
                    if (n13 < 16 && n14 > 16) {
                        GuiComponent.blit(poseStack, n3, n2, 64.0f, 32.0f, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(poseStack, n3, n2, 64.0f, 0.0f, 32, 32, 256, 256);
                    }
                }
            }
        }

        public void updateServerList() {
            this.screen.getServers().save();
        }

        protected void drawIcon(PoseStack poseStack, int n, int n2, ResourceLocation resourceLocation) {
            RenderSystem.setShaderTexture(0, resourceLocation);
            RenderSystem.enableBlend();
            GuiComponent.blit(poseStack, n, n2, 0.0f, 0.0f, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        private boolean canJoin() {
            return true;
        }

        private boolean uploadServerIcon(@Nullable String string) {
            if (string == null) {
                this.minecraft.getTextureManager().release(this.iconLocation);
                if (this.icon != null && this.icon.getPixels() != null) {
                    this.icon.getPixels().close();
                }
                this.icon = null;
            } else {
                try {
                    NativeImage nativeImage = NativeImage.fromBase64(string);
                    Validate.validState((nativeImage.getWidth() == 64 ? 1 : 0) != 0, (String)"Must be 64 pixels wide", (Object[])new Object[0]);
                    Validate.validState((nativeImage.getHeight() == 64 ? 1 : 0) != 0, (String)"Must be 64 pixels high", (Object[])new Object[0]);
                    if (this.icon == null) {
                        this.icon = new DynamicTexture(nativeImage);
                    } else {
                        this.icon.setPixels(nativeImage);
                        this.icon.upload();
                    }
                    this.minecraft.getTextureManager().register(this.iconLocation, (AbstractTexture)this.icon);
                }
                catch (Throwable throwable) {
                    LOGGER.error("Invalid icon for server {} ({})", new Object[]{this.serverData.name, this.serverData.ip, throwable});
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean keyPressed(int n, int n2, int n3) {
            if (Screen.hasShiftDown()) {
                ServerSelectionList serverSelectionList = this.screen.serverSelectionList;
                int n4 = serverSelectionList.children().indexOf(this);
                if (n4 == -1) {
                    return true;
                }
                if (n == 264 && n4 < this.screen.getServers().size() - 1 || n == 265 && n4 > 0) {
                    this.swap(n4, n == 264 ? n4 + 1 : n4 - 1);
                    return true;
                }
            }
            return super.keyPressed(n, n2, n3);
        }

        private void swap(int n, int n2) {
            this.screen.getServers().swap(n, n2);
            this.screen.serverSelectionList.updateOnlineServers(this.screen.getServers());
            Entry entry = (Entry)this.screen.serverSelectionList.children().get(n2);
            this.screen.serverSelectionList.setSelected(entry);
            ServerSelectionList.this.ensureVisible(entry);
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            double d3 = d - (double)ServerSelectionList.this.getRowLeft();
            double d4 = d2 - (double)ServerSelectionList.this.getRowTop(ServerSelectionList.this.children().indexOf(this));
            if (d3 <= 32.0) {
                if (d3 < 32.0 && d3 > 16.0 && this.canJoin()) {
                    this.screen.setSelected(this);
                    this.screen.joinSelectedServer();
                    return true;
                }
                int n2 = this.screen.serverSelectionList.children().indexOf(this);
                if (d3 < 16.0 && d4 < 16.0 && n2 > 0) {
                    this.swap(n2, n2 - 1);
                    return true;
                }
                if (d3 < 16.0 && d4 > 16.0 && n2 < this.screen.getServers().size() - 1) {
                    this.swap(n2, n2 + 1);
                    return true;
                }
            }
            this.screen.setSelected(this);
            if (Util.getMillis() - this.lastClickTime < 250L) {
                this.screen.joinSelectedServer();
            }
            this.lastClickTime = Util.getMillis();
            return false;
        }

        public ServerData getServerData() {
            return this.serverData;
        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", this.serverData.name);
        }
    }

    public static class NetworkServerEntry
    extends Entry {
        private static final int ICON_WIDTH = 32;
        private static final Component LAN_SERVER_HEADER = new TranslatableComponent("lanServer.title");
        private static final Component HIDDEN_ADDRESS_TEXT = new TranslatableComponent("selectServer.hiddenAddress");
        private final JoinMultiplayerScreen screen;
        protected final Minecraft minecraft;
        protected final LanServer serverData;
        private long lastClickTime;

        protected NetworkServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, LanServer lanServer) {
            this.screen = joinMultiplayerScreen;
            this.serverData = lanServer;
            this.minecraft = Minecraft.getInstance();
        }

        @Override
        public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            this.minecraft.font.draw(poseStack, LAN_SERVER_HEADER, (float)(n3 + 32 + 3), (float)(n2 + 1), 0xFFFFFF);
            this.minecraft.font.draw(poseStack, this.serverData.getMotd(), (float)(n3 + 32 + 3), (float)(n2 + 12), 0x808080);
            if (this.minecraft.options.hideServerAddress) {
                this.minecraft.font.draw(poseStack, HIDDEN_ADDRESS_TEXT, (float)(n3 + 32 + 3), (float)(n2 + 12 + 11), 0x303030);
            } else {
                this.minecraft.font.draw(poseStack, this.serverData.getAddress(), (float)(n3 + 32 + 3), (float)(n2 + 12 + 11), 0x303030);
            }
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            this.screen.setSelected(this);
            if (Util.getMillis() - this.lastClickTime < 250L) {
                this.screen.joinSelectedServer();
            }
            this.lastClickTime = Util.getMillis();
            return false;
        }

        public LanServer getServerData() {
            return this.serverData;
        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", new TextComponent("").append(LAN_SERVER_HEADER).append(" ").append(this.serverData.getMotd()));
        }
    }
}

