package com.supermartijn642.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class BaseBlock extends Block {

    private final boolean saveTileData;

    public BaseBlock(String registryName, boolean saveTileData, Properties properties){
        super(properties);
        this.setRegistryName(registryName);
        this.saveTileData = saveTileData;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
        if(!this.saveTileData)
            return;

        CompoundNBT tag = stack.getTag();
        tag = tag == null ? null : tag.contains("tileData") ? tag.getCompound("tileData") : null;
        if(tag == null || tag.isEmpty())
            return;

        TileEntity tile = worldIn.getBlockEntity(pos);
        if(tile instanceof BaseTileEntity)
            ((BaseTileEntity)tile).readData(tag);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder){
        List<ItemStack> items = super.getDrops(state, builder);

        if(!this.saveTileData)
            return items;

        TileEntity tile = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if(!(tile instanceof BaseTileEntity))
            return items;

        CompoundNBT tileTag = ((BaseTileEntity)tile).writeData();
        if(tileTag == null || tileTag.isEmpty())
            return items;

        CompoundNBT tag = new CompoundNBT();
        tag.put("tileData", tileTag);

        for(ItemStack stack : items){
            if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == this){
                stack.setTag(tag);
            }
        }

        return items;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player){
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if(!this.saveTileData)
            return stack;

        TileEntity tile = world.getBlockEntity(pos);
        if(!(tile instanceof BaseTileEntity))
            return stack;

        CompoundNBT tileTag = ((BaseTileEntity)tile).writeData();
        if(tileTag == null || tileTag.isEmpty())
            return stack;

        CompoundNBT tag = new CompoundNBT();
        tag.put("tileData", tileTag);

        if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() == this)
            stack.setTag(tag);

        return stack;
    }
}
