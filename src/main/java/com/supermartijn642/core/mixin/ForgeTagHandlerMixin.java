package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.TagLoaderExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Created 11/02/2024 by SuperMartijn642
 */
@Mixin(ForgeTagHandler.class)
public class ForgeTagHandlerMixin {

    @Inject(
        method = "createCustomTagTypeReaders",
        at = @At("RETURN"),
        remap = false
    )
    private static void createCustomTagTypeReaders(CallbackInfoReturnable<Map<ResourceLocation,TagLoader<?>>> ci){
        ci.getReturnValue().forEach((registryName, tagLoader) -> ((TagLoaderExtension)tagLoader).supermartijn642corelibSetRegistry(null, RegistryManager.ACTIVE.getRegistry(registryName)));
    }
}
