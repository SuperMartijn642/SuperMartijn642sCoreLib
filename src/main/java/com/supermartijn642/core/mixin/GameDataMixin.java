package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 25/07/2022 by SuperMartijn642
 */
@Mixin(GameData.class)
public class GameDataMixin {

    private static RegisterEvent registerEvent;

    @ModifyVariable(method = "postRegisterEvents()V", at = @At("STORE"), ordinal = 0, remap = false)
    private static RegisterEvent modifyRegisterEvent(RegisterEvent registerEvent){
        GameDataMixin.registerEvent = registerEvent;
        return registerEvent;
    }

    @Inject(
        method = "postRegisterEvents()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/fml/ModLoader;postEventWrapContainerInModOrder(Lnet/neoforged/bus/api/Event;)V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private static void postRegisterEvents(CallbackInfo ci){
        RegistryEntryAcceptor.Handler.onRegisterEvent(registerEvent);
    }
}
