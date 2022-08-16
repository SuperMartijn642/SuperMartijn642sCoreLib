package com.supermartijn642.core.registry;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreLib;
import com.supermartijn642.core.extensions.EntityRendererManagerExtension;
import com.supermartijn642.core.extensions.TileEntityRendererDispatcherExtension;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.CustomItemRenderer;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.core.util.TriFunction;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 14/07/2022 by SuperMartijn642
 */
public class ClientRegistrationHandler {

    /**
     * {@link Item.teisr}
     */
    private static final Field itemIster;

    static{
        try{
            itemIster = Item.class.getDeclaredField("teisr");
        }catch(NoSuchFieldException e){
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Supplier<ItemStackTileEntityRenderer> getItemCustomRenderer(Item item){
        try{
            return (Supplier<ItemStackTileEntityRenderer>)itemIster.get(item);
        }catch(IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }

    private static void setItemCustomRenderer(Item item, Supplier<ItemStackTileEntityRenderer> customRenderer){
        try{
            itemIster.set(item, customRenderer);
        }catch(IllegalAccessException e){
            throw new RuntimeException(e);
        }
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
    public static ClientRegistrationHandler get(String modid){
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

    private final String modid;

    private final Set<ResourceLocation> models = new HashSet<>();
    private final Map<ResourceLocation,Supplier<IBakedModel>> specialModels = new HashMap<>();
    private final Map<ResourceLocation,Function<IBakedModel,IBakedModel>> modelOverwrites = new HashMap<>();

    private final List<Pair<Supplier<EntityType<?>>,Supplier<EntityRenderer<?>>>> entityRenderers = new ArrayList<>();
    private final List<Pair<Supplier<TileEntityType<?>>,Function<TileEntityRendererDispatcher,TileEntityRenderer<?>>>> blockEntityRenderers = new ArrayList<>();

    private final Map<ResourceLocation,Set<ResourceLocation>> textureAtlasSprites = new HashMap<>();

    private final List<Pair<Supplier<Item>,Supplier<ItemStackTileEntityRenderer>>> customItemRenderers = new ArrayList<>();

    private final List<Pair<Supplier<ContainerType<?>>,TriFunction<Container,PlayerInventory,ITextComponent,Screen>>> containerScreens = new ArrayList<>();

    private boolean passedModelRegistry;
    private boolean passedModelBake;
    private boolean passedClientSetup;
    private boolean passedTextureStitch;

    private ClientRegistrationHandler(String modid){
        this.modid = modid;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleModelRegistryEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleModelBakeEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleRegisterRenderersEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::handleTextureStitchEvent);
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
        if(this.modelOverwrites.containsKey(fullIdentifier))
            throw new RuntimeException("Overlapping special model and model overwrite '" + fullIdentifier + "'!");

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
        if(this.modelOverwrites.containsKey(identifier))
            throw new RuntimeException("Duplicate model overwrite '" + identifier + "'!");
        if(this.specialModels.containsKey(identifier))
            throw new RuntimeException("Overlapping special model and model overwrite '" + identifier + "'!");

        this.modelOverwrites.put(identifier, modelOverwrite);
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
     * Registers the given entity renderer for the given entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, Supplier<EntityRenderer<? super T>> entityRenderer){
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new renderers after RegisterRenderers has been fired!");

        this.entityRenderers.add(Pair.of((Supplier<EntityType<?>>)(Object)entityType, (Supplier<EntityRenderer<?>>)(Object)entityRenderer));
    }

    /**
     * Registers the given entity renderer for the given entity type.
     */
    public <T extends Entity> void registerEntityRenderer(Supplier<EntityType<T>> entityType, EntityRenderer<? super T> entityRenderer){
        this.registerEntityRenderer(entityType, () -> entityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    @SuppressWarnings("unchecked")
    public <T extends TileEntity> void registerBlockEntityRenderer(Supplier<TileEntityType<T>> entityType, Function<TileEntityRendererDispatcher,TileEntityRenderer<? super T>> blockEntityRenderer){
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new renderers after RegisterRenderers has been fired!");

        this.blockEntityRenderers.add(Pair.of((Supplier<TileEntityType<?>>)(Object)entityType, (Function<TileEntityRendererDispatcher,TileEntityRenderer<?>>)(Object)blockEntityRenderer));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends TileEntity> void registerBlockEntityRenderer(Supplier<TileEntityType<T>> entityType, Supplier<TileEntityRenderer<? super T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer.get());
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends TileEntity> void registerBlockEntityRenderer(Supplier<TileEntityType<T>> entityType, TileEntityRenderer<? super T> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> blockEntityRenderer);
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends TileEntity> void registerCustomBlockEntityRenderer(Supplier<TileEntityType<T>> entityType, Supplier<CustomBlockEntityRenderer<? super T>> blockEntityRenderer){
        this.registerBlockEntityRenderer(entityType, context -> CustomBlockEntityRenderer.of(blockEntityRenderer.get()));
    }

    /**
     * Registers the given block entity renderer for the given block entity type.
     */
    public <T extends TileEntity> void registerCustomBlockEntityRenderer(Supplier<TileEntityType<T>> entityType, CustomBlockEntityRenderer<? super T> blockEntityRenderer){
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
    public void registerItemRenderer(Supplier<Item> item, Supplier<ItemStackTileEntityRenderer> itemRenderer){
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new renderers after item RegistryEvent has been fired!");

        this.customItemRenderers.add(Pair.of(item, itemRenderer));
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Supplier<Item> item, ItemStackTileEntityRenderer itemRenderer){
        this.registerItemRenderer(item, () -> itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Item item, Supplier<ItemStackTileEntityRenderer> itemRenderer){
        this.registerItemRenderer(() -> item, itemRenderer);
    }

    /**
     * Registers the given custom item renderer for the given item.
     */
    public void registerItemRenderer(Item item, ItemStackTileEntityRenderer itemRenderer){
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
    public <T extends Container, U extends Screen & IHasContainer<T>> void registerContainerScreen(Supplier<ContainerType<T>> menuType, TriFunction<T,PlayerInventory,ITextComponent,U> screenSupplier){
        if(this.passedClientSetup)
            throw new IllegalStateException("Cannot register new menu screens after the ClientInitialization event has been fired!");

        //noinspection unchecked
        this.containerScreens.add(Pair.of((Supplier<ContainerType<?>>)(Object)menuType, (TriFunction<Container,PlayerInventory,ITextComponent,Screen>)(Object)screenSupplier));
    }

    /**
     * Registers the given screen constructor for the given menu type.
     */
    public <T extends Container, U extends Screen & IHasContainer<T>> void registerContainerScreen(Supplier<ContainerType<T>> menuType, Function<T,U> screenSupplier){
        this.registerContainerScreen(menuType, (container, inventory, title) -> screenSupplier.apply(container));
    }

    /**
     * Registers the given screen constructor for the given menu type.
     */
    public <T extends Container, U extends Screen & IHasContainer<T>> void registerContainerScreen(ContainerType<T> menuType, TriFunction<T,PlayerInventory,ITextComponent,U> screenSupplier){
        this.registerContainerScreen(() -> menuType, screenSupplier);
    }

    /**
     * Registers the given screen constructor for the given menu type.
     */
    public <T extends Container, U extends Screen & IHasContainer<T>> void registerContainerScreen(ContainerType<T> menuType, Function<T,U> screenSupplier){
        this.registerContainerScreen(() -> menuType, (container, inventory, title) -> screenSupplier.apply(container));
    }

    private void handleModelRegistryEvent(ModelRegistryEvent e){
        this.passedModelRegistry = true;

        // Additional models
        for(ResourceLocation model : this.models)
            ModelLoader.addSpecialModel(model);
    }

    private void handleModelBakeEvent(ModelBakeEvent e){
        this.passedModelBake = true;

        // Special models
        for(Map.Entry<ResourceLocation,Supplier<IBakedModel>> entry : this.specialModels.entrySet()){
            ResourceLocation identifier = entry.getKey();
            if(e.getModelRegistry().containsKey(identifier))
                throw new RuntimeException("Special model '" + identifier + "' is trying to overwrite another model!");

            IBakedModel model = entry.getValue().get();
            if(model == null)
                throw new RuntimeException("Got null object for special model '" + entry.getKey() + "'!");

            e.getModelRegistry().put(entry.getKey(), model);
        }

        // Model overwrites
        for(Map.Entry<ResourceLocation,Function<IBakedModel,IBakedModel>> entry : this.modelOverwrites.entrySet()){
            ResourceLocation identifier = entry.getKey();
            if(!e.getModelRegistry().containsKey(identifier))
                throw new RuntimeException("No model registered for model overwrite '" + identifier + "'!");

            IBakedModel model = e.getModelRegistry().get(identifier);
            model = entry.getValue().apply(model);
            if(model == null)
                throw new RuntimeException("Model overwrite '" + identifier + "' returned a null model!");

            e.getModelRegistry().put(identifier, model);
        }
    }

    private void handleRegisterRenderersEvent(FMLClientSetupEvent e){
        this.passedClientSetup = true;

        // Entity renderers
        Set<EntityType<?>> entityTypes = new HashSet<>();
        for(Pair<Supplier<EntityType<?>>,Supplier<EntityRenderer<?>>> entry : this.entityRenderers){
            EntityType<?> entityType = entry.left().get();
            if(entityType == null)
                throw new RuntimeException("Entity renderer registered with null entity type!");
            if(entityTypes.contains(entityType))
                throw new RuntimeException("Duplicate entity renderer for entity type '" + Registries.ENTITY_TYPES.getIdentifier(entityType) + "'!");

            EntityRenderer<?> entityRenderer = entry.right().get();
            if(entityRenderer == null)
                throw new RuntimeException("Got null entity renderer for entity type '" + Registries.ENTITY_TYPES.getIdentifier(entityType) + "!");

            entityTypes.add(entityType);
            //noinspection unchecked,rawtypes
            ((EntityRendererManagerExtension)ClientUtils.getMinecraft().getEntityRenderDispatcher()).coreLibRegisterRenderer((EntityType)entityType, (EntityRenderer)entityRenderer);
        }

        // Entity renderers
        Set<TileEntityType<?>> blockEntityTypes = new HashSet<>();
        for(Pair<Supplier<TileEntityType<?>>,Function<TileEntityRendererDispatcher,TileEntityRenderer<?>>> entry : this.blockEntityRenderers){
            TileEntityType<?> blockEntityType = entry.left().get();
            if(blockEntityType == null)
                throw new RuntimeException("Block entity renderer registered with null block entity type!");
            if(blockEntityTypes.contains(blockEntityType))
                throw new RuntimeException("Duplicate block entity renderer for block entity type '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(blockEntityType) + "'!");

            TileEntityRenderer<?> entityRenderer = entry.right().apply(TileEntityRendererDispatcher.instance);
            if(entityRenderer == null)
                throw new RuntimeException("Got null block entity renderer for block entity type '" + Registries.BLOCK_ENTITY_TYPES.getIdentifier(blockEntityType) + "'!");

            blockEntityTypes.add(blockEntityType);
            //noinspection unchecked,rawtypes
            ((TileEntityRendererDispatcherExtension)TileEntityRendererDispatcher.instance).coreLibRegisterRenderer((TileEntityType)blockEntityType, (TileEntityRenderer)entityRenderer);
        }

        // Custom item renderers
        Set<Item> items = new HashSet<>();
        for(Pair<Supplier<Item>,Supplier<ItemStackTileEntityRenderer>> entry : this.customItemRenderers){
            Item item = entry.left().get();
            if(item == null)
                throw new RuntimeException("Custom item renderer registered with null item!");
            if(items.contains(item))
                throw new RuntimeException("Duplicate custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "'!");
            if(getItemCustomRenderer(item) != null)
                throw new RuntimeException("Item '" + Registries.ITEMS.getIdentifier(item) + "' already has a custom item renderer set!");

            ItemStackTileEntityRenderer customRenderer = entry.right().get();
            if(customRenderer == null)
                throw new RuntimeException("Got null custom item renderer for item '" + Registries.ITEMS.getIdentifier(item) + "'!");

            items.add(item);
            setItemCustomRenderer(item, () -> customRenderer);
        }

        // Container Screens
        Set<ContainerType<?>> menuTypes = new HashSet<>();
        for(Pair<Supplier<ContainerType<?>>,TriFunction<Container,PlayerInventory,ITextComponent,Screen>> entry : this.containerScreens){
            ContainerType<?> menuType = entry.left().get();
            if(menuType == null)
                throw new RuntimeException("Container screen registered with null menu type!");
            if(menuTypes.contains(menuType))
                throw new RuntimeException("Duplicate container screen for menu type '" + Registries.MENU_TYPES.getIdentifier(menuType) + "'!");

            menuTypes.add(menuType);
            //noinspection unchecked,rawtypes,NullableProblems
            ScreenManager.register((ContainerType)menuType, (ScreenManager.IScreenFactory)entry.right()::apply);
        }
    }

    private void handleTextureStitchEvent(TextureStitchEvent.Pre e){
        this.passedTextureStitch = true;

        // Texture atlas sprites
        Set<ResourceLocation> sprites = this.textureAtlasSprites.get(e.getMap().getBasePath()); // TODO
        if(sprites == null)
            return;

        sprites.forEach(e::addSprite);
    }
}
