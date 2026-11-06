package com.supermartijn642.core.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.supermartijn642.core.CoreLib;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockEntity.class)
public class SkullBlockEntityMixin {

    @Inject(
        method = "setProfileCache",
        at = @At("HEAD")
    )
    private static void setProfileCache(GameProfileCache profileCache, CallbackInfo ci){
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
