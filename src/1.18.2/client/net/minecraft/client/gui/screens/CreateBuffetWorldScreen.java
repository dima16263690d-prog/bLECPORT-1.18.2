/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ibm.icu.text.Collator
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import com.ibm.icu.text.Collator;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class CreateBuffetWorldScreen
extends Screen {
    private static final Component BIOME_SELECT_INFO = new TranslatableComponent("createWorld.customize.buffet.biome");
    private final Screen parent;
    private final Consumer<Holder<Biome>> applySettings;
    final Registry<Biome> biomes;
    private BiomeList list;
    Holder<Biome> biome;
    private Button doneButton;

    public CreateBuffetWorldScreen(Screen screen, RegistryAccess registryAccess, Consumer<Holder<Biome>> consumer, Holder<Biome> holder) {
        super(new TranslatableComponent("createWorld.customize.buffet.title"));
        this.parent = screen;
        this.applySettings = consumer;
        this.biome = holder;
        this.biomes = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.list = new BiomeList();
        this.addWidget(this.list);
        this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 28, 150, 20, CommonComponents.GUI_DONE, button -> {
            this.applySettings.accept(this.biome);
            this.minecraft.setScreen(this.parent);
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)));
        this.list.setSelected((BiomeList.Entry)this.list.children().stream().filter(entry -> Objects.equals(entry.biome, this.biome)).findFirst().orElse(null));
    }

    void updateButtonValidity() {
        this.doneButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(PoseStack poseStack, int n, int n2, float f) {
        this.renderDirtBackground(0);
        this.list.render(poseStack, n, n2, f);
        CreateBuffetWorldScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        CreateBuffetWorldScreen.drawCenteredString(poseStack, this.font, BIOME_SELECT_INFO, this.width / 2, 28, 0xA0A0A0);
        super.render(poseStack, n, n2, f);
    }

    class BiomeList
    extends ObjectSelectionList<Entry> {
        BiomeList() {
            super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height, 40, CreateBuffetWorldScreen.this.height - 37, 16);
            Collator collator = Collator.getInstance((Locale)Locale.getDefault());
            CreateBuffetWorldScreen.this.biomes.holders().map(reference -> new Entry((Holder.Reference<Biome>)reference)).sorted(Comparator.comparing(entry -> entry.name.getString(), collator)).forEach(entry -> this.addEntry(entry));
        }

        @Override
        protected boolean isFocused() {
            return CreateBuffetWorldScreen.this.getFocused() == this;
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            if (entry != null) {
                CreateBuffetWorldScreen.this.biome = entry.biome;
            }
            CreateBuffetWorldScreen.this.updateButtonValidity();
        }

        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            final Holder.Reference<Biome> biome;
            final Component name;

            public Entry(Holder.Reference<Biome> reference) {
                this.biome = reference;
                ResourceLocation resourceLocation = reference.key().location();
                String string = "biome." + resourceLocation.getNamespace() + "." + resourceLocation.getPath();
                this.name = Language.getInstance().has(string) ? new TranslatableComponent(string) : new TextComponent(resourceLocation.toString());
            }

            @Override
            public Component getNarration() {
                return new TranslatableComponent("narrator.select", this.name);
            }

            @Override
            public void render(PoseStack poseStack, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                GuiComponent.drawString(poseStack, CreateBuffetWorldScreen.this.font, this.name, n3 + 5, n2 + 2, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                if (n == 0) {
                    BiomeList.this.setSelected(this);
                    return true;
                }
                return false;
            }
        }
    }
}

