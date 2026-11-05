/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class CycleOption<T>
extends Option {
    private final OptionSetter<T> setter;
    private final Function<Options, T> getter;
    private final Supplier<CycleButton.Builder<T>> buttonSetup;
    private Function<Minecraft, CycleButton.TooltipSupplier<T>> tooltip = minecraft -> object -> ImmutableList.of();

    private CycleOption(String string, Function<Options, T> function, OptionSetter<T> optionSetter, Supplier<CycleButton.Builder<T>> supplier) {
        super(string);
        this.getter = function;
        this.setter = optionSetter;
        this.buttonSetup = supplier;
    }

    public static <T> CycleOption<T> create(String string, List<T> list, Function<T, Component> function, Function<Options, T> function2, OptionSetter<T> optionSetter) {
        return new CycleOption<T>(string, function2, optionSetter, () -> CycleButton.builder(function).withValues(list));
    }

    public static <T> CycleOption<T> create(String string, Supplier<List<T>> supplier, Function<T, Component> function, Function<Options, T> function2, OptionSetter<T> optionSetter) {
        return new CycleOption<T>(string, function2, optionSetter, () -> CycleButton.builder(function).withValues((List)supplier.get()));
    }

    public static <T> CycleOption<T> create(String string, List<T> list, List<T> list2, BooleanSupplier booleanSupplier, Function<T, Component> function, Function<Options, T> function2, OptionSetter<T> optionSetter) {
        return new CycleOption<T>(string, function2, optionSetter, () -> CycleButton.builder(function).withValues(booleanSupplier, list, list2));
    }

    public static <T> CycleOption<T> create(String string, T[] TArray, Function<T, Component> function, Function<Options, T> function2, OptionSetter<T> optionSetter) {
        return new CycleOption<T>(string, function2, optionSetter, () -> CycleButton.builder(function).withValues(TArray));
    }

    public static CycleOption<Boolean> createBinaryOption(String string, Component component, Component component2, Function<Options, Boolean> function, OptionSetter<Boolean> optionSetter) {
        return new CycleOption<Boolean>(string, function, optionSetter, () -> CycleButton.booleanBuilder(component, component2));
    }

    public static CycleOption<Boolean> createOnOff(String string, Function<Options, Boolean> function, OptionSetter<Boolean> optionSetter) {
        return new CycleOption<Boolean>(string, function, optionSetter, CycleButton::onOffBuilder);
    }

    public static CycleOption<Boolean> createOnOff(String string, Component component, Function<Options, Boolean> function, OptionSetter<Boolean> optionSetter) {
        return CycleOption.createOnOff(string, function, optionSetter).setTooltip(minecraft -> {
            List<FormattedCharSequence> list = minecraft.font.split(component, 200);
            return bl -> list;
        });
    }

    public CycleOption<T> setTooltip(Function<Minecraft, CycleButton.TooltipSupplier<T>> function) {
        this.tooltip = function;
        return this;
    }

    @Override
    public AbstractWidget createButton(Options options, int n, int n2, int n3) {
        CycleButton.TooltipSupplier<T> tooltipSupplier = this.tooltip.apply(Minecraft.getInstance());
        return this.buttonSetup.get().withTooltip(tooltipSupplier).withInitialValue(this.getter.apply(options)).create(n, n2, n3, 20, this.getCaption(), (cycleButton, object) -> {
            this.setter.accept(options, this, object);
            options.save();
        });
    }

    @FunctionalInterface
    public static interface OptionSetter<T> {
        public void accept(Options var1, Option var2, T var3);
    }
}

