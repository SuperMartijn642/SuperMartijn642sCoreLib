package com.supermartijn642.core.mixin;

import com.supermartijn642.core.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Created 31/08/2022 by SuperMartijn642
 */
@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow
    private final float size = 0;

    @Redirect(
        method = "doExplosionB",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;dropBlockAsItemWithChance(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;FI)V"
        )
    )
    private void dropBlockRedirect(Block block, World worldIn, BlockPos pos, IBlockState state, float chance, int fortune){
        if(!(block instanceof BaseBlock)){
            block.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
            return;
        }

        ((BaseBlock)block).dropItemsFromExplosion(worldIn, pos, state, this.size);
    }
}
