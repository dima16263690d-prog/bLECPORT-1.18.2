/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.LinkedHashMultiset
 *  com.google.common.collect.Multiset
 *  com.google.common.collect.Multisets
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapItem
extends ComplexItem {
    public static final int IMAGE_WIDTH = 128;
    public static final int IMAGE_HEIGHT = 128;
    private static final int DEFAULT_MAP_COLOR = -12173266;
    private static final String TAG_MAP = "map";

    public MapItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack create(Level level, int n, int n2, byte by, boolean bl, boolean bl2) {
        ItemStack itemStack = new ItemStack(Items.FILLED_MAP);
        MapItem.createAndStoreSavedData(itemStack, level, n, n2, by, bl, bl2, level.dimension());
        return itemStack;
    }

    @Nullable
    public static MapItemSavedData getSavedData(@Nullable Integer n, Level level) {
        return n == null ? null : level.getMapData(MapItem.makeKey(n));
    }

    @Nullable
    public static MapItemSavedData getSavedData(ItemStack itemStack, Level level) {
        Integer n = MapItem.getMapId(itemStack);
        return MapItem.getSavedData(n, level);
    }

    @Nullable
    public static Integer getMapId(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && compoundTag.contains(TAG_MAP, 99) ? Integer.valueOf(compoundTag.getInt(TAG_MAP)) : null;
    }

    private static int createNewSavedData(Level level, int n, int n2, int n3, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        MapItemSavedData mapItemSavedData = MapItemSavedData.createFresh(n, n2, (byte)n3, bl, bl2, resourceKey);
        int n4 = level.getFreeMapId();
        level.setMapData(MapItem.makeKey(n4), mapItemSavedData);
        return n4;
    }

    private static void storeMapData(ItemStack itemStack, int n) {
        itemStack.getOrCreateTag().putInt(TAG_MAP, n);
    }

    private static void createAndStoreSavedData(ItemStack itemStack, Level level, int n, int n2, int n3, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        int n4 = MapItem.createNewSavedData(level, n, n2, n3, bl, bl2, resourceKey);
        MapItem.storeMapData(itemStack, n4);
    }

    public static String makeKey(int n) {
        return "map_" + n;
    }

    public void update(Level level, Entity entity, MapItemSavedData mapItemSavedData) {
        if (level.dimension() != mapItemSavedData.dimension || !(entity instanceof Player)) {
            return;
        }
        int n = 1 << mapItemSavedData.scale;
        int n2 = mapItemSavedData.x;
        int n3 = mapItemSavedData.z;
        int n4 = Mth.floor(entity.getX() - (double)n2) / n + 64;
        int n5 = Mth.floor(entity.getZ() - (double)n3) / n + 64;
        int n6 = 128 / n;
        if (level.dimensionType().hasCeiling()) {
            n6 /= 2;
        }
        MapItemSavedData.HoldingPlayer holdingPlayer = mapItemSavedData.getHoldingPlayer((Player)entity);
        ++holdingPlayer.step;
        boolean bl = false;
        for (int i = n4 - n6 + 1; i < n4 + n6; ++i) {
            if ((i & 0xF) != (holdingPlayer.step & 0xF) && !bl) continue;
            bl = false;
            double d = 0.0;
            for (int j = n5 - n6 - 1; j < n5 + n6; ++j) {
                double d2;
                Object object;
                Object object2;
                if (i < 0 || j < -1 || i >= 128 || j >= 128) continue;
                int n7 = i - n4;
                int n8 = j - n5;
                boolean bl2 = n7 * n7 + n8 * n8 > (n6 - 2) * (n6 - 2);
                int n9 = (n2 / n + i - 64) * n;
                int n10 = (n3 / n + j - 64) * n;
                LinkedHashMultiset linkedHashMultiset = LinkedHashMultiset.create();
                LevelChunk levelChunk = level.getChunkAt(new BlockPos(n9, 0, n10));
                if (levelChunk.isEmpty()) continue;
                ChunkPos chunkPos = levelChunk.getPos();
                int n11 = n9 & 0xF;
                int n12 = n10 & 0xF;
                int n13 = 0;
                double d3 = 0.0;
                if (level.dimensionType().hasCeiling()) {
                    int n14 = n9 + n10 * 231871;
                    if (((n14 = n14 * n14 * 31287121 + n14 * 11) >> 20 & 1) == 0) {
                        linkedHashMultiset.add((Object)Blocks.DIRT.defaultBlockState().getMapColor(level, BlockPos.ZERO), 10);
                    } else {
                        linkedHashMultiset.add((Object)Blocks.STONE.defaultBlockState().getMapColor(level, BlockPos.ZERO), 100);
                    }
                    d3 = 100.0;
                } else {
                    object2 = new BlockPos.MutableBlockPos();
                    object = new BlockPos.MutableBlockPos();
                    for (int k = 0; k < n; ++k) {
                        for (int i2 = 0; i2 < n; ++i2) {
                            BlockState blockState;
                            int n15 = levelChunk.getHeight(Heightmap.Types.WORLD_SURFACE, k + n11, i2 + n12) + 1;
                            if (n15 > level.getMinBuildHeight() + 1) {
                                do {
                                    ((BlockPos.MutableBlockPos)object2).set(chunkPos.getMinBlockX() + k + n11, --n15, chunkPos.getMinBlockZ() + i2 + n12);
                                } while ((blockState = levelChunk.getBlockState((BlockPos)object2)).getMapColor(level, (BlockPos)object2) == MaterialColor.NONE && n15 > level.getMinBuildHeight());
                                if (n15 > level.getMinBuildHeight() && !blockState.getFluidState().isEmpty()) {
                                    BlockState blockState2;
                                    int n16 = n15 - 1;
                                    object.set((Vec3i)object2);
                                    do {
                                        object.setY(n16--);
                                        blockState2 = levelChunk.getBlockState((BlockPos)object);
                                        ++n13;
                                    } while (n16 > level.getMinBuildHeight() && !blockState2.getFluidState().isEmpty());
                                    blockState = this.getCorrectStateForFluidBlock(level, blockState, (BlockPos)object2);
                                }
                            } else {
                                blockState = Blocks.BEDROCK.defaultBlockState();
                            }
                            mapItemSavedData.checkBanners(level, chunkPos.getMinBlockX() + k + n11, chunkPos.getMinBlockZ() + i2 + n12);
                            d3 += (double)n15 / (double)(n * n);
                            linkedHashMultiset.add((Object)blockState.getMapColor(level, (BlockPos)object2));
                        }
                    }
                }
                object2 = (MaterialColor)Iterables.getFirst((Iterable)Multisets.copyHighestCountFirst((Multiset)linkedHashMultiset), (Object)MaterialColor.NONE);
                object = object2 == MaterialColor.WATER ? ((d2 = (double)(n13 /= n * n) * 0.1 + (double)(i + j & 1) * 0.2) < 0.5 ? MaterialColor.Brightness.HIGH : (d2 > 0.9 ? MaterialColor.Brightness.LOW : MaterialColor.Brightness.NORMAL)) : ((d2 = (d3 - d) * 4.0 / (double)(n + 4) + ((double)(i + j & 1) - 0.5) * 0.4) > 0.6 ? MaterialColor.Brightness.HIGH : (d2 < -0.6 ? MaterialColor.Brightness.LOW : MaterialColor.Brightness.NORMAL));
                d = d3;
                if (j < 0 || n7 * n7 + n8 * n8 >= n6 * n6 || bl2 && (i + j & 1) == 0) continue;
                bl |= mapItemSavedData.updateColor(i, j, ((MaterialColor)object2).getPackedId((MaterialColor.Brightness)((Object)object)));
            }
        }
    }

    private BlockState getCorrectStateForFluidBlock(Level level, BlockState blockState, BlockPos blockPos) {
        FluidState fluidState = blockState.getFluidState();
        if (!fluidState.isEmpty() && !blockState.isFaceSturdy(level, blockPos, Direction.UP)) {
            return fluidState.createLegacyBlock();
        }
        return blockState;
    }

    private static boolean isBiomeWatery(boolean[] blArray, int n, int n2) {
        return blArray[n2 * 128 + n];
    }

    public static void renderBiomePreviewMap(ServerLevel serverLevel, ItemStack itemStack) {
        int n;
        int n2;
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, (Level)serverLevel);
        if (mapItemSavedData == null) {
            return;
        }
        if (serverLevel.dimension() != mapItemSavedData.dimension) {
            return;
        }
        int n3 = 1 << mapItemSavedData.scale;
        int n4 = mapItemSavedData.x;
        int n5 = mapItemSavedData.z;
        boolean[] blArray = new boolean[16384];
        int n6 = n4 / n3 - 64;
        int n7 = n5 / n3 - 64;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (n2 = 0; n2 < 128; ++n2) {
            for (n = 0; n < 128; ++n) {
                Biome.BiomeCategory biomeCategory = Biome.getBiomeCategory(serverLevel.getBiome(mutableBlockPos.set((n6 + n) * n3, 0, (n7 + n2) * n3)));
                blArray[n2 * 128 + n] = biomeCategory == Biome.BiomeCategory.OCEAN || biomeCategory == Biome.BiomeCategory.RIVER || biomeCategory == Biome.BiomeCategory.SWAMP;
            }
        }
        for (n2 = 1; n2 < 127; ++n2) {
            for (n = 1; n < 127; ++n) {
                int n8 = 0;
                for (int i = -1; i < 2; ++i) {
                    for (int j = -1; j < 2; ++j) {
                        if (i == 0 && j == 0 || !MapItem.isBiomeWatery(blArray, n2 + i, n + j)) continue;
                        ++n8;
                    }
                }
                MaterialColor.Brightness brightness = MaterialColor.Brightness.LOWEST;
                MaterialColor materialColor = MaterialColor.NONE;
                if (MapItem.isBiomeWatery(blArray, n2, n)) {
                    materialColor = MaterialColor.COLOR_ORANGE;
                    if (n8 > 7 && n % 2 == 0) {
                        switch ((n2 + (int)(Mth.sin((float)n + 0.0f) * 7.0f)) / 8 % 5) {
                            case 0: 
                            case 4: {
                                brightness = MaterialColor.Brightness.LOW;
                                break;
                            }
                            case 1: 
                            case 3: {
                                brightness = MaterialColor.Brightness.NORMAL;
                                break;
                            }
                            case 2: {
                                brightness = MaterialColor.Brightness.HIGH;
                            }
                        }
                    } else if (n8 > 7) {
                        materialColor = MaterialColor.NONE;
                    } else if (n8 > 5) {
                        brightness = MaterialColor.Brightness.NORMAL;
                    } else if (n8 > 3) {
                        brightness = MaterialColor.Brightness.LOW;
                    } else if (n8 > 1) {
                        brightness = MaterialColor.Brightness.LOW;
                    }
                } else if (n8 > 0) {
                    materialColor = MaterialColor.COLOR_BROWN;
                    brightness = n8 > 3 ? MaterialColor.Brightness.NORMAL : MaterialColor.Brightness.LOWEST;
                }
                if (materialColor == MaterialColor.NONE) continue;
                mapItemSavedData.setColor(n2, n, materialColor.getPackedId(brightness));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int n, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData == null) {
            return;
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            mapItemSavedData.tickCarriedBy(player, itemStack);
        }
        if (!mapItemSavedData.locked && (bl || entity instanceof Player && ((Player)entity).getOffhandItem() == itemStack)) {
            this.update(level, entity, mapItemSavedData);
        }
    }

    @Override
    @Nullable
    public Packet<?> getUpdatePacket(ItemStack itemStack, Level level, Player player) {
        Integer n = MapItem.getMapId(itemStack);
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(n, level);
        if (mapItemSavedData != null) {
            return mapItemSavedData.getUpdatePacket(n, player);
        }
        return null;
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
        CompoundTag compoundTag = itemStack.getTag();
        if (compoundTag != null && compoundTag.contains("map_scale_direction", 99)) {
            MapItem.scaleMap(itemStack, level, compoundTag.getInt("map_scale_direction"));
            compoundTag.remove("map_scale_direction");
        } else if (compoundTag != null && compoundTag.contains("map_to_lock", 1) && compoundTag.getBoolean("map_to_lock")) {
            MapItem.lockMap(level, itemStack);
            compoundTag.remove("map_to_lock");
        }
    }

    private static void scaleMap(ItemStack itemStack, Level level, int n) {
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData != null) {
            int n2 = level.getFreeMapId();
            level.setMapData(MapItem.makeKey(n2), mapItemSavedData.scaled(n));
            MapItem.storeMapData(itemStack, n2);
        }
    }

    public static void lockMap(Level level, ItemStack itemStack) {
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, level);
        if (mapItemSavedData != null) {
            int n = level.getFreeMapId();
            String string = MapItem.makeKey(n);
            MapItemSavedData mapItemSavedData2 = mapItemSavedData.locked();
            level.setMapData(string, mapItemSavedData2);
            MapItem.storeMapData(itemStack, n);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        MapItemSavedData mapItemSavedData;
        Integer n = MapItem.getMapId(itemStack);
        MapItemSavedData mapItemSavedData2 = mapItemSavedData = level == null ? null : MapItem.getSavedData(n, level);
        if (mapItemSavedData != null && mapItemSavedData.locked) {
            list.add(new TranslatableComponent("filled_map.locked", n).withStyle(ChatFormatting.GRAY));
        }
        if (tooltipFlag.isAdvanced()) {
            if (mapItemSavedData != null) {
                list.add(new TranslatableComponent("filled_map.id", n).withStyle(ChatFormatting.GRAY));
                list.add(new TranslatableComponent("filled_map.scale", 1 << mapItemSavedData.scale).withStyle(ChatFormatting.GRAY));
                list.add(new TranslatableComponent("filled_map.level", mapItemSavedData.scale, 4).withStyle(ChatFormatting.GRAY));
            } else {
                list.add(new TranslatableComponent("filled_map.unknown").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement("display");
        if (compoundTag != null && compoundTag.contains("MapColor", 99)) {
            int n = compoundTag.getInt("MapColor");
            return 0xFF000000 | n & 0xFFFFFF;
        }
        return -12173266;
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        BlockState blockState = useOnContext.getLevel().getBlockState(useOnContext.getClickedPos());
        if (blockState.is(BlockTags.BANNERS)) {
            MapItemSavedData mapItemSavedData;
            if (!useOnContext.getLevel().isClientSide && (mapItemSavedData = MapItem.getSavedData(useOnContext.getItemInHand(), useOnContext.getLevel())) != null && !mapItemSavedData.toggleBanner(useOnContext.getLevel(), useOnContext.getClickedPos())) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.sidedSuccess(useOnContext.getLevel().isClientSide);
        }
        return super.useOn(useOnContext);
    }
}

