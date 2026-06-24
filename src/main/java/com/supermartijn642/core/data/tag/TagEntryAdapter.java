package com.supermartijn642.core.data.tag;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public class TagEntryAdapter extends TagEntry {

    public static final ThreadLocal<Registry<?>> REGISTRY_CONTEXT = new ThreadLocal<>();

    final Identifier identifier;
    final CustomTagEntry customEntry;
    private Registry<?> registry;

    TagEntryAdapter(Identifier identifier, CustomTagEntry customEntry){
        super(Identifier.fromNamespaceAndPath("supermartijn642corelib", "dummy"), false, false);
        this.identifier = identifier;
        this.customEntry = customEntry;
    }

    public void setRegistry(Registry<?> registry){
        this.registry = registry;
    }

    @Override
    public <T> boolean build(Lookup<T> lookup, Consumer<T> entryConsumer){
        CustomTagEntry.TagEntryResolutionContext<T> context = new CustomTagEntry.TagEntryResolutionContext<>() {
            @Override
            public T getElement(Identifier identifier){
                return lookup.element(identifier, false);
            }

            @Override
            public Collection<T> getTag(Identifier identifier){
                return lookup.tag(identifier);
            }

            @Override
            public Set<Identifier> getAllIdentifiers(){
                if(TagEntryAdapter.this.registry == null)
                    return Collections.emptySet();
                return TagEntryAdapter.this.registry.keySet();
            }
        };
        Collection<T> entries = this.customEntry.resolve(context);
        if(entries != null)
            entries.forEach(entryConsumer);
        return true;
    }

    @Override
    public void visitOptionalDependencies(Consumer<Identifier> consumer){
        Collection<Identifier> dependencies = this.customEntry.getTagDependencies();
        if(dependencies != null)
            dependencies.forEach(consumer);
    }

    @Override
    public String toString(){
        return "'" + this.identifier + "'{" + this.customEntry + "}";
    }
}
