package com.supermartijn642.core.block;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class BaseBlock extends Block {

    public static final DataComponentType<CompoundTag> TILE_DATA = DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).build();

    private final boolean saveTileData;
    private final BlockProperties properties;

    private BaseBlock(boolean saveTileData, Properties properties, BlockProperties blockProperties){
        super(properties);
        this.saveTileData = saveTileData;
        this.properties = blockProperties;
        if(blockProperties != null && blockProperties.lootTable != null)
            this.drops = blockProperties.lootTable;
    }

    public BaseBlock(boolean saveTileData, Properties properties){
        this(saveTileData, properties, null);
    }

    public BaseBlock(boolean saveTileData, BlockProperties properties){
        this(saveTileData, properties.toUnderlying(), properties);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
        if(!this.saveTileData)
            return;

        if(!stack.has(TILE_DATA))
            return;
        CompoundTag tag = stack.get(TILE_DATA);
        if(tag == null || tag.isEmpty())
            return;

        BlockEntity entity = worldIn.getBlockEntity(pos);
        if(entity instanceof BaseBlockEntity)
            ((BaseBlockEntity)entity).readData(tag);
    }

    @Override
    public ResourceKey<LootTable> getLootTable(){
        if(this.drops == null){
            if(this.properties != null && this.properties.noLootTable)
                this.drops = BuiltInLootTables.EMPTY;
            else if(this.properties != null && this.properties.lootTable != null)
                this.drops = this.properties.lootTable;
            else if(this.properties != null && this.properties.lootTableBlock != null){
                Block block = this.properties.lootTableBlock.get();
                if(block == null){
                    CoreLib.LOGGER.warn("Received null block from BlockProperties#lootTableFrom's supplier for block '" + Registries.BLOCKS.getIdentifier(this) + "'!");
                    return super.getLootTable();
                }
                if(block.properties.drops == null)
                    return super.getLootTable();
                this.drops = block.properties.drops;
            }else
                return super.getLootTable();
        }
        return this.drops;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder){
        List<ItemStack> items = super.getDrops(state, builder);

        if(!this.saveTileData)
            return items;

        BlockEntity entity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if(!(entity instanceof BaseBlockEntity))
            return items;

        CompoundTag entityTag = ((BaseBlockEntity)entity).writeItemStackData();
        if(entityTag == null || entityTag.isEmpty())
            return items;

        for(ItemStack stack : items){
            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == this)
                stack.set(TILE_DATA, entityTag);
        }

        return items;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state){
        ItemStack stack = super.getCloneItemStack(world, pos, state);

        if(!this.saveTileData)
            return stack;

        BlockEntity entity = world.getBlockEntity(pos);
        if(!(entity instanceof BaseBlockEntity))
            return stack;

        CompoundTag entityTag = ((BaseBlockEntity)entity).writeItemStackData();
        if(entityTag == null || entityTag.isEmpty())
            return stack;

        if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == this){
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            stack.set(TILE_DATA, entityTag);
        }

        return stack;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult){
        return this.interact(state, level, pos, player, hand, hitResult.getDirection(), hitResult.getLocation()).interactionResult;
    }

    /**
     * Called when a player interacts with this block.
     * @return whether the player's interaction should be consumed or passed on
     */
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        return InteractionFeedback.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> information, TooltipFlag flag){
        this.appendItemInformation(stack, information::add, flag.isAdvanced());
        super.appendHoverText(stack, context, information, flag);
    }

    /**
     * Adds information to be displayed when hovering over the item corresponding to this block in the inventory.
     * @param stack    the stack being hovered over
     * @param info     consumes the information which should be added
     * @param advanced whether advanced tooltips is enabled
     */
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
    }

    /**
     * Gets the item corresponding to this block.
     */
    @Override
    public Item asItem(){
        return super.asItem();
    }

    @Override
    public String getDescriptionId(){
        ResourceLocation identifier = Registries.BLOCKS.getIdentifier(this);
        return identifier.getNamespace() + ".block." + identifier.getPath();
    }

    protected enum InteractionFeedback {
        PASS(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION),
        CONSUME(ItemInteractionResult.CONSUME),
        SUCCESS(ItemInteractionResult.SUCCESS);

        private final ItemInteractionResult interactionResult;

        InteractionFeedback(ItemInteractionResult interactionResult){
            this.interactionResult = interactionResult;
        }
    }
}
