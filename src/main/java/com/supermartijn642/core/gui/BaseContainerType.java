package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public final class BaseContainerType<T extends BaseContainer> extends ExtendedScreenHandlerType<T,T> {

    /**
     * Creates a new container type.
     * @param containerSerializer   used to write container's data for the client
     * @param containerDeserializer used to read data from the server to create a container
     */
    public static <T extends BaseContainer> BaseContainerType<T> create(BiConsumer<T,FriendlyByteBuf> containerSerializer, BiFunction<Player,FriendlyByteBuf,T> containerDeserializer){
        return new BaseContainerType<>(containerSerializer, containerDeserializer);
    }

    private final BiConsumer<T,FriendlyByteBuf> containerSerializer;
    private final BiFunction<Player,FriendlyByteBuf,T> containerDeserializer;

    private BaseContainerType(BiConsumer<T,FriendlyByteBuf> containerSerializer, BiFunction<Player,FriendlyByteBuf,T> containerDeserializer){
        super((id, inventory, container) -> {
            container.setContainerId(id);
            return container;
        }, StreamCodec.of(
            (buffer, container) -> containerSerializer.accept(container, buffer),
            buffer -> containerDeserializer.apply(ClientUtils.getPlayer(), buffer)
        ));
        this.containerSerializer = containerSerializer;
        this.containerDeserializer = containerDeserializer;
    }

    public void writeContainer(T container, FriendlyByteBuf buffer){
        this.containerSerializer.accept(container, buffer);
    }
}
