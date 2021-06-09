package com.supermartijn642.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class ClientUtils {

    public static Minecraft getMinecraft(){
        return Minecraft.getMinecraft();
    }

    public static TextureManager getTextureManager(){
        return getMinecraft().getTextureManager();
    }

    public static FontRenderer getFontRenderer(){
        return getMinecraft().fontRenderer;
    }

    public static EntityPlayer getPlayer(){
        return getMinecraft().player;
    }

    public static World getWorld(){
        return getMinecraft().world;
    }

    public static BlockRendererDispatcher getBlockRenderer(){
        return getMinecraft().getBlockRendererDispatcher();
    }

    public static RenderItem getItemRenderer(){
        return getMinecraft().getRenderItem();
    }

    public static float getPartialTicks(){
        return getMinecraft().getRenderPartialTicks();
    }

    /**
     * Closes the player's opened screen
     */
    public static void closeScreen(){
        getPlayer().closeScreen();
    }

    /**
     * Queues the given task on the main thread
     * @param task task to be queued
     */
    public static void queueTask(Runnable task){
        getMinecraft().addScheduledTask(task);
    }

    public static String translate(String translationKey, Object... args){
        return I18n.format(translationKey, args);
    }

    public static void displayScreen(GuiScreen screen){
        getMinecraft().displayGuiScreen(screen);
    }

}
