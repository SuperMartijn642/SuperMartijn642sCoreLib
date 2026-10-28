package com.supermartijn642.core.mixin;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
        method = "createUserApiService",
        at = @At("HEAD")
    )
    private static void captureServices(CallbackInfoReturnable<?> ci){
        // Forge's mixin version still can't target stuff in constructors, so we get this instead of targeting Services#create in the constructor
        CoreLib.services = ClientUtils.getMinecraft().services();
    }
}
