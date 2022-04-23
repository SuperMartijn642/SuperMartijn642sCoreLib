package com.supermartijn642.core.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.world.World;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class BaseContainer extends Container {

    public final PlayerEntity player;
    public final World world;

    public BaseContainer(ContainerType<?> type, int id, PlayerEntity player){
        super(type, id);
        this.player = player;
        this.world = player.level;
    }

    /**
     * Adds slots to the container by calling {@link #addSlots(PlayerEntity)}.
     */
    protected void addSlots(){
        this.addSlots(this.player);
    }

    /**
     * Adds slots to the container
     */
    protected abstract void addSlots(PlayerEntity player);

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

    @Override
    public boolean stillValid(PlayerEntity playerIn){
        return true;
    }
}
