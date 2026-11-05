/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.gui.task;

import com.mojang.realmsclient.gui.task.RestartDelayCalculator;

public class NoStartupDelay
implements RestartDelayCalculator {
    @Override
    public void markExecutionStart() {
    }

    @Override
    public long getNextDelayMs() {
        return 0L;
    }
}

