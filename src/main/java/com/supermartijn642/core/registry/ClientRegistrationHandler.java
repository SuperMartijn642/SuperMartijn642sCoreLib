package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.item.EditableClientItemExtensions;
import com.supermartijn642.core.util.Pair;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class ClientRegistrationHandler {

    /**
     * Contains one registration helper per modid
     */
    private static final Map<String,ClientRegistrationHandler> REGISTRATION_HELPER_MAP = new HashMap<>();

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static ClientRegistrationHandler get(String modid){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        String activeMod = ModLoadingContext.get().getActiveNamespace();
        if(activeMod != null && !activeMod.equals(modid) && !activeMod.equals("minecraft") && !activeMod.equals("forge"))
            CoreLib.LOGGER.warn("Mod '" + ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName() + "' is requesting registration helper for different modid '" + modid + "'!");

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, ClientRegistrationHandler::new);
    }

    private final String modid;
    private final Set<Class<? extends Event>> registeredEvents = new HashSet<>();

    private final Set<ResourceLocation> models = new HashSet<>();
    private final Map<ResourceLocation,Supplier<BakedModel>> specialModels = new HashMap<>();
    private final Map<ResourceLocation,Function<BakedModel,BakedModel>> modelOverwrites = new HashMap<>();

    private final List<Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?>>>> entityRenderers = new ArrayList<>();
    private final List<Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?>>>> blockEntityRenderers = new ArrayList<>();

    private final Map<ResourceLocation,Set<ResourceLocation>> textureAtlasSprites = new HashMap<>();

    private final List<Pair<Supplier<Item>,Supplier<BlockEntityWithoutLevelRenderer>>> customItemRenderers = new ArrayList<>();

    private ClientRegistrationHandler(String modid){
        this.modid = modid;
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    /**
     * Registers the given model location to be loaded from a json file.
     */
    public void registerModel(ResourceLocation identifier){
        if(this.models.contains(identifier))
            throw new RuntimeException("Duplicate model location '" + identifier + "'!");
        if(this.specialModels.containsKey(identifier))
            throw new RuntimeException("Overlapping special model and model location '" + identifier + "'!");

        this.models.add(identifier);

        this.registerModelRegisterAdditionalHandler();
    }

    /**
     * Registers the given model location to be loaded from a json file.
     */
    public void registerModel(String namespace, String identifier){
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        this.registerModel(new ResourceLocation(namespace, identifier));
    }

    /**
     * Registers the given model location to be loaded from a json file.
     */
    public void registerModel(String identifier){
        this.registerModel(this.modid, identifier);
    }

    /**
     * Registers the given baked model under the given identifier. The identifier must not already contain a model.
     */
    public void registerSpecialModel(String identifier, Supplier<BakedModel> model){
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        ResourceLocation fullIdentifier = new ResourceLocation(this.modid, identifier);
        if(this.specialModels.containsKey(fullIdentifier))
            throw new RuntimeException("Duplicate special model entry '" + fullIdentifier + "'!");
        if(this.modelOverwrites.containsKey(fullIdentifier))
            throw new RuntimeException("Overlapping special model and model overwrite '" + fullIdentifier + "'!");

        this.specialModels.put(fullIdentifier, model);

        this.registerModelBakeHandler();
    }

    /**
     * Registers the given baked model under the given identifier. The identifier must not already contain a model.
     */
    public void registerSpecialModel(String identifier, BakedModel model){
        this.registerSpecialModel(identifier, () -> model);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(ResourceLocation identifier, Function<BakedModel,BakedModel> modelOverwrite){
        if(this.modelOverwrites.containsKey(identifier))
            throw new RuntimeException("Duplicate model overwrite '" + identifier + "'!");
        if(this.specialModels.containsKey(identifier))
            throw new RuntimeException("Overlapping special model and model overwrite '" + identifier + "'!");

        this.modelOverwrites.put(identifier, modelOverwrite);

        this.registerModelBakeHandler();
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, String variant, Function<BakedModel,BakedModel> modelOverwrite){
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");
        if(!RegistryUtil.isValidPath(variant))
            throw new IllegalArgumentException("Variant '" + variant + "' must only contain characters [a-z0-9_./-]!");

        ResourceLocation fullIdentifier = new ModelResourceLocation(namespace, identifier, variant);
        this.registerModelOverwrite(fullIdentifier, modelOverwrite);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, Function<BakedModel,BakedModel> modelOverwrite){
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        ResourceLocation fullIdentifier = new ResourceLocation(namespace, identifier);
        this.registerModelOverwrite(fullIdentifier, modelOverwrite);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, String variant, Supplier<BakedModel> modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, variant, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, Supplier<BakedModel> modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, String variant, BakedModel modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, variant, model -> modelOverwrite);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, BakedModel modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, model -> modelOverwrite);
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, Function<EntityRendererProvider.Context,EntityRenderer<T>> entityRenderer){
        this.entityRenderers.add(Pair.of((Supplier<EntityType<?>>)(Object)entityType, (Function<EntityRendererProvider.Context,EntityRenderer<?>>)(Object)entityRenderer));

        this.registerRegisterRenderersHandler();
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, Supplier<EntityRenderer<T>> entityRenderer){
        this.registerEntityRenderer(entityType, context -> entityRenderer.get());
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, EntityRenderer<T> entityRenderer){
        this.registerEntityRenderer(entityType, context -> entityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<T>> blockEntityRenderer){
        this.blockEntityRenderers.add(Pair.of((Supplier<BlockEntityType<?>>)(Object)entityType, (Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?>>)(Object)blockEntityRenderer));

        this.registerRegisterRenderersHandler();
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Supplier<BlockEntityRenderer<T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer.get());
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, BlockEntityRenderer<T> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer);
    }

    /**
     * Adds the given sprite to the given atlas.
     */
    public void registerAtlasSprite(ResourceLocation textureAtlas, String spriteLocation){
        if(textureAtlas == null)
            throw new IllegalArgumentException("Texture atlas must not be null!");
        if(!RegistryUtil.isValidPath(spriteLocation))
            throw new IllegalArgumentException("Sprite location '" + spriteLocation + "' must only contain characters [a-z0-9_./-]!");

        ResourceLocation fullSpriteLocation = new ResourceLocation(this.modid, spriteLocation);
        this.textureAtlasSprites.putIfAbsent(textureAtlas, new HashSet<>());
        if(this.textureAtlasSprites.get(textureAtlas).contains(fullSpriteLocation))
            throw new RuntimeException("Duplicate sprite registration '" + fullSpriteLocation + "' for atlas '" + textureAtlas + "'!");

        this.textureAtlasSprites.get(textureAtlas).add(fullSpriteLocation);

        this.registerTextureStitchHandler();
    }

    /**
     * Registers the given custom item renderer for the given item. The given item must provide an instance of {@link EditableClientItemExtensions} in its {@link Item#initializeClient(Consumer)} method.
     */
    public void registerCustomItemRenderer(Supplier<Item> item, Supplier<BlockEntityWithoutLevelRenderer> customRenderer){
        this.customItemRenderers.add(Pair.of(item, customRenderer));

        this.registerRegisterHandler();
    }

    /**
     * Registers the given custom item renderer for the given item. The given item must provide an instance of {@link EditableClientItemExtensions} in its {@link Item#initializeClient(Consumer)} method.
     */
    public void registerCustomItemRenderer(Supplier<Item> item, BlockEntityWithoutLevelRenderer customRenderer){
        this.registerCustomItemRenderer(item, () -> customRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item. The given item must provide an instance of {@link EditableClientItemExtensions} in its {@link Item#initializeClient(Consumer)} method.
     */
    public void registerCustomItemRenderer(Item item, Supplier<BlockEntityWithoutLevelRenderer> customRenderer){
        this.registerCustomItemRenderer(() -> item, customRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item. The given item must provide an instance of {@link EditableClientItemExtensions} in its {@link Item#initializeClient(Consumer)} method.
     */
    public void registerCustomItemRenderer(Item item, BlockEntityWithoutLevelRenderer customRenderer){
        this.registerCustomItemRenderer(() -> item, () -> customRenderer);
    }

    private void registerModelRegisterAdditionalHandler(){
        this.registerEventHandler(ModelRegistryEvent.class, e -> {
            for(ResourceLocation model : this.models)
                ForgeModelBakery.addSpecialModel(model);
        });
    }

    private void registerModelBakeHandler(){
        this.registerEventHandler(ModelBakeEvent.class, e -> {
            // Special models
            for(Map.Entry<ResourceLocation,Supplier<BakedModel>> entry : this.specialModels.entrySet()){
                ResourceLocation identifier = entry.getKey();
                if(e.getModelRegistry().containsKey(identifier))
                    throw new RuntimeException("Special model '" + identifier + "' is trying to overwrite another model!");

                BakedModel model = entry.getValue().get();
                if(model == null)
                    throw new RuntimeException("Got null object for special model '" + entry.getKey() + "'!");

                e.getModelRegistry().put(entry.getKey(), model);
            }

            // Model overwrites
            for(Map.Entry<ResourceLocation,Function<BakedModel,BakedModel>> entry : this.modelOverwrites.entrySet()){
                ResourceLocation identifier = entry.getKey();
                if(!e.getModelRegistry().containsKey(identifier))
                    throw new RuntimeException("No model registered for model overwrite '" + identifier + "'!");

                BakedModel model = e.getModelRegistry().get(identifier);
                model = entry.getValue().apply(model);
                if(model == null)
                    throw new RuntimeException("Model overwrite '" + identifier + "' returned a null model!");

                e.getModelRegistry().put(identifier, model);
            }
        });
    }

    private void registerRegisterRenderersHandler(){
        this.registerEventHandler(EntityRenderersEvent.RegisterRenderers.class, e -> {
            // Entity renderers
            Set<EntityType<?>> entityTypes = new HashSet<>();
            for(Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?>>> entry : this.entityRenderers){
                EntityType<?> entityType = entry.left().get();
                if(entityType == null)
                    throw new RuntimeException("Entity renderer registered with null entity type!");
                if(entityTypes.contains(entityType))
                    throw new RuntimeException("Duplicate entity renderer for entity type '" + Registries.ENTITY_TYPES.getIdentifier(entityType) + "'!");

                entityTypes.add(entityType);
                //noinspection unchecked,rawtypes,NullableProblems
                e.registerEntityRenderer((EntityType)entityType, (EntityRendererProvider)entry.right()::apply);
            }

            // Entity renderers
            Set<BlockEntityType<?>> blockEntityTypes = new HashSet<>();
            for(Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?>>> entry : this.blockEntityRenderers){
                BlockEntityType<?> blockEntityType = entry.left().get();
                if(blockEntityType == null)
                    throw new RuntimeException("Block entity renderer registered with null block entity type!");
                if(blockEntityTypes.contains(blockEntityType))
                    throw new RuntimeException("Duplicate block entity renderer for block entity type '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(blockEntityType) + "'!");

                blockEntityTypes.add(blockEntityType);
                //noinspection unchecked,rawtypes,NullableProblems
                e.registerBlockEntityRenderer((BlockEntityType)blockEntityType, (BlockEntityRendererProvider)entry.right()::apply);
            }
        });
    }

    private void registerTextureStitchHandler(){
        this.registerEventHandler(TextureStitchEvent.Pre.class, e -> {
            Set<ResourceLocation> sprites = this.textureAtlasSprites.get(e.getAtlas().location());
            if(sprites == null)
                return;

            sprites.forEach(e::addSprite);
        });
    }

    private void registerRegisterHandler(){
        this.registerGenericEventHandler(RegistryEvent.Register.class, EventPriority.LOWEST, e -> {
            // Custom item renderers
            Set<Item> items = new HashSet<>();
            if(Registries.ITEMS.getUnderlying().equals(e.getRegistry())){
                for(Pair<Supplier<Item>,Supplier<BlockEntityWithoutLevelRenderer>> entry : this.customItemRenderers){
                    Item item = entry.left().get();
                    if(item == null)
                        throw new RuntimeException("Custom item renderer registered with null item!");
                    if(items.contains(item))
                        throw new RuntimeException("Duplicate custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "'!");

                    Object renderProperties = item.getRenderPropertiesInternal();
                    if(!(renderProperties instanceof EditableClientItemExtensions))
                        throw new RuntimeException("Cannot register custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "' without EditableClientItemExtensions render properties!");

                    BlockEntityWithoutLevelRenderer customRenderer = entry.right().get();
                    if(customRenderer == null)
                        throw new RuntimeException("Got null custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "'!");

                    items.add(item);
                    ((EditableClientItemExtensions)renderProperties).setCustomRenderer(customRenderer);
                }
            }
        });
    }

    private <T extends Event> void registerEventHandler(Class<T> event, Consumer<T> eventHandler){
        this.registerEventHandler(event, EventPriority.NORMAL, eventHandler);
    }

    private <T extends Event> void registerEventHandler(Class<T> event, EventPriority priority, Consumer<T> eventHandler){
        if(this.registeredEvents.add(event))
            FMLJavaModLoadingContext.get().getModEventBus().addListener(priority, eventHandler);
    }

    private <T extends GenericEvent<?>> void registerGenericEventHandler(Class<T> event, Consumer<T> eventHandler){
        this.registerGenericEventHandler(event, EventPriority.NORMAL, eventHandler);
    }

    private <T extends GenericEvent<? extends F>, F> void registerGenericEventHandler(Class<T> event, EventPriority priority, Consumer<T> eventHandler){
        if(this.registeredEvents.add(event))
            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Object.class, priority, eventHandler);
    }
}
