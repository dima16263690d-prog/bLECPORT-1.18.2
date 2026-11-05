/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.gui.task;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.realmsclient.gui.task.RestartDelayCalculator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class IntervalBasedStartupDelay
implements RestartDelayCalculator {
    private final Duration interval;
    private final Supplier<Clock> clock;
    @Nullable
    private Instant lastStartedTimestamp;

    public IntervalBasedStartupDelay(Duration duration) {
        this.interval = duration;
        this.clock = Clock::systemUTC;
    }

    @VisibleForTesting
    protected IntervalBasedStartupDelay(Duration duration, Supplier<Clock> supplier) {
        this.interval = duration;
        this.clock = supplier;
    }

    @Override
    public void markExecutionStart() {
        this.lastStartedTimestamp = Instant.now(this.clock.get());
    }

    @Override
    public long getNextDelayMs() {
        if (this.lastStartedTimestamp == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(Instant.now(this.clock.get()), this.lastStartedTimestamp.plus(this.interval)).toMillis());
    }
}

