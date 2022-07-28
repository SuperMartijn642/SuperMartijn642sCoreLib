package com.supermartijn642.core.gui;

import com.supermartijn642.core.ClientUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nonnull;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public abstract class ObjectBaseContainer<T> extends BaseContainer {

    protected T object;
    private final boolean alwaysRenewObject;

    public ObjectBaseContainer(MenuType<?> type, int id, Player player, boolean alwaysRenewObject){
        super(type, id, player);
        this.alwaysRenewObject = alwaysRenewObject;
    }

    public ObjectBaseContainer(MenuType<?> type, int id, Player player){
        this(type, id, player, false);
    }

    @Override
    protected void addSlots(Player player){
        if(!this.validateObjectOrClose())
            this.addSlots(player, this.object);
    }

    /**
     * Adds slots to the container
     */
    protected abstract void addSlots(Player player, @Nonnull T object);

    /**
     * Called to obtain object needed for this widget to remain active. May be called at any time.
     * @param oldObject the old object, will be {@code null} when the widget is first added
     * @return the object required for the container to remain open
     */
    protected abstract T getObject(T oldObject);

    /**
     * Validates the object obtained from {@link #getObject(Object)}.
     * The associated screen will be closed if {@code false} is returned.
     * @param object object to be validated, may be null
     * @return true if the object is valid
     */
    protected abstract boolean validateObject(T object);

    /**
     * Validates the object. If the object is not valid the screen will be closed.
     * @return true if the object is valid
     */
    protected boolean validateObjectOrClose(){
        if(this.alwaysRenewObject || !this.validateObject(this.object)){
            this.object = this.getObject(this.object);
            if(!this.validateObject(this.object)){
                ClientUtils.closeScreen();
                return false;
            }
        }
        return true;
    }
}
