package com.supermartijn642.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Created 24/07/2022 by SuperMartijn642
 */
public class BlockProperties {

    public static BlockProperties create(Material material, MapColor color){
        return new BlockProperties(material, color);
    }

    public static BlockProperties create(Material material, EnumDyeColor color){
        return new BlockProperties(material, MapColor.getBlockColor(color));
    }

    public static BlockProperties create(Material material){
        return new BlockProperties(material, material.getMaterialMapColor());
    }

    public static BlockProperties copy(Block block){
        BlockProperties properties = create(block.blockMaterial, block.blockMapColor);
        properties.hasCollision = block.isCollidable();
        properties.canOcclude = block.isOpaqueCube(block.getDefaultState());
        properties.soundType = block.getSoundType();
        properties.lightLevel = block::getLightValue;
        properties.explosionResistance = block.blockResistance;
        properties.destroyTime = block.blockHardness;
        properties.requiresCorrectTool = !block.blockMaterial.isToolNotRequired();
        properties.ticksRandomly = block.getTickRandomly();
        properties.friction = block.slipperiness;
        properties.speedFactor = 1;
        properties.jumpFactor = 1;
        properties.isAir = block.blockMaterial == Material.AIR;
        properties.isSuffocating = block::causesSuffocation;
        properties.copyLootTableBlock = block;
        return properties;
    }

    final Material material;
    final MapColor mapColor;
    boolean hasCollision = true;
    boolean canOcclude = true;
    SoundType soundType = SoundType.STONE;
    ToIntFunction<IBlockState> lightLevel = state -> 0;
    float explosionResistance;
    float destroyTime;
    boolean requiresCorrectTool = false;
    boolean ticksRandomly = false;
    float friction = 0.6f;
    float speedFactor = 1.0f;
    float jumpFactor = 1.0f;
    boolean isAir;
    Predicate<IBlockState> isSuffocating = (state) -> state.getMaterial().blocksMovement() && state.isFullCube();
    boolean noLootTable = false;
    Supplier<Block> lootTableBlock;
    Block copyLootTableBlock;
    Supplier<ResourceLocation> lootTableSupplier;

    private BlockProperties(Material material, MapColor color){
        this.material = material;
        this.mapColor = color;
        this.isAir = material == Material.AIR;
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

    public BlockProperties lightLevel(ToIntFunction<IBlockState> stateLightFunction){
        this.lightLevel = stateLightFunction;
        return this;
    }

    public BlockProperties lightLevel(int light){
        this.lightLevel = state -> light;
        return this;
    }

    public BlockProperties explosionResistance(float resistance){
        this.explosionResistance = Math.max(0, resistance * 5 / 3);
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

    public BlockProperties isSuffocating(Predicate<IBlockState> isSuffocating){
        this.isSuffocating = isSuffocating;
        return this;
    }

    public BlockProperties isSuffocating(boolean isSuffocating){
        this.isSuffocating = state -> isSuffocating;
        return this;
    }

    public BlockProperties noLootTable(){
        this.noLootTable = true;
        this.lootTableBlock = null;
        this.copyLootTableBlock = null;
        this.lootTableSupplier = null;
        return this;
    }

    public BlockProperties lootTable(ResourceLocation lootTable){
        this.noLootTable = false;
        this.lootTableBlock = null;
        this.copyLootTableBlock = null;
        this.lootTableSupplier = () -> lootTable;
        return this;
    }

    public BlockProperties lootTableFrom(Supplier<Block> block){
        this.noLootTable = false;
        this.lootTableBlock = block;
        this.copyLootTableBlock = null;
        this.lootTableSupplier = null;
        return this;
    }
}
