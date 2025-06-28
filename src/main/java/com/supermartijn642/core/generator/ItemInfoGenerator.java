package com.supermartijn642.core.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 22/12/2024 by SuperMartijn642
 */
public abstract class ItemInfoGenerator extends ResourceGenerator {

    private final Map<ResourceLocation,ItemInfoBuilder> infos = new HashMap<>();

    public ItemInfoGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void save(){
        DynamicOps<JsonElement> ops = ResourceGenerator.registryAccess.createSerializationContext(JsonOps.INSTANCE);
        // Loop over all item infos
        for(ItemInfoBuilder builder : this.infos.values()){
            // Serialize to json
            JsonObject json = ClientItem.CODEC.encodeStart(ops, builder.toClientItem()).getOrThrow().getAsJsonObject();
            // Save the object to the cache
            ResourceLocation identifier = builder.location;
            this.cache.saveJsonResource(ResourceType.ASSET, json, identifier.getNamespace(), "items", identifier.getPath());
        }
    }

    protected ItemInfoBuilder info(ResourceLocation item){
        return this.infos.computeIfAbsent(item, ItemInfoBuilder::new);
    }

    protected ItemInfoBuilder info(String namespace, String identifier){
        return this.info(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
    }

    protected ItemInfoBuilder info(String identifier){
        return this.info(this.modid, identifier);
    }

    protected ItemInfoBuilder info(ItemLike item){
        return this.info(Registries.ITEMS.getIdentifier(item.asItem()));
    }

    protected ItemInfoBuilder simpleInfo(ItemLike item, String model){
        return this.info(item).model(this.model(model));
    }

    protected ModelModelBuilder model(ResourceLocation location){
        return new ModelModelBuilder(location);
    }

    protected ModelModelBuilder model(String namespace, String path){
        return this.model(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    protected ModelModelBuilder model(String location){
        return this.model(this.modid, location);
    }

    protected CompositeModelBuilder compositeModel(){
        return new CompositeModelBuilder();
    }

    protected ModelBuilder emptyModel(){
        return ModelBuilder.of(new EmptyModel.Unbaked());
    }

    /**
     * @param baseModel model used for transformations, particle texture, and gui lighting
     */
    protected ModelBuilder specialModel(SpecialModelRenderer.Unbaked specialModel, ResourceLocation baseModel){
        return ModelBuilder.of(new SpecialModelWrapper.Unbaked(baseModel, specialModel));
    }

    @Override
    public String getName(){
        return this.modName + " Item Info Generator";
    }

    protected static class ItemInfoBuilder {

        private final ResourceLocation location;
        private boolean handAnimationOnSwap = true;
        private boolean oversizedInGui = false;
        private ModelBuilder model;

        protected ItemInfoBuilder(ResourceLocation location){
            this.location = location;
        }

        public ItemInfoBuilder noHandAnimationOnSwap(){
            this.handAnimationOnSwap = false;
            return this;
        }

        public ItemInfoBuilder oversizedInGui(){
            this.oversizedInGui = true;
            return this;
        }

        public ItemInfoBuilder model(ModelBuilder model){
            this.model = model;
            return this;
        }

        public ItemInfoBuilder model(ItemModel.Unbaked model){
            return this.model(ModelBuilder.of(model));
        }

        private ClientItem toClientItem(){
            ItemModel.Unbaked model = this.model == null ? new EmptyModel.Unbaked() : this.model.toItemModel();
            return new ClientItem(model, new ClientItem.Properties(this.handAnimationOnSwap, this.oversizedInGui));
        }
    }

    protected static abstract class ModelBuilder {

        private ModelBuilder(){
        }

        abstract ItemModel.Unbaked toItemModel();

        private static ModelBuilder of(Object model){
            // TODO temporary fix to prevent ItemModel.Unbaked class from being loaded when this class is
            // Referencing the constructor for this class like 'handler.addGenerator(PackedUpItemInfoGenerator::new)' makes it load the class
            // Ideally, rework GeneratorRegistrationHandler, so the generator classes aren't loaded at all unless actually running datagen
            if(!(model instanceof ItemModel.Unbaked))
                throw new IllegalArgumentException("Model must be an instance of " + ItemModel.Unbaked.class.getName());
            return new ModelBuilder() {
                @Override
                ItemModel.Unbaked toItemModel(){
                    return (ItemModel.Unbaked)model;
                }
            };
        }
    }

    protected static class ModelModelBuilder extends ModelBuilder {

        private final ResourceLocation model;
        private final List<ItemTintSource> tintSources = new ArrayList<>();

        protected ModelModelBuilder(ResourceLocation model){
            this.model = model;
        }

        @Override
        protected ItemModel.Unbaked toItemModel(){
            return new BlockModelWrapper.Unbaked(this.model, this.tintSources);
        }

        public ModelModelBuilder addTintSource(ItemTintSource tintSource){
            this.tintSources.add(tintSource);
            return this;
        }

        public ModelModelBuilder addConstantTint(int rgb){
            return this.addTintSource(new Constant(rgb));
        }

        public ModelModelBuilder addConstantTint(float red, float green, float blue){
            if(red < 0 || red > 1 || green < 0 || green > 1 || blue < 0 || blue > 1)
                throw new IllegalArgumentException("Color component values must be between 0 and 1!");
            return this.addTintSource(new Constant(ARGB.colorFromFloat(1, red, green, blue)));
        }

        /**
         * Uses the color from the item's {@link DataComponents#DYED_COLOR} component as tint.
         * @param defaultColor the color to use when an item does not have a {@link DataComponents#DYED_COLOR} component
         */
        public ModelModelBuilder addDyeComponentTint(int defaultColor){
            return this.addTintSource(new Dye(defaultColor));
        }

        public ModelModelBuilder addGrassTint(float temperature, float downfall){
            if(temperature < 0 || temperature > 1)
                throw new IllegalArgumentException("Temperature must be between 0 and 1!");
            if(downfall < 0 || downfall > 1)
                throw new IllegalArgumentException("Downfall must be between 0 and 1!");
            return this.addTintSource(new GrassColorSource(temperature, downfall));
        }
    }

    protected static class CompositeModelBuilder extends ModelBuilder {
        private final List<ModelBuilder> models = new ArrayList<>();

        @Override
        protected ItemModel.Unbaked toItemModel(){
            return new CompositeModel.Unbaked(this.models.stream().map(ModelBuilder::toItemModel).toList());
        }

        public CompositeModelBuilder addModel(ModelBuilder model){
            this.models.add(model);
            return this;
        }

        public CompositeModelBuilder addModel(ItemModel.Unbaked model){
            return this.addModel(ModelBuilder.of(model));
        }
    }
}
