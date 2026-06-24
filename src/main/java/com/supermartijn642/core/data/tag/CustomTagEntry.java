package com.supermartijn642.core.data.tag;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public interface CustomTagEntry {

    static TagEntry createVanillaEntry(CustomTagEntry customEntry){
        return CustomTagEntries.wrap(customEntry);
    }

    <T> Collection<T> resolve(TagEntryResolutionContext<T> context);

    default Collection<Identifier> getTagDependencies(){
        return Collections.emptyList();
    }

    CustomTagEntrySerializer<?> getSerializer();

    interface TagEntryResolutionContext<T> {
        T getElement(Identifier identifier);

        Collection<T> getTag(Identifier identifier);

        Set<Identifier> getAllIdentifiers();
    }
}
