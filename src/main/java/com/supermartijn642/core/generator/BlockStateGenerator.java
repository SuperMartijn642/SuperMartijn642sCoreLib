package com.supermartijn642.core.generator;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created 19/08/2022 by SuperMartijn642
 */
public abstract class BlockStateGenerator extends ResourceGenerator {

    private final Map<Block,BlockStateBuilder> blockStates = new HashMap<>();

    public BlockStateGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    @Override
    public void save(){
        // Loop over all block states
        for(BlockStateBuilder blockStateBuilder : this.blockStates.values()){
            ResourceLocation block = Registries.BLOCKS.getIdentifier(blockStateBuilder.block);
            JsonObject json = new JsonObject();
            // Loop over all variants
            JsonObject variantsJson = new JsonObject();
            for(Map.Entry<PartialBlockState,VariantBuilder> variantEntry : blockStateBuilder.variants.entrySet()){
                if(variantEntry.getValue().models.isEmpty())
                    continue;
                String name = formatVariantName(variantEntry.getKey());
                JsonObject[] models = new JsonObject[variantEntry.getValue().models.size()];
                for(int i = 0; i < models.length; i++){
                    VariantModel model = variantEntry.getValue().models.get(i);
                    JsonObject modelJson = new JsonObject();
                    // Model location
                    if(!this.cache.doesResourceExist(ResourceType.ASSET, model.modelLocation.getNamespace(), "models", model.modelLocation.getPath(), ".json"))
                        throw new RuntimeException("Could not find model '" + model.modelLocation + "' in block state for block '" + block + "'!");
                    modelJson.addProperty("model", model.modelLocation.toString());
                    // Rotation
                    if(model.xRotation != 0)
                        modelJson.addProperty("x", model.xRotation);
                    if(model.yRotation != 0)
                        modelJson.addProperty("y", model.yRotation);
                    // UV lock
                    if(model.uvLock)
                        modelJson.addProperty("uvlock", true);
                    // Weight
                    if(model.weight != 1 && models.length > 1)
                        modelJson.addProperty("weight", model.weight);
                    models[i] = modelJson;
                }
                variantsJson.add(name, models.length > 1 ? createJsonArray(models) : models[0]);
            }
            json.add("variants", variantsJson);

            // Save the object to the cache
            this.cache.saveJsonResource(ResourceType.ASSET, json, this.modid, "blockstates", block.getPath());
        }
    }

    private static JsonArray createJsonArray(JsonElement... elements){
        // Because they can't just make a proper json array constructor...
        JsonArray array = new JsonArray(elements.length);
        for(JsonElement element : elements)
            array.add(element);
        return array;
    }

    private static String formatVariantName(PartialBlockState state){
        //noinspection unchecked,rawtypes
        return state.properties.entrySet().stream().map(entry -> entry.getKey().getName() + "=" + ((Property)entry.getKey()).getName(entry.getValue())).collect(Collectors.joining(","));
    }

    /**
     * Creates a partial state builder for the given block.
     * @param block block to create a partial state builder for
     */
    protected PartialBlockStateBuilder createPartialStateBuilder(Block block){
        return new PartialBlockStateBuilder(block);
    }

    /**
     * Creates an empty partial state for the given block.
     * @param block block to create an empty partial state for
     */
    protected PartialBlockState createEmptyPartialState(Block block){
        return this.createPartialStateBuilder(block).build();
    }

    /**
     * Creates a partial state with the properties from the given state.
     * @param state state to copy the properties from
     */
    protected PartialBlockState createPartialState(BlockState state){
        return this.createPartialStateBuilder(state.getBlock()).copy(state).build();
    }

    /**
     * Gets a block state builder for the given block. The returned block state builder may be a new block state builder or an existing one if requested before.
     * @param block block to get a block state builder for
     */
    protected BlockStateBuilder blockState(Block block){
        ResourceLocation identifier = Registries.BLOCKS.getIdentifier(block);
        this.cache.trackToBeGeneratedResource(ResourceType.DATA, identifier.getNamespace(), "blockstates", identifier.getPath(), ".json");
        return this.blockStates.computeIfAbsent(block, BlockStateBuilder::new);
    }

    @Override
    public String getName(){
        return this.modName + " Block State Generator";
    }

    protected class BlockStateBuilder {

        private final Block block;
        private final Map<PartialBlockState,VariantBuilder> variants = new HashMap<>();

        public BlockStateBuilder(Block block){
            this.block = block;
        }

