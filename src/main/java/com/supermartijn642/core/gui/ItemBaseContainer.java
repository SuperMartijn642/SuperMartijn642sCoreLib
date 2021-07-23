package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseContainer extends ObjectBaseContainer<ItemStack> {

    private final Supplier<ItemStack> stackSupplier;

    private ItemBaseContainer(MenuType<?> type, int id, Player player, Supplier<ItemStack> itemStackSupplier){
        super(type, id, player);
        this.stackSupplier = itemStackSupplier;
    }

    protected ItemBaseContainer(MenuType<?> type, int id, Player player, int playerSlot){
        this(type, id, player, () -> player.getInventory().getItem(playerSlot));
    }

    protected ItemBaseContainer(MenuType<?> type, int id, Player player, InteractionHand hand){
        this(type, id, player, () -> ClientUtils.getPlayer().getItemInHand(hand));
    }

    @Override
    protected ItemStack getObject(){
        return this.stackSupplier.get();
    }
}
