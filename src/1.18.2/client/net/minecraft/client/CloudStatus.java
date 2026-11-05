/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client;

public enum CloudStatus {
    OFF("options.off"),
    FAST("options.clouds.fast"),
    FANCY("options.clouds.fancy");

    private final String key;

    private CloudStatus(String string2) {
        this.key = string2;
    }

    public String getKey() {
        return this.key;
    }
}

