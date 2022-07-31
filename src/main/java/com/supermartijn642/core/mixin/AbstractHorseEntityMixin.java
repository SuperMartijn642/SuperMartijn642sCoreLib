package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
@Mixin(AbstractHorseEntity.class)
public class AbstractHorseEntityMixin {

    @Redirect(
        method = "travel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/passive/horse/AbstractHorseEntity;getCustomJump()D"
        )
    )
    private double travel(AbstractHorseEntity entity){
        Block block = entity.level.getBlockState(new BlockPos(entity)).getBlock();
        float jumpFactor = block instanceof BaseBlock ? ((BaseBlock)block).getJumpFactor() : 1;
        if(jumpFactor == 1){
            block = entity.level.getBlockState(new BlockPos(entity.x, entity.getBoundingBox().minY - 0.5000001D, entity.z)).getBlock();
            if(block instanceof BaseBlock)
                jumpFactor = ((BaseBlock)block).getJumpFactor();
        }
        return entity.getCustomJump() * jumpFactor;
    }
}
