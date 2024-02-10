package com.supermartijn642.core.data.tag;

import com.google.gson.JsonArray;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class TagEntryAdapter implements Tag.Entry {

    final ResourceLocation identifier;
    final CustomTagEntry customEntry;
    private Registry<?> registry;

    TagEntryAdapter(ResourceLocation identifier, CustomTagEntry customEntry){
        this.identifier = identifier;
        this.customEntry = customEntry;
    }

    public void setRegistry(Registry<?> registry){
        this.registry = registry;
    }

    @Override
    public <T> boolean build(Function<ResourceLocation,Tag<T>> tagLookup, Function<ResourceLocation,T> elementLookup, Consumer<T> entryConsumer){
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
                return (Collection<T>)TagEntryAdapter.this.registry.holders().collect(Collectors.toList());
            }

            @Override
            public Set<ResourceLocation> getAllIdentifiers(){
                return TagEntryAdapter.this.registry.keySet();
            }
        };
        Collection<T> entries = this.customEntry.resolve(context);
        if(entries != null)
            entries.forEach(entryConsumer);
        return true;
    }

    @Override
    public void visitOptionalDependencies(Consumer<ResourceLocation> consumer){
        Collection<ResourceLocation> dependencies = this.customEntry.getTagDependencies();
        if(dependencies != null)
            dependencies.forEach(consumer);
    }

    @Override
    public boolean verifyIfPresent(Predicate<ResourceLocation> elementTest, Predicate<ResourceLocation> tagTest){
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
