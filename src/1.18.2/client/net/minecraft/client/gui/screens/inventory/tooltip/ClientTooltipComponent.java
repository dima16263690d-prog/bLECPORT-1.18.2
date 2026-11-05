/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public interface ClientTooltipComponent {
    public static ClientTooltipComponent create(FormattedCharSequence formattedCharSequence) {
        return new ClientTextTooltip(formattedCharSequence);
    }

    public static ClientTooltipComponent create(TooltipComponent tooltipComponent) {
        if (tooltipComponent instanceof BundleTooltip) {
            return new ClientBundleTooltip((BundleTooltip)tooltipComponent);
        }
        throw new IllegalArgumentException("Unknown TooltipComponent");
    }

    public int getHeight();

    public int getWidth(Font var1);

    default public void renderText(Font font, int n, int n2, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
    }

    default public void renderImage(Font font, int n, int n2, PoseStack poseStack, ItemRenderer itemRenderer, int n3) {
    }
}

