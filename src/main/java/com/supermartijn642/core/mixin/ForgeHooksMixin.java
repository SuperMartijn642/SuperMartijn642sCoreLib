package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
    public static void canHarvestBlock(BlockState state, PlayerEntity player, IBlockReader world, BlockPos pos, CallbackInfoReturnable<Boolean> ci){
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
}
