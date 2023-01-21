package com.supermartijn642.core.registry;

import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.item.EditableClientItemExtensions;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.CustomItemRenderer;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.core.util.TriFunction;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
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
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    private final Set<ResourceLocation> models = new HashSet<>();
    private final Map<ResourceLocation,Supplier<BakedModel>> specialModels = new HashMap<>();
    private final List<Pair<Predicate<ResourceLocation>,Function<BakedModel,BakedModel>>> modelOverwrites = new ArrayList<>();

    private final List<Pair<Supplier<EntityType<?>>,Function<EntityRendererProvider.Context,EntityRenderer<?>>>> entityRenderers = new ArrayList<>();
    private final List<Pair<Supplier<BlockEntityType<?>>,Function<BlockEntityRendererProvider.Context,BlockEntityRenderer<?>>>> blockEntityRenderers = new ArrayList<>();

    private final Map<ResourceLocation,Set<ResourceLocation>> textureAtlasSprites = new HashMap<>();

    private final List<Pair<Supplier<Item>,Supplier<BlockEntityWithoutLevelRenderer>>> customItemRenderers = new ArrayList<>();

    private final List<Pair<Supplier<MenuType<?>>,TriFunction<AbstractContainerMenu,Inventory,Component,Screen>>> containerScreens = new ArrayList<>();
    private final List<Pair<Supplier<Block>,Supplier<RenderType>>> blockRenderTypes = new ArrayList<>();

    private boolean passedModelRegistry;
    private boolean passedModelBake;
    private boolean passedRegisterRenderers;
    private boolean passedTextureStitch;

    private ClientRegistrationHandler(String modid){
        this.modid = modid;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleModelRegistryEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleModelBakeEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleRegisterRenderersEvent);
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
    public void registerSpecialModel(String identifier, Supplier<BakedModel> model){
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
    public void registerSpecialModel(String identifier, BakedModel model){
        this.registerSpecialModel(identifier, () -> model);
    }

    /**
     * Registers an overwrite for an already present baked model.
     */
    public void registerModelOverwrite(ResourceLocation identifier, Function<BakedModel,BakedModel> modelOverwrite){
        if(this.passedModelBake)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");
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
     * Registers an overwrite for all models for the given block, including the block's item model.
     */
    public void registerBlockModelOverwrite(Supplier<Block> block, Function<BakedModel,BakedModel> modelOverwrite){
        if(this.passedModelBake)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        this.modelOverwrites.add(Pair.of(identifier -> {
            ResourceLocation blockIdentifier = Registries.BLOCKS.getIdentifier(block.get());
            return identifier instanceof ModelResourceLocation
                && identifier.getNamespace().equals(blockIdentifier.getNamespace())
                && identifier.getPath().equals(blockIdentifier.getPath());
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
        if(this.passedModelBake)
            throw new IllegalStateException("Cannot register new model overwrites after ModelBakeEvent has been fired!");

        this.modelOverwrites.add(Pair.of(identifier -> {
            ResourceLocation itemIdentifier = Registries.ITEMS.getIdentifier(item.get());
            return identifier.getNamespace().equals(itemIdentifier.getNamespace())
                && (identifier instanceof ModelResourceLocation ?
                identifier.getPath().equals(itemIdentifier.getPath()) && ((ModelResourceLocation)identifier).getVariant().equals("inventory")
                : identifier.getPath().equals("item/" + itemIdentifier.getPath()));
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
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, Function<EntityRendererProvider.Context,EntityRenderer<? super T>> entityRenderer){
        if(this.passedRegisterRenderers)
            throw new IllegalStateException("Cannot register new renderers after RegisterRenderers has been fired!");

        this.entityRenderers.add(Pair.of((Supplier<EntityType<?>>)(Object)entityType, (Function<EntityRendererProvider.Context,EntityRenderer<?>>)(Object)entityRenderer));
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, Supplier<EntityRenderer<? super T>> entityRenderer){
        this.registerEntityRenderer(entityType, context -> entityRenderer.get());
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, EntityRenderer<? super T> entityRenderer){
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
    public void registerAtlasSprite(ResourceLocation textureAtlas, String spriteLocation){
        if(this.passedTextureStitch)
            throw new IllegalStateException("Cannot register new models after TextureStitchEvent has been fired!");
        if(textureAtlas == null)
            throw new IllegalArgumentException("Texture atlas must not be null!");
        if(!RegistryUtil.isValidPath(spriteLocation))
            throw new IllegalArgumentException("Sprite location '" + spriteLocation + "' must only contain characters [a-z0-9_./-]!");

        if(textureAtlas.getPath().startsWith("textures/atlas/") && textureAtlas.getPath().endsWith(".png"))
            textureAtlas = new ResourceLocation(textureAtlas.getNamespace(), textureAtlas.getPath().substring("textures/atlas/".length(), textureAtlas.getPath().length() - ".png".length()));

        ResourceLocation fullSpriteLocation = new ResourceLocation(this.modid, spriteLocation);
        this.textureAtlasSprites.putIfAbsent(textureAtlas, new HashSet<>());
        if(this.textureAtlasSprites.get(textureAtlas).contains(fullSpriteLocation))
            throw new RuntimeException("Duplicate sprite registration '" + fullSpriteLocation + "' for atlas '" + textureAtlas + "'!");

        this.textureAtlasSprites.get(textureAtlas).add(fullSpriteLocation);
    }

    /**
     * Registers the given custom item renderer for the given item. The given item must provide an instance of {@link EditableClientItemExtensions} in its {@link Item#initializeClient(Consumer)} method.
     */
    public void registerItemRenderer(Supplier<Item> item, Supplier<BlockEntityWithoutLevelRenderer> itemRenderer){
        if(this.passedRegisterRenderers)
            throw new IllegalStateException("Cannot register new renderers after item RegistryEvent has been fired!");

        this.customItemRenderers.add(Pair.of(item, itemRenderer));
    }

    /**
     * Registers the given custom item renderer for the given item. The given item must provide an instance of {@link EditableClientItemExtensions} in its {@link Item#initializeClient(Consumer)} method.
     */
    public void registerItemRenderer(Supplier<Item> item, BlockEntityWithoutLevelRenderer itemRenderer){
        this.registerItemRenderer(item, () -> itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item. The given item must provide an instance of {@link EditableClientItemExtensions} in its {@link Item#initializeClient(Consumer)} method.
     */
    public void registerItemRenderer(Item item, Supplier<BlockEntityWithoutLevelRenderer> itemRenderer){
        this.registerItemRenderer(() -> item, itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item. The given item must provide an instance of {@link EditableClientItemExtensions} in its {@link Item#initializeClient(Consumer)} method.
     */
    public void registerItemRenderer(Item item, BlockEntityWithoutLevelRenderer itemRenderer){
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
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderType(ResourceLocation)} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelRenderType(Supplier<Block> block, Supplier<RenderType> renderTypeSupplier){
        if(this.passedRegisterRenderers)
            throw new IllegalStateException("Cannot register new menu screens after the ClientInitialization event has been fired!");

        this.blockRenderTypes.add(Pair.of(block, renderTypeSupplier));
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderType(ResourceLocation)} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelRenderType(Supplier<Block> block, RenderType renderType){
        this.registerBlockModelRenderType(block, renderType);
    }

    /**
     * Registers the given render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderType(ResourceLocation)} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelRenderType(Block block, Supplier<RenderType> renderTypeSupplier){
        this.registerBlockModelRenderType(() -> block, renderTypeSupplier);
    }

    /**
     * Registers the solid render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderTypeSolid()} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelSolidRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, RenderType::solid);
    }

    /**
     * Registers the solid render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderTypeSolid()} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelSolidRenderType(Block block){
        this.registerBlockModelRenderType(block, RenderType::solid);
    }

    /**
     * Registers the cutout mipped render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderTypeCutoutMipped()} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelCutoutMippedRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, RenderType::cutoutMipped);
    }

    /**
     * Registers the cutout mipped render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderTypeCutoutMipped()} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelCutoutMippedRenderType(Block block){
        this.registerBlockModelRenderType(block, RenderType::cutoutMipped);
    }

    /**
     * Registers the cutout render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderTypeCutout()} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelCutoutRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, RenderType::cutout);
    }

    /**
     * Registers the cutout render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderTypeCutout()} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelCutoutRenderType(Block block){
        this.registerBlockModelRenderType(block, RenderType::cutout);
    }

    /**
     * Registers the translucent render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderTypeTranslucent()} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelTranslucentRenderType(Supplier<Block> block){
        this.registerBlockModelRenderType(block, RenderType::translucent);
    }

    /**
     * Registers the translucent render type to be used when rendering the given block.
     * @deprecated use {@link ModelGenerator.ModelBuilder#renderTypeTranslucent()} to set the render type when generating the model
     * or use {@link BakedModel#getRenderTypes(BlockState, RandomSource, ModelData)} to give the render types directly.
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated
    public void registerBlockModelTranslucentRenderType(Block block){
        this.registerBlockModelRenderType(block, RenderType::translucent);
    }

    private void handleModelRegistryEvent(ModelEvent.RegisterAdditional e){
        this.passedModelRegistry = true;

        // Additional models
        for(ResourceLocation model : this.models)
            e.register(model);
    }

    private void handleModelBakeEvent(ModelEvent.ModifyBakingResult e){
        this.passedModelBake = true;

        // Special models
        for(Map.Entry<ResourceLocation,Supplier<BakedModel>> entry : this.specialModels.entrySet()){
            ResourceLocation identifier = entry.getKey();
            if(e.getModels().containsKey(identifier))
                throw new RuntimeException("Special model '" + identifier + "' is trying to overwrite another model!");

            BakedModel model = entry.getValue().get();
            if(model == null)
                throw new RuntimeException("Got null object for special model '" + entry.getKey() + "'!");

            e.getModels().put(entry.getKey(), model);
        }

        // Model overwrites
        for(Pair<Predicate<ResourceLocation>,Function<BakedModel,BakedModel>> pair : this.modelOverwrites){
            // Find all the identifiers which should be replaced
            List<ResourceLocation> modelIdentifiers = e.getModels().keySet().stream()
                .filter(identifier -> pair.left().test(identifier))
                .collect(Collectors.toList());

            for(ResourceLocation identifier : modelIdentifiers){
                if(!e.getModels().containsKey(identifier))
                    throw new RuntimeException("No model registered for model overwrite '" + identifier + "'!");

                BakedModel model = e.getModels().get(identifier);
                model = pair.right().apply(model);
                if(model == null)
                    throw new RuntimeException("Model overwrite for '" + identifier + "' returned a null model!");

                e.getModels().put(identifier, model);
            }
        }
    }

    private void handleRegisterRenderersEvent(EntityRenderersEvent.RegisterRenderers e){
        this.passedRegisterRenderers = true;

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

        // Custom item renderers
        Set<Item> items = new HashSet<>();
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

        // Container Screens
        Set<MenuType<?>> menuTypes = new HashSet<>();
        for(Pair<Supplier<MenuType<?>>,TriFunction<AbstractContainerMenu,Inventory,Component,Screen>> entry : this.containerScreens){
            MenuType<?> menuType = entry.left().get();
            if(menuType == null)
                throw new RuntimeException("Container screen registered with null menu type!");
            if(menuTypes.contains(menuType))
                throw new RuntimeException("Duplicate container screen for menu type '" + Registries.MENU_TYPES.getIdentifier(menuType) + "'!");

            menuTypes.add(menuType);
            //noinspection unchecked,rawtypes,NullableProblems
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
            //noinspection removal
            ItemBlockRenderTypes.setRenderLayer(block, renderType);
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
