package com.supermartijn642.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.core.data.tag.TagEntryAdapter;
import com.supermartijn642.core.extensions.TagLoaderExtension;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
@Mixin(TagLoader.class)
public class TagLoaderMixin implements TagLoaderExtension {

    @Unique
    private Registry<?> registry;

    @Override
    public void supermartijn642corelibSetRegistry(Registry<?> registry){
        this.registry = registry;
    }

    @Inject(
        method = "loadTagsForRegistry",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tags/TagLoader;load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;",
            shift = At.Shift.BEFORE
        )
    )
    private static void loadTagsForRegistry(ResourceManager resourceManager, WritableRegistry<?> registry, CallbackInfo ci, @Local TagLoader<?> tagLoader){
        ((TagLoaderExtension)tagLoader).supermartijn642corelibSetRegistry(registry);
    }

    @Inject(
        method = "loadPendingTags",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tags/TagLoader;load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;",
            shift = At.Shift.BEFORE
        )
    )
    private static void loadPendingTags(ResourceManager resourceManager, Registry<?> registry, CallbackInfoReturnable<?> ci, @Local TagLoader<?> tagLoader){
        ((TagLoaderExtension)tagLoader).supermartijn642corelibSetRegistry(registry);
    }

    @Inject(
        method = "build(Ljava/util/Map;)Ljava/util/Map;",
        at = @At("HEAD")
    )
    private void build(Map<ResourceLocation,List<TagLoader.EntryWithSource>> tags, CallbackInfoReturnable<Map<?,?>> ci){
        for(List<TagLoader.EntryWithSource> tag : tags.values()){
            for(TagLoader.EntryWithSource entry : tag){
                if(entry.entry() instanceof TagEntryAdapter)
                    ((TagEntryAdapter)entry.entry()).setRegistry(this.registry);
            }
        }
    }
}
