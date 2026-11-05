/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.gui.task;

public interface RestartDelayCalculator {
    public void markExecutionStart();

    public long getNextDelayMs();
}

