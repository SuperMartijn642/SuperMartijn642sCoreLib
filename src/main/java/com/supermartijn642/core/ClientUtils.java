package com.supermartijn642.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class ClientUtils {

    public static Minecraft getMinecraft(){
        return Minecraft.getInstance();
    }

    public static PlayerEntity getPlayer(){
        return getMinecraft().player;
    }

    public static void closeScreen(){
        getPlayer().closeScreen();
    }

    public static void queueTask(Runnable task){
        getMinecraft().enqueue(task);
    }

}
