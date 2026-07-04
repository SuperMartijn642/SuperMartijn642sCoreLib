package com.supermartijn642.core.registry;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.data.tag.CustomTagEntrySerializer;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.BaseItem;
import com.supermartijn642.core.util.MappedSetView;
import com.supermartijn642.core.util.Pair;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.core.registries.BuiltInRegistries.*;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public final class Registries {

    static final Map<Identifier,Registry<?>> IDENTIFIER_TO_REGISTRY = new HashMap<>();
    static final Map<net.minecraft.core.Registry<?>,Registry<?>> VANILLA_REGISTRY_MAP = new HashMap<>();
    static final Map<Identifier,Registry<?>> FORGE_REGISTRY_MAP = new HashMap<>();
    /**
     * Each entry is a registry which has a vanilla registry and a list of registries which do not have a vanilla registry.
     */
    static final Map<Registry<?>,List<Registry<?>>> REGISTRATION_ORDER_MAP = new HashMap<>();

    private static void addRegistry(Registry<?> registry){
        if(IDENTIFIER_TO_REGISTRY.containsKey(registry.getRegistryIdentifier()))
            throw new RuntimeException("Duplicate registry registration for identifier '" + registry.getRegistryIdentifier() + "'!");
        if(registry.hasVanillaRegistry() && VANILLA_REGISTRY_MAP.containsKey(registry.getVanillaRegistry()))
            throw new RuntimeException("Duplicate registry wrapper for objects of type '" + registry.getValueClass() + "'!");
        if(registry.hasForgeRegistry() && FORGE_REGISTRY_MAP.containsKey(registry.getRegistryIdentifier()))
            throw new RuntimeException("Duplicate registry wrapper for objects of type '" + registry.getValueClass() + "'!");

        IDENTIFIER_TO_REGISTRY.put(registry.getRegistryIdentifier(), registry);
        if(registry.hasVanillaRegistry())
            VANILLA_REGISTRY_MAP.put(registry.getVanillaRegistry(), registry);
        if(registry.hasForgeRegistry())
            FORGE_REGISTRY_MAP.put(registry.getRegistryIdentifier(), registry);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Registry<T> fromUnderlying(net.minecraft.core.Registry<T> registry){
        return (Registry<T>)VANILLA_REGISTRY_MAP.get(registry);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Registry<T> fromUnderlying(IForgeRegistry<T> registry){
        Registry<?> r = FORGE_REGISTRY_MAP.get(registry.getRegistryName());
        if(r == null){
            // Core lib might have a registry for the vanilla registry rather than the forge one, so check by name as well
            r = IDENTIFIER_TO_REGISTRY.get(registry.getRegistryName());
        }
        return (Registry<T>)r;
    }

    /**
     * Gets the registry registered under the given identifier.
     * @param identifier identifier of the registry
     * @return the registry registered under the given identifier or {@code null} if no registry is registered
     */
    public static Registry<?> getRegistry(Identifier identifier){
        return IDENTIFIER_TO_REGISTRY.get(identifier);
    }

    public static final Registry<Block> BLOCKS = forge(BLOCK, ForgeRegistries.BLOCKS, Block.class);
    public static final Registry<Fluid> FLUIDS = forge(FLUID, ForgeRegistries.FLUIDS, Fluid.class);
    public static final Registry<Item> ITEMS = new ForgeRegistryWrapper<>(ITEM, ForgeRegistries.ITEMS, Item.class){
        @Override
        public void register(Identifier identifier, Item object){
            super.register(identifier, object);
            if(object instanceof BaseItem)
                ((BaseItem)object).resolveRegistryDependencies();
            if(object instanceof BaseBlockItem)
                ((BaseBlockItem)object).resolveRegistryDependencies();
        }
    };
    public static final Registry<MobEffect> MOB_EFFECTS = forge(MOB_EFFECT, ForgeRegistries.MOB_EFFECTS, MobEffect.class);
    public static final Registry<SoundEvent> SOUND_EVENTS = forge(SOUND_EVENT, ForgeRegistries.SOUND_EVENTS, SoundEvent.class);
    public static final Registry<Potion> POTIONS = forge(POTION, ForgeRegistries.POTIONS, Potion.class);
    public static final Registry<EntityType<?>> ENTITY_TYPES = forge(ENTITY_TYPE, ForgeRegistries.ENTITY_TYPES, EntityType.class);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = forge(BLOCK_ENTITY_TYPE, ForgeRegistries.BLOCK_ENTITY_TYPES, BlockEntityType.class);
    public static final Registry<ParticleType<?>> PARTICLE_TYPES = forge(PARTICLE_TYPE, ForgeRegistries.PARTICLE_TYPES, ParticleType.class);
    public static final Registry<MenuType<?>> MENU_TYPES = forge(MENU, ForgeRegistries.MENU_TYPES, MenuType.class);
    public static final Registry<RecipeType<?>> RECIPE_TYPES = forge(RECIPE_TYPE, ForgeRegistries.RECIPE_TYPES, RecipeType.class);
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZERS = forge(RECIPE_SERIALIZER, ForgeRegistries.RECIPE_SERIALIZERS, RecipeSerializer.class);
    public static final Registry<Attribute> ATTRIBUTES = forge(ATTRIBUTE, ForgeRegistries.ATTRIBUTES, Attribute.class);
    public static final Registry<StatType<?>> STAT_TYPES = forge(STAT_TYPE, ForgeRegistries.STAT_TYPES, StatType.class);
    public static final Registry<MapCodec<? extends ICondition>> RECIPE_CONDITION_SERIALIZERS = forge(ForgeRegistries.CONDITION_SERIALIZERS, ForgeRegistries.Keys.CONDITION_SERIALIZERS.identifier(), MapCodec.class);
    public static final Registry<CustomTagEntrySerializer<?>> CUSTOM_TAG_ENTRY_SERIALIZERS = new MapBackedRegistry<>(Identifier.fromNamespaceAndPath("supermartijn642corelib", "custom_tag_entries"), CustomTagEntrySerializer.class);
    public static final Registry<DataComponentType<?>> DATA_COMPONENT_TYPES = vanilla(DATA_COMPONENT_TYPE, DataComponentType.class);
    public static final Registry<CriterionTrigger<?>> TRIGGER_TYPES = vanilla(BuiltInRegistries.TRIGGER_TYPES, CriterionTrigger.class);

    static{
        // Add all registries which don't have a forge registry
        REGISTRATION_ORDER_MAP.put(RECIPE_SERIALIZERS, Lists.newArrayList(CUSTOM_TAG_ENTRY_SERIALIZERS));
    }

    private static <T> Registry<T> vanilla(net.minecraft.core.Registry<T> registry, Class<? super T> valueClass){
        return new VanillaRegistryWrapper<>(registry, valueClass);
    }

    private static <T> Registry<T> forge(net.minecraft.core.Registry<T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
        return new ForgeRegistryWrapper<>(registry, forgeRegistry, valueClass);
    }

    private static <T> Registry<T> forge(Supplier<IForgeRegistry<T>> forgeRegistry, Identifier identifier, Class<? super T> valueClass){
        return new ForgeRegistryWrapper<>(forgeRegistry, identifier, valueClass);
    }

    public interface Registry<T> {

        Identifier getRegistryIdentifier();

        @Nullable
        net.minecraft.core.Registry<T> getVanillaRegistry();

        boolean hasVanillaRegistry();

        @Nullable
        IForgeRegistry<T> getForgeRegistry();

        boolean hasForgeRegistry();

        void register(Identifier identifier, T object);

        Identifier getIdentifier(T object);

        boolean hasIdentifier(Identifier identifier);

        T getValue(Identifier identifier);

        Set<Identifier> getIdentifiers();

        Collection<T> getValues();

        Set<Pair<Identifier,T>> getEntries();

        Class<T> getValueClass();
    }

    private static class VanillaRegistryWrapper<T> implements Registry<T> {

        private final net.minecraft.core.Registry<T> registry;
        private final Identifier identifier;
        private final Class<T> valueClass;

        private VanillaRegistryWrapper(net.minecraft.core.Registry<T> registry, Class<? super T> valueClass){
            this.registry = registry;
            this.identifier = registry.key().identifier();
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            addRegistry(this);
        }

        @Override
        public Identifier getRegistryIdentifier(){
            return this.identifier;
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

        @Nullable
        @Deprecated
        public IForgeRegistry<T> getForgeRegistry(){
            return null;
        }

        @Override
        public boolean hasForgeRegistry(){
            return false;
        }

        public void register(Identifier identifier, T object){
            net.minecraft.core.Registry.register(this.registry, identifier, object);
        }

        public Identifier getIdentifier(T object){
            return this.registry.getKey(object);
        }

        @Override
        public boolean hasIdentifier(Identifier identifier){
            return this.registry.containsKey(identifier);
        }

        public T getValue(Identifier identifier){
            return this.registry.getValue(identifier);
        }

        public Set<Identifier> getIdentifiers(){
            return this.registry.keySet();
        }

        public Collection<T> getValues(){
            return MappedSetView.map(this.registry.entrySet(), Map.Entry::getValue);
        }

        public Set<Pair<Identifier,T>> getEntries(){
            return MappedSetView.map(this.registry.entrySet(), entry -> Pair.of(entry.getKey().identifier(), entry.getValue()));
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

    private static class ForgeRegistryWrapper<T> implements Registry<T> {

        private final net.minecraft.core.Registry<T> registry;
        private final Supplier<IForgeRegistry<T>> forgeRegistrySupplier;
        private IForgeRegistry<T> forgeRegistry;
        private final Identifier identifier;
        private final Class<T> valueClass;

        private ForgeRegistryWrapper(net.minecraft.core.Registry<T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
            this.registry = registry;
            this.forgeRegistrySupplier = null;
            this.forgeRegistry = forgeRegistry;
            this.identifier = forgeRegistry.getRegistryName();
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            addRegistry(this);
        }

        private ForgeRegistryWrapper(Supplier<IForgeRegistry<T>> forgeRegistry, Identifier identifier, Class<? super T> valueClass){
            this.registry = null;
            this.forgeRegistrySupplier = forgeRegistry;
            this.identifier = identifier;
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            addRegistry(this);
        }

        private IForgeRegistry<T> resolveForgeRegistry(){
            if(this.forgeRegistry == null){
                this.forgeRegistry = this.forgeRegistrySupplier.get();
                if(this.forgeRegistry == null)
                    throw new IllegalStateException("Tried resolving for Forge registry '" + this.identifier + "' too early!");
            }
            return this.forgeRegistry;
        }

        @Override
        public Identifier getRegistryIdentifier(){
            return this.identifier;
        }

        @Nullable
        @Deprecated
        public net.minecraft.core.Registry<T> getVanillaRegistry(){
            return this.registry;
        }

        @Override
        public boolean hasVanillaRegistry(){
            return this.registry != null;
        }

        @Nullable
        @Deprecated
        public IForgeRegistry<T> getForgeRegistry(){
            return this.resolveForgeRegistry();
        }

        @Override
        public boolean hasForgeRegistry(){
            return true;
        }

        public void register(Identifier identifier, T object){
            this.resolveForgeRegistry().register(identifier, object);
        }

        public Identifier getIdentifier(T object){
            return this.resolveForgeRegistry().getKey(object);
        }

        @Override
        public boolean hasIdentifier(Identifier identifier){
            return this.resolveForgeRegistry().containsKey(identifier);
        }

        public T getValue(Identifier identifier){
            return this.resolveForgeRegistry().getValue(identifier);
        }

        public Set<Identifier> getIdentifiers(){
            return this.resolveForgeRegistry().getKeys();
        }

        public Collection<T> getValues(){
            return this.resolveForgeRegistry().getValues();
        }

        public Set<Pair<Identifier,T>> getEntries(){
            return MappedSetView.map(this.resolveForgeRegistry().getEntries(), entry -> Pair.of(entry.getKey().identifier(), entry.getValue()));
        }

        public Class<T> getValueClass(){
            return this.valueClass;
        }

        @Override
        public int hashCode(){
            return this.identifier.hashCode();
        }
    }

    private static class MapBackedRegistry<T> implements Registry<T> {

        private final Identifier identifier;
        private final Map<Identifier,T> identifierToObject = new HashMap<>();
        private final Map<T,Identifier> objectToIdentifier = new HashMap<>();
        private final Set<Pair<Identifier,T>> entries = new HashSet<>();
        private final Class<T> valueClass;

        private MapBackedRegistry(Identifier identifier, Class<? super T> valueClass){
            this.identifier = identifier;
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;
        }

        @Override
        public Identifier getRegistryIdentifier(){
            return this.identifier;
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

        @Nullable
        @Override
        public IForgeRegistry<T> getForgeRegistry(){
            return null;
        }

        @Override
        public boolean hasForgeRegistry(){
            return false;
        }

        @Override
        public void register(Identifier identifier, T object){
            if(this.identifierToObject.containsKey(identifier))
                throw new RuntimeException("Duplicate registry for identifier '" + identifier + "'!");
            if(this.objectToIdentifier.containsKey(object))
                throw new RuntimeException("Duplicate registry for object under '" + this.objectToIdentifier.get(object) + "' and '" + identifier + "'!");

            this.identifierToObject.put(identifier, object);
            this.objectToIdentifier.put(object, identifier);
            this.entries.add(Pair.of(identifier, object));
        }

        @Override
        public Identifier getIdentifier(T object){
            return this.objectToIdentifier.get(object);
        }

        @Override
        public boolean hasIdentifier(Identifier identifier){
            return this.identifierToObject.containsKey(identifier);
        }

        @Override
        public T getValue(Identifier identifier){
            return this.identifierToObject.get(identifier);
        }

        @Override
        public Set<Identifier> getIdentifiers(){
            return Collections.unmodifiableSet(this.identifierToObject.keySet());
        }

        @Override
        public Collection<T> getValues(){
            return Collections.unmodifiableCollection(this.objectToIdentifier.keySet());
        }

        @Override
        public Set<Pair<Identifier,T>> getEntries(){
            return Collections.unmodifiableSet(this.entries);
        }

        @Override
        public Class<T> getValueClass(){
            return this.valueClass;
        }
    }
}
