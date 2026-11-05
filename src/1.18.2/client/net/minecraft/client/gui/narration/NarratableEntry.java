/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.narration;

import net.minecraft.client.gui.narration.NarrationSupplier;

public interface NarratableEntry
extends NarrationSupplier {
    public NarrationPriority narrationPriority();

    default public boolean isActive() {
        return true;
    }

    public static enum NarrationPriority {
        NONE,
        HOVERED,
        FOCUSED;


        public boolean isTerminal() {
            return this == FOCUSED;
        }
    }
}

