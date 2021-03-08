package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseContainer extends ObjectBaseContainer<ItemStack> {

    private final Supplier<ItemStack> stackSupplier;

    private ItemBaseContainer(EntityPlayer player, Supplier<ItemStack> itemStackSupplier){
        super(player);
        this.stackSupplier = itemStackSupplier;
    }

    protected ItemBaseContainer(EntityPlayer player, int playerSlot){
        this(player, () -> player.inventory.getStackInSlot(playerSlot));
    }

    protected ItemBaseContainer(EntityPlayer player, EnumHand hand){
        this(player, () -> ClientUtils.getPlayer().getHeldItem(hand));
    }

    @Override
    protected ItemStack getObject(){
        return this.stackSupplier.get();
    }
}
