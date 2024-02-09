package com.supermartijn642.core.mixin;

import com.supermartijn642.core.extensions.TagLoaderExtension;
import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
@Mixin(TagManager.class)
public class TagManagerMixin {

    @Inject(
        method = "createLoader(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;Lnet/minecraft/tags/StaticTagHelper;)Lnet/minecraft/tags/TagManager$LoaderInfo;",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
            shift = At.Shift.BEFORE,
            ordinal = 0
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void createLoader(ResourceManager resourceManager, Executor executor, StaticTagHelper<?> tagHelper, CallbackInfoReturnable<CompletableFuture<?>> ci, Optional<?> optional, Registry<?> registry, TagLoader<?> tagLoader){
        ((TagLoaderExtension)tagLoader).supermartijn642corelibSetRegistry(registry, null);
    }
}
