/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  org.apache.commons.io.FileUtils
 */
package com.mojang.realmsclient.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;

public class RealmsPersistence {
    private static final String FILE_NAME = "realms_persistence.json";
    private static final GuardedSerializer GSON = new GuardedSerializer();

    public RealmsPersistenceData read() {
        return RealmsPersistence.readFile();
    }

    public void save(RealmsPersistenceData realmsPersistenceData) {
        RealmsPersistence.writeFile(realmsPersistenceData);
    }

    public static RealmsPersistenceData readFile() {
        File file = RealmsPersistence.getPathToData();
        try {
            String string = FileUtils.readFileToString((File)file, (Charset)StandardCharsets.UTF_8);
            RealmsPersistenceData realmsPersistenceData = GSON.fromJson(string, RealmsPersistenceData.class);
            return realmsPersistenceData != null ? realmsPersistenceData : new RealmsPersistenceData();
        }
        catch (IOException iOException) {
            return new RealmsPersistenceData();
        }
    }

    public static void writeFile(RealmsPersistenceData realmsPersistenceData) {
        File file = RealmsPersistence.getPathToData();
        try {
            FileUtils.writeStringToFile((File)file, (String)GSON.toJson(realmsPersistenceData), (Charset)StandardCharsets.UTF_8);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static File getPathToData() {
        return new File(Minecraft.getInstance().gameDirectory, FILE_NAME);
    }

    public static class RealmsPersistenceData
    implements ReflectionBasedSerialization {
        @SerializedName(value="newsLink")
        public String newsLink;
        @SerializedName(value="hasUnreadNews")
        public boolean hasUnreadNews;
    }
}

