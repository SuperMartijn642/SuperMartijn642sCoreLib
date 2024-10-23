package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.CustomItemRenderer;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.core.util.TriFunction;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.ApiStatus;

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

    /**
     * Contains one registration helper per modid
     */
    private static final Map<String,ClientRegistrationHandler> REGISTRATION_HELPER_MAP = new HashMap<>();
    private static boolean haveRenderersBeenRegistered = false;
    private static boolean haveModelsBeenRegistered = false;

    @ApiStatus.Internal
    public static void registerRenderersInternal(){
        haveRenderersBeenRegistered = true;
        REGISTRATION_HELPER_MAP.values().forEach(ClientRegistrationHandler::registerRenderers);
    }

    @ApiStatus.Internal
    public static void registerModelOverwritesInternal(Map<ModelResourceLocation,BakedModel> modelRegistry){
        haveModelsBeenRegistered = true;
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.registerModelOverwrites(modelRegistry));
    }

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static synchronized ClientRegistrationHandler get(String modid){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        if(modid.equals("minecraft"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");
        else{
            ModContainer container = FabricLoader.getInstance().getModContainer(modid).orElse(null);
            if(container == null)
                CoreLib.LOGGER.warn("Mod is requesting registration helper for unknown modid '" + modid + "'!");
        }

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, ClientRegistrationHandler::new);
    }

    @ApiStatus.Internal
    public static void collectSprites(ResourceLocation atlas, Consumer<ResourceLocation> spriteConsumer){
        for(ClientRegistrationHandler value : REGISTRATION_HELPER_MAP.values())
            value.addSprites(atlas, spriteConsumer);
    }

    private final String modid;

    private final Set<ResourceLocation> models = new HashSet<>();
    private final Map<ModelResourceLocation,Supplier<BakedModel>> specialModels = new HashMap<>();
    private final List<Pair<Predicate<ModelResourceLocation>,Function<BakedModel,BakedModel>>> modelOverwrites = new ArrayList<>();

    private final List<Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?,?>>>> entityRenderers = new ArrayList<>();
    private final List<Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?>>>> blockEntityRenderers = new ArrayList<>();

    private final Map<ResourceLocation,Set<ResourceLocation>> textureAtlasSprites = new HashMap<>();

    private final List<Pair<Supplier<Item>,Supplier<BuiltinItemRendererRegistry.DynamicItemRenderer>>> customItemRenderers = new ArrayList<>();

    private final List<Pair<Supplier<MenuType<?>>,TriFunction<AbstractContainerMenu,Inventory,Component,Screen>>> containerScreens = new ArrayList<>();
    private final List<Pair<Supplier<Block>,Supplier<RenderType>>> blockRenderTypes = new ArrayList<>();

    private boolean passedModelRegistry;
    private boolean passedTextureStitch;

    private ClientRegistrationHandler(String modid){
        this.modid = modid;
        ModelLoadingPluginManager.registerPlugin(context -> this.handleModelRegistryEvent(context::addModels));
    }

    /**
     * Registers the given model location to be loaded from a json file.
     */
    public void registerModel(ResourceLocation identifier){
        if(this.passedModelRegistry)
            throw new IllegalStateException("Cannot register new models after model registry has been completed!");
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

        this.registerModel(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
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
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new special models after model baking has completed!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        ModelResourceLocation fullIdentifier = new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(this.modid, identifier), "");
        if(this.specialModels.containsKey(fullIdentifier))
            throw new RuntimeException("Duplicate special model entry '" + fullIdentifier + "'!");

        this.specialModels.put(fullIdentifier, model);
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
    public void registerModelOverwrite(ModelResourceLocation identifier, Function<BakedModel,BakedModel> modelOverwrite){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model overwrites after model baking has completed!");
        if(this.specialModels.containsKey(identifier))
            throw new RuntimeException("Overlapping special model and model overwrite '" + identifier + "'!");

        this.modelOverwrites.add(Pair.of(identifier::equals, modelOverwrite));
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

        ModelResourceLocation fullIdentifier = new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(namespace, identifier), variant);
        this.registerModelOverwrite(fullIdentifier, modelOverwrite);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, Function<BakedModel,BakedModel> modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, "", modelOverwrite);
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
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, Function<BakedModel,BakedModel> modelOverwrite){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        Holder<ResourceLocation> blockIdentifierHolder = new Holder<>();
        this.modelOverwrites.add(Pair.of(identifier -> {
            ResourceLocation blockIdentifier = blockIdentifierHolder.get();
            if(blockIdentifier == null){
                blockIdentifier = Registries.BLOCKS.getIdentifier(block.get());
                blockIdentifierHolder.set(blockIdentifier);
            }
            return identifier.id().equals(blockIdentifier);
        }, modelOverwrite));
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, Supplier<BakedModel> modelOverwrite){
        this.registerBlockModelOverwrite(block, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, BakedModel modelOverwrite){
        this.registerBlockModelOverwrite(block, model -> modelOverwrite);
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, Function<BakedModel,BakedModel> modelOverwrite){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        Holder<ResourceLocation> itemIdentifierHolder = new Holder<>();
        this.modelOverwrites.add(Pair.of(identifier -> {
            ResourceLocation itemIdentifier = itemIdentifierHolder.get();
            if(itemIdentifier == null){
                itemIdentifier = Registries.ITEMS.getIdentifier(item.get());
                itemIdentifierHolder.set(itemIdentifier);
            }
            return identifier.id().getNamespace().equals(itemIdentifier.getNamespace())
                && identifier.id().getPath().equals(itemIdentifier.getPath())
                && identifier.getVariant().equals("inventory");
        }, modelOverwrite));
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, Supplier<BakedModel> modelOverwrite){
        this.registerItemModelOverwrite(item, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, BakedModel modelOverwrite){
        this.registerItemModelOverwrite(item, model -> modelOverwrite);
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, Function<EntityRendererProvider.Context,EntityRenderer<? super T,?>> entityRenderer){
        if(haveRenderersBeenRegistered)
            throw new IllegalStateException("Cannot register new renderers after renderer registration has been completed!");

        //noinspection RedundantCast
        this.entityRenderers.add(Pair.of((Supplier<EntityType<?>>)(Object)entityType, (Function<EntityRendererProvider.Context,EntityRenderer<?,?>>)(Object)entityRenderer));
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, Supplier<EntityRenderer<? super T,?>> entityRenderer){
        this.registerEntityRenderer(entityType, context -> entityRenderer.get());
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, EntityRenderer<? super T,?> entityRenderer){
        this.registerEntityRenderer(entityType, context -> entityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<? super T>> blockEntityRenderer){
        if(haveRenderersBeenRegistered)
            throw new IllegalStateException("Cannot register new renderers after renderer registration has been completed!");

        this.blockEntityRenderers.add(Pair.of((Supplier<BlockEntityType<?>>)(Object)entityType, (Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?>>)(Object)blockEntityRenderer));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Supplier<BlockEntityRenderer<? super T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer.get());
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, BlockEntityRenderer<? super T> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerCustomBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Supplier<CustomBlockEntityRenderer<? super T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> CustomBlockEntityRenderer.of(blockEntityRenderer.get()));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerCustomBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, CustomBlockEntityRenderer<? super T> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> CustomBlockEntityRenderer.of(blockEntityRenderer));
    }

    /**
     * Adds the given sprite to the given atlas.
     */
    public void registerAtlasSprite(ResourceLocation textureAtlas, ResourceLocation spriteLocation){
        if(this.passedTextureStitch)
            throw new IllegalStateException("Cannot register new models after texture stitching has been completed!");
        if(textureAtlas == null)
            throw new IllegalArgumentException("Texture atlas must not be null!");

        if(textureAtlas.getPath().startsWith("textures/atlas/") && textureAtlas.getPath().endsWith(".png"))
            textureAtlas = ResourceLocation.fromNamespaceAndPath(textureAtlas.getNamespace(), textureAtlas.getPath().substring("textures/atlas/".length(), textureAtlas.getPath().length() - ".png".length()));

        this.textureAtlasSprites.putIfAbsent(textureAtlas, new HashSet<>());
        if(this.textureAtlasSprites.get(textureAtlas).contains(spriteLocation))
            throw new RuntimeException("Duplicate sprite registration '" + spriteLocation + "' for atlas '" + textureAtlas + "'!");

        this.textureAtlasSprites.get(textureAtlas).add(spriteLocation);
    }

    /**
     * Adds the given sprite to the given atlas.
     */
    public void registerAtlasSprite(ResourceLocation textureAtlas, String spriteLocation){
        if(!RegistryUtil.isValidPath(spriteLocation))
            throw new IllegalArgumentException("Sprite location '" + spriteLocation + "' must only contain characters [a-z0-9_./-]!");

        this.registerAtlasSprite(textureAtlas, ResourceLocation.fromNamespaceAndPath(this.modid, spriteLocation));
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Supplier<Item> item, Supplier<BuiltinItemRendererRegistry.DynamicItemRenderer> itemRenderer){
        if(haveRenderersBeenRegistered)
            throw new IllegalStateException("Cannot register new renderers after renderer registration has been completed!");

        this.customItemRenderers.add(Pair.of(item, itemRenderer));
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Supplier<Item> item, BuiltinItemRendererRegistry.DynamicItemRenderer itemRenderer){
        this.registerItemRenderer(item, () -> itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Item item, Supplier<BuiltinItemRendererRegistry.DynamicItemRenderer> itemRenderer){
        this.registerItemRenderer(() -> item, itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Item item, BuiltinItemRendererRegistry.DynamicItemRenderer itemRenderer){
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
    public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerContainerScreen(Supplier<MenuType<T>> menuType, TriFunction<T,Inventory,Component,U> screenSupplier){
        if(haveRenderersBeenRegistered)
            throw new IllegalStateException("Cannot register new menu screens after the ClientInitialization event has been fired!");

        //noinspection unchecked
        this.containerScreens.add(Pair.of((Supplier<MenuType<?>>)(Object)menuType, (TriFunction<AbstractContainerMenu,Inventory,Component,Screen>)(Object)screenSupplier));
    }

    /**
     * Registers the given screen constructor for the given menu type.
     */
    public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerContainerScreen(Supplier<MenuType<T>> menuType, Function<T,U> screenSupplier){
        this.registerContainerScreen(menuType, (container, inventory, title) -> screenSupplier.apply(container));
    }

    /**
     * Registers the given screen constructor for the given menu type.
     */
    public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerContainerScreen(MenuType<T> menuType, TriFunction<T,Inventory,Component,U> screenSupplier){
        this.registerContainerScreen(() -> menuType, screenSupplier);
    }

    /**
     * Registers the given screen constructor for the given menu type.
     */
    public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerContainerScreen(MenuType<T> menuType, Function<T,U> screenSupplier){
        this.registerContainerScreen(() -> menuType, (container, inventory, title) -> screenSupplier.apply(container));
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     */
    public void registerBlockModelRenderType(Supplier<Block> block, Supplier<RenderType> renderTypeSupplier){
        if(haveRenderersBeenRegistered)
            throw new IllegalStateException("Cannot register new menu screens after the ClientInitialization event has been fired!");

        this.blockRenderTypes.add(Pair.of(block, renderTypeSupplier));
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     */
    public void registerBlockModelRenderType(Supplier<Block> block, RenderType renderType){
        this.registerBlockModelRenderType(block, renderType);
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     */
    public void registerBlockModelRenderType(Block block, Supplier<RenderType> renderTypeSupplier){
        this.registerBlockModelRenderType(() -> block, renderTypeSupplier);
    }

    /**
     * Registers the solid render type to be used when rendering the given block.
     */
    public void registerBlockModelSolidRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, RenderType::solid);
    }

    /**
     * Registers the solid render type to be used when rendering the given block.
     */
    public void registerBlockModelSolidRenderType(Block block){
        this.registerBlockModelRenderType(block, RenderType::solid);
    }

    /**
     * Registers the cutout mipped render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutMippedRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, RenderType::cutoutMipped);
    }

    /**
     * Registers the cutout mipped render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutMippedRenderType(Block block){
        this.registerBlockModelRenderType(block, RenderType::cutoutMipped);
    }

    /**
     * Registers the cutout render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, RenderType::cutout);
    }

    /**
     * Registers the cutout render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutRenderType(Block block){
        this.registerBlockModelRenderType(block, RenderType::cutout);
    }

    /**
     * Registers the translucent render type to be used when rendering the given block.
     */
    public void registerBlockModelTranslucentRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, RenderType::translucent);
    }

    /**
     * Registers the translucent render type to be used when rendering the given block.
     */
    public void registerBlockModelTranslucentRenderType(Block block){
        this.registerBlockModelRenderType(block, RenderType::translucent);
    }

    private void registerRenderers(){
        // Entity renderers
        Set<EntityType<?>> entityTypes = new HashSet<>();
        for(Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?,?>>> entry : this.entityRenderers){
            EntityType<?> entityType = entry.left().get();
            if(entityType == null)
                throw new RuntimeException("Entity renderer registered with null entity type!");
            if(entityTypes.contains(entityType))
                throw new RuntimeException("Duplicate entity renderer for entity type '" + Registries.ENTITY_TYPES.getIdentifier(entityType) + "'!");

            entityTypes.add(entityType);
            //noinspection unchecked,rawtypes
            EntityRendererRegistry.register((EntityType)entityType, entry.right()::apply);
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
            //noinspection unchecked,rawtypes
            BlockEntityRendererRegistry.register((BlockEntityType)blockEntityType, entry.right()::apply);
        }

        // Custom item renderers
        Set<Item> items = new HashSet<>();
        for(Pair<Supplier<Item>,Supplier<BuiltinItemRendererRegistry.DynamicItemRenderer>> entry : this.customItemRenderers){
            Item item = entry.left().get();
            if(item == null)
                throw new RuntimeException("Custom item renderer registered with null item!");
            if(items.contains(item))
                throw new RuntimeException("Duplicate custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "'!");

            BuiltinItemRendererRegistry.DynamicItemRenderer customRenderer = entry.right().get();
            if(customRenderer == null)
                throw new RuntimeException("Got null custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "'!");

            items.add(item);
            BuiltinItemRendererRegistry.INSTANCE.register(item, customRenderer);
        }

        // Container Screens
        Set<MenuType<?>> menuTypes = new HashSet<>();
        for(Pair<Supplier<MenuType<?>>,TriFunction<AbstractContainerMenu,Inventory,Component,Screen>> entry : this.containerScreens){
            MenuType<?> menuType = entry.left().get();
            if(menuType == null)
                throw new RuntimeException("Container screen registered with null menu type!");
            if(menuTypes.contains(menuType))
                throw new RuntimeException("Duplicate container screen for menu type '" + Registries.MENU_TYPES.getIdentifier(menuType) + "'!");

            menuTypes.add(menuType);
            //noinspection unchecked,rawtypes
            MenuScreens.register((MenuType)menuType, (MenuScreens.ScreenConstructor)entry.right()::apply);
        }

        // Block render types
        Set<Block> blocks = new HashSet<>();
        for(Pair<Supplier<Block>,Supplier<RenderType>> entry : this.blockRenderTypes){
            Block block = entry.left().get();
            if(block == null)
                throw new RuntimeException("Block render type registered for null block!");
            if(blocks.contains(block))
                throw new RuntimeException("Duplicate render type for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");
            RenderType renderType = entry.right().get();
            if(renderType == null)
                throw new RuntimeException("Got null render type for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");

            blocks.add(block);
            BlockRenderLayerMap.INSTANCE.putBlock(block, renderType);
        }
    }

    private void handleModelRegistryEvent(Consumer<Collection<ResourceLocation>> out){
        this.passedModelRegistry = true;

        // Additional models
        out.accept(this.models);
    }

    private void registerModelOverwrites(Map<ModelResourceLocation,BakedModel> modelRegistry){
        // Special models
        for(Map.Entry<ModelResourceLocation,Supplier<BakedModel>> entry : this.specialModels.entrySet()){
            ModelResourceLocation identifier = entry.getKey();
            if(modelRegistry.containsKey(identifier))
                throw new RuntimeException("Special model '" + identifier + "' is trying to overwrite another model!");

            BakedModel model = entry.getValue().get();
            if(model == null)
                throw new RuntimeException("Got null object for special model '" + entry.getKey() + "'!");

            modelRegistry.put(entry.getKey(), model);
        }

        // Model overwrites
        for(Pair<Predicate<ModelResourceLocation>,Function<BakedModel,BakedModel>> pair : this.modelOverwrites){
            // Find all the identifiers which should be replaced
            List<ModelResourceLocation> modelIdentifiers = modelRegistry.keySet().stream()
                .filter(identifier -> pair.left().test(identifier))
                .collect(Collectors.toList());

            for(ModelResourceLocation identifier : modelIdentifiers){
                if(!modelRegistry.containsKey(identifier))
                    throw new RuntimeException("No model registered for model overwrite '" + identifier + "'!");

                BakedModel model = modelRegistry.get(identifier);
                model = pair.right().apply(model);
                if(model == null)
                    throw new RuntimeException("Model overwrite for '" + identifier + "' returned a null model!");

                modelRegistry.put(identifier, model);
            }
        }
    }

    private void addSprites(ResourceLocation atlas, Consumer<ResourceLocation> spriteConsumer){
        this.passedTextureStitch = true;

        // Texture atlas sprites
        Set<ResourceLocation> sprites = this.textureAtlasSprites.get(atlas);
        if(sprites == null)
            return;

        sprites.forEach(spriteConsumer);
    }
}
