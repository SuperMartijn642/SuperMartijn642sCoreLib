package com.supermartijn642.core.registry;

import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.CustomItemRenderer;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.core.util.TriFunction;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialBlockRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.level.block.state.BlockState;
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
    private static final Map<String,ClientRegistrationHandler> REGISTRATION_HELPER_MAP = new TreeMap<>();
    private static boolean haveRenderersBeenRegistered = false;
    private static boolean haveModelsBeenRegistered = false;

    @ApiStatus.Internal
    public static void registerRenderersInternal(){
        haveRenderersBeenRegistered = true;
        REGISTRATION_HELPER_MAP.values().forEach(ClientRegistrationHandler::registerRenderers);
    }

    @ApiStatus.Internal
    public static void registerBlockModelConsumerDependenciesInternal(Predicate<ResourceLocation> markModelDependency){
        haveModelsBeenRegistered = true;
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.registerBlockModelConsumerDependencies(markModelDependency));
    }

    @ApiStatus.Internal
    public static void registerPictureInPictureRenderersInternal(MultiBufferSource.BufferSource bufferSource, Consumer<PictureInPictureRenderer<?>> output){
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.registerPictureInPictureRenderers(bufferSource, output));
    }

    @ApiStatus.Internal
    public static void applyBlockModelConsumersInternal(Function<ResourceLocation,BlockStateModel> modelGetter){
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.handleBlockModelConsumers(modelGetter));
    }

    @ApiStatus.Internal
    public static void applyBlockModelOverwritesInternal(Map<BlockState,BlockStateModel> models){
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.applyBlockModelOverwrites(models));
    }

    @ApiStatus.Internal
    public static void applyItemModelOverwritesInternal(Map<ResourceLocation,ItemModel> models){
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.applyItemModelOverwrites(models));
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

    private final List<Pair<ResourceLocation,Consumer<BlockStateModel>>> blockModelConsumers = new ArrayList<>();
    private final List<Pair<Supplier<Block>,Function<BlockStateModel,BlockStateModel>>> blockModelOverwrites = new ArrayList<>();
    private final List<Pair<Supplier<Item>,Function<ItemModel,ItemModel>>> itemModelOverwrites = new ArrayList<>();

    private final List<Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?,?>>>> entityRenderers = new ArrayList<>();
    private final List<Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?,?>>>> blockEntityRenderers = new ArrayList<>();

    private final Map<ResourceLocation,Set<ResourceLocation>> textureAtlasSprites = new HashMap<>();

    private final List<Pair<Supplier<Block>,Supplier<SpecialModelRenderer.Unbaked>>> blockSpecialRenderers = new ArrayList<>();

    private final List<Pair<Supplier<MenuType<?>>,TriFunction<AbstractContainerMenu,Inventory,Component,Screen>>> containerScreens = new ArrayList<>();
    private final List<Pair<Supplier<Block>,Supplier<ChunkSectionLayer>>> blockRenderTypes = new ArrayList<>();

    private final List<Function<MultiBufferSource.BufferSource,PictureInPictureRenderer<?>>> pictureInPictureRenderers = new ArrayList<>();

    private boolean passedTextureStitch;

    private ClientRegistrationHandler(String modid){
        this.modid = modid;
    }

    /**
     * Causes the model at the given location to be loaded as a block model.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerBlockModelConsumer(ResourceLocation location, Consumer<BlockStateModel> consumer){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model consumer after model registry has been completed!");
        this.blockModelConsumers.add(Pair.of(location, consumer));
    }

    /**
     * Causes the model at the given location to be loaded as a block model.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerBlockModelConsumer(String namespace, String identifier, Consumer<BlockStateModel> consumer){
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        this.registerBlockModelConsumer(ResourceLocation.fromNamespaceAndPath(namespace, identifier), consumer);
    }

    /**
     * Causes the model at the given location to be loaded as a block model.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerBlockModelConsumer(String identifier, Consumer<BlockStateModel> consumer){
        this.registerBlockModelConsumer(this.modid, identifier, consumer);
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, Function<BlockStateModel,BlockStateModel> modelOverwrite){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        this.blockModelOverwrites.add(Pair.of(block, modelOverwrite));
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, Supplier<BlockStateModel> modelOverwrite){
        this.registerBlockModelOverwrite(block, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, BlockStateModel modelOverwrite){
        this.registerBlockModelOverwrite(block, model -> modelOverwrite);
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, Function<ItemModel,ItemModel> modelOverwrite){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        this.itemModelOverwrites.add(Pair.of(item, modelOverwrite));
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, Supplier<ItemModel> modelOverwrite){
        this.registerItemModelOverwrite(item, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for the given item's model.
     */
    public void registerItemModelOverwrite(Supplier<Item> item, ItemModel modelOverwrite){
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
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<? super T,?>> blockEntityRenderer){
        if(haveRenderersBeenRegistered)
            throw new IllegalStateException("Cannot register new renderers after renderer registration has been completed!");

        // Compiler is not happy without the (Object) cast ¯\(o_o)/¯
        //noinspection RedundantCast
        this.blockEntityRenderers.add(Pair.of((Supplier<BlockEntityType<?>>)(Object)entityType, (Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?,?>>)(Object)blockEntityRenderer));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Supplier<BlockEntityRenderer<? super T,?>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer.get());
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, BlockEntityRenderer<? super T,?> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerCustomBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Supplier<CustomBlockEntityRenderer<? super T,?>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> CustomBlockEntityRenderer.of(blockEntityRenderer.get()));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends BlockEntity> void registerCustomBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, CustomBlockEntityRenderer<? super T,?> blockEntityRenderer){
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
     * Registers the given special model renderer.
     */
    public void registerSpecialModelRenderer(String identifier, MapCodec<? extends SpecialModelRenderer.Unbaked> codec){
        SpecialModelRenderers.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(this.modid, identifier), codec);
    }

    /**
     * Registers the given special model renderer.
     */
    public void registerBlockSpecialModelRenderer(Supplier<Block> block, Supplier<SpecialModelRenderer.Unbaked> renderer){
        this.blockSpecialRenderers.add(Pair.of(block, renderer));
    }

    /**
     * Registers the given special model renderer.
     */
    public void registerBlockSpecialModelRenderer(Block block, SpecialModelRenderer.Unbaked renderer){
        SpecialBlockRendererRegistry.register(block, renderer);
    }

    /**
     * Registers the given custom item renderer.
     */
    public void registerCustomItemRenderer(String identifier, Supplier<CustomItemRenderer> itemRenderer){
        Holder<MapCodec<SpecialModelRenderer.Unbaked>> holder = new Holder<>();
        MapCodec<SpecialModelRenderer.Unbaked> codec = MapCodec.unit(new SpecialModelRenderer.Unbaked() {
            SpecialModelRenderer<?> renderer = null;

            @Override
            public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context){
                if(this.renderer == null)
                    this.renderer = CustomItemRenderer.toSpecialModelRenderer(itemRenderer.get());
                return this.renderer;
            }

            @Override
            public MapCodec<? extends SpecialModelRenderer.Unbaked> type(){
                return holder.get();
            }
        });
        holder.set(codec);
        this.registerSpecialModelRenderer(identifier, codec);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerCustomItemRenderer(String identifier, CustomItemRenderer itemRenderer){
        this.registerCustomItemRenderer(identifier, () -> itemRenderer);
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
    public void registerBlockModelRenderType(Supplier<Block> block, Supplier<ChunkSectionLayer> renderTypeSupplier){
        if(haveRenderersBeenRegistered)
            throw new IllegalStateException("Cannot register new menu screens after the ClientInitialization event has been fired!");

        this.blockRenderTypes.add(Pair.of(block, renderTypeSupplier));
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     */
    public void registerBlockModelRenderType(Supplier<Block> block, ChunkSectionLayer renderType){
        this.registerBlockModelRenderType(block, () -> renderType);
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     */
    public void registerBlockModelRenderType(Block block, Supplier<ChunkSectionLayer> renderTypeSupplier){
        this.registerBlockModelRenderType(() -> block, renderTypeSupplier);
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     */
    public void registerBlockModelRenderType(Block block, ChunkSectionLayer renderType){
        this.registerBlockModelRenderType(() -> block, renderType);
    }

    /**
     * Registers the solid render type to be used when rendering the given block.
     */
    public void registerBlockModelSolidRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, ChunkSectionLayer.SOLID);
    }

    /**
     * Registers the solid render type to be used when rendering the given block.
     */
    public void registerBlockModelSolidRenderType(Block block){
        this.registerBlockModelRenderType(block, ChunkSectionLayer.SOLID);
    }

    /**
     * Registers the cutout mipped render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutMippedRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, ChunkSectionLayer.CUTOUT_MIPPED);
    }

    /**
     * Registers the cutout mipped render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutMippedRenderType(Block block){
        this.registerBlockModelRenderType(block, ChunkSectionLayer.CUTOUT_MIPPED);
    }

    /**
     * Registers the cutout render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, ChunkSectionLayer.CUTOUT);
    }

    /**
     * Registers the cutout render type to be used when rendering the given block.
     */
    public void registerBlockModelCutoutRenderType(Block block){
        this.registerBlockModelRenderType(block, ChunkSectionLayer.CUTOUT);
    }

    /**
     * Registers the translucent render type to be used when rendering the given block.
     */
    public void registerBlockModelTranslucentRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, ChunkSectionLayer.TRANSLUCENT);
    }

    /**
     * Registers the translucent render type to be used when rendering the given block.
     */
    public void registerBlockModelTranslucentRenderType(Block block){
        this.registerBlockModelRenderType(block, ChunkSectionLayer.TRANSLUCENT);
    }

    public void registerItemModelType(String identifier, MapCodec<? extends ItemModel.Unbaked> codec){
        ItemModels.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(this.modid, identifier), codec);
    }

    public void registerPictureInPictureRenderer(Function<MultiBufferSource.BufferSource,PictureInPictureRenderer<?>> renderer){
        this.pictureInPictureRenderers.add(renderer);
    }

    public void registerPictureInPictureRenderer(Supplier<PictureInPictureRenderer<?>> renderer){
        this.registerPictureInPictureRenderer(buffers -> renderer.get());
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
            EntityRenderers.register((EntityType)entityType, entry.right()::apply);
        }

        // Entity renderers
        Set<BlockEntityType<?>> blockEntityTypes = new HashSet<>();
        for(Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?,?>>> entry : this.blockEntityRenderers){
            BlockEntityType<?> blockEntityType = entry.left().get();
            if(blockEntityType == null)
                throw new RuntimeException("Block entity renderer registered with null block entity type!");
            if(blockEntityTypes.contains(blockEntityType))
                throw new RuntimeException("Duplicate block entity renderer for block entity type '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(blockEntityType) + "'!");

            blockEntityTypes.add(blockEntityType);
            //noinspection unchecked,rawtypes
            BlockEntityRenderers.register((BlockEntityType)blockEntityType, entry.right()::apply);
        }

        // Block special renderers
        Set<Block> blocks = new HashSet<>();
        for(Pair<Supplier<Block>,Supplier<SpecialModelRenderer.Unbaked>> entry : this.blockSpecialRenderers){
            Block block = entry.left().get();
            if(block == null)
                throw new RuntimeException("Special model renderer registered for null block!");
            if(blocks.contains(block))
                throw new RuntimeException("Duplicate special model renderer for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");

            SpecialModelRenderer.Unbaked renderer = entry.right().get();
            if(renderer == null)
                throw new RuntimeException("Got null special model renderer for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");

            blocks.add(block);
            SpecialBlockRendererRegistry.register(block, renderer);
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
        blocks = new HashSet<>();
        for(Pair<Supplier<Block>,Supplier<ChunkSectionLayer>> entry : this.blockRenderTypes){
            Block block = entry.left().get();
            if(block == null)
                throw new RuntimeException("Block render layer registered for null block!");
            if(blocks.contains(block))
                throw new RuntimeException("Duplicate render layer for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");
            ChunkSectionLayer layer = entry.right().get();
            if(layer == null)
                throw new RuntimeException("Got null render layer for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");

            blocks.add(block);
            BlockRenderLayerMap.putBlock(block, layer);
        }
    }

    private void registerBlockModelConsumerDependencies(Predicate<ResourceLocation> markModelDependency){
        Set<ResourceLocation> missingModels = null;
        for(Pair<ResourceLocation,Consumer<BlockStateModel>> consumer : this.blockModelConsumers){
            ResourceLocation location = consumer.left();
            if(!markModelDependency.test(location)){
                if(missingModels == null)
                    missingModels = new HashSet<>();
                missingModels.add(location);
            }
        }
        if(missingModels != null)
            CoreLib.LOGGER.error("Missing models for block model consumers from mod '{}': {}", this.modid, missingModels.stream().map(l -> "'" + l + "'").collect(Collectors.joining(", ")));
    }

    private void registerPictureInPictureRenderers(MultiBufferSource.BufferSource bufferSource, Consumer<PictureInPictureRenderer<?>> output){
        Set<Class<?>> stateClasses = new HashSet<>();
        for(Function<MultiBufferSource.BufferSource,PictureInPictureRenderer<?>> rendererFunction : this.pictureInPictureRenderers){
            PictureInPictureRenderer<?> renderer;
            try{
                renderer = rendererFunction.apply(bufferSource);
            }catch(RuntimeException e){
                throw new RuntimeException("Encountered an exception whilst creating picture in picture renderer for mod '" + this.modid + "'!", e);
            }
            if(renderer == null)
                throw new RuntimeException("Picture in picture renderer function for mod '" + this.modid + "' returned null!");
            if(!stateClasses.add(renderer.getRenderStateClass()))
                throw new RuntimeException("Mod '" + this.modid + "' registered multiple picture in picture renderers for class '" + renderer.getRenderStateClass().getName() + "'!");
            output.accept(renderer);
        }
    }

    private void handleBlockModelConsumers(Function<ResourceLocation,BlockStateModel> modelGetter){
        // Model callbacks
        for(Pair<ResourceLocation,Consumer<BlockStateModel>> entry : this.blockModelConsumers){
            try{
                entry.right().accept(modelGetter.apply(entry.left()));
            }catch(Exception e){
                CoreLib.LOGGER.error("Encountered an exception whilst applying a model consumer for mod '{}'!", this.modid, e);
            }
        }
    }

    private void applyBlockModelOverwrites(Map<BlockState,BlockStateModel> models){
        for(Pair<Supplier<Block>,Function<BlockStateModel,BlockStateModel>> overwrite : this.blockModelOverwrites){
            Block block = overwrite.left().get();
            if(block == null){
                CoreLib.LOGGER.error("Got 'null' block for block model overwrite from mod '{}'!", this.modid);
                continue;
            }
            for(BlockState state : block.getStateDefinition().getPossibleStates()){
                BlockStateModel model = models.get(state);
                if(model == null)
                    continue;
                try{
                    model = overwrite.right().apply(model);
                }catch(Exception e){
                    CoreLib.LOGGER.error("Encountered an error while applying block model overwrite from mod '{}' for block state '{}'!", this.modid, state, e);
                    continue;
                }
                if(model == null){
                    CoreLib.LOGGER.error("Block model overwrite from mod '{}' for block state '{}' returned null!", this.modid, state);
                    continue;
                }
                models.put(state, model);
            }
        }
    }

    private void applyItemModelOverwrites(Map<ResourceLocation,ItemModel> models){
        for(Pair<Supplier<Item>,Function<ItemModel,ItemModel>> overwrite : this.itemModelOverwrites){
            Item item = overwrite.left().get();
            if(item == null){
                CoreLib.LOGGER.error("Got 'null' item for item model overwrite from mod '{}'!", this.modid);
                continue;
            }
            ResourceLocation modelLocation = item.components().get(DataComponents.ITEM_MODEL);
            ItemModel model = models.get(modelLocation);
            if(model == null)
                continue;
            try{
                model = overwrite.right().apply(model);
            }catch(Exception e){
                CoreLib.LOGGER.error("Encountered an error while applying item model overwrite from mod '{}' for item '{}'!", this.modid, item, e);
                continue;
            }
            if(model == null){
                CoreLib.LOGGER.error("Item model overwrite from mod '{}' for item '{}' returned null!", this.modid, item);
                continue;
            }
            models.put(modelLocation, model);
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
