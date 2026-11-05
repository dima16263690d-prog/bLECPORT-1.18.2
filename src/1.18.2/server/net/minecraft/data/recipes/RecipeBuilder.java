/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.data.recipes;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
    public RecipeBuilder unlockedBy(String var1, CriterionTriggerInstance var2);

    public RecipeBuilder group(@Nullable String var1);

    public Item getResult();

    public void save(Consumer<FinishedRecipe> var1, ResourceLocation var2);

    default public void save(Consumer<FinishedRecipe> consumer) {
        this.save(consumer, RecipeBuilder.getDefaultRecipeId(this.getResult()));
    }

    default public void save(Consumer<FinishedRecipe> consumer, String string) {
        ResourceLocation resourceLocation = new ResourceLocation(string);
        ResourceLocation resourceLocation2 = RecipeBuilder.getDefaultRecipeId(this.getResult());
        if (resourceLocation.equals(resourceLocation2)) {
            throw new IllegalStateException("Recipe " + string + " should remove its 'save' argument as it is equal to default one");
        }
        this.save(consumer, resourceLocation);
    }

    public static ResourceLocation getDefaultRecipeId(ItemLike itemLike) {
        return Registry.ITEM.getKey(itemLike.asItem());
    }
}

