package com.supermartijn642.core.mixin;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.supermartijn642.core.CoreLib;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(SkullBlockEntity.class)
public class SkullBlockEntityMixin {

    @Inject(
        method = "setup",
        at = @At("HEAD")
    )
    private static void setup(GameProfileCache profileCache, MinecraftSessionService sessionService, Executor executor, CallbackInfo ci){
        CoreLib.sessionService = sessionService;
        CoreLib.gameProfileCache = profileCache;
    }
}
