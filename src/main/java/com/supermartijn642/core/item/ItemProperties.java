package com.supermartijn642.core.item;

import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class ItemProperties {

    public static ItemProperties create(){
        return new ItemProperties();
    }

    private int maxStackSize = 64;
    private int durability;
    private Item craftingRemainingItem;
    private Rarity rarity = Rarity.COMMON;
    private Food foodProperties;
    boolean isFireResistant;
    final Set<ItemGroup> groups = new HashSet<>();

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

    public ItemProperties group(ItemGroup group){
        this.groups.add(group);
        return this;
    }

    public ItemProperties rarity(Rarity rarity){
        this.rarity = rarity;
        return this;
    }

    public ItemProperties rarity(ItemRarity rarity){
        this.rarity = rarity.getUnderlying();
        return this;
    }

    public ItemProperties food(Food foodProperties){
        this.foodProperties = foodProperties;
        return this;
    }

    public ItemProperties fireResistant(){
        this.isFireResistant = true;
        return this;
    }

    /**
     * Converts the properties into {@link Item.Properties}.
     */
    @Deprecated
    public Item.Properties toUnderlying(){
        Item.Properties properties = new Item.Properties();
        properties.stacksTo(this.maxStackSize);
        properties.durability(this.durability);
        properties.craftRemainder(this.craftingRemainingItem);
        properties.rarity(this.rarity);
        properties.food(this.foodProperties);
        if(!this.groups.isEmpty())
            properties.tab(this.groups.iterator().next());
        return properties;
    }
}
