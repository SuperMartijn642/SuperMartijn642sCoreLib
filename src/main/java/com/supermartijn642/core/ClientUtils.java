package com.supermartijn642.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class ClientUtils {

    public static Minecraft getMinecraft(){
        return Minecraft.getMinecraft();
    }

    public static EntityPlayer getPlayer(){
        return getMinecraft().player;
    }

    public static void closeScreen(){
        getPlayer().closeScreen();
    }

    public static void queueTask(Runnable task){
        getMinecraft().addScheduledTask(task);
    }

}
