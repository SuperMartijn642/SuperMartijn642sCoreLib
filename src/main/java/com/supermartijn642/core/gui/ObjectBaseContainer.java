package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;

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

    protected abstract void addSlots(PlayerEntity player, T object);

    protected T getObjectOrClose(){
        T object = this.getObject();
        if(object == null)
            ClientUtils.closeScreen();
        return object;
    }

    protected abstract T getObject();
}
