package com.supermartijn642.core.item;

import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class CreativeItemGroup extends CreativeTabs {

    public static CreativeItemGroup create(String modid, String name, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Item group name '" + name + "' must only contain characters [a-z0-9_.-]!");

        String identifier = modid + "." + name;
        String translationKey = modid + ".item_group." + name;
        return new CreativeItemGroup(identifier, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, String name, ItemSupplier icon){
        return create(modid, name, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeItemGroup create(String modid, String name, BlockSupplier icon){
        return create(modid, name, () -> Item.getItemFromBlock(icon.asBlock()));
    }

    public static CreativeItemGroup create(String modid, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");

        String translationKey = modid + ".item_group";
        return new CreativeItemGroup(modid, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, ItemSupplier icon){
        return create(modid, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeItemGroup create(String modid, BlockSupplier icon){
        return create(modid, () -> Item.getItemFromBlock(icon.asBlock()));
    }

    public static CreativeTabs getBuildingBlocks(){
        return CreativeTabs.BUILDING_BLOCKS;
    }

    public static CreativeTabs getDecoration(){
        return CreativeTabs.DECORATIONS;
    }

    public static CreativeTabs getRedstone(){
        return CreativeTabs.REDSTONE;
    }

    public static CreativeTabs getTransportation(){
        return CreativeTabs.TRANSPORTATION;
    }

    public static CreativeTabs getMisc(){
        return CreativeTabs.MISC;
    }

    public static CreativeTabs getSearch(){
        return CreativeTabs.SEARCH;
    }

    public static CreativeTabs getFood(){
        return CreativeTabs.FOOD;
    }

    public static CreativeTabs getTools(){
        return CreativeTabs.TOOLS;
    }

    public static CreativeTabs getCombat(){
        return CreativeTabs.COMBAT;
    }

    public static CreativeTabs getBrewing(){
        return CreativeTabs.BREWING;
    }

    private final String identifier;
    private final String translationKey;
    private final Supplier<ItemStack> icon;

    private CreativeItemGroup(String identifier, String translationKey, Supplier<ItemStack> icon){
        super(identifier);
        this.identifier = identifier;
        this.translationKey = translationKey;
        this.icon = icon;
    }

    @Override
    public ItemStack getTabIconItem(){
        ItemStack stack = this.icon.get();
        if(stack == null || stack.isEmpty())
            throw new RuntimeException("Item group '" + this.identifier + "'s icon stack must not be empty!");
        return stack;
    }

    @Override
    public String getTabLabel(){
        return this.identifier;
    }

    @Override
    public String getTranslatedTabLabel(){
        return this.translationKey;
    }

    @FunctionalInterface
    public interface ItemSupplier {

        Item asItem();
    }

    @FunctionalInterface
    public interface BlockSupplier {

        Block asBlock();
    }
}
