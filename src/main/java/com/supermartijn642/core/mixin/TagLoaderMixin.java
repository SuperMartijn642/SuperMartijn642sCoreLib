package com.supermartijn642.core.mixin;

import com.supermartijn642.core.data.tag.TagEntryAdapter;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
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
public class TagLoaderMixin {

    @Unique
    private Registry<?> registry;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(TagLoader.ElementLookup<?> elementLookup, String directory, CallbackInfo ci){
        this.registry = TagEntryAdapter.REGISTRY_CONTEXT.get();
        TagEntryAdapter.REGISTRY_CONTEXT.remove();
    }

    @Inject(
        method = "build(Ljava/util/Map;)Ljava/util/Map;",
        at = @At("HEAD")
    )
    private void build(Map<Identifier,List<TagLoader.EntryWithSource>> tags, CallbackInfoReturnable<Map<?,?>> ci){
        for(List<TagLoader.EntryWithSource> tag : tags.values()){
            for(TagLoader.EntryWithSource entry : tag){
                if(entry.entry() instanceof TagEntryAdapter)
                    ((TagEntryAdapter)entry.entry()).setRegistry(this.registry);
            }
        }
    }
}
