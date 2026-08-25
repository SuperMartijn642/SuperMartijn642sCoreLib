package com.supermartijn642.core.data.condition;

import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

/**
 * TODO properly do tags
 * Created 30/11/2022 by SuperMartijn642
 */
public class TagPopulatedResourceCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    private final Registries.Registry<?> registry;
    private final Identifier tag;

    public TagPopulatedResourceCondition(Registries.Registry<?> registry, Identifier tag){
        if(!registry.hasVanillaRegistry() && !registry.hasForgeRegistry())
            throw new IllegalArgumentException("Registry '" + registry.getRegistryIdentifier() + "' is not supported!");

        this.registry = registry;
        this.tag = tag;
    }

    @Override
    public boolean test(ResourceConditionContext context){
        return context.isTagPopulated(TagKey.create(this.registry.getVanillaRegistry().key(), this.tag));
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    private static class Serializer implements ResourceConditionSerializer<TagPopulatedResourceCondition> {

        @Override
        public void serialize(JsonObject json, TagPopulatedResourceCondition condition){
            json.addProperty("registry", condition.registry.getRegistryIdentifier().toString());
            json.addProperty("tag", condition.tag.toString());
        }

        @Override
        public TagPopulatedResourceCondition deserialize(JsonObject json){
            if(!json.has("registry") || !json.get("registry").isJsonPrimitive() || !json.getAsJsonPrimitive("registry").isString())
                throw new RuntimeException("Condition must have key 'registry' of type string!");
            if(!json.has("tag") || !json.get("tag").isJsonPrimitive() || !json.getAsJsonPrimitive("tag").isString())
                throw new RuntimeException("Condition must have key 'tag' of type string!");
            if(!RegistryUtil.isValidIdentifier(json.get("registry").getAsString()))
                throw new RuntimeException("Value for 'registry' must be a valid identifier!");
            if(!RegistryUtil.isValidIdentifier(json.get("tag").getAsString()))
                throw new RuntimeException("Value for 'tag' must be a valid identifier!");

            Registries.Registry<?> registry = Registries.getRegistry(Identifier.parse(json.get("registry").getAsString()));
            if(registry == null)
                throw new RuntimeException("Could not find a registry with identifier '" + json.get("registry").getAsString() + "'!");

            Identifier tag = Identifier.parse(json.get("tag").getAsString());
            return new TagPopulatedResourceCondition(registry, tag);
        }
    }
}
