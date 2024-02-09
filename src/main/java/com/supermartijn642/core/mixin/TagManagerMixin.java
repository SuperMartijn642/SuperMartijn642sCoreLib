package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.TagLoaderExtension;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
@Mixin(TagManager.class)
public class TagManagerMixin {

    @Inject(
        method = "createLoader(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;Lnet/minecraft/core/RegistryAccess$RegistryEntry;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("RETURN"),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void createLoader(ResourceManager resourceManager, Executor executor, RegistryAccess.RegistryEntry<?> registryEntry, CallbackInfoReturnable<CompletableFuture<?>> ci, ResourceKey<?> registryKey, Registry<?> registry, TagLoader<?> tagLoader){
        ((TagLoaderExtension)tagLoader).supermartijn642corelibSetRegistry(registry);
    }
}
