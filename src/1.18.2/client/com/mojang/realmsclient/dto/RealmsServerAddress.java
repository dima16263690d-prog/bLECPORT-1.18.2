/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import org.slf4j.Logger;

public class RealmsServerAddress
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String address;
    public String resourcePackUrl;
    public String resourcePackHash;

    public static RealmsServerAddress parse(String string) {
        JsonParser jsonParser = new JsonParser();
        RealmsServerAddress realmsServerAddress = new RealmsServerAddress();
        try {
            JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
            realmsServerAddress.address = JsonUtils.getStringOr("address", jsonObject, null);
            realmsServerAddress.resourcePackUrl = JsonUtils.getStringOr("resourcePackUrl", jsonObject, null);
            realmsServerAddress.resourcePackHash = JsonUtils.getStringOr("resourcePackHash", jsonObject, null);
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse RealmsServerAddress: {}", (Object)exception.getMessage());
        }
        return realmsServerAddress;
    }
}

