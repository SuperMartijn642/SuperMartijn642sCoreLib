package com.supermartijn642.core.item;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.reflect.Method;
import java.util.*;
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
        return CreativeModeTabs.BUILDING_BLOCKS;
    }

    public static CreativeModeTab getColoredBlocks(){
        return CreativeModeTabs.COLORED_BLOCKS;
    }

    public static CreativeModeTab getNaturalBlocks(){
        return CreativeModeTabs.NATURAL_BLOCKS;
    }

    public static CreativeModeTab getFunctionalBlocks(){
        return CreativeModeTabs.FUNCTIONAL_BLOCKS;
    }

    public static CreativeModeTab getRedstoneBlocks(){
        return CreativeModeTabs.REDSTONE_BLOCKS;
    }

    public static CreativeModeTab getToolsAndUtilities(){
        return CreativeModeTabs.TOOLS_AND_UTILITIES;
    }

    public static CreativeModeTab getCombat(){
        return CreativeModeTabs.COMBAT;
    }

    public static CreativeModeTab getFoodAndDrinks(){
        return CreativeModeTabs.FOOD_AND_DRINKS;
    }

    public static CreativeModeTab getIngredients(){
        return CreativeModeTabs.INGREDIENTS;
    }

    public static CreativeModeTab getSpawnEggs(){
        return CreativeModeTabs.SPAWN_EGGS;
    }

    public static CreativeModeTab getOperatorUtilities(){
        return CreativeModeTabs.OP_BLOCKS;
    }

    private final String modid, identifier;
    private Consumer<Consumer<ItemStack>> filler;
    private Comparator<ItemStack> sorter;
    private List<ItemStack> sortedDisplayItems;

    private CreativeItemGroup(String modid, String identifier, String translationKey, Supplier<ItemStack> icon){
        super(CreativeModeTab.builder(Row.TOP, 0).icon(icon).title(TextComponents.translation(translationKey).get()));
        this.modid = modid;
        this.identifier = identifier;
        this.displayItemsGenerator = (flags, output, hasPermissions) -> this.applyFiller(output::accept);

        FMLJavaModLoadingContext.get().getModEventBus().addListener((Consumer<CreativeModeTabEvent.Register>)event -> registerTab(this));
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
    public void buildContents(FeatureFlagSet flags, boolean hasPermissions){
        super.buildContents(flags, hasPermissions);
        if(this.sorter != null){
            this.sortedDisplayItems = new ArrayList<>(this.displayItems);
            this.sortedDisplayItems.sort(this.sorter);
        }
    }

    @Override
    public Collection<ItemStack> getDisplayItems(){
        return this.sortedDisplayItems == null ? super.getDisplayItems() : this.sortedDisplayItems;
    }

    /**
     * {@link CreativeModeTabRegistry#processCreativeModeTab(CreativeModeTab, ResourceLocation, List, List)}
     */
    @SuppressWarnings("JavadocReference")
    private static final Method processCreativeModeTab;

    static{
        try{
            processCreativeModeTab = CreativeModeTabRegistry.class.getDeclaredMethod("processCreativeModeTab", CreativeModeTab.class, ResourceLocation.class, List.class, List.class);
            processCreativeModeTab.setAccessible(true);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private static void registerTab(CreativeItemGroup tab){
        ResourceLocation identifier = new ResourceLocation(tab.modid, tab.identifier);
        if(CreativeModeTabRegistry.getTab(identifier) != null)
            throw new IllegalStateException("Duplicate creative mode tab with name: " + identifier);

        try{
            processCreativeModeTab.invoke(null, tab, identifier, Collections.emptyList(), Collections.emptyList());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
