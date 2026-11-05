/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSyntaxException
 */
package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ShapedRecipe
implements CraftingRecipe {
    final int width;
    final int height;
    final NonNullList<Ingredient> recipeItems;
    final ItemStack result;
    private final ResourceLocation id;
    final String group;

    public ShapedRecipe(ResourceLocation resourceLocation, String string, int n, int n2, NonNullList<Ingredient> nonNullList, ItemStack itemStack) {
        this.id = resourceLocation;
        this.group = string;
        this.width = n;
        this.height = n2;
        this.recipeItems = nonNullList;
        this.result = itemStack;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.recipeItems;
    }

    @Override
    public boolean canCraftInDimensions(int n, int n2) {
        return n >= this.width && n2 >= this.height;
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        for (int i = 0; i <= craftingContainer.getWidth() - this.width; ++i) {
            for (int j = 0; j <= craftingContainer.getHeight() - this.height; ++j) {
                if (this.matches(craftingContainer, i, j, true)) {
                    return true;
                }
                if (!this.matches(craftingContainer, i, j, false)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean matches(CraftingContainer craftingContainer, int n, int n2, boolean bl) {
        for (int i = 0; i < craftingContainer.getWidth(); ++i) {
            for (int j = 0; j < craftingContainer.getHeight(); ++j) {
                int n3 = i - n;
                int n4 = j - n2;
                Ingredient ingredient = Ingredient.EMPTY;
                if (n3 >= 0 && n4 >= 0 && n3 < this.width && n4 < this.height) {
                    ingredient = bl ? this.recipeItems.get(this.width - n3 - 1 + n4 * this.width) : this.recipeItems.get(n3 + n4 * this.width);
                }
                if (ingredient.test(craftingContainer.getItem(i + j * craftingContainer.getWidth()))) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        return this.getResultItem().copy();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    static NonNullList<Ingredient> dissolvePattern(String[] stringArray, Map<String, Ingredient> map, int n, int n2) {
        NonNullList<Ingredient> nonNullList = NonNullList.withSize(n * n2, Ingredient.EMPTY);
        HashSet hashSet = Sets.newHashSet(map.keySet());
        hashSet.remove(" ");
        for (int i = 0; i < stringArray.length; ++i) {
            for (int j = 0; j < stringArray[i].length(); ++j) {
                String string = stringArray[i].substring(j, j + 1);
                Ingredient ingredient = map.get(string);
                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + string + "' but it's not defined in the key");
                }
                hashSet.remove(string);
                nonNullList.set(j + n * i, ingredient);
            }
        }
        if (!hashSet.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + hashSet);
        }
        return nonNullList;
    }

    @VisibleForTesting
    static String[] shrink(String ... stringArray) {
        int n = Integer.MAX_VALUE;
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        for (int i = 0; i < stringArray.length; ++i) {
            String string = stringArray[i];
            n = Math.min(n, ShapedRecipe.firstNonSpace(string));
            int n5 = ShapedRecipe.lastNonSpace(string);
            n2 = Math.max(n2, n5);
            if (n5 < 0) {
                if (n3 == i) {
                    ++n3;
                }
                ++n4;
                continue;
            }
            n4 = 0;
        }
        if (stringArray.length == n4) {
            return new String[0];
        }
        String[] stringArray2 = new String[stringArray.length - n4 - n3];
        for (int i = 0; i < stringArray2.length; ++i) {
            stringArray2[i] = stringArray[i + n3].substring(n, n2 + 1);
        }
        return stringArray2;
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> nonNullList = this.getIngredients();
        return nonNullList.isEmpty() || nonNullList.stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.getItems().length == 0);
    }

    private static int firstNonSpace(String string) {
        int n;
        for (n = 0; n < string.length() && string.charAt(n) == ' '; ++n) {
        }
        return n;
    }

    private static int lastNonSpace(String string) {
        int n;
        for (n = string.length() - 1; n >= 0 && string.charAt(n) == ' '; --n) {
        }
        return n;
    }

    static String[] patternFromJson(JsonArray jsonArray) {
        String[] stringArray = new String[jsonArray.size()];
        if (stringArray.length > 3) {
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
        }
        if (stringArray.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }
        for (int i = 0; i < stringArray.length; ++i) {
            String string = GsonHelper.convertToString(jsonArray.get(i), "pattern[" + i + "]");
            if (string.length() > 3) {
                throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            }
            if (i > 0 && stringArray[0].length() != string.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }
            stringArray[i] = string;
        }
        return stringArray;
    }

    static Map<String, Ingredient> keyFromJson(JsonObject jsonObject) {
        HashMap hashMap = Maps.newHashMap();
        for (Map.Entry entry : jsonObject.entrySet()) {
            if (((String)entry.getKey()).length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + (String)entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }
            hashMap.put((String)entry.getKey(), Ingredient.fromJson((JsonElement)entry.getValue()));
        }
        hashMap.put(" ", Ingredient.EMPTY);
        return hashMap;
    }

    public static ItemStack itemStackFromJson(JsonObject jsonObject) {
        Item item = ShapedRecipe.itemFromJson(jsonObject);
        if (jsonObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
        }
        int n = GsonHelper.getAsInt(jsonObject, "count", 1);
        if (n < 1) {
            throw new JsonSyntaxException("Invalid output count: " + n);
        }
        return new ItemStack(item, n);
    }

    public static Item itemFromJson(JsonObject jsonObject) {
        String string = GsonHelper.getAsString(jsonObject, "item");
        Item item = Registry.ITEM.getOptional(new ResourceLocation(string)).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + string + "'"));
        if (item == Items.AIR) {
            throw new JsonSyntaxException("Invalid item: " + string);
        }
        return item;
    }

    public static class Serializer
    implements RecipeSerializer<ShapedRecipe> {
        @Override
        public ShapedRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            String string = GsonHelper.getAsString(jsonObject, "group", "");
            Map<String, Ingredient> map = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(jsonObject, "key"));
            String[] stringArray = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(jsonObject, "pattern")));
            int n = stringArray[0].length();
            int n2 = stringArray.length;
            NonNullList<Ingredient> nonNullList = ShapedRecipe.dissolvePattern(stringArray, map, n, n2);
            ItemStack itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
            return new ShapedRecipe(resourceLocation, string, n, n2, nonNullList, itemStack);
        }

        @Override
        public ShapedRecipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            int n = friendlyByteBuf.readVarInt();
            int n2 = friendlyByteBuf.readVarInt();
            String string = friendlyByteBuf.readUtf();
            NonNullList<Ingredient> nonNullList = NonNullList.withSize(n * n2, Ingredient.EMPTY);
            for (int i = 0; i < nonNullList.size(); ++i) {
                nonNullList.set(i, Ingredient.fromNetwork(friendlyByteBuf));
            }
            ItemStack itemStack = friendlyByteBuf.readItem();
            return new ShapedRecipe(resourceLocation, string, n, n2, nonNullList, itemStack);
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, ShapedRecipe shapedRecipe) {
            friendlyByteBuf.writeVarInt(shapedRecipe.width);
            friendlyByteBuf.writeVarInt(shapedRecipe.height);
            friendlyByteBuf.writeUtf(shapedRecipe.group);
            for (Ingredient ingredient : shapedRecipe.recipeItems) {
                ingredient.toNetwork(friendlyByteBuf);
            }
            friendlyByteBuf.writeItem(shapedRecipe.result);
        }

        @Override
        public /* synthetic */ Recipe fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            return this.fromNetwork(resourceLocation, friendlyByteBuf);
        }

        @Override
        public /* synthetic */ Recipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            return this.fromJson(resourceLocation, jsonObject);
        }
    }
}