        /**
         * Constructs model options for a given variant.
         * @param state                  the variant
         * @param variantBuilderConsumer consumer to build the model options
         */
        public BlockStateBuilder variant(PartialBlockState state, Consumer<VariantBuilder> variantBuilderConsumer){
            if(state.block != this.block)
                throw new IllegalArgumentException("Cannot use state from block '" + state.block + "' in block state builder for block '" + this.block + "'!");

            variantBuilderConsumer.accept(this.variants.computeIfAbsent(state, o -> new VariantBuilder()));
            return this;
        }

        /**
         * Constructs model options for a given variant.
         * @param state                  the variant
         * @param variantBuilderConsumer consumer to build the model options
         */
        public BlockStateBuilder variant(BlockState state, Consumer<VariantBuilder> variantBuilderConsumer){
            return this.variant(BlockStateGenerator.this.createPartialState(state), variantBuilderConsumer);
        }

        /**
         * Constructs model options for the variant with empty key.
         * @param variantBuilderConsumer consumer to build the model options
         */
        public BlockStateBuilder emptyVariant(Consumer<VariantBuilder> variantBuilderConsumer){
            return this.variant(BlockStateGenerator.this.createEmptyPartialState(this.block), variantBuilderConsumer);
        }

        /**
         * Constructs model options for all values of the given property.
         * @param property               property to iterate all values for
         * @param variantBuilderConsumer consumer to build the model options
         */
        public BlockStateBuilder variantsForProperty(Property<?> property, BiConsumer<PartialBlockState,VariantBuilder> variantBuilderConsumer){
            if(!this.block.getStateDefinition().getProperties().contains(property))
                throw new IllegalArgumentException("Property '" + property + "' is not a property of block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");

            PartialBlockStateBuilder builder = BlockStateGenerator.this.createPartialStateBuilder(this.block);
            for(Comparable<?> value : property.getPossibleValues()){
                //noinspection rawtypes,unchecked
                PartialBlockState state = builder.set((Property)property, (Comparable)value).build();
                variantBuilderConsumer.accept(state, this.variants.computeIfAbsent(state, o -> new VariantBuilder()));
            }
            return this;
        }

        /**
         * Constructs model options for all possible variants except the given property.
         * @param variantBuilderConsumer consumer to build the model options
         * @param excluded               properties which should not be considered
         */
        public BlockStateBuilder variantsForAllExcept(BiConsumer<PartialBlockState,VariantBuilder> variantBuilderConsumer, Property<?>... excluded){
            PartialBlockStateBuilder builder = BlockStateGenerator.this.createPartialStateBuilder(this.block);
            List<Property<?>> properties = this.block.getStateDefinition().getProperties().stream().filter(property -> Arrays.stream(excluded).noneMatch(p -> p == property)).collect(Collectors.toList());
            this.loopThroughAll(builder, properties, 0, variantBuilderConsumer);
            return this;
        }

        private void loopThroughAll(PartialBlockStateBuilder builder, List<Property<?>> properties, int index, BiConsumer<PartialBlockState,VariantBuilder> variantBuilderConsumer){
            if(index == properties.size()){
                PartialBlockState state = builder.build();
                variantBuilderConsumer.accept(state, this.variants.computeIfAbsent(state, o -> new VariantBuilder()));
                return;
            }

            Property<?> property = properties.get(index);
            for(Comparable<?> value : property.getPossibleValues()){
                //noinspection rawtypes,unchecked
                builder.set((Property)property, (Comparable)value);
                this.loopThroughAll(builder, properties, index + 1, variantBuilderConsumer);
            }
        }

        /**
         * Constructs model options for all possible variants.
         * @param variantBuilderConsumer consumer to build the model options
         */
        public BlockStateBuilder variantsForAll(BiConsumer<PartialBlockState,VariantBuilder> variantBuilderConsumer){
            return this.variantsForAllExcept(variantBuilderConsumer);
        }
    }

    protected static class VariantBuilder {

        private final List<VariantModel> models = new ArrayList<>();

