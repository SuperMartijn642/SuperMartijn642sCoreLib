package com.supermartijn642.core.mixin;

import com.supermartijn642.core.render.RenderWorldEvent;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 17/11/2021 by SuperMartijn642
 */
@Mixin(ForgeHooksClient.class) // Yes this is stupid, but I need to keep it for legacy reasons and proper mixin placement causes problems with every other mod in 1.12
public class LevelRendererMixin {

    @Inject(method = "dispatchRenderLast(Lnet/minecraft/client/renderer/RenderGlobal;F)V", at = @At("HEAD"), remap = false)
    private static void dispatchRenderLast(RenderGlobal context, float partialTicks, CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new RenderWorldEvent(partialTicks));
    }
}
