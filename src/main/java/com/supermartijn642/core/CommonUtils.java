package com.supermartijn642.core;

import com.supermartijn642.core.gui.BaseContainer;
import com.supermartijn642.core.gui.BaseContainerType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Created 20/03/2022 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonUtils {

    private static MinecraftServer server;

    static void initialize(){
        MinecraftForge.EVENT_BUS.addListener((Consumer<ServerAboutToStartEvent>)(e -> server = e.getServer()));
        MinecraftForge.EVENT_BUS.addListener((Consumer<ServerStoppedEvent>)(e -> server = null));
    }

    /**
     * @return the integrated server on the client or the server instance a dedicated server
     */
    public static MinecraftServer getServer(){
        return server;
    }

    public static Level getLevel(ResourceKey<Level> resourceKey){
        MinecraftServer server = getServer();
        return server == null ? null : server.getLevel(resourceKey);
    }

    /**
     * @return which environment the game is running in.
     */
    public static CoreSide getEnvironmentSide(){
        return CoreSide.fromUnderlying(FMLEnvironment.dist);
    }

    /**
     * Checks whether a mod with the given modid is loaded and active.
     */
    public static boolean isModLoaded(String modid){
        return ModList.get().isLoaded(modid);
    }

    /**
     * Opens the given container. This method will do nothing if called client-side.
     * @param container the container to be opened
     */
    public static void openContainer(BaseContainer container){
        Player player = container.player;
        if(!(container.player instanceof ServerPlayer))
            return;

        // Open the container
        //noinspection unchecked,rawtypes
        NetworkHooks.openGui((ServerPlayer)player, new MenuProvider() {
            @Override
            public Component getDisplayName(){
                return TextComponents.empty().get();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player){
                container.setContainerId(windowId);
                return container;
            }
        }, data -> ((BaseContainerType)container.getContainerType()).writeContainer(container, data));
    }

    /**
     * Closes the currently open container for the given player. If the player is not in a container, this method won't do anything.
     */
    public static void closeContainer(Player player){
        player.closeContainer();
    }
}
