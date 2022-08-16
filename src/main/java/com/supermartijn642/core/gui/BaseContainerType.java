package com.supermartijn642.core.gui;

import com.supermartijn642.core.util.TriFunction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.function.BiConsumer;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public final class BaseContainerType<T extends Container> extends ContainerType<T> {

    /**
     * Creates a new container type.
     * @param containerSerializer   used to write container's data for the client
     * @param containerDeserializer used to read data from the server to create a container
     */
    public static <T extends Container> BaseContainerType<T> create(BiConsumer<T,PacketBuffer> containerSerializer, TriFunction<Integer,PlayerEntity,PacketBuffer,T> containerDeserializer){
        return new BaseContainerType<>(containerSerializer, containerDeserializer);
    }

    private final BiConsumer<T,PacketBuffer> containerSerializer;
    private final TriFunction<Integer,PlayerEntity,PacketBuffer,T> containerDeserializer;

    private BaseContainerType(BiConsumer<T,PacketBuffer> containerSerializer, TriFunction<Integer,PlayerEntity,PacketBuffer,T> containerDeserializer){
        super((IContainerFactory<T>)(id, inventory, data) -> containerDeserializer.apply(id, inventory.player, data));
        this.containerSerializer = containerSerializer;
        this.containerDeserializer = containerDeserializer;
    }

    public void writeContainer(T container, PacketBuffer buffer){
        this.containerSerializer.accept(container, buffer);
    }
}
