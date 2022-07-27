package com.supermartijn642.core.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static net.minecraft.core.Registry.*;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public final class Registries {

    static final Map<net.minecraft.core.Registry<?>,Registry<?>> VANILLA_REGISTRY_MAP = new HashMap<>();

    private static void addRegistry(Registry<?> registry){
        if(VANILLA_REGISTRY_MAP.containsKey(registry.getVanillaRegistry()))
            throw new RuntimeException("Duplicate registry wrapper for objects of type '" + registry.getValueClass() + "'!");

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

    private static <T> Registry<T> vanilla(net.minecraft.core.Registry<T> registry, Class<? super T> valueClass){
        return new VanillaRegistryWrapper<>(registry, valueClass);
    }

    public interface Registry<T> {

        net.minecraft.core.Registry<T> getVanillaRegistry();

        boolean hasForgeRegistry();

        void register(ResourceLocation identifier, T object);

        ResourceLocation getIdentifier(T object);

        boolean hasIdentifier(ResourceLocation identifier);

        T getValue(ResourceLocation identifier);

        Collection<ResourceLocation> getIdentifiers();

        Stream<T> getValues();

        Set<Map.Entry<ResourceKey<T>,T>> getEntries();

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

        @Deprecated
        public net.minecraft.core.Registry<T> getVanillaRegistry(){
            return this.registry;
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

        public Collection<ResourceLocation> getIdentifiers(){
            return this.registry.keySet();
        }

        public Stream<T> getValues(){
            return this.registry.stream();
        }

        public Set<Map.Entry<ResourceKey<T>,T>> getEntries(){
            return this.registry.entrySet();
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
}
