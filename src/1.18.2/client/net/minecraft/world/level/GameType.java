/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Abilities;

public enum GameType {
    SURVIVAL(0, "survival"),
    CREATIVE(1, "creative"),
    ADVENTURE(2, "adventure"),
    SPECTATOR(3, "spectator");

    public static final GameType DEFAULT_MODE;
    private static final int NOT_SET = -1;
    private final int id;
    private final String name;
    private final Component shortName;
    private final Component longName;

    private GameType(int n2, String string2) {
        this.id = n2;
        this.name = string2;
        this.shortName = new TranslatableComponent("selectWorld.gameMode." + string2);
        this.longName = new TranslatableComponent("gameMode." + string2);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Component getLongDisplayName() {
        return this.longName;
    }

    public Component getShortDisplayName() {
        return this.shortName;
    }

    public void updatePlayerAbilities(Abilities abilities) {
        if (this == CREATIVE) {
            abilities.mayfly = true;
            abilities.instabuild = true;
            abilities.invulnerable = true;
        } else if (this == SPECTATOR) {
            abilities.mayfly = true;
            abilities.instabuild = false;
            abilities.invulnerable = true;
            abilities.flying = true;
        } else {
            abilities.mayfly = false;
            abilities.instabuild = false;
            abilities.invulnerable = false;
            abilities.flying = false;
        }
        abilities.mayBuild = !this.isBlockPlacingRestricted();
    }

    public boolean isBlockPlacingRestricted() {
        return this == ADVENTURE || this == SPECTATOR;
    }

    public boolean isCreative() {
        return this == CREATIVE;
    }

    public boolean isSurvival() {
        return this == SURVIVAL || this == ADVENTURE;
    }

    public static GameType byId(int n) {
        return GameType.byId(n, DEFAULT_MODE);
    }

    public static GameType byId(int n, GameType gameType) {
        for (GameType gameType2 : GameType.values()) {
            if (gameType2.id != n) continue;
            return gameType2;
        }
        return gameType;
    }

    public static GameType byName(String string) {
        return GameType.byName(string, SURVIVAL);
    }

    public static GameType byName(String string, GameType gameType) {
        for (GameType gameType2 : GameType.values()) {
            if (!gameType2.name.equals(string)) continue;
            return gameType2;
        }
        return gameType;
    }

    public static int getNullableId(@Nullable GameType gameType) {
        return gameType != null ? gameType.id : -1;
    }

    @Nullable
    public static GameType byNullableId(int n) {
        if (n == -1) {
            return null;
        }
        return GameType.byId(n);
    }

    static {
        DEFAULT_MODE = SURVIVAL;
    }
}

