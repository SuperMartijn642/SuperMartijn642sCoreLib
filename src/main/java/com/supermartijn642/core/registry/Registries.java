package com.supermartijn642.core.registry;

import com.google.common.collect.Lists;
import com.supermartijn642.core.data.condition.ResourceConditionSerializer;
import com.supermartijn642.core.util.MappedSetView;
import com.supermartijn642.core.util.Pair;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.core.Registry.*;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public final class Registries {

    static final Map<net.minecraft.core.Registry<?>,Registry<?>> VANILLA_REGISTRY_MAP = new HashMap<>();
    static final List<Registry<?>> REGISTRATION_ORDER;

    private static void addRegistry(Registry<?> registry){
        if(registry.hasVanillaRegistry() && VANILLA_REGISTRY_MAP.containsKey(registry.getVanillaRegistry()))
            throw new RuntimeException("Duplicate registry wrapper for objects of type '" + registry.getValueClass() + "'!");

        if(registry.hasVanillaRegistry())
            VANILLA_REGISTRY_MAP.put(registry.getVanillaRegistry(), registry);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Registry<T> fromUnderlying(net.minecraft.core.Registry<T> registry){
        return (Registry<T>)VANILLA_REGISTRY_MAP.get(registry);
    }

    public static final Registry<Block> BLOCKS = vanilla(BLOCK, Block.class);
    public static final Registry<Fluid> FLUIDS = vanilla(FLUID, Fluid.class);
    public static final Registry<Item> ITEMS = vanilla(ITEM, Item.class);
    public static final Registry<MobEffect> MOB_EFFECTS = vanilla(MOB_EFFECT, MobEffect.class);
    public static final Registry<SoundEvent> SOUND_EVENTS = vanilla(SOUND_EVENT, SoundEvent.class);
    public static final Registry<Potion> POTIONS = vanilla(POTION, Potion.class);
    public static final Registry<Enchantment> ENCHANTMENTS = vanilla(ENCHANTMENT, Enchantment.class);
    public static final Registry<EntityType<?>> ENTITY_TYPES = vanilla(ENTITY_TYPE, EntityType.class);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = vanilla(BLOCK_ENTITY_TYPE, BlockEntityType.class);
    public static final Registry<ParticleType<?>> PARTICLE_TYPES = vanilla(PARTICLE_TYPE, ParticleType.class);
    public static final Registry<MenuType<?>> MENU_TYPES = vanilla(MENU, MenuType.class);
    public static final Registry<Motive> PAINTING_VARIANTS = vanilla(MOTIVE, Motive.class);
    public static final Registry<RecipeType<?>> RECIPE_TYPES = vanilla(RECIPE_TYPE, RecipeType.class);
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZERS = vanilla(RECIPE_SERIALIZER, RecipeSerializer.class);
    public static final Registry<Attribute> ATTRIBUTES = vanilla(ATTRIBUTE, Attribute.class);
    public static final Registry<StatType<?>> STAT_TYPES = vanilla(STAT_TYPE, StatType.class);
    public static final Registry<ResourceConditionSerializer<?>> RESOURCE_CONDITION_SERIALIZERS = new MapBackedRegistry<>(ResourceConditionSerializer.class);

    static{
        REGISTRATION_ORDER = Lists.newArrayList(
            Registries.BLOCKS,
            Registries.FLUIDS,
            Registries.ITEMS,
            Registries.MOB_EFFECTS,
            Registries.SOUND_EVENTS,
            Registries.POTIONS,
            Registries.ENCHANTMENTS,
            Registries.ENTITY_TYPES,
            Registries.BLOCK_ENTITY_TYPES,
            Registries.PARTICLE_TYPES,
            Registries.MENU_TYPES,
            Registries.PAINTING_VARIANTS,
            Registries.RECIPE_SERIALIZERS,
            Registries.ATTRIBUTES,
            Registries.STAT_TYPES,
            Registries.RESOURCE_CONDITION_SERIALIZERS
        );
    }

    private static <T> Registry<T> vanilla(net.minecraft.core.Registry<T> registry, Class<? super T> valueClass){
        return new VanillaRegistryWrapper<>(registry, valueClass);
    }

    public interface Registry<T> {

        @Nullable net.minecraft.core.Registry<T> getVanillaRegistry();

        boolean hasVanillaRegistry();

        void register(ResourceLocation identifier, T object);

        ResourceLocation getIdentifier(T object);

        boolean hasIdentifier(ResourceLocation identifier);

        T getValue(ResourceLocation identifier);

        Set<ResourceLocation> getIdentifiers();

        Collection<T> getValues();

        Set<Pair<ResourceLocation,T>> getEntries();

        Class<T> getValueClass();
    }

    private static class VanillaRegistryWrapper<T> implements Registry<T> {

        private final net.minecraft.core.Registry<T> registry;
        private final Class<T> valueClass;

        private VanillaRegistryWrapper(net.minecraft.core.Registry<T> registry, Class<? super T> valueClass){
            this.registry = registry;
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            addRegistry(this);
        }

        @Nullable
        @Deprecated
        public net.minecraft.core.Registry<T> getVanillaRegistry(){
            return this.registry;
        }

        @Override
        public boolean hasVanillaRegistry(){
            return true;
        }

        public void register(ResourceLocation identifier, T object){
            net.minecraft.core.Registry.register(this.registry, identifier, object);
        }

        public ResourceLocation getIdentifier(T object){
            return this.registry.getKey(object);
        }

        @Override
        public boolean hasIdentifier(ResourceLocation identifier){
            return this.registry.containsKey(identifier);
        }

        public T getValue(ResourceLocation identifier){
            return this.registry.get(identifier);
        }

        public Set<ResourceLocation> getIdentifiers(){
            return this.registry.keySet();
        }

        public Collection<T> getValues(){
            return MappedSetView.map(this.registry.entrySet(), Map.Entry::getValue);
        }

        public Set<Pair<ResourceLocation,T>> getEntries(){
            return MappedSetView.map(this.registry.entrySet(), entry -> Pair.of(entry.getKey().location(), entry.getValue()));
        }

        public Class<T> getValueClass(){
            return this.valueClass;
        }

        @Override
        public int hashCode(){
            int result = this.registry.hashCode();
            result = 31 * result + this.valueClass.hashCode();
            return result;
        }
    }

    private static class MapBackedRegistry<T> implements Registry<T> {

        private final Map<ResourceLocation,T> identifierToObject = new HashMap<>();
        private final Map<T,ResourceLocation> objectToIdentifier = new HashMap<>();
        private final Set<Pair<ResourceLocation,T>> entries = new HashSet<>();
        private final Class<T> valueClass;

        private MapBackedRegistry(Class<? super T> valueClass){
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;
        }

        @Nullable
        @Override
        public net.minecraft.core.Registry<T> getVanillaRegistry(){
            return null;
        }

        @Override
        public boolean hasVanillaRegistry(){
            return false;
        }

        @Override
        public void register(ResourceLocation identifier, T object){
            if(this.identifierToObject.containsKey(identifier))
                throw new RuntimeException("Duplicate registry for identifier '" + identifier + "'!");
            if(this.objectToIdentifier.containsKey(object))
                throw new RuntimeException("Duplicate registry for object under '" + this.objectToIdentifier.get(object) + "' and '" + identifier + "'!");
            if(ResourceConditions.get(identifier) != null)
                throw new RuntimeException("A resource condition with identifier '" + identifier + "' has already been registered to Fabric's registry!");

            this.identifierToObject.put(identifier, object);
            this.objectToIdentifier.put(object, identifier);
            this.entries.add(Pair.of(identifier, object));

            RegistryEntryAcceptor.Handler.onRegisterEvent(this, identifier, object);
        }

        @Override
        public ResourceLocation getIdentifier(T object){
            return this.objectToIdentifier.get(object);
        }

        @Override
        public boolean hasIdentifier(ResourceLocation identifier){
            return this.identifierToObject.containsKey(identifier);
        }

        @Override
        public T getValue(ResourceLocation identifier){
            return this.identifierToObject.get(identifier);
        }

        @Override
        public Set<ResourceLocation> getIdentifiers(){
            return Collections.unmodifiableSet(this.identifierToObject.keySet());
        }

        @Override
        public Collection<T> getValues(){
            return Collections.unmodifiableCollection(this.objectToIdentifier.keySet());
        }

        @Override
        public Set<Pair<ResourceLocation,T>> getEntries(){
            return Collections.unmodifiableSet(this.entries);
        }

        @Override
        public Class<T> getValueClass(){
            return this.valueClass;
        }
    }
}
