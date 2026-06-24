package com.supermartijn642.core.mixin;

import com.supermartijn642.core.data.tag.TagEntryAdapter;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 24/06/2026 by SuperMartijn642
 */
@Mixin(TagLoader.ElementLookup.class)
public class TagLoaderElementLookupMixin {

    @Inject(
        method = "fromFrozenRegistry",
        at = @At("HEAD")
    )
    private static void fromFrozenRegistry(Registry<?> registry, CallbackInfoReturnable<TagLoader.ElementLookup<?>> ci){
        TagEntryAdapter.REGISTRY_CONTEXT.set(registry);
    }

    @Inject(
        method = "fromGetters",
        at = @At("HEAD")
    )
    private static void fromGetters(ResourceKey<? extends Registry<?>> registryKey, HolderGetter<?> writable, HolderGetter<?> immutable, CallbackInfoReturnable<TagLoader.ElementLookup<?>> ci){
        if(immutable instanceof Registry<?> registry)
            TagEntryAdapter.REGISTRY_CONTEXT.set(registry);
    }
}
