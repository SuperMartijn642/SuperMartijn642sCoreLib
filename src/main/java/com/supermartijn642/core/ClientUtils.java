package com.supermartijn642.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    public static Font getFontRenderer(){
        return getMinecraft().font;
    }

    public static Player getPlayer(){
        return getMinecraft().player;
    }

    public static Level getWorld(){
        return getMinecraft().level;
    }

    public static BlockRenderDispatcher getBlockRenderer(){
        return getMinecraft().getBlockRenderer();
    }

    public static ItemRenderer getItemRenderer(){
        return getMinecraft().getItemRenderer();
    }

    public static float getPartialTicks(){
        Minecraft minecraft = getMinecraft();
        return minecraft.isPaused() ? minecraft.pausePartialTick : minecraft.getFrameTime();
    }

    /**
     * Closes the player's opened screen
     */
    public static void closeScreen(){
        Player player = getPlayer();
        player.containerMenu = player.inventoryMenu;
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
