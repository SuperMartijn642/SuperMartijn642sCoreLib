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
public abstract class BaseBlockEntity extends TileEntity {

    /**
     * Create's contraptions call {@link #getUpdatePacket()} when placing back blocks, so this should be {@code true} initially
     */
    private boolean dataChanged = true;

    public BaseBlockEntity(TileEntityType<?> tileEntityTypeIn){
        super(tileEntityTypeIn);
    }

    /**
     * Marks the tile entity as dirty and send an update packet to clients.
     */
    public void dataChanged(){
        this.dataChanged = true;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2 | 4);
    }

    /**
     * Writes tile entity data to be saved with the chunk.
     * The stored data will be read in {@link #readData(CompoundNBT)}.
     * @return a {@link CompoundNBT} with the stored data
     */
    protected abstract CompoundNBT writeData();

    /**
     * Writes tile entity data to be sent to the client.
     * The stored data will be read in {@link #readData(CompoundNBT)}.
     * @return a {@link CompoundNBT} with the stored client data
     */
    protected CompoundNBT writeClientData(){
        return this.writeData();
    }

    /**
     * Writes tile entity data to be stored on item stacks.
     * The stored data will be read in {@link #readData(CompoundNBT)}.
     * @return a {@link CompoundNBT} with the stored item stack data
     */
    protected CompoundNBT writeItemStackData(){
        return this.writeData();
    }

    /**
     * Reads data stored by {@link #writeData()}, {@link #writeClientData()},
     * and {@link #writeItemStackData()}.
     * @param tag data to be read
     */
    protected abstract void readData(CompoundNBT tag);

    @Override
    public CompoundNBT save(CompoundNBT compound){
        super.save(compound);
        CompoundNBT data = this.writeData();
        if(data != null && !data.isEmpty())
            compound.put("data", data);
        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt){
        super.load(state, nbt);
        this.readData(nbt.getCompound("data"));
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT tag = super.save(new CompoundNBT());
        CompoundNBT data = this.writeClientData();
        if(data != null && !data.isEmpty())
            tag.put("data", data);
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag){
        super.load(state, tag);
        this.readData(tag.getCompound("data"));
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        if(this.dataChanged){
            this.dataChanged = false;
            CompoundNBT data = this.writeClientData();
            if(data != null && !data.isEmpty())
                return new SUpdateTileEntityPacket(this.worldPosition, 0, this.writeClientData());
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.readData(pkt.getTag());
    }
}