        protected VariantBuilder(){
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param modelLocation location of the model
         * @param xRotation     rotation around the x-axis for the model
         * @param yRotation     rotation around the y-axis for the model
         * @param uvLock        whether to apply uv lock to the model
         * @param weight        weight of the model when considering which model to pick
         */
        public VariantBuilder model(ResourceLocation modelLocation, int xRotation, int yRotation, boolean uvLock, int weight){
            this.models.add(new VariantModel(modelLocation, xRotation, yRotation, uvLock, weight));
            return this;
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param modelLocation location of the model
         * @param xRotation     rotation around the x-axis for the model
         * @param yRotation     rotation around the y-axis for the model
         * @param uvLock        whether to apply uv lock to the model
         */
        public VariantBuilder model(ResourceLocation modelLocation, int xRotation, int yRotation, boolean uvLock){
            return this.model(modelLocation, xRotation, yRotation, uvLock, 1);
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param modelLocation location of the model
         * @param xRotation     rotation around the x-axis for the model
         * @param yRotation     rotation around the y-axis for the model
         */
        public VariantBuilder model(ResourceLocation modelLocation, int xRotation, int yRotation){
            return this.model(modelLocation, xRotation, yRotation, false, 1);
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param modelLocation location of the model
         */
        public VariantBuilder model(ResourceLocation modelLocation){
            return this.model(modelLocation, 0, 0, false, 1);
        }
    }

    protected static class VariantModel {

        public final ResourceLocation modelLocation;
        public final int xRotation;
        public final int yRotation;
        public final boolean uvLock;
        public final int weight;

        public VariantModel(ResourceLocation modelLocation, int xRotation, int yRotation, boolean uvLock, int weight){
            this.modelLocation = modelLocation;
            this.xRotation = xRotation;
            this.yRotation = yRotation;
            this.uvLock = uvLock;
            this.weight = weight;
        }
    }

    protected static class PartialBlockState {

        private final Block block;
        private final Map<Property<?>,Comparable<?>> properties;

        protected PartialBlockState(Block block, Map<Property<?>,Comparable<?>> properties){
            this.block = block;
            this.properties = ImmutableMap.copyOf(properties);
        }

        /**
         * Gets the block this partial state is for.
         * @return parent block of this partial state
         */
        public Block getBlock(){
            return this.block;
        }

        /**
         * Checks whether the given property is set on this partial state.
         * @param property property to be checked
         */
        public boolean has(Property<?> property){
            return this.properties.containsKey(property);
        }

        /**
         * Gets the value of the given property in this partial state.
         * @param property property to get
         * @return this state's value for the given property
         * @throws IllegalArgumentException when the given property is not part of this state's parent block
         */
        public <T extends Comparable<T>> T get(Property<T> property){
            if(!this.block.getStateDefinition().getProperties().contains(property))
                throw new IllegalArgumentException("Property '" + property + "' is not a property of block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");

            //noinspection unchecked
            return (T)this.properties.get(property);
        }
    }

    protected static class PartialBlockStateBuilder {

        private final Block block;
        private final Map<Property<?>,Comparable<?>> properties = new HashMap<>();

        protected PartialBlockStateBuilder(Block block){
            this.block = block;
        }

        /**
         * Sets the value for the given property on this partial state.
         * @param property property to be set
         * @param value    value of the property
         * @throws IllegalArgumentException when the given property is not part of this state's parent block
         * @throws IllegalArgumentException when the given value is not a valid value for the given property
         */
        public <T extends Comparable<T>> PartialBlockStateBuilder set(Property<T> property, T value){
            if(!this.block.getStateDefinition().getProperties().contains(property))
                throw new IllegalArgumentException("Property '" + property + "' is not a property of block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");
            if(!property.getPossibleValues().contains(value))
                throw new IllegalArgumentException("Value '" + value + "' does not belong to property '" + property + "'!");

            this.properties.put(property, value);
            return this;
        }

        /**
         * Copies all properties from the given state to this partial state.
         * @param state state to copy properties from
         */
        public PartialBlockStateBuilder copy(BlockState state){
            if(this.block != state.getBlock())
                throw new IllegalArgumentException("Cannot copy properties of state for block '" + Registries.BLOCKS.getIdentifier(state.getBlock()) + "' to block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");

            this.properties.putAll(state.getValues());
            return this;
        }

        /**
         * Checks whether the given property is set on this partial state.
         * @param property property to be checked
         */
        public boolean has(Property<?> property){
            return this.properties.containsKey(property);
        }

        /**
         * Gets the value of the given property in this partial state.
         * @param property property to get
         * @return this state's value for the given property
         * @throws IllegalArgumentException when the given property is not part of this state's parent block
         */
        public <T extends Comparable<T>> T get(Property<T> property){
            if(!this.block.getStateDefinition().getProperties().contains(property))
                throw new IllegalArgumentException("Property '" + property + "' is not a property of block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");

            //noinspection unchecked
            return (T)this.properties.get(property);
        }

        /**
         * Builds this partial state builder.
         * @return a partial state with this builder's properties
         */
        public PartialBlockState build(){
            return new PartialBlockState(this.block, this.properties);
        }
    }
}
