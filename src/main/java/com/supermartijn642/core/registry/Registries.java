package com.supermartijn642.core.registry;

import com.supermartijn642.core.util.MappedSetView;
import com.supermartijn642.core.util.Pair;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.minecraft.core.Registry.*;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public final class Registries {

    static final Map<net.minecraft.core.Registry<?>,Registry<?>> VANILLA_REGISTRY_MAP = new HashMap<>();
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
    public static <T> Registry<T> fromUnderlying(net.minecraft.core.Registry<T> registry){
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
    public static final Registry<MobEffect> MOB_EFFECTS = forge(MOB_EFFECT, ForgeRegistries.MOB_EFFECTS, MobEffect.class);
    public static final Registry<SoundEvent> SOUND_EVENTS = forge(SOUND_EVENT, ForgeRegistries.SOUND_EVENTS, SoundEvent.class);
    public static final Registry<Potion> POTIONS = forge(POTION, ForgeRegistries.POTIONS, Potion.class);
    public static final Registry<Enchantment> ENCHANTMENTS = forge(ENCHANTMENT, ForgeRegistries.ENCHANTMENTS, Enchantment.class);
    public static final Registry<EntityType<?>> ENTITY_TYPES = forge(ENTITY_TYPE, ForgeRegistries.ENTITIES, EntityType.class);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = forge(BLOCK_ENTITY_TYPE, ForgeRegistries.BLOCK_ENTITIES, BlockEntityType.class);
    public static final Registry<ParticleType<?>> PARTICLE_TYPES = forge(PARTICLE_TYPE, ForgeRegistries.PARTICLE_TYPES, ParticleType.class);
    public static final Registry<MenuType<?>> MENU_TYPES = forge(MENU, ForgeRegistries.CONTAINERS, MenuType.class);
    public static final Registry<Motive> PAINTING_VARIANTS = forge(MOTIVE, ForgeRegistries.PAINTING_TYPES, Motive.class);
    public static final Registry<RecipeType<?>> RECIPE_TYPES = vanilla(RECIPE_TYPE, RecipeType.class);
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZERS = forge(RECIPE_SERIALIZER, ForgeRegistries.RECIPE_SERIALIZERS, RecipeSerializer.class);
    public static final Registry<Attribute> ATTRIBUTES = forge(ATTRIBUTE, ForgeRegistries.ATTRIBUTES, Attribute.class);
    public static final Registry<StatType<?>> STAT_TYPES = forge(STAT_TYPE, ForgeRegistries.STAT_TYPES, StatType.class);

    private static <T> Registry<T> vanilla(net.minecraft.core.Registry<T> registry, Class<? super T> valueClass){
        return new VanillaRegistryWrapper<>(registry, valueClass);
    }

    private static <T extends IForgeRegistryEntry<T>> Registry<T> forge(net.minecraft.core.Registry<T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
        return new ForgeRegistryWrapper<>(registry, forgeRegistry, valueClass);
    }

    public interface Registry<T> {

        @Nullable
        net.minecraft.core.Registry<T> getVanillaRegistry();

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

    private static class ForgeRegistryWrapper<T extends IForgeRegistryEntry<T>> implements Registry<T> {

        private final net.minecraft.core.Registry<T> registry;
        private final IForgeRegistry<T> forgeRegistry;
        private final Class<T> valueClass;

        private ForgeRegistryWrapper(net.minecraft.core.Registry<T> registry, IForgeRegistry<T> forgeRegistry, Class<? super T> valueClass){
            this.registry = registry;
            this.forgeRegistry = forgeRegistry;
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
}
