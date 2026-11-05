/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class ObjectSelectionList<E extends Entry<E>>
extends AbstractSelectionList<E> {
    private static final Component USAGE_NARRATION = new TranslatableComponent("narration.selection.usage");
    private boolean inFocus;

    public ObjectSelectionList(Minecraft minecraft, int n, int n2, int n3, int n4, int n5) {
        super(minecraft, n, n2, n3, n4, n5);
    }

    @Override
    public boolean changeFocus(boolean bl) {
        if (!this.inFocus && this.getItemCount() == 0) {
            return false;
        }
        boolean bl2 = this.inFocus = !this.inFocus;
        if (this.inFocus && this.getSelected() == null && this.getItemCount() > 0) {
            this.moveSelection(AbstractSelectionList.SelectionDirection.DOWN);
        } else if (this.inFocus && this.getSelected() != null) {
            this.refreshSelection();
        }
        return this.inFocus;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        Entry entry = (Entry)this.getHovered();
        if (entry != null) {
            this.narrateListElementPosition(narrationElementOutput.nest(), entry);
            entry.updateNarration(narrationElementOutput);
        } else {
            Entry entry2 = (Entry)this.getSelected();
            if (entry2 != null) {
                this.narrateListElementPosition(narrationElementOutput.nest(), entry2);
                entry2.updateNarration(narrationElementOutput);
            }
        }
        if (this.isFocused()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
    }

    public static abstract class Entry<E extends Entry<E>>
    extends AbstractSelectionList.Entry<E>
    implements NarrationSupplier {
        @Override
        public boolean changeFocus(boolean bl) {
            return false;
        }

        public abstract Component getNarration();

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(NarratedElementType.TITLE, this.getNarration());
        }
    }
}

