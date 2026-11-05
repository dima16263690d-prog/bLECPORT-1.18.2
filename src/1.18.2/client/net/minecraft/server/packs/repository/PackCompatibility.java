/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public enum PackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Component description;
    private final Component confirmation;

    private PackCompatibility(String string2) {
        this.description = new TranslatableComponent("pack.incompatible." + string2).withStyle(ChatFormatting.GRAY);
        this.confirmation = new TranslatableComponent("pack.incompatible.confirm." + string2);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static PackCompatibility forFormat(int n, PackType packType) {
        int n2 = packType.getVersion(SharedConstants.getCurrentVersion());
        if (n < n2) {
            return TOO_OLD;
        }
        if (n > n2) {
            return TOO_NEW;
        }
        return COMPATIBLE;
    }

    public static PackCompatibility forMetadata(PackMetadataSection packMetadataSection, PackType packType) {
        return PackCompatibility.forFormat(packMetadataSection.getPackFormat(), packType);
    }

    public Component getDescription() {
        return this.description;
    }

    public Component getConfirmation() {
        return this.confirmation;
    }
}

