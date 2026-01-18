package com.supermartijn642.core.block;

import com.supermartijn642.core.extensions.BlockExtension;
import com.supermartijn642.core.mixin.BlockPropertiesAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraft.util.BlockRenderLayer;
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
        return new BlockProperties(material).mapColor(color);
    }

    public static BlockProperties create(Material material, DyeColor color){
        return new BlockProperties(material).mapColor(color.getMaterialColor());
    }

    public static BlockProperties create(Material material){
        return new BlockProperties(material);
    }

    public static BlockProperties fromVanilla(Block.Properties vanilla){
        BlockProperties properties = create(vanilla.material);
        properties.mapColor = vanilla.materialColor;
        properties.hasCollision = vanilla.hasCollision;
        properties.soundType = vanilla.soundType;
        int lightEmission = vanilla.lightEmission;
        properties.lightLevel = state -> lightEmission;
        properties.explosionResistance = vanilla.explosionResistance;
        properties.destroyTime = vanilla.destroyTime;
        properties.requiresCorrectTool = !vanilla.material.isAlwaysDestroyable();
        properties.ticksRandomly = vanilla.isTicking;
        properties.friction = vanilla.friction;
        properties.canOcclude = vanilla.hasCollision;
        properties.hasDynamicShape = vanilla.dynamicShape;
        if(LootTables.EMPTY.equals(vanilla.drops))
            properties.noLootTable = true;
        else
            properties.lootTable(vanilla.drops);
        return properties;
    }

    public static BlockProperties copy(Block block){
        BlockProperties properties = fromVanilla(((BlockExtension)block).supermartijn642corelibGetProperties());
        //noinspection deprecation
        properties.lightLevel = block::getLightEmission;
        properties.canOcclude = block.hasCollision && block.getRenderLayer() == BlockRenderLayer.SOLID;
        //noinspection deprecation
        properties.isAir = block.defaultBlockState().isAir();
        //noinspection deprecation
        properties.isRedstoneConductor = block::isRedstoneConductor;
        //noinspection deprecation
        properties.isSuffocating = block::isViewBlocking;
        if(properties.lootTableSupplier != null){
            ResourceLocation lootTable = properties.lootTableSupplier.get();
            ResourceLocation registryName = block.getRegistryName();
            if(registryName != null && !lootTable.getNamespace().equals(block.getRegistryName().getNamespace()) && !lootTable.getPath().equals("block/" + block.getRegistryName().getPath()))
                properties.lootTable(lootTable);
        }
        return properties;
    }

    private final Material material;
    private MaterialColor mapColor;
    private boolean hasCollision = true;
    private SoundType soundType = SoundType.STONE;
    ToIntFunction<BlockState> lightLevel = state -> 0;
    private float explosionResistance;
    private float destroyTime;
    boolean requiresCorrectTool = false;
    private boolean ticksRandomly = false;
    private float friction = 0.6f;
    float speedFactor = 1.0f;
    float jumpFactor = 1.0f;
    boolean canOcclude = true;
    boolean isAir = false;
    TriPredicate<BlockState,IBlockReader,BlockPos> isRedstoneConductor = (state, level, pos) -> state.getMaterial().isSolidBlocking() && state.isCollisionShapeFullBlock(level, pos);
    TriPredicate<BlockState,IBlockReader,BlockPos> isSuffocating = (state, level, pos) -> state.getMaterial().blocksMotion() && state.isCollisionShapeFullBlock(level, pos);
    private boolean hasDynamicShape = false;
    private boolean noLootTable = false;
    Supplier<ResourceLocation> lootTableSupplier;

    private BlockProperties(Material material){
        this.material = material;
        this.mapColor = material.getColor();
    }

    public BlockProperties mapColor(MaterialColor color){
        if(color == null)
            this.mapColor = this.material.getColor();
        else
            this.mapColor = color;
        return this;
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
        Block.Properties properties = Block.Properties.of(this.material);
        if(this.mapColor != null)
            properties.materialColor = this.mapColor;
        if(!this.hasCollision)
            properties.noCollission();
        properties.sound(this.soundType);
        properties.strength(this.explosionResistance);
        properties.destroyTime = this.destroyTime;
        if(this.ticksRandomly)
            properties.randomTicks();
        properties.friction(this.friction);
        if(this.noLootTable)
            properties.noDrops();
        ((BlockPropertiesAccessor)properties).setLootTableSupplier(this.lootTableSupplier);
        if(this.hasDynamicShape)
            properties.dynamicShape();
        return properties;
    }
}
