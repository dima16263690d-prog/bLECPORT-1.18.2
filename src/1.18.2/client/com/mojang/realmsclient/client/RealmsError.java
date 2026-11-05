/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class RealmsError {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String errorMessage;
    private final int errorCode;

    private RealmsError(String string, int n) {
        this.errorMessage = string;
        this.errorCode = n;
    }

    @Nullable
    public static RealmsError parse(String string) {
        if (Strings.isNullOrEmpty((String)string)) {
            return null;
        }
        try {
            JsonObject jsonObject = JsonParser.parseString((String)string).getAsJsonObject();
            String string2 = JsonUtils.getStringOr("errorMsg", jsonObject, "");
            int n = JsonUtils.getIntOr("errorCode", jsonObject, -1);
            return new RealmsError(string2, n);
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse RealmsError: {}", (Object)exception.getMessage());
            LOGGER.error("The error was: {}", (Object)string);
            return null;
        }
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}

