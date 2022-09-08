package com.supermartijn642.core.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class BaseContainer extends AbstractContainerMenu {

    public final Player player;
    public final Level level;

    public BaseContainer(BaseContainerType<?> type, Player player){
        super(type, 0);
        this.player = player;
        this.level = player.level;
    }

    public void setContainerId(int id){
        this.containerId = id;
    }

    public BaseContainerType<?> getContainerType(){
        return (BaseContainerType<?>)this.getType();
    }

    /**
     * Adds slots to the container by calling {@link #addSlots(Player)}.
     */
    protected void addSlots(){
        this.addSlots(this.player);
    }

    /**
     * Adds slots to the container
     */
    protected abstract void addSlots(Player player);

    /**
     * Adds the player's slots to the container at the given {@code x} and {@code y}.
     * @param x the x-coordinate of the left side of the left most slots
     * @param y the y-coordinate of the top edge of the top most slots
     */
    protected void addPlayerSlots(int x, int y){
        // player
        for(int row = 0; row < 3; row++){
            for(int column = 0; column < 9; column++){
                this.addSlot(new Slot(this.player.getInventory(), row * 9 + column + 9, x + 18 * column, y + 18 * row));
            }
        }

        // hot bar
        for(int column = 0; column < 9; column++)
            this.addSlot(new Slot(this.player.getInventory(), column, x + 18 * column, y + 58));
    }

    @Override
    public boolean stillValid(Player playerIn){
        return true;
    }
}
