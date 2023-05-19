package com.supermartijn642.core.item;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public final class CreativeItemGroup extends ItemGroup {

    public static CreativeItemGroup create(String modid, String name, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Item group name '" + name + "' must only contain characters [a-z0-9_.-]!");

        String identifier = modid + "." + name;
        String translationKey = modid + ".item_group." + name;
        return new CreativeItemGroup(identifier, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, String name, IItemProvider icon){
        return create(modid, name, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeItemGroup create(String modid, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");

        String translationKey = modid + ".item_group";
        return new CreativeItemGroup(modid, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, IItemProvider icon){
        return create(modid, () -> icon.asItem().getDefaultInstance());
    }

    public static ItemGroup getBuildingBlocks(){
        return ItemGroup.TAB_BUILDING_BLOCKS;
    }

    public static ItemGroup getDecoration(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getRedstone(){
        return ItemGroup.TAB_REDSTONE;
    }

    public static ItemGroup getTransportation(){
        return ItemGroup.TAB_TRANSPORTATION;
    }

    public static ItemGroup getMisc(){
        return ItemGroup.TAB_MISC;
    }

    public static ItemGroup getSearch(){
        return ItemGroup.TAB_SEARCH;
    }

    public static ItemGroup getFood(){
        return ItemGroup.TAB_FOOD;
    }

    public static ItemGroup getTools(){
        return ItemGroup.TAB_TOOLS;
    }

    public static ItemGroup getCombat(){
        return ItemGroup.TAB_COMBAT;
    }

    public static ItemGroup getBrewing(){
        return ItemGroup.TAB_BREWING;
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
        return this.sorter(Comparator.comparing(stack -> TextComponents.itemStack(stack).format()));
    }

    @Override
    public ItemStack makeIcon(){
        ItemStack stack = this.icon.get();
        if(stack == null || stack.isEmpty())
            throw new RuntimeException("Item group '" + this.identifier + "'s icon stack must not be empty!");
        return stack;
    }

    @Override
    public String getName(){
        return this.translationKey;
    }

    @Override
    public String getLangId(){
        return this.translationKey;
    }

    @Override
    public String getRecipeFolderName(){
        return this.identifier;
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items){
        // Fill the list with items
        if(this.filler == null)
            super.fillItemList(items);
        else
            this.filler.accept(items::add);
        // Sort the items
        if(this.sorter != null)
            items.sort(this.sorter);
    }
}
