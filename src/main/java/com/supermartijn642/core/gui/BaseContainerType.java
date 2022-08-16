package com.supermartijn642.core.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiConsumer;

/**
 * Created 05/08/2022 by SuperMartijn642
 */
public final class BaseContainerType<T extends AbstractContainerMenu> extends MenuType<T> {

    /**
     * Creates a new container type.
     * @param containerSerializer   used to write container's data for the client
     * @param containerDeserializer used to read data from the server to create a container
     */
    public static <T extends AbstractContainerMenu> BaseContainerType<T> create(BiConsumer<T,FriendlyByteBuf> containerSerializer, TriFunction<Integer,Player,FriendlyByteBuf,T> containerDeserializer){
        return new BaseContainerType<>(containerSerializer, containerDeserializer);
    }

    private final BiConsumer<T,FriendlyByteBuf> containerSerializer;
    private final TriFunction<Integer,Player,FriendlyByteBuf,T> containerDeserializer;

    private BaseContainerType(BiConsumer<T,FriendlyByteBuf> containerSerializer, TriFunction<Integer,Player,FriendlyByteBuf,T> containerDeserializer){
        super((IContainerFactory<T>)(id, inventory, data) -> containerDeserializer.apply(id, inventory.player, data));
        this.containerSerializer = containerSerializer;
        this.containerDeserializer = containerDeserializer;
    }

    public void writeContainer(T container, FriendlyByteBuf buffer){
        this.containerSerializer.accept(container, buffer);
    }
}
