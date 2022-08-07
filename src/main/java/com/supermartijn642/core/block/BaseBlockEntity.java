package com.supermartijn642.core.block;

import com.supermartijn642.core.registry.Registries;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class BaseBlockEntity extends TileEntity {

    private final BaseBlockEntityType<?> type;

    // Used for block drops
    boolean destroyedByCreativePlayer = false;
    private boolean dataChanged = false;

    public BaseBlockEntity(BaseBlockEntityType<?> type){
        super();
        this.type = type;

        // Add the block entity's class to type's list of classes
        type.blockEntityClasses.add(this.getClass());
    }

    /**
     * Marks the tile entity as dirty and send an update packet to clients.
     */
    public void dataChanged(){
        this.dataChanged = true;
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2 | 4);
    }

    /**
     * Writes tile entity data to be saved with the chunk.
     * The stored data will be read in {@link #readData(NBTTagCompound)}.
     * @return a {@link NBTTagCompound} with the stored data
     */
    protected abstract NBTTagCompound writeData();

    /**
     * Writes tile entity data to be sent to the client.
     * The stored data will be read in {@link #readData(NBTTagCompound)}.
     * @return a {@link NBTTagCompound} with the stored client data
     */
    protected NBTTagCompound writeClientData(){
        return this.writeData();
    }

    /**
     * Writes tile entity data to be stored on item stacks.
     * The stored data will be read in {@link #readData(NBTTagCompound)}.
     * @return a {@link NBTTagCompound} with the stored item stack data
     */
    protected NBTTagCompound writeItemStackData(){
        return this.writeData();
    }

    /**
     * Reads data stored by {@link #writeData()}, {@link #writeClientData()},
     * and {@link #writeItemStackData()}.
     * @param tag data to be read
     */
    protected abstract void readData(NBTTagCompound tag);

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        // Replace the 'id' key
        compound.setString("id", Registries.BLOCK_ENTITY_TYPES.getIdentifier(this.type).toString());
        NBTTagCompound data = this.writeData();
        if(data != null && !data.hasNoTags())
            compound.setTag("data", data);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt){
        super.readFromNBT(nbt);
        this.readData(nbt.getCompoundTag("data"));
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        NBTTagCompound tag = super.writeToNBT(new NBTTagCompound());
        NBTTagCompound data = this.writeClientData();
        if(data != null && !data.hasNoTags())
            tag.setTag("data", data);
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag){
        super.readFromNBT(tag);
        this.readData(tag.getCompoundTag("data"));
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        if(this.dataChanged){
            this.dataChanged = false;
            return new SPacketUpdateTileEntity(this.pos, 0, this.writeClientData());
        }
        return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.readData(pkt.getNbtCompound());
    }

    public IBlockState getBlockState(){
        return this.world.getBlockState(this.pos);
    }

    public BaseBlockEntityType<?> getType(){
        return this.type;
    }
}
