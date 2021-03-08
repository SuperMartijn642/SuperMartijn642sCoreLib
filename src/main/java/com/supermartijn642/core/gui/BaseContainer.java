package com.supermartijn642.core.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;

/**
 * Created 1/19/2021 by SuperMartijn642
 */
public abstract class BaseContainer extends Container {

    public final EntityPlayer player;
    public final World world;

    public BaseContainer(EntityPlayer player){
        this.player = player;
        this.world = player.world;
    }

    protected void addSlots(){
        this.addSlots(this.player);
    }

    protected abstract void addSlots(EntityPlayer player);

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
