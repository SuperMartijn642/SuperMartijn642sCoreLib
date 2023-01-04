package com.supermartijn642.core.mixin;

import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.GameData;
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

    private static RegistryEvent.Register<?> registerEvent;

    @ModifyVariable(method = "lambda$postRegistryEventDispatch$15(Lnet/minecraftforge/fml/ModLoadingStage$EventGenerator;)V", at = @At("STORE"), ordinal = 0, remap = false)
    private static RegistryEvent.Register<?> modifyRegisterEvent(RegistryEvent.Register<?> registerEvent){
        GameDataMixin.registerEvent = registerEvent;
        return registerEvent;
    }

    @Inject(
        method = "lambda$postRegistryEventDispatch$15(Lnet/minecraftforge/fml/ModLoadingStage$EventGenerator;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/registries/ObjectHolderRegistry;applyObjectHolders(Ljava/util/function/Predicate;)V"
        ),
        remap = false
    )
    private static void postRegistryEventDispatch(CallbackInfo ci){
        RegistryEntryAcceptor.Handler.onRegisterEvent(registerEvent);
    }
}
