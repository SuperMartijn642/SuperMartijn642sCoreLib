package com.supermartijn642.core.item;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class ItemGroup extends net.minecraft.item.ItemGroup {

    public static ItemGroup create(String modid, String name, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Item group name '" + name + "' must only contain characters [a-z0-9_.-]!");

        String identifier = modid + "." + name;
        String translationKey = modid + ".itemGroup." + name;
        return new ItemGroup(identifier, translationKey, icon);
    }

    public static ItemGroup create(String modid, String name, IItemProvider icon){
        return create(modid, name, () -> icon.asItem().getDefaultInstance());
    }

    public static ItemGroup create(String modid, Supplier<ItemStack> icon){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");

        String translationKey = modid + ".itemGroup";
        return new ItemGroup(modid, translationKey, icon);
    }

    public static net.minecraft.item.ItemGroup getBuildingBlocks(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getDecoration(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getRedstone(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getTransportation(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getMisc(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getSearch(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getFood(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getTools(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getCombat(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static net.minecraft.item.ItemGroup getBrewing(){
        return net.minecraft.item.ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup create(String modid, IItemProvider icon){
        return create(modid, () -> icon.asItem().getDefaultInstance());
    }

    private final String identifier;
    private final ITextComponent displayName;
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
    public ITextComponent getDisplayName(){
        return this.displayName;
    }
}
