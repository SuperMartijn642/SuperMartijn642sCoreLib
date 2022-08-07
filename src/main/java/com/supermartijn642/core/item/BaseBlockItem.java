package com.supermartijn642.core.item;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BaseBlockItem extends ItemBlock {

    private final ItemProperties properties;

    public BaseBlockItem(Block block, ItemProperties properties){
        super(block);
        this.properties = properties;
    }

    /**
     * Adds information to be displayed when hovering over this item in the inventory.
     * @param stack    the stack being hovered over
     * @param level    the world the player is in, may be {@code null}
     * @param info     consumes the information which should be added
     * @param advanced whether advanced tooltips is enabled
     */
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
    }

    /**
     * Called when a player right-clicks with this item.
     * @return whether the player's interaction should be consumed or passed on, together with the new item stack
     */
    public ItemUseResult interact(ItemStack stack, EntityPlayer player, EnumHand hand, World level){
        return ItemUseResult.fromUnderlying(super.onItemRightClick(level, player, hand));
    }

    /**
     * Called when a player right-clicks on an entity.
     * @return whether the player's interaction should be consumed or passed on
     */
    public InteractionFeedback interactWithEntity(ItemStack stack, EntityLivingBase target, EntityPlayer player, EnumHand hand){
        return InteractionFeedback.PASS;
    }

    /**
     * Called once every tick when this item is in an entity's inventory.
     */
    public void inventoryUpdate(ItemStack stack, World level, Entity entity, int itemSlot, boolean isSelected){
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World level, List<String> information, ITooltipFlag flag){
        this.appendItemInformation(stack, level, component -> information.add(component.getFormattedText()), flag.isAdvanced());
        super.addInformation(stack, level, information, flag);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World level, EntityPlayer player, EnumHand hand){
        return this.interact(player.getHeldItem(hand), player, hand, level).toUnderlying();
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand){
        return this.interactWithEntity(stack, target, player, hand).consumesAction();
    }

    @Override
    public void onUpdate(ItemStack stack, World level, Entity entity, int slot, boolean isSelected){
        this.inventoryUpdate(stack, level, entity, slot, isSelected);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack){
        return I18n.format(this.getUnlocalizedName(stack)).trim();
    }

    protected static class ItemUseResult {

        public static ItemUseResult pass(ItemStack stack){
            return new ItemUseResult(EnumActionResult.SUCCESS, stack);
        }

        public static ItemUseResult consume(ItemStack stack){
            return new ItemUseResult(EnumActionResult.SUCCESS, stack);
        }

        public static ItemUseResult success(ItemStack stack){
            return new ItemUseResult(EnumActionResult.SUCCESS, stack);
        }

        public static ItemUseResult fail(ItemStack stack){
            return new ItemUseResult(EnumActionResult.FAIL, stack);
        }

        @Deprecated
        public static ItemUseResult fromUnderlying(ActionResult<ItemStack> underlying){
            return new ItemUseResult(underlying.getType(), underlying.getResult());
        }

        private final EnumActionResult result;
        private final ItemStack resultingStack;

        private ItemUseResult(EnumActionResult result, ItemStack resultingStack){
            this.result = result;
            this.resultingStack = resultingStack;
        }

        @Deprecated
        public ActionResult<ItemStack> toUnderlying(){
            return new ActionResult<>(this.result, this.resultingStack);
        }
    }

    public enum InteractionFeedback {
        PASS(EnumActionResult.PASS), CONSUME(EnumActionResult.SUCCESS), SUCCESS(EnumActionResult.SUCCESS);

        private final EnumActionResult interactionResult;

        InteractionFeedback(EnumActionResult interactionResult){
            this.interactionResult = interactionResult;
        }

        private boolean consumesAction(){
            return this == SUCCESS || this == CONSUME;
        }
    }
}
