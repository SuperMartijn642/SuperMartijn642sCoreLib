package com.supermartijn642.core.item;

import com.supermartijn642.core.registry.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BaseItem extends Item {

    private final ItemProperties properties;

    public BaseItem(Properties properties){
        super(properties);
        this.properties = null;
    }

    public BaseItem(ItemProperties properties){
        super(properties.toUnderlying());
        this.properties = properties;
    }

    /**
     * Adds information to be displayed when hovering over this item in the inventory.
     * @param stack    the stack being hovered over
     * @param level    the world the player is in, may be {@code null}
     * @param info     consumes the information which should be added
     * @param advanced whether advanced tooltips is enabled
     */
    protected void appendItemInformation(ItemStack stack, @Nullable BlockGetter level, Consumer<Component> info, boolean advanced){
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
        return InteractionFeedback.PASS;
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> information, TooltipFlag flag){
        this.appendItemInformation(stack, level, information::add, flag.isAdvanced());
        super.appendHoverText(stack, level, information, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
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
    protected String getOrCreateDescriptionId(){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(this);
        return identifier.getNamespace() + ".item." + identifier.getPath();
    }

    public boolean isInCreativeGroup(CreativeModeTab tab){
        return this.properties != null && this.properties.groups.contains(tab);
    }

    protected static class ItemUseResult {

        public static ItemUseResult pass(ItemStack stack){
            return new ItemUseResult(InteractionResult.SUCCESS, stack);
        }

        public static ItemUseResult consume(ItemStack stack){
            return new ItemUseResult(InteractionResult.CONSUME, stack);
        }

        public static ItemUseResult success(ItemStack stack){
            return new ItemUseResult(InteractionResult.SUCCESS, stack);
        }

        public static ItemUseResult fail(ItemStack stack){
            return new ItemUseResult(InteractionResult.FAIL, stack);
        }

        @Deprecated
        public static ItemUseResult fromUnderlying(InteractionResultHolder<ItemStack> underlying){
            return new ItemUseResult(underlying.getResult(), underlying.getObject());
        }

        private final InteractionResult result;
        private final ItemStack resultingStack;

        private ItemUseResult(InteractionResult result, ItemStack resultingStack){
            this.result = result;
            this.resultingStack = resultingStack;
        }

        @Deprecated
        public InteractionResultHolder<ItemStack> toUnderlying(boolean isClientSide){
            return new InteractionResultHolder<>(this.result == InteractionResult.SUCCESS ? isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME : this.result, this.resultingStack);
        }
    }

    public enum InteractionFeedback {
        PASS(InteractionResult.PASS), CONSUME(InteractionResult.CONSUME), SUCCESS(InteractionResult.SUCCESS);

        private final InteractionResult interactionResult;

        InteractionFeedback(InteractionResult interactionResult){
            this.interactionResult = interactionResult;
        }

        @Deprecated
        public InteractionResult getUnderlying(){
            return this.interactionResult;
        }
    }
}
