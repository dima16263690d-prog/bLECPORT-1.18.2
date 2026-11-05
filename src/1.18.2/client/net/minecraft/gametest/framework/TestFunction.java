/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;

public class TestFunction {
    private final String batchName;
    private final String testName;
    private final String structureName;
    private final boolean required;
    private final int maxAttempts;
    private final int requiredSuccesses;
    private final Consumer<GameTestHelper> function;
    private final int maxTicks;
    private final long setupTicks;
    private final Rotation rotation;

    public TestFunction(String string, String string2, String string3, int n, long l, boolean bl, Consumer<GameTestHelper> consumer) {
        this(string, string2, string3, Rotation.NONE, n, l, bl, 1, 1, consumer);
    }

    public TestFunction(String string, String string2, String string3, Rotation rotation, int n, long l, boolean bl, Consumer<GameTestHelper> consumer) {
        this(string, string2, string3, rotation, n, l, bl, 1, 1, consumer);
    }

    public TestFunction(String string, String string2, String string3, Rotation rotation, int n, long l, boolean bl, int n2, int n3, Consumer<GameTestHelper> consumer) {
        this.batchName = string;
        this.testName = string2;
        this.structureName = string3;
        this.rotation = rotation;
        this.maxTicks = n;
        this.required = bl;
        this.requiredSuccesses = n2;
        this.maxAttempts = n3;
        this.function = consumer;
        this.setupTicks = l;
    }

    public void run(GameTestHelper gameTestHelper) {
        this.function.accept(gameTestHelper);
    }

    public String getTestName() {
        return this.testName;
    }

    public String getStructureName() {
        return this.structureName;
    }

    public String toString() {
        return this.testName;
    }

    public int getMaxTicks() {
        return this.maxTicks;
    }

    public boolean isRequired() {
        return this.required;
    }

    public String getBatchName() {
        return this.batchName;
    }

    public long getSetupTicks() {
        return this.setupTicks;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public boolean isFlaky() {
        return this.maxAttempts > 1;
    }

    public int getMaxAttempts() {
        return this.maxAttempts;
    }

    public int getRequiredSuccesses() {
        return this.requiredSuccesses;
    }
}

