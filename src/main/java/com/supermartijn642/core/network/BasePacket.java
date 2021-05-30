package com.supermartijn642.core.network;

import net.minecraft.network.PacketBuffer;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public interface BasePacket {

    void write(PacketBuffer buffer);

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
