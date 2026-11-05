/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;

public class RealmsServiceException
extends Exception {
    public final int httpResultCode;
    public final String rawResponse;
    @Nullable
    public final RealmsError realmsError;

    public RealmsServiceException(int n, String string, RealmsError realmsError) {
        super(string);
        this.httpResultCode = n;
        this.rawResponse = string;
        this.realmsError = realmsError;
    }

    public RealmsServiceException(int n, String string) {
        super(string);
        this.httpResultCode = n;
        this.rawResponse = string;
        this.realmsError = null;
    }

    @Override
    public String toString() {
        if (this.realmsError != null) {
            String string = "mco.errorMessage." + this.realmsError.getErrorCode();
            String string2 = I18n.exists(string) ? I18n.get(string, new Object[0]) : this.realmsError.getErrorMessage();
            return "Realms service error (%d/%d) %s".formatted(new Object[]{this.httpResultCode, this.realmsError.getErrorCode(), string2});
        }
        return "Realms service error (%d) %s".formatted(new Object[]{this.httpResultCode, this.rawResponse});
    }

    public int realmsErrorCodeOrDefault(int n) {
        return this.realmsError != null ? this.realmsError.getErrorCode() : n;
    }
}

