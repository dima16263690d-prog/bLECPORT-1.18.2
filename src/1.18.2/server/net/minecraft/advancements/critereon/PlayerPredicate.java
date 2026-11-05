/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonPrimitive
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap
 *  it.unimi.dsi.fastutil.objects.Object2BooleanMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PlayerPredicate {
    public static final PlayerPredicate ANY = new Builder().build();
    public static final int LOOKING_AT_RANGE = 100;
    private final MinMaxBounds.Ints level;
    @Nullable
    private final GameType gameType;
    private final Map<Stat<?>, MinMaxBounds.Ints> stats;
    private final Object2BooleanMap<ResourceLocation> recipes;
    private final Map<ResourceLocation, AdvancementPredicate> advancements;
    private final EntityPredicate lookingAt;

    private static AdvancementPredicate advancementPredicateFromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            boolean bl = jsonElement.getAsBoolean();
            return new AdvancementDonePredicate(bl);
        }
        Object2BooleanOpenHashMap object2BooleanOpenHashMap = new Object2BooleanOpenHashMap();
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "criterion data");
        jsonObject.entrySet().forEach(arg_0 -> PlayerPredicate.lambda$advancementPredicateFromJson$0((Object2BooleanMap)object2BooleanOpenHashMap, arg_0));
        return new AdvancementCriterionsPredicate((Object2BooleanMap<String>)object2BooleanOpenHashMap);
    }

    PlayerPredicate(MinMaxBounds.Ints ints, @Nullable GameType gameType, Map<Stat<?>, MinMaxBounds.Ints> map, Object2BooleanMap<ResourceLocation> object2BooleanMap, Map<ResourceLocation, AdvancementPredicate> map2, EntityPredicate entityPredicate) {
        this.level = ints;
        this.gameType = gameType;
        this.stats = map;
        this.recipes = object2BooleanMap;
        this.advancements = map2;
        this.lookingAt = entityPredicate;
    }

    public boolean matches(Entity entity2) {
        Object object;
        if (this == ANY) {
            return true;
        }
        if (!(entity2 instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity2;
        if (!this.level.matches(serverPlayer.experienceLevel)) {
            return false;
        }
        if (this.gameType != null && this.gameType != serverPlayer.gameMode.getGameModeForPlayer()) {
            return false;
        }
        ServerStatsCounter serverStatsCounter = serverPlayer.getStats();
        for (Map.Entry<Stat<?>, MinMaxBounds.Ints> object22 : this.stats.entrySet()) {
            int n = serverStatsCounter.getValue(object22.getKey());
            if (object22.getValue().matches(n)) continue;
            return false;
        }
        ServerRecipeBook serverRecipeBook = serverPlayer.getRecipeBook();
        for (Object2BooleanMap.Entry entry : this.recipes.object2BooleanEntrySet()) {
            if (serverRecipeBook.contains((ResourceLocation)entry.getKey()) == entry.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
            ServerAdvancementManager serverAdvancementManager = serverPlayer.getServer().getAdvancements();
            for (Map.Entry<ResourceLocation, AdvancementPredicate> entry : this.advancements.entrySet()) {
                object = serverAdvancementManager.getAdvancement(entry.getKey());
                if (object != null && entry.getValue().test(playerAdvancements.getOrStartProgress((Advancement)object))) continue;
                return false;
            }
        }
        if (this.lookingAt != EntityPredicate.ANY) {
            Vec3 vec3 = serverPlayer.getEyePosition();
            Vec3 vec32 = serverPlayer.getViewVector(1.0f);
            Vec3 vec33 = vec3.add(vec32.x * 100.0, vec32.y * 100.0, vec32.z * 100.0);
            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(serverPlayer.level, serverPlayer, vec3, vec33, new AABB(vec3, vec33).inflate(1.0), entity -> !entity.isSpectator(), 0.0f);
            if (entityHitResult == null || entityHitResult.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            object = entityHitResult.getEntity();
            if (!this.lookingAt.matches(serverPlayer, (Entity)object) || !serverPlayer.hasLineOfSight((Entity)object)) {
                return false;
            }
        }
        return true;
    }

    public static PlayerPredicate fromJson(@Nullable JsonElement jsonElement) {
        Object object;
        Object object2;
        StatType<?> statType;
        Object object32;
        Object object4;
        JsonElement jsonElement22;
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "player");
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
        String string = GsonHelper.getAsString(jsonObject, "gamemode", "");
        GameType gameType = GameType.byName(string, null);
        HashMap hashMap = Maps.newHashMap();
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "stats", null);
        if (jsonArray != null) {
            for (JsonElement jsonElement22 : jsonArray) {
                object4 = GsonHelper.convertToJsonObject(jsonElement22, "stats entry");
                object32 = new ResourceLocation(GsonHelper.getAsString((JsonObject)object4, "type"));
                statType = Registry.STAT_TYPE.get((ResourceLocation)object32);
                if (statType == null) {
                    throw new JsonParseException("Invalid stat type: " + (ResourceLocation)object32);
                }
                ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString((JsonObject)object4, "stat"));
                object2 = PlayerPredicate.getStat(statType, resourceLocation);
                object = MinMaxBounds.Ints.fromJson(object4.get("value"));
                hashMap.put(object2, object);
            }
        }
        Iterator iterator = new Object2BooleanOpenHashMap();
        jsonElement22 = GsonHelper.getAsJsonObject(jsonObject, "recipes", new JsonObject());
        for (Object object32 : jsonElement22.entrySet()) {
            statType = new ResourceLocation((String)object32.getKey());
            boolean bl = GsonHelper.convertToBoolean((JsonElement)object32.getValue(), "recipe present");
            iterator.put(statType, bl);
        }
        object4 = Maps.newHashMap();
        object32 = GsonHelper.getAsJsonObject(jsonObject, "advancements", new JsonObject());
        for (Map.Entry entry : object32.entrySet()) {
            object2 = new ResourceLocation((String)entry.getKey());
            object = PlayerPredicate.advancementPredicateFromJson((JsonElement)entry.getValue());
            object4.put(object2, object);
        }
        statType = EntityPredicate.fromJson(jsonObject.get("looking_at"));
        return new PlayerPredicate(ints, gameType, hashMap, (Object2BooleanMap<ResourceLocation>)iterator, (Map<ResourceLocation, AdvancementPredicate>)object4, (EntityPredicate)((Object)statType));
    }

    private static <T> Stat<T> getStat(StatType<T> statType, ResourceLocation resourceLocation) {
        Registry<T> registry = statType.getRegistry();
        T t = registry.get(resourceLocation);
        if (t == null) {
            throw new JsonParseException("Unknown object " + resourceLocation + " for stat type " + Registry.STAT_TYPE.getKey(statType));
        }
        return statType.get(t);
    }

    private static <T> ResourceLocation getStatValueId(Stat<T> stat) {
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    public JsonElement serializeToJson() {
        JsonArray jsonArray;
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("level", this.level.serializeToJson());
        if (this.gameType != null) {
            jsonObject.addProperty("gamemode", this.gameType.getName());
        }
        if (!this.stats.isEmpty()) {
            jsonArray = new JsonArray();
            this.stats.forEach((stat, ints) -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", Registry.STAT_TYPE.getKey(stat.getType()).toString());
                jsonObject.addProperty("stat", PlayerPredicate.getStatValueId(stat).toString());
                jsonObject.add("value", ints.serializeToJson());
                jsonArray.add((JsonElement)jsonObject);
            });
            jsonObject.add("stats", (JsonElement)jsonArray);
        }
        if (!this.recipes.isEmpty()) {
            jsonArray = new JsonObject();
            this.recipes.forEach((arg_0, arg_1) -> PlayerPredicate.lambda$serializeToJson$3((JsonObject)jsonArray, arg_0, arg_1));
            jsonObject.add("recipes", (JsonElement)jsonArray);
        }
        if (!this.advancements.isEmpty()) {
            jsonArray = new JsonObject();
            this.advancements.forEach((arg_0, arg_1) -> PlayerPredicate.lambda$serializeToJson$4((JsonObject)jsonArray, arg_0, arg_1));
            jsonObject.add("advancements", (JsonElement)jsonArray);
        }
        jsonObject.add("looking_at", this.lookingAt.serializeToJson());
        return jsonObject;
    }

    private static /* synthetic */ void lambda$serializeToJson$4(JsonObject jsonObject, ResourceLocation resourceLocation, AdvancementPredicate advancementPredicate) {
        jsonObject.add(resourceLocation.toString(), advancementPredicate.toJson());
    }

    private static /* synthetic */ void lambda$serializeToJson$3(JsonObject jsonObject, ResourceLocation resourceLocation, Boolean bl) {
        jsonObject.addProperty(resourceLocation.toString(), bl);
    }

    private static /* synthetic */ void lambda$advancementPredicateFromJson$0(Object2BooleanMap object2BooleanMap, Map.Entry entry) {
        boolean bl = GsonHelper.convertToBoolean((JsonElement)entry.getValue(), "criterion test");
        object2BooleanMap.put((Object)((String)entry.getKey()), bl);
    }

    static class AdvancementDonePredicate
    implements AdvancementPredicate {
        private final boolean state;

        public AdvancementDonePredicate(boolean bl) {
            this.state = bl;
        }

        @Override
        public JsonElement toJson() {
            return new JsonPrimitive(Boolean.valueOf(this.state));
        }

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            return advancementProgress.isDone() == this.state;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }

    static class AdvancementCriterionsPredicate
    implements AdvancementPredicate {
        private final Object2BooleanMap<String> criterions;

        public AdvancementCriterionsPredicate(Object2BooleanMap<String> object2BooleanMap) {
            this.criterions = object2BooleanMap;
        }

        @Override
        public JsonElement toJson() {
            JsonObject jsonObject = new JsonObject();
            this.criterions.forEach((arg_0, arg_1) -> ((JsonObject)jsonObject).addProperty(arg_0, arg_1));
            return jsonObject;
        }

        @Override
        public boolean test(AdvancementProgress advancementProgress) {
            for (Object2BooleanMap.Entry entry : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress criterionProgress = advancementProgress.getCriterion((String)entry.getKey());
                if (criterionProgress != null && criterionProgress.isDone() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((AdvancementProgress)object);
        }
    }

    static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public JsonElement toJson();
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        @Nullable
        private GameType gameType;
        private final Map<Stat<?>, MinMaxBounds.Ints> stats = Maps.newHashMap();
        private final Object2BooleanMap<ResourceLocation> recipes = new Object2BooleanOpenHashMap();
        private final Map<ResourceLocation, AdvancementPredicate> advancements = Maps.newHashMap();
        private EntityPredicate lookingAt = EntityPredicate.ANY;

        public static Builder player() {
            return new Builder();
        }

        public Builder setLevel(MinMaxBounds.Ints ints) {
            this.level = ints;
            return this;
        }

        public Builder addStat(Stat<?> stat, MinMaxBounds.Ints ints) {
            this.stats.put(stat, ints);
            return this;
        }

        public Builder addRecipe(ResourceLocation resourceLocation, boolean bl) {
            this.recipes.put((Object)resourceLocation, bl);
            return this;
        }

        public Builder setGameType(GameType gameType) {
            this.gameType = gameType;
            return this;
        }

        public Builder setLookingAt(EntityPredicate entityPredicate) {
            this.lookingAt = entityPredicate;
            return this;
        }

        public Builder checkAdvancementDone(ResourceLocation resourceLocation, boolean bl) {
            this.advancements.put(resourceLocation, new AdvancementDonePredicate(bl));
            return this;
        }

        public Builder checkAdvancementCriterions(ResourceLocation resourceLocation, Map<String, Boolean> map) {
            this.advancements.put(resourceLocation, new AdvancementCriterionsPredicate((Object2BooleanMap<String>)new Object2BooleanOpenHashMap(map)));
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.gameType, this.stats, this.recipes, this.advancements, this.lookingAt);
        }
    }
}

