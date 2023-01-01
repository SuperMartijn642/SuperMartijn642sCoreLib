package com.supermartijn642.core.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.IRarity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class ItemProperties {

    public static ItemProperties create(){
        return new ItemProperties();
    }

    int maxStackSize = 64;
    int durability;
    Item craftingRemainingItem;
    IRarity rarity = ItemRarity.COMMON.getUnderlying();
    boolean isFireResistant;
    final Set<CreativeTabs> groups = new HashSet<>();

    private ItemProperties(){
    }

    public ItemProperties maxStackSize(int maxStackSize){
        if(maxStackSize < 1)
            throw new IllegalArgumentException("Maximum stack size must be greater than zero!");
        if(maxStackSize > 1 && this.durability != 0)
            throw new RuntimeException("An item cannot have durability and be stackable!");

        this.maxStackSize = maxStackSize;
        return this;
    }

    public ItemProperties durability(int durability){
        if(this.maxStackSize != 64 && this.maxStackSize > 1)
            throw new RuntimeException("An item cannot have durability and be stackable!");

        this.durability = durability;
        this.maxStackSize = 1;
        return this;
    }

    public ItemProperties craftRemainder(Item item){
        this.craftingRemainingItem = item;
        return this;
    }

    public ItemProperties group(CreativeTabs group){
        this.groups.add(group);
        return this;
    }

    public ItemProperties rarity(IRarity rarity){
        this.rarity = rarity;
        return this;
    }

    public ItemProperties rarity(ItemRarity rarity){
        this.rarity = rarity.getUnderlying();
        return this;
    }

    public ItemProperties fireResistant(){
        this.isFireResistant = true;
        return this;
    }
}
