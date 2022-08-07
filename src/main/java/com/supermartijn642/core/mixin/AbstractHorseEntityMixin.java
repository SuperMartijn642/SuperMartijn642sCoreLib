package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
@Mixin(AbstractHorse.class)
public class AbstractHorseEntityMixin {

    @Redirect(
        method = "travel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/passive/AbstractHorse;getHorseJumpStrength()D"
        )
    )
    private double travel(AbstractHorse entity){
        Block block = entity.world.getBlockState(new BlockPos(entity)).getBlock();
        float jumpFactor = block instanceof BaseBlock ? ((BaseBlock)block).getJumpFactor() : 1;
        if(jumpFactor == 1){
            block = entity.world.getBlockState(new BlockPos(entity.posX, entity.getEntityBoundingBox().minY - 0.5000001D, entity.posZ)).getBlock();
            if(block instanceof BaseBlock)
                jumpFactor = ((BaseBlock)block).getJumpFactor();
        }
        return entity.getHorseJumpStrength() * jumpFactor;
    }
}
