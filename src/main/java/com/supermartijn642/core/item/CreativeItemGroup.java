package com.supermartijn642.core.item;

import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public final class CreativeItemGroup extends CreativeTabs {

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
    private Consumer<Consumer<ItemStack>> filler;
    private Comparator<ItemStack> sorter;

    private CreativeItemGroup(String identifier, String translationKey, Supplier<ItemStack> icon){
        super(identifier);
        this.identifier = identifier;
        this.translationKey = translationKey;
        this.icon = icon;
    }

    /**
     * Sets a custom filler for this creative tab. By default, the creative will be filled by items with this tab set in their properties.
     * @param filler a functions which pushes items to the given consumer
     */
    public CreativeItemGroup filler(Consumer<Consumer<ItemStack>> filler){
        this.filler = filler;
        return this;
    }

    /**
     * Sets a sorter for the items in this creative tab.
     * @param sorter compares two item stacks
     */
    public CreativeItemGroup sorter(Comparator<ItemStack> sorter){
        this.sorter = sorter;
        return this;
    }

    /**
     * Set the sorter to sort items alphabetically based on their display name.
     */
    public CreativeItemGroup sortAlphabetically(){
        return this.sorter(Comparator.comparing(ItemStack::getDisplayName));
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

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> items){
        // Fill the list with items
        if(this.filler == null)
            super.displayAllRelevantItems(items);
        else
            this.filler.accept(items::add);
        // Sort the items
        if(this.sorter != null)
            items.sort(this.sorter);
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
