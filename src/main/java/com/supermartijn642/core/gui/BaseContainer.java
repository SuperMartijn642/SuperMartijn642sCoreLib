package com.supermartijn642.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class BaseContainer extends Container {

    private final BaseContainerType<?> containerType;
    public final EntityPlayer player;
    public final World level;

    public BaseContainer(BaseContainerType<?> type, EntityPlayer player){
        this.containerType = type;
        this.player = player;
        this.level = player.world;
    }

    public BaseContainerType<?> getContainerType(){
        return this.containerType;
    }

    /**
     * Adds slots to the container by calling {@link #addSlots(EntityPlayer)}.
     */
    protected void addSlots(){
        this.addSlots(this.player);
    }

    /**
     * Adds slots to the container
     */
    protected abstract void addSlots(EntityPlayer player);

    /**
     * Adds the player's slots to the container at the given {@code x} and {@code y}.
     * @param x the x-coordinate of the left side of the left most slots
     * @param y the y-coordinate of the top edge of the top most slots
     */
    protected void addPlayerSlots(int x, int y){
        // player
        for(int row = 0; row < 3; row++){
            for(int column = 0; column < 9; column++){
                this.addSlot(new Slot(this.player.inventory, row * 9 + column + 9, x + 18 * column, y + 18 * row));
            }
        }

        // hot bar
        for(int column = 0; column < 9; column++)
            this.addSlot(new Slot(this.player.inventory, column, x + 18 * column, y + 58));
    }

    protected Slot addSlot(Slot slot){
        return this.addSlotToContainer(slot);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn){
        return true;
    }
}
