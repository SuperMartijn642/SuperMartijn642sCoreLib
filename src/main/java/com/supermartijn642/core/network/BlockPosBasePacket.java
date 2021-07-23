package com.supermartijn642.core.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public abstract class BlockPosBasePacket implements BasePacket {

    public BlockPos pos;

    public BlockPosBasePacket(){
    }

    /**
     * Stores the given {@code pos} with the packet data.
     * @param pos position to be stored
     */
    public BlockPosBasePacket(BlockPos pos){
        this.pos = pos;
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void handle(PacketContext context){
        this.handle(this.pos, context);
    }

    protected abstract void handle(BlockPos pos, PacketContext context);
}
