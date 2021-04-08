package com.supermartijn642.core.block;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class BaseTileEntity extends TileEntity {

    private boolean dataChanged = false;

    public BaseTileEntity(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
    }

    public void dataChanged(){
        this.dataChanged = true;
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2 | 4);
    }

    protected abstract CompoundNBT writeData();

    protected CompoundNBT writeClientData(){
        return this.writeData();
    }

    protected abstract void readData(CompoundNBT tag);

    @Override
    public CompoundNBT write(CompoundNBT compound){
        super.write(compound);
        CompoundNBT data = this.writeData();
        if(data != null && !data.isEmpty())
            compound.put("data", data);
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt){
        super.read(state, nbt);
        this.readData(nbt.getCompound("data"));
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT tag = super.write(new CompoundNBT());
        CompoundNBT data = this.writeClientData();
        if(data != null && !data.isEmpty())
            tag.put("data", data);
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag){
        super.read(state, tag);
        this.readData(tag.getCompound("data"));
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        if(this.dataChanged){
            this.dataChanged = false;
            return new SUpdateTileEntityPacket(this.pos, 0, this.writeClientData());
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.readData(pkt.getNbtCompound());
    }
}
