package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLModIdMappingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

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
        STAT_TYPES(Registries.STAT_TYPES),
        RECIPE_CONDITION_SERIALIZERS(Registries.RECIPE_CONDITION_SERIALIZERS);

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
            for(ModFileScanData scanData : ModList.get().getAllScanData()){
                for(ModFileScanData.AnnotationData annotationData : scanData.getAnnotations()){
                    // Skip other annotations
                    if(!TYPE.equals(annotationData.getAnnotationType()))
                        continue;

                    try{
                        String namespace = (String)annotationData.getAnnotationData().get("namespace");
                        if(!RegistryUtil.isValidNamespace(namespace))
                            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
                        String identifier = (String)annotationData.getAnnotationData().get("identifier");
                        if(!RegistryUtil.isValidPath(identifier))
                            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

                        Registry registry = Registry.valueOf(((ModAnnotation.EnumHolder)annotationData.getAnnotationData().get("registry")).getValue());

                        // Get the class the annotation is located in
                        Class<?> clazz = Class.forName(annotationData.getClassType().getClassName(), false, RegistryEntryAcceptor.class.getClassLoader());

                        // Now get the targeted field or method
                        if(annotationData.getTargetType().equals(ElementType.FIELD)){
                            Field field = clazz.getDeclaredField(annotationData.getMemberName());

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
                        }else if(annotationData.getTargetType().equals(ElementType.METHOD)){
                            Method method = clazz.getDeclaredMethod(annotationData.getMemberName());

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
                        }else
                            throw new RuntimeException("@RegistryEntryAcceptor only supports field and method targets!");
                    }catch(Exception e){
                        throw new RuntimeException("Failed to register @RegistryEntryAcceptor annotation target '" + annotationData.getMemberName() + "' in '" + annotationData.getClassType().getClassName() + "'!", e);
                    }
                }
            }

            // Register event listeners
            FMLJavaModLoadingContext.get().getModEventBus().addListener(Handler::onIdRemapping);
        }

        public static void onRegisterEvent(RegistryEvent.Register<?> e){
            Registries.Registry<?> registry = Registries.fromUnderlying(e.getRegistry());
            if(registry == null)
                return;

            applyToFields(registry);
            applyToMethods(registry);

            for(Registries.Registry<?> otherRegistry : Registries.REGISTRATION_ORDER_MAP.getOrDefault(registry, Collections.emptyList())){
                applyToFields(otherRegistry);
                applyToMethods(otherRegistry);
            }
        }

        public static void onIdRemapping(FMLModIdMappingEvent e){
            FIELDS.keySet().forEach(Handler::applyToFields);
            METHODS.keySet().forEach(Handler::applyToMethods);
        }

        private static <T> void applyToFields(Registries.Registry<T> registry){
            if(registry == null || !FIELDS.containsKey(registry))
                return;

            for(Map.Entry<ResourceLocation,Set<Field>> entry : FIELDS.get(registry).entrySet()){
                // Skip if no value is registered with the identifier
                if(!registry.hasIdentifier(entry.getKey())){
                    if(registry.getForgeRegistry() != null)
                        CoreLib.LOGGER.warn("Could not find value '" + entry.getKey() + "' in registry '" + registry.getForgeRegistry().getRegistryName() + "' for @RegistryEntryAcceptor!");
                    else
                        CoreLib.LOGGER.warn("Could not find value '" + entry.getKey() + "' in registry for type '" + registry.getValueClass().getName() + "' for @RegistryEntryAcceptor!");
                    continue;
                }

                // Get the value
                T value = registry.getValue(entry.getKey());
                // Apply the value to all fields
                for(Field field : entry.getValue()){
                    // Check if the value can be assigned to the field
                    if(!field.getType().isAssignableFrom(value.getClass())){
                        CoreLib.LOGGER.warn("@RegistryEntryAcceptor field '" + field.getDeclaringClass().getName() + "." + field.getName() + "' for '" + entry.getKey() + "' could not be assigned value of type '" + value.getClass() + "'.");
                        continue;
                    }
                    // Set the field's value
                    try{
                        field.set(null, value);
                    }catch(IllegalAccessException e){
                        CoreLib.LOGGER.error("Encountered an error when trying to apply @RegistryEntryAcceptor annotation on field '" + field.getDeclaringClass().getName() + "." + field.getName() + "'!", e);
                    }
                }
            }
        }

        private static <T> void applyToMethods(Registries.Registry<T> registry){
            if(registry == null || !METHODS.containsKey(registry))
                return;

            for(Map.Entry<ResourceLocation,Set<Method>> entry : METHODS.get(registry).entrySet()){
                // Skip if no value is registered with the identifier
                if(!registry.hasIdentifier(entry.getKey())){
                    if(registry.getForgeRegistry() != null)
                        CoreLib.LOGGER.warn("Could not find value '" + entry.getKey() + "' in registry '" + registry.getForgeRegistry().getRegistryName() + "' for @RegistryEntryAcceptor!");
                    else
                        CoreLib.LOGGER.warn("Could not find value '" + entry.getKey() + "' in registry for type '" + registry.getValueClass().getName() + "' for @RegistryEntryAcceptor!");
                    continue;
                }

                // Get the value
                T value = registry.getValue(entry.getKey());
                // Apply the value to all methods
                for(Method method : entry.getValue()){
                    // Check if the value can be passed to the method
                    if(!method.getParameterTypes()[0].isAssignableFrom(value.getClass())){
                        CoreLib.LOGGER.warn("@RegistryEntryAcceptor method '" + method.getDeclaringClass().getName() + "." + method.getName() + "' for '" + entry.getKey() + "' could not be assigned value of type '" + value.getClass() + "'.");
                        continue;
                    }
                    // Set the method's value
                    try{
                        method.invoke(null, value);
                    }catch(InvocationTargetException |
                           IllegalAccessException e){
                        CoreLib.LOGGER.error("Encountered an error when trying to apply @RegistryEntryAcceptor annotation on method '" + method.getDeclaringClass().getName() + "." + method.getName() + "'!", e);
                    }
                }
            }
        }
    }
}
