package com.supermartijn642.core.generator;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;

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

            // Serialize all variants
            JsonObject variantsJson = new JsonObject();
            for(Map.Entry<PartialBlockState,VariantBuilder> variantEntry : blockStateBuilder.variants.entrySet()){
                if(variantEntry.getValue().models.isEmpty())
                    continue;
                String name = formatVariantName(variantEntry.getKey());
                variantsJson.add(name, this.serializeVariant(variantEntry.getValue(), block));
            }
            if(variantsJson.size() > 0)
                json.add("variants", variantsJson);

            // Serialize all multiparts
            JsonArray multipartsJson = new JsonArray();
            for(Pair<MultipartConditionBuilder,VariantBuilder> multipartEntry : blockStateBuilder.multipartVariants){
                if(multipartEntry.right().models.isEmpty())
                    continue;
                JsonObject multipartJson = new JsonObject();
                multipartJson.add("apply", this.serializeVariant(multipartEntry.right(), block));
                multipartEntry.left().flatten();
                JsonArray whenJson = new JsonArray();
                for(MultipartConditionBuilder condition : multipartEntry.left().or){
                    if(condition.properties.isEmpty())
                        continue;
                    JsonObject conditionJson = new JsonObject();
                    //noinspection rawtypes,unchecked
                    condition.properties.forEach(
                        (property, values) ->
                            conditionJson.addProperty(property.getName(), Arrays.stream(values).map(((IProperty)property)::getName).collect(Collectors.joining("|")))
                    );
                    whenJson.add(conditionJson);
                }
                if(whenJson.size() == 1)
                    multipartJson.add("when", whenJson.get(0));
                else if(whenJson.size() != 0){
                    JsonObject newWhenJson = new JsonObject();
                    newWhenJson.add("OR", whenJson);
                    multipartJson.add("when", newWhenJson);
                }
                multipartsJson.add(multipartJson);
            }
            if(multipartsJson.size() != 0)
                json.add("multipart", multipartsJson);

            // Check if there's at one entry in the block state
            if(variantsJson.size() == 0 && multipartsJson.size() == 0)
                throw new RuntimeException("Block state for block '" + block + "' is empty!");

            // Save the object to the cache
            this.cache.saveJsonResource(ResourceType.ASSET, json, block.getNamespace(), "blockstates", block.getPath());
        }
    }

    private JsonElement serializeVariant(VariantBuilder builder, ResourceLocation block){
        JsonObject[] models = new JsonObject[builder.models.size()];
        for(int i = 0; i < models.length; i++){
            VariantModel model = builder.models.get(i);
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
        return models.length > 1 ? createJsonArray(models) : models[0];
    }

    private static JsonArray createJsonArray(JsonElement... elements){
        // Because they can't just make a proper json array constructor...
        JsonArray array = new JsonArray();
        for(JsonElement element : elements)
            array.add(element);
        return array;
    }

    private static String formatVariantName(PartialBlockState state){
        //noinspection unchecked,rawtypes
        return state.properties.entrySet().stream().map(entry -> entry.getKey().getName() + "=" + ((IProperty)entry.getKey()).getName(entry.getValue())).collect(Collectors.joining(","));
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
        this.cache.trackToBeGeneratedResource(ResourceType.ASSET, identifier.getNamespace(), "blockstates", identifier.getPath(), ".json");
        return this.blockStates.computeIfAbsent(block, o -> new BlockStateBuilder(this.modid, o));
    }

    @Override
    public String getName(){
        return this.modName + " Block State Generator";
    }

    protected class BlockStateBuilder {

        private final String modid;
        private final Block block;
        private final Map<PartialBlockState,VariantBuilder> variants = new LinkedHashMap<>();
        private final List<Pair<MultipartConditionBuilder,VariantBuilder>> multipartVariants = new ArrayList<>();

        public BlockStateBuilder(String modid, Block block){
            this.modid = modid;
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

            variantBuilderConsumer.accept(this.variants.computeIfAbsent(state, o -> new VariantBuilder(this.modid)));
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
        public BlockStateBuilder variantsForProperty(IProperty<?> property, BiConsumer<PartialBlockState,VariantBuilder> variantBuilderConsumer){
            if(!this.block.getStateDefinition().getProperties().contains(property))
                throw new IllegalArgumentException("Property '" + property + "' is not a property of block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");

            PartialBlockStateBuilder builder = BlockStateGenerator.this.createPartialStateBuilder(this.block);
            for(Comparable<?> value : property.getPossibleValues()){
                //noinspection rawtypes,unchecked
                PartialBlockState state = builder.set((IProperty)property, (Comparable)value).build();
                variantBuilderConsumer.accept(state, this.variants.computeIfAbsent(state, o -> new VariantBuilder(this.modid)));
            }
            return this;
        }

        /**
         * Constructs model options for all possible variants except the given property.
         * @param variantBuilderConsumer consumer to build the model options
         * @param excluded               properties which should not be considered
         */
        public BlockStateBuilder variantsForAllExcept(BiConsumer<PartialBlockState,VariantBuilder> variantBuilderConsumer, IProperty<?>... excluded){
            PartialBlockStateBuilder builder = BlockStateGenerator.this.createPartialStateBuilder(this.block);
            List<IProperty<?>> properties = this.block.getStateDefinition().getProperties().stream().filter(property -> Arrays.stream(excluded).noneMatch(p -> p == property)).collect(Collectors.toList());
            this.loopThroughAll(builder, properties, 0, variantBuilderConsumer);
            return this;
        }

        private void loopThroughAll(PartialBlockStateBuilder builder, List<IProperty<?>> properties, int index, BiConsumer<PartialBlockState,VariantBuilder> variantBuilderConsumer){
            if(index == properties.size()){
                PartialBlockState state = builder.build();
                variantBuilderConsumer.accept(state, this.variants.computeIfAbsent(state, o -> new VariantBuilder(this.modid)));
                return;
            }

            IProperty<?> property = properties.get(index);
            for(Comparable<?> value : property.getPossibleValues()){
                //noinspection rawtypes,unchecked
                builder.set((IProperty)property, (Comparable)value);
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

        /**
         * Constructs a condition and model options which will be added whenever a state matches all properties in constructed condition.
         * @param conditionBuilderConsumer consumer to build the condition for the multipart
         * @param variantBuilderConsumer   consumer to build the model options
         */
        public BlockStateBuilder multipart(Consumer<MultipartConditionBuilder> conditionBuilderConsumer, Consumer<VariantBuilder> variantBuilderConsumer){
            MultipartConditionBuilder condition = new MultipartConditionBuilder(this.block);
            conditionBuilderConsumer.accept(condition);
            VariantBuilder variant = new VariantBuilder(this.modid);
            variantBuilderConsumer.accept(variant);
            this.multipartVariants.add(Pair.of(condition, variant));
            return this;
        }

        /**
         * Constructs model options which will be added whenever a state matches all properties in the given state.
         * @param state                  the properties to match
         * @param variantBuilderConsumer consumer to build the model options
         */
        public BlockStateBuilder multipart(PartialBlockState state, Consumer<VariantBuilder> variantBuilderConsumer){
            if(state.block != this.block)
                throw new IllegalArgumentException("Cannot use state from block '" + state.block + "' in block state builder for block '" + this.block + "'!");

            //noinspection unchecked,rawtypes
            return this.multipart(condition -> state.properties.forEach((property, value) -> condition.requireProperty((IProperty)property, (Comparable)value)), variantBuilderConsumer);
        }

        /**
         * Constructs model options which will be added whenever a state matches all properties in the given state.
         * @param state                  the properties to match
         * @param variantBuilderConsumer consumer to build the model options
         */
        public BlockStateBuilder multipart(BlockState state, Consumer<VariantBuilder> variantBuilderConsumer){
            return this.multipart(BlockStateGenerator.this.createPartialState(state), variantBuilderConsumer);
        }

        /**
         * Constructs model options which will be added to the regular variants.
         * @param variantBuilderConsumer consumer to build the model options
         */
        public BlockStateBuilder unconditionalMultipart(Consumer<VariantBuilder> variantBuilderConsumer){
            return this.multipart(BlockStateGenerator.this.createEmptyPartialState(this.block), variantBuilderConsumer);
        }
    }

    protected static class VariantBuilder {

        private final String modid;
        private final List<VariantModel> models = new ArrayList<>();

        protected VariantBuilder(String modid){
            this.modid = modid;
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
         * @param namespace  namespace of the model
         * @param identifier path of the model
         * @param xRotation  rotation around the x-axis for the model
         * @param yRotation  rotation around the y-axis for the model
         * @param uvLock     whether to apply uv lock to the model
         * @param weight     weight of the model when considering which model to pick
         */
        public VariantBuilder model(String namespace, String identifier, int xRotation, int yRotation, boolean uvLock, int weight){
            return this.model(new ResourceLocation(namespace, identifier), xRotation, yRotation, uvLock, weight);
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param identifier path of the model
         * @param xRotation  rotation around the x-axis for the model
         * @param yRotation  rotation around the y-axis for the model
         * @param uvLock     whether to apply uv lock to the model
         * @param weight     weight of the model when considering which model to pick
         */
        public VariantBuilder model(String identifier, int xRotation, int yRotation, boolean uvLock, int weight){
            return this.model(this.modid, identifier, xRotation, yRotation, uvLock, weight);
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
         * @param namespace  namespace of the model
         * @param identifier path of the model
         * @param xRotation  rotation around the x-axis for the model
         * @param yRotation  rotation around the y-axis for the model
         * @param uvLock     whether to apply uv lock to the model
         */
        public VariantBuilder model(String namespace, String identifier, int xRotation, int yRotation, boolean uvLock){
            return this.model(new ResourceLocation(namespace, identifier), xRotation, yRotation, uvLock);
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param identifier path of the model
         * @param xRotation  rotation around the x-axis for the model
         * @param yRotation  rotation around the y-axis for the model
         * @param uvLock     whether to apply uv lock to the model
         */
        public VariantBuilder model(String identifier, int xRotation, int yRotation, boolean uvLock){
            return this.model(this.modid, identifier, xRotation, yRotation, uvLock);
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
         * @param namespace  namespace of the model
         * @param identifier path of the model
         * @param xRotation  rotation around the x-axis for the model
         * @param yRotation  rotation around the y-axis for the model
         */
        public VariantBuilder model(String namespace, String identifier, int xRotation, int yRotation){
            return this.model(new ResourceLocation(namespace, identifier), xRotation, yRotation);
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param identifier path of the model
         * @param xRotation  rotation around the x-axis for the model
         * @param yRotation  rotation around the y-axis for the model
         */
        public VariantBuilder model(String identifier, int xRotation, int yRotation){
            return this.model(this.modid, identifier, xRotation, yRotation);
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param modelLocation location of the model
         */
        public VariantBuilder model(ResourceLocation modelLocation){
            return this.model(modelLocation, 0, 0, false, 1);
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param namespace  namespace of the model
         * @param identifier path of the model
         */
        public VariantBuilder model(String namespace, String identifier){
            return this.model(new ResourceLocation(namespace, identifier));
        }

        /**
         * Adds a model to the list of options for this variant.
         * @param identifier path of the model
         */
        public VariantBuilder model(String identifier){
            return this.model(this.modid, identifier);
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

    protected static class MultipartConditionBuilder {

        private final Block block;
        private final Map<IProperty<?>,Comparable<?>[]> properties = new HashMap<>();
        private final List<MultipartConditionBuilder> or = new ArrayList<>();

        private MultipartConditionBuilder(Block block){
            this.block = block;
        }

        /**
         * Adds required values for a property for this condition to be met.
         * @param property       property which should have any of the given values
         * @param acceptedValues values which the property may have for the condition to be met
         */
        public <T extends Comparable<T>> MultipartConditionBuilder requireProperty(IProperty<T> property, T... acceptedValues){
            if(acceptedValues.length == 0)
                throw new RuntimeException("Accepted values cannot be empty for multipart condition property!");
            if(this.properties.containsKey(property))
                throw new RuntimeException("Duplicate requirements for property '" + property + "' for multipart condition for block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");
            if(!this.block.getStateDefinition().getProperties().contains(property))
                throw new IllegalArgumentException("Property '" + property + "' is not a property of block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");
            for(T value : acceptedValues){
                if(!property.getPossibleValues().contains(value))
                    throw new IllegalArgumentException("Value '" + value + "' does not belong to property '" + property + "'!");
            }

            this.properties.put(property, acceptedValues);
            return this;
        }

        /**
         * Adds an alternative condition to this one.
         * @param alternativeBuilderConsumer consumer to construct the alternative condition
         */
        public MultipartConditionBuilder or(Consumer<MultipartConditionBuilder> alternativeBuilderConsumer){
            MultipartConditionBuilder builder = new MultipartConditionBuilder(this.block);
            alternativeBuilderConsumer.accept(builder);
            if(builder.properties.isEmpty())
                throw new IllegalArgumentException("Alternative condition cannot be empty!");
            this.or.add(builder);
            return this;
        }

        /**
         * Constructs an alternative condition to this one.
         * @return builder for the alternative condition
         */
        public MultipartConditionBuilder or(){
            MultipartConditionBuilder builder = new MultipartConditionBuilder(this.block);
            this.or.add(builder);
            return builder;
        }

        private void flatten(){
            this.or.add(0, this);
            for(int i = 1; i < this.or.size(); i++)
                this.or.addAll(this.or.get(i).or);
        }
    }

    protected static class PartialBlockState {

        private final Block block;
        private final Map<IProperty<?>,Comparable<?>> properties;

        protected PartialBlockState(Block block, Map<IProperty<?>,Comparable<?>> properties){
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
        public boolean has(IProperty<?> property){
            return this.properties.containsKey(property);
        }

        /**
         * Gets the value of the given property in this partial state.
         * @param property property to get
         * @return this state's value for the given property
         * @throws IllegalArgumentException when the given property is not part of this state's parent block
         */
        public <T extends Comparable<T>> T get(IProperty<T> property){
            if(!this.block.getStateDefinition().getProperties().contains(property))
                throw new IllegalArgumentException("Property '" + property + "' is not a property of block '" + Registries.BLOCKS.getIdentifier(this.block) + "'!");

            //noinspection unchecked
            return (T)this.properties.get(property);
        }
    }

    protected static class PartialBlockStateBuilder {

        private final Block block;
        private final Map<IProperty<?>,Comparable<?>> properties = new HashMap<>();

        protected PartialBlockStateBuilder(Block block){
            this.block = block;
        }

        public Block getBlock(){
            return this.block;
        }

        /**
         * Sets the value for the given property on this partial state.
         * @param property property to be set
         * @param value    value of the property
         * @throws IllegalArgumentException when the given property is not part of this state's parent block
         * @throws IllegalArgumentException when the given value is not a valid value for the given property
         */
        public <T extends Comparable<T>> PartialBlockStateBuilder set(IProperty<T> property, T value){
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
        public boolean has(IProperty<?> property){
            return this.properties.containsKey(property);
        }

        /**
         * Gets the value of the given property in this partial state.
         * @param property property to get
         * @return this state's value for the given property
         * @throws IllegalArgumentException when the given property is not part of this state's parent block
         */
        public <T extends Comparable<T>> T get(IProperty<T> property){
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
