package com.supermartijn642.core.item;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class CreativeItemGroup extends ItemGroup {

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
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getDecoration(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getRedstone(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getTransportation(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getMisc(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getSearch(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getFood(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getTools(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getCombat(){
        return ItemGroup.TAB_DECORATIONS;
    }

    public static ItemGroup getBrewing(){
        return ItemGroup.TAB_DECORATIONS;
    }

    private final String identifier;
    private final ITextComponent displayName;
    private final Supplier<ItemStack> icon;

    private CreativeItemGroup(String identifier, String translationKey, Supplier<ItemStack> icon){
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
