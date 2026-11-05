/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemOverrides {
    public static final ItemOverrides EMPTY = new ItemOverrides();
    private final BakedOverride[] overrides;
    private final ResourceLocation[] properties;

    private ItemOverrides() {
        this.overrides = new BakedOverride[0];
        this.properties = new ResourceLocation[0];
    }

    public ItemOverrides(ModelBakery modelBakery, BlockModel blockModel, Function<ResourceLocation, UnbakedModel> function, List<ItemOverride> list) {
        this.properties = (ResourceLocation[])list.stream().flatMap(ItemOverride::getPredicates).map(ItemOverride.Predicate::getProperty).distinct().toArray(ResourceLocation[]::new);
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        for (int i = 0; i < this.properties.length; ++i) {
            object2IntOpenHashMap.put((Object)this.properties[i], i);
        }
        ArrayList arrayList = Lists.newArrayList();
        for (int i = list.size() - 1; i >= 0; --i) {
            ItemOverride itemOverride = list.get(i);
            BakedModel bakedModel = this.bakeModel(modelBakery, blockModel, function, itemOverride);
            PropertyMatcher[] propertyMatcherArray = (PropertyMatcher[])itemOverride.getPredicates().map(arg_0 -> ItemOverrides.lambda$new$1((Object2IntMap)object2IntOpenHashMap, arg_0)).toArray(PropertyMatcher[]::new);
            arrayList.add(new BakedOverride(propertyMatcherArray, bakedModel));
        }
        this.overrides = arrayList.toArray(new BakedOverride[0]);
    }

    @Nullable
    private BakedModel bakeModel(ModelBakery modelBakery, BlockModel blockModel, Function<ResourceLocation, UnbakedModel> function, ItemOverride itemOverride) {
        UnbakedModel unbakedModel = function.apply(itemOverride.getModel());
        if (Objects.equals(unbakedModel, blockModel)) {
            return null;
        }
        return modelBakery.bake(itemOverride.getModel(), BlockModelRotation.X0_Y0);
    }

    @Nullable
    public BakedModel resolve(BakedModel bakedModel, ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        if (this.overrides.length != 0) {
            Item item = itemStack.getItem();
            int n2 = this.properties.length;
            float[] fArray = new float[n2];
            for (int i = 0; i < n2; ++i) {
                ResourceLocation resourceLocation = this.properties[i];
                ItemPropertyFunction itemPropertyFunction = ItemProperties.getProperty(item, resourceLocation);
                fArray[i] = itemPropertyFunction != null ? itemPropertyFunction.call(itemStack, clientLevel, livingEntity, n) : Float.NEGATIVE_INFINITY;
            }
            for (BakedOverride bakedOverride : this.overrides) {
                if (!bakedOverride.test(fArray)) continue;
                BakedModel bakedModel2 = bakedOverride.model;
                if (bakedModel2 == null) {
                    return bakedModel;
                }
                return bakedModel2;
            }
        }
        return bakedModel;
    }

    private static /* synthetic */ PropertyMatcher lambda$new$1(Object2IntMap object2IntMap, ItemOverride.Predicate predicate) {
        int n = object2IntMap.getInt((Object)predicate.getProperty());
        return new PropertyMatcher(n, predicate.getValue());
    }

    static class BakedOverride {
        private final PropertyMatcher[] matchers;
        @Nullable
        final BakedModel model;

        BakedOverride(PropertyMatcher[] propertyMatcherArray, @Nullable BakedModel bakedModel) {
            this.matchers = propertyMatcherArray;
            this.model = bakedModel;
        }

        boolean test(float[] fArray) {
            for (PropertyMatcher propertyMatcher : this.matchers) {
                float f = fArray[propertyMatcher.index];
                if (!(f < propertyMatcher.value)) continue;
                return false;
            }
            return true;
        }
    }

    static class PropertyMatcher {
        public final int index;
        public final float value;

        PropertyMatcher(int n, float f) {
            this.index = n;
            this.value = f;
        }
    }
}

