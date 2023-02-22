package com.supermartijn642.core;

import com.supermartijn642.core.gui.BaseContainer;
import com.supermartijn642.core.gui.BaseContainerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 20/03/2022 by SuperMartijn642
 */
public class CommonUtils {

    private static MinecraftServer server;

    static void initialize(){
        MinecraftForge.EVENT_BUS.addListener((Consumer<FMLServerAboutToStartEvent>)(e -> server = e.getServer()));
        MinecraftForge.EVENT_BUS.addListener((Consumer<FMLServerStoppedEvent>)(e -> server = null));
    }

    /**
     * @return the integrated server on the client or the server instance a dedicated server
     */
    public static MinecraftServer getServer(){
        return server;
    }

    public static World getLevel(RegistryKey<World> resourceKey){
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
        PlayerEntity player = container.player;
        if(!(container.player instanceof ServerPlayerEntity))
            return;

        // Open the container
        //noinspection unchecked,rawtypes
        NetworkHooks.openGui((ServerPlayerEntity)player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName(){
                return TextComponents.empty().get();
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player){
                container.setContainerId(windowId);
                return container;
            }
        }, data -> ((BaseContainerType)container.getContainerType()).writeContainer(container, data));
    }

    /**
     * Closes the currently open container for the given player. If the player is not in a container, this method won't do anything.
     */
    public static void closeContainer(PlayerEntity player){
        player.closeContainer();
    }
}
