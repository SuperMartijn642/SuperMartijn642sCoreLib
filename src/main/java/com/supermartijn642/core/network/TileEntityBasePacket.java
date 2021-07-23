package com.supermartijn642.core.network;

import com.supermartijn642.core.CoreSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public abstract class TileEntityBasePacket<T extends BlockEntity> extends BlockPosBasePacket {

    public ResourceKey<Level> dimension;

    public TileEntityBasePacket(){
    }

    /**
     * Grabs the tile entity in {@code dimension} at {@code pos}.
     * @param dimension dimension of the tile entity
     * @param pos       position of the tile entity
     */
    public TileEntityBasePacket(ResourceKey<Level> dimension, BlockPos pos){
        super(pos);
        this.dimension = dimension;
    }

    /**
     * Grabs the tile entity in {@code world} at {@code pos}.
     * @param world world the tile entity is in
     * @param pos   position of the tile entity
     */
    public TileEntityBasePacket(Level world, BlockPos pos){
        this(world == null ? null : world.dimension(), pos);
    }

    /**
     * Grabs the tile entity at {@code pos} in the relevant player's dimension.
     * @param pos position of the tile entity
     */
    public TileEntityBasePacket(BlockPos pos){
        this((ResourceKey<Level>)null, pos);
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        super.write(buffer);
        buffer.writeBoolean(this.dimension != null);
        if(this.dimension != null)
            buffer.writeResourceLocation(this.dimension.location());
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        super.read(buffer);
        if(buffer.readBoolean())
            this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
    }

    @Override
    protected void handle(BlockPos pos, PacketContext context){
        T tile = this.getTileEntity(context);
        if(tile != null)
            this.handle(tile, context);
    }

    protected abstract void handle(T tile, PacketContext context);

    @SuppressWarnings("unchecked")
    private T getTileEntity(PacketContext context){
        Level world = this.dimension == null ? context.getWorld() :
            context.getHandlingSide() == CoreSide.CLIENT ?
                context.getWorld().dimension() == this.dimension ? context.getWorld() : null :
                context.getWorld().getServer().getLevel(this.dimension);

        if(world == null)
            return null;

        BlockEntity tile = world.getBlockEntity(this.pos);

        if(tile == null)
            return null;

        try{
            return (T)tile;
        }catch(ClassCastException ignore){}
        return null;
    }
}
