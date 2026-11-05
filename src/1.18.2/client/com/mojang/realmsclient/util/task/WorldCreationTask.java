/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.task.LongRunningTask;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import org.slf4j.Logger;

public class WorldCreationTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final String motd;
    private final long worldId;
    private final Screen lastScreen;

    public WorldCreationTask(long l, String string, String string2, Screen screen) {
        this.worldId = l;
        this.name = string;
        this.motd = string2;
        this.lastScreen = screen;
    }

    @Override
    public void run() {
        this.setTitle(new TranslatableComponent("mco.create.world.wait"));
        RealmsClient realmsClient = RealmsClient.create();
        try {
            realmsClient.initializeWorld(this.worldId, this.name, this.motd);
            WorldCreationTask.setScreen(this.lastScreen);
        }
        catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't create world");
            this.error(realmsServiceException.toString());
        }
        catch (Exception exception) {
            LOGGER.error("Could not create world");
            this.error(exception.getLocalizedMessage());
        }
    }
}

