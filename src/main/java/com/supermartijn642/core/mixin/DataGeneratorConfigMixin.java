package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.DataGeneratorExtension;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 16/10/2023 by SuperMartijn642
 */
@Mixin(value = GatherDataEvent.DataGeneratorConfig.class, remap = false)
public class DataGeneratorConfigMixin {

    @Inject(
        method = "makeGenerator",
        at = @At("RETURN")
    )
    public void makeGenerator(CallbackInfoReturnable<DataGenerator> ci){
        DataGenerator generator = ci.getReturnValue();
        if(generator != null)
            //noinspection DataFlowIssue
            ((DataGeneratorExtension)generator).setDataGeneratorConfig((GatherDataEvent.DataGeneratorConfig)(Object)this);
    }
}
