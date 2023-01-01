package com.supermartijn642.core.item;

import com.supermartijn642.core.registry.Registries;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
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
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
    }

    /**
     * Called when a player right-clicks with this item.
     * @return whether the player's interaction should be consumed or passed on, together with the new item stack
     */
    public ItemUseResult interact(ItemStack stack, PlayerEntity player, Hand hand, World level){
        return ItemUseResult.fromUnderlying(super.use(level, player, hand));
    }

    /**
     * Called when a player right-clicks on a block with this item, before the block is interacted with.
     * @return whether the player's interaction should be consumed or passed on
     */
    public InteractionFeedback interactWithBlockFirst(ItemStack stack, PlayerEntity player, Hand hand, World level, BlockPos hitPos, Direction hitSide, Vec3d hitLocation){
        return InteractionFeedback.PASS;
    }

    /**
     * Called when a player right-clicks on a block with this item, after the block is interacted with.
     * @return whether the player's interaction should be consumed or passed on
     */
    public InteractionFeedback interactWithBlock(ItemStack stack, PlayerEntity player, Hand hand, World level, BlockPos hitPos, Direction hitSide, Vec3d hitLocation){
        return InteractionFeedback.PASS;
    }

    /**
     * Called when a player right-clicks on an entity.
     * @return whether the player's interaction should be consumed or passed on
     */
    public InteractionFeedback interactWithEntity(ItemStack stack, LivingEntity target, PlayerEntity player, Hand hand){
        return InteractionFeedback.PASS;
    }

    /**
     * Called once every tick when this item is in an entity's inventory.
     */
    public void inventoryUpdate(ItemStack stack, World level, Entity entity, int itemSlot, boolean isSelected){
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> information, ITooltipFlag flag){
        this.appendItemInformation(stack, level, information::add, flag.isAdvanced());
        super.appendHoverText(stack, level, information, flag);
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand){
        return this.interact(player.getItemInHand(hand), player, hand, level).toUnderlying();
    }

    @Override
    public boolean interactEnemy(ItemStack stack, PlayerEntity player, LivingEntity target, Hand hand){
        return this.interactWithEntity(stack, target, player, hand).consumesAction();
    }

    @Override
    public ActionResultType useOn(ItemUseContext context){
        return this.interactWithBlock(context.getItemInHand(), context.getPlayer(), context.getHand(), context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getClickLocation()).interactionResult;
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context){
        return this.interactWithBlockFirst(stack, context.getPlayer(), context.getHand(), context.getLevel(), context.getClickedPos(), context.getClickedFace(), context.getClickLocation()).interactionResult;
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, Entity entity, int slot, boolean isSelected){
        this.inventoryUpdate(stack, level, entity, slot, isSelected);
    }

    @Override
    protected String getOrCreateDescriptionId(){
        ResourceLocation identifier = Registries.ITEMS.getIdentifier(this);
        return identifier.getNamespace() + ".item." + identifier.getPath();
    }

    public boolean isInCreativeGroup(ItemGroup tab){
        return this.properties != null && this.properties.groups.contains(tab);
    }

    @Override
    public Collection<ItemGroup> getCreativeTabs(){
        return this.properties != null ? this.properties.groups : super.getCreativeTabs();
    }

    public boolean isFireResistant(){
        return this.properties != null && this.properties.isFireResistant;
    }

    public boolean canBeHurtBy(DamageSource source){
        return !this.isFireResistant() || !source.isFire();
    }

    protected static class ItemUseResult {

        public static ItemUseResult pass(ItemStack stack){
            return new ItemUseResult(ActionResultType.SUCCESS, stack);
        }

        public static ItemUseResult consume(ItemStack stack){
            return new ItemUseResult(ActionResultType.SUCCESS, stack);
        }

        public static ItemUseResult success(ItemStack stack){
            return new ItemUseResult(ActionResultType.SUCCESS, stack);
        }

        public static ItemUseResult fail(ItemStack stack){
            return new ItemUseResult(ActionResultType.FAIL, stack);
        }

        @Deprecated
        public static ItemUseResult fromUnderlying(ActionResult<ItemStack> underlying){
            return new ItemUseResult(underlying.getResult(), underlying.getObject());
        }

        private final ActionResultType result;
        private final ItemStack resultingStack;

        private ItemUseResult(ActionResultType result, ItemStack resultingStack){
            this.result = result;
            this.resultingStack = resultingStack;
        }

        @Deprecated
        public ActionResult<ItemStack> toUnderlying(){
            return new ActionResult<>(this.result, this.resultingStack);
        }
    }

    public enum InteractionFeedback {
        PASS(ActionResultType.PASS), CONSUME(ActionResultType.SUCCESS), SUCCESS(ActionResultType.SUCCESS);

        private final ActionResultType interactionResult;

        InteractionFeedback(ActionResultType interactionResult){
            this.interactionResult = interactionResult;
        }

        private boolean consumesAction(){
            return this == SUCCESS || this == CONSUME;
        }
    }
}
