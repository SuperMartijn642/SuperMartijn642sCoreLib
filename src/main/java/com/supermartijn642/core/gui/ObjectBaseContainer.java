package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.entity.player.EntityPlayer;

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

    protected abstract void addSlots(EntityPlayer player, T object);

    protected T getObjectOrClose(){
        T object = this.getObject();
        if(object == null)
            ClientUtils.closeScreen();
        return object;
    }

    protected abstract T getObject();
}
