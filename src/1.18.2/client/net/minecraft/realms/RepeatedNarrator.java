/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.util.concurrent.RateLimiter
 */
package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;

public class RepeatedNarrator {
    private final float permitsPerSecond;
    private final AtomicReference<Params> params = new AtomicReference();

    public RepeatedNarrator(Duration duration) {
        this.permitsPerSecond = 1000.0f / (float)duration.toMillis();
    }

    public void narrate(Component component) {
        Params params2 = this.params.updateAndGet(params -> {
            if (params == null || !component.equals(params.narration)) {
                return new Params(component, RateLimiter.create((double)this.permitsPerSecond));
            }
            return params;
        });
        if (params2.rateLimiter.tryAcquire(1)) {
            NarratorChatListener.INSTANCE.sayNow(component);
        }
    }

    static class Params {
        final Component narration;
        final RateLimiter rateLimiter;

        Params(Component component, RateLimiter rateLimiter) {
            this.narration = component;
            this.rateLimiter = rateLimiter;
        }
    }
}

