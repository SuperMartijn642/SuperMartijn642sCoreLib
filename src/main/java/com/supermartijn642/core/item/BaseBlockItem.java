package com.supermartijn642.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BaseBlockItem extends BlockItem {

    public BaseBlockItem(Block block, Properties properties){
        super(block, properties);
    }

    public BaseBlockItem(Block block, ItemProperties properties){
        this(block, properties.toUnderlying());
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
    protected ItemUseResult interact(ItemStack stack, Player player, InteractionHand hand, Level level){
        return ItemUseResult.fromUnderlying(super.use(level, player, hand));
    }

    /**
     * Called when a player right-clicks on an entity.
     * @return whether the player's interaction should be consumed or passed on
     */
    protected InteractionFeedback interactWithEntity(ItemStack stack, LivingEntity target, Player player, InteractionHand hand){
        return InteractionFeedback.PASS;
    }

    /**
     * Called once every tick when this item is in an entity's inventory.
     */
    protected void inventoryUpdate(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected){
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected){
        this.inventoryUpdate(stack, level, entity, slot, isSelected);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer){
        consumer.accept(new EditableClientItemExtensions());
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

    protected enum InteractionFeedback {
        PASS(InteractionResult.PASS), CONSUME(InteractionResult.CONSUME), SUCCESS(InteractionResult.SUCCESS);

        private final InteractionResult interactionResult;

        InteractionFeedback(InteractionResult interactionResult){
            this.interactionResult = interactionResult;
        }
    }
}
