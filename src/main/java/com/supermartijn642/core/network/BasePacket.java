package com.supermartijn642.core.network;

import net.minecraft.network.PacketBuffer;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public interface BasePacket {

    /**
     * Writes the data in the packet to the given {@code buffer}.
     * The written data will be decoded in {@link #read(PacketBuffer)}.
     * @param buffer data buffer to write to
     */
    void write(PacketBuffer buffer);

    /**
     * Reads data written by {@link #write(PacketBuffer)} from the given
     * {@code buffer} into the packet.
     * @param buffer data buffer to read from
     */
    void read(PacketBuffer buffer);

    /**
     * Checks whether the received values are valid.
     * If {@code false} is returned, the packet will be discarded.
     * @return {@code true} if all received values are valid
     */
    default boolean verify(PacketContext context){
        return true;
    }

    void handle(PacketContext context);

}
