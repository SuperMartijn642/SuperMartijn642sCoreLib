package com.supermartijn642.core.data.tag;

import com.google.gson.JsonArray;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class TagEntryAdapter implements ITag.ITagEntry {

    final ResourceLocation identifier;
    final CustomTagEntry customEntry;
    private Registry<?> registry;
    private ForgeRegistry<?> forgeRegistry;

    TagEntryAdapter(ResourceLocation identifier, CustomTagEntry customEntry){
        this.identifier = identifier;
        this.customEntry = customEntry;
    }

    public void setRegistry(Registry<?> registry, ForgeRegistry<?> forgeRegistry){
        this.registry = registry;
        this.forgeRegistry = forgeRegistry;
    }

    @Override
    public <T> boolean build(Function<ResourceLocation,ITag<T>> tagLookup, Function<ResourceLocation,T> elementLookup, Consumer<T> entryConsumer){
        CustomTagEntry.TagEntryResolutionContext<T> context = new CustomTagEntry.TagEntryResolutionContext<T>() {
            @Override
            public T getElement(ResourceLocation identifier){
                return elementLookup.apply(identifier);
            }

            @Override
            public Collection<T> getTag(ResourceLocation identifier){
                return tagLookup.apply(identifier).getValues();
            }

            @Override
            public Collection<T> getAllElements(){
                //noinspection unchecked
                return TagEntryAdapter.this.registry == null ?
                    (Collection<T>)TagEntryAdapter.this.forgeRegistry.getValues() :
                    (Collection<T>)TagEntryAdapter.this.registry.stream().collect(Collectors.toList());
            }

            @Override
            public Set<ResourceLocation> getAllIdentifiers(){
                return TagEntryAdapter.this.registry == null ?
                    TagEntryAdapter.this.forgeRegistry.getKeys() :
                    TagEntryAdapter.this.registry.keySet();
            }
        };
        Collection<T> entries = this.customEntry.resolve(context);
        if(entries != null)
            entries.forEach(entryConsumer);
        return true;
    }

    @Override
    public String toString(){
        return "'" + this.identifier + "'{" + this.customEntry + "}";
    }

    @Override
    public void serializeTo(JsonArray array){
        array.add(CustomTagEntries.serialize(this));
    }
}
