package com.supermartijn642.core;

import com.supermartijn642.core.gui.BaseContainer;
import com.supermartijn642.core.gui.BaseContainerType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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
     * Opens the given container. This method will do nothing if called client-side.
     * @param container the container to be opened
     */
    public static void openContainer(BaseContainer container){
        Player player = container.player;
        if(!(player instanceof ServerPlayer))
            return;

        // Open the container
        BaseContainerType<?> containerType = container.getContainerType();
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf data){
                //noinspection unchecked,rawtypes
                ((BaseContainerType)containerType).writeContainer(container, data);
            }

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
        });
    }
}
