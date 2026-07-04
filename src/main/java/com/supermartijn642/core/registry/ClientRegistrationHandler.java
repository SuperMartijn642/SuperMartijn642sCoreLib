package com.supermartijn642.core.registry;

import com.mojang.serialization.MapCodec;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.CustomItemRenderer;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.core.util.TriFunction;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.block.BuiltInBlockModels;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.*;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class ClientRegistrationHandler {

    /**
     * Contains one registration helper per modid
     */
    private static final Map<String,ClientRegistrationHandler> REGISTRATION_HELPER_MAP = new TreeMap<>();
    private static boolean haveModelsBeenRegistered = false;

    @ApiStatus.Internal
    public static void registerBlockModelConsumerDependenciesInternal(Predicate<Identifier> markModelDependency){
        haveModelsBeenRegistered = true;
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.registerBlockModelConsumerDependencies(markModelDependency));
    }

    @ApiStatus.Internal
    public static void applyBlockModelConsumersInternal(Function<Identifier,BlockStateModel> modelGetter){
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.handleBlockModelConsumers(modelGetter));
    }

    @ApiStatus.Internal
    public static void applyBlockModelOverwritesInternal(Map<BlockState,BlockStateModel> models){
        REGISTRATION_HELPER_MAP.values().forEach(handler -> handler.applyBlockModelOverwrites(models));
    }

    @ApiStatus.Internal
    public static void applyItemModelOverwritesInternal(Map<Identifier,ItemModel> models){
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
        String activeMod = ModLoadingContext.get().getActiveNamespace();
        if(activeMod != null && !activeMod.equals("minecraft") && !activeMod.equals("forge")){
            if(!activeMod.equals(modid))
                CoreLib.LOGGER.warn("Mod '" + ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName() + "' is requesting registration helper for different modid '" + modid + "'!");
        }else if(modid.equals("minecraft") || modid.equals("forge"))
            CoreLib.LOGGER.warn("Mod is requesting registration helper for modid '" + modid + "'!");

        return REGISTRATION_HELPER_MAP.computeIfAbsent(modid, ClientRegistrationHandler::new);
    }

    @ApiStatus.Internal
    public static void collectSprites(Identifier atlas, Consumer<Identifier> spriteConsumer){
        for(ClientRegistrationHandler value : REGISTRATION_HELPER_MAP.values())
            value.addSprites(atlas, spriteConsumer);
    }

    private final String modid;

    private final List<Pair<Identifier,Consumer<BlockStateModel>>> blockStateModelConsumers = new ArrayList<>();
    private final List<Pair<Supplier<Block>,Function<BlockStateModel,BlockStateModel>>> blockStateModelOverwrites = new ArrayList<>();
    private final List<Pair<Supplier<Item>,Function<ItemModel,ItemModel>>> itemModelOverwrites = new ArrayList<>();
    private final List<Pair<Supplier<Block>,BiFunction<BlockState,BlockColors,BlockModel.Unbaked>>> builtinBlockModels = new ArrayList<>();

    private final List<Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?,?>>>> entityRenderers = new ArrayList<>();
    private final List<Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?,?>>>> blockEntityRenderers = new ArrayList<>();

    private final Map<Identifier,Set<Identifier>> textureAtlasSprites = new HashMap<>();

    private final List<Pair<Identifier,MapCodec<? extends SpecialModelRenderer.Unbaked<?>>>> specialModelRenderers = new ArrayList<>();

    private final List<Pair<Supplier<MenuType<?>>,TriFunction<AbstractContainerMenu,Inventory,Component,Screen>>> containerScreens = new ArrayList<>();

    private final List<Pair<Identifier,MapCodec<? extends ItemModel.Unbaked>>> itemModelTypes = new ArrayList<>();

    private final Map<Class<? extends PictureInPictureRenderState>,Supplier<PictureInPictureRenderer<?>>> pictureRenderers = new HashMap<>();

    private boolean passedRegisterRenderers;
    private boolean passedTextureStitch;

    private ClientRegistrationHandler(String modid){
        this.modid = modid;
        IEventBus eventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        eventBus.addListener(this::handleRegisterRenderersEvent);
        eventBus.addListener(this::handleRegisterMenuScreensEvent);
        eventBus.addListener(this::handleRegisterSpecialModelRenderersEvent);
        eventBus.addListener(this::handleRegisterItemModelsEvent);
        eventBus.addListener(this::handleRegisterPictureInPictureRenderersEvent);
        eventBus.addListener(this::handleRegisterBlockModelsEvent);
    }

    /**
     * Causes the model at the given location to be loaded as a block model.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerBlockStateModelConsumer(Identifier location, Consumer<BlockStateModel> consumer){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model consumer after model registry has been completed!");
        this.blockStateModelConsumers.add(Pair.of(location, consumer));
    }

    /**
     * Causes the model at the given location to be loaded as a block model.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerBlockStateModelConsumer(String namespace, String identifier, Consumer<BlockStateModel> consumer){
        if(!RegistryUtil.isValidNamespace(namespace))
            throw new IllegalArgumentException("Namespace '" + namespace + "' must only contain characters [a-z0-9_.-]!");
        if(!RegistryUtil.isValidPath(identifier))
            throw new IllegalArgumentException("Identifier '" + identifier + "' must only contain characters [a-z0-9_./-]!");

        this.registerBlockStateModelConsumer(Identifier.fromNamespaceAndPath(namespace, identifier), consumer);
    }

    /**
     * Causes the model at the given location to be loaded as a block model.
     * @param consumer called whenever the model for the given location is baked
     */
    public void registerBlockStateModelConsumer(String identifier, Consumer<BlockStateModel> consumer){
        this.registerBlockStateModelConsumer(this.modid, identifier, consumer);
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockStateModelOverwrite(Supplier<Block> block, Function<BlockStateModel,BlockStateModel> modelOverwrite){
        if(haveModelsBeenRegistered)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        this.blockStateModelOverwrites.add(Pair.of(block, modelOverwrite));
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockStateModelOverwrite(Supplier<Block> block, Supplier<BlockStateModel> modelOverwrite){
        this.registerBlockStateModelOverwrite(block, model -> modelOverwrite.get());
    }

    /**
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockStateModelOverwrite(Supplier<Block> block, BlockStateModel modelOverwrite){
        this.registerBlockStateModelOverwrite(block, model -> modelOverwrite);
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

    public void registerBuiltInBlockModel(Supplier<Block> block, BiFunction<BlockState,BlockColors,BlockModel.Unbaked> modelOverwrite){
        this.builtinBlockModels.add(Pair.of(block, modelOverwrite));
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
    public <T extends BlockEntity> void registerBlockEntityRenderer(Supplier<BlockEntityType<T>> entityType, Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<? super T,?>> blockEntityRenderer){
        if(this.passedRegisterRenderers)
            throw new IllegalStateException("Cannot register new renderers after RegisterRenderers has been fired!");

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
    public void registerAtlasSprite(Identifier textureAtlas, Identifier spriteLocation){
        if(this.passedTextureStitch)
            throw new IllegalStateException("Cannot register new models after TextureStitchEvent has been fired!");
        if(textureAtlas == null)
            throw new IllegalArgumentException("Texture atlas must not be null!");

        if(textureAtlas.getPath().startsWith("textures/atlas/") && textureAtlas.getPath().endsWith(".png"))
            textureAtlas = Identifier.fromNamespaceAndPath(textureAtlas.getNamespace(), textureAtlas.getPath().substring("textures/atlas/".length(), textureAtlas.getPath().length() - ".png".length()));

        this.textureAtlasSprites.putIfAbsent(textureAtlas, new HashSet<>());
        if(this.textureAtlasSprites.get(textureAtlas).contains(spriteLocation))
            throw new RuntimeException("Duplicate sprite registration '" + spriteLocation + "' for atlas '" + textureAtlas + "'!");

        this.textureAtlasSprites.get(textureAtlas).add(spriteLocation);
    }

    /**
     * Adds the given sprite to the given atlas.
     */
    public void registerAtlasSprite(Identifier textureAtlas, String spriteLocation){
        if(!RegistryUtil.isValidPath(spriteLocation))
            throw new IllegalArgumentException("Sprite location '" + spriteLocation + "' must only contain characters [a-z0-9_./-]!");

        this.registerAtlasSprite(textureAtlas, Identifier.fromNamespaceAndPath(this.modid, spriteLocation));
    }

    /**
     * Registers the given special model renderer.
     */
    public void registerSpecialModelRenderer(String identifier, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec){
        this.specialModelRenderers.add(Pair.of(Identifier.fromNamespaceAndPath(this.modid, identifier), codec));
    }

    /**
     * Registers the given custom item renderer.
     */
    public <S> void registerCustomItemRenderer(String identifier, Supplier<CustomItemRenderer<S>> itemRenderer){
        Holder<MapCodec<SpecialModelRenderer.Unbaked<S>>> holder = new Holder<>();
        MapCodec<SpecialModelRenderer.Unbaked<S>> codec = MapCodec.unit(new SpecialModelRenderer.Unbaked<>() {
            SpecialModelRenderer<S> renderer = null;

            @Override
            public SpecialModelRenderer<S> bake(SpecialModelRenderer.BakingContext context){
                if(this.renderer == null)
                    this.renderer = CustomItemRenderer.toSpecialModelRenderer(itemRenderer.get());
                return this.renderer;
            }

            @Override
            public MapCodec<? extends SpecialModelRenderer.Unbaked<S>> type(){
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

    public void registerItemModelType(String identifier, MapCodec<? extends ItemModel.Unbaked> codec){
        this.itemModelTypes.add(Pair.of(Identifier.fromNamespaceAndPath(this.modid, identifier), codec));
    }

    public <T extends PictureInPictureRenderState> void registerPictureInPictureRenderer(Class<T> state, Supplier<PictureInPictureRenderer<?>> renderer){
        //noinspection unchecked
        if(this.pictureRenderers.put(state, (Supplier<PictureInPictureRenderer<?>>)(Object)renderer) != null)
            throw new IllegalStateException("Duplicate picture in picture renderer registration for state class '" + state + "'!");
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
        for(Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?,?>>> entry : this.blockEntityRenderers){
            BlockEntityType<?> blockEntityType = entry.left().get();
            if(blockEntityType == null)
                throw new RuntimeException("Block entity renderer registered with null block entity type!");
            if(blockEntityTypes.contains(blockEntityType))
                throw new RuntimeException("Duplicate block entity renderer for block entity type '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(blockEntityType) + "'!");

            blockEntityTypes.add(blockEntityType);
            //noinspection unchecked,rawtypes,NullableProblems
            e.registerBlockEntityRenderer((BlockEntityType)blockEntityType, (BlockEntityRendererProvider)entry.right()::apply);
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

    private void registerBlockModelConsumerDependencies(Predicate<Identifier> markModelDependency){
        Set<Identifier> missingModels = null;
        for(Pair<Identifier,Consumer<BlockStateModel>> consumer : this.blockStateModelConsumers){
            Identifier location = consumer.left();
            if(!markModelDependency.test(location)){
                if(missingModels == null)
                    missingModels = new HashSet<>();
                missingModels.add(location);
            }
        }
        if(missingModels != null)
            CoreLib.LOGGER.error("Missing models for block model consumers from mod '{}': {}", this.modid, missingModels.stream().map(l -> "'" + l + "'").collect(Collectors.joining(", ")));
    }

    private void handleBlockModelConsumers(Function<Identifier,BlockStateModel> modelGetter){
        // Model callbacks
        for(Pair<Identifier,Consumer<BlockStateModel>> entry : this.blockStateModelConsumers){
            try{
                entry.right().accept(modelGetter.apply(entry.left()));
            }catch(Exception e){
                CoreLib.LOGGER.error("Encountered an exception whilst applying a model consumer for mod '{}'!", this.modid, e);
            }
        }
    }

    private void applyBlockModelOverwrites(Map<BlockState,BlockStateModel> models){
        for(Pair<Supplier<Block>,Function<BlockStateModel,BlockStateModel>> overwrite : this.blockStateModelOverwrites){
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

    private void applyItemModelOverwrites(Map<Identifier,ItemModel> models){
        for(Pair<Supplier<Item>,Function<ItemModel,ItemModel>> overwrite : this.itemModelOverwrites){
            Item item = overwrite.left().get();
            if(item == null){
                CoreLib.LOGGER.error("Got 'null' item for item model overwrite from mod '{}'!", this.modid);
                continue;
            }
            Identifier modelLocation = item.components().get(DataComponents.ITEM_MODEL);
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

    private void handleRegisterItemModelsEvent(RegisterItemModelsEvent e){
        // Item model types
        this.itemModelTypes.forEach(p -> e.register(p.left(), p.right()));
    }

    private void addSprites(Identifier atlas, Consumer<Identifier> spriteConsumer){
        this.passedTextureStitch = true;

        // Texture atlas sprites
        Set<Identifier> sprites = this.textureAtlasSprites.get(atlas);
        if(sprites == null)
            return;

        sprites.forEach(spriteConsumer);
    }

    private void handleRegisterPictureInPictureRenderersEvent(RegisterPictureInPictureRenderersEvent e){
        //noinspection unchecked
        this.pictureRenderers.forEach((state, renderer) -> e.register((Class<PictureInPictureRenderState>)state, (Supplier<PictureInPictureRenderer<PictureInPictureRenderState>>)(Object)renderer));
    }

    private void handleRegisterBlockModelsEvent(RegisterBlockModelsEvent e){
        // Block special renderers
        Set<Block> blocks = new HashSet<>(this.builtinBlockModels.size());
        for(Pair<Supplier<Block>,BiFunction<BlockState,BlockColors,BlockModel.Unbaked>> entry : this.builtinBlockModels){
            Block block = entry.left().get();
            if(block == null)
                throw new RuntimeException("Built-in block model registered for null block!");
            if(blocks.contains(block))
                throw new RuntimeException("Duplicate built-in block model for block '" + Registries.BLOCKS.getIdentifier(block) + "'!");

            blocks.add(block);
            BiFunction<BlockState,BlockColors,BlockModel.Unbaked> modelCreator = entry.right();
            e.register(
                (BuiltInBlockModels.ModelFactory)(colors, state) -> modelCreator.apply(state, colors),
                block
            );
        }
    }
}
