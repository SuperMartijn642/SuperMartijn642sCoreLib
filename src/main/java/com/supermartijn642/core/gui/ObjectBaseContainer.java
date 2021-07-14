package com.supermartijn642.core.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ObjectBaseContainer<T> extends BaseContainer {

    public ObjectBaseContainer(ContainerType<?> type, int id, PlayerEntity player){
        super(type, id, player);
    }

    @Override
    protected void addSlots(PlayerEntity player){
        T object = this.getObjectOrClose();
        if(object != null)
            this.addSlots(player, object);
    }

    /**
     * Adds slots to the container
     */
    protected abstract void addSlots(PlayerEntity player, @Nonnull T object);

    /**
     * Gets the object from {@link #getObject()}, if {@code null} the screen
     * will be closed, the object from {@link #getObject()} will be returned.
     * @return the object from {@link #getObject()} or {@code null}
     */
    @Nullable
    protected T getObjectOrClose(){
        T object = this.getObject();
        if(object == null)
            this.player.closeScreen();
        return object;
    }

    /**
     * @return the object required for the container to remain open
     */
    @Nullable
    protected abstract T getObject();
}
