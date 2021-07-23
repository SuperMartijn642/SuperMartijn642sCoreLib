package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseScreen extends ObjectBaseScreen<ItemStack> {

    private final Supplier<ItemStack> stackSupplier;

    private ItemBaseScreen(Component title, Supplier<ItemStack> itemStackSupplier){
        super(title);
        this.stackSupplier = itemStackSupplier;
    }

    protected ItemBaseScreen(Component title, int playerSlot){
        this(title, () -> ClientUtils.getPlayer().getInventory().getItem(playerSlot));
    }

    protected ItemBaseScreen(Component title, InteractionHand hand){
        this(title, () -> ClientUtils.getPlayer().getItemInHand(hand));
    }

    @Override
    protected ItemStack getObject(){
        return this.stackSupplier.get();
    }
}
