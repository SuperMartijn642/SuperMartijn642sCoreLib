package com.supermartijn642.core.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.PaintingVariant;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class Registries<T> {

    private static final Map<IForgeRegistry<?>,Registries<?>> UNDERLYING_REGISTRY_MAP = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> Registries<T> fromUnderlying(IForgeRegistry<T> registry){
        return (Registries<T>)UNDERLYING_REGISTRY_MAP.get(registry);
    }

    public static final Registries<Block> BLOCKS = new Registries<>(ForgeRegistries.BLOCKS, Block.class);
    public static final Registries<Fluid> FLUIDS = new Registries<>(ForgeRegistries.FLUIDS, Fluid.class);
    public static final Registries<Item> ITEMS = new Registries<>(ForgeRegistries.ITEMS, Item.class);
    public static final Registries<MobEffect> MOB_EFFECTS = new Registries<>(ForgeRegistries.MOB_EFFECTS, MobEffect.class);
    public static final Registries<SoundEvent> SOUND_EVENTS = new Registries<>(ForgeRegistries.SOUND_EVENTS, SoundEvent.class);
    public static final Registries<Potion> POTIONS = new Registries<>(ForgeRegistries.POTIONS, Potion.class);
    public static final Registries<Enchantment> ENCHANTMENTS = new Registries<>(ForgeRegistries.ENCHANTMENTS, Enchantment.class);
    public static final Registries<EntityType<?>> ENTITY_TYPES = new Registries<>(ForgeRegistries.ENTITY_TYPES, EntityType.class);
    public static final Registries<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new Registries<>(ForgeRegistries.BLOCK_ENTITY_TYPES, BlockEntityType.class);
    public static final Registries<ParticleType<?>> PARTICLE_TYPES = new Registries<>(ForgeRegistries.PARTICLE_TYPES, ParticleType.class);
    public static final Registries<MenuType<?>> MENU_TYPES = new Registries<>(ForgeRegistries.MENU_TYPES, MenuType.class);
    public static final Registries<PaintingVariant> PAINTING_VARIANTS = new Registries<>(ForgeRegistries.PAINTING_VARIANTS, PaintingVariant.class);
    public static final Registries<RecipeType<?>> RECIPE_TYPES = new Registries<>(ForgeRegistries.RECIPE_TYPES, RecipeType.class);
    public static final Registries<RecipeSerializer<?>> RECIPE_SERIALIZERS = new Registries<>(ForgeRegistries.RECIPE_SERIALIZERS, RecipeSerializer.class);
    public static final Registries<Attribute> ATTRIBUTES = new Registries<>(ForgeRegistries.ATTRIBUTES, Attribute.class);
    public static final Registries<StatType<?>> STAT_TYPES = new Registries<>(ForgeRegistries.STAT_TYPES, StatType.class);

    private final IForgeRegistry<T> registry;
    private final Class<T> valueClass;

    Registries(IForgeRegistry<T> registry, Class<? super T> valueClass){
        this.registry = registry;
        this.valueClass = (Class<T>)valueClass;

        UNDERLYING_REGISTRY_MAP.put(registry, this);
    }

    @Deprecated
    public IForgeRegistry<T> getUnderlying(){
        return this.registry;
    }

    public void register(ResourceLocation identifier, T object){
        this.getUnderlying().register(identifier, object);
    }

    public ResourceLocation getIdentifier(T object){
        return this.registry.getKey(object);
    }

    public T getValue(ResourceLocation identifier){
        return this.registry.getValue(identifier);
    }

    public Collection<ResourceLocation> getIdentifiers(){
        return this.registry.getKeys();
    }

    public Stream<T> getValues(){
        return this.registry.getValues().stream();
    }

    public Set<Map.Entry<ResourceKey<T>,T>> getEntries(){
        return this.registry.getEntries();
    }

    public Class<T> getValueClass(){
        return this.valueClass;
    }
}
