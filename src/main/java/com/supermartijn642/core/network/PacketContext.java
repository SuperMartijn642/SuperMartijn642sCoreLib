package com.supermartijn642.core.network;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreSide;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public class PacketContext {

    private final NetworkEvent.Context context;

    public PacketContext(NetworkEvent.Context context){
        this.context = context;
    }

    /**
     * @return the side the packet is received on
     */
    public CoreSide getHandlingSide(){
        return this.context.getDirection().getReceptionSide() == LogicalSide.CLIENT ? CoreSide.CLIENT : CoreSide.SERVER;
    }

    /**
     * @return the side the packet is originating from
     */
    public CoreSide getOriginatingSide(){
        return this.context.getDirection().getOriginationSide() == LogicalSide.CLIENT ? CoreSide.CLIENT : CoreSide.SERVER;
    }

    /**
     * @return the local player on the client, the player entity corresponding to the client that sent the packet on the server
     */
    public PlayerEntity getPlayer(){
        if(this.getHandlingSide().isClient())
            return ClientUtils.getPlayer();
        return this.context.getSender();
    }

    /**
     * @deprecated Use {@link #getPlayer()}.
     */
    @Deprecated
    public PlayerEntity getSendingPlayer(){
        return this.context.getSender();
    }

    /**
     * @return the client world if client-side, or the sending player's world if server-side
     */
    public World getWorld(){
        return this.getHandlingSide() == CoreSide.CLIENT ? ClientUtils.getWorld() : this.getSendingPlayer().level;
    }

    public void queueTask(Runnable task){
        if(this.getHandlingSide() == CoreSide.SERVER)
            this.context.enqueueWork(task);
        else
            ClientUtils.queueTask(task);
    }

    @Deprecated
    public NetworkEvent.Context getUnderlyingContext(){
        return this.context;
    }

}
