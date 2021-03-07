package com.supermartijn642.core.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ItemBaseContainerScreen<T extends ObjectBaseContainer<ItemStack>> extends ObjectBaseContainerScreen<ItemStack,T> {
    public ItemBaseContainerScreen(T screenContainer, ITextComponent title){
        super(screenContainer, title);
    }
}
