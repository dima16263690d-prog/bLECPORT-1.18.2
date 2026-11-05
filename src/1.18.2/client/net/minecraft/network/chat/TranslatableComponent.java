/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ContextAwareComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableFormatException;
import net.minecraft.world.entity.Entity;

public class TranslatableComponent
extends BaseComponent
implements ContextAwareComponent {
    private static final Object[] NO_ARGS = new Object[0];
    private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
    private static final FormattedText TEXT_NULL = FormattedText.of("null");
    private final String key;
    private final Object[] args;
    @Nullable
    private Language decomposedWith;
    private List<FormattedText> decomposedParts = ImmutableList.of();
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public TranslatableComponent(String string) {
        this.key = string;
        this.args = NO_ARGS;
    }

    public TranslatableComponent(String string, Object ... objectArray) {
        this.key = string;
        this.args = objectArray;
    }

    private void decompose() {
        Language language = Language.getInstance();
        if (language == this.decomposedWith) {
            return;
        }
        this.decomposedWith = language;
        String string = language.getOrDefault(this.key);
        try {
            ImmutableList.Builder builder = ImmutableList.builder();
            this.decomposeTemplate(string, arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
            this.decomposedParts = builder.build();
        }
        catch (TranslatableFormatException translatableFormatException) {
            this.decomposedParts = ImmutableList.of((Object)FormattedText.of(string));
        }
    }

    private void decomposeTemplate(String string, Consumer<FormattedText> consumer) {
        Matcher matcher = FORMAT_PATTERN.matcher(string);
        try {
            int n = 0;
            int n2 = 0;
            while (matcher.find(n2)) {
                String string2;
                int n3 = matcher.start();
                int n4 = matcher.end();
                if (n3 > n2) {
                    string2 = string.substring(n2, n3);
                    if (string2.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }
                    consumer.accept(FormattedText.of(string2));
                }
                string2 = matcher.group(2);
                String string3 = string.substring(n3, n4);
                if ("%".equals(string2) && "%%".equals(string3)) {
                    consumer.accept(TEXT_PERCENT);
                } else if ("s".equals(string2)) {
                    int n5;
                    String string4 = matcher.group(1);
                    int n6 = n5 = string4 != null ? Integer.parseInt(string4) - 1 : n++;
                    if (n5 < this.args.length) {
                        consumer.accept(this.getArgument(n5));
                    }
                } else {
                    throw new TranslatableFormatException(this, "Unsupported format: '" + string3 + "'");
                }
                n2 = n4;
            }
            if (n2 < string.length()) {
                String string5 = string.substring(n2);
                if (string5.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }
                consumer.accept(FormattedText.of(string5));
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            throw new TranslatableFormatException(this, (Throwable)illegalArgumentException);
        }
    }

    private FormattedText getArgument(int n) {
        if (n >= this.args.length) {
            throw new TranslatableFormatException(this, n);
        }
        Object object = this.args[n];
        if (object instanceof Component) {
            return (Component)object;
        }
        return object == null ? TEXT_NULL : FormattedText.of(object.toString());
    }

    @Override
    public TranslatableComponent plainCopy() {
        return new TranslatableComponent(this.key, this.args);
    }

    @Override
    public <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        this.decompose();
        for (FormattedText formattedText : this.decomposedParts) {
            Optional<T> optional = formattedText.visit(styledContentConsumer, style);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> contentConsumer) {
        this.decompose();
        for (FormattedText formattedText : this.decomposedParts) {
            Optional<T> optional = formattedText.visit(contentConsumer);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int n) throws CommandSyntaxException {
        Object[] objectArray = new Object[this.args.length];
        for (int i = 0; i < objectArray.length; ++i) {
            Object object = this.args[i];
            objectArray[i] = object instanceof Component ? ComponentUtils.updateForEntity(commandSourceStack, (Component)object, entity, n) : object;
        }
        return new TranslatableComponent(this.key, objectArray);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof TranslatableComponent) {
            TranslatableComponent translatableComponent = (TranslatableComponent)object;
            return Arrays.equals(this.args, translatableComponent.args) && this.key.equals(translatableComponent.key) && super.equals(object);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int n = super.hashCode();
        n = 31 * n + this.key.hashCode();
        n = 31 * n + Arrays.hashCode(this.args);
        return n;
    }

    @Override
    public String toString() {
        return "TranslatableComponent{key='" + this.key + "', args=" + Arrays.toString(this.args) + ", siblings=" + this.siblings + ", style=" + this.getStyle() + "}";
    }

    public String getKey() {
        return this.key;
    }

    public Object[] getArgs() {
        return this.args;
    }

    @Override
    public /* synthetic */ BaseComponent plainCopy() {
        return this.plainCopy();
    }

    @Override
    public /* synthetic */ MutableComponent plainCopy() {
        return this.plainCopy();
    }
}

