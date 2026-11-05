/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

public class ModelResourceLocation
extends ResourceLocation {
    @VisibleForTesting
    static final char VARIANT_SEPARATOR = '#';
    private final String variant;

    protected ModelResourceLocation(String[] stringArray) {
        super(stringArray);
        this.variant = stringArray[2].toLowerCase(Locale.ROOT);
    }

    public ModelResourceLocation(String string, String string2, String string3) {
        this(new String[]{string, string2, string3});
    }

    public ModelResourceLocation(String string) {
        this(ModelResourceLocation.decompose(string));
    }

    public ModelResourceLocation(ResourceLocation resourceLocation, String string) {
        this(resourceLocation.toString(), string);
    }

    public ModelResourceLocation(String string, String string2) {
        this(ModelResourceLocation.decompose(string + "#" + string2));
    }

    protected static String[] decompose(String string) {
        String[] stringArray = new String[]{null, string, ""};
        int n = string.indexOf(35);
        String string2 = string;
        if (n >= 0) {
            stringArray[2] = string.substring(n + 1, string.length());
            if (n > 1) {
                string2 = string.substring(0, n);
            }
        }
        System.arraycopy(ResourceLocation.decompose(string2, ':'), 0, stringArray, 0, 2);
        return stringArray;
    }

    public String getVariant() {
        return this.variant;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ModelResourceLocation && super.equals(object)) {
            ModelResourceLocation modelResourceLocation = (ModelResourceLocation)object;
            return this.variant.equals(modelResourceLocation.variant);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + this.variant.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + "#" + this.variant;
    }
}

