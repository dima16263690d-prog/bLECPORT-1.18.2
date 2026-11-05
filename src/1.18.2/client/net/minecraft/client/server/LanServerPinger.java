/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.server;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandler;
import org.slf4j.Logger;

public class LanServerPinger
extends Thread {
    private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MULTICAST_GROUP = "224.0.2.60";
    public static final int PING_PORT = 4445;
    private static final long PING_INTERVAL = 1500L;
    private final String motd;
    private final DatagramSocket socket;
    private boolean isRunning = true;
    private final String serverAddress;

    public LanServerPinger(String string, String string2) throws IOException {
        super("LanServerPinger #" + UNIQUE_THREAD_ID.incrementAndGet());
        this.motd = string;
        this.serverAddress = string2;
        this.setDaemon(true);
        this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        String string = LanServerPinger.createPingString(this.motd, this.serverAddress);
        byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
        while (!this.isInterrupted() && this.isRunning) {
            try {
                InetAddress inetAddress = InetAddress.getByName(MULTICAST_GROUP);
                DatagramPacket datagramPacket = new DatagramPacket(byArray, byArray.length, inetAddress, 4445);
                this.socket.send(datagramPacket);
            }
            catch (IOException iOException) {
                LOGGER.warn("LanServerPinger: {}", (Object)iOException.getMessage());
                break;
            }
            try {
                LanServerPinger.sleep(1500L);
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        this.isRunning = false;
    }

    public static String createPingString(String string, String string2) {
        return "[MOTD]" + string + "[/MOTD][AD]" + string2 + "[/AD]";
    }

    public static String parseMotd(String string) {
        int n = string.indexOf("[MOTD]");
        if (n < 0) {
            return "missing no";
        }
        int n2 = string.indexOf("[/MOTD]", n + "[MOTD]".length());
        if (n2 < n) {
            return "missing no";
        }
        return string.substring(n + "[MOTD]".length(), n2);
    }

    public static String parseAddress(String string) {
        int n = string.indexOf("[/MOTD]");
        if (n < 0) {
            return null;
        }
        int n2 = string.indexOf("[/MOTD]", n + "[/MOTD]".length());
        if (n2 >= 0) {
            return null;
        }
        int n3 = string.indexOf("[AD]", n + "[/MOTD]".length());
        if (n3 < 0) {
            return null;
        }
        int n4 = string.indexOf("[/AD]", n3 + "[AD]".length());
        if (n4 < n3) {
            return null;
        }
        return string.substring(n3 + "[AD]".length(), n4);
    }
}

