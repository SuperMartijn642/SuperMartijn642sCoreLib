package com.supermartijn642.core.registry;

import com.google.common.collect.Lists;
import com.supermartijn642.core.data.tag.CustomTagEntrySerializer;
import com.supermartijn642.core.util.MappedSetView;
import com.supermartijn642.core.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.particles.ParticleType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.util.registry.Registry.*;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public final class Registries {

    static final Map<ResourceLocation,Registry<?>> IDENTIFIER_TO_REGISTRY = new HashMap<>();
    static final Map<net.minecraft.util.registry.Registry<?>,Registry<?>> VANILLA_REGISTRY_MAP = new HashMap<>();
    static final Map<IForgeRegistry<?>,Registry<?>> FORGE_REGISTRY_MAP = new HashMap<>();
    /**
     * Each entry is a registry which has a vanilla registry and a list of registries which do not have a vanilla registry.
     */
    static final Map<Registry<?>,List<Registry<?>>> REGISTRATION_ORDER_MAP = new HashMap<>();

    private static void addRegistry(Registry<?> registry){
        if(IDENTIFIER_TO_REGISTRY.containsKey(registry.getRegistryIdentifier()))
            throw new RuntimeException("Duplicate registry registration for identifier '" + registry.getRegistryIdentifier() + "'!");
        if(registry.hasVanillaRegistry() && VANILLA_REGISTRY_MAP.containsKey(registry.getVanillaRegistry()))
            throw new RuntimeException("Duplicate registry wrapper for objects of type '" + registry.getValueClass() + "'!");
        if(registry.hasForgeRegistry() && FORGE_REGISTRY_MAP.containsKey(registry.getForgeRegistry()))
            throw new RuntimeException("Duplicate registry wrapper for objects of type '" + registry.getValueClass() + "'!");

        IDENTIFIER_TO_REGISTRY.put(registry.getRegistryIdentifier(), registry);
        if(registry.hasVanillaRegistry())
            VANILLA_REGISTRY_MAP.put(registry.getVanillaRegistry(), registry);
        if(registry.hasForgeRegistry())
            FORGE_REGISTRY_MAP.put(registry.getForgeRegistry(), registry);
    }

    @Deprecated
    public static void onRecipeConditionSerializerAdded(IConditionSerializer<?> serializer){
        ((RecipeConditionSerializerRegistry)RECIPE_CONDITION_SERIALIZERS).onObjectAdded(serializer);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Registry<T> fromUnderlying(net.minecraft.util.registry.Registry<T> registry){
        return (Registry<T>)VANILLA_REGISTRY_MAP.get(registry);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T extends IForgeRegistryEntry<T>> Registry<T> fromUnderlying(IForgeRegistry<T> registry){
        return (Registry<T>)FORGE_REGISTRY_MAP.get(registry);
    }

    /**
     * Gets the registry registered under the given identifier.
     * @param identifier identifier of the registry
     * @return the registry registered under the given identifier or {@code null} if no registry is registered
     */
    public static Registry<?> getRegistry(ResourceLocation identifier){
        return IDENTIFIER_TO_REGISTRY.get(identifier);
    }

    public static final Registry<Block> BLOCKS = forge(BLOCK, ForgeRegistries.BLOCKS, Block.class);
    public static final Registry<Fluid> FLUIDS = forge(FLUID, ForgeRegistries.FLUIDS, Fluid.class);
    public static final Registry<Item> ITEMS = forge(ITEM, ForgeRegistries.ITEMS, Item.class);
    public static final Registry<Effect> MOB_EFFECTS = forge(MOB_EFFECT, ForgeRegistries.POTIONS, Effect.class);
    public static final Registry<SoundEvent> SOUND_EVENTS = forge(SOUND_EVENT, ForgeRegistries.SOUND_EVENTS, SoundEvent.class);
    public static final Registry<Potion> POTIONS = forge(POTION, ForgeRegistries.POTION_TYPES, Potion.class);
    public static final Registry<Enchantment> ENCHANTMENTS = forge(ENCHANTMENT, ForgeRegistries.ENCHANTMENTS, Enchantment.class);
    public static final Registry<EntityType<?>> ENTITY_TYPES = forge(ENTITY_TYPE, ForgeRegistries.ENTITIES, EntityType.class);
    public static final Registry<TileEntityType<?>> BLOCK_ENTITY_TYPES = forge(BLOCK_ENTITY_TYPE, ForgeRegistries.TILE_ENTITIES, TileEntityType.class);
    public static final Registry<ParticleType<?>> PARTICLE_TYPES = forge(PARTICLE_TYPE, ForgeRegistries.PARTICLE_TYPES, ParticleType.class);
    public static final Registry<ContainerType<?>> MENU_TYPES = forge(MENU, ForgeRegistries.CONTAINERS, ContainerType.class);
    public static final Registry<PaintingType> PAINTING_VARIANTS = forge(MOTIVE, ForgeRegistries.PAINTING_TYPES, PaintingType.class);
    public static final Registry<IRecipeType<?>> RECIPE_TYPES = vanilla(RECIPE_TYPE, IRecipeType.class);
    public static final Registry<IRecipeSerializer<?>> RECIPE_SERIALIZERS = forge(RECIPE_SERIALIZER, ForgeRegistries.RECIPE_SERIALIZERS, IRecipeSerializer.class);
    public static final Registry<Attribute> ATTRIBUTES = forge(ATTRIBUTE, ForgeRegistries.ATTRIBUTES, Attribute.class);
    public static final Registry<StatType<?>> STAT_TYPES = forge(STAT_TYPE, ForgeRegistries.STAT_TYPES, StatType.class);
    public static final Registry<IConditionSerializer<?>> RECIPE_CONDITION_SERIALIZERS = new RecipeConditionSerializerRegistry();
    public static final Registry<CustomTagEntrySerializer<?>> CUSTOM_TAG_ENTRY_SERIALIZERS = new MapBackedRegistry<>(new ResourceLocation("supermartijn642corelib", "custom_tag_entries"), CustomTagEntrySerializer.class);

    static{
        // Add all registries which don't have a forge registry
        REGISTRATION_ORDER_MAP.put(POTIONS, Lists.newArrayList(RECIPE_TYPES));
        REGISTRATION_ORDER_MAP.put(RECIPE_SERIALIZERS, Lists.newArrayList(RECIPE_CONDITION_SERIALIZERS, CUSTOM_TAG_ENTRY_SERIALIZERS));
    }

    private static <T> Registry<T> vanilla(net.minecraft.util.registry.Registry<T> registry, Class<? super T> valueClass){
        return new VanillaRegistryWrapper<>(registry, valueClass);
    }

    private static <T extends IForgeRegistryEntry<T>> Registry<T> forge(net.minecraft.util.registry.Registry<T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
        return new ForgeRegistryWrapper<>(registry, forgeRegistry, valueClass);
    }

    public interface Registry<T> {

        ResourceLocation getRegistryIdentifier();

        @Nullable
        net.minecraft.util.registry.Registry<T> getVanillaRegistry();

        boolean hasVanillaRegistry();

        @Nullable
        <X extends IForgeRegistryEntry<X>> IForgeRegistry<X> getForgeRegistry();

        boolean hasForgeRegistry();

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

        private final net.minecraft.util.registry.Registry<T> registry;
        private final ResourceLocation identifier;
        private final Class<T> valueClass;

        private VanillaRegistryWrapper(net.minecraft.util.registry.Registry<T> registry, Class<? super T> valueClass){
            this.registry = registry;
            this.identifier = registry.key().location();
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            addRegistry(this);
        }

        @Override
        public ResourceLocation getRegistryIdentifier(){
            return this.identifier;
        }

        @Nullable
        @Deprecated
        public net.minecraft.util.registry.Registry<T> getVanillaRegistry(){
            return this.registry;
        }

        @Override
        public boolean hasVanillaRegistry(){
            return true;
        }

        @Nullable
        @Deprecated
        public <X extends IForgeRegistryEntry<X>> IForgeRegistry<X> getForgeRegistry(){
            return null;
        }

        @Override
        public boolean hasForgeRegistry(){
            return false;
        }

        public void register(ResourceLocation identifier, T object){
            net.minecraft.util.registry.Registry.register(this.registry, identifier, object);
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

    private static class ForgeRegistryWrapper<T extends IForgeRegistryEntry<T>> implements Registry<T> {

        private final net.minecraft.util.registry.Registry<T> registry;
        private final IForgeRegistry<T> forgeRegistry;
        private final ResourceLocation identifier;
        private final Class<T> valueClass;

        private ForgeRegistryWrapper(net.minecraft.util.registry.Registry<T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
            this.registry = registry;
            this.forgeRegistry = forgeRegistry;
            this.identifier = forgeRegistry.getRegistryName();
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            addRegistry(this);
        }

        @Override
        public ResourceLocation getRegistryIdentifier(){
            return this.identifier;
        }

        @Nullable
        @Deprecated
        public net.minecraft.util.registry.Registry<T> getVanillaRegistry(){
            return this.registry;
        }

        @Override
        public boolean hasVanillaRegistry(){
            return this.registry != null;
        }

        @Nullable
        @Deprecated
        public <X extends IForgeRegistryEntry<X>> IForgeRegistry<X> getForgeRegistry(){
            //noinspection unchecked
            return (IForgeRegistry<X>)this.forgeRegistry;
        }

        @Override
        public boolean hasForgeRegistry(){
            return true;
        }

        public void register(ResourceLocation identifier, T object){
            object.setRegistryName(identifier);
            this.forgeRegistry.register(object);
        }

        public ResourceLocation getIdentifier(T object){
            return this.forgeRegistry.getKey(object);
        }

        @Override
        public boolean hasIdentifier(ResourceLocation identifier){
            return this.forgeRegistry.containsKey(identifier);
        }

        public T getValue(ResourceLocation identifier){
            return this.forgeRegistry.getValue(identifier);
        }

        public Set<ResourceLocation> getIdentifiers(){
            return this.forgeRegistry.getKeys();
        }

        public Collection<T> getValues(){
            return this.forgeRegistry.getValues();
        }

        public Set<Pair<ResourceLocation,T>> getEntries(){
            return MappedSetView.map(this.forgeRegistry.getEntries(), entry -> Pair.of(entry.getKey().location(), entry.getValue()));
        }

        public Class<T> getValueClass(){
            return this.valueClass;
        }

        @Override
        public int hashCode(){
            int result = this.registry.hashCode();
            result = 31 * result + this.forgeRegistry.hashCode();
            result = 31 * result + this.valueClass.hashCode();
            return result;
        }
    }

    private static class RecipeConditionSerializerRegistry implements Registry<IConditionSerializer<?>> {

        private static final Supplier<Map<ResourceLocation,IConditionSerializer<?>>> craftingHelperConditions;

        static{
            try{
                Field field = CraftingHelper.class.getDeclaredField("conditions");
                field.setAccessible(true);
                craftingHelperConditions = () -> {
                    try{
                        //noinspection unchecked
                        return (Map<ResourceLocation,IConditionSerializer<?>>)field.get(null);
                    }catch(IllegalAccessException e){
                        throw new RuntimeException(e);
                    }
                };
            }catch(NoSuchFieldException e){
                throw new RuntimeException(e);
            }
        }

        private static final ResourceLocation IDENTIFIER = new ResourceLocation("supermartijn642corelib", "resource_conditions");

        private final Map<ResourceLocation,IConditionSerializer<?>> identifierToObject;
        private final Map<IConditionSerializer<?>,ResourceLocation> objectToIdentifier = new HashMap<>();
        private final Set<Pair<ResourceLocation,IConditionSerializer<?>>> entries = new HashSet<>();
        private final Class<IConditionSerializer<?>> valueClass;

        private RecipeConditionSerializerRegistry(){
            this.identifierToObject = craftingHelperConditions.get();
            //noinspection unchecked
            this.valueClass = (Class<IConditionSerializer<?>>)(Object)IConditionSerializer.class;
        }

        @Override
        public ResourceLocation getRegistryIdentifier(){
            return IDENTIFIER;
        }

        @Nullable
        @Override
        public net.minecraft.util.registry.Registry<IConditionSerializer<?>> getVanillaRegistry(){
            return null;
        }

        @Override
        public boolean hasVanillaRegistry(){
            return false;
        }

        @Override
        public @Nullable <X extends IForgeRegistryEntry<X>> IForgeRegistry<X> getForgeRegistry(){
            return null;
        }

        @Override
        public boolean hasForgeRegistry(){
            return false;
        }

        @Override
        public void register(ResourceLocation identifier, IConditionSerializer<?> object){
            if(this.identifierToObject.containsKey(identifier))
                throw new RuntimeException("Duplicate registry for identifier '" + identifier + "'!");
            if(this.objectToIdentifier.containsKey(object))
                throw new RuntimeException("Duplicate registry for object under '" + this.objectToIdentifier.get(object) + "' and '" + identifier + "'!");
            if(!identifier.equals(object.getID()))
                throw new IllegalArgumentException("Condition serializer's id '" + object.getID() + "' does not match the given id '" + identifier + "'!");

            CraftingHelper.register(object);
        }

        public void onObjectAdded(IConditionSerializer<?> object){
            this.objectToIdentifier.put(object, object.getID());
            this.entries.add(Pair.of(object.getID(), object));
        }

        @Override
        public ResourceLocation getIdentifier(IConditionSerializer<?> object){
            return this.objectToIdentifier.get(object);
        }

        @Override
        public boolean hasIdentifier(ResourceLocation identifier){
            return this.identifierToObject.containsKey(identifier);
        }

        @Override
        public IConditionSerializer<?> getValue(ResourceLocation identifier){
            return this.identifierToObject.get(identifier);
        }

        @Override
        public Set<ResourceLocation> getIdentifiers(){
            return Collections.unmodifiableSet(this.identifierToObject.keySet());
        }

        @Override
        public Collection<IConditionSerializer<?>> getValues(){
            return Collections.unmodifiableCollection(this.objectToIdentifier.keySet());
        }

        @Override
        public Set<Pair<ResourceLocation,IConditionSerializer<?>>> getEntries(){
            return Collections.unmodifiableSet(this.entries);
        }

        @Override
        public Class<IConditionSerializer<?>> getValueClass(){
            return this.valueClass;
        }
    }

    private static class MapBackedRegistry<T> implements Registry<T> {

        private final ResourceLocation identifier;
        private final Map<ResourceLocation,T> identifierToObject = new HashMap<>();
        private final Map<T,ResourceLocation> objectToIdentifier = new HashMap<>();
        private final Set<Pair<ResourceLocation,T>> entries = new HashSet<>();
        private final Class<T> valueClass;

        private MapBackedRegistry(ResourceLocation identifier, Class<? super T> valueClass){
            this.identifier = identifier;
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;
        }

        @Override
        public ResourceLocation getRegistryIdentifier(){
            return this.identifier;
        }

        @Nullable
        @Override
        public net.minecraft.util.registry.Registry<T> getVanillaRegistry(){
            return null;
        }

        @Override
        public boolean hasVanillaRegistry(){
            return false;
        }

        @Nullable
        @Override
        public <X extends IForgeRegistryEntry<X>> IForgeRegistry<X> getForgeRegistry(){
            return null;
        }

        @Override
        public boolean hasForgeRegistry(){
            return false;
        }

        @Override
        public void register(ResourceLocation identifier, T object){
            if(this.identifierToObject.containsKey(identifier))
                throw new RuntimeException("Duplicate registry for identifier '" + identifier + "'!");
            if(this.objectToIdentifier.containsKey(object))
                throw new RuntimeException("Duplicate registry for object under '" + this.objectToIdentifier.get(object) + "' and '" + identifier + "'!");

            this.identifierToObject.put(identifier, object);
            this.objectToIdentifier.put(object, identifier);
            this.entries.add(Pair.of(identifier, object));
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
