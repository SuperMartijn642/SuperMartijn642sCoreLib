package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.recipe.condition.RecipeConditionSerializer;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.stats.StatType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

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

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static RegistrationHandler get(String modid){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        String activeMod = ModLoadingContext.get().getActiveNamespace();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName() + "' is requesting registration helper for different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, RegistrationHandler::new);
    }

    private final String modid;
    private final Map<Registries.Registry<?>,Map<ResourceLocation,Supplier<?>>> entryMap = new HashMap<>();
    private final Map<Registries.Registry<?>,List<Consumer<Helper<?>>>> callbacks = new HashMap<>();
    private final Set<Registries.Registry<?>> encounteredEvents = new HashSet<>();

    private RegistrationHandler(String modid){
        this.modid = modid;
        //noinspection unchecked,rawtypes
        Registries.FORGE_REGISTRY_MAP.values().forEach(registry -> this.registerRegistryEventHandler((Registries.Registry)registry));
    }

    @SuppressWarnings("unchecked")
    private <T extends IForgeRegistryEntry<T>> void registerRegistryEventHandler(Registries.Registry<T> registry){
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(registry.getValueClass(), (Consumer<RegistryEvent.Register<T>>)(Object)(Consumer<RegistryEvent.Register<?>>)this::handleRegisterEvent);
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

    public void registerMobEffect(String identifier, Supplier<Effect> effect){
        this.addEntry(Registries.MOB_EFFECTS, identifier, effect);
    }

    public void registerMobEffect(String identifier, Effect effect){
        this.addEntry(Registries.MOB_EFFECTS, identifier, () -> effect);
    }

    public void registerMobEffectOverride(String namespace, String identifier, Supplier<Effect> effect){
        this.addEntry(Registries.MOB_EFFECTS, namespace, identifier, effect);
    }

    public void registerMobEffectOverride(String namespace, String identifier, Effect effect){
        this.addEntry(Registries.MOB_EFFECTS, namespace, identifier, () -> effect);
    }

    public void registerMobEffectCallback(Consumer<Helper<Effect>> callback){
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

    public void registerBlockEntityType(String identifier, Supplier<TileEntityType<?>> blockEntityType){
        this.addEntry(Registries.BLOCK_ENTITY_TYPES, identifier, blockEntityType);
    }

    public void registerBlockEntityType(String identifier, TileEntityType<?> blockEntityType){
        this.addEntry(Registries.BLOCK_ENTITY_TYPES, identifier, () -> blockEntityType);
    }

    public void registerBlockEntityTypeOverride(String namespace, String identifier, Supplier<TileEntityType<?>> blockEntityType){
        this.addEntry(Registries.BLOCK_ENTITY_TYPES, namespace, identifier, blockEntityType);
    }

    public void registerBlockEntityTypeOverride(String namespace, String identifier, TileEntityType<?> blockEntityType){
        this.addEntry(Registries.BLOCK_ENTITY_TYPES, namespace, identifier, () -> blockEntityType);
    }

    public void registerBlockEntityTypeCallback(Consumer<Helper<TileEntityType<?>>> callback){
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

    public void registerMenuType(String identifier, Supplier<ContainerType<?>> menuType){
        this.addEntry(Registries.MENU_TYPES, identifier, menuType);
    }

    public void registerMenuType(String identifier, ContainerType<?> menuType){
        this.addEntry(Registries.MENU_TYPES, identifier, () -> menuType);
    }

    public void registerMenuTypeOverride(String namespace, String identifier, Supplier<ContainerType<?>> menuType){
        this.addEntry(Registries.MENU_TYPES, namespace, identifier, menuType);
    }

    public void registerMenuTypeOverride(String namespace, String identifier, ContainerType<?> menuType){
        this.addEntry(Registries.MENU_TYPES, namespace, identifier, () -> menuType);
    }

    public void registerMenuTypeCallback(Consumer<Helper<ContainerType<?>>> callback){
        this.addCallback(Registries.MENU_TYPES, callback);
    }

    public void registerPaintingVariant(String identifier, Supplier<PaintingType> paintingVariant){
        this.addEntry(Registries.PAINTING_VARIANTS, identifier, paintingVariant);
    }

    public void registerPaintingVariant(String identifier, PaintingType paintingVariant){
        this.addEntry(Registries.PAINTING_VARIANTS, identifier, () -> paintingVariant);
    }

    public void registerPaintingVariantOverride(String namespace, String identifier, Supplier<PaintingType> paintingVariant){
        this.addEntry(Registries.PAINTING_VARIANTS, namespace, identifier, paintingVariant);
    }

    public void registerPaintingVariantOverride(String namespace, String identifier, PaintingType paintingVariant){
        this.addEntry(Registries.PAINTING_VARIANTS, namespace, identifier, () -> paintingVariant);
    }

    public void registerPaintingVariantCallback(Consumer<Helper<PaintingType>> callback){
        this.addCallback(Registries.PAINTING_VARIANTS, callback);
    }

    public void registerRecipeSerializer(String identifier, Supplier<IRecipeSerializer<?>> recipeSerializer){
        this.addEntry(Registries.RECIPE_SERIALIZERS, identifier, recipeSerializer);
    }

    public void registerRecipeSerializer(String identifier, IRecipeSerializer<?> recipeSerializer){
        this.addEntry(Registries.RECIPE_SERIALIZERS, identifier, () -> recipeSerializer);
    }

    public void registerRecipeSerializerOverride(String namespace, String identifier, Supplier<IRecipeSerializer<?>> recipeSerializer){
        this.addEntry(Registries.RECIPE_SERIALIZERS, namespace, identifier, recipeSerializer);
    }

    public void registerRecipeSerializerOverride(String namespace, String identifier, IRecipeSerializer<?> recipeSerializer){
        this.addEntry(Registries.RECIPE_SERIALIZERS, namespace, identifier, () -> recipeSerializer);
    }

    public void registerRecipeSerializerCallback(Consumer<Helper<IRecipeSerializer<?>>> callback){
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

    public void registerConditionSerializer(String identifier, Supplier<IConditionSerializer<?>> recipeSerializer){
        this.addEntry(Registries.RECIPE_CONDITION_SERIALIZERS, identifier, recipeSerializer);
    }

    public void registerConditionSerializer(String identifier, IConditionSerializer<?> recipeSerializer){
        this.addEntry(Registries.RECIPE_CONDITION_SERIALIZERS, identifier, () -> recipeSerializer);
    }

    public void registerConditionSerializerOverride(String namespace, String identifier, Supplier<IConditionSerializer<?>> recipeSerializer){
        this.addEntry(Registries.RECIPE_CONDITION_SERIALIZERS, namespace, identifier, recipeSerializer);
    }

    public void registerConditionSerializerOverride(String namespace, String identifier, IConditionSerializer<?> recipeSerializer){
        this.addEntry(Registries.RECIPE_CONDITION_SERIALIZERS, namespace, identifier, () -> recipeSerializer);
    }

    public void registerConditionSerializerCallback(Consumer<Helper<IConditionSerializer<?>>> callback){
        this.addCallback(Registries.RECIPE_CONDITION_SERIALIZERS, callback);
    }

    public void registerRecipeConditionSerializer(String identifier, Supplier<RecipeConditionSerializer<?>> recipeSerializer){
        this.registerConditionSerializer(identifier, () -> RecipeConditionSerializer.createForgeConditionSerializer(new ResourceLocation(this.modid, identifier), recipeSerializer.get()));
    }

    public void registerRecipeConditionSerializer(String identifier, RecipeConditionSerializer<?> recipeSerializer){
        this.registerConditionSerializer(identifier, () -> RecipeConditionSerializer.createForgeConditionSerializer(new ResourceLocation(this.modid, identifier), recipeSerializer));
    }

    public void registerRecipeConditionSerializerOverride(String namespace, String identifier, Supplier<RecipeConditionSerializer<?>> recipeSerializer){
        this.registerConditionSerializerOverride(namespace, identifier, () -> RecipeConditionSerializer.createForgeConditionSerializer(new ResourceLocation(namespace, identifier), recipeSerializer.get()));
    }

    public void registerRecipeConditionSerializerOverride(String namespace, String identifier, RecipeConditionSerializer<?> recipeSerializer){
        this.registerConditionSerializerOverride(namespace, identifier, () -> RecipeConditionSerializer.createForgeConditionSerializer(new ResourceLocation(namespace, identifier), recipeSerializer));
    }

    public void registerRecipeConditionSerializerCallback(Consumer<Helper<RecipeConditionSerializer<?>>> callback){
        this.registerConditionSerializerCallback(helper -> callback.accept(new Helper<RecipeConditionSerializer<?>>(null) {
            @Override
            public <X extends RecipeConditionSerializer<?>> X register(String identifier, X object){
                helper.register(identifier, RecipeConditionSerializer.createForgeConditionSerializer(new ResourceLocation(RegistrationHandler.this.modid, identifier), object));
                return object;
            }

            @Override
            public <X extends RecipeConditionSerializer<?>> X registerOverride(String namespace, String identifier, X object){
                helper.register(namespace, identifier, RecipeConditionSerializer.createForgeConditionSerializer(new ResourceLocation(namespace, identifier), object));
                return object;
            }
        }));
    }

    private <T> void addEntry(Registries.Registry<T> registry, String identifier, Supplier<T> entry){
        this.addEntry(registry, this.modid, identifier, entry);
    }

    private <T> void addEntry(Registries.Registry<T> registry, String namespace, String identifier, Supplier<T> entry){
        if(this.encounteredEvents.contains(registry))
            throw new IllegalStateException("Cannot register new entries after RegisterEvent has been fired!");
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");
        if(entry == null)
            throw new IllegalArgumentException("Entry supplier for '" + namespace + ":" + identifier + "' must not be null!");

        ResourceLocation fullIdentifier = new ResourceLocation(namespace, identifier);
        Map<ResourceLocation,Supplier<?>> entries = this.entryMap.computeIfAbsent(registry, o -> new HashMap<>());
        if(entries.containsKey(fullIdentifier))
            throw new RuntimeException("Duplicate entry '" + fullIdentifier + "' for registry '" + registry.getVanillaRegistry().key().location() + "'!");

        entries.put(fullIdentifier, entry);
    }

    private <T> void addCallback(Registries.Registry<T> registry, Consumer<Helper<T>> callback){
        if(this.encounteredEvents.contains(registry))
            throw new IllegalStateException("Cannot register callbacks after RegisterEvent has been fired!");
        if(callback == null)
            throw new IllegalArgumentException("Registration callback must not be null!");

        //noinspection unchecked,rawtypes
        this.callbacks.computeIfAbsent(registry, o -> new ArrayList<>()).add((Consumer)callback);
    }

    private void handleRegisterEvent(RegistryEvent.Register<?> event){
        IForgeRegistry<?> underlyingRegistry = event.getRegistry();
        Registries.Registry<?> registry = Registries.fromUnderlying(underlyingRegistry);
        this.encounteredEvents.add(registry);

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
            Map<ResourceLocation,Supplier<?>> entries = RegistrationHandler.this.entryMap.computeIfAbsent(this.registry, o -> new HashMap<>());
            if(entries.containsKey(fullIdentifier))
                throw new RuntimeException("Duplicate entry '" + fullIdentifier + "' for registry '" + this.registry.getVanillaRegistry().key().location() + "'!");

            this.registry.register(fullIdentifier, object);
        }
    }
}
