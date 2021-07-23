package com.supermartijn642.core.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseContainerScreen<T extends ObjectBaseContainer<ItemStack>> extends ObjectBaseContainerScreen<ItemStack,T> {

    public ItemBaseContainerScreen(T screenContainer, Component title){
        super(screenContainer, title);
    }
}
