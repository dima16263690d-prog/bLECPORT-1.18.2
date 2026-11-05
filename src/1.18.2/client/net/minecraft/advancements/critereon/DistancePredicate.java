/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class DistancePredicate {
    public static final DistancePredicate ANY = new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    private final MinMaxBounds.Doubles x;
    private final MinMaxBounds.Doubles y;
    private final MinMaxBounds.Doubles z;
    private final MinMaxBounds.Doubles horizontal;
    private final MinMaxBounds.Doubles absolute;

    public DistancePredicate(MinMaxBounds.Doubles doubles, MinMaxBounds.Doubles doubles2, MinMaxBounds.Doubles doubles3, MinMaxBounds.Doubles doubles4, MinMaxBounds.Doubles doubles5) {
        this.x = doubles;
        this.y = doubles2;
        this.z = doubles3;
        this.horizontal = doubles4;
        this.absolute = doubles5;
    }

    public static DistancePredicate horizontal(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate vertical(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, doubles, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate absolute(MinMaxBounds.Doubles doubles) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, doubles);
    }

    public boolean matches(double d, double d2, double d3, double d4, double d5, double d6) {
        float f = (float)(d - d4);
        float f2 = (float)(d2 - d5);
        float f3 = (float)(d3 - d6);
        if (!(this.x.matches(Mth.abs(f)) && this.y.matches(Mth.abs(f2)) && this.z.matches(Mth.abs(f3)))) {
            return false;
        }
        if (!this.horizontal.matchesSqr(f * f + f3 * f3)) {
            return false;
        }
        return this.absolute.matchesSqr(f * f + f2 * f2 + f3 * f3);
    }

    public static DistancePredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "distance");
        MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromJson(jsonObject.get("x"));
        MinMaxBounds.Doubles doubles2 = MinMaxBounds.Doubles.fromJson(jsonObject.get("y"));
        MinMaxBounds.Doubles doubles3 = MinMaxBounds.Doubles.fromJson(jsonObject.get("z"));
        MinMaxBounds.Doubles doubles4 = MinMaxBounds.Doubles.fromJson(jsonObject.get("horizontal"));
        MinMaxBounds.Doubles doubles5 = MinMaxBounds.Doubles.fromJson(jsonObject.get("absolute"));
        return new DistancePredicate(doubles, doubles2, doubles3, doubles4, doubles5);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("x", this.x.serializeToJson());
        jsonObject.add("y", this.y.serializeToJson());
        jsonObject.add("z", this.z.serializeToJson());
        jsonObject.add("horizontal", this.horizontal.serializeToJson());
        jsonObject.add("absolute", this.absolute.serializeToJson());
        return jsonObject;
    }
}

