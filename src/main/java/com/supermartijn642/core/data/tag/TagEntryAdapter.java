package com.supermartijn642.core.data.tag;

import com.google.gson.JsonArray;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class TagEntryAdapter<T> implements Tag.ITagEntry<T> {

    final ResourceLocation identifier;
    final CustomTagEntry customEntry;
    private Registry<?> registry;
    private ForgeRegistry<?> forgeRegistry;
    private Function<ResourceLocation,Tag<T>> tagLookup;

    TagEntryAdapter(ResourceLocation identifier, CustomTagEntry customEntry){
        this.identifier = identifier;
        this.customEntry = customEntry;
    }

    public void setRegistry(Registry<?> registry){
        this.registry = registry;
    }

    @Override
    public boolean canBuild(Function<ResourceLocation,Tag<T>> tagLookup){
        this.tagLookup = tagLookup;
        return true;
    }

    @Override
    public void build(Collection<T> collection){
        CustomTagEntry.TagEntryResolutionContext<T> context = new CustomTagEntry.TagEntryResolutionContext<T>() {
            @Override
            public T getElement(ResourceLocation identifier){
                //noinspection unchecked
                return TagEntryAdapter.this.registry == null ?
                    (T)TagEntryAdapter.this.forgeRegistry.getValue(identifier) :
                    (T)TagEntryAdapter.this.registry.get(identifier);
            }

            @Override
            public Collection<T> getTag(ResourceLocation identifier){
                return TagEntryAdapter.this.tagLookup.apply(identifier).getValues();
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
            collection.addAll(entries);
    }

    @Override
    public String toString(){
        return "'" + this.identifier + "'{" + this.customEntry + "}";
    }

    @Override
    public void serializeTo(JsonArray array, Function<T,ResourceLocation> elementIdentifier){
        array.add(CustomTagEntries.serialize(this));
    }
}
