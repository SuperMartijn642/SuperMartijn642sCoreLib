package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 31/07/2022 by SuperMartijn642
 */
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(
        method = "updateAutoJump",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/MovementInput;getMoveVector()Lnet/minecraft/util/math/Vec2f;",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private void updateAutoJump(float xDirection, float zDirection, CallbackInfo ci){
        Entity entity = (Entity)(Object)this;
        Block block = entity.level.getBlockState(new BlockPos(entity)).getBlock();
        float jumpFactor = block instanceof BaseBlock ? ((BaseBlock)block).getJumpFactor() : 1;
        if(jumpFactor < 1)
            ci.cancel();
        else if(jumpFactor == 1){
            block = entity.level.getBlockState(new BlockPos(entity.x, entity.getBoundingBox().minY - 0.5000001D, entity.z)).getBlock();
            if(block instanceof BaseBlock && ((BaseBlock)block).getJumpFactor() < 1)
                ci.cancel();
        }
    }
}
