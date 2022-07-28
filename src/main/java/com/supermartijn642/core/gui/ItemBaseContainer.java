package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseContainer extends ObjectBaseContainer<ItemStack> {

    private final Supplier<ItemStack> stackSupplier;
    protected final Predicate<ItemStack> stackValidator;

    private ItemBaseContainer(ContainerType<?> type, int id, PlayerEntity player, Supplier<ItemStack> itemStackSupplier, Predicate<ItemStack> stackValidator){
        super(type, id, player);
        this.stackSupplier = itemStackSupplier;
        this.stackValidator = stackValidator;
    }

    protected ItemBaseContainer(ContainerType<?> type, int id, PlayerEntity player, int playerSlot, Predicate<ItemStack> stackValidator){
        this(type, id, player, () -> player.inventory.getItem(playerSlot), stackValidator);
    }

    protected ItemBaseContainer(ContainerType<?> type, int id, PlayerEntity player, Hand hand, Predicate<ItemStack> stackValidator){
        this(type, id, player, () -> ClientUtils.getPlayer().getItemInHand(hand), stackValidator);
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
