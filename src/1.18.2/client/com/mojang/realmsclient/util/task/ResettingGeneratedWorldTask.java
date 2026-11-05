/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import com.mojang.realmsclient.util.task.ResettingWorldTask;
import net.minecraft.network.chat.Component;

public class ResettingGeneratedWorldTask
extends ResettingWorldTask {
    private final WorldGenerationInfo generationInfo;

    public ResettingGeneratedWorldTask(WorldGenerationInfo worldGenerationInfo, long l, Component component, Runnable runnable) {
        super(l, component, runnable);
        this.generationInfo = worldGenerationInfo;
    }

    @Override
    protected void sendResetRequest(RealmsClient realmsClient, long l) throws RealmsServiceException {
        realmsClient.resetWorldWithSeed(l, this.generationInfo);
    }
}

