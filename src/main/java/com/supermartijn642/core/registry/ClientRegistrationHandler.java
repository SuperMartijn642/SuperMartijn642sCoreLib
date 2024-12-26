package com.supermartijn642.core.registry;

import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.CustomItemRenderer;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.core.util.TriFunction;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BakedModel;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class ClientRegistrationHandler {

    /**
     * Contains one registration helper per modid
     */
    private static final Map<String,ClientRegistrationHandler> REGISTRATION_HELPER_MAP = new HashMap<>();
    private static boolean haveModelsBeenRegistered = false;

    @ApiStatus.Internal
    public static Set<ResourceLocation> getModelConsumerLocations(){
        haveModelsBeenRegistered = true;
        return REGISTRATION_HELPER_MAP.values()
            .stream()
            .flatMap(ClientRegistrationHandler::modelConsumerLocations)
            .collect(Collectors.toSet());
    }

    @ApiStatus.Internal
    public static void applyModelConsumersInternal(Function<ResourceLocation,BakedModel> modelGetter){
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.handleModelConsumers(modelGetter));
    }

    @ApiStatus.Internal
    public static Map<ResourceLocation,Function<BakedModel,BakedModel>> gatherModelOverwritesInternal(){
        return combineModelOverwrites(handler -> handler.modelOverwrites);
    }

    @ApiStatus.Internal
    public static Map<ResourceLocation,Function<BakedModel,BakedModel>> gatherBlockModelOverwritesInternal(){
        return combineModelOverwrites(
            handler -> handler.blockModelOverwrites.stream()
                .collect(Collectors.toMap(
                    p -> Registries.BLOCKS.getIdentifier(p.left().get()),
                    Pair::right
                ))
        );
    }

    @ApiStatus.Internal
    public static Map<ResourceLocation,Function<ItemModel,ItemModel>> gatherItemModelOverwritesInternal(){
        return combineModelOverwrites(
            handler -> handler.itemModelOverwrites.stream()
                .collect(Collectors.toMap(
                    p -> Registries.ITEMS.getIdentifier(p.left().get()),
                    Pair::right
                ))
        );
    }

    private static <S, T> Map<ResourceLocation,Function<T,T>> combineModelOverwrites(Function<ClientRegistrationHandler,Map<ResourceLocation,Function<T,T>>> overwritesExtractor){
        haveModelsBeenRegistered = true;
        Map<ResourceLocation,Function<T,T>> combined = new HashMap<>();
        REGISTRATION_HELPER_MAP.forEach((modid, handler) -> {
            overwritesExtractor.apply(handler).forEach((location, overwrite) -> {
                Function<T,T> encapsulated = model -> {
                    try{
                        return overwrite.apply(model);
                    }catch(Exception e){
                        throw new RuntimeException("Encountered an error whilst computing model overwrite for mod '" + modid + "'", e);
                    }
                };
                combined.compute(location, (l, f) -> f == null ? encapsulated : f.andThen(encapsulated));
            });
        });
        return combined;
    }

    /**
     * Get a registration handler for a given modid. This will always return one unique registration handler per modid.
     * @param modid modid of the mod registering entries
     * @return a unique registration handler for the given modid
     */
    public static synchronized ClientRegistrationHandler get(String modid){
        if(!RegistryUtil.isValidNamespace(modid))
            throw new IllegalArgumentException("Modid '" + modid + "' must only contain characters [a-z0-9_.-]!");
        String activeMod = ModLoadingContext.get().getActiveNamespace();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName() + "' is requesting registration helper for different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, ClientRegistrationHandler::new);
    }

    @ApiStatus.Internal
    public static void collectSprites(ResourceLocation atlas, Consumer<ResourceLocation> spriteConsumer){
        for(ClientRegistrationHandler value : REGISTRATION_HELPER_MAP.values())
            value.addSprites(atlas, spriteConsumer);
    }

    private final String modid;

    private final List<Pair<ResourceLocation,Consumer<BakedModel>>> modelConsumers = new ArrayList<>();
    private final Map<ResourceLocation,Function<BakedModel,BakedModel>> modelOverwrites = new HashMap<>();
    private final List<Pair<Supplier<Block>,Function<BakedModel,BakedModel>>> blockModelOverwrites = new ArrayList<>();
    private final List<Pair<Supplier<Item>,Function<ItemModel,ItemModel>>> itemModelOverwrites = new ArrayList<>();

    private final List<Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?,?>>>> entityRenderers = new ArrayList<>();
    private final List<Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?>>>> blockEntityRenderers = new ArrayList<>();

    private final Map<ResourceLocation,Set<ResourceLocation>> textureAtlasSprites = new HashMap<>();

    private final List<Pair<ResourceLocation,MapCodec<? extends SpecialModelRenderer.Unbaked>>> specialModelRenderers = new ArrayList<>();
    private final List<Pair<Supplier<Block>,Supplier<SpecialModelRenderer.Unbaked>>> blockSpecialRenderers = new ArrayList<>();

    private final List<Pair<Supplier<MenuType<?>>,TriFunction<AbstractContainerMenu,Inventory,Component,Screen>>> containerScreens = new ArrayList<>();
    private final List<Pair<Supplier<Block>,Supplier<RenderType>>> blockRenderTypes = new ArrayList<>();

    private final List<Pair<ResourceLocation,MapCodec<? extends ItemModel.Unbaked>>> itemModelTypes = new ArrayList<>();

    private boolean passedRegisterRenderers;
    private boolean passedTextureStitch;

    private ClientRegistrationHandler(String modid){
        this.modid = modid;
        IEventBus eventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        eventBus.addListener(this::handleRegisterRenderersEvent);
        eventBus.addListener(this::handleRegisterMenuScreensEvent);
        eventBus.addListener(this::handleRegisterSpecialModelRenderersEvent);
        eventBus.addListener(this::handleRegisterSpecialBlockModelRenderersEvent);
        eventBus.addListener(this::handleRegisterItemModelsEvent);
    }

    /**
     * Causes the model at the given location to be loaded.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerModelConsumer(ResourceLocation location, Consumer<BakedModel> consumer){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model consumer after model registry has been completed!");
        this.modelConsumers.add(Pair.of(location, consumer));
    }

    /**
     * Causes the model at the given location to be loaded.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerModelConsumer(String namespace, String identifier, Consumer<BakedModel> consumer){
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        this.registerModelConsumer(ResourceLocation.fromNamespaceAndPath(namespace, identifier), consumer);
    }

    /**
     * Causes the model at the given location to be loaded.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerModelConsumer(String identifier, Consumer<BakedModel> consumer){
        this.registerModelConsumer(this.modid, identifier, consumer);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(ResourceLocation location, Function<BakedModel,BakedModel> modelOverwrite){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model overwrites after model baking has completed!");

        this.modelOverwrites.compute(location, (l, f) -> {
            if(f == null)
                return modelOverwrite;
            return f.andThen(modelOverwrite);
        });
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(String namespace, String identifier, Function<BakedModel,BakedModel> modelOverwrite){
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        ResourceLocation fullIdentifier = ResourceLocation.fromNamespaceAndPath(namespace, identifier);
        this.registerModelOverwrite(fullIdentifier, modelOverwrite);
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
    public void registerModelOverwrite(String namespace, String identifier, BakedModel modelOverwrite){
        this.registerModelOverwrite(namespace, identifier, model -> modelOverwrite);
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, Function<BakedModel,BakedModel> modelOverwrite){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        this.blockModelOverwrites.add(Pair.of(block, modelOverwrite));
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
        if(this.passedRegisterRenderers)
            throw new IllegalStateException("Cannot register new renderers after RegisterRenderers has been fired!");

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
        if(this.passedRegisterRenderers)
            throw new IllegalStateException("Cannot register new renderers after RegisterRenderers has been fired!");

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
            throw new IllegalStateException("Cannot register new models after TextureStitchEvent has been fired!");
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
        this.specialModelRenderers.add(Pair.of(ResourceLocation.fromNamespaceAndPath(this.modid, identifier), codec));
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
        this.registerBlockSpecialModelRenderer(() -> block, () -> renderer);
    }

    /**
     * Registers the given custom item renderer.
     */
    public void registerCustomItemRenderer(String identifier, Supplier<CustomItemRenderer> itemRenderer){
        Holder<MapCodec<SpecialModelRenderer.Unbaked>> holder = new Holder<>();
        MapCodec<SpecialModelRenderer.Unbaked> codec = MapCodec.unit(new SpecialModelRenderer.Unbaked() {
            SpecialModelRenderer<?> renderer = null;

            @Override
            public SpecialModelRenderer<?> bake(EntityModelSet entityModelSet){
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
        if(this.passedRegisterRenderers)
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
        if(this.passedRegisterRenderers)
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

    public void registerItemModelType(String identifier, MapCodec<? extends ItemModel.Unbaked> codec){
        this.itemModelTypes.add(Pair.of(ResourceLocation.fromNamespaceAndPath(this.modid, identifier), codec));
    }

    private void handleRegisterRenderersEvent(EntityRenderersEvent.RegisterRenderers e){
        this.passedRegisterRenderers = true;

        // Entity renderers
        Set<EntityType<?>> entityTypes = new HashSet<>();
        for(Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?,?>>> entry : this.entityRenderers){
            EntityType<?> entityType = entry.left().get();
            if(entityType == null)
                throw new RuntimeException("Entity renderer registered with null entity type!");
            if(entityTypes.contains(entityType))
                throw new RuntimeException("Duplicate entity renderer for entity type '" + Registries.ENTITY_TYPES.getIdentifier(entityType) + "'!");

            entityTypes.add(entityType);
            // noinspection unchecked,rawtypes
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
            //noinspection deprecation
            ItemBlockRenderTypes.setRenderLayer(block, renderType);
        }
    }

    private void handleRegisterMenuScreensEvent(RegisterMenuScreensEvent e){
        // Container Screens
        Set<MenuType<?>> menuTypes = new HashSet<>();
        for(Pair<Supplier<MenuType<?>>,TriFunction<AbstractContainerMenu,Inventory,Component,Screen>> entry : this.containerScreens){
            MenuType<?> menuType = entry.left().get();
            if(menuType == null)
                throw new RuntimeException("Container screen registered with null menu type!");
            if(menuTypes.contains(menuType))
                throw new RuntimeException("Duplicate container screen for menu type '" + Registries.MENU_TYPES.getIdentifier(menuType) + "'!");

            menuTypes.add(menuType);
            //noinspection rawtypes,unchecked
            e.register((MenuType)menuType, (MenuScreens.ScreenConstructor)entry.right()::apply);
        }
    }

    private void handleRegisterSpecialModelRenderersEvent(RegisterSpecialModelRendererEvent e){
        // Special model renderers
        this.specialModelRenderers.forEach(p -> e.register(p.left(), p.right()));
    }

    private void handleRegisterSpecialBlockModelRenderersEvent(RegisterSpecialBlockModelRendererEvent e){
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
            e.register(block, renderer);
        }
    }

    private Stream<ResourceLocation> modelConsumerLocations(){
        return this.modelConsumers.stream().map(Pair::left);
    }

    private void handleModelConsumers(Function<ResourceLocation,BakedModel> modelGetter){
        // Model callbacks
        for(Pair<ResourceLocation,Consumer<BakedModel>> entry : this.modelConsumers){
            try{
                entry.right().accept(modelGetter.apply(entry.left()));
            }catch(Exception e){
                CoreLib.LOGGER.error("Encountered an exception whilst applying a model consumer for mod '{}'!", this.modid, e);
            }
        }
    }

    private void handleRegisterItemModelsEvent(RegisterItemModelsEvent e){
        // Item model types
        this.itemModelTypes.forEach(p -> e.register(p.left(), p.right()));
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
