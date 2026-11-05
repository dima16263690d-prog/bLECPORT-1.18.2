/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.Gson;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import javax.annotation.Nullable;

public class GuardedSerializer {
    private final Gson gson = new Gson();

    public String toJson(ReflectionBasedSerialization reflectionBasedSerialization) {
        return this.gson.toJson((Object)reflectionBasedSerialization);
    }

    @Nullable
    public <T extends ReflectionBasedSerialization> T fromJson(String string, Class<T> clazz) {
        return (T)((ReflectionBasedSerialization)this.gson.fromJson(string, clazz));
    }
}

