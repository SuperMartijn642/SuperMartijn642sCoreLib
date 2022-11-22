package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraft.advancements.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {

    @Inject(
        method = "canHarvestBlock",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void canHarvestBlock(Block block, EntityPlayer player, IBlockAccess world, BlockPos pos, CallbackInfoReturnable<Boolean> ci){
        if(block instanceof BaseBlock && !((BaseBlock)block).requiresCorrectToolForDrops())
            ci.setReturnValue(ForgeEventFactory.doPlayerHarvestCheck(player, world.getBlockState(pos), true));
    }

    @Redirect(
        method = "canHarvestBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/Item;getHarvestLevel(Lnet/minecraft/item/ItemStack;Ljava/lang/String;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/block/state/IBlockState;)I"
        ),
        remap = false
    )
    private static int canHarvestBlockRedirect(Item item, ItemStack stack, String type, EntityPlayer player, IBlockState state){
        if(state.getBlock() instanceof BaseBlock){
            int bestHarvestLevel = -1;
            for(String toolType : item.getToolClasses(stack)){
                if(state.getBlock().isToolEffective(toolType, state)){
                    int harvestLevel = item.getHarvestLevel(stack, toolType, player, state);
                    if(harvestLevel > bestHarvestLevel)
                        bestHarvestLevel = harvestLevel;
                }
            }
            if(bestHarvestLevel == -1)
                bestHarvestLevel = item.getHarvestLevel(stack, type, player, state);
            return bestHarvestLevel;
        }
        return item.getHarvestLevel(stack, type, player, state);
    }

    @Inject(
        method = "canToolHarvestBlock",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void canToolHarvestBlock(IBlockAccess world, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> ci){
        if(stack.isEmpty())
            return;
        IBlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof BaseBlock){
            int harvestLevel = state.getBlock().getHarvestLevel(state);
            for(String toolType : stack.getItem().getToolClasses(stack)){
                if(state.getBlock().isToolEffective(toolType, state) && stack.getItem().getHarvestLevel(stack, toolType, null, state) >= harvestLevel){
                    ci.setReturnValue(true);
                    return;
                }
            }
            ci.setReturnValue(false);
        }
    }

    @Inject(
        method = "loadAdvancements",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/common/crafting/CraftingHelper;init()V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private static void loadAdvancements(Map<ResourceLocation, Advancement.Builder> map, CallbackInfoReturnable<Boolean> ci){
        RegistrationHandler.handleResourceConditionSerializerRegistryEvent();
        RegistryEntryAcceptor.Handler.onRegisterEvent(Registries.RECIPE_CONDITION_SERIALIZERS);
    }
}
