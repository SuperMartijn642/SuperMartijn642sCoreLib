package com.supermartijn642.core.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.BaseItem;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * Created 09/06/2025 by SuperMartijn642
 */
@Mixin(Item.class)
public class ItemMixin {

    @WrapWithCondition(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/component/DataComponentInitializers;add(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/component/DataComponentInitializers$Initializer;)V"
        )
    )
    private boolean init(DataComponentInitializers initializers, ResourceKey<?> resourceKey, DataComponentInitializers.Initializer<?> componentInitializer) {
        //noinspection ConstantValue
        return !((Object)this instanceof BaseItem) && !((Object)this instanceof BaseBlockItem);
    }

    @Inject(
        method = "appendHoverText",
        at = @At("HEAD")
    )
    private void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag, CallbackInfo ci) {
        //noinspection ConstantValue
        if((Object)this instanceof BlockItem blockItem && blockItem.getBlock() instanceof BaseBlock block)
            block.appendItemInformation(stack, consumer, flag.isAdvanced());
    }
}
