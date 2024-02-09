package com.supermartijn642.core.mixin;

import com.supermartijn642.core.data.tag.TagEntryAdapter;
import com.supermartijn642.core.extensions.TagLoaderExtension;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
        method = "build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;",
        at = @At("HEAD")
    )
    private void build(TagEntry.Lookup<?> lookup, List<TagLoader.EntryWithSource> entries, CallbackInfoReturnable<Map<?,?>> ci){
        for(TagLoader.EntryWithSource entry : entries){
            if(entry.entry() instanceof TagEntryAdapter)
                ((TagEntryAdapter)entry.entry()).setRegistry(this.registry);
        }
    }
}
