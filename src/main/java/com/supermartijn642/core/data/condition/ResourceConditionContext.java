package com.supermartijn642.core.data.condition;

import net.minecraft.core.HolderSet;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;

/**
 * TODO eventually add stuff similar to obtain tags and such
 * <p>
 * Created 14/11/2022 by SuperMartijn642
 */
public class ResourceConditionContext {

    public static final ResourceConditionContext EMPTY = new ResourceConditionContext(null);

    private final RegistryOps.RegistryInfoLookup registryLookup;

    public ResourceConditionContext(RegistryOps.RegistryInfoLookup registryLookup){
        this.registryLookup = registryLookup;
    }

    private <T> HolderSet.Named<T> getTag(TagKey<T> tag){
        return this.registryLookup.lookup(tag.registry())
            .flatMap(i -> i.getter().get(tag))
            .orElse(null);
    }

    public boolean isTagAvailable(TagKey<?> tag){
        return this.getTag(tag) != null;
    }

    public boolean isTagPopulated(TagKey<?> tag){
        HolderSet.Named<?> values = this.getTag(tag);
        return values != null && (!values.isBound() || values.size() > 0);
    }
}
