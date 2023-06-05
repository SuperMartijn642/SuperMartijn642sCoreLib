package com.supermartijn642.core.item;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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

        String translationKey = modid + ".item_group." + name;
        return new CreativeItemGroup(modid, name, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, String name, ItemLike icon){
        return create(modid, name, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeItemGroup create(String modid, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");

        String translationKey = modid + ".item_group";
        return new CreativeItemGroup(modid, modid, translationKey, icon);
    }

    public static CreativeItemGroup create(String modid, ItemLike icon){
        return create(modid, () -> icon.asItem().getDefaultInstance());
    }

    public static CreativeModeTab getBuildingBlocks(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.BUILDING_BLOCKS);
    }

    public static CreativeModeTab getColoredBlocks(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.COLORED_BLOCKS);
    }

    public static CreativeModeTab getNaturalBlocks(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.NATURAL_BLOCKS);
    }

    public static CreativeModeTab getFunctionalBlocks(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.FUNCTIONAL_BLOCKS);
    }

    public static CreativeModeTab getRedstoneBlocks(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.REDSTONE_BLOCKS);
    }

    public static CreativeModeTab getToolsAndUtilities(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.TOOLS_AND_UTILITIES);
    }

    public static CreativeModeTab getCombat(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.COMBAT);
    }

    public static CreativeModeTab getFoodAndDrinks(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.FOOD_AND_DRINKS);
    }

    public static CreativeModeTab getIngredients(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.INGREDIENTS);
    }

    public static CreativeModeTab getSpawnEggs(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SPAWN_EGGS);
    }

    public static CreativeModeTab getOperatorUtilities(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.OP_BLOCKS);
    }

    public static CreativeModeTab getSearch(){
        return BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH);
    }

    private final String modid, identifier;
    private Consumer<Consumer<ItemStack>> filler;
    private Comparator<ItemStack> sorter;
    private List<ItemStack> sortedDisplayItems;

    private CreativeItemGroup(String modid, String identifier, String translationKey, Supplier<ItemStack> icon){
        super(Row.TOP, 0, Type.CATEGORY, TextComponents.translation(translationKey).get(), icon, (a, b) -> {
        });
        this.modid = modid;
        this.identifier = identifier;
        this.displayItemsGenerator = (flags, output) -> this.applyFiller(output::accept);

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(modid, identifier), this);
    }

    private void applyFiller(Consumer<ItemStack> output){
        if(this.filler != null)
            this.filler.accept(output);
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
    public void buildContents(ItemDisplayParameters parameters){
        super.buildContents(parameters);
        if(this.sorter != null){
            this.sortedDisplayItems = new ArrayList<>(this.displayItems);
            this.sortedDisplayItems.sort(this.sorter);
        }
    }

    @Override
    public Collection<ItemStack> getDisplayItems(){
        return this.sortedDisplayItems == null ? super.getDisplayItems() : this.sortedDisplayItems;
    }
}
