package com.supermartijn642.core.item;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class ItemGroup extends CreativeModeTab {

    public static ItemGroup create(String modid, String name, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Item group name '" + name + "' must only contain characters [a-z0-9_.-]!");

        String identifier = modid + "." + name;
        String translationKey = modid + ".itemGroup." + name;
        return new ItemGroup(identifier, translationKey, icon);
    }

    public static ItemGroup create(String modid, String name, ItemLike icon){
        return create(modid, name, () -> icon.asItem().getDefaultInstance());
    }

    public static ItemGroup create(String modid, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");

        String translationKey = modid + ".itemGroup";
        return new ItemGroup(modid, translationKey, icon);
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

    public static ItemGroup create(String modid, ItemLike icon){
        return create(modid, () -> icon.asItem().getDefaultInstance());
    }

    private final String identifier;
    private final Component displayName;
    private final Supplier<ItemStack> icon;

    private ItemGroup(String identifier, String translationKey, Supplier<ItemStack> icon){
        super(identifier);
        this.identifier = identifier;
        this.displayName = TextComponents.translation(translationKey).get();
        this.icon = icon;
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
}
