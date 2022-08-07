package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseContainer extends ObjectBaseContainer<ItemStack> {

    private final Supplier<ItemStack> stackSupplier;
    protected final Predicate<ItemStack> stackValidator;

    private ItemBaseContainer(EntityPlayer player, Supplier<ItemStack> itemStackSupplier, Predicate<ItemStack> stackValidator){
        super(player, true);
        this.stackSupplier = itemStackSupplier;
        this.stackValidator = stackValidator;
    }

    protected ItemBaseContainer(EntityPlayer player, int playerSlot, Predicate<ItemStack> stackValidator){
        this(player, () -> player.inventory.getStackInSlot(playerSlot), stackValidator);
    }

    protected ItemBaseContainer(EntityPlayer player, EnumHand hand, Predicate<ItemStack> stackValidator){
        this(player, () -> ClientUtils.getPlayer().getHeldItem(hand), stackValidator);
    }

    @Override
    protected ItemStack getObject(ItemStack oldObject){
        return this.stackSupplier.get();
    }

    @Override
    protected boolean validateObject(ItemStack object){
        return object != null && this.stackValidator.test(object);
    }
}
