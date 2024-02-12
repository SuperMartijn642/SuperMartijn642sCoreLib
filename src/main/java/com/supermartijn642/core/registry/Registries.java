package com.supermartijn642.core.registry;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.data.tag.CustomTagEntrySerializer;
import com.supermartijn642.core.extensions.RegistrySimpleExtension;
import com.supermartijn642.core.gui.BaseContainerType;
import com.supermartijn642.core.util.MappedSetView;
import com.supermartijn642.core.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public final class Registries {

    static final Map<ResourceLocation,Registry<?>> IDENTIFIER_TO_REGISTRY = new HashMap<>();
    static final Map<IRegistry<ResourceLocation,?>,Registry<?>> VANILLA_REGISTRY_MAP = new HashMap<>();
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
    public static void onRecipeConditionSerializerAdded(ResourceLocation identifier, IConditionFactory serializer){
        ((RecipeConditionSerializerRegistry)RECIPE_CONDITION_SERIALIZERS).onObjectAdded(identifier, serializer);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Registry<T> fromUnderlying(IRegistry<ResourceLocation,T> registry){
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

    public static final Registry<Block> BLOCKS = forge(Block.REGISTRY, ForgeRegistries.BLOCKS, Block.class);
    public static final Registry<Fluid> FLUIDS = new FluidRegistryWrapper();
    public static final Registry<Item> ITEMS = new ForgeRegistryWrapper<Item>(Item.REGISTRY, ForgeRegistries.ITEMS, Item.class) {
        @Override
        public void register(ResourceLocation identifier, Item object){
            super.register(identifier, object);

            // Make sure to set custom model resource location for items as soon as they're registered
            if(CommonUtils.getEnvironmentSide().isClient())
                ClientRegistrationHandler.setItemCustomModelLocation(object);
        }
    };
    public static final Registry<Potion> MOB_EFFECTS = forge(Potion.REGISTRY, ForgeRegistries.POTIONS, Potion.class);
    public static final Registry<SoundEvent> SOUND_EVENTS = forge(SoundEvent.REGISTRY, ForgeRegistries.SOUND_EVENTS, SoundEvent.class);
    public static final Registry<PotionType> POTIONS = forge(PotionType.REGISTRY, ForgeRegistries.POTION_TYPES, PotionType.class);
    public static final Registry<Enchantment> ENCHANTMENTS = forge(Enchantment.REGISTRY, ForgeRegistries.ENCHANTMENTS, Enchantment.class);
    public static final Registry<EntityEntry> ENTITY_TYPES = forge(null, ForgeRegistries.ENTITIES, EntityEntry.class);
    public static final Registry<BaseBlockEntityType<?>> BLOCK_ENTITY_TYPES = new MapBackedRegistry<>(new ResourceLocation("supermartijn642corelib", "block_entity_types"), BaseBlockEntityType.class);
    public static final Registry<Class<? extends TileEntity>> BLOCK_ENTITY_CLASSES = vanilla(new ResourceLocation("block_entities"), TileEntity.REGISTRY, Class.class);
    public static final Registry<BaseContainerType<?>> MENU_TYPES = new MapBackedRegistry<>(new ResourceLocation("supermartijn642corelib", "container_types"), BaseContainerType.class);
    public static final Registry<IConditionFactory> RECIPE_CONDITION_SERIALIZERS = new RecipeConditionSerializerRegistry();
    public static final Registry<CustomTagEntrySerializer<?>> CUSTOM_TAG_ENTRY_SERIALIZERS = new MapBackedRegistry<>(new ResourceLocation("supermartijn642corelib", "custom_tag_entries"), CustomTagEntrySerializer.class);

    static{
        ((RecipeConditionSerializerRegistry)RECIPE_CONDITION_SERIALIZERS).initializeMap();

        // Add all registries which don't have a forge registry
        REGISTRATION_ORDER_MAP.put(ITEMS, Lists.newArrayList(BLOCK_ENTITY_TYPES, BLOCK_ENTITY_CLASSES, FLUIDS));
        REGISTRATION_ORDER_MAP.put(ENTITY_TYPES, Lists.newArrayList(MENU_TYPES, CUSTOM_TAG_ENTRY_SERIALIZERS));
    }

    private static <T> Registry<T> vanilla(ResourceLocation identifier, IRegistry<ResourceLocation,T> registry, Class<? super T> valueClass){
        return new VanillaRegistryWrapper<>(identifier, registry, valueClass);
    }

    @SuppressWarnings("unchecked")
    private static <T extends IForgeRegistryEntry<T>> Registry<T> forge(IRegistry<ResourceLocation,? extends T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
        return new ForgeRegistryWrapper<>((IRegistry<ResourceLocation,T>)registry, forgeRegistry, valueClass);
    }

    public interface Registry<T> {

        ResourceLocation getRegistryIdentifier();

        @Nullable
        IRegistry<ResourceLocation,T> getVanillaRegistry();

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

        private final IRegistry<ResourceLocation,T> registry;
        private final ResourceLocation identifier;
        private final Class<T> valueClass;

        private VanillaRegistryWrapper(ResourceLocation identifier, IRegistry<ResourceLocation,T> registry, Class<? super T> valueClass){
            this.identifier = identifier;
            this.registry = registry;
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            if(!(registry instanceof RegistrySimple)) // Clearable registry should not occur here
                throw new RuntimeException("Registry for type '" + valueClass.getName() + "' is not an instance of RegistrySimple!");

            addRegistry(this);
        }

        @Override
        public ResourceLocation getRegistryIdentifier(){
            return this.identifier;
        }

        @Nullable
        @Deprecated
        public IRegistry<ResourceLocation,T> getVanillaRegistry(){
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
            this.registry.putObject(identifier, object);
        }

        public ResourceLocation getIdentifier(T object){
            return (ResourceLocation)((RegistrySimpleExtension)this.registry).coreLibGetKey(object);
        }

        @Override
        public boolean hasIdentifier(ResourceLocation identifier){
            return this.registry.getKeys().contains(identifier);
        }

        public T getValue(ResourceLocation identifier){
            return this.registry.getObject(identifier);
        }

        public Set<ResourceLocation> getIdentifiers(){
            return this.registry.getKeys();
        }

        public Collection<T> getValues(){
            return ((RegistrySimple<ResourceLocation,T>)this.registry).registryObjects.values();
        }

        public Set<Pair<ResourceLocation,T>> getEntries(){
            return MappedSetView.map(((RegistrySimple<ResourceLocation,T>)this.registry).registryObjects.entrySet(), entry -> Pair.of(entry.getKey(), entry.getValue()));
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

        private final IRegistry<ResourceLocation,T> registry;
        private final IForgeRegistry<T> forgeRegistry;
        private final ResourceLocation identifier;
        private final Class<T> valueClass;

        private ForgeRegistryWrapper(IRegistry<ResourceLocation,T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
            this.registry = registry;
            this.forgeRegistry = forgeRegistry;
            this.identifier = RegistryManager.ACTIVE.getName(forgeRegistry);
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
        public IRegistry<ResourceLocation,T> getVanillaRegistry(){
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
            if(!identifier.equals(object.getRegistryName()))
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
            return MappedSetView.map(this.forgeRegistry.getEntries(), entry -> Pair.of(entry.getKey(), entry.getValue()));
        }

        public Class<T> getValueClass(){
            return this.valueClass;
        }

        @Override
        public int hashCode(){
            return Objects.hashCode(this.registry, this.forgeRegistry, this.valueClass);
        }
    }

    private static class FluidRegistryWrapper implements Registry<Fluid> {

        private static final ResourceLocation IDENTIFIER = new ResourceLocation("fluids");

        @Override
        public ResourceLocation getRegistryIdentifier(){
            return IDENTIFIER;
        }

        @Nullable
        @Override
        public IRegistry<ResourceLocation,Fluid> getVanillaRegistry(){
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
        public void register(ResourceLocation identifier, Fluid object){
            if(!identifier.toString().equals(object.getName()))
                throw new RuntimeException("Mismatched fluid name '" + object.getName() + "' and identifier '" + identifier + "'!");
            FluidRegistry.registerFluid(object);
        }

        @Override
        public ResourceLocation getIdentifier(Fluid object){
            return new ResourceLocation(FluidRegistry.getFluidName(object));
        }

        @Override
        public boolean hasIdentifier(ResourceLocation identifier){
            return FluidRegistry.isFluidRegistered(identifier.toString());
        }

        @Override
        public Fluid getValue(ResourceLocation identifier){
            return FluidRegistry.getFluid(identifier.toString());
        }

        @Override
        public Set<ResourceLocation> getIdentifiers(){
            return MappedSetView.map(FluidRegistry.getRegisteredFluids().keySet(), ResourceLocation::new);
        }

        @Override
        public Collection<Fluid> getValues(){
            return FluidRegistry.getRegisteredFluids().values();
        }

        @Override
        public Set<Pair<ResourceLocation,Fluid>> getEntries(){
            return MappedSetView.map(FluidRegistry.getRegisteredFluids().entrySet(), entry -> Pair.of(new ResourceLocation(entry.getKey()), entry.getValue()));
        }

        @Override
        public Class<Fluid> getValueClass(){
            return Fluid.class;
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
        public IRegistry<ResourceLocation,T> getVanillaRegistry(){
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

            if(object instanceof IForgeRegistryEntry)
                ((IForgeRegistryEntry<?>)object).setRegistryName(identifier);

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

    private static class RecipeConditionSerializerRegistry implements Registry<IConditionFactory> {

        private static final Supplier<Map<ResourceLocation,IConditionFactory>> craftingHelperConditions;

        static{
            try{
                Field field = CraftingHelper.class.getDeclaredField("conditions");
                field.setAccessible(true);
                craftingHelperConditions = () -> {
                    try{
                        //noinspection unchecked
                        return (Map<ResourceLocation,IConditionFactory>)field.get(null);
                    }catch(IllegalAccessException e){
                        throw new RuntimeException(e);
                    }
                };
            }catch(NoSuchFieldException e){
                throw new RuntimeException(e);
            }
        }

        private static final ResourceLocation IDENTIFIER = new ResourceLocation("supermartijn642corelib", "resource_conditions");

        private Map<ResourceLocation,IConditionFactory> identifierToObject;
        private final Map<IConditionFactory,ResourceLocation> objectToIdentifier = new HashMap<>();
        private final Set<Pair<ResourceLocation,IConditionFactory>> entries = new HashSet<>();
        private final Class<IConditionFactory> valueClass = IConditionFactory.class;

        private RecipeConditionSerializerRegistry(){
        }

        private void initializeMap(){
            this.identifierToObject = craftingHelperConditions.get();
        }

        @Override
        public ResourceLocation getRegistryIdentifier(){
            return IDENTIFIER;
        }

        @Nullable
        @Override
        public IRegistry<ResourceLocation,IConditionFactory> getVanillaRegistry(){
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
        public void register(ResourceLocation identifier, IConditionFactory object){
            if(this.identifierToObject.containsKey(identifier))
                throw new RuntimeException("Duplicate registry for identifier '" + identifier + "'!");
            if(this.objectToIdentifier.containsKey(object))
                throw new RuntimeException("Duplicate registry for object under '" + this.objectToIdentifier.get(object) + "' and '" + identifier + "'!");

            CraftingHelper.register(identifier, object);
        }

        public void onObjectAdded(ResourceLocation identifier, IConditionFactory object){
            this.objectToIdentifier.put(object, identifier);
            this.entries.add(Pair.of(identifier, object));
        }

        @Override
        public ResourceLocation getIdentifier(IConditionFactory object){
            return this.objectToIdentifier.get(object);
        }

        @Override
        public boolean hasIdentifier(ResourceLocation identifier){
            return this.identifierToObject.containsKey(identifier);
        }

        @Override
        public IConditionFactory getValue(ResourceLocation identifier){
            return this.identifierToObject.get(identifier);
        }

        @Override
        public Set<ResourceLocation> getIdentifiers(){
            return Collections.unmodifiableSet(this.identifierToObject.keySet());
        }

        @Override
        public Collection<IConditionFactory> getValues(){
            return Collections.unmodifiableCollection(this.objectToIdentifier.keySet());
        }

        @Override
        public Set<Pair<ResourceLocation,IConditionFactory>> getEntries(){
            return Collections.unmodifiableSet(this.entries);
        }

        @Override
        public Class<IConditionFactory> getValueClass(){
            return this.valueClass;
        }
    }
}
