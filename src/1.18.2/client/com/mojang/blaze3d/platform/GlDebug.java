/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.EvictingQueue
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.lwjgl.opengl.ARBDebugOutput
 *  org.lwjgl.opengl.GL
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GLCapabilities
 *  org.lwjgl.opengl.GLDebugMessageARBCallback
 *  org.lwjgl.opengl.GLDebugMessageARBCallbackI
 *  org.lwjgl.opengl.GLDebugMessageCallback
 *  org.lwjgl.opengl.GLDebugMessageCallbackI
 *  org.lwjgl.opengl.KHRDebug
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.DebugMemoryUntracker;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageARBCallbackI;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;

public class GlDebug {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CIRCULAR_LOG_SIZE = 10;
    private static final Queue<LogEntry> MESSAGE_BUFFER = EvictingQueue.create((int)10);
    @Nullable
    private static volatile LogEntry lastEntry;
    private static final List<Integer> DEBUG_LEVELS;
    private static final List<Integer> DEBUG_LEVELS_ARB;
    private static boolean debugEnabled;

    private static String printUnknownToken(int n) {
        return "Unknown (0x" + Integer.toHexString(n).toUpperCase() + ")";
    }

    public static String sourceToString(int n) {
        switch (n) {
            case 33350: {
                return "API";
            }
            case 33351: {
                return "WINDOW SYSTEM";
            }
            case 33352: {
                return "SHADER COMPILER";
            }
            case 33353: {
                return "THIRD PARTY";
            }
            case 33354: {
                return "APPLICATION";
            }
            case 33355: {
                return "OTHER";
            }
        }
        return GlDebug.printUnknownToken(n);
    }

    public static String typeToString(int n) {
        switch (n) {
            case 33356: {
                return "ERROR";
            }
            case 33357: {
                return "DEPRECATED BEHAVIOR";
            }
            case 33358: {
                return "UNDEFINED BEHAVIOR";
            }
            case 33359: {
                return "PORTABILITY";
            }
            case 33360: {
                return "PERFORMANCE";
            }
            case 33361: {
                return "OTHER";
            }
            case 33384: {
                return "MARKER";
            }
        }
        return GlDebug.printUnknownToken(n);
    }

    public static String severityToString(int n) {
        switch (n) {
            case 37190: {
                return "HIGH";
            }
            case 37191: {
                return "MEDIUM";
            }
            case 37192: {
                return "LOW";
            }
            case 33387: {
                return "NOTIFICATION";
            }
        }
        return GlDebug.printUnknownToken(n);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void printDebugLog(int n, int n2, int n3, int n4, int n5, long l, long l2) {
        LogEntry logEntry;
        String string = GLDebugMessageCallback.getMessage((int)n5, (long)l);
        Queue<LogEntry> queue = MESSAGE_BUFFER;
        synchronized (queue) {
            logEntry = lastEntry;
            if (logEntry == null || !logEntry.isSame(n, n2, n3, n4, string)) {
                logEntry = new LogEntry(n, n2, n3, n4, string);
                MESSAGE_BUFFER.add(logEntry);
                lastEntry = logEntry;
            } else {
                ++logEntry.count;
            }
        }
        LOGGER.info("OpenGL debug message: {}", (Object)logEntry);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<String> getLastOpenGlDebugMessages() {
        Queue<LogEntry> queue = MESSAGE_BUFFER;
        synchronized (queue) {
            ArrayList arrayList = Lists.newArrayListWithCapacity((int)MESSAGE_BUFFER.size());
            for (LogEntry logEntry : MESSAGE_BUFFER) {
                arrayList.add(logEntry + " x " + logEntry.count);
            }
            return arrayList;
        }
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void enableDebugCallback(int n, boolean bl) {
        RenderSystem.assertInInitPhase();
        if (n <= 0) {
            return;
        }
        GLCapabilities gLCapabilities = GL.getCapabilities();
        if (gLCapabilities.GL_KHR_debug) {
            debugEnabled = true;
            GL11.glEnable((int)37600);
            if (bl) {
                GL11.glEnable((int)33346);
            }
            for (int i = 0; i < DEBUG_LEVELS.size(); ++i) {
                boolean bl2 = i < n;
                KHRDebug.glDebugMessageControl((int)4352, (int)4352, (int)DEBUG_LEVELS.get(i), (int[])null, (boolean)bl2);
            }
            KHRDebug.glDebugMessageCallback((GLDebugMessageCallbackI)((GLDebugMessageCallbackI)GLX.make(GLDebugMessageCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack)), (long)0L);
        } else if (gLCapabilities.GL_ARB_debug_output) {
            debugEnabled = true;
            if (bl) {
                GL11.glEnable((int)33346);
            }
            for (int i = 0; i < DEBUG_LEVELS_ARB.size(); ++i) {
                boolean bl3 = i < n;
                ARBDebugOutput.glDebugMessageControlARB((int)4352, (int)4352, (int)DEBUG_LEVELS_ARB.get(i), (int[])null, (boolean)bl3);
            }
            ARBDebugOutput.glDebugMessageCallbackARB((GLDebugMessageARBCallbackI)((GLDebugMessageARBCallbackI)GLX.make(GLDebugMessageARBCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack)), (long)0L);
        }
    }

    static {
        DEBUG_LEVELS = ImmutableList.of((Object)37190, (Object)37191, (Object)37192, (Object)33387);
        DEBUG_LEVELS_ARB = ImmutableList.of((Object)37190, (Object)37191, (Object)37192);
    }

    static class LogEntry {
        private final int id;
        private final int source;
        private final int type;
        private final int severity;
        private final String message;
        int count = 1;

        LogEntry(int n, int n2, int n3, int n4, String string) {
            this.id = n3;
            this.source = n;
            this.type = n2;
            this.severity = n4;
            this.message = string;
        }

        boolean isSame(int n, int n2, int n3, int n4, String string) {
            return n2 == this.type && n == this.source && n3 == this.id && n4 == this.severity && string.equals(this.message);
        }

        public String toString() {
            return "id=" + this.id + ", source=" + GlDebug.sourceToString(this.source) + ", type=" + GlDebug.typeToString(this.type) + ", severity=" + GlDebug.severityToString(this.severity) + ", message='" + this.message + "'";
        }
    }
}

