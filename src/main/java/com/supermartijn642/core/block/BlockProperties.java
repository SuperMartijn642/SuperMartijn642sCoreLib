package com.supermartijn642.core.block;

import com.supermartijn642.core.mixin.BlockPropertiesAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.storage.loot.LootTables;
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
        BlockProperties properties = create(block.material, block.materialColor);
        properties.hasCollision = block.hasCollision;
        properties.canOcclude = block.canOcclude;
        properties.soundType = block.soundType;
        properties.lightLevel = block::getLightEmission;
        properties.explosionResistance = block.getExplosionResistance();
        properties.destroyTime = block.destroySpeed;
        properties.requiresCorrectTool = !block.material.isAlwaysDestroyable();
        properties.ticksRandomly = block.isRandomlyTicking(block.defaultBlockState());
        properties.friction = block.getFriction();
        properties.speedFactor = block.getSpeedFactor();
        properties.jumpFactor = block.getJumpFactor();
        properties.isAir = block.defaultBlockState().isAir();
        properties.isRedstoneConductor = block::isRedstoneConductor;
        properties.isSuffocating = block::isSuffocating;
        properties.hasDynamicShape = block.hasDynamicShape();
        ResourceLocation lootTable = block.getLootTable();
        if(LootTables.EMPTY.equals(lootTable))
            properties.noLootTable = true;
        else if(lootTable != null){
            ResourceLocation registryName = block.getRegistryName();
            if(registryName != null && !lootTable.getNamespace().equals(block.getRegistryName().getNamespace()) && !lootTable.getPath().equals("block/" + block.getRegistryName().getPath())){
                properties.lootTableSupplier = () -> lootTable;
            }
        }
        return properties;
    }

    private final Material material;
    private final MaterialColor mapColor;
    private boolean hasCollision = true;
    private boolean canOcclude = true;
    private SoundType soundType = SoundType.STONE;
    ToIntFunction<BlockState> lightLevel = state -> 0;
    private float explosionResistance;
    private float destroyTime;
    boolean requiresCorrectTool = false;
    private boolean ticksRandomly = false;
    private float friction = 0.6f;
    private float speedFactor = 1.0f;
    private float jumpFactor = 1.0f;
    boolean isAir = false;
    TriPredicate<BlockState,IBlockReader,BlockPos> isRedstoneConductor = (state, level, pos) -> state.getMaterial().isSolidBlocking() && state.isCollisionShapeFullBlock(level, pos);
    TriPredicate<BlockState,IBlockReader,BlockPos> isSuffocating = (state, level, pos) -> state.getMaterial().blocksMotion() && state.isCollisionShapeFullBlock(level, pos);
    private boolean hasDynamicShape = false;
    private boolean noLootTable = false;
    Supplier<ResourceLocation> lootTableSupplier;

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

    public BlockProperties isRedstoneConductor(TriPredicate<BlockState,IBlockReader,BlockPos> isRedstoneConductor){
        this.isRedstoneConductor = isRedstoneConductor;
        return this;
    }

    public BlockProperties isRedstoneConductor(boolean isRedstoneConductor){
        this.isRedstoneConductor = (state, blockGetter, pos) -> isRedstoneConductor;
        return this;
    }

    public BlockProperties isSuffocating(TriPredicate<BlockState,IBlockReader,BlockPos> isSuffocating){
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
     * Converts the properties into {@link Block.Properties}.
     */
    @Deprecated
    public Block.Properties toUnderlying(){
        Block.Properties properties = Block.Properties.of(this.material, this.mapColor);
        if(!this.hasCollision)
            properties.noCollission();
        properties.sound(this.soundType);
        properties.strength(this.explosionResistance);
        properties.destroyTime = this.destroyTime;
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
        if(this.hasDynamicShape)
            properties.dynamicShape();
        return properties;
    }
}
