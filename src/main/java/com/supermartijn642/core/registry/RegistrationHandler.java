package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.data.condition.ResourceConditionSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.particles.ParticleType;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class RegistrationHandler {

    /**
     * Contains one registration helper per modid
     */
    private static final Map<String,RegistrationHandler> REGISTRATION_HELPER_MAP = new HashMap<>();
    private static boolean hasBeenRegistered = false;

    @ApiStatus.Internal
    @Deprecated
    public static void registerInternal(){
        hasBeenRegistered = true;
        REGISTRATION_HELPER_MAP.values().forEach(RegistrationHandler::registerAll);
    }

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static synchronized RegistrationHandler get(String modid){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(modid.equals("minecraft"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");
        else{
            ModContainer container = FabricLoader.getInstance().getModContainer(modid).orElse(null);
            if(container == null)
                CoreLib.LOGGER.warn("Mod is requesting registration helper for unknown modid '" + modid + "'!");
        }

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, RegistrationHandler::new);
    }

    private final String modid;
    private final Map<Registries.Registry<?>,Map<ResourceLocation,Supplier<?>>> entryMap = new HashMap<>();
    private final Map<Registries.Registry<?>,List<Consumer<Helper<?>>>> callbacks = new HashMap<>();

    private RegistrationHandler(String modid){
        this.modid = modid;
    }

    public void registerBlock(String identifier, Supplier<Block> block){
        this.addEntry(Registries.BLOCKS, identifier, block);
    }

    public void registerBlock(String identifier, Block block){
        this.addEntry(Registries.BLOCKS, identifier, () -> block);
    }

    public void registerBlockOverride(String namespace, String identifier, Supplier<Block> block){
        this.addEntry(Registries.BLOCKS, namespace, identifier, block);
    }

    public void registerBlockOverride(String namespace, String identifier, Block block){
        this.addEntry(Registries.BLOCKS, namespace, identifier, () -> block);
    }

    public void registerBlockCallback(Consumer<Helper<Block>> callback){
        this.addCallback(Registries.BLOCKS, callback);
    }

    public void registerFluid(String identifier, Supplier<Fluid> fluid){
        this.addEntry(Registries.FLUIDS, identifier, fluid);
    }

    public void registerFluid(String identifier, Fluid fluid){
        this.addEntry(Registries.FLUIDS, identifier, () -> fluid);
    }

    public void registerFluidOverride(String namespace, String identifier, Supplier<Fluid> fluid){
        this.addEntry(Registries.FLUIDS, namespace, identifier, fluid);
    }

    public void registerFluidOverride(String namespace, String identifier, Fluid fluid){
        this.addEntry(Registries.FLUIDS, namespace, identifier, () -> fluid);
    }

    public void registerFluidCallback(Consumer<Helper<Fluid>> callback){
        this.addCallback(Registries.FLUIDS, callback);
    }

    public void registerItem(String identifier, Supplier<Item> item){
        this.addEntry(Registries.ITEMS, identifier, item);
    }

    public void registerItem(String identifier, Item item){
        this.addEntry(Registries.ITEMS, identifier, () -> item);
    }

    public void registerItemOverride(String namespace, String identifier, Supplier<Item> item){
        this.addEntry(Registries.ITEMS, namespace, identifier, item);
    }

    public void registerItemOverride(String namespace, String identifier, Item item){
        this.addEntry(Registries.ITEMS, namespace, identifier, () -> item);
    }

    public void registerItemCallback(Consumer<Helper<Item>> callback){
        this.addCallback(Registries.ITEMS, callback);
    }

    public void registerMobEffect(String identifier, Supplier<MobEffect> effect){
        this.addEntry(Registries.MOB_EFFECTS, identifier, effect);
    }

    public void registerMobEffect(String identifier, MobEffect effect){
        this.addEntry(Registries.MOB_EFFECTS, identifier, () -> effect);
    }

    public void registerMobEffectOverride(String namespace, String identifier, Supplier<MobEffect> effect){
        this.addEntry(Registries.MOB_EFFECTS, namespace, identifier, effect);
    }

    public void registerMobEffectOverride(String namespace, String identifier, MobEffect effect){
        this.addEntry(Registries.MOB_EFFECTS, namespace, identifier, () -> effect);
    }

    public void registerMobEffectCallback(Consumer<Helper<MobEffect>> callback){
        this.addCallback(Registries.MOB_EFFECTS, callback);
    }

    public void registerSoundEvent(String identifier, Supplier<SoundEvent> sound){
        this.addEntry(Registries.SOUND_EVENTS, identifier, sound);
    }

    public void registerSoundEvent(String identifier, SoundEvent sound){
        this.addEntry(Registries.SOUND_EVENTS, identifier, () -> sound);
    }

    public void registerSoundEventOverride(String namespace, String identifier, Supplier<SoundEvent> sound){
        this.addEntry(Registries.SOUND_EVENTS, namespace, identifier, sound);
    }

    public void registerSoundEventOverride(String namespace, String identifier, SoundEvent sound){
        this.addEntry(Registries.SOUND_EVENTS, namespace, identifier, () -> sound);
    }

    public void registerSoundEventCallback(Consumer<Helper<SoundEvent>> callback){
        this.addCallback(Registries.SOUND_EVENTS, callback);
    }

    public void registerPotion(String identifier, Supplier<Potion> potion){
        this.addEntry(Registries.POTIONS, identifier, potion);
    }

    public void registerPotion(String identifier, Potion potion){
        this.addEntry(Registries.POTIONS, identifier, () -> potion);
    }

    public void registerPotionOverride(String namespace, String identifier, Supplier<Potion> potion){
        this.addEntry(Registries.POTIONS, namespace, identifier, potion);
    }

    public void registerPotionOverride(String namespace, String identifier, Potion potion){
        this.addEntry(Registries.POTIONS, namespace, identifier, () -> potion);
    }

    public void registerPotionCallback(Consumer<Helper<Potion>> callback){
        this.addCallback(Registries.POTIONS, callback);
    }

    public void registerEnchantment(String identifier, Supplier<Enchantment> enchantment){
        this.addEntry(Registries.ENCHANTMENTS, identifier, enchantment);
    }

    public void registerEnchantment(String identifier, Enchantment enchantment){
        this.addEntry(Registries.ENCHANTMENTS, identifier, () -> enchantment);
    }

    public void registerEnchantmentOverride(String namespace, String identifier, Supplier<Enchantment> enchantment){
        this.addEntry(Registries.ENCHANTMENTS, namespace, identifier, enchantment);
    }

    public void registerEnchantmentOverride(String namespace, String identifier, Enchantment enchantment){
        this.addEntry(Registries.ENCHANTMENTS, namespace, identifier, () -> enchantment);
    }

    public void registerEnchantmentCallback(Consumer<Helper<Enchantment>> callback){
        this.addCallback(Registries.ENCHANTMENTS, callback);
    }

    public void registerEntityType(String identifier, Supplier<EntityType<?>> entityType){
        this.addEntry(Registries.ENTITY_TYPES, identifier, entityType);
    }

    public void registerEntityType(String identifier, EntityType<?> entityType){
        this.addEntry(Registries.ENTITY_TYPES, identifier, () -> entityType);
    }

    public void registerEntityTypeOverride(String namespace, String identifier, Supplier<EntityType<?>> entityType){
        this.addEntry(Registries.ENTITY_TYPES, namespace, identifier, entityType);
    }

    public void registerEntityTypeOverride(String namespace, String identifier, EntityType<?> entityType){
        this.addEntry(Registries.ENTITY_TYPES, namespace, identifier, () -> entityType);
    }

    public void registerEntityTypeCallback(Consumer<Helper<EntityType<?>>> callback){
        this.addCallback(Registries.ENTITY_TYPES, callback);
    }

    public void registerBlockEntityType(String identifier, Supplier<BlockEntityType<?>> blockEntityType){
        this.addEntry(Registries.BLOCK_ENTITY_TYPES, identifier, blockEntityType);
    }

    public void registerBlockEntityType(String identifier, BlockEntityType<?> blockEntityType){
        this.addEntry(Registries.BLOCK_ENTITY_TYPES, identifier, () -> blockEntityType);
    }

    public void registerBlockEntityTypeOverride(String namespace, String identifier, Supplier<BlockEntityType<?>> blockEntityType){
        this.addEntry(Registries.BLOCK_ENTITY_TYPES, namespace, identifier, blockEntityType);
    }

    public void registerBlockEntityTypeOverride(String namespace, String identifier, BlockEntityType<?> blockEntityType){
        this.addEntry(Registries.BLOCK_ENTITY_TYPES, namespace, identifier, () -> blockEntityType);
    }

    public void registerBlockEntityTypeCallback(Consumer<Helper<BlockEntityType<?>>> callback){
        this.addCallback(Registries.BLOCK_ENTITY_TYPES, callback);
    }

    public void registerParticleType(String identifier, Supplier<ParticleType<?>> particleType){
        this.addEntry(Registries.PARTICLE_TYPES, identifier, particleType);
    }

    public void registerParticleType(String identifier, ParticleType<?> particleType){
        this.addEntry(Registries.PARTICLE_TYPES, identifier, () -> particleType);
    }

    public void registerParticleTypeOverride(String namespace, String identifier, Supplier<ParticleType<?>> particleType){
        this.addEntry(Registries.PARTICLE_TYPES, namespace, identifier, particleType);
    }

    public void registerParticleTypeOverride(String namespace, String identifier, ParticleType<?> particleType){
        this.addEntry(Registries.PARTICLE_TYPES, namespace, identifier, () -> particleType);
    }

    public void registerParticleTypeCallback(Consumer<Helper<ParticleType<?>>> callback){
        this.addCallback(Registries.PARTICLE_TYPES, callback);
    }

    public void registerMenuType(String identifier, Supplier<MenuType<?>> menuType){
        this.addEntry(Registries.MENU_TYPES, identifier, menuType);
    }

    public void registerMenuType(String identifier, MenuType<?> menuType){
        this.addEntry(Registries.MENU_TYPES, identifier, () -> menuType);
    }

    public void registerMenuTypeOverride(String namespace, String identifier, Supplier<MenuType<?>> menuType){
        this.addEntry(Registries.MENU_TYPES, namespace, identifier, menuType);
    }

    public void registerMenuTypeOverride(String namespace, String identifier, MenuType<?> menuType){
        this.addEntry(Registries.MENU_TYPES, namespace, identifier, () -> menuType);
    }

    public void registerMenuTypeCallback(Consumer<Helper<MenuType<?>>> callback){
        this.addCallback(Registries.MENU_TYPES, callback);
    }

    public void registerPaintingVariant(String identifier, Supplier<PaintingVariant> paintingVariant){
        this.addEntry(Registries.PAINTING_VARIANTS, identifier, paintingVariant);
    }

    public void registerPaintingVariant(String identifier, PaintingVariant paintingVariant){
        this.addEntry(Registries.PAINTING_VARIANTS, identifier, () -> paintingVariant);
    }

    public void registerPaintingVariantOverride(String namespace, String identifier, Supplier<PaintingVariant> paintingVariant){
        this.addEntry(Registries.PAINTING_VARIANTS, namespace, identifier, paintingVariant);
    }

    public void registerPaintingVariantOverride(String namespace, String identifier, PaintingVariant paintingVariant){
        this.addEntry(Registries.PAINTING_VARIANTS, namespace, identifier, () -> paintingVariant);
    }

    public void registerPaintingVariantCallback(Consumer<Helper<PaintingVariant>> callback){
        this.addCallback(Registries.PAINTING_VARIANTS, callback);
    }

    public void registerRecipeSerializer(String identifier, Supplier<RecipeSerializer<?>> recipeSerializer){
        this.addEntry(Registries.RECIPE_SERIALIZERS, identifier, recipeSerializer);
    }

    public void registerRecipeSerializer(String identifier, RecipeSerializer<?> recipeSerializer){
        this.addEntry(Registries.RECIPE_SERIALIZERS, identifier, () -> recipeSerializer);
    }

    public void registerRecipeSerializerOverride(String namespace, String identifier, Supplier<RecipeSerializer<?>> recipeSerializer){
        this.addEntry(Registries.RECIPE_SERIALIZERS, namespace, identifier, recipeSerializer);
    }

    public void registerRecipeSerializerOverride(String namespace, String identifier, RecipeSerializer<?> recipeSerializer){
        this.addEntry(Registries.RECIPE_SERIALIZERS, namespace, identifier, () -> recipeSerializer);
    }

    public void registerRecipeSerializerCallback(Consumer<Helper<RecipeSerializer<?>>> callback){
        this.addCallback(Registries.RECIPE_SERIALIZERS, callback);
    }

    public void registerAttribute(String identifier, Supplier<Attribute> attribute){
        this.addEntry(Registries.ATTRIBUTES, identifier, attribute);
    }

    public void registerAttribute(String identifier, Attribute attribute){
        this.addEntry(Registries.ATTRIBUTES, identifier, () -> attribute);
    }

    public void registerAttributeOverride(String namespace, String identifier, Supplier<Attribute> attribute){
        this.addEntry(Registries.ATTRIBUTES, namespace, identifier, attribute);
    }

    public void registerAttributeOverride(String namespace, String identifier, Attribute attribute){
        this.addEntry(Registries.ATTRIBUTES, namespace, identifier, () -> attribute);
    }

    public void registerAttributeCallback(Consumer<Helper<Attribute>> callback){
        this.addCallback(Registries.ATTRIBUTES, callback);
    }

    public void registerStatType(String identifier, Supplier<StatType<?>> statType){
        this.addEntry(Registries.STAT_TYPES, identifier, statType);
    }

    public void registerStatType(String identifier, StatType<?> statType){
        this.addEntry(Registries.STAT_TYPES, identifier, () -> statType);
    }

    public void registerStatTypeOverride(String namespace, String identifier, Supplier<StatType<?>> statType){
        this.addEntry(Registries.STAT_TYPES, namespace, identifier, statType);
    }

    public void registerStatTypeOverride(String namespace, String identifier, StatType<?> statType){
        this.addEntry(Registries.STAT_TYPES, namespace, identifier, () -> statType);
    }

    public void registerStatTypeCallback(Consumer<Helper<StatType<?>>> callback){
        this.addCallback(Registries.STAT_TYPES, callback);
    }

    public void registerResourceConditionSerializer(String identifier, Supplier<ResourceConditionSerializer<?>> conditionSerializer){
        this.addEntry(Registries.RESOURCE_CONDITION_SERIALIZERS, identifier, conditionSerializer);
    }

    public void registerResourceConditionSerializer(String identifier, ResourceConditionSerializer<?> conditionSerializer){
        this.addEntry(Registries.RESOURCE_CONDITION_SERIALIZERS, identifier, () -> conditionSerializer);
    }

    public void registerResourceConditionSerializerOverride(String namespace, String identifier, Supplier<ResourceConditionSerializer<?>> conditionSerializer){
        this.addEntry(Registries.RESOURCE_CONDITION_SERIALIZERS, namespace, identifier, conditionSerializer);
    }

    public void registerResourceConditionSerializerOverride(String namespace, String identifier, ResourceConditionSerializer<?> conditionSerializer){
        this.addEntry(Registries.RESOURCE_CONDITION_SERIALIZERS, namespace, identifier, () -> conditionSerializer);
    }

    public void registerResourceConditionSerializerCallback(Consumer<Helper<ResourceConditionSerializer<?>>> callback){
        this.addCallback(Registries.RESOURCE_CONDITION_SERIALIZERS, callback);
    }

    private <T> void addEntry(Registries.Registry<T> registry, String identifier, Supplier<T> entry){
        this.addEntry(registry, this.modid, identifier, entry);
    }

    private <T> void addEntry(Registries.Registry<T> registry, String namespace, String identifier, Supplier<T> entry){
        if(hasBeenRegistered)
            throw new IllegalStateException("Cannot register new entries after mod initialization!");
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");
        if(entry == null)
            throw new IllegalArgumentException("Entry supplier for '" + namespace + ":" + identifier + "' must not be null!");

        ResourceLocation fullIdentifier = new ResourceLocation(namespace, identifier);
        Map<ResourceLocation,Supplier<?>> entries = this.entryMap.computeIfAbsent(registry, o -> new LinkedHashMap<>());
        if(entries.containsKey(fullIdentifier))
            throw new RuntimeException("Duplicate entry '" + fullIdentifier + "' for registry '" + registry.getRegistryIdentifier() + "'!");

        entries.put(fullIdentifier, entry);
    }

    private <T> void addCallback(Registries.Registry<T> registry, Consumer<Helper<T>> callback){
        if(hasBeenRegistered)
            throw new IllegalStateException("Cannot register new entries after mod initialization!");
        if(callback == null)
            throw new IllegalArgumentException("Registration callback must not be null!");

        //noinspection unchecked,rawtypes
        this.callbacks.computeIfAbsent(registry, o -> new ArrayList<>()).add((Consumer)callback);
    }

    private void registerAll(){
        for(Registries.Registry<?> registry : Registries.REGISTRATION_ORDER)
            this.handleRegistry(registry);
    }

    private <T> void handleRegistry(Registries.Registry<T> registry){
        // Register entries
        if(this.entryMap.containsKey(registry))
            this.registerEntries(registry);

        // Call callbacks
        if(this.callbacks.containsKey(registry))
            this.callCallbacks(registry);
    }

    @SuppressWarnings("unchecked")
    private <T> void registerEntries(Registries.Registry<T> registry){
        Map<ResourceLocation,Supplier<?>> entries = this.entryMap.get(registry);
        for(Map.Entry<ResourceLocation,Supplier<?>> entry : entries.entrySet()){
            T object = (T)entry.getValue().get();
            registry.register(entry.getKey(), object);
        }
    }

    private void callCallbacks(Registries.Registry<?> registry){
        Helper<?> helper = new Helper<>(registry);
        List<Consumer<Helper<?>>> callbacks = this.callbacks.get(registry);
        for(Consumer<Helper<?>> callback : callbacks)
            callback.accept(helper);
    }

    public class Helper<T> {

        private final Registries.Registry<T> registry;

        public Helper(Registries.Registry<T> registry){
            this.registry = registry;
        }

        public <X extends T> X register(String identifier, X object){
            this.register(RegistrationHandler.this.modid, identifier, object);
            return object;
        }

        public <X extends T> X registerOverride(String namespace, String identifier, X object){
            this.register(namespace, identifier, object);
            return object;
        }

        private void register(String namespace, String identifier, T object){
            if(!RegistryUtil.isValidNamespace(namespace))
                throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
            if(!RegistryUtil.isValidPath(identifier))
                throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

            ResourceLocation fullIdentifier = new ResourceLocation(namespace, identifier);
            Map<ResourceLocation,Supplier<?>> entries = RegistrationHandler.this.entryMap.computeIfAbsent(this.registry, o -> new LinkedHashMap<>());
            if(entries.containsKey(fullIdentifier))
                throw new RuntimeException("Duplicate entry '" + fullIdentifier + "' for registry '" + this.registry.getRegistryIdentifier() + "'!");

            this.registry.register(fullIdentifier, object);
        }
    }
}
