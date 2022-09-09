package com.supermartijn642.core.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public final class BaseContainerType<T extends BaseContainer> extends ContainerType<T> {

    /**
     * Creates a new container type.
     * @param containerSerializer   used to write container's data for the client
     * @param containerDeserializer used to read data from the server to create a container
     */
    public static <T extends BaseContainer> BaseContainerType<T> create(BiConsumer<T,PacketBuffer> containerSerializer, BiFunction<PlayerEntity,PacketBuffer,T> containerDeserializer){
        return new BaseContainerType<>(containerSerializer, containerDeserializer);
    }

    private final BiConsumer<T,PacketBuffer> containerSerializer;
    private final BiFunction<PlayerEntity,PacketBuffer,T> containerDeserializer;

    private BaseContainerType(BiConsumer<T,PacketBuffer> containerSerializer, BiFunction<PlayerEntity,PacketBuffer,T> containerDeserializer){
        super((IContainerFactory<T>)(id, inventory, data) -> {
            T container = containerDeserializer.apply(inventory.player, data);
            container.setContainerId(id);
            return container;
        });
        this.containerSerializer = containerSerializer;
        this.containerDeserializer = containerDeserializer;
    }

    public void writeContainer(T container, PacketBuffer buffer){
        this.containerSerializer.accept(container, buffer);
    }
}
