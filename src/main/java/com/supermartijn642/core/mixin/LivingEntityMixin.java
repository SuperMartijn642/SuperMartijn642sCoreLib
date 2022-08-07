package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
@Mixin(EntityLivingBase.class)
public class LivingEntityMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(
        method = "getJumpUpwardsMotion",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getJumpUpwardsMotion(CallbackInfoReturnable<Float> ci){
        EntityLivingBase entity = (EntityLivingBase)(Object)this;
        Block block = entity.world.getBlockState(new BlockPos(entity)).getBlock();
        float jumpFactor = block instanceof BaseBlock ? ((BaseBlock)block).getJumpFactor() : 1;
        if(jumpFactor == 1){
            block = entity.world.getBlockState(new BlockPos(entity.posX, entity.getEntityBoundingBox().minY - 0.5000001D, entity.posZ)).getBlock();
            if(block instanceof BaseBlock){
                jumpFactor = ((BaseBlock)block).getJumpFactor();
                if(jumpFactor != 1)
                    ci.setReturnValue(0.42F * jumpFactor);
            }
        }else
            ci.setReturnValue(0.42F * jumpFactor);
    }
}
