package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ObjectBaseContainer<T> extends BaseContainer {

    public ObjectBaseContainer(EntityPlayer player){
        super(player);
    }

    @Override
    protected void addSlots(EntityPlayer player){
        T object = this.getObjectOrClose();
        if(object != null)
            this.addSlots(player, object);
    }

    /**
     * Adds slots to the container
     */
    protected abstract void addSlots(EntityPlayer player, @Nonnull T object);

    /**
     * Gets the object from {@link #getObject()}, if {@code null} the screen
     * will be closed, the object from {@link #getObject()} will be returned.
     * @return the object from {@link #getObject()} or {@code null}
     */
    @Nullable
    protected T getObjectOrClose(){
        T object = this.getObject();
        if(object == null)
            ClientUtils.closeScreen();
        return object;
    }

    /**
     * @return the object required for the container to remain open
     */
    @Nullable
    protected abstract T getObject();
}
