package com.supermartijn642.core.network;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreSide;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public class PacketContext {

    private final CustomPayloadEvent.Context context;

    public PacketContext(CustomPayloadEvent.Context context){
        this.context = context;
    }

    /**
     * @return the side the packet is received on
     */
    public CoreSide getHandlingSide(){
        return this.context.isClientSide() ? CoreSide.CLIENT : CoreSide.SERVER;
    }

    /**
     * @return the side the packet is originating from
     */
    public CoreSide getOriginatingSide(){
        return this.context.isServerSide() ? CoreSide.CLIENT : CoreSide.SERVER;
    }

    public Player getSendingPlayer(){
        return this.context.getSender();
    }

    /**
     * @return the client world if client-side, or the sending player's world if server-side
     */
    public Level getWorld(){
        return this.getHandlingSide() == CoreSide.CLIENT ? ClientUtils.getWorld() : this.getSendingPlayer().level();
    }

    public void queueTask(Runnable task){
        this.context.enqueueWork(task);
    }

    @Deprecated
    public CustomPayloadEvent.Context getUnderlyingContext(){
        return this.context;
    }

}
