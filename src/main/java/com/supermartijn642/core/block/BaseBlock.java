package com.supermartijn642.core.block;

import com.supermartijn642.core.ToolType;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class BaseBlock extends Block {

    private final boolean saveTileData;

    public BaseBlock(String registryName, boolean saveTileData, Properties properties){
        super(properties.material, properties.mapColor);
        this.setRegistryName(registryName);
        this.saveTileData = saveTileData;

        this.setSoundType(properties.soundType);
        this.setLightLevel(properties.lightLevel.applyAsInt(this.getDefaultState()));
        this.setResistance(properties.resistance);
        this.setHardness(properties.hardness);
        this.setTickRandomly(properties.ticksRandomly);
        this.setDefaultSlipperiness(properties.slipperiness);
        if(properties.harvestTool != null)
            this.setHarvestLevel(properties.harvestTool.getName(), properties.harvestLevel);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        if(!this.saveTileData)
            return;

        NBTTagCompound tag = stack.getTagCompound();
        tag = tag == null ? null : tag.hasKey("tileData") ? tag.getCompoundTag("tileData") : null;
        if(tag == null || tag.hasNoTags())
            return;

        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof BaseTileEntity)
            ((BaseTileEntity)tile).readData(tag);
    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state){

    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        for(ItemStack drop : this.getActualDrops(worldIn, pos, state, 0))
            spawnAsEntity(worldIn, pos, drop);

//        super.breakBlock(worldIn, pos, state);
    }

    public List<ItemStack> getActualDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
        NonNullList<ItemStack> drops = NonNullList.create();

        super.getDrops(drops, world, pos, state, fortune);

        if(!this.saveTileData)
            return drops;

        TileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof BaseTileEntity))
            return drops;

        if(((BaseTileEntity)tile).destroyedByCreativePlayer)
            return NonNullList.create();

        NBTTagCompound tileTag = ((BaseTileEntity)tile).writeData();
        if(tileTag == null || tileTag.hasNoTags())
            return drops;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("tileData", tileTag);

        for(ItemStack stack : drops){
            if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == this){
                stack.setTagCompound(tag);
            }
        }

        return drops;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player){
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if(!this.saveTileData)
            return stack;

        TileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof BaseTileEntity))
            return stack;

        NBTTagCompound tileTag = ((BaseTileEntity)tile).writeData();
        if(tileTag == null || tileTag.hasNoTags())
            return stack;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("tileData", tileTag);

        if(stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == this)
            stack.setTagCompound(tag);

        return stack;
    }

    public static class Properties {

        private final Material material;
        private final MapColor mapColor;
        private SoundType soundType = SoundType.STONE;
        private ToIntFunction<IBlockState> lightLevel = state -> 0;
        private float resistance;
        private float hardness;
        private boolean requiresTool;
        private boolean ticksRandomly;
        private float slipperiness = 0.6F;
        private float speedFactor = 1.0F;
        private float jumpFactor = 1.0F;
        private int harvestLevel = -1;
        private ToolType harvestTool;
        private boolean variableOpacity;

        private Properties(Material material, MapColor color){
            this.material = material;
            this.mapColor = color;
        }

        public static Properties create(Material material, MapColor color){
            return new Properties(material, color);
        }

        public static Properties create(Material material, EnumDyeColor color){
            return new Properties(material, MapColor.getBlockColor(color));
        }

        public static Properties create(Material material){
            return new Properties(material, material.getMaterialMapColor());
        }

        public Properties harvestLevel(int harvestLevel){
            this.harvestLevel = harvestLevel;
            return this;
        }

        public Properties harvestTool(ToolType harvestTool){
            this.harvestTool = harvestTool;
            return this;
        }

        public int getHarvestLevel(){
            return this.harvestLevel;
        }

        public ToolType getHarvestTool(){
            return this.harvestTool;
        }

        public Properties slipperiness(float slipperinessIn){
            this.slipperiness = slipperinessIn;
            return this;
        }

        public Properties speedFactor(float factor){
            this.speedFactor = factor;
            return this;
        }

        public Properties jumpFactor(float factor){
            this.jumpFactor = factor;
            return this;
        }

        public Properties sound(SoundType soundTypeIn){
            this.soundType = soundTypeIn;
            return this;
        }

        public Properties setLightLevel(ToIntFunction<IBlockState> stateLightFunction){
            this.lightLevel = stateLightFunction;
            return this;
        }

        public Properties hardnessAndResistance(float hardnessIn, float resistanceIn){
            this.hardness = hardnessIn;
            this.resistance = Math.max(0.0F, resistanceIn);
            return this;
        }

        public Properties zeroHardnessAndResistance(){
            return this.hardnessAndResistance(0.0F);
        }

        public Properties hardnessAndResistance(float hardnessAndResistance){
            this.hardnessAndResistance(hardnessAndResistance, hardnessAndResistance);
            return this;
        }

        public Properties tickRandomly(){
            this.ticksRandomly = true;
            return this;
        }

        public Properties setRequiresTool(){
            this.requiresTool = true;
            return this;
        }
    }
}
