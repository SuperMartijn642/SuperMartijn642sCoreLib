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
import net.minecraftforge.client.model.animation.Animation;

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
        return getMinecraft().font;
    }

    public static PlayerEntity getPlayer(){
        return getMinecraft().player;
    }

    public static World getWorld(){
        return getMinecraft().level;
    }

    public static BlockRendererDispatcher getBlockRenderer(){
        return getMinecraft().getBlockRenderer();
    }

    public static ItemRenderer getItemRenderer(){
        return getMinecraft().getItemRenderer();
    }

    public static float getPartialTicks(){
        return Animation.getPartialTickTime();
    }

    /**
     * Closes the player's opened screen
     */
    public static void closeScreen(){
        getPlayer().closeContainer();
    }

    /**
     * Queues the given task on the main thread
     * @param task task to be queued
     */
    public static void queueTask(Runnable task){
        getMinecraft().tell(task);
    }

    public static String translate(String translationKey, Object... args){
        return I18n.get(translationKey, args);
    }

    public static void displayScreen(Screen screen){
        getMinecraft().setScreen(screen);
    }

}
