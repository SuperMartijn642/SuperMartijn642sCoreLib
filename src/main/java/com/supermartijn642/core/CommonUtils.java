package com.supermartijn642.core;

import com.google.common.eventbus.EventBus;
import com.supermartijn642.core.gui.BaseContainerType;
import com.supermartijn642.core.network.OpenContainerPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created 20/03/2022 by SuperMartijn642
 */
public class CommonUtils {

    private static final Field modControllerField;
    private static final Field eventChannelsField;

    static{
        try{
            modControllerField = Loader.class.getDeclaredField("modController");
            modControllerField.setAccessible(true);
            eventChannelsField = LoadController.class.getDeclaredField("eventChannels");
            eventChannelsField.setAccessible(true);
        }catch(NoSuchFieldException e){
            throw new RuntimeException(e);
        }
    }

    private static MinecraftServer server;

    static void initialize(){
        EventBus eventBus = getEventBus("supermartijn642corelib");
        eventBus.register(new Object() {
            @SubscribeEvent
            public void serverAboutToStart(FMLServerAboutToStartEvent e){
                server = e.getServer();
            }

            @SubscribeEvent
            public void serverStopped(FMLServerStoppedEvent e){
                server = null;
            }
        });
    }

    /**
     * @return the integrated server on the client or the server instance a dedicated server
     */
    public static MinecraftServer getServer(){
        return server;
    }

    public static World getLevel(DimensionType dimensionType){
        MinecraftServer server = getServer();
        return server == null ? null : server.getWorld(dimensionType.getId());
    }

    /**
     * @return which environment the game is running in.
     */
    public static CoreSide getEnvironmentSide(){
        return CoreSide.fromUnderlying(FMLCommonHandler.instance().getSide());
    }

    /**
     * Checks whether a mod with the given modid is loaded and active.
     */
    public static boolean isModLoaded(String modid){
        return Loader.isModLoaded(modid);
    }

    @SuppressWarnings("unchecked")
    public static EventBus getEventBus(String modid){
        try{
            LoadController modController = (LoadController)modControllerField.get(Loader.instance());
            Map<String,EventBus> eventChannels = (Map<String,EventBus>)eventChannelsField.get(modController);
            return eventChannels.get(modid);
        }catch(Exception e){
            CoreLib.LOGGER.error("Encountered an error whilst trying to obtain internal event bus for modid '" + modid + "'!", e);
        }
        return null;
    }

    public <T extends Container> void openContainer(EntityPlayer player, BaseContainerType<T> baseContainerType, T container){
        if(!(player instanceof EntityPlayerMP))
            return;

        ((EntityPlayerMP)player).getNextWindowId();
        ((EntityPlayerMP)player).closeContainer();
        CoreLib.CHANNEL.sendToPlayer(player, new OpenContainerPacket<>(baseContainerType, container));
        player.openContainer = container;
        container.windowId = ((EntityPlayerMP)player).currentWindowId;
        container.addListener((EntityPlayerMP)player);
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
    }
}
