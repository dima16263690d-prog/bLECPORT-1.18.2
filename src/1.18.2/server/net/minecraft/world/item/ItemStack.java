/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Streams
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.AdventureModeCheck;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.slf4j.Logger;

public final class ItemStack {
    public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Registry.ITEM.byNameCodec().fieldOf("id").forGetter(itemStack -> itemStack.item), (App)Codec.INT.fieldOf("Count").forGetter(itemStack -> itemStack.count), (App)CompoundTag.CODEC.optionalFieldOf("tag").forGetter(itemStack -> Optional.ofNullable(itemStack.tag))).apply((Applicative)instance, ItemStack::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((ItemLike)null);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), decimalFormat -> decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
    public static final String TAG_ENCH = "Enchantments";
    public static final String TAG_DISPLAY = "display";
    public static final String TAG_DISPLAY_NAME = "Name";
    public static final String TAG_LORE = "Lore";
    public static final String TAG_DAMAGE = "Damage";
    public static final String TAG_COLOR = "color";
    private static final String TAG_UNBREAKABLE = "Unbreakable";
    private static final String TAG_REPAIR_COST = "RepairCost";
    private static final String TAG_CAN_DESTROY_BLOCK_LIST = "CanDestroy";
    private static final String TAG_CAN_PLACE_ON_BLOCK_LIST = "CanPlaceOn";
    private static final String TAG_HIDE_FLAGS = "HideFlags";
    private static final int DONT_HIDE_TOOLTIP = 0;
    private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
    private int count;
    private int popTime;
    @Deprecated
    private final Item item;
    @Nullable
    private CompoundTag tag;
    private boolean emptyCacheFlag;
    @Nullable
    private Entity entityRepresentation;
    @Nullable
    private AdventureModeCheck adventureBreakCheck;
    @Nullable
    private AdventureModeCheck adventurePlaceCheck;

    public Optional<TooltipComponent> getTooltipImage() {
        return this.getItem().getTooltipImage(this);
    }

    public ItemStack(ItemLike itemLike) {
        this(itemLike, 1);
    }

    public ItemStack(Holder<Item> holder) {
        this(holder.value(), 1);
    }

    private ItemStack(ItemLike itemLike, int n, Optional<CompoundTag> optional) {
        this(itemLike, n);
        optional.ifPresent(this::setTag);
    }

    public ItemStack(ItemLike itemLike, int n) {
        this.item = itemLike == null ? null : itemLike.asItem();
        this.count = n;
        if (this.item != null && this.item.canBeDepleted()) {
            this.setDamageValue(this.getDamageValue());
        }
        this.updateEmptyCacheFlag();
    }

    private void updateEmptyCacheFlag() {
        this.emptyCacheFlag = false;
        this.emptyCacheFlag = this.isEmpty();
    }

    private ItemStack(CompoundTag compoundTag) {
        this.item = Registry.ITEM.get(new ResourceLocation(compoundTag.getString("id")));
        this.count = compoundTag.getByte("Count");
        if (compoundTag.contains("tag", 10)) {
            this.tag = compoundTag.getCompound("tag");
            this.getItem().verifyTagAfterLoad(this.tag);
        }
        if (this.getItem().canBeDepleted()) {
            this.setDamageValue(this.getDamageValue());
        }
        this.updateEmptyCacheFlag();
    }

    public static ItemStack of(CompoundTag compoundTag) {
        try {
            return new ItemStack(compoundTag);
        }
        catch (RuntimeException runtimeException) {
            LOGGER.debug("Tried to load invalid item: {}", (Object)compoundTag, (Object)runtimeException);
            return EMPTY;
        }
    }

    public boolean isEmpty() {
        if (this == EMPTY) {
            return true;
        }
        if (this.getItem() == null || this.is(Items.AIR)) {
            return true;
        }
        return this.count <= 0;
    }

    public ItemStack split(int n) {
        int n2 = Math.min(n, this.count);
        ItemStack itemStack = this.copy();
        itemStack.setCount(n2);
        this.shrink(n2);
        return itemStack;
    }

    public Item getItem() {
        return this.emptyCacheFlag ? Items.AIR : this.item;
    }

    public boolean is(TagKey<Item> tagKey) {
        return this.getItem().builtInRegistryHolder().is(tagKey);
    }

    public boolean is(Item item) {
        return this.getItem() == item;
    }

    public Stream<TagKey<Item>> getTags() {
        return this.getItem().builtInRegistryHolder().tags();
    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        Player player = useOnContext.getPlayer();
        BlockPos blockPos = useOnContext.getClickedPos();
        BlockInWorld blockInWorld = new BlockInWorld(useOnContext.getLevel(), blockPos, false);
        if (player != null && !player.getAbilities().mayBuild && !this.hasAdventureModePlaceTagForBlock(useOnContext.getLevel().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY), blockInWorld)) {
            return InteractionResult.PASS;
        }
        Item item = this.getItem();
        InteractionResult interactionResult = item.useOn(useOnContext);
        if (player != null && interactionResult.shouldAwardStats()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
        return interactionResult;
    }

    public float getDestroySpeed(BlockState blockState) {
        return this.getItem().getDestroySpeed(this, blockState);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        return this.getItem().use(level, player, interactionHand);
    }

    public ItemStack finishUsingItem(Level level, LivingEntity livingEntity) {
        return this.getItem().finishUsingItem(this, level, livingEntity);
    }

    public CompoundTag save(CompoundTag compoundTag) {
        ResourceLocation resourceLocation = Registry.ITEM.getKey(this.getItem());
        compoundTag.putString("id", resourceLocation == null ? "minecraft:air" : resourceLocation.toString());
        compoundTag.putByte("Count", (byte)this.count);
        if (this.tag != null) {
            compoundTag.put("tag", this.tag.copy());
        }
        return compoundTag;
    }

    public int getMaxStackSize() {
        return this.getItem().getMaxStackSize();
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
    }

    public boolean isDamageableItem() {
        if (this.emptyCacheFlag || this.getItem().getMaxDamage() <= 0) {
            return false;
        }
        CompoundTag compoundTag = this.getTag();
        return compoundTag == null || !compoundTag.getBoolean(TAG_UNBREAKABLE);
    }

    public boolean isDamaged() {
        return this.isDamageableItem() && this.getDamageValue() > 0;
    }

    public int getDamageValue() {
        return this.tag == null ? 0 : this.tag.getInt(TAG_DAMAGE);
    }

    public void setDamageValue(int n) {
        this.getOrCreateTag().putInt(TAG_DAMAGE, Math.max(0, n));
    }

    public int getMaxDamage() {
        return this.getItem().getMaxDamage();
    }

    public boolean hurt(int n, Random random, @Nullable ServerPlayer serverPlayer) {
        int n2;
        if (!this.isDamageableItem()) {
            return false;
        }
        if (n > 0) {
            n2 = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, this);
            int n3 = 0;
            for (int i = 0; n2 > 0 && i < n; ++i) {
                if (!DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(this, n2, random)) continue;
                ++n3;
            }
            if ((n -= n3) <= 0) {
                return false;
            }
        }
        if (serverPlayer != null && n != 0) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, this, this.getDamageValue() + n);
        }
        n2 = this.getDamageValue() + n;
        this.setDamageValue(n2);
        return n2 >= this.getMaxDamage();
    }

    public <T extends LivingEntity> void hurtAndBreak(int n, T t, Consumer<T> consumer) {
        if (t.level.isClientSide || t instanceof Player && ((Player)t).getAbilities().instabuild) {
            return;
        }
        if (!this.isDamageableItem()) {
            return;
        }
        if (this.hurt(n, t.getRandom(), t instanceof ServerPlayer ? (ServerPlayer)t : null)) {
            consumer.accept(t);
            Item item = this.getItem();
            this.shrink(1);
            if (t instanceof Player) {
                ((Player)t).awardStat(Stats.ITEM_BROKEN.get(item));
            }
            this.setDamageValue(0);
        }
    }

    public boolean isBarVisible() {
        return this.item.isBarVisible(this);
    }

    public int getBarWidth() {
        return this.item.getBarWidth(this);
    }

    public int getBarColor() {
        return this.item.getBarColor(this);
    }

    public boolean overrideStackedOnOther(Slot slot, ClickAction clickAction, Player player) {
        return this.getItem().overrideStackedOnOther(this, slot, clickAction, player);
    }

    public boolean overrideOtherStackedOnMe(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        return this.getItem().overrideOtherStackedOnMe(this, itemStack, slot, clickAction, player, slotAccess);
    }

    public void hurtEnemy(LivingEntity livingEntity, Player player) {
        Item item = this.getItem();
        if (item.hurtEnemy(this, livingEntity, player)) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public void mineBlock(Level level, BlockState blockState, BlockPos blockPos, Player player) {
        Item item = this.getItem();
        if (item.mineBlock(this, level, blockState, blockPos, player)) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public boolean isCorrectToolForDrops(BlockState blockState) {
        return this.getItem().isCorrectToolForDrops(blockState);
    }

    public InteractionResult interactLivingEntity(Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        return this.getItem().interactLivingEntity(this, player, livingEntity, interactionHand);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemStack = new ItemStack(this.getItem(), this.count);
        itemStack.setPopTime(this.getPopTime());
        if (this.tag != null) {
            itemStack.tag = this.tag.copy();
        }
        return itemStack;
    }

    public static boolean tagMatches(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return true;
        }
        if (itemStack.isEmpty() || itemStack2.isEmpty()) {
            return false;
        }
        if (itemStack.tag == null && itemStack2.tag != null) {
            return false;
        }
        return itemStack.tag == null || itemStack.tag.equals(itemStack2.tag);
    }

    public static boolean matches(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return true;
        }
        if (itemStack.isEmpty() || itemStack2.isEmpty()) {
            return false;
        }
        return itemStack.matches(itemStack2);
    }

    private boolean matches(ItemStack itemStack) {
        if (this.count != itemStack.count) {
            return false;
        }
        if (!this.is(itemStack.getItem())) {
            return false;
        }
        if (this.tag == null && itemStack.tag != null) {
            return false;
        }
        return this.tag == null || this.tag.equals(itemStack.tag);
    }

    public static boolean isSame(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack == itemStack2) {
            return true;
        }
        if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            return itemStack.sameItem(itemStack2);
        }
        return false;
    }

    public static boolean isSameIgnoreDurability(ItemStack itemStack, ItemStack itemStack2) {
        if (itemStack == itemStack2) {
            return true;
        }
        if (!itemStack.isEmpty() && !itemStack2.isEmpty()) {
            return itemStack.sameItemStackIgnoreDurability(itemStack2);
        }
        return false;
    }

    public boolean sameItem(ItemStack itemStack) {
        return !itemStack.isEmpty() && this.is(itemStack.getItem());
    }

    public boolean sameItemStackIgnoreDurability(ItemStack itemStack) {
        if (this.isDamageableItem()) {
            return !itemStack.isEmpty() && this.is(itemStack.getItem());
        }
        return this.sameItem(itemStack);
    }

    public static boolean isSameItemSameTags(ItemStack itemStack, ItemStack itemStack2) {
        return itemStack.is(itemStack2.getItem()) && ItemStack.tagMatches(itemStack, itemStack2);
    }

    public String getDescriptionId() {
        return this.getItem().getDescriptionId(this);
    }

    public String toString() {
        return this.count + " " + this.getItem();
    }

    public void inventoryTick(Level level, Entity entity, int n, boolean bl) {
        if (this.popTime > 0) {
            --this.popTime;
        }
        if (this.getItem() != null) {
            this.getItem().inventoryTick(this, level, entity, n, bl);
        }
    }

    public void onCraftedBy(Level level, Player player, int n) {
        player.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), n);
        this.getItem().onCraftedBy(this, level, player);
    }

    public int getUseDuration() {
        return this.getItem().getUseDuration(this);
    }

    public UseAnim getUseAnimation() {
        return this.getItem().getUseAnimation(this);
    }

    public void releaseUsing(Level level, LivingEntity livingEntity, int n) {
        this.getItem().releaseUsing(this, level, livingEntity, n);
    }

    public boolean useOnRelease() {
        return this.getItem().useOnRelease(this);
    }

    public boolean hasTag() {
        return !this.emptyCacheFlag && this.tag != null && !this.tag.isEmpty();
    }

    @Nullable
    public CompoundTag getTag() {
        return this.tag;
    }

    public CompoundTag getOrCreateTag() {
        if (this.tag == null) {
            this.setTag(new CompoundTag());
        }
        return this.tag;
    }

    public CompoundTag getOrCreateTagElement(String string) {
        if (this.tag == null || !this.tag.contains(string, 10)) {
            CompoundTag compoundTag = new CompoundTag();
            this.addTagElement(string, compoundTag);
            return compoundTag;
        }
        return this.tag.getCompound(string);
    }

    @Nullable
    public CompoundTag getTagElement(String string) {
        if (this.tag == null || !this.tag.contains(string, 10)) {
            return null;
        }
        return this.tag.getCompound(string);
    }

    public void removeTagKey(String string) {
        if (this.tag != null && this.tag.contains(string)) {
            this.tag.remove(string);
            if (this.tag.isEmpty()) {
                this.tag = null;
            }
        }
    }

    public ListTag getEnchantmentTags() {
        if (this.tag != null) {
            return this.tag.getList(TAG_ENCH, 10);
        }
        return new ListTag();
    }

    public void setTag(@Nullable CompoundTag compoundTag) {
        this.tag = compoundTag;
        if (this.getItem().canBeDepleted()) {
            this.setDamageValue(this.getDamageValue());
        }
        if (compoundTag != null) {
            this.getItem().verifyTagAfterLoad(compoundTag);
        }
    }

    public Component getHoverName() {
        CompoundTag compoundTag = this.getTagElement(TAG_DISPLAY);
        if (compoundTag != null && compoundTag.contains(TAG_DISPLAY_NAME, 8)) {
            try {
                MutableComponent mutableComponent = Component.Serializer.fromJson(compoundTag.getString(TAG_DISPLAY_NAME));
                if (mutableComponent != null) {
                    return mutableComponent;
                }
                compoundTag.remove(TAG_DISPLAY_NAME);
            }
            catch (Exception exception) {
                compoundTag.remove(TAG_DISPLAY_NAME);
            }
        }
        return this.getItem().getName(this);
    }

    public ItemStack setHoverName(@Nullable Component component) {
        CompoundTag compoundTag = this.getOrCreateTagElement(TAG_DISPLAY);
        if (component != null) {
            compoundTag.putString(TAG_DISPLAY_NAME, Component.Serializer.toJson(component));
        } else {
            compoundTag.remove(TAG_DISPLAY_NAME);
        }
        return this;
    }

    public void resetHoverName() {
        CompoundTag compoundTag = this.getTagElement(TAG_DISPLAY);
        if (compoundTag != null) {
            compoundTag.remove(TAG_DISPLAY_NAME);
            if (compoundTag.isEmpty()) {
                this.removeTagKey(TAG_DISPLAY);
            }
        }
        if (this.tag != null && this.tag.isEmpty()) {
            this.tag = null;
        }
    }

    public boolean hasCustomHoverName() {
        CompoundTag compoundTag = this.getTagElement(TAG_DISPLAY);
        return compoundTag != null && compoundTag.contains(TAG_DISPLAY_NAME, 8);
    }

    public List<Component> getTooltipLines(@Nullable Player player, TooltipFlag tooltipFlag) {
        Object object;
        Object object2;
        int n;
        Object object3;
        int n2;
        Integer n3;
        ArrayList arrayList = Lists.newArrayList();
        MutableComponent mutableComponent = new TextComponent("").append(this.getHoverName()).withStyle(this.getRarity().color);
        if (this.hasCustomHoverName()) {
            mutableComponent.withStyle(ChatFormatting.ITALIC);
        }
        arrayList.add(mutableComponent);
        if (!tooltipFlag.isAdvanced() && !this.hasCustomHoverName() && this.is(Items.FILLED_MAP) && (n3 = MapItem.getMapId(this)) != null) {
            arrayList.add(new TextComponent("#" + n3).withStyle(ChatFormatting.GRAY));
        }
        if (ItemStack.shouldShowInTooltip(n2 = this.getHideFlags(), TooltipPart.ADDITIONAL)) {
            this.getItem().appendHoverText(this, player == null ? null : player.level, arrayList, tooltipFlag);
        }
        if (this.hasTag()) {
            if (ItemStack.shouldShowInTooltip(n2, TooltipPart.ENCHANTMENTS)) {
                ItemStack.appendEnchantmentNames(arrayList, this.getEnchantmentTags());
            }
            if (this.tag.contains(TAG_DISPLAY, 10)) {
                object3 = this.tag.getCompound(TAG_DISPLAY);
                if (ItemStack.shouldShowInTooltip(n2, TooltipPart.DYE) && ((CompoundTag)object3).contains(TAG_COLOR, 99)) {
                    if (tooltipFlag.isAdvanced()) {
                        arrayList.add(new TranslatableComponent("item.color", String.format("#%06X", ((CompoundTag)object3).getInt(TAG_COLOR))).withStyle(ChatFormatting.GRAY));
                    } else {
                        arrayList.add(new TranslatableComponent("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    }
                }
                if (((CompoundTag)object3).getTagType(TAG_LORE) == 9) {
                    ListTag listTag = ((CompoundTag)object3).getList(TAG_LORE, 8);
                    for (n = 0; n < listTag.size(); ++n) {
                        object2 = listTag.getString(n);
                        try {
                            object = Component.Serializer.fromJson((String)object2);
                            if (object == null) continue;
                            arrayList.add(ComponentUtils.mergeStyles(object, LORE_STYLE));
                            continue;
                        }
                        catch (Exception exception) {
                            ((CompoundTag)object3).remove(TAG_LORE);
                        }
                    }
                }
            }
        }
        if (ItemStack.shouldShowInTooltip(n2, TooltipPart.MODIFIERS)) {
            object3 = EquipmentSlot.values();
            int n4 = ((EquipmentSlot[])object3).length;
            for (n = 0; n < n4; ++n) {
                object2 = object3[n];
                object = this.getAttributeModifiers((EquipmentSlot)((Object)object2));
                if (object.isEmpty()) continue;
                arrayList.add(TextComponent.EMPTY);
                arrayList.add(new TranslatableComponent("item.modifiers." + object2.getName()).withStyle(ChatFormatting.GRAY));
                for (Map.Entry entry : object.entries()) {
                    AttributeModifier attributeModifier = (AttributeModifier)entry.getValue();
                    double d = attributeModifier.getAmount();
                    boolean bl = false;
                    if (player != null) {
                        if (attributeModifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
                            d += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                            d += (double)EnchantmentHelper.getDamageBonus(this, MobType.UNDEFINED);
                            bl = true;
                        } else if (attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID) {
                            d += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                            bl = true;
                        }
                    }
                    double d2 = attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE || attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL ? d * 100.0 : (((Attribute)entry.getKey()).equals(Attributes.KNOCKBACK_RESISTANCE) ? d * 10.0 : d);
                    if (bl) {
                        arrayList.add(new TextComponent(" ").append(new TranslatableComponent("attribute.modifier.equals." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d2), new TranslatableComponent(((Attribute)entry.getKey()).getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
                        continue;
                    }
                    if (d > 0.0) {
                        arrayList.add(new TranslatableComponent("attribute.modifier.plus." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d2), new TranslatableComponent(((Attribute)entry.getKey()).getDescriptionId())).withStyle(ChatFormatting.BLUE));
                        continue;
                    }
                    if (!(d < 0.0)) continue;
                    arrayList.add(new TranslatableComponent("attribute.modifier.take." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d2 *= -1.0), new TranslatableComponent(((Attribute)entry.getKey()).getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }
        if (this.hasTag()) {
            if (ItemStack.shouldShowInTooltip(n2, TooltipPart.UNBREAKABLE) && this.tag.getBoolean(TAG_UNBREAKABLE)) {
                arrayList.add(new TranslatableComponent("item.unbreakable").withStyle(ChatFormatting.BLUE));
            }
            if (ItemStack.shouldShowInTooltip(n2, TooltipPart.CAN_DESTROY) && this.tag.contains(TAG_CAN_DESTROY_BLOCK_LIST, 9) && !((ListTag)(object3 = this.tag.getList(TAG_CAN_DESTROY_BLOCK_LIST, 8))).isEmpty()) {
                arrayList.add(TextComponent.EMPTY);
                arrayList.add(new TranslatableComponent("item.canBreak").withStyle(ChatFormatting.GRAY));
                for (int i = 0; i < ((ListTag)object3).size(); ++i) {
                    arrayList.addAll(ItemStack.expandBlockState(((ListTag)object3).getString(i)));
                }
            }
            if (ItemStack.shouldShowInTooltip(n2, TooltipPart.CAN_PLACE) && this.tag.contains(TAG_CAN_PLACE_ON_BLOCK_LIST, 9) && !((ListTag)(object3 = this.tag.getList(TAG_CAN_PLACE_ON_BLOCK_LIST, 8))).isEmpty()) {
                arrayList.add(TextComponent.EMPTY);
                arrayList.add(new TranslatableComponent("item.canPlace").withStyle(ChatFormatting.GRAY));
                for (int i = 0; i < ((ListTag)object3).size(); ++i) {
                    arrayList.addAll(ItemStack.expandBlockState(((ListTag)object3).getString(i)));
                }
            }
        }
        if (tooltipFlag.isAdvanced()) {
            if (this.isDamaged()) {
                arrayList.add(new TranslatableComponent("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
            }
            arrayList.add(new TextComponent(Registry.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (this.hasTag()) {
                arrayList.add(new TranslatableComponent("item.nbt_tags", this.tag.getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return arrayList;
    }

    private static boolean shouldShowInTooltip(int n, TooltipPart tooltipPart) {
        return (n & tooltipPart.getMask()) == 0;
    }

    private int getHideFlags() {
        if (this.hasTag() && this.tag.contains(TAG_HIDE_FLAGS, 99)) {
            return this.tag.getInt(TAG_HIDE_FLAGS);
        }
        return 0;
    }

    public void hideTooltipPart(TooltipPart tooltipPart) {
        CompoundTag compoundTag = this.getOrCreateTag();
        compoundTag.putInt(TAG_HIDE_FLAGS, compoundTag.getInt(TAG_HIDE_FLAGS) | tooltipPart.getMask());
    }

    public static void appendEnchantmentNames(List<Component> list, ListTag listTag) {
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            Registry.ENCHANTMENT.getOptional(EnchantmentHelper.getEnchantmentId(compoundTag)).ifPresent(enchantment -> list.add(enchantment.getFullname(EnchantmentHelper.getEnchantmentLevel(compoundTag))));
        }
    }

    private static Collection<Component> expandBlockState(String string) {
        try {
            List<Component> list;
            boolean bl;
            BlockStateParser blockStateParser = new BlockStateParser(new StringReader(string), true).parse(true);
            BlockState blockState = blockStateParser.getState();
            TagKey<Block> tagKey = blockStateParser.getTag();
            boolean bl2 = blockState != null;
            boolean bl3 = bl = tagKey != null;
            if (bl2) {
                return Lists.newArrayList((Object[])new Component[]{blockState.getBlock().getName().withStyle(ChatFormatting.DARK_GRAY)});
            }
            if (bl && !(list = Streams.stream(Registry.BLOCK.getTagOrEmpty(tagKey)).map(holder -> ((Block)holder.value()).getName()).map(mutableComponent -> mutableComponent.withStyle(ChatFormatting.DARK_GRAY)).collect(Collectors.toList())).isEmpty()) {
                return list;
            }
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return Lists.newArrayList((Object[])new Component[]{new TextComponent("missingno").withStyle(ChatFormatting.DARK_GRAY)});
    }

    public boolean hasFoil() {
        return this.getItem().isFoil(this);
    }

    public Rarity getRarity() {
        return this.getItem().getRarity(this);
    }

    public boolean isEnchantable() {
        if (!this.getItem().isEnchantable(this)) {
            return false;
        }
        return !this.isEnchanted();
    }

    public void enchant(Enchantment enchantment, int n) {
        this.getOrCreateTag();
        if (!this.tag.contains(TAG_ENCH, 9)) {
            this.tag.put(TAG_ENCH, new ListTag());
        }
        ListTag listTag = this.tag.getList(TAG_ENCH, 10);
        listTag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), (byte)n));
    }

    public boolean isEnchanted() {
        if (this.tag != null && this.tag.contains(TAG_ENCH, 9)) {
            return !this.tag.getList(TAG_ENCH, 10).isEmpty();
        }
        return false;
    }

    public void addTagElement(String string, Tag tag) {
        this.getOrCreateTag().put(string, tag);
    }

    public boolean isFramed() {
        return this.entityRepresentation instanceof ItemFrame;
    }

    public void setEntityRepresentation(@Nullable Entity entity) {
        this.entityRepresentation = entity;
    }

    @Nullable
    public ItemFrame getFrame() {
        return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
    }

    @Nullable
    public Entity getEntityRepresentation() {
        return !this.emptyCacheFlag ? this.entityRepresentation : null;
    }

    public int getBaseRepairCost() {
        if (this.hasTag() && this.tag.contains(TAG_REPAIR_COST, 3)) {
            return this.tag.getInt(TAG_REPAIR_COST);
        }
        return 0;
    }

    public void setRepairCost(int n) {
        this.getOrCreateTag().putInt(TAG_REPAIR_COST, n);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot) {
        HashMultimap hashMultimap;
        if (this.hasTag() && this.tag.contains("AttributeModifiers", 9)) {
            hashMultimap = HashMultimap.create();
            ListTag listTag = this.tag.getList("AttributeModifiers", 10);
            for (int i = 0; i < listTag.size(); ++i) {
                AttributeModifier attributeModifier;
                Optional<Attribute> optional;
                CompoundTag compoundTag = listTag.getCompound(i);
                if (compoundTag.contains("Slot", 8) && !compoundTag.getString("Slot").equals(equipmentSlot.getName()) || !(optional = Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(compoundTag.getString("AttributeName")))).isPresent() || (attributeModifier = AttributeModifier.load(compoundTag)) == null || attributeModifier.getId().getLeastSignificantBits() == 0L || attributeModifier.getId().getMostSignificantBits() == 0L) continue;
                hashMultimap.put((Object)optional.get(), (Object)attributeModifier);
            }
        } else {
            hashMultimap = this.getItem().getDefaultAttributeModifiers(equipmentSlot);
        }
        return hashMultimap;
    }

    public void addAttributeModifier(Attribute attribute, AttributeModifier attributeModifier, @Nullable EquipmentSlot equipmentSlot) {
        this.getOrCreateTag();
        if (!this.tag.contains("AttributeModifiers", 9)) {
            this.tag.put("AttributeModifiers", new ListTag());
        }
        ListTag listTag = this.tag.getList("AttributeModifiers", 10);
        CompoundTag compoundTag = attributeModifier.save();
        compoundTag.putString("AttributeName", Registry.ATTRIBUTE.getKey(attribute).toString());
        if (equipmentSlot != null) {
            compoundTag.putString("Slot", equipmentSlot.getName());
        }
        listTag.add(compoundTag);
    }

    public Component getDisplayName() {
        MutableComponent mutableComponent = new TextComponent("").append(this.getHoverName());
        if (this.hasCustomHoverName()) {
            mutableComponent.withStyle(ChatFormatting.ITALIC);
        }
        MutableComponent mutableComponent2 = ComponentUtils.wrapInSquareBrackets(mutableComponent);
        if (!this.emptyCacheFlag) {
            mutableComponent2.withStyle(this.getRarity().color).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this))));
        }
        return mutableComponent2;
    }

    public boolean hasAdventureModePlaceTagForBlock(Registry<Block> registry, BlockInWorld blockInWorld) {
        if (this.adventurePlaceCheck == null) {
            this.adventurePlaceCheck = new AdventureModeCheck(TAG_CAN_PLACE_ON_BLOCK_LIST);
        }
        return this.adventurePlaceCheck.test(this, registry, blockInWorld);
    }

    public boolean hasAdventureModeBreakTagForBlock(Registry<Block> registry, BlockInWorld blockInWorld) {
        if (this.adventureBreakCheck == null) {
            this.adventureBreakCheck = new AdventureModeCheck(TAG_CAN_DESTROY_BLOCK_LIST);
        }
        return this.adventureBreakCheck.test(this, registry, blockInWorld);
    }

    public int getPopTime() {
        return this.popTime;
    }

    public void setPopTime(int n) {
        this.popTime = n;
    }

    public int getCount() {
        return this.emptyCacheFlag ? 0 : this.count;
    }

    public void setCount(int n) {
        this.count = n;
        this.updateEmptyCacheFlag();
    }

    public void grow(int n) {
        this.setCount(this.count + n);
    }

    public void shrink(int n) {
        this.grow(-n);
    }

    public void onUseTick(Level level, LivingEntity livingEntity, int n) {
        this.getItem().onUseTick(level, livingEntity, this, n);
    }

    public void onDestroyed(ItemEntity itemEntity) {
        this.getItem().onDestroyed(itemEntity);
    }

    public boolean isEdible() {
        return this.getItem().isEdible();
    }

    public SoundEvent getDrinkingSound() {
        return this.getItem().getDrinkingSound();
    }

    public SoundEvent getEatingSound() {
        return this.getItem().getEatingSound();
    }

    @Nullable
    public SoundEvent getEquipSound() {
        return this.getItem().getEquipSound();
    }

    public static enum TooltipPart {
        ENCHANTMENTS,
        MODIFIERS,
        UNBREAKABLE,
        CAN_DESTROY,
        CAN_PLACE,
        ADDITIONAL,
        DYE;

        private final int mask = 1 << this.ordinal();

        public int getMask() {
            return this.mask;
        }
    }
}

