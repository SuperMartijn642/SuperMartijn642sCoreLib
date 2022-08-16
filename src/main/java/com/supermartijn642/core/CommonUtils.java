package com.supermartijn642.core;

import com.supermartijn642.core.gui.BaseContainerType;
import com.supermartijn642.core.registry.Registries;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

/**
 * Created 20/03/2022 by SuperMartijn642
 */
public class CommonUtils {

    private static MinecraftServer server;

    static void initialize(){
        ServerLifecycleEvents.SERVER_STARTING.register(server -> CommonUtils.server = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> CommonUtils.server = null);
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
        return CoreSide.fromUnderlying(FabricLoader.getInstance().getEnvironmentType());
    }

    /**
     * Checks whether a mod with the given modid is loaded and active.
     */
    public static boolean isModLoaded(String modid){
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    /**
     * Opens the given container for the given player. This method will do nothing if called client-side.
     * @param player            player to show the container to
     * @param baseContainerType type of the container, used to send the container's data to the client
     * @param container         the container to be opened
     */
    public <T extends AbstractContainerMenu> void openContainer(Player player, BaseContainerType<T> baseContainerType, T container){
        if(!(player instanceof ServerPlayer))
            return;

        ContainerProviderRegistry.INSTANCE.openContainer(Registries.MENU_TYPES.getIdentifier(baseContainerType), (ServerPlayer)player, data -> baseContainerType.writeContainer(container, data));
    }
}
