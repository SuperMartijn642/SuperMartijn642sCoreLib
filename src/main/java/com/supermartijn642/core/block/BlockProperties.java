package com.supermartijn642.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.util.TriPredicate;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BlockProperties {

    public static BlockProperties create(Material material, MaterialColor color){
        return new BlockProperties(material, color);
    }

    public static BlockProperties create(Material material, DyeColor color){
        return new BlockProperties(material, color.getMaterialColor());
    }

    public static BlockProperties create(Material material){
        return new BlockProperties(material, material.getColor());
    }

    public static BlockProperties copy(Block block){
        BlockBehaviour.Properties sourceProperties = block.properties;
        BlockProperties properties = create(sourceProperties.material, block.defaultMaterialColor());
        properties.hasCollision = sourceProperties.hasCollision;
        properties.canOcclude = block.defaultBlockState().canOcclude();
        properties.soundType = block.getSoundType(block.defaultBlockState());
        properties.lightLevel = sourceProperties.lightEmission;
        properties.explosionResistance = block.getExplosionResistance();
        properties.destroyTime = block.defaultDestroyTime();
        properties.requiresCorrectTool = block.defaultBlockState().requiresCorrectToolForDrops();
        properties.ticksRandomly = block.isRandomlyTicking(block.defaultBlockState());
        properties.friction = block.getFriction();
        properties.speedFactor = block.getSpeedFactor();
        properties.jumpFactor = block.getJumpFactor();
        properties.isAir = block.defaultBlockState().isAir();
        properties.isRedstoneConductor = sourceProperties.isRedstoneConductor::test;
        properties.isSuffocating = sourceProperties.isSuffocating::test;
        properties.hasDynamicShape = block.hasDynamicShape();
        properties.lootTableBlock = () -> block;
        return properties;
    }

    private final Material material;
    private final MaterialColor mapColor;
    private boolean hasCollision = true;
    private boolean canOcclude = true;
    private SoundType soundType = SoundType.STONE;
    private ToIntFunction<BlockState> lightLevel = state -> 0;
    private float explosionResistance;
    private float destroyTime;
    private boolean requiresCorrectTool = false;
    private boolean ticksRandomly = false;
    private float friction = 0.6f;
    private float speedFactor = 1.0f;
    private float jumpFactor = 1.0f;
    private boolean isAir = false;
    private TriPredicate<BlockState,BlockGetter,BlockPos> isRedstoneConductor = (state, level, pos) -> state.getMaterial().isSolidBlocking() && state.isCollisionShapeFullBlock(level, pos);
    private TriPredicate<BlockState,BlockGetter,BlockPos> isSuffocating = (state, level, pos) -> state.getMaterial().blocksMotion() && state.isCollisionShapeFullBlock(level, pos);
    private boolean hasDynamicShape = false;
    private boolean noLootTable = false;
    private Supplier<Block> lootTableBlock;

    private BlockProperties(Material material, MaterialColor color){
        this.material = material;
        this.mapColor = color;
    }

    public BlockProperties noCollision(){
        this.hasCollision = false;
        this.canOcclude = false;
        return this;
    }

    public BlockProperties noOcclusion(){
        this.canOcclude = false;
        return this;
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

    public BlockProperties requiresCorrectTool(){
        this.requiresCorrectTool = true;
        return this;
    }

    public BlockProperties randomTicks(){
        this.ticksRandomly = true;
        return this;
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

    public BlockProperties air(){
        this.isAir = true;
        return this;
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
        this.lootTableBlock = null;
        return this;
    }

    public BlockProperties lootTableFrom(Supplier<Block> block){
        this.noLootTable = false;
        this.lootTableBlock = block;
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
        if(this.lootTableBlock != null)
            properties.lootFrom(this.lootTableBlock);
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
