package com.supermartijn642.core.mixin;

import com.supermartijn642.core.data.tag.TagEntryAdapter;
import net.minecraft.tags.NetworkTagCollection;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
@Mixin(TagCollection.class)
public class TagCollectionMixin {

    @Unique
    private Registry<?> registry;
    @Final
    @Shadow
    private String directory;

    @Inject(
        method = "load(Ljava/util/Map;)V",
        at = @At("HEAD")
    )
    private void load(Map<ResourceLocation,Tag.Builder<?>> builders, CallbackInfo ci){
        if(this.registry == null){
            //noinspection ConstantValue
            if((Object)this instanceof NetworkTagCollection<?>)
                this.registry = ((NetworkTagCollection<?>)(Object)this).registry;
            else if(this.directory.equals("tags/blocks"))
                //noinspection deprecation
                this.registry = Registry.BLOCK;
            else if(this.directory.equals("tags/items"))
                //noinspection deprecation
                this.registry = Registry.ITEM;
            else if(this.directory.equals("tags/fluids"))
                //noinspection deprecation
                this.registry = Registry.FLUID;
            else if(this.directory.equals("tags/entity_types"))
                //noinspection deprecation
                this.registry = Registry.ENTITY_TYPE;
        }
        for(Tag.Builder<?> builder : builders.values()){
            for(Tag.ITagEntry<?> entry : builder.values){
                if(entry instanceof TagEntryAdapter)
                    ((TagEntryAdapter<?>)entry).setRegistry(this.registry);
            }
        }
    }
}
