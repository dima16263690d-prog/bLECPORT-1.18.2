/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;

public class ClientTextTooltip
implements ClientTooltipComponent {
    private final FormattedCharSequence text;

    public ClientTextTooltip(FormattedCharSequence formattedCharSequence) {
        this.text = formattedCharSequence;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(this.text);
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void renderText(Font font, int n, int n2, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        font.drawInBatch(this.text, (float)n, (float)n2, -1, true, matrix4f, (MultiBufferSource)bufferSource, false, 0, 0xF000F0);
    }
}

