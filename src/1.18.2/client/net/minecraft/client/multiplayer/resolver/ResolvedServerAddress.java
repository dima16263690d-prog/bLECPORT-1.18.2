/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.multiplayer.resolver;

import java.net.InetSocketAddress;

public interface ResolvedServerAddress {
    public String getHostName();

    public String getHostIp();

    public int getPort();

    public InetSocketAddress asInetSocketAddress();

    public static ResolvedServerAddress from(final InetSocketAddress inetSocketAddress) {
        return new ResolvedServerAddress(){

            @Override
            public String getHostName() {
                return inetSocketAddress.getAddress().getHostName();
            }

            @Override
            public String getHostIp() {
                return inetSocketAddress.getAddress().getHostAddress();
            }

            @Override
            public int getPort() {
                return inetSocketAddress.getPort();
            }

            @Override
            public InetSocketAddress asInetSocketAddress() {
                return inetSocketAddress;
            }
        };
    }
}

