package com.supermartijn642.core.mixin;

import com.supermartijn642.core.data.tag.TagEntryAdapter;
import com.supermartijn642.core.extensions.TagLoaderExtension;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        method = "build(Ljava/util/Map;)Ljava/util/Map;",
        at = @At("HEAD")
    )
    private void build(Map<ResourceLocation,Tag.Builder> builders, CallbackInfoReturnable<Map<?,?>> ci){
        for(Tag.Builder builder : builders.values()){
            for(Tag.BuilderEntry entry : builder.entries){
                if(entry.entry() instanceof TagEntryAdapter)
                    ((TagEntryAdapter)entry.entry()).setRegistry(this.registry);
            }
        }
    }
}
