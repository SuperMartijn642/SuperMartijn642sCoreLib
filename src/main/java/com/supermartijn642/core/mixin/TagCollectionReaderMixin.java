package com.supermartijn642.core.mixin;

import com.supermartijn642.core.data.tag.TagEntryAdapter;
import com.supermartijn642.core.extensions.TagLoaderExtension;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionReader;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
@Mixin(TagCollectionReader.class)
public class TagCollectionReaderMixin implements TagLoaderExtension {

    @Unique
    private Registry<?> registry;
    @Unique
    private ForgeRegistry<?> forgeRegistry;
    @Final
    @Shadow
    private String directory;

    @Override
    public void supermartijn642corelibSetRegistry(Registry<?> registry, ForgeRegistry<?> forgeRegistry){
        this.registry = registry;
        this.forgeRegistry = forgeRegistry;
    }

    @Inject(
        method = "load(Ljava/util/Map;)Lnet/minecraft/tags/ITagCollection;",
        at = @At("HEAD")
    )
    private void build(Map<ResourceLocation,ITag.Builder> builders, CallbackInfoReturnable<ITagCollection<?>> ci){
        if(this.registry == null && this.forgeRegistry == null){
            //noinspection IfCanBeSwitch
            if(this.directory.equals("tags/blocks"))
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
        for(ITag.Builder builder : builders.values()){
            for(ITag.Proxy entry : builder.entries){
                if(entry.entry instanceof TagEntryAdapter)
                    ((TagEntryAdapter)entry.entry).setRegistry(this.registry, this.forgeRegistry);
            }
        }
    }
}
