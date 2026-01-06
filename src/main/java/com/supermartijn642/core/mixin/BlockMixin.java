package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.BlockExtension;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 06/01/2026 by SuperMartijn642
 */
@Mixin(Block.class)
public class BlockMixin implements BlockExtension {

    @Unique
    private Block.Properties properties;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(Block.Properties properties, CallbackInfo ci){
        this.properties = properties;
    }

    @Override
    public Block.Properties supermartijn642corelibGetProperties(){
        return this.properties;
    }
}
