package com.supermartijn642.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class BaseBlockEntity extends BlockEntity {

    /**
     * Create's contraptions call {@link #getUpdatePacket()} when placing back blocks, so this should be {@code true} initially
     */
    private boolean dataChanged = true;

    public BaseBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state){
        super(tileEntityTypeIn, pos, state);
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
     * The stored data will be read in {@link #readData(CompoundTag)}.
     * @return a {@link CompoundTag} with the stored data
     */
    protected abstract CompoundTag writeData();

    /**
     * Writes tile entity data to be sent to the client.
     * The stored data will be read in {@link #readData(CompoundTag)}.
     * @return a {@link CompoundTag} with the stored client data
     */
    protected CompoundTag writeClientData(){
        return this.writeData();
    }

    /**
     * Writes tile entity data to be stored on item stacks.
     * The stored data will be read in {@link #readData(CompoundTag)}.
     * @return a {@link CompoundTag} with the stored item stack data
     */
    protected CompoundTag writeItemStackData(){
        return this.writeData();
    }

    /**
     * Reads data stored by {@link #writeData()}, {@link #writeClientData()},
     * and {@link #writeItemStackData()}.
     * @param tag data to be read
     */
    protected abstract void readData(CompoundTag tag);

    @Override
    public CompoundTag save(CompoundTag compound){
        super.save(compound);
        CompoundTag data = this.writeData();
        if(data != null && !data.isEmpty())
            compound.put("data", data);
        return compound;
    }

    @Override
    public void load(CompoundTag nbt){
        super.load(nbt);
        this.readData(nbt.getCompound("data"));
    }

    @Override
    public CompoundTag getUpdateTag(){
        CompoundTag tag = super.save(new CompoundTag());
        CompoundTag data = this.writeClientData();
        if(data != null && !data.isEmpty())
            tag.put("data", data);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag){
        super.load(tag);
        this.readData(tag.getCompound("data"));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        if(this.dataChanged){
            this.dataChanged = false;
            return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.writeClientData());
        }
        return null;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
        this.readData(pkt.getTag() == null ? new CompoundTag() : pkt.getTag());
    }
}
