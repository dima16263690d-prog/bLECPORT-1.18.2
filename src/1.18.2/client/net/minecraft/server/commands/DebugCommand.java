/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ProfileResults;
import org.slf4j.Logger;

public class DebugCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType((Message)new TranslatableComponent("commands.debug.notRunning"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType((Message)new TranslatableComponent("commands.debug.alreadyRunning"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debug").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.literal("start").executes(commandContext -> DebugCommand.start((CommandSourceStack)commandContext.getSource())))).then(Commands.literal("stop").executes(commandContext -> DebugCommand.stop((CommandSourceStack)commandContext.getSource())))).then(((LiteralArgumentBuilder)Commands.literal("function").requires(commandSourceStack -> commandSourceStack.hasPermission(3))).then(Commands.argument("name", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).executes(commandContext -> DebugCommand.traceFunction((CommandSourceStack)commandContext.getSource(), FunctionArgument.getFunctions((CommandContext<CommandSourceStack>)commandContext, "name"))))));
    }

    private static int start(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer.isTimeProfilerRunning()) {
            throw ERROR_ALREADY_RUNNING.create();
        }
        minecraftServer.startTimeProfiler();
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.started"), true);
        return 0;
    }

    private static int stop(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (!minecraftServer.isTimeProfilerRunning()) {
            throw ERROR_NOT_RUNNING.create();
        }
        ProfileResults profileResults = minecraftServer.stopTimeProfiler();
        double d = (double)profileResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
        double d2 = (double)profileResults.getTickDuration() / d;
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), profileResults.getTickDuration(), String.format("%.2f", d2)), true);
        return (int)d2;
    }

    private static int traceFunction(CommandSourceStack commandSourceStack, Collection<CommandFunction> collection) {
        int n = 0;
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        String string = "debug-trace-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt";
        try {
            Path path = minecraftServer.getFile("debug").toPath();
            Files.createDirectories(path, new FileAttribute[0]);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path.resolve(string), StandardCharsets.UTF_8, new OpenOption[0]);){
                PrintWriter printWriter = new PrintWriter(bufferedWriter);
                for (CommandFunction commandFunction : collection) {
                    printWriter.println(commandFunction.getId());
                    Tracer tracer = new Tracer(printWriter);
                    n += commandSourceStack.getServer().getFunctions().execute(commandFunction, commandSourceStack.withSource(tracer).withMaximumPermission(2), tracer);
                }
            }
        }
        catch (IOException | UncheckedIOException exception) {
            LOGGER.warn("Tracing failed", (Throwable)exception);
            commandSourceStack.sendFailure(new TranslatableComponent("commands.debug.function.traceFailed"));
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.function.success.single", n, collection.iterator().next().getId(), string), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.function.success.multiple", n, collection.size(), string), true);
        }
        return n;
    }

    static class Tracer
    implements CommandSource,
    ServerFunctionManager.TraceCallbacks {
        public static final int INDENT_OFFSET = 1;
        private final PrintWriter output;
        private int lastIndent;
        private boolean waitingForResult;

        Tracer(PrintWriter printWriter) {
            this.output = printWriter;
        }

        private void indentAndSave(int n) {
            this.printIndent(n);
            this.lastIndent = n;
        }

        private void printIndent(int n) {
            for (int i = 0; i < n + 1; ++i) {
                this.output.write("    ");
            }
        }

        private void newLine() {
            if (this.waitingForResult) {
                this.output.println();
                this.waitingForResult = false;
            }
        }

        @Override
        public void onCommand(int n, String string) {
            this.newLine();
            this.indentAndSave(n);
            this.output.print("[C] ");
            this.output.print(string);
            this.waitingForResult = true;
        }

        @Override
        public void onReturn(int n, String string, int n2) {
            if (this.waitingForResult) {
                this.output.print(" -> ");
                this.output.println(n2);
                this.waitingForResult = false;
            } else {
                this.indentAndSave(n);
                this.output.print("[R = ");
                this.output.print(n2);
                this.output.print("] ");
                this.output.println(string);
            }
        }

        @Override
        public void onCall(int n, ResourceLocation resourceLocation, int n2) {
            this.newLine();
            this.indentAndSave(n);
            this.output.print("[F] ");
            this.output.print(resourceLocation);
            this.output.print(" size=");
            this.output.println(n2);
        }

        @Override
        public void onError(int n, String string) {
            this.newLine();
            this.indentAndSave(n + 1);
            this.output.print("[E] ");
            this.output.print(string);
        }

        @Override
        public void sendMessage(Component component, UUID uUID) {
            this.newLine();
            this.printIndent(this.lastIndent + 1);
            this.output.print("[M] ");
            if (uUID != Util.NIL_UUID) {
                this.output.print(uUID);
                this.output.print(": ");
            }
            this.output.println(component.getString());
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return false;
        }

        @Override
        public boolean alwaysAccepts() {
            return true;
        }
    }
}

