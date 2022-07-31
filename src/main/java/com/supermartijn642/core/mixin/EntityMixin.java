package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.extensions.EntityExtension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 29/07/2022 by SuperMartijn642
 */
@Mixin(Entity.class)
public class EntityMixin implements EntityExtension {

    @Inject(
        method = "fireImmune",
        at = @At("HEAD"),
        cancellable = true
    )
    private void fireImmune(CallbackInfoReturnable<Boolean> ci){
        if(this.coreLibIsFireImmune())
            ci.setReturnValue(true);
    }

    @Override
    public boolean coreLibIsFireImmune(){
        return false;
    }

    @Redirect(
        method = "isInWall",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;isViewBlocking(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"
        )
    )
    private boolean isInWall(BlockState state, IBlockReader level, BlockPos pos){
        return state.getBlock() instanceof BaseBlock ? ((BaseBlock)state.getBlock()).isSuffocating(state, level, pos) : state.isViewBlocking(level, pos);
    }

    @Inject(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;isInWaterRainOrBubble()Z",
            shift = At.Shift.BEFORE
        )
    )
    private void move(MoverType moverType, Vec3d direction, CallbackInfo ci){
        Entity entity = (Entity)(Object)this;
        Block block = entity.level.getBlockState(new BlockPos(entity)).getBlock();
        float speedFactor = block instanceof BaseBlock ? ((BaseBlock)block).getSpeedFactor() : 1;
        if(block != Blocks.WATER && block != Blocks.BUBBLE_COLUMN && speedFactor == 1){
            block = entity.level.getBlockState(new BlockPos(entity.x, entity.getBoundingBox().minY - 0.5000001D, entity.z)).getBlock();
            if(block instanceof BaseBlock)
                speedFactor = ((BaseBlock)block).getSpeedFactor();
        }
        if(speedFactor != 1)
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(speedFactor, 1, speedFactor));
    }
}
