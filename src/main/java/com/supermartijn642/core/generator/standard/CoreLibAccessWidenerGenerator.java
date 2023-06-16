package com.supermartijn642.core.generator.standard;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.generator.ResourceType;
import com.supermartijn642.core.registry.RegistryOverrideHandlers;
import com.supermartijn642.core.util.Pair;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created 16/06/2023 by SuperMartijn642
 */
public class CoreLibAccessWidenerGenerator extends ResourceGenerator {

    private final List<String> entries = new ArrayList<>();

    public CoreLibAccessWidenerGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void generate(){
        for(Pair<Class<?>,Class<?>> clazzRequest : RegistryOverrideHandlers.REQUESTED_FIELDS){
            for(Field field : RegistryOverrideHandlers.findFieldsInClass(clazzRequest.left(), clazzRequest.right(), true).get()){
                // Generate an entry
                this.entries.add("mutable field " + field.getDeclaringClass().getName().replace('.', '/') + " " + field.getName() + " " + field.getType().describeConstable().get().descriptorString());
            }
        }
    }

    @Override
    public void save(){
        this.entries.sort(String::compareTo);
        Optional<String> optional = this.entries.stream().reduce((a, b) -> a + '\n' + b);
        optional.ifPresent(concatenation -> {
            this.cache.saveResource(ResourceType.DATA, concatenation.getBytes(StandardCharsets.UTF_8), this.modid, "accesswidener", "generated-entries", ".accesswidener");
        });
    }
}
