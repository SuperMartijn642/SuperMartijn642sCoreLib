package com.supermartijn642.core.data.tag;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class TagEntryAdapter extends TagEntry {

    final ResourceLocation identifier;
    final CustomTagEntry customEntry;
    private Registry<?> registry;

    TagEntryAdapter(ResourceLocation identifier, CustomTagEntry customEntry){
        super(ResourceLocation.fromNamespaceAndPath("supermartijn642corelib", "dummy"), false, false);
        this.identifier = identifier;
        this.customEntry = customEntry;
    }

    public void setRegistry(Registry<?> registry){
        this.registry = registry;
    }

    @Override
    public <T> boolean build(Lookup<T> lookup, Consumer<T> entryConsumer){
        CustomTagEntry.TagEntryResolutionContext<T> context = new CustomTagEntry.TagEntryResolutionContext<T>() {
            @Override
            public T getElement(ResourceLocation identifier){
                return lookup.element(identifier, false);
            }

            @Override
            public Collection<T> getTag(ResourceLocation identifier){
                return lookup.tag(identifier);
            }

            @Override
            public Collection<T> getAllElements(){
                //noinspection unchecked
                return (Collection<T>)TagEntryAdapter.this.registry.stream().collect(Collectors.toList());
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
    public String toString(){
        return "'" + this.identifier + "'{" + this.customEntry + "}";
    }
}
