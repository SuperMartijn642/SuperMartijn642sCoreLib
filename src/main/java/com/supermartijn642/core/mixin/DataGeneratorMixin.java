package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Created 23/01/2023 by SuperMartijn642
 */
@Mixin(DataGenerator.class)
public class DataGeneratorMixin {

    @ModifyVariable(
        method = "run",
        at = @At("STORE"),
        ordinal = 0
    )
    private HashCache run(HashCache hashCache){
        GeneratorRegistrationHandler.setCache(hashCache);
        return hashCache;
    }
}
