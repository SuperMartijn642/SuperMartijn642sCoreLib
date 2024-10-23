package com.supermartijn642.core.item;

import com.supermartijn642.core.registry.Registries;
import net.minecraft.client.resources.model.MissingBlockModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BaseBlockItem extends BlockItem {

    private final ItemProperties properties;
    private boolean resolvedRegistryDependencies;

    public BaseBlockItem(Block block, Properties properties){
        super(block, removeDescriptionAndModelFromProperties(properties));
        this.properties = null;
    }

    public BaseBlockItem(Block block, ItemProperties properties){
        super(block, removeDescriptionAndModelFromProperties(properties.toUnderlying()));
        this.properties = properties;
    }

    private static Properties removeDescriptionAndModelFromProperties(Properties properties){
        return properties.overrideDescription("").overrideModel(MissingBlockModel.LOCATION).setId(ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.fromNamespaceAndPath("supermartijn642corelib", "dummy")));
    }

    @ApiStatus.Internal
    public void resolveRegistryDependencies(){
        if(!this.resolvedRegistryDependencies){
            this.descriptionId = this.getBlock().getDescriptionId();
            ResourceLocation identifier = Registries.ITEMS.getIdentifier(this);
            this.components = DataComponentMap.builder().addAll(this.components)
                .set(DataComponents.ITEM_NAME, Component.translatable(this.descriptionId))
                .set(DataComponents.ITEM_MODEL, identifier)
                .build();
            this.resolvedRegistryDependencies = true;
        }
    }

    /**
     * Adds information to be displayed when hovering over this item in the inventory.
     * @param stack    the stack being hovered over
     * @param info     consumes the information which should be added
     * @param advanced whether advanced tooltips is enabled
     */
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
    }

    /**
     * Called when a player right-clicks with this item.
     * @return whether the player's interaction should be consumed or passed on, together with the new item stack
     */
    public ItemUseResult interact(ItemStack stack, Player player, InteractionHand hand, Level level){
        return ItemUseResult.fromUnderlying(super.use(level, player, hand));
    }

    /**
     * Called when a player right-clicks on a block with this item, before the block is interacted with.
     * @return whether the player's interaction should be consumed or passed on
     */
    public InteractionFeedback interactWithBlockFirst(ItemStack stack, Player player, InteractionHand hand, Level level, BlockPos hitPos, Direction hitSide, Vec3 hitLocation){
        return InteractionFeedback.PASS;
    }

    /**
     * Called when a player right-clicks on a block with this item, after the block is interacted with.
     * @return whether the player's interaction should be consumed or passed on
     */
    public InteractionFeedback interactWithBlock(ItemStack stack, Player player, InteractionHand hand, Level level, BlockPos hitPos, Direction hitSide, Vec3 hitLocation){
        return InteractionFeedback.fromUnderlying(super.useOn(new UseOnContext(player, hand, new BlockHitResult(hitLocation, hitSide, hitPos, false))));
    }

    /**
     * Called when a player right-clicks on an entity.
     * @return whether the player's interaction should be consumed or passed on
     */
    public InteractionFeedback interactWithEntity(ItemStack stack, LivingEntity target, Player player, InteractionHand hand){
        return InteractionFeedback.PASS;
    }

    /**
     * Called once every tick when this item is in an entity's inventory.
     */
    public void inventoryUpdate(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected){
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> information, TooltipFlag flag){
        this.appendItemInformation(stack, information::add, flag.isAdvanced());
        super.appendHoverText(stack, context, information, flag);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand){
        return this.interact(player.getItemInHand(hand), player, hand, level).toUnderlying(level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand){
        return this.interactWithEntity(stack, target, player, hand).interactionResult;
    }

    @Override
    public InteractionResult useOn(UseOnContext context){
        return this.interactWithBlock(context.getItemInHand(), context.getPlayer(), context.getHand(), context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getClickLocation()).interactionResult;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected){
        this.inventoryUpdate(stack, level, entity, slot, isSelected);
    }

    @Override
    public String getDescriptionId(){
        this.resolveRegistryDependencies();
        return super.getDescriptionId();
    }

    @Override
    public DataComponentMap components(){
        this.resolveRegistryDependencies();
        return super.components();
    }

    @Override
    public Component getName(ItemStack itemStack){
        this.resolveRegistryDependencies();
        return super.getName(itemStack);
    }

    public boolean isInCreativeGroup(CreativeModeTab tab){
        return this.properties != null && this.properties.groups.contains(tab);
    }

    public static class ItemUseResult {

        public static ItemUseResult pass(ItemStack stack){
            return new ItemUseResult(InteractionResult.PASS);
        }

        public static ItemUseResult consume(ItemStack stack){
            return new ItemUseResult(InteractionResult.CONSUME.heldItemTransformedTo(stack));
        }

        public static ItemUseResult success(ItemStack stack){
            return new ItemUseResult(InteractionResult.SUCCESS.heldItemTransformedTo(stack));
        }

        public static ItemUseResult fail(ItemStack stack){
            return new ItemUseResult(InteractionResult.FAIL);
        }

        @Deprecated
        public static ItemUseResult fromUnderlying(InteractionResult underlying){
            return new ItemUseResult(underlying);
        }

        private final InteractionResult result;

        private ItemUseResult(InteractionResult result){
            this.result = result;
        }

        @Deprecated
        public InteractionResult toUnderlying(boolean isClientSide){
            if(!isClientSide && this.result instanceof InteractionResult.Success
                && ((InteractionResult.Success)this.result).swingSource() == InteractionResult.SwingSource.CLIENT){
                return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(((InteractionResult.Success)this.result).itemContext().heldItemTransformedTo());
            }
            return this.result;
        }
    }

    public enum InteractionFeedback {
        PASS(InteractionResult.PASS), CONSUME(InteractionResult.CONSUME), SUCCESS(InteractionResult.SUCCESS);

        private final InteractionResult interactionResult;

        InteractionFeedback(InteractionResult interactionResult){
            this.interactionResult = interactionResult;
        }

        @Deprecated
        public static InteractionFeedback fromUnderlying(InteractionResult interactionResult){
            if(interactionResult instanceof InteractionResult.Success){
                if(((InteractionResult.Success)interactionResult).swingSource() == InteractionResult.SwingSource.NONE)
                    return CONSUME;
                return SUCCESS;
            }
            if(interactionResult instanceof InteractionResult.Pass)
                return PASS;
            return CONSUME;
        }

        @Deprecated
        public InteractionResult getUnderlying(){
            return this.interactionResult;
        }
    }
}
