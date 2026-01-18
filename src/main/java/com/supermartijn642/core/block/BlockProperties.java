package com.supermartijn642.core.block;

import com.supermartijn642.core.mixin.BlockPropertiesAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.util.TriPredicate;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BlockProperties {

    public static BlockProperties create(Material material, MaterialColor color){
        return new BlockProperties(material).mapColor(color);
    }

    public static BlockProperties create(Material material, DyeColor color){
        return new BlockProperties(material).mapColor(color.getMaterialColor());
    }

    public static BlockProperties create(Material material){
        return new BlockProperties(material);
    }

    public static BlockProperties fromVanilla(BlockBehaviour.Properties vanilla){
        BlockProperties properties = create(vanilla.material);
        properties.mapColor = vanilla.materialColor;
        properties.hasCollision = vanilla.hasCollision;
        properties.soundType = vanilla.soundType;
        properties.lightLevel = vanilla.lightEmission;
        properties.explosionResistance = vanilla.explosionResistance;
        properties.destroyTime = vanilla.destroyTime;
        properties.requiresCorrectTool = vanilla.requiresCorrectToolForDrops;
        properties.ticksRandomly = vanilla.isRandomlyTicking;
        properties.friction = vanilla.friction;
        properties.speedFactor = vanilla.speedFactor;
        properties.jumpFactor = vanilla.jumpFactor;
        properties.canOcclude = vanilla.canOcclude;
        properties.isAir = vanilla.isAir;
        properties.isRedstoneConductor = vanilla.isRedstoneConductor::test;
        properties.isSuffocating = vanilla.isSuffocating::test;
        properties.hasDynamicShape = vanilla.dynamicShape;
        properties.lootTableSupplier = vanilla.drops != null ? () -> vanilla.drops : ((BlockPropertiesAccessor)vanilla).getLootTableSupplier();
        return properties;
    }

    public static BlockProperties copy(Block block){
        return fromVanilla(block.properties);
    }

    private final Material material;
    private Function<BlockState,MaterialColor> mapColor;
    private boolean hasCollision = true;
    private SoundType soundType = SoundType.STONE;
    private ToIntFunction<BlockState> lightLevel = state -> 0;
    private float explosionResistance;
    private float destroyTime;
    private boolean requiresCorrectTool = false;
    private boolean ticksRandomly = false;
    private float friction = 0.6f;
    private float speedFactor = 1.0f;
    private float jumpFactor = 1.0f;
    private boolean canOcclude = true;
    private boolean isAir = false;
    private TriPredicate<BlockState,BlockGetter,BlockPos> isRedstoneConductor = (state, level, pos) -> state.getMaterial().isSolidBlocking() && state.isCollisionShapeFullBlock(level, pos);
    private TriPredicate<BlockState,BlockGetter,BlockPos> isSuffocating = (state, level, pos) -> state.getMaterial().blocksMotion() && state.isCollisionShapeFullBlock(level, pos);
    private boolean hasDynamicShape = false;
    private boolean noLootTable = false;
    private Supplier<ResourceLocation> lootTableSupplier;

    private BlockProperties(Material material){
        this.material = material;
        this.mapColor = state -> material.getColor();
    }

    public BlockProperties mapColor(Function<BlockState,MaterialColor> colorFunction){
        if(colorFunction == null){
            Material material = this.material;
            colorFunction = state -> material.getColor();
        }
        this.mapColor = colorFunction;
        return this;
    }

    public BlockProperties mapColor(MaterialColor color){
        return this.mapColor(color == null ? null : state -> color);
    }

    public BlockProperties collision(boolean hasCollision){
        this.hasCollision = hasCollision;
        if(!hasCollision)
            this.canOcclude = false;
        return this;
    }

    public BlockProperties noCollision(){
        return this.collision(false);
    }

    public BlockProperties sound(SoundType soundTypeIn){
        this.soundType = soundTypeIn;
        return this;
    }

    public BlockProperties lightLevel(ToIntFunction<BlockState> stateLightFunction){
        this.lightLevel = stateLightFunction;
        return this;
    }

    public BlockProperties lightLevel(int light){
        this.lightLevel = state -> light;
        return this;
    }

    public BlockProperties explosionResistance(float resistance){
        this.explosionResistance = Math.max(0, resistance);
        return this;
    }

    public BlockProperties destroyTime(float destroyTime){
        this.destroyTime = destroyTime;
        return this;
    }

    /**
     * Sets both explosion resistance and destroy time.
     */
    public BlockProperties strength(float strength){
        return this.explosionResistance(strength).destroyTime(strength);
    }

    public BlockProperties requiresCorrectTool(boolean requiresCorrectTool){
        this.requiresCorrectTool = requiresCorrectTool;
        return this;
    }

    public BlockProperties requiresCorrectTool(){
        return this.requiresCorrectTool(true);
    }

    public BlockProperties randomTicks(boolean receiveRandomTicks){
        this.ticksRandomly = receiveRandomTicks;
        return this;
    }

    public BlockProperties randomTicks(){
        return this.randomTicks(true);
    }

    public BlockProperties friction(float friction){
        this.friction = friction;
        return this;
    }

    public BlockProperties speedFactor(float factor){
        this.speedFactor = factor;
        return this;
    }

    public BlockProperties jumpFactor(float factor){
        this.jumpFactor = factor;
        return this;
    }

    public BlockProperties canOcclude(boolean canOcclude){
        this.canOcclude = canOcclude;
        return this;
    }

    public BlockProperties noOcclusion(){
        return this.canOcclude(false);
    }

    public BlockProperties air(boolean isAir){
        this.isAir = isAir;
        return this;
    }

    public BlockProperties air(){
        return this.air(true);
    }

    public BlockProperties isRedstoneConductor(TriPredicate<BlockState,BlockGetter,BlockPos> isRedstoneConductor){
        this.isRedstoneConductor = isRedstoneConductor;
        return this;
    }

    public BlockProperties isRedstoneConductor(boolean isRedstoneConductor){
        this.isRedstoneConductor = (state, blockGetter, pos) -> isRedstoneConductor;
        return this;
    }

    public BlockProperties isSuffocating(TriPredicate<BlockState,BlockGetter,BlockPos> isSuffocating){
        this.isSuffocating = isSuffocating;
        return this;
    }

    public BlockProperties isSuffocating(boolean isSuffocating){
        this.isSuffocating = (state, blockGetter, pos) -> isSuffocating;
        return this;
    }

    public BlockProperties dynamicShape(){
        this.hasDynamicShape = true;
        return this;
    }

    public BlockProperties noLootTable(){
        this.noLootTable = true;
        this.lootTableSupplier = null;
        return this;
    }

    public BlockProperties lootTable(ResourceLocation lootTable){
        this.noLootTable = false;
        this.lootTableSupplier = () -> lootTable;
        return this;
    }

    public BlockProperties lootTableFrom(Supplier<Block> block){
        this.noLootTable = false;
        this.lootTableSupplier = block == null ? null : () -> block.get().getLootTable();
        return this;
    }

    /**
     * Converts the properties into {@link BlockBehaviour.Properties}.
     */
    @Deprecated
    public BlockBehaviour.Properties toUnderlying(){
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of(this.material, this.mapColor);
        if(!this.hasCollision)
            properties.noCollission();
        properties.sound(this.soundType);
        properties.lightLevel(this.lightLevel);
        properties.strength(this.explosionResistance);
        properties.destroyTime(this.destroyTime);
        if(this.requiresCorrectTool)
            properties.requiresCorrectToolForDrops();
        if(this.ticksRandomly)
            properties.randomTicks();
        properties.friction(this.friction);
        properties.speedFactor(this.speedFactor);
        properties.jumpFactor(this.jumpFactor);
        if(this.noLootTable)
            properties.noDrops();
        ((BlockPropertiesAccessor)properties).setLootTableSupplier(this.lootTableSupplier);
        if(!this.canOcclude)
            properties.noOcclusion();
        if(this.isAir)
            properties.air();
        properties.isRedstoneConductor(this.isRedstoneConductor::test);
        properties.isSuffocating(this.isSuffocating::test);
        properties.isViewBlocking(this.isSuffocating::test);
        if(this.hasDynamicShape)
            properties.dynamicShape();
        return properties;
    }
}
