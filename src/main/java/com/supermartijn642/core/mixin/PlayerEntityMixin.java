package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(
        method = "canDestroy",
        at = @At("HEAD"),
        cancellable = true
    )
    private void canDestroy(BlockState state, CallbackInfoReturnable<Boolean> ci){
        if(state.getBlock() instanceof BaseBlock)
            ci.setReturnValue(ForgeEventFactory.doPlayerHarvestCheck((PlayerEntity)(Object)this, state, !((BaseBlock)state.getBlock()).requiresCorrectToolForDrops() || ((PlayerEntity)(Object)this).inventory.canDestroy(state)));
    }

    @Redirect(
        method = "freeAt",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/block/BlockState.isViewBlocking(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"
        )
    )
    private boolean freeAt(BlockState state, IBlockReader level, BlockPos pos){
        return state.getBlock() instanceof BaseBlock ? ((BaseBlock)state.getBlock()).isSuffocating(state, level, pos) : state.isViewBlocking(level, pos);
    }
}
