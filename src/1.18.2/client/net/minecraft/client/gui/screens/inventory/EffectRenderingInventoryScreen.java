/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Ordering
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class EffectRenderingInventoryScreen<T extends AbstractContainerMenu>
extends AbstractContainerScreen<T> {
    public EffectRenderingInventoryScreen(T t, Inventory inventory, Component component) {
        super(t, inventory, component);
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        super.render(poseStack, n, n2, f);
        this.renderEffects(poseStack, n, n2);
    }

    public boolean canSeeEffects() {
        int n = this.leftPos + this.imageWidth + 2;
        int n2 = this.width - n;
        return n2 >= 32;
    }

    private void renderEffects(PoseStack poseStack, int n, int n2) {
        int n3 = this.leftPos + this.imageWidth + 2;
        int n4 = this.width - n3;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty() || n4 < 32) {
            return;
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        boolean bl = n4 >= 120;
        int n5 = 33;
        if (collection.size() > 5) {
            n5 = 132 / (collection.size() - 1);
        }
        List list = Ordering.natural().sortedCopy(collection);
        this.renderBackgrounds(poseStack, n3, n5, list, bl);
        this.renderIcons(poseStack, n3, n5, list, bl);
        if (bl) {
            this.renderLabels(poseStack, n3, n5, list);
        } else if (n >= n3 && n <= n3 + 33) {
            int n6 = this.topPos;
            MobEffectInstance mobEffectInstance = null;
            for (MobEffectInstance mobEffectInstance2 : list) {
                if (n2 >= n6 && n2 <= n6 + n5) {
                    mobEffectInstance = mobEffectInstance2;
                }
                n6 += n5;
            }
            if (mobEffectInstance != null) {
                List list2 = List.of((Object)this.getEffectName(mobEffectInstance), (Object)new TextComponent(MobEffectUtil.formatDuration(mobEffectInstance, 1.0f)));
                this.renderTooltip(poseStack, list2, Optional.empty(), n, n2);
            }
        }
    }

    private void renderBackgrounds(PoseStack poseStack, int n, int n2, Iterable<MobEffectInstance> iterable, boolean bl) {
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int n3 = this.topPos;
        for (MobEffectInstance mobEffectInstance : iterable) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (bl) {
                this.blit(poseStack, n, n3, 0, 166, 120, 32);
            } else {
                this.blit(poseStack, n, n3, 0, 198, 32, 32);
            }
            n3 += n2;
        }
    }

    private void renderIcons(PoseStack poseStack, int n, int n2, Iterable<MobEffectInstance> iterable, boolean bl) {
        MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
        int n3 = this.topPos;
        for (MobEffectInstance mobEffectInstance : iterable) {
            MobEffect mobEffect = mobEffectInstance.getEffect();
            TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
            RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
            EffectRenderingInventoryScreen.blit(poseStack, n + (bl ? 6 : 7), n3 + 7, this.getBlitOffset(), 18, 18, textureAtlasSprite);
            n3 += n2;
        }
    }

    private void renderLabels(PoseStack poseStack, int n, int n2, Iterable<MobEffectInstance> iterable) {
        int n3 = this.topPos;
        for (MobEffectInstance mobEffectInstance : iterable) {
            Component component = this.getEffectName(mobEffectInstance);
            this.font.drawShadow(poseStack, component, (float)(n + 10 + 18), (float)(n3 + 6), 0xFFFFFF);
            String string = MobEffectUtil.formatDuration(mobEffectInstance, 1.0f);
            this.font.drawShadow(poseStack, string, (float)(n + 10 + 18), (float)(n3 + 6 + 10), 0x7F7F7F);
            n3 += n2;
        }
    }

    private Component getEffectName(MobEffectInstance mobEffectInstance) {
        MutableComponent mutableComponent = mobEffectInstance.getEffect().getDisplayName().copy();
        if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
            mutableComponent.append(" ").append(new TranslatableComponent("enchantment.level." + (mobEffectInstance.getAmplifier() + 1)));
        }
        return mutableComponent;
    }
}

