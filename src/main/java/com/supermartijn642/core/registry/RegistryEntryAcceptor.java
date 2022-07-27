package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface RegistryEntryAcceptor {

    String namespace();

    String identifier();

    Registry registry();

    enum Registry {
        BLOCKS(Registries.BLOCKS),
        FLUIDS(Registries.FLUIDS),
        ITEMS(Registries.ITEMS),
        MOB_EFFECTS(Registries.MOB_EFFECTS),
        SOUND_EVENTS(Registries.SOUND_EVENTS),
        POTIONS(Registries.POTIONS),
        ENCHANTMENTS(Registries.ENCHANTMENTS),
        ENTITY_TYPES(Registries.ENTITY_TYPES),
        BLOCK_ENTITY_TYPES(Registries.BLOCK_ENTITY_TYPES),
        PARTICLE_TYPES(Registries.PARTICLE_TYPES),
        MENU_TYPES(Registries.MENU_TYPES),
        PAINTING_VARIANTS(Registries.PAINTING_VARIANTS),
        RECIPE_SERIALIZERS(Registries.RECIPE_SERIALIZERS),
        ATTRIBUTES(Registries.ATTRIBUTES),
        STAT_TYPES(Registries.STAT_TYPES);

        public final Registries.Registry<?> registry;

        Registry(Registries.Registry<?> registry){
            this.registry = registry;
        }
    }

    class Handler {

        private static final Type TYPE = Type.getType(RegistryEntryAcceptor.class);

        private static final Map<Registries.Registry<?>,Map<ResourceLocation,Set<Field>>> FIELDS = new HashMap<>();
        private static final Map<Registries.Registry<?>,Map<ResourceLocation,Set<Method>>> METHODS = new HashMap<>();

        public static void gatherAnnotatedFields(){
            for(EntrypointContainer<ModInitializer> entrypoint : FabricLoader.getInstance().getEntrypointContainers("main", ModInitializer.class)){
                // Fields
                for(Field field : entrypoint.getEntrypoint().getClass().getFields()){
                    RegistryEntryAcceptor annotation = field.getAnnotation(RegistryEntryAcceptor.class);
                    if(annotation == null)
                        continue;

                    String namespace = annotation.namespace();
                    if(!RegistryUtil.isValidNamespace(namespace))
                        throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
                    String identifier = annotation.identifier();
                    if(!RegistryUtil.isValidPath(identifier))
                        throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");
                    Registry registry = annotation.registry();
                    if(registry == null)
                        throw new IllegalArgumentException("Registry must not be null!");

                    // Check if the field is static
                    if(!Modifier.isStatic(field.getModifiers()))
                        throw new RuntimeException("Field must be static!");
                    // Check if the field is non-final
                    if(Modifier.isFinal(field.getModifiers()))
                        throw new RuntimeException("Field must not be final!");
                    // Check if the field has the correct type
                    if(!registry.registry.getValueClass().isAssignableFrom(field.getType()))
                        throw new RuntimeException("Field must have a type assignable from '" + registry.registry.getValueClass().getName() + "'!");

                    // Make the field accessible
                    field.setAccessible(true);

                    // Add the field
                    FIELDS.computeIfAbsent(registry.registry, o -> new HashMap<>())
                        .computeIfAbsent(new ResourceLocation(namespace, identifier), o -> new HashSet<>())
                        .add(field);
                }

                // Methods
                for(Method method : entrypoint.getEntrypoint().getClass().getMethods()){
                    RegistryEntryAcceptor annotation = method.getAnnotation(RegistryEntryAcceptor.class);
                    if(annotation == null)
                        continue;

                    String namespace = annotation.namespace();
                    if(!RegistryUtil.isValidNamespace(namespace))
                        throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
                    String identifier = annotation.identifier();
                    if(!RegistryUtil.isValidPath(identifier))
                        throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");
                    Registry registry = annotation.registry();
                    if(registry == null)
                        throw new IllegalArgumentException("Registry must not be null!");

                    // Check if the method is static
                    if(!Modifier.isStatic(method.getModifiers()))
                        throw new RuntimeException("Method must be static!");
                    // Check if the method has exactly one parameter
                    if(method.getParameterCount() != 1)
                        throw new RuntimeException("Method must have exactly 1 parameter!");
                    // Check if the parameter has the correct type
                    if(!registry.registry.getValueClass().isAssignableFrom(method.getParameterTypes()[0]))
                        throw new RuntimeException("Method parameter must have a type assignable from '" + registry.registry.getValueClass().getName() + "'!");

                    // Make the method accessible
                    method.setAccessible(true);

                    // Add the method
                    METHODS.computeIfAbsent(registry.registry, o -> new HashMap<>())
                        .computeIfAbsent(new ResourceLocation(namespace, identifier), o -> new HashSet<>())
                        .add(method);
                }
            }

            // Register event listeners
            Set<Registries.Registry<?>> registries = new HashSet<>();
            registries.addAll(FIELDS.keySet());
            registries.addAll(METHODS.keySet());
            for(Registries.Registry<?> registry : registries)
                RegistryEntryAddedCallback.event(registry.getVanillaRegistry()).register((rawId, id, object) -> onRegisterEvent(registry, id, object));
        }

        public static void onRegisterEvent(Registries.Registry<?> registry, ResourceLocation identifier, Object object){
            applyToFields(registry, identifier, object);
            applyToMethods(registry, identifier, object);
        }

        private static void applyToFields(Registries.Registry<?> registry, ResourceLocation identifier, Object object){
            if(registry == null || !FIELDS.containsKey(registry))
                return;

            for(Map.Entry<ResourceLocation,Set<Field>> entry : FIELDS.get(registry).entrySet()){
                if(!identifier.equals(entry.getKey()))
                    continue;

                // Apply the value to all fields
                for(Field field : entry.getValue()){
                    // Check if the value can be assigned to the field
                    if(!field.getType().isAssignableFrom(object.getClass())){
                        CoreLib.LOGGER.warn("@RegistryEntryAcceptor field '" + field.getDeclaringClass().getName() + "." + field.getName() + "' for '" + entry.getKey() + "' could not be assigned value of type '" + object.getClass() + "'.");
                        continue;
                    }
                    // Set the field's value
                    try{
                        field.set(null, object);
                    }catch(IllegalAccessException e){
                        CoreLib.LOGGER.error("Encountered an error when trying to apply @RegistryEntryAcceptor annotation on field '" + field.getDeclaringClass().getName() + "." + field.getName() + "'!", e);
                    }
                }
            }
        }

        private static void applyToMethods(Registries.Registry<?> registry, ResourceLocation identifier, Object object){
            if(registry == null || !METHODS.containsKey(registry))
                return;

            for(Map.Entry<ResourceLocation,Set<Method>> entry : METHODS.get(registry).entrySet()){
                if(!identifier.equals(entry.getKey()))
                    continue;

                // Apply the value to all methods
                for(Method method : entry.getValue()){
                    // Check if the value can be passed to the method
                    if(!method.getParameterTypes()[0].isAssignableFrom(object.getClass())){
                        CoreLib.LOGGER.warn("@RegistryEntryAcceptor method '" + method.getDeclaringClass().getName() + "." + method.getName() + "' for '" + entry.getKey() + "' could not be assigned value of type '" + object.getClass() + "'.");
                        continue;
                    }
                    // Set the method's value
                    try{
                        method.invoke(null, object);
                    }catch(InvocationTargetException |
                           IllegalAccessException e){
                        CoreLib.LOGGER.error("Encountered an error when trying to apply @RegistryEntryAcceptor annotation on method '" + method.getDeclaringClass().getName() + "." + method.getName() + "'!", e);
                    }
                }
            }
        }

        public static void reportMissing(){
            Set<Registries.Registry<?>> registries = new HashSet<>();
            registries.addAll(FIELDS.keySet());
            registries.addAll(METHODS.keySet());
            registries.forEach(Handler::reportMissing);
        }

        private static void reportMissing(Registries.Registry<?> registry){
            // Fields
            if(FIELDS.containsKey(registry)){
                for(Map.Entry<ResourceLocation,Set<Field>> entry : FIELDS.get(registry).entrySet()){
                    if(!registry.hasIdentifier(entry.getKey()))
                        CoreLib.LOGGER.warn("Could not find value '" + entry.getKey() + "' in registry '" + registry.getVanillaRegistry().key().location() + "' for @RegistryEntryAcceptor!");
                }
            }
            // Methods
            if(METHODS.containsKey(registry)){
                for(Map.Entry<ResourceLocation,Set<Method>> entry : METHODS.get(registry).entrySet()){
                    if(!registry.hasIdentifier(entry.getKey()))
                        CoreLib.LOGGER.warn("Could not find value '" + entry.getKey() + "' in registry '" + registry.getVanillaRegistry().key().location() + "' for @RegistryEntryAcceptor!");
                }
            }
        }
    }
}
