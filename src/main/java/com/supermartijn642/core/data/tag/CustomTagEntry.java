package com.supermartijn642.core.data.tag;

import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Created 09/02/2024 by SuperMartijn642
 */
public interface CustomTagEntry {

    <T> Collection<T> resolve(TagEntryResolutionContext<T> context);

    default Collection<ResourceLocation> getTagDependencies(){
        return Collections.emptyList();
    }

    CustomTagEntrySerializer<?> getSerializer();

    interface TagEntryResolutionContext<T> {
        T getElement(ResourceLocation identifier);

        Collection<T> getTag(ResourceLocation identifier);

        Collection<T> getAllElements();

        Set<ResourceLocation> getAllIdentifiers();
    }
}
