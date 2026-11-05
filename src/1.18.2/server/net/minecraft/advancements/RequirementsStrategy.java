/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements;

import java.util.Collection;

public interface RequirementsStrategy {
    public static final RequirementsStrategy AND = collection -> {
        String[][] stringArray = new String[collection.size()][];
        int n = 0;
        for (String string : collection) {
            stringArray[n++] = new String[]{string};
        }
        return stringArray;
    };
    public static final RequirementsStrategy OR = collection -> new String[][]{collection.toArray(new String[0])};

    public String[][] createRequirements(Collection<String> var1);
}

