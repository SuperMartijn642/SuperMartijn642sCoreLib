package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseContainer extends ObjectBaseContainer<ItemStack> {

    private final Supplier<ItemStack> stackSupplier;

    private ItemBaseContainer(ContainerType<?> type, int id, PlayerEntity player, Supplier<ItemStack> itemStackSupplier){
        super(type, id, player);
        this.stackSupplier = itemStackSupplier;
    }

    protected ItemBaseContainer(ContainerType<?> type, int id, PlayerEntity player, int playerSlot){
        this(type, id, player, () -> player.inventory.getItem(playerSlot));
    }

    protected ItemBaseContainer(ContainerType<?> type, int id, PlayerEntity player, Hand hand){
        this(type, id, player, () -> ClientUtils.getPlayer().getItemInHand(hand));
    }

    @Override
    protected ItemStack getObject(){
        return this.stackSupplier.get();
    }
}
