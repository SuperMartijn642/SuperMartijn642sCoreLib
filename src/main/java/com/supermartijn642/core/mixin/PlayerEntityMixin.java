package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(EntityPlayer.class)
public class PlayerEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(
        method = "canHarvestBlock",
        at = @At("HEAD"),
        cancellable = true
    )
    private void canHarvestBlock(IBlockState state, CallbackInfoReturnable<Boolean> ci){
        if(state.getBlock() instanceof BaseBlock)
            ci.setReturnValue(ForgeEventFactory.doPlayerHarvestCheck((EntityPlayer)(Object)this, state, !((BaseBlock)state.getBlock()).requiresCorrectToolForDrops() || ((EntityPlayer)(Object)this).inventory.canHarvestBlock(state)));
    }
}
