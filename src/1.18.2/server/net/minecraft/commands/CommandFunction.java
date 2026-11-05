/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;

public class CommandFunction {
    private final Entry[] entries;
    final ResourceLocation id;

    public CommandFunction(ResourceLocation resourceLocation, Entry[] entryArray) {
        this.id = resourceLocation;
        this.entries = entryArray;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public Entry[] getEntries() {
        return this.entries;
    }

    public static CommandFunction fromLines(ResourceLocation resourceLocation, CommandDispatcher<CommandSourceStack> commandDispatcher, CommandSourceStack commandSourceStack, List<String> list) {
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)list.size());
        for (int i = 0; i < list.size(); ++i) {
            Object object;
            int n = i + 1;
            String string = list.get(i).trim();
            StringReader stringReader = new StringReader(string);
            if (!stringReader.canRead() || stringReader.peek() == '#') continue;
            if (stringReader.peek() == '/') {
                stringReader.skip();
                if (stringReader.peek() == '/') {
                    throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + n + " (if you intended to make a comment, use '#' not '//')");
                }
                object = stringReader.readUnquotedString();
                throw new IllegalArgumentException("Unknown or invalid command '" + string + "' on line " + n + " (did you mean '" + (String)object + "'? Do not use a preceding forwards slash.)");
            }
            try {
                object = commandDispatcher.parse(stringReader, (Object)commandSourceStack);
                if (object.getReader().canRead()) {
                    throw Commands.getParseException(object);
                }
                arrayList.add(new CommandEntry((ParseResults<CommandSourceStack>)object));
                continue;
            }
            catch (CommandSyntaxException commandSyntaxException) {
                throw new IllegalArgumentException("Whilst parsing command on line " + n + ": " + commandSyntaxException.getMessage());
            }
        }
        return new CommandFunction(resourceLocation, arrayList.toArray(new Entry[0]));
    }

    @FunctionalInterface
    public static interface Entry {
        public void execute(ServerFunctionManager var1, CommandSourceStack var2, Deque<ServerFunctionManager.QueuedCommand> var3, int var4, int var5, @Nullable ServerFunctionManager.TraceCallbacks var6) throws CommandSyntaxException;
    }

    public static class CommandEntry
    implements Entry {
        private final ParseResults<CommandSourceStack> parse;

        public CommandEntry(ParseResults<CommandSourceStack> parseResults) {
            this.parse = parseResults;
        }

        @Override
        public void execute(ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, Deque<ServerFunctionManager.QueuedCommand> deque, int n, int n2, @Nullable ServerFunctionManager.TraceCallbacks traceCallbacks) throws CommandSyntaxException {
            if (traceCallbacks != null) {
                String string = this.parse.getReader().getString();
                traceCallbacks.onCommand(n2, string);
                int n3 = this.execute(serverFunctionManager, commandSourceStack);
                traceCallbacks.onReturn(n2, string, n3);
            } else {
                this.execute(serverFunctionManager, commandSourceStack);
            }
        }

        private int execute(ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack) throws CommandSyntaxException {
            return serverFunctionManager.getDispatcher().execute(new ParseResults(this.parse.getContext().withSource((Object)commandSourceStack), this.parse.getReader(), this.parse.getExceptions()));
        }

        public String toString() {
            return this.parse.getReader().getString();
        }
    }

    public static class CacheableFunction {
        public static final CacheableFunction NONE = new CacheableFunction((ResourceLocation)null);
        @Nullable
        private final ResourceLocation id;
        private boolean resolved;
        private Optional<CommandFunction> function = Optional.empty();

        public CacheableFunction(@Nullable ResourceLocation resourceLocation) {
            this.id = resourceLocation;
        }

        public CacheableFunction(CommandFunction commandFunction) {
            this.resolved = true;
            this.id = null;
            this.function = Optional.of(commandFunction);
        }

        public Optional<CommandFunction> get(ServerFunctionManager serverFunctionManager) {
            if (!this.resolved) {
                if (this.id != null) {
                    this.function = serverFunctionManager.get(this.id);
                }
                this.resolved = true;
            }
            return this.function;
        }

        @Nullable
        public ResourceLocation getId() {
            return this.function.map(commandFunction -> commandFunction.id).orElse(this.id);
        }
    }

    public static class FunctionEntry
    implements Entry {
        private final CacheableFunction function;

        public FunctionEntry(CommandFunction commandFunction) {
            this.function = new CacheableFunction(commandFunction);
        }

        @Override
        public void execute(ServerFunctionManager serverFunctionManager, CommandSourceStack commandSourceStack, Deque<ServerFunctionManager.QueuedCommand> deque, int n, int n2, @Nullable ServerFunctionManager.TraceCallbacks traceCallbacks) {
            Util.ifElse(this.function.get(serverFunctionManager), commandFunction -> {
                Entry[] entryArray = commandFunction.getEntries();
                if (traceCallbacks != null) {
                    traceCallbacks.onCall(n2, commandFunction.getId(), entryArray.length);
                }
                int n3 = n - deque.size();
                int n4 = Math.min(entryArray.length, n3);
                for (int i = n4 - 1; i >= 0; --i) {
                    deque.addFirst(new ServerFunctionManager.QueuedCommand(commandSourceStack, n2 + 1, entryArray[i]));
                }
            }, () -> {
                if (traceCallbacks != null) {
                    traceCallbacks.onCall(n2, this.function.getId(), -1);
                }
            });
        }

        public String toString() {
            return "function " + this.function.getId();
        }
    }
}

