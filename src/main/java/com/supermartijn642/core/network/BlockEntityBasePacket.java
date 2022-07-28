package com.supermartijn642.core.network;

import com.supermartijn642.core.CoreSide;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public abstract class BlockEntityBasePacket<T extends TileEntity> extends BlockPosBasePacket {

    public RegistryKey<World> dimension;

    public BlockEntityBasePacket(){
    }

    /**
     * Grabs the tile entity in {@code dimension} at {@code pos}.
     * @param dimension dimension of the tile entity
     * @param pos position of the tile entity
     */
    public BlockEntityBasePacket(RegistryKey<World> dimension, BlockPos pos){
        super(pos);
        this.dimension = dimension;
    }

    /**
     * Grabs the tile entity in {@code world} at {@code pos}.
     * @param world world the tile entity is in
     * @param pos position of the tile entity
     */
    public BlockEntityBasePacket(World world, BlockPos pos){
        this(world == null ? null : world.dimension(), pos);
    }

    /**
     * Grabs the tile entity at {@code pos} in the relevant player's dimension.
     * @param pos position of the tile entity
     */
    public BlockEntityBasePacket(BlockPos pos){
        this((RegistryKey<World>)null, pos);
    }

    @Override
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeBoolean(this.dimension != null);
        if(this.dimension != null)
            buffer.writeResourceLocation(this.dimension.location());
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        if(buffer.readBoolean())
            this.dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
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
        World world = this.dimension == null ? context.getWorld() :
            context.getHandlingSide() == CoreSide.CLIENT ?
                context.getWorld().dimension() == this.dimension ? context.getWorld() : null :
                context.getWorld().getServer().getLevel(this.dimension);

        if(world == null)
            return null;

        TileEntity tile = world.getBlockEntity(this.pos);

        if(tile == null)
            return null;

        try{
            return (T)tile;
        }catch(ClassCastException ignore){}
        return null;
    }
}
