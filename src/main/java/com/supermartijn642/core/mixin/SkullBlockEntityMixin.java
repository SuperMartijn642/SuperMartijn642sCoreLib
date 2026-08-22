package com.supermartijn642.core.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.supermartijn642.core.CoreLib;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.SkullTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullTileEntity.class)
public class SkullBlockEntityMixin {

    @Inject(
        method = "setProfileCache",
        at = @At("HEAD")
    )
    private static void setProfileCache(PlayerProfileCache profileCache, CallbackInfo ci){
        CoreLib.gameProfileCache = profileCache;
    }

    @Inject(
        method = "setSessionService",
        at = @At("HEAD")
    )
    private static void setSessionService(MinecraftSessionService sessionService, CallbackInfo ci){
        CoreLib.sessionService = sessionService;
    }
}
