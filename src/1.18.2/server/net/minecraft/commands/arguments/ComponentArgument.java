/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ComponentArgument
implements ArgumentType<Component> {
    private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
    public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType(object -> new TranslatableComponent("argument.component.invalid", object));

    private ComponentArgument() {
    }

    public static Component getComponent(CommandContext<CommandSourceStack> commandContext, String string) {
        return (Component)commandContext.getArgument(string, Component.class);
    }

    public static ComponentArgument textComponent() {
        return new ComponentArgument();
    }

    public Component parse(StringReader stringReader) throws CommandSyntaxException {
        try {
            MutableComponent mutableComponent = Component.Serializer.fromJson(stringReader);
            if (mutableComponent == null) {
                throw ERROR_INVALID_JSON.createWithContext((ImmutableStringReader)stringReader, (Object)"empty");
            }
            return mutableComponent;
        }
        catch (Exception exception) {
            String string = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();
            throw ERROR_INVALID_JSON.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

