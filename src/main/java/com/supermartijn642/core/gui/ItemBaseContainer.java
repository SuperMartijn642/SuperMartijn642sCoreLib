package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseContainer extends ObjectBaseContainer<ItemStack> {

    private final Supplier<ItemStack> stackSupplier;
    protected final Predicate<ItemStack> stackValidator;

    private ItemBaseContainer(BaseContainerType<?> type, Player player, Supplier<ItemStack> itemStackSupplier, Predicate<ItemStack> stackValidator){
        super(type, player, true);
        this.stackSupplier = itemStackSupplier;
        this.stackValidator = stackValidator;
    }

    protected ItemBaseContainer(BaseContainerType<?> type, Player player, int playerSlot, Predicate<ItemStack> stackValidator){
        this(type, player, () -> player.getInventory().getItem(playerSlot), stackValidator);
    }

    protected ItemBaseContainer(BaseContainerType<?> type, Player player, InteractionHand hand, Predicate<ItemStack> stackValidator){
        this(type, player, () -> ClientUtils.getPlayer().getItemInHand(hand), stackValidator);
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
