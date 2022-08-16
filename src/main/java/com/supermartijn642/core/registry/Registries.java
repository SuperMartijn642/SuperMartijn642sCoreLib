package com.supermartijn642.core.registry;

import com.supermartijn642.core.util.MappedSetView;
import com.supermartijn642.core.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
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
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.minecraft.util.registry.Registry.*;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public final class Registries {

    static final Map<net.minecraft.util.registry.Registry<?>,Registry<?>> VANILLA_REGISTRY_MAP = new HashMap<>();
    static final Map<IForgeRegistry<?>,Registry<?>> FORGE_REGISTRY_MAP = new HashMap<>();

    private static void addRegistry(Registry<?> registry){
        if(registry.hasVanillaRegistry() && VANILLA_REGISTRY_MAP.containsKey(registry.getVanillaRegistry()))
            throw new RuntimeException("Duplicate registry wrapper for objects of type '" + registry.getValueClass() + "'!");
        if(registry.hasForgeRegistry() && FORGE_REGISTRY_MAP.containsKey(registry.getForgeRegistry()))
            throw new RuntimeException("Duplicate registry wrapper for objects of type '" + registry.getValueClass() + "'!");

        if(registry.hasVanillaRegistry())
            VANILLA_REGISTRY_MAP.put(registry.getVanillaRegistry(), registry);
        if(registry.hasForgeRegistry())
            FORGE_REGISTRY_MAP.put(registry.getForgeRegistry(), registry);
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
    public static final Registry<StatType<?>> STAT_TYPES = forge(STAT_TYPE, ForgeRegistries.STAT_TYPES, StatType.class);

    private static <T> Registry<T> vanilla(net.minecraft.util.registry.Registry<T> registry, Class<? super T> valueClass){
        return new VanillaRegistryWrapper<>(registry, valueClass);
    }

    @SuppressWarnings("unchecked")
    private static <T extends IForgeRegistryEntry<T>> Registry<T> forge(net.minecraft.util.registry.Registry<? extends T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
        return new ForgeRegistryWrapper<>((net.minecraft.util.registry.Registry<T>)registry, forgeRegistry, valueClass);
    }

    public interface Registry<T> {

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
        private final Class<T> valueClass;

        private VanillaRegistryWrapper(net.minecraft.util.registry.Registry<T> registry, Class<? super T> valueClass){
            this.registry = registry;
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            if(!(registry instanceof SimpleRegistry)) // Clearable registry should not occur here
                throw new RuntimeException("Registry for type '" + valueClass.getName() + "' is not an instance of SimpleRegistry!");

            addRegistry(this);
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
            return ((SimpleRegistry<T>)this.registry).storage.values();
        }

        public Set<Pair<ResourceLocation,T>> getEntries(){
            return MappedSetView.map(((SimpleRegistry<T>)this.registry).storage.entrySet(), entry -> Pair.of(entry.getKey(), entry.getValue()));
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
        private final Class<T> valueClass;

        private ForgeRegistryWrapper(net.minecraft.util.registry.Registry<T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
            this.registry = registry;
            this.forgeRegistry = forgeRegistry;
            //noinspection unchecked
            this.valueClass = (Class<T>)valueClass;

            addRegistry(this);
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
            return MappedSetView.map(this.forgeRegistry.getEntries(), entry -> Pair.of(entry.getKey(), entry.getValue()));
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
}
