/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ca.weblite.objc.NSObject
 *  com.sun.jna.Pointer
 *  org.lwjgl.glfw.GLFWNativeCocoa
 */
package com.mojang.blaze3d.platform;

import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import java.util.Optional;
import org.lwjgl.glfw.GLFWNativeCocoa;

public class MacosUtil {
    private static final int NS_FULL_SCREEN_WINDOW_MASK = 16384;

    public static void toggleFullscreen(long l) {
        MacosUtil.getNsWindow(l).filter(MacosUtil::isInKioskMode).ifPresent(MacosUtil::toggleFullscreen);
    }

    private static Optional<NSObject> getNsWindow(long l) {
        long l2 = GLFWNativeCocoa.glfwGetCocoaWindow((long)l);
        if (l2 != 0L) {
            return Optional.of(new NSObject(new Pointer(l2)));
        }
        return Optional.empty();
    }

    private static boolean isInKioskMode(NSObject nSObject) {
        return ((Long)nSObject.sendRaw("styleMask", new Object[0]) & 0x4000L) == 16384L;
    }

    private static void toggleFullscreen(NSObject nSObject) {
        nSObject.send("toggleFullScreen:", new Object[0]);
    }
}

