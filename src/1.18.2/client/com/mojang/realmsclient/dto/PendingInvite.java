/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Date;
import org.slf4j.Logger;

public class PendingInvite
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String invitationId;
    public String worldName;
    public String worldOwnerName;
    public String worldOwnerUuid;
    public Date date;

    public static PendingInvite parse(JsonObject jsonObject) {
        PendingInvite pendingInvite = new PendingInvite();
        try {
            pendingInvite.invitationId = JsonUtils.getStringOr("invitationId", jsonObject, "");
            pendingInvite.worldName = JsonUtils.getStringOr("worldName", jsonObject, "");
            pendingInvite.worldOwnerName = JsonUtils.getStringOr("worldOwnerName", jsonObject, "");
            pendingInvite.worldOwnerUuid = JsonUtils.getStringOr("worldOwnerUuid", jsonObject, "");
            pendingInvite.date = JsonUtils.getDateOr("date", jsonObject);
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse PendingInvite: {}", (Object)exception.getMessage());
        }
        return pendingInvite;
    }
}

