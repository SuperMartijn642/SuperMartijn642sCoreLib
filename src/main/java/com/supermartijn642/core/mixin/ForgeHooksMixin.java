package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private static void canHarvestBlock(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos, CallbackInfoReturnable<Boolean> ci){
        if(state.getBlock() instanceof BaseBlock){
            if(!((BaseBlock)state.getBlock()).requiresCorrectToolForDrops()){
                ci.setReturnValue(ForgeEventFactory.doPlayerHarvestCheck(player, state, true));
                return;
            }

            ItemStack stack = player.getMainHandItem();
            ToolType tool = state.getHarvestTool();
            if(stack.isEmpty() || tool == null){
                ci.setReturnValue(player.canDestroy(state));
                return;
            }

            int toolLevel = stack.getHarvestLevel(tool, player, state);
            if(toolLevel < 0){
                ci.setReturnValue(player.canDestroy(state));
                return;
            }

            ci.setReturnValue(ForgeEventFactory.doPlayerHarvestCheck(player, state, toolLevel >= state.getHarvestLevel()));
        }
    }

    @Redirect(
        method = "canHarvestBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/Item;getHarvestLevel(Lnet/minecraft/item/ItemStack;Lnet/minecraftforge/common/ToolType;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/block/BlockState;)I"
        ),
        remap = false
    )
    private static int canHarvestBlockRedirect(Item item, ItemStack stack, ToolType type, PlayerEntity player, BlockState state){
        if(state.getBlock() instanceof BaseBlock){
            int bestHarvestLevel = -1;
            for(ToolType toolType : stack.getToolTypes()){
                if(state.isToolEffective(toolType)){
                    int harvestLevel = stack.getHarvestLevel(toolType, player, state);
                    if(harvestLevel > bestHarvestLevel)
                        bestHarvestLevel = harvestLevel;
                }
            }
            if(bestHarvestLevel == -1)
                bestHarvestLevel = stack.getHarvestLevel(type, player, state);
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
    private static void canToolHarvestBlock(IWorldReader world, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> ci){
        if(stack.isEmpty())
            return;
        BlockState state = world.getBlockState(pos);
        int harvestLevel = state.getHarvestLevel();
        if(state.getBlock() instanceof BaseBlock){
            for(ToolType toolType : stack.getToolTypes()){
                if(state.isToolEffective(toolType) && stack.getHarvestLevel(toolType, null, state) >= harvestLevel){
                    ci.setReturnValue(true);
                    return;
                }
            }
            ci.setReturnValue(false);
        }
    }
}
