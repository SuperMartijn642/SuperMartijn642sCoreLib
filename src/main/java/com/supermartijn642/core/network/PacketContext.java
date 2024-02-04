package com.supermartijn642.core.network;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreSide;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public class PacketContext {

    private final IPayloadContext context;

    public PacketContext(IPayloadContext context){
        this.context = context;
    }

    /**
     * @return the side the packet is received on
     */
    public CoreSide getHandlingSide(){
        return this.context.flow().isClientbound() ? CoreSide.CLIENT : CoreSide.SERVER;
    }

    /**
     * @return the side the packet is originating from
     */
    public CoreSide getOriginatingSide(){
        return this.context.flow().isServerbound() ? CoreSide.CLIENT : CoreSide.SERVER;
    }

    public Player getSendingPlayer(){
        return this.context.player().orElse(null);
    }

    /**
     * @return the client world if client-side, or the sending player's world if server-side
     */
    public Level getWorld(){
        return this.getHandlingSide() == CoreSide.CLIENT ? ClientUtils.getWorld() : this.getSendingPlayer().level();
    }

    public void queueTask(Runnable task){
        this.context.workHandler().execute(task);
    }

    @Deprecated
    public IPayloadContext getUnderlyingContext(){
        return this.context;
    }

}
