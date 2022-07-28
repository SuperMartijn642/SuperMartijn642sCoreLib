package com.supermartijn642.core.gui.widget;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Created 23/07/2022 by SuperMartijn642
 */
public abstract class ItemBaseContainerWidget<C extends Container> extends ObjectBaseContainerWidget<ItemStack,C> {

    protected final Supplier<ItemStack> stackSupplier;
    protected final Predicate<ItemStack> stackValidator;

    public ItemBaseContainerWidget(int x, int y, int width, int height, Supplier<ItemStack> stackSupplier, Predicate<ItemStack> stackValidator){
        super(x, y, width, height, true);
        this.stackSupplier = stackSupplier;
        this.stackValidator = stackValidator;
    }

    public ItemBaseContainerWidget(int x, int y, int width, int height, int slotIndex, Predicate<ItemStack> stackValidator){
        this(x, y, width, height, () -> ClientUtils.getPlayer().inventory.getItem(slotIndex), stackValidator);
    }

    public ItemBaseContainerWidget(int x, int y, int width, int height, int slotIndex, Item itemType){
        this(x, y, width, height, () -> ClientUtils.getPlayer().inventory.getItem(slotIndex), stack -> stack.getItem() == itemType);
    }

    public ItemBaseContainerWidget(int x, int y, int width, int height, Hand hand, Predicate<ItemStack> stackValidator){
        this(x, y, width, height, () -> ClientUtils.getPlayer().getItemInHand(hand), stackValidator);
    }

    public ItemBaseContainerWidget(int x, int y, int width, int height, Hand hand, Item itemType){
        this(x, y, width, height, () -> ClientUtils.getPlayer().getItemInHand(hand), stack -> stack.getItem() == itemType);
    }

    @Override
    protected ItemStack getObject(ItemStack oldObject){
        return this.stackSupplier.get();
    }

    @Override
    protected boolean validateObject(ItemStack object){
        return this.stackValidator.test(object);
    }
}
