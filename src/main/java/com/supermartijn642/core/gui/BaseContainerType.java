package com.supermartijn642.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public final class BaseContainerType<T extends Container> {

    /**
     * Creates a new container type.
     * @param containerSerializer   used to write container's data for the client
     * @param containerDeserializer used to read data from the server to create a container
     */
    public static <T extends BaseContainer> BaseContainerType<T> create(BiConsumer<T,PacketBuffer> containerSerializer, BiFunction<EntityPlayer,PacketBuffer,T> containerDeserializer){
        return new BaseContainerType<>(containerSerializer, containerDeserializer);
    }

    private final BiConsumer<T,PacketBuffer> containerSerializer;
    private final BiFunction<EntityPlayer,PacketBuffer,T> containerDeserializer;

    private BaseContainerType(BiConsumer<T,PacketBuffer> containerSerializer, BiFunction<EntityPlayer,PacketBuffer,T> containerDeserializer){
        this.containerSerializer = containerSerializer;
        this.containerDeserializer = containerDeserializer;
    }

    public void writeContainer(T container, PacketBuffer buffer){
        this.containerSerializer.accept(container, buffer);
    }

    public T readContainer(EntityPlayer player, PacketBuffer buffer){
        return this.containerDeserializer.apply(player, buffer);
    }
}
