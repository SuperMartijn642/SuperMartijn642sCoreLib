package com.supermartijn642.core.item;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.RegistryUtil;
import net.fabricmc.fabric.impl.item.group.ItemGroupExtensions;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public final class CreativeItemGroup extends CreativeModeTab {

    public static CreativeItemGroup create(String modid, String name, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Item group name '" + name + "' must only contain characters [a-z0-9_.-]!");

        ((ItemGroupExtensions)CreativeModeTab.TAB_BUILDING_BLOCKS).fabric_expandArray();

        String identifier = modid + "." + name;
        String translationKey = modid + ".item_group." + name;
        return new CreativeItemGroup(identifier, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, String name, ItemLike icon){
        return create(modid, name, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeItemGroup create(String modid, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");

        ((ItemGroupExtensions)CreativeModeTab.TAB_BUILDING_BLOCKS).fabric_expandArray();

        String translationKey = modid + ".item_group";
        return new CreativeItemGroup(modid, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, ItemLike icon){
        return create(modid, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeModeTab getBuildingBlocks(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getDecoration(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getRedstone(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getTransportation(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getMisc(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getSearch(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getFood(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getTools(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getCombat(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    public static CreativeModeTab getBrewing(){
        return CreativeModeTab.TAB_DECORATIONS;
    }

    private final String identifier;
    private final Component displayName;
    private final Supplier<ItemStack> icon;
    private Consumer<Consumer<ItemStack>> filler;
    private Comparator<ItemStack> sorter;

    private CreativeItemGroup(String identifier, String translationKey, Supplier<ItemStack> icon){
        super(CreativeModeTab.TABS.length - 1, identifier);
        this.identifier = identifier;
        this.displayName = TextComponents.translation(translationKey).get();
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
    public Component getDisplayName(){
        return this.displayName;
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
