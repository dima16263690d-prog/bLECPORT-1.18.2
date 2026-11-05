/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.server;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerPinger;
import org.slf4j.Logger;

public class LanServerDetection {
    static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();

    public static class LanServerDetector
    extends Thread {
        private final LanServerList serverList;
        private final InetAddress pingGroup;
        private final MulticastSocket socket;

        public LanServerDetector(LanServerList lanServerList) throws IOException {
            super("LanServerDetector #" + UNIQUE_THREAD_ID.incrementAndGet());
            this.serverList = lanServerList;
            this.setDaemon(true);
            this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            this.socket = new MulticastSocket(4445);
            this.pingGroup = InetAddress.getByName("224.0.2.60");
            this.socket.setSoTimeout(5000);
            this.socket.joinGroup(this.pingGroup);
        }

        @Override
        public void run() {
            byte[] byArray = new byte[1024];
            while (!this.isInterrupted()) {
                DatagramPacket datagramPacket = new DatagramPacket(byArray, byArray.length);
                try {
                    this.socket.receive(datagramPacket);
                }
                catch (SocketTimeoutException socketTimeoutException) {
                    continue;
                }
                catch (IOException iOException) {
                    LOGGER.error("Couldn't ping server", (Throwable)iOException);
                    break;
                }
                String string = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength(), StandardCharsets.UTF_8);
                LOGGER.debug("{}: {}", (Object)datagramPacket.getAddress(), (Object)string);
                this.serverList.addServer(string, datagramPacket.getAddress());
            }
            try {
                this.socket.leaveGroup(this.pingGroup);
            }
            catch (IOException iOException) {
                // empty catch block
            }
            this.socket.close();
        }
    }

    public static class LanServerList {
        private final List<LanServer> servers = Lists.newArrayList();
        private boolean isDirty;

        public synchronized boolean isDirty() {
            return this.isDirty;
        }

        public synchronized void markClean() {
            this.isDirty = false;
        }

        public synchronized List<LanServer> getServers() {
            return Collections.unmodifiableList(this.servers);
        }

        public synchronized void addServer(String string, InetAddress inetAddress) {
            String string2 = LanServerPinger.parseMotd(string);
            Object object = LanServerPinger.parseAddress(string);
            if (object == null) {
                return;
            }
            object = inetAddress.getHostAddress() + ":" + (String)object;
            boolean bl = false;
            for (LanServer lanServer : this.servers) {
                if (!lanServer.getAddress().equals(object)) continue;
                lanServer.updatePingTime();
                bl = true;
                break;
            }
            if (!bl) {
                this.servers.add(new LanServer(string2, (String)object));
                this.isDirty = true;
            }
        }
    }
}

