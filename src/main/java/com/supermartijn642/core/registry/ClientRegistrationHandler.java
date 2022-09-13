package com.supermartijn642.core.registry;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.EditableBlockRenderLayer;
import com.supermartijn642.core.extensions.TileEntityRendererDispatcherExtension;
import com.supermartijn642.core.gui.BaseContainerType;
import com.supermartijn642.core.gui.ContainerScreenManager;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.CustomItemRenderer;
import com.supermartijn642.core.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class ClientRegistrationHandler {

    static void setItemCustomModelLocation(Item item){
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Registries.ITEMS.getIdentifier(item), "inventory"));
    }

    /**
     * Do not use!
     */
    @Deprecated
    public static void registerAllSpecialModels(Consumer<ResourceLocation> consumer){
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.handleModelRegistry(consumer));
    }

    /**
     * Do not use!
     */
    @Deprecated
    public static void registerAllRenderers(){
        REGISTRATION_HELPER_MAP.values().forEach(ClientRegistrationHandler::registerRenderers);
    }

    /**
     * Contains one registration helper per modid
     */
    private static final Map<String,ClientRegistrationHandler> REGISTRATION_HELPER_MAP = new HashMap<>();

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static synchronized ClientRegistrationHandler get(String modid){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        String activeMod = Loader.instance().activeModContainer() == null ? null : Loader.instance().activeModContainer().getModId();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + Loader.instance().activeModContainer().getName() + "' is requesting registration helper for different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, ClientRegistrationHandler::new);
    }

    private final String modid;

    private final Set<ResourceLocation> models = new HashSet<>();
    private final Map<ResourceLocation,Supplier<IBakedModel>> specialModels = new HashMap<>();
    private final List<Pair<Predicate<ResourceLocation>,Function<IBakedModel,IBakedModel>>> modelOverwrites = new ArrayList<>();

    private final Map<Class<?>,Supplier<Render<?>>> entityRenderers = new HashMap<>();
    private final List<Pair<Supplier<BaseBlockEntityType<?>>,Function<TileEntityRendererDispatcher,TileEntitySpecialRenderer<?>>>> blockEntityRenderers = new ArrayList<>();
    private final Map<Class<?>,Function<TileEntityRendererDispatcher,TileEntitySpecialRenderer<?>>> classBlockEntityRenderers = new HashMap<>();

    private final Map<ResourceLocation,Set<ResourceLocation>> textureAtlasSprites = new HashMap<>();

    private final List<Pair<Supplier<Item>,Supplier<TileEntityItemStackRenderer>>> customItemRenderers = new ArrayList<>();

    private final List<Pair<Supplier<BaseContainerType<?>>,Function<Container,GuiContainer>>> containerScreens = new ArrayList<>();
    private final List<Pair<Supplier<Block>,BlockRenderLayer>> blockRenderTypes = new ArrayList<>();

    private boolean passedModelRegistry;
    private boolean passedModelBake;
    private boolean passedClientSetup;
    private boolean passedTextureStitch;

    private ClientRegistrationHandler(String modid){
        this.modid = modid;
        MinecraftForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void handleModelBakeEvent(ModelBakeEvent e){
                ClientRegistrationHandler.this.handleModelBakeEvent(e);
            }

            @SubscribeEvent
            public void handleTextureStitchEvent(TextureStitchEvent.Pre e){
                ClientRegistrationHandler.this.handleTextureStitchEvent(e);
            }
        });
    }

    /**
     * Registers the given model location to be loaded from a json file.
     */
    public void registerModel(ResourceLocation identifier){
        if(this.passedModelRegistry)
            throw new IllegalStateException("Cannot register new models after ModelRegistryEvent has been fired!");
        if(this.models.contains(identifier))
            throw new RuntimeException("Duplicate model location '" + identifier + "'!");
        if(this.specialModels.containsKey(identifier))
            throw new RuntimeException("Overlapping special model and model location '" + identifier + "'!");

        this.models.add(identifier);
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
    public void registerSpecialModel(String identifier, Supplier<IBakedModel> model){
        if(this.passedModelBake)
            throw new IllegalStateException("Cannot register new special models after ModelBakeEvent has been fired!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        ResourceLocation fullIdentifier = new ResourceLocation(this.modid, identifier);
        if(this.specialModels.containsKey(fullIdentifier))
            throw new RuntimeException("Duplicate special model entry '" + fullIdentifier + "'!");

        this.specialModels.put(fullIdentifier, model);
    }

    /**
     * Registers the given baked model under the given identifier. The identifier must not already contain a model.
     */
    public void registerSpecialModel(String identifier, IBakedModel model){
        this.registerSpecialModel(identifier, () -> model);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(ResourceLocation identifier, Function<IBakedModel,IBakedModel> modelOverwrite){
        if(this.passedModelBake)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");
        if(this.specialModels.containsKey(identifier))
            throw new RuntimeException("Overlapping special model and model overwrite '" + identifier + "'!");

        if(!(identifier instanceof ModelResourceLocation))
            identifier = new ResourceLocation(identifier.toString());
        this.modelOverwrites.add(Pair.of(identifier::equals, modelOverwrite));
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, String variant, Function<IBakedModel,IBakedModel> modelOverwrite){
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");
        if(!RegistryUtil.isValidPath(variant))
            throw new IllegalArgumentException("Variant '" + variant + "' must only contain characters [a-z0-9_./-]!");

        ResourceLocation fullIdentifier = new ModelResourceLocation(namespace + ":" + identifier + "#" + variant);
        this.registerModelOverwrite(fullIdentifier, modelOverwrite);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, Function<IBakedModel,IBakedModel> modelOverwrite){
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
    public void registerModelOverwrite(String namespace, String identifier, String variant, Supplier<IBakedModel> modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, variant, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, Supplier<IBakedModel> modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, String variant, IBakedModel modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, variant, model -> modelOverwrite);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, IBakedModel modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, model -> modelOverwrite);
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, Function<IBakedModel,IBakedModel> modelOverwrite){
        if(this.passedModelBake)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        this.modelOverwrites.add(Pair.of(identifier -> {
            ResourceLocation blockIdentifier = Registries.BLOCKS.getIdentifier(block.get());
            return identifier.getResourceDomain().equals(blockIdentifier.getResourceDomain());
        }, modelOverwrite));
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, Supplier<IBakedModel> modelOverwrite){
        this.registerBlockModelOverwrite(block, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, IBakedModel modelOverwrite){
        this.registerBlockModelOverwrite(block, model -> modelOverwrite);
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, Function<IBakedModel,IBakedModel> modelOverwrite){
        if(this.passedModelBake)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        this.modelOverwrites.add(Pair.of(identifier -> {
            ResourceLocation itemIdentifier = Registries.ITEMS.getIdentifier(item.get());
            return identifier.getResourceDomain().equals(itemIdentifier.getResourceDomain())
                && identifier.getResourcePath().equals(itemIdentifier.getResourcePath())
                && ((ModelResourceLocation)identifier).getVariant().equals("inventory");
        }, modelOverwrite));
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, Supplier<IBakedModel> modelOverwrite){
        this.registerItemModelOverwrite(item, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, IBakedModel modelOverwrite){
        this.registerItemModelOverwrite(item, model -> modelOverwrite);
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Entity> void registerEntityRenderer(Class<T> entityClass, Supplier<Render<? super T>> entityRenderer){
        if(this.classBlockEntityRenderers.containsKey(entityClass))
            throw new RuntimeException("Duplicate block entity renderer for block entity class '" + entityClass.getCanonicalName() + "'!");
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new renderers after item registry event has been fired!");

        this.entityRenderers.put(entityClass, (Supplier<Render<?>>)(Object)entityRenderer);
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    public <T extends Entity> void registerEntityRenderer(Class<T> entityClass, Render<? super T> entityRenderer){
        this.registerEntityRenderer(entityClass, () -> entityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends TileEntity> void registerBlockEntityRenderer(Class<T> entityClass, Function<TileEntityRendererDispatcher,TileEntitySpecialRenderer<? super T>> blockEntityRenderer){
        if(this.classBlockEntityRenderers.containsKey(entityClass))
            throw new RuntimeException("Duplicate block entity renderer for block entity class '" + entityClass.getCanonicalName() + "'!");
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new renderers after item registry event has been fired!");

        this.classBlockEntityRenderers.put(entityClass, (Function<TileEntityRendererDispatcher,TileEntitySpecialRenderer<?>>)(Object)blockEntityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends TileEntity> void registerBlockEntityRenderer(Class<T> entityClass, Supplier<TileEntitySpecialRenderer<? super T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityClass, context -> blockEntityRenderer.get());
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends TileEntity> void registerBlockEntityRenderer(Class<T> entityClass, TileEntitySpecialRenderer<? super T> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityClass, context -> blockEntityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends TileEntity> void registerCustomBlockEntityRenderer(Class<T> entityClass, Supplier<CustomBlockEntityRenderer<? super T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityClass, context -> CustomBlockEntityRenderer.of(blockEntityRenderer.get()));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends TileEntity> void registerCustomBlockEntityRenderer(Class<T> entityClass, CustomBlockEntityRenderer<? super T> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityClass, context -> CustomBlockEntityRenderer.of(blockEntityRenderer));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseBlockEntity> void registerBlockEntityRenderer(Supplier<BaseBlockEntityType<T>> entityType, Function<TileEntityRendererDispatcher,TileEntitySpecialRenderer<? super T>> blockEntityRenderer){
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new renderers after RegisterRenderers has been fired!");

        this.blockEntityRenderers.add(Pair.of((Supplier<BaseBlockEntityType<?>>)(Object)entityType, (Function<TileEntityRendererDispatcher,TileEntitySpecialRenderer<?>>)(Object)blockEntityRenderer));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BaseBlockEntity> void registerBlockEntityRenderer(Supplier<BaseBlockEntityType<T>> entityType, Supplier<TileEntitySpecialRenderer<? super T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer.get());
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BaseBlockEntity> void registerBlockEntityRenderer(Supplier<BaseBlockEntityType<T>> entityType, TileEntitySpecialRenderer<? super T> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BaseBlockEntity> void registerCustomBlockEntityRenderer(Supplier<BaseBlockEntityType<T>> entityType, Supplier<CustomBlockEntityRenderer<? super T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> CustomBlockEntityRenderer.of(blockEntityRenderer.get()));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BaseBlockEntity> void registerCustomBlockEntityRenderer(Supplier<BaseBlockEntityType<T>> entityType, CustomBlockEntityRenderer<? super T> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> CustomBlockEntityRenderer.of(blockEntityRenderer));
    }

    /**
     * Adds the given sprite to the given atlas.
     */
    public void registerAtlasSprite(ResourceLocation textureAtlas, String spriteLocation){
        if(this.passedTextureStitch)
            throw new IllegalStateException("Cannot register new models after TextureStitchEvent has been fired!");
        if(textureAtlas == null)
            throw new IllegalArgumentException("Texture atlas must not be null!");
        if(!RegistryUtil.isValidPath(spriteLocation))
            throw new IllegalArgumentException("Sprite location '" + spriteLocation + "' must only contain characters [a-z0-9_./-]!");

        ResourceLocation fullSpriteLocation = new ResourceLocation(this.modid, spriteLocation);
        this.textureAtlasSprites.putIfAbsent(textureAtlas, new HashSet<>());
        if(this.textureAtlasSprites.get(textureAtlas).contains(fullSpriteLocation))
            throw new RuntimeException("Duplicate sprite registration '" + fullSpriteLocation + "' for atlas '" + textureAtlas + "'!");

        this.textureAtlasSprites.get(textureAtlas).add(fullSpriteLocation);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Supplier<Item> item, Supplier<TileEntityItemStackRenderer> itemRenderer){
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new renderers after item RegistryEvent has been fired!");

        this.customItemRenderers.add(Pair.of(item, itemRenderer));
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Supplier<Item> item, TileEntityItemStackRenderer itemRenderer){
        this.registerItemRenderer(item, () -> itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Item item, Supplier<TileEntityItemStackRenderer> itemRenderer){
        this.registerItemRenderer(() -> item, itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Item item, TileEntityItemStackRenderer itemRenderer){
        this.registerItemRenderer(() -> item, () -> itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerCustomItemRenderer(Supplier<Item> item, Supplier<CustomItemRenderer> itemRenderer){
        this.registerItemRenderer(item, () -> CustomItemRenderer.of(itemRenderer.get()));
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerCustomItemRenderer(Supplier<Item> item, CustomItemRenderer itemRenderer){
        this.registerItemRenderer(item, () -> CustomItemRenderer.of(itemRenderer));
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerCustomItemRenderer(Item item, Supplier<CustomItemRenderer> itemRenderer){
        this.registerItemRenderer(() -> item, () -> CustomItemRenderer.of(itemRenderer.get()));
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerCustomItemRenderer(Item item, CustomItemRenderer itemRenderer){
        this.registerItemRenderer(() -> item, () -> CustomItemRenderer.of(itemRenderer));
    }

    /**
     * Registers the given screen constructor for the given menu type.
     */
    public <T extends Container, U extends GuiContainer> void registerContainerScreen(Supplier<BaseContainerType<T>> menuType, Function<T,U> screenSupplier){
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new menu screens after the ClientInitialization event has been fired!");

        //noinspection unchecked
        this.containerScreens.add(Pair.of((Supplier<BaseContainerType<?>>)(Object)menuType, (Function<Container,GuiContainer>)(Object)screenSupplier));
    }

    /**
     * Registers the given screen constructor for the given menu type.
     */
    public <T extends Container, U extends GuiContainer> void registerContainerScreen(BaseContainerType<T> menuType, Function<T,U> screenSupplier){
        this.registerContainerScreen(() -> menuType, screenSupplier);
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     */
    public void registerBlockModelRenderType(Supplier<Block> block, BlockRenderLayer renderType){
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new menu screens after the ClientInitialization event has been fired!");

        this.blockRenderTypes.add(Pair.of(block, renderType));
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     */
    public void registerBlockModelRenderType(Block block, BlockRenderLayer renderType){
        this.registerBlockModelRenderType(() -> block, renderType);
    }

    /**
     * Registers the solid render type to be used when rendering the given block.
     */
    public void registerBlockModelSolidRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, BlockRenderLayer.SOLID);
    }

    /**
     * Registers the solid render type to be used when rendering the given block.
     */
    public void registerBlockModelSolidRenderType(Block block){
        this.registerBlockModelRenderType(block, BlockRenderLayer.SOLID);
    }

    /**
     * Registers the cutout mipped render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutMippedRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, BlockRenderLayer.CUTOUT_MIPPED);
    }

    /**
     * Registers the cutout mipped render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutMippedRenderType(Block block){
        this.registerBlockModelRenderType(block, BlockRenderLayer.CUTOUT_MIPPED);
    }

    /**
     * Registers the cutout render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, BlockRenderLayer.CUTOUT);
    }

    /**
     * Registers the cutout render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutRenderType(Block block){
        this.registerBlockModelRenderType(block, BlockRenderLayer.CUTOUT);
    }

    /**
     * Registers the translucent render type to be used when rendering the given block.
     */
    public void registerBlockModelTranslucentRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, BlockRenderLayer.TRANSLUCENT);
    }

    /**
     * Registers the translucent render type to be used when rendering the given block.
     */
    public void registerBlockModelTranslucentRenderType(Block block){
        this.registerBlockModelRenderType(block, BlockRenderLayer.TRANSLUCENT);
    }

    private void handleModelRegistry(Consumer<ResourceLocation> consumer){
        this.passedModelRegistry = true;

        // Additional models
        for(ResourceLocation model : this.models)
            consumer.accept(model);
    }

    private void handleModelBakeEvent(ModelBakeEvent e){
        this.passedModelBake = true;

        // Special models
        for(Map.Entry<ResourceLocation,Supplier<IBakedModel>> entry : this.specialModels.entrySet()){
            ResourceLocation identifier = entry.getKey();
            ModelResourceLocation modelIdentifier = identifier instanceof ModelResourceLocation ? (ModelResourceLocation)identifier : new ModelResourceLocation(identifier.toString());
            if(e.getModelRegistry().getObject(modelIdentifier) != null)
                throw new RuntimeException("Special model '" + identifier + "' is trying to overwrite another model!");

            IBakedModel model = entry.getValue().get();
            if(model == null)
                throw new RuntimeException("Got null object for special model '" + entry.getKey() + "'!");

            e.getModelRegistry().putObject(modelIdentifier, model);
        }

        // Model overwrites
        for(Pair<Predicate<ResourceLocation>,Function<IBakedModel,IBakedModel>> pair : this.modelOverwrites){
            // Find all the identifiers which should be replaced
            List<ModelResourceLocation> modelIdentifiers = e.getModelRegistry().getKeys().stream()
                .filter(identifier -> pair.left().test(identifier))
                .collect(Collectors.toList());

            for(ModelResourceLocation identifier : modelIdentifiers){
                if(e.getModelRegistry().getObject(identifier) == null)
                    throw new RuntimeException("No model registered for model overwrite '" + identifier + "'!");

                IBakedModel model = e.getModelRegistry().getObject(identifier);
                model = pair.right().apply(model);
                if(model == null)
                    throw new RuntimeException("Model overwrite for '" + identifier + "' returned a null model!");

                e.getModelRegistry().putObject(identifier, model);
            }
        }
    }

    private void registerRenderers(){
        this.passedClientSetup = true;

        // Entity renderers
        for(Map.Entry<Class<?>,Supplier<Render<?>>> entry : this.entityRenderers.entrySet()){
            Class<?> entityClass = entry.getKey();

            Render<?> entityRenderer = entry.getValue().get();
            if(entityRenderer == null)
                throw new RuntimeException("Got null entity renderer for entity class '" + entityClass.getCanonicalName() + "!");

            //noinspection unchecked,rawtypes
            ClientUtils.getMinecraft().getRenderManager().entityRenderMap.put((Class)entityClass, entityRenderer);
        }

        // Block entity renderers
        Set<BaseBlockEntityType<?>> blockEntityTypes = new HashSet<>();
        for(Pair<Supplier<BaseBlockEntityType<?>>,Function<TileEntityRendererDispatcher,TileEntitySpecialRenderer<?>>> entry : this.blockEntityRenderers){
            BaseBlockEntityType<?> blockEntityType = entry.left().get();
            if(blockEntityType == null)
                throw new RuntimeException("Block entity renderer registered with null block entity type!");
            if(blockEntityTypes.contains(blockEntityType))
                throw new RuntimeException("Duplicate block entity renderer for block entity type '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(blockEntityType) + "'!");

            TileEntitySpecialRenderer<?> entityRenderer = entry.right().apply(TileEntityRendererDispatcher.instance);
            if(entityRenderer == null)
                throw new RuntimeException("Got null block entity renderer for block entity type '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(blockEntityType) + "'!");

            blockEntityTypes.add(blockEntityType);
            //noinspection unchecked,rawtypes
            ((TileEntityRendererDispatcherExtension)TileEntityRendererDispatcher.instance).coreLibRegisterRenderer((BaseBlockEntityType)blockEntityType, (TileEntitySpecialRenderer)entityRenderer);
        }
        for(Map.Entry<Class<?>,Function<TileEntityRendererDispatcher,TileEntitySpecialRenderer<?>>> entry : this.classBlockEntityRenderers.entrySet()){
            Class<?> blockEntityClass = entry.getKey();

            TileEntitySpecialRenderer<?> entityRenderer = entry.getValue().apply(TileEntityRendererDispatcher.instance);
            if(entityRenderer == null)
                throw new RuntimeException("Got null block entity renderer for block entity class '" + blockEntityClass.getCanonicalName() + "'!");

            //noinspection unchecked,rawtypes
            ClientRegistry.bindTileEntitySpecialRenderer((Class)blockEntityClass, (TileEntitySpecialRenderer)entityRenderer);
        }

        // Custom item renderers
        Set<Item> items = new HashSet<>();
        for(Pair<Supplier<Item>,Supplier<TileEntityItemStackRenderer>> entry : this.customItemRenderers){
            Item item = entry.left().get();
            if(item == null)
                throw new RuntimeException("Custom item renderer registered with null item!");
            if(items.contains(item))
                throw new RuntimeException("Duplicate custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "'!");
            if(item.getTileEntityItemStackRenderer() != TileEntityItemStackRenderer.instance)
                throw new RuntimeException("Item '" + Registries.ITEMS.getIdentifier(item) + "' already has a custom item renderer set!");

            TileEntityItemStackRenderer customRenderer = entry.right().get();
            if(customRenderer == null)
                throw new RuntimeException("Got null custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "'!");

            items.add(item);
            item.setTileEntityItemStackRenderer(customRenderer);
        }

        // Container Screens
        Set<BaseContainerType<?>> menuTypes = new HashSet<>();
        for(Pair<Supplier<BaseContainerType<?>>,Function<Container,GuiContainer>> entry : this.containerScreens){
            BaseContainerType<?> menuType = entry.left().get();
            if(menuType == null)
                throw new RuntimeException("Container screen registered with null menu type!");
            if(menuTypes.contains(menuType))
                throw new RuntimeException("Duplicate container screen for menu type '" + Registries.MENU_TYPES.getIdentifier(menuType) + "'!");

            menuTypes.add(menuType);
            //noinspection unchecked,rawtypes
            ContainerScreenManager.registerContainerScreen((BaseContainerType)menuType, entry.right());
        }

        // Block render types
        Set<Block> blocks = new HashSet<>();
        for(Pair<Supplier<Block>,BlockRenderLayer> entry : this.blockRenderTypes){
            Block block = entry.left().get();
            if(block == null)
                throw new RuntimeException("Block render type registered for null block!");
            if(blocks.contains(block))
                throw new RuntimeException("Duplicate render type for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");
            if(!(block instanceof EditableBlockRenderLayer))
                throw new RuntimeException("Block '" + Registries.BLOCKS.getIdentifier(block) + "' must implement EditableBlockRenderLayer to set it's render type!");
            BlockRenderLayer renderType = entry.right();
            if(renderType == null)
                throw new RuntimeException("Got null render type for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");

            blocks.add(block);
            ((EditableBlockRenderLayer)block).setRenderLayer(renderType);
        }
    }

    private void handleTextureStitchEvent(TextureStitchEvent.Pre e){
        this.passedTextureStitch = true;

        // Texture atlas sprites
        Set<ResourceLocation> sprites = this.textureAtlasSprites.entrySet()
            .stream()
            .filter(entry -> ClientUtils.getTextureManager().getTexture(entry.getKey()) == e.getMap())
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

        sprites.forEach(e.getMap()::registerSprite);
    }
}
