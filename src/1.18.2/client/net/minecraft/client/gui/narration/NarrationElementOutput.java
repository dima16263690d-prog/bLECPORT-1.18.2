/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.gui.narration;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationThunk;
import net.minecraft.network.chat.Component;

public interface NarrationElementOutput {
    default public void add(NarratedElementType narratedElementType, Component component) {
        this.add(narratedElementType, NarrationThunk.from(component.getString()));
    }

    default public void add(NarratedElementType narratedElementType, String string) {
        this.add(narratedElementType, NarrationThunk.from(string));
    }

    default public void add(NarratedElementType narratedElementType, Component ... componentArray) {
        this.add(narratedElementType, NarrationThunk.from((List<Component>)ImmutableList.copyOf((Object[])componentArray)));
    }

    public void add(NarratedElementType var1, NarrationThunk<?> var2);

    public NarrationElementOutput nest();
}

