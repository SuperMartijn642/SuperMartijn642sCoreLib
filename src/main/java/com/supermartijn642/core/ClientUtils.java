package com.supermartijn642.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class ClientUtils {

    public static Minecraft getMinecraft(){
        return Minecraft.getInstance();
    }

    public static TextureManager getTextureManager(){
        return getMinecraft().getTextureManager();
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

    public static String translate(String translationKey, Object... args){
        return I18n.format(translationKey, args);
    }

}
