package com.supermartijn642.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

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

    public static FontRenderer getFontRenderer(){
        return getMinecraft().fontRenderer;
    }

    public static PlayerEntity getPlayer(){
        return getMinecraft().player;
    }

    public static World getWorld(){
        return getMinecraft().world;
    }

    public static BlockRendererDispatcher getBlockRenderer(){
        return getMinecraft().getBlockRendererDispatcher();
    }

    public static ItemRenderer getItemRenderer(){
        return getMinecraft().getItemRenderer();
    }

    public static float getPartialTicks(){
        return getMinecraft().getRenderPartialTicks();
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

    public static void displayScreen(Screen screen){
        getMinecraft().displayGuiScreen(screen);
    }

}
