package com.supermartijn642.core.mixin;

import com.supermartijn642.core.CoreLib;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 27/07/2022 by SuperMartijn642
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;",
            ordinal = 0,
            shift = At.Shift.BEFORE
        ),
        method = "<init>"
    )
    private void beforeInit(CallbackInfo ci){
        CoreLib.beforeInitialize();
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;",
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        method = "<init>"
    )
    private void afterInit(CallbackInfo ci){
        CoreLib.afterInitializeAll();
    }
}
