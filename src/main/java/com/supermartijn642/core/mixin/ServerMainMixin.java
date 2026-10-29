package com.supermartijn642.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.core.CoreLib;
import net.minecraft.server.Main;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class ServerMainMixin {

    @Inject(
        method = "main",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/server/Services;create(Lcom/mojang/authlib/services/MinecraftServicesDiscoveryService;Ljava/io/File;)Lnet/minecraft/server/Services;",
            shift = At.Shift.AFTER
        )
    )
    private static void captureServices(CallbackInfo ci, @Local Services services){
        CoreLib.services = services;
    }
}
