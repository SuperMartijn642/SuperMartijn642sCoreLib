package com.supermartijn642.core.network;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CoreSide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Created 5/30/2021 by SuperMartijn642
 */
public class PacketContext {

    private final CoreSide handlingSide;
    private final Player sendingPlayer;
    private final MinecraftServer server;

    public PacketContext(CoreSide handlingSide, Player sendingPlayer, MinecraftServer server){
        this.handlingSide = handlingSide;
        this.sendingPlayer = sendingPlayer;
        this.server = server;
    }

    /**
     * @return the side the packet is received on
     */
    public CoreSide getHandlingSide(){
        return this.handlingSide;
    }

    /**
     * @return the side the packet is originating from
     */
    public CoreSide getOriginatingSide(){
        return this.handlingSide == CoreSide.CLIENT ? CoreSide.SERVER : CoreSide.CLIENT;
    }

    public Player getSendingPlayer(){
        return this.sendingPlayer;
    }

    /**
     * @return the client world if client-side, or the sending player's world if server-side
     */
    public Level getWorld(){
        return this.getHandlingSide() == CoreSide.CLIENT ? ClientUtils.getWorld() : this.getSendingPlayer().level;
    }

    public void queueTask(Runnable task){
        if(this.getHandlingSide() == CoreSide.SERVER)
            this.server.submit(task);
        else
            ClientUtils.queueTask(task);
    }
}
