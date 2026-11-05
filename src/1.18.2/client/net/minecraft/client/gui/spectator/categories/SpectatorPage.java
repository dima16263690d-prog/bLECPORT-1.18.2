/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 */
package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;

public class SpectatorPage {
    public static final int NO_SELECTION = -1;
    private final List<SpectatorMenuItem> items;
    private final int selection;

    public SpectatorPage(List<SpectatorMenuItem> list, int n) {
        this.items = list;
        this.selection = n;
    }

    public SpectatorMenuItem getItem(int n) {
        if (n < 0 || n >= this.items.size()) {
            return SpectatorMenu.EMPTY_SLOT;
        }
        return (SpectatorMenuItem)MoreObjects.firstNonNull((Object)this.items.get(n), (Object)SpectatorMenu.EMPTY_SLOT);
    }

    public int getSelectedSlot() {
        return this.selection;
    }
}

