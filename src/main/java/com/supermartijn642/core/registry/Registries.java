package com.supermartijn642.core.registry;

import net.minecraft.core.Registry;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class Registries<T> {

    static final Map<Registry<?>,Registries<?>> UNDERLYING_REGISTRY_MAP = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Registries<T> fromUnderlying(Registry<T> registry){
        return (Registries<T>)UNDERLYING_REGISTRY_MAP.get(registry);
    }

    public static final Registries<Block> BLOCKS = new Registries<>(Registry.BLOCK, Block.class);
    public static final Registries<Fluid> FLUIDS = new Registries<>(Registry.FLUID, Fluid.class);
    public static final Registries<Item> ITEMS = new Registries<>(Registry.ITEM, Item.class);
    public static final Registries<MobEffect> MOB_EFFECTS = new Registries<>(Registry.MOB_EFFECT, MobEffect.class);
    public static final Registries<SoundEvent> SOUND_EVENTS = new Registries<>(Registry.SOUND_EVENT, SoundEvent.class);
    public static final Registries<Potion> POTIONS = new Registries<>(Registry.POTION, Potion.class);
    public static final Registries<Enchantment> ENCHANTMENTS = new Registries<>(Registry.ENCHANTMENT, Enchantment.class);
    public static final Registries<EntityType<?>> ENTITY_TYPES = new Registries<>(Registry.ENTITY_TYPE, EntityType.class);
    public static final Registries<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new Registries<>(Registry.BLOCK_ENTITY_TYPE, BlockEntityType.class);
    public static final Registries<ParticleType<?>> PARTICLE_TYPES = new Registries<>(Registry.PARTICLE_TYPE, ParticleType.class);
    public static final Registries<MenuType<?>> MENU_TYPES = new Registries<>(Registry.MENU, MenuType.class);
    public static final Registries<Motive> PAINTING_VARIANTS = new Registries<>(Registry.MOTIVE, Motive.class);
    public static final Registries<RecipeSerializer<?>> RECIPE_SERIALIZERS = new Registries<>(Registry.RECIPE_SERIALIZER, RecipeSerializer.class);
    public static final Registries<Attribute> ATTRIBUTES = new Registries<>(Registry.ATTRIBUTE, Attribute.class);
    public static final Registries<StatType<?>> STAT_TYPES = new Registries<>(Registry.STAT_TYPE, StatType.class);

    private final Registry<T> registry;
    private final Class<T> valueClass;

    Registries(Registry<T> registry, Class<? super T> valueClass){
        this.registry = registry;
        this.valueClass = (Class<T>)valueClass;

        UNDERLYING_REGISTRY_MAP.put(registry, this);
    }

    @Deprecated
    public Registry<T> getUnderlying(){
        return this.registry;
    }

    public void register(ResourceLocation identifier, T object){
        Registry.register(this.registry, identifier, object);
    }

    public ResourceLocation getIdentifier(T object){
        return this.registry.getKey(object);
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
}
