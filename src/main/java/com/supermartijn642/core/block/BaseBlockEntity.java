package com.supermartijn642.core.block;

import com.supermartijn642.core.CoreLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
     * Writes tile entity data to be saved with the chunk to the given output.
     * The stored data will be read in {@link #readData(ValueInput)}.
     */
    protected abstract void writeData(ValueOutput output);

    /**
     * Writes tile entity data to be sent to the client to the given output.
     * The stored data will be read in {@link #readData(ValueInput)}.
     */
    protected void writeClientData(ValueOutput output){
        this.writeData(output);
    }

    /**
     * Writes tile entity data to be stored on item stacks to the given output.
     * The stored data will be read in {@link #readData(ValueInput)}.
     */
    protected void writeItemStackData(ValueOutput output){
        this.writeData(output);
    }

    /**
     * Reads data stored by {@link #writeData(ValueOutput)}, {@link #writeClientData(ValueOutput)},
     * and {@link #writeItemStackData(ValueOutput)}.
     */
    protected abstract void readData(ValueInput input);

    @Override
    protected void saveAdditional(ValueOutput output){
        super.saveAdditional(output);
        this.writeData(output.child("data"));
    }

    @Override
    protected void loadAdditional(ValueInput valueInput){
        super.loadAdditional(valueInput);
        this.readData(valueInput.childOrEmpty("data"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider){
        TagValueOutput output = TagValueOutput.createWithContext(new ProblemReporter.ScopedCollector(this.problemPath(), CoreLib.LOGGER), provider);
        super.saveAdditional(output);
        this.writeClientData(output.child("data"));
        return output.buildResult();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        if(this.dataChanged){
            this.dataChanged = false;
            return ClientboundBlockEntityDataPacket.create(this, (entity, registryAccess) -> {
                TagValueOutput output = TagValueOutput.createWithContext(new ProblemReporter.ScopedCollector(this.problemPath(), CoreLib.LOGGER), registryAccess);
                ((BaseBlockEntity)entity).writeClientData(output.child("data"));
                return output.buildResult();
            });
        }
        return null;
    }
}
