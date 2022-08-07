package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.extensions.EntityExtension;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(Entity.class)
public class EntityMixin implements EntityExtension {

    @Inject(
        method = "isImmuneToFire",
        at = @At("HEAD"),
        cancellable = true
    )
    private void isImmuneToFire(CallbackInfoReturnable<Boolean> ci){
        if(this.coreLibIsFireImmune())
            ci.setReturnValue(true);
    }

    @Override
    public boolean coreLibIsFireImmune(){
        return false;
    }

    @Inject(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;isWet()Z",
            shift = At.Shift.BEFORE
        )
    )
    private void move(MoverType moverType, double moveX, double moveY, double moveZ, CallbackInfo ci){
        Entity entity = (Entity)(Object)this;
        Block block = entity.world.getBlockState(new BlockPos(entity)).getBlock();
        float speedFactor = block instanceof BaseBlock ? ((BaseBlock)block).getSpeedFactor() : 1;
        if(block != Blocks.WATER && block != Blocks.FLOWING_WATER && speedFactor == 1){
            block = entity.world.getBlockState(new BlockPos(entity.posX, entity.getEntityBoundingBox().minY - 0.5000001D, entity.posZ)).getBlock();
            if(block instanceof BaseBlock)
                speedFactor = ((BaseBlock)block).getSpeedFactor();
        }
        if(speedFactor != 1)
            entity.setVelocity(entity.motionX * speedFactor, entity.motionY, entity.motionZ * speedFactor);
    }
}
