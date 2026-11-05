/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.saveddata.maps;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class MapDecoration {
    private final Type type;
    private final byte x;
    private final byte y;
    private final byte rot;
    @Nullable
    private final Component name;

    public MapDecoration(Type type, byte by, byte by2, byte by3, @Nullable Component component) {
        this.type = type;
        this.x = by;
        this.y = by2;
        this.rot = by3;
        this.name = component;
    }

    public byte getImage() {
        return this.type.getIcon();
    }

    public Type getType() {
        return this.type;
    }

    public byte getX() {
        return this.x;
    }

    public byte getY() {
        return this.y;
    }

    public byte getRot() {
        return this.rot;
    }

    public boolean renderOnFrame() {
        return this.type.isRenderedOnFrame();
    }

    @Nullable
    public Component getName() {
        return this.name;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MapDecoration)) {
            return false;
        }
        MapDecoration mapDecoration = (MapDecoration)object;
        return this.type == mapDecoration.type && this.rot == mapDecoration.rot && this.x == mapDecoration.x && this.y == mapDecoration.y && Objects.equals(this.name, mapDecoration.name);
    }

    public int hashCode() {
        int n = this.type.getIcon();
        n = 31 * n + this.x;
        n = 31 * n + this.y;
        n = 31 * n + this.rot;
        n = 31 * n + Objects.hashCode(this.name);
        return n;
    }

    public static enum Type {
        PLAYER(false, true),
        FRAME(true, true),
        RED_MARKER(false, true),
        BLUE_MARKER(false, true),
        TARGET_X(true, false),
        TARGET_POINT(true, false),
        PLAYER_OFF_MAP(false, true),
        PLAYER_OFF_LIMITS(false, true),
        MANSION(true, 5393476, false),
        MONUMENT(true, 3830373, false),
        BANNER_WHITE(true, true),
        BANNER_ORANGE(true, true),
        BANNER_MAGENTA(true, true),
        BANNER_LIGHT_BLUE(true, true),
        BANNER_YELLOW(true, true),
        BANNER_LIME(true, true),
        BANNER_PINK(true, true),
        BANNER_GRAY(true, true),
        BANNER_LIGHT_GRAY(true, true),
        BANNER_CYAN(true, true),
        BANNER_PURPLE(true, true),
        BANNER_BLUE(true, true),
        BANNER_BROWN(true, true),
        BANNER_GREEN(true, true),
        BANNER_RED(true, true),
        BANNER_BLACK(true, true),
        RED_X(true, false);

        private final byte icon;
        private final boolean renderedOnFrame;
        private final int mapColor;
        private final boolean trackCount;

        private Type(boolean bl, boolean bl2) {
            this(bl, -1, bl2);
        }

        private Type(boolean bl, int n2, boolean bl2) {
            this.trackCount = bl2;
            this.icon = (byte)this.ordinal();
            this.renderedOnFrame = bl;
            this.mapColor = n2;
        }

        public byte getIcon() {
            return this.icon;
        }

        public boolean isRenderedOnFrame() {
            return this.renderedOnFrame;
        }

        public boolean hasMapColor() {
            return this.mapColor >= 0;
        }

        public int getMapColor() {
            return this.mapColor;
        }

        public static Type byIcon(byte by) {
            return Type.values()[Mth.clamp((int)by, 0, Type.values().length - 1)];
        }

        public boolean shouldTrackCount() {
            return this.trackCount;
        }
    }
}

