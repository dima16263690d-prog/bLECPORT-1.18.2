/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonSyntaxException
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.ArrayUtils;

public class Advancement {
    @Nullable
    private final Advancement parent;
    @Nullable
    private final DisplayInfo display;
    private final AdvancementRewards rewards;
    private final ResourceLocation id;
    private final Map<String, Criterion> criteria;
    private final String[][] requirements;
    private final Set<Advancement> children = Sets.newLinkedHashSet();
    private final Component chatComponent;

    public Advancement(ResourceLocation resourceLocation, @Nullable Advancement advancement, @Nullable DisplayInfo displayInfo, AdvancementRewards advancementRewards, Map<String, Criterion> map, String[][] stringArray) {
        this.id = resourceLocation;
        this.display = displayInfo;
        this.criteria = ImmutableMap.copyOf(map);
        this.parent = advancement;
        this.rewards = advancementRewards;
        this.requirements = stringArray;
        if (advancement != null) {
            advancement.addChild(this);
        }
        if (displayInfo == null) {
            this.chatComponent = new TextComponent(resourceLocation.toString());
        } else {
            Component component = displayInfo.getTitle();
            ChatFormatting chatFormatting = displayInfo.getFrame().getChatColor();
            MutableComponent mutableComponent = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatFormatting)).append("\n").append(displayInfo.getDescription());
            MutableComponent mutableComponent2 = component.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableComponent)));
            this.chatComponent = ComponentUtils.wrapInSquareBrackets(mutableComponent2).withStyle(chatFormatting);
        }
    }

    public Builder deconstruct() {
        return new Builder(this.parent == null ? null : this.parent.getId(), this.display, this.rewards, this.criteria, this.requirements);
    }

    @Nullable
    public Advancement getParent() {
        return this.parent;
    }

    @Nullable
    public DisplayInfo getDisplay() {
        return this.display;
    }

    public AdvancementRewards getRewards() {
        return this.rewards;
    }

    public String toString() {
        return "SimpleAdvancement{id=" + this.getId() + ", parent=" + (Comparable)(this.parent == null ? "null" : this.parent.getId()) + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString((Object[])this.requirements) + "}";
    }

    public Iterable<Advancement> getChildren() {
        return this.children;
    }

    public Map<String, Criterion> getCriteria() {
        return this.criteria;
    }

    public int getMaxCriteraRequired() {
        return this.requirements.length;
    }

    public void addChild(Advancement advancement) {
        this.children.add(advancement);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Advancement)) {
            return false;
        }
        Advancement advancement = (Advancement)object;
        return this.id.equals(advancement.id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String[][] getRequirements() {
        return this.requirements;
    }

    public Component getChatComponent() {
        return this.chatComponent;
    }

    public static class Builder {
        @Nullable
        private ResourceLocation parentId;
        @Nullable
        private Advancement parent;
        @Nullable
        private DisplayInfo display;
        private AdvancementRewards rewards = AdvancementRewards.EMPTY;
        private Map<String, Criterion> criteria = Maps.newLinkedHashMap();
        @Nullable
        private String[][] requirements;
        private RequirementsStrategy requirementsStrategy = RequirementsStrategy.AND;

        Builder(@Nullable ResourceLocation resourceLocation, @Nullable DisplayInfo displayInfo, AdvancementRewards advancementRewards, Map<String, Criterion> map, String[][] stringArray) {
            this.parentId = resourceLocation;
            this.display = displayInfo;
            this.rewards = advancementRewards;
            this.criteria = map;
            this.requirements = stringArray;
        }

        private Builder() {
        }

        public static Builder advancement() {
            return new Builder();
        }

        public Builder parent(Advancement advancement) {
            this.parent = advancement;
            return this;
        }

        public Builder parent(ResourceLocation resourceLocation) {
            this.parentId = resourceLocation;
            return this;
        }

        public Builder display(ItemStack itemStack, Component component, Component component2, @Nullable ResourceLocation resourceLocation, FrameType frameType, boolean bl, boolean bl2, boolean bl3) {
            return this.display(new DisplayInfo(itemStack, component, component2, resourceLocation, frameType, bl, bl2, bl3));
        }

        public Builder display(ItemLike itemLike, Component component, Component component2, @Nullable ResourceLocation resourceLocation, FrameType frameType, boolean bl, boolean bl2, boolean bl3) {
            return this.display(new DisplayInfo(new ItemStack(itemLike.asItem()), component, component2, resourceLocation, frameType, bl, bl2, bl3));
        }

        public Builder display(DisplayInfo displayInfo) {
            this.display = displayInfo;
            return this;
        }

        public Builder rewards(AdvancementRewards.Builder builder) {
            return this.rewards(builder.build());
        }

        public Builder rewards(AdvancementRewards advancementRewards) {
            this.rewards = advancementRewards;
            return this;
        }

        public Builder addCriterion(String string, CriterionTriggerInstance criterionTriggerInstance) {
            return this.addCriterion(string, new Criterion(criterionTriggerInstance));
        }

        public Builder addCriterion(String string, Criterion criterion) {
            if (this.criteria.containsKey(string)) {
                throw new IllegalArgumentException("Duplicate criterion " + string);
            }
            this.criteria.put(string, criterion);
            return this;
        }

        public Builder requirements(RequirementsStrategy requirementsStrategy) {
            this.requirementsStrategy = requirementsStrategy;
            return this;
        }

        public Builder requirements(String[][] stringArray) {
            this.requirements = stringArray;
            return this;
        }

        public boolean canBuild(Function<ResourceLocation, Advancement> function) {
            if (this.parentId == null) {
                return true;
            }
            if (this.parent == null) {
                this.parent = function.apply(this.parentId);
            }
            return this.parent != null;
        }

        public Advancement build(ResourceLocation resourceLocation2) {
            if (!this.canBuild(resourceLocation -> null)) {
                throw new IllegalStateException("Tried to build incomplete advancement!");
            }
            if (this.requirements == null) {
                this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }
            return new Advancement(resourceLocation2, this.parent, this.display, this.rewards, this.criteria, this.requirements);
        }

        public Advancement save(Consumer<Advancement> consumer, String string) {
            Advancement advancement = this.build(new ResourceLocation(string));
            consumer.accept(advancement);
            return advancement;
        }

        public JsonObject serializeToJson() {
            if (this.requirements == null) {
                this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }
            JsonObject jsonObject = new JsonObject();
            if (this.parent != null) {
                jsonObject.addProperty("parent", this.parent.getId().toString());
            } else if (this.parentId != null) {
                jsonObject.addProperty("parent", this.parentId.toString());
            }
            if (this.display != null) {
                jsonObject.add("display", this.display.serializeToJson());
            }
            jsonObject.add("rewards", this.rewards.serializeToJson());
            JsonObject jsonObject2 = new JsonObject();
            for (Map.Entry<String, Criterion> stringArray : this.criteria.entrySet()) {
                jsonObject2.add(stringArray.getKey(), stringArray.getValue().serializeToJson());
            }
            jsonObject.add("criteria", (JsonElement)jsonObject2);
            JsonArray jsonArray = new JsonArray();
            for (String[] stringArray : this.requirements) {
                JsonArray jsonArray2 = new JsonArray();
                for (String string : stringArray) {
                    jsonArray2.add(string);
                }
                jsonArray.add((JsonElement)jsonArray2);
            }
            jsonObject.add("requirements", (JsonElement)jsonArray);
            return jsonObject;
        }

        public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
            if (this.requirements == null) {
                this.requirements = this.requirementsStrategy.createRequirements(this.criteria.keySet());
            }
            if (this.parentId == null) {
                friendlyByteBuf.writeBoolean(false);
            } else {
                friendlyByteBuf.writeBoolean(true);
                friendlyByteBuf.writeResourceLocation(this.parentId);
            }
            if (this.display == null) {
                friendlyByteBuf.writeBoolean(false);
            } else {
                friendlyByteBuf.writeBoolean(true);
                this.display.serializeToNetwork(friendlyByteBuf);
            }
            Criterion.serializeToNetwork(this.criteria, friendlyByteBuf);
            friendlyByteBuf.writeVarInt(this.requirements.length);
            for (String[] stringArray : this.requirements) {
                friendlyByteBuf.writeVarInt(stringArray.length);
                for (String string : stringArray) {
                    friendlyByteBuf.writeUtf(string);
                }
            }
        }

        public String toString() {
            return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + Arrays.deepToString((Object[])this.requirements) + "}";
        }

        public static Builder fromJson(JsonObject jsonObject, DeserializationContext deserializationContext) {
            int n;
            ResourceLocation resourceLocation = jsonObject.has("parent") ? new ResourceLocation(GsonHelper.getAsString(jsonObject, "parent")) : null;
            DisplayInfo displayInfo = jsonObject.has("display") ? DisplayInfo.fromJson(GsonHelper.getAsJsonObject(jsonObject, "display")) : null;
            AdvancementRewards advancementRewards = jsonObject.has("rewards") ? AdvancementRewards.deserialize(GsonHelper.getAsJsonObject(jsonObject, "rewards")) : AdvancementRewards.EMPTY;
            Map<String, Criterion> map = Criterion.criteriaFromJson(GsonHelper.getAsJsonObject(jsonObject, "criteria"), deserializationContext);
            if (map.isEmpty()) {
                throw new JsonSyntaxException("Advancement criteria cannot be empty");
            }
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "requirements", new JsonArray());
            String[][] stringArray = new String[jsonArray.size()][];
            for (n = 0; n < jsonArray.size(); ++n) {
                JsonArray jsonArray2 = GsonHelper.convertToJsonArray(jsonArray.get(n), "requirements[" + n + "]");
                stringArray[n] = new String[jsonArray2.size()];
                for (int i = 0; i < jsonArray2.size(); ++i) {
                    stringArray[n][i] = GsonHelper.convertToString(jsonArray2.get(i), "requirements[" + n + "][" + i + "]");
                }
            }
            if (stringArray.length == 0) {
                stringArray = new String[map.size()][];
                n = 0;
                for (String string : map.keySet()) {
                    stringArray[n++] = new String[]{string};
                }
            }
            for (String[] stringArray2 : stringArray) {
                if (stringArray2.length == 0 && map.isEmpty()) {
                    throw new JsonSyntaxException("Requirement entry cannot be empty");
                }
                String[] stringArray3 = stringArray2;
                int n2 = stringArray3.length;
                for (int i = 0; i < n2; ++i) {
                    String string = stringArray3[i];
                    if (map.containsKey(string)) continue;
                    throw new JsonSyntaxException("Unknown required criterion '" + string + "'");
                }
            }
            for (String string : map.keySet()) {
                int n3 = 0;
                for (String string2 : stringArray) {
                    if (!ArrayUtils.contains((Object[])string2, (Object)string)) continue;
                    n3 = 1;
                    break;
                }
                if (n3 != 0) continue;
                throw new JsonSyntaxException("Criterion '" + string + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
            }
            return new Builder(resourceLocation, displayInfo, advancementRewards, map, stringArray);
        }

        public static Builder fromNetwork(FriendlyByteBuf friendlyByteBuf) {
            ResourceLocation resourceLocation = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readResourceLocation() : null;
            DisplayInfo displayInfo = friendlyByteBuf.readBoolean() ? DisplayInfo.fromNetwork(friendlyByteBuf) : null;
            Map<String, Criterion> map = Criterion.criteriaFromNetwork(friendlyByteBuf);
            String[][] stringArray = new String[friendlyByteBuf.readVarInt()][];
            for (int i = 0; i < stringArray.length; ++i) {
                stringArray[i] = new String[friendlyByteBuf.readVarInt()];
                for (int j = 0; j < stringArray[i].length; ++j) {
                    stringArray[i][j] = friendlyByteBuf.readUtf();
                }
            }
            return new Builder(resourceLocation, displayInfo, AdvancementRewards.EMPTY, map, stringArray);
        }

        public Map<String, Criterion> getCriteria() {
            return this.criteria;
        }
    }
}

